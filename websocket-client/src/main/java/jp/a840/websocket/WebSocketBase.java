package jp.a840.websocket;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.FrameParser;
import jp.a840.websocket.handler.PacketDumpStreamHandler;
import jp.a840.websocket.handler.StreamHandlerAdapter;
import jp.a840.websocket.handler.StreamHandlerChain;
import jp.a840.websocket.handler.WebSocketPipeline;
import jp.a840.websocket.handler.WebSocketStreamHandler;
import jp.a840.websocket.handshake.Handshake;

/**
 * A websocket base client
 * 
 * @author t-hashimoto
 * 
 */
abstract public class WebSocketBase implements WebSocket {
	private static Logger logger = Logger.getLogger(WebSocketBase.class
			.getName());

	protected URI location;

	/** the URL to which to connect */
	protected String path;

	/** endpoint */
	protected InetSocketAddress endpoint;

	/** connection timeout(second) */
	private int connectionTimeout = 60 * 1000;

	private int connectionReadTimeout = 0;
	
	private int packatdumpMode;

	/** blocking mode */
	private boolean blockingMode = true;

	/** quit flag */
	private volatile boolean quit;

	/** subprotocol name array */
	protected String[] protocols;

	protected String[] serverProtocols;

	protected ByteBuffer downstreamBuffer;

	protected String origin;

	protected BlockingQueue<ByteBuffer> upstreamQueue = new LinkedBlockingQueue<ByteBuffer>();

	/** websocket handler */
	protected WebSocketHandler handler;

	protected WebSocketPipeline pipeline;

	protected SocketChannel socket;

	protected Selector selector;

	private Handshake handshake;

	private FrameParser frameParser;

	protected Map<String, String> responseHeaderMap;
	protected Map<String, String> requestHeaderMap = new HashMap<String, String>();

	protected int responseStatus;

	volatile private State state = State.CLOSED;

	public WebSocketBase(String url, WebSocketHandler handler,
			String... protocols) throws URISyntaxException, IOException {
		this.protocols = protocols;
		this.handler = handler;

		// init properties
		this.origin = System.getProperty("websocket.origin");

		int downstreamBufferSize = Integer.getInteger("websocket.buffersize",
				8192);
		this.downstreamBuffer = ByteBuffer.allocate(downstreamBufferSize);
		this.packatdumpMode = Integer.getInteger("websocket.packatdump", 0);

		// parse url
		parseUrl(url);

		// setup pipeline
		this.pipeline = new WebSocketPipeline();
		
		// Add upstream qeueue handler first.
		// it push the upstream buffer to a sendqueue and then wakeup a selector if needed
		this.pipeline.addStreamHandler(new StreamHandlerAdapter() {
			public void nextUpstreamHandler(WebSocket ws, ByteBuffer buffer,
					Frame frame, StreamHandlerChain chain) throws WebSocketException {
				try {
					upstreamQueue.put(buffer);
					selector.wakeup();
				} catch (InterruptedException e) {
					throw new WebSocketException(3011, e);
				}
			}
			public void nextHandshakeUpstreamHandler(WebSocket ws, ByteBuffer buffer,
					StreamHandlerChain chain) throws WebSocketException {
				try{
					upstreamQueue.put(buffer);
				} catch (InterruptedException e) {
					throw new WebSocketException(3012, e);
				}
			}
		});
		
		// for debug
		if(packatdumpMode > 0){
			this.pipeline.addStreamHandler(new PacketDumpStreamHandler(packatdumpMode));
		}
		
		// orverriding initilize method by subclass
		initializePipeline(pipeline);
		
		// Add base response handler
		this.pipeline.addStreamHandler(new StreamHandlerAdapter() {
			public void nextDownstreamHandler(WebSocket ws, ByteBuffer buffer,
					Frame frame, StreamHandlerChain chain) throws WebSocketException {
				WebSocketBase.this.handler.onMessage(ws, frame);
			}

			public void nextHandshakeDownstreamHandler(WebSocket ws, ByteBuffer buffer,
					StreamHandlerChain chain) throws WebSocketException {
				// set response status
				responseHeaderMap = getHandshake()
						.getResponseHeaderMap();
				responseStatus = getHandshake()
						.getResponseStatus();
				transitionTo(State.WAIT);
				// HANDSHAKE -> WAIT
				WebSocketBase.this.handler.onOpen(WebSocketBase.this);
			}
		});

	}

	protected void initializePipeline(WebSocketPipeline pipeline) {
		this.pipeline.addStreamHandler(new WebSocketStreamHandler(getHandshake(), getFrameParser()));
	}

	private void parseUrl(String urlStr) throws URISyntaxException {
		URI uri = new URI(urlStr);
		if (!(uri.getScheme().equals("ws") || uri.getScheme().equals("wss"))) {
			throw new IllegalArgumentException("Not supported protocol. "
					+ uri.toString());
		}
		path = uri.getPath();
		int port = uri.getPort();
		if (port < 0) {
			if (uri.getScheme().equals("ws")) {
				port = 80;
			} else if (uri.getScheme().equals("wss")) {
				port = 443;
			} else {
				throw new IllegalArgumentException("Not supported protocol. "
						+ uri.toString());
			}
		}
		endpoint = new InetSocketAddress(uri.getHost(), port);
		location = uri;
	}

	public void send(Frame frame) throws WebSocketException {
		if(!isConnected()){
			throw new WebSocketException(3800, "WebSocket is not connected");
		}
		pipeline.sendUpstream(this, null, frame);
	}

	public void send(Object obj) throws WebSocketException {
		send(createFrame(obj));
	}

	public void send(String str) throws WebSocketException {
		send(createFrame(str));
	}

	/**
	 * <pre>
	 * CONNECTED -> HANDSHAKE, CLOSED
	 * HANDSHAKE -> WAIT, CLOSED
	 * WAIT -> WAIT, CLOSED
	 * CLOSED -> CONNECTED, CLOSED
	 * </pre>
	 */
	enum State {
		CONNECTED, HANDSHAKE, WAIT, CLOSED;

		private static EnumMap<State, EnumSet<State>> stateMap = new EnumMap<State, EnumSet<State>>(
				State.class);
		static {
			stateMap.put(CONNECTED, EnumSet.of(State.HANDSHAKE, State.CLOSED));
			stateMap.put(HANDSHAKE, EnumSet.of(State.WAIT, State.CLOSED));
			stateMap.put(WAIT, EnumSet.of(State.WAIT, State.CLOSED));
			stateMap.put(CLOSED, EnumSet.of(State.CONNECTED, State.CLOSED));
		}

		boolean canTransitionTo(State state) {
			EnumSet<State> set = stateMap.get(this);
			if (set == null)
				return false;
			return set.contains(state);
		}
	}

	protected State transitionTo(State to) {
		if (state.canTransitionTo(to)) {
			State old = state;
			state = to;
			return old;
		} else {
			throw new IllegalStateException("Couldn't transtion from " + state
					+ " to " + to);
		}
	}

	protected State state() {
		return state;
	}

	protected void read(SocketChannel socket, ByteBuffer buffer)
			throws WebSocketException {
		try {
			buffer.clear();
			if (socket.read(buffer) < 0) {
				throw new WebSocketException(3001, "Connection closed.");
			}
			buffer.flip();
		} catch (IOException ioe) {
			throw new WebSocketException(3002, "Caught IOException.", ioe);
		}
	}

	public void connect() throws WebSocketException {
		try {
			if (!state.canTransitionTo(State.CONNECTED)) {
				throw new WebSocketException(3000,
						"Can't transit state to CONNECTED. current state="
								+ state);
			}
			
			URI proxyUri = new URI("http", null, location.getHost(), location.getPort(), null,null,null);
			List<Proxy> proxyList = ProxySelector.getDefault().select(proxyUri);
			proxyList.get(0).address();

			socket = SocketChannel.open();
			socket.configureBlocking(false);
			selector = Selector.open();
			socket.register(selector, OP_READ | OP_WRITE);

			long start = System.currentTimeMillis();
			if (socket.connect(endpoint)) {
				throw new WebSocketException(3000, "Already connected");
			}
			while (!socket.finishConnect()) {
				if ((System.currentTimeMillis() - start) > connectionTimeout) {
					throw new WebSocketException(3004, "Connection Timeout");
				}
			}

			transitionTo(State.CONNECTED);
			
			pipeline.sendHandshakeUpstream(this, null); // send handshake request
			socket.write(upstreamQueue.take());

			transitionTo(State.HANDSHAKE);

			Runnable worker = new Runnable() {
				public void run() {
					try {
						while (!quit) {
							selector.select(connectionReadTimeout);
							for (SelectionKey key : selector.selectedKeys()) {
								if (key.isValid() && key.isWritable() && upstreamQueue.peek() != null) {
									SocketChannel channel = (SocketChannel) key
											.channel();
									channel.write(upstreamQueue.poll());
								} else if (key.isValid() && key.isReadable()) {
									read(socket, downstreamBuffer); // read
									// response
									switch (state) {
									case HANDSHAKE: // CONNECTED -> HANDSHAKE
										pipeline.sendHandshakeDownstream(WebSocketBase.this, downstreamBuffer);
										if (getHandshake().isDone()){
											if (downstreamBuffer.hasRemaining()) {
												processBuffer(downstreamBuffer);
											}
										}
										break;
									case WAIT: // read frames
										processBuffer(downstreamBuffer);
										break;
									case CLOSED:
										break;
									}
								}
							}
						}
					} catch (WebSocketException we) {
						handler.onError(WebSocketBase.this, we);
					} catch (Exception e) {
						handler.onError(WebSocketBase.this,
								new WebSocketException(3000, e));
					} finally {
						try {
							socket.close();
						} catch (IOException e) {
							;
						}
						try {
							selector.close();
						} catch (IOException e) {
							;
						}
					}
				}
			};

			quit = false;
			if (blockingMode) {
				worker.run();
			} else {
				ExecutorService executorService = Executors
						.newCachedThreadPool();
				executorService.submit(worker);
			}

		} catch (WebSocketException we) {
			handler.onError(this, we);
		} catch (Exception e) {
			handler.onError(this, new WebSocketException(3100, e));
		}
	}

	protected void processBuffer(ByteBuffer buffer) throws WebSocketException {
		while (buffer.hasRemaining()) {
			pipeline.sendDownstream(this, buffer, null);
		}
		return;
	}

	public boolean isConnected() {
		return socket.isConnected();
	}

	public void close() {
		try {
			quit = true;
			selector.wakeup();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Caught exception.", e);
		} finally {
			handler.onClose(this);
		}
	}

	abstract public Frame createFrame(Object obj) throws WebSocketException;

	abstract public Frame createFrame(String str) throws WebSocketException;

	protected static String join(String delim, Collection<String> collections) {
		String[] values = new String[collections.size()];
		collections.toArray(values);
		return join(delim, 0, collections.size(), values);
	}

	protected static String join(String delim, String... strings) {
		return join(delim, 0, strings.length, strings);
	}

	protected static String join(String delim, int start, int end,
			String... strings) {
		if (strings.length == 1) {
			return strings[0];
		}
		StringBuilder sb = new StringBuilder(strings[start]);
		for (int i = start + 1; i < end; i++) {
			sb.append(delim).append(strings[i]);
		}
		return sb.toString();
	}

	protected static void addHeader(StringBuilder sb, String key, String value) {
		// TODO need folding?
		sb.append(key + ": " + value + "\r\n");
	}

	abstract protected int getWebSocketVersion();

	abstract protected Handshake newHandshakeInstance();

	protected synchronized Handshake getHandshake() {
		if (handshake == null) {
			handshake = newHandshakeInstance();
		}
		return handshake;
	}

	abstract protected FrameParser newFrameParserInstance();

	protected synchronized FrameParser getFrameParser() {
		if (frameParser == null) {
			frameParser = newFrameParserInstance();
		}
		return frameParser;
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

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout * 1000;
	}

	public int getConnectionReadTimeout() {
		return connectionReadTimeout;
	}

	public void setConnectionReadTimeout(int connectionReadTimeout) {
		this.connectionReadTimeout = connectionReadTimeout * 1000;
	}
}
