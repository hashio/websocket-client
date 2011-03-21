package jp.a840.websocket;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.handler.WebSocketPipeline;
import jp.a840.websocket.handler.WebSocketStreamHandler;


/**
 * A simple websocket client
 * @author t-hashimoto
 *
 */
abstract public class WebSocketBase implements WebSocket {
	private static Logger logger = Logger.getLogger(WebSocketBase.class.getName());
	
	/** the URL to which to connect */
	protected String path;
	
	protected InetSocketAddress endpoint;
	
	private int connectionTimeout = 60;

	private boolean blockingMode = true;
	
	private volatile boolean quit;
	
	/** subprotocol name array */
	protected String[] protocols;
	
	protected ByteBuffer downstreamBuffer;
	protected int interval;
	protected String origin;	
	
	protected BlockingQueue<Frame> upstreamQueue = new LinkedBlockingQueue<Frame>();
	
	protected WebSocketHandler handler;
	protected WebSocketPipeline pipeline;
	
	protected SocketChannel socket;
	
	protected Selector selector;
	
	private ExecutorService executorService = Executors.newCachedThreadPool();
	
	protected Map<String, String> responseHeaderMap = new HashMap<String, String>();
	protected Map<String, String> requestHeaderMap = new HashMap<String, String>();
	
	public WebSocketBase(String url, WebSocketHandler handler, String... protocols) throws MalformedURLException, IOException {
		this.protocols = protocols;
		this.handler = handler;
		
		parseUrl(url);
		
		this.pipeline = new WebSocketPipeline();
		this.pipeline.addStreamHandler(new WebSocketStreamHandler(handler));
		initializePipeline(pipeline);
		
		this.origin = System.getProperty("websocket.origin");
		
		int downstreamBufferSize = Integer.getInteger("websocket.buffersize", 8192);
		this.downstreamBuffer = ByteBuffer.allocate(downstreamBufferSize);
	}
	
	protected void initializePipeline(WebSocketPipeline pipeline){
	}
	
	private void parseUrl(String urlStr) throws MalformedURLException {
		if(!urlStr.startsWith("ws://")){
			throw new IllegalArgumentException("No supported protocol. " + urlStr);
		}
		String[] pathchunk = urlStr.split("/+");
		String[] host = pathchunk[1].split(":");
		path = "/" + join("/", 2, pathchunk.length, pathchunk);
		endpoint = new InetSocketAddress(host[0], (host.length == 2 ? Integer.valueOf(host[1]) : 80));
	}
	
	public void send(Frame frame) throws WebSocketException {
		try{
			upstreamQueue.add(frame);
			socket.register(selector, OP_READ | OP_WRITE);
		}catch(ClosedChannelException e){
			throw new WebSocketException(3010);
		}
	}

	public void connect() throws WebSocketException {
		try {
			socket = SocketChannel.open();
			socket.configureBlocking(false);		
			selector = Selector.open();
			socket.register(selector, SelectionKey.OP_READ);

			Future future = executorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						socket.connect(endpoint);
						while (!socket.finishConnect())
							;

						handshake(socket);
						handler.onOpen(WebSocketBase.this);
					} catch (WebSocketException we) {
						handler.onError(WebSocketBase.this, we);
					} catch (Exception e) {
						handler.onError(WebSocketBase.this,
								new WebSocketException(3100, e));
					}
				}
			});
			future.get(connectionTimeout, TimeUnit.SECONDS);

			Runnable worker = new Runnable() {
				
				@Override
				public void run() {
					try {
						socket.register(selector, SelectionKey.OP_READ);
						while (!quit) {
							selector.select();
							for (SelectionKey key : selector.selectedKeys()) {
								if (key.isValid() && key.isWritable()) {
									SocketChannel channel = (SocketChannel) key
											.channel();
									channel.write(upstreamQueue.poll()
											.toByteBuffer());
								} else if (key.isValid() && key.isReadable()) {
									try {
										List<Frame> frameList = new ArrayList<Frame>();
										downstreamBuffer.clear();
										if (socket.read(downstreamBuffer) < 0) {
											throw new WebSocketException(3001,
													"Connection closed.");
										}
										downstreamBuffer.flip();
										readFrame(frameList, downstreamBuffer);
										for (Frame frame : frameList) {
											pipeline.sendDownstream(
													WebSocketBase.this, frame);
										}
									} catch (IOException ioe) {
										handler.onError(WebSocketBase.this,
												new WebSocketException(3000,
														ioe));
									}

								}
							}
						}
					} catch (Exception e) {
						handler.onError(WebSocketBase.this,
								new WebSocketException(3900, e));
					}
				}
			};

			quit = false;
			if(blockingMode){
				worker.run();
			}else{
				ExecutorService executorService = Executors.newCachedThreadPool();
				executorService.submit(worker);
			}
		} catch (Exception e) {
			throw new WebSocketException(3200, e);
		}
	}
	
	public boolean isConnected(){
		return socket.isConnected();
	}
	
	public void close(){
		try {
			quit = true;
			selector.wakeup();
			socket.close();
		}catch(Exception e){
			logger.log(Level.WARNING, "Caught exception.", e);
		}finally{
			handler.onClose(this);
		}
	}
	
	/**
	 * handshake
	 * 
	 * Sample (Draft06)
	 * client => server
	 *   GET /chat HTTP/1.1
	 *   Host: server.example.com
	 *   Upgrade: websocket
	 *   Connection: Upgrade
	 *   Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
	 *   Sec-WebSocket-Origin: http://example.com
	 *   Sec-WebSocket-Protocol: chat, superchat
	 *   Sec-WebSocket-Version:6
	 *   
	 * server => client
	 *   HTTP/1.1 101 Switching Protocols
	 *   Upgrade: websocket
	 *   Connection: Upgrade
	 *   Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=
	 *   Sec-WebSocket-Protocol: chat
	 *   
	 * @param socket
	 */
	private void handshake(SocketChannel socket) throws WebSocketException {
		try{
			ByteBuffer request = createHandshakeRequest();
			
			socket.register(selector, SelectionKey.OP_READ);
			socket.write(request);

			// Response from server
			while(selector.select() > 0);

			for(SelectionKey key : selector.selectedKeys()){
				if(!(key.isValid() && key.isReadable())){
					throw new WebSocketException(3001, "Not readable state on socket.");
				}
				
				downstreamBuffer.clear();
				if (socket.read(downstreamBuffer) < 0) {
					throw new WebSocketException(3001, "Connection closed.");
				}
				downstreamBuffer.flip();

				handshakeResponse(downstreamBuffer);
			}			
		}catch(IOException ioe){
			throw new WebSocketException(3000, ioe);
		}
	}

	abstract protected ByteBuffer createHandshakeRequest() throws WebSocketException;
	abstract protected void handshakeResponse(ByteBuffer buffer) throws WebSocketException;
	
	protected static String readLine(ByteBuffer buf){
		int position = buf.position();
		int limit = buf.limit() - buf.position();
		int i = 0;
		for(; i < limit; i++){
			if(buf.get(position + i) == '\r'){
				if(buf.get(position + i + 1) == '\n'){
					i++;
					break;
				}
			}
			if(buf.get(position + i) == '\n'){
				break;
			}
		}
		byte[] tmp = new byte[i + 1];
		buf.get(tmp);
		try{
			return new String(tmp, "US-ASCII");
		}catch(UnsupportedEncodingException e){
			return null;
		}
	}
	
	protected static String join(String delim, String... strings){
		return join(delim, 0, strings.length, strings);
	}
	
	protected static String join(String delim, int start, int end, String... strings){
		if(strings.length == 1){
			return strings[0];
		}
		StringBuilder sb = new StringBuilder(strings[start]);
		for(int i = start + 1; i < end; i++){
			sb.append(delim).append(strings[i]);
		}
		return sb.toString();
	}
	
	protected static void addHeader(StringBuilder sb, String key, String value){
		sb.append(key + ": " + value + "\r\n");
	}

	abstract protected void readFrame(List<Frame> frameList, ByteBuffer buffer) throws IOException;
	abstract protected int getWebSocketVersion();

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public boolean isBlockingMode() {
		return blockingMode;
	}

	public void setBlockingMode(boolean blockingMode) {
		this.blockingMode = blockingMode;
	} 
}
