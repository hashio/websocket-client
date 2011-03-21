package jp.a840.websocket;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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
	
	protected URI location;

	/** the URL to which to connect */
	protected String path;

	/** endpoint */
	protected InetSocketAddress endpoint;
	
	/** connection timeout(second) */
	private int connectionTimeout = 60;

	/** blocking mode */
	private boolean blockingMode = true;
	
	/** quit flag */
	private volatile boolean quit;
	
	/** subprotocol name array */
	protected String[] protocols;
	
	protected String[] serverProtocols;
	
	protected ByteBuffer downstreamBuffer;

	protected String origin;	
	
	protected BlockingQueue<Frame> upstreamQueue = new LinkedBlockingQueue<Frame>();
	
	/** websocket handler */
	protected WebSocketHandler handler;

	protected WebSocketPipeline pipeline;
	
	protected SocketChannel socket;
	
	protected Selector selector;
	
	private ExecutorService executorService = Executors.newCachedThreadPool();
	
	protected Map<String, String> responseHeaderMap = new HashMap<String, String>();
	protected Map<String, String> requestHeaderMap = new HashMap<String, String>();
	
	protected int responseStatus;
	
	public WebSocketBase(String url, WebSocketHandler handler, String... protocols) throws URISyntaxException, IOException {
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
	
	private void parseUrl(String urlStr) throws URISyntaxException {
		URI uri = new URI(urlStr);
		if(!(uri.getScheme().equals("ws")
				|| uri.getScheme().equals("wss"))){
			throw new IllegalArgumentException("Not supported protocol. " + uri.toString());
		}
		path = uri.getPath();
		int port = uri.getPort();
		if(port < 0){
			if(uri.getScheme().equals("ws")){
				port = 80;
			}else if(uri.getScheme().equals("wss")){
				port = 443;
			}else{
				throw new IllegalArgumentException("Not supported protocol. " + uri.toString());
			}
		}
		endpoint = new InetSocketAddress(uri.getHost(), port);
		location = uri;
	}
	
	public void send(Frame frame) throws WebSocketException {
		try{
			upstreamQueue.add(frame);
			socket.register(selector, OP_READ | OP_WRITE);
		}catch(ClosedChannelException e){
			throw new WebSocketException(3010);
		}
	}
	
	public void send(Object obj) throws WebSocketException {
		send(createFrame(obj));
	}

	public void send(String str) throws WebSocketException {
		send(createFrame(str));		
	}
	
	public void connect() throws WebSocketException {
		try {
			socket = SocketChannel.open();
			socket.configureBlocking(false);		
			selector = Selector.open();
			socket.register(selector, SelectionKey.OP_READ);

			final AtomicReference<WebSocketException> exceptionHolder = new AtomicReference<WebSocketException>();
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
						exceptionHolder.set(we);
					} catch (Exception e) {
						exceptionHolder.set(new WebSocketException(3100, e));
					}
				}
			});
			future.get(connectionTimeout, TimeUnit.SECONDS);

			if(exceptionHolder.get() != null){
				// has error in handshake
				throw exceptionHolder.get();
			}
			
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
										if(quit){
											break;
										}
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
					}finally{
						try{
							socket.close();
						}catch(IOException e){
							;
						}
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
		} catch (WebSocketException e) {
			throw e;
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

	protected void handshakeResponse(ByteBuffer buffer)
			throws WebSocketException {
		String line = readLine(downstreamBuffer);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(line);
		}
		if (!line.startsWith("HTTP/1.1")) {
			throw new WebSocketException(3001,
					"Invalid server response.(HTTP version) " + line);
		}
		responseStatus = Integer.valueOf(line.substring(9, 12));
		if (responseStatus != 101) {
			throw new WebSocketException(3001,
					"Invalid server response.(Status Code) " + line);
		}

		// header lines
		do {
			line = readLine(downstreamBuffer);
			if (logger.isLoggable(Level.FINE)) {
				logger.fine(line);
			}
			if (line.indexOf(':') > 0) {
				String[] keyValue = line.split(":", 2);
				if (keyValue.length > 1) {
					responseHeaderMap.put(keyValue[0].trim().toLowerCase(),
							keyValue[1].trim().toLowerCase());
				}
			}
		} while ("\r\n".compareTo(line) != 0);
	}

	abstract protected ByteBuffer createHandshakeRequest() throws WebSocketException;
	
	abstract protected Frame createFrame(Object obj) throws WebSocketException;
	
	abstract protected Frame createFrame(String str) throws WebSocketException;
	
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
			String line = new String(tmp, "US-ASCII");
			if(logger.isLoggable(Level.FINE)){
				logger.fine(line.trim());
			}
			return line;
		}catch(UnsupportedEncodingException e){
			return null;
		}
	}
	
	protected static String join(String delim, Collection<String>collections){
		String[] values = new String[collections.size()];
		collections.toArray(values);
		return join(delim, 0, collections.size(), values);
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
		// TODO need folding?
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

	public String[] getServerProtocols() {
		return serverProtocols;
	}

	public void setServerProtocols(String[] serverProtocols) {
		this.serverProtocols = serverProtocols;
	}

	public String getPath() {
		return path;
	}

	public InetSocketAddress getEndpoint() {
		return endpoint;
	}

	public String[] getProtocols() {
		return protocols;
	}

	public String getOrigin() {
		return origin;
	}

	public Map<String, String> getResponseHeaderMap() {
		return responseHeaderMap;
	}

	public Map<String, String> getRequestHeaderMap() {
		return requestHeaderMap;
	}

	public int getResponseStatus() {
		return responseStatus;
	} 
}
