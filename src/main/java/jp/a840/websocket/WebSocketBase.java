/*
 * The MIT License
 * 
 * Copyright (c) 2011 Takahiro Hashimoto
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jp.a840.websocket;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.a840.websocket.auth.Authenticator;
import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.FrameParser;
import jp.a840.websocket.handler.PacketDumpStreamHandler;
import jp.a840.websocket.handler.SSLStreamHandler;
import jp.a840.websocket.handler.StreamHandlerAdapter;
import jp.a840.websocket.handler.StreamHandlerChain;
import jp.a840.websocket.handler.WebSocketPipeline;
import jp.a840.websocket.handler.WebSocketStreamHandler;
import jp.a840.websocket.handshake.Handshake;
import jp.a840.websocket.handshake.ProxyHandshake;
import jp.a840.websocket.handshake.SSLHandshake;
import jp.a840.websocket.proxy.Proxy;
import jp.a840.websocket.util.StringUtil;


/**
 * The WebSocket base client.
 *
 * @author t-hashimoto
 */
abstract public class WebSocketBase implements WebSocket {
	
	/** The log. */
	private static Logger log = Logger.getLogger(WebSocketBase.class
			.getName());

	/** The location. */
	protected URI location;

	/** the URL to which to connect. */
	protected String path;

	/** The use ssl. */
	protected boolean useSsl = false;
	
	/** The ssl handshake. */
	protected SSLHandshake sslHandshake;
	
	/** endpoint. */
	protected InetSocketAddress endpointAddress;

	/** proxy. */
	protected Proxy proxy;
	
	/** connection timeout(second). */
	protected int connectionTimeout = 60 * 1000;

	/** connection read timeout(second). */
	protected int connectionReadTimeout = 0;
	
	/** blocking mode. */
	private boolean blockingMode = true;

	/** The packet dump mode. */
	private static int packetDumpMode;
	
	/** quit flag. */
	volatile protected boolean quit;

	/** subprotocol name array. */
	protected String[] protocols;

	/** The server protocols. */
	protected String[] serverProtocols;

	/** The buffer size. */
	protected int bufferSize;
	
	/** The upstream buffer. */
	protected ByteBuffer upstreamBuffer;

	/** The downstream buffer. */
	protected ByteBuffer downstreamBuffer;

	/** The origin. */
	protected String origin;

	/** The upstream queue. */
	protected BlockingQueue<ByteBuffer> upstreamQueue;

	/** websocket handler. */
	protected WebSocketHandler handler;

	/** The pipeline. */
	protected WebSocketPipeline pipeline;

	/** The socket. */
	protected SocketChannel socket;

	/** The selector. */
	protected Selector selector;

	/** The handshake. */
	private Handshake handshake;

	/** The frame parser. */
	private FrameParser frameParser;

	/** The response header map. */
	protected HttpHeader responseHeader;
	
	/** The request header map. */
	protected HttpHeader requestHeader;

	/** The response status. */
	protected int responseStatus;

	/** The state. */
	volatile protected State state = State.CLOSED;

	/** The close latch. */
	protected CountDownLatch closeLatch;
    
	/** The handshake latch. */
	protected CountDownLatch handshakeLatch;
	
	/** worker. */
	protected ExecutorService executorService;

	/**
	 * Instantiates a new web socket base.
	 *
	 * @param url the url
	 * @param handler the handler
	 * @param protocols the protocols
	 * @throws WebSocketException the web socket exception
	 */
	public WebSocketBase(String url, WebSocketHandler handler,
			String... protocols) throws WebSocketException {
		this.protocols = protocols;
		this.handler = handler;

		init(url);
	}
	
	/**
	 * Instantiates a new web socket base.
	 *
	 * @param url the url
	 * @param proxy the proxy
	 * @param handler the handler
	 * @param protocols the protocols
	 * @throws WebSocketException the web socket exception
	 */
	public WebSocketBase(String url, Proxy proxy, WebSocketHandler handler,
			String... protocols) throws WebSocketException {
		this.protocols = protocols;
		this.handler = handler;
		this.proxy = proxy;
		init(url);
	}
	
	/**
	 * Inits the.
	 *
	 * @param url the url
	 * @throws WebSocketException the web socket exception
	 */
	protected void init(String url) throws WebSocketException {
		// init properties
		initializeProperties();
		parseUrl(url);
		// parse url
		initializePipeline();
	}
	
	/**
	 * Initialize properties.
	 *
	 * @throws WebSocketException the web socket exception
	 */
	protected void initializeProperties() throws WebSocketException {
		this.origin = System.getProperty("websocket.origin");
		this.bufferSize = Integer.getInteger("websocket.bufferSize",0x7FFF);
		int upstreamQueueSize = Integer.getInteger("websocket.upstreamQueueSize", 500);
		this.upstreamQueue = new LinkedBlockingQueue<ByteBuffer>(upstreamQueueSize);
		this.downstreamBuffer = ByteBuffer.allocate(this.bufferSize);
		this.upstreamBuffer = ByteBuffer.allocate(this.bufferSize);
		this.packetDumpMode = Integer.getInteger("websocket.packatdump", 0);
	}

	/**
	 * Initialize pipeline.
	 *
	 * @throws WebSocketException the web socket exception
	 */
	protected void initializePipeline() throws WebSocketException {
		// setup pipeline
		this.pipeline = new WebSocketPipeline();
		
		// Add upstream qeueue handler first.
		// it push the upstream buffer to a sendqueue and then wakeup a selector
		this.pipeline.addStreamHandler(new StreamHandlerAdapter() {
			public void nextUpstreamHandler(WebSocket ws, ByteBuffer buffer,
					Frame frame, StreamHandlerChain chain) throws WebSocketException {
				if(!upstreamQueue.offer(buffer)){
					throw new WebSocketException(3030, "Couldn't add buffer to upstream queue");
				}
				selector.wakeup();
			}
			public void nextHandshakeUpstreamHandler(WebSocket ws, ByteBuffer buffer,
					StreamHandlerChain chain) throws WebSocketException {
				if(!upstreamQueue.offer(buffer)){
					throw new WebSocketException(3031, "Couldn't add buffer to upstream queue");
				}
				selector.wakeup();
			}
		});
		
		if(this.useSsl){
			this.sslHandshake = new SSLHandshake(this.endpointAddress);
			this.pipeline.addStreamHandler(new PacketDumpStreamHandler());
			this.pipeline.addStreamHandler(new SSLStreamHandler(this.sslHandshake, this.bufferSize));
		}
		
		// orverriding initilize method by subclass
		initializePipeline(pipeline);
	}
	
	/**
	 * Initialize pipeline.
	 *
	 * @param pipeline the pipeline
	 * @throws WebSocketException the web socket exception
	 */
	protected void initializePipeline(WebSocketPipeline pipeline) throws WebSocketException {
		// for debug
		this.pipeline.addStreamHandler(new PacketDumpStreamHandler());
		this.pipeline.addStreamHandler(new WebSocketStreamHandler(getHandshake(), getFrameParser()));
	}
	
	/**
	 * Parses the url.
	 *
	 * @param urlStr the url str
	 * @throws WebSocketException the web socket exception
	 */
	private void parseUrl(String urlStr) throws WebSocketException {
		try {
			URI uri = new URI(urlStr);
			if (!(uri.getScheme().equals("ws") || uri.getScheme().equals("wss"))) {
				throw new WebSocketException(3007, "Not supported protocol. "
						+ uri.toString());
			}
			if(uri.getScheme().equals("wss")){
				useSsl = true;
			}
			path = uri.getPath();
			int port = uri.getPort();
			if (port < 0) {
				if (uri.getScheme().equals("ws")) {
					port = 80;
				} else if (uri.getScheme().equals("wss")) {
					port = 443;
					useSsl = true;
				} else {
					throw new WebSocketException(3008,
							"Not supported protocol. " + uri.toString());
				}
			}
			endpointAddress = new InetSocketAddress(uri.getHost(), port);
			location = uri;
		} catch (URISyntaxException e) {
			throw new WebSocketException(3009, e);
		}
	}

	/* (non-Javadoc)
	 * @see jp.a840.websocket.WebSocket#getLocation()
	 */
	public URI getLocation() {
		return location;
	}

	/* (non-Javadoc)
	 * @see jp.a840.websocket.WebSocket#send(jp.a840.websocket.frame.Frame)
	 */
	public void send(Frame frame) throws WebSocketException {
		if(!isConnected()){
			throw new WebSocketException(3010, "WebSocket is not connected");
		}
		pipeline.sendUpstream(this, null, frame);
	}

	/**
	 * Send.
	 *
	 * @param obj the obj
	 * @throws WebSocketException the web socket exception
	 */
	public void send(Object obj) throws WebSocketException {
		send(createFrame(obj));
	}

	/**
	 * Send.
	 *
	 * @param str the str
	 * @throws WebSocketException the web socket exception
	 */
	public void send(String str) throws WebSocketException {
		send(createFrame(str));
	}

	/**
	 * <pre>
	 * CONNECTED -> HANDSHAKE, CLOSED
	 * HANDSHAKE -> WAIT, CLOSED
	 * WAIT -> WAIT, CLOSED
	 * CLOSED -> CONNECTED, CLOSED
	 * </pre>.
	 *
	 * @author Takahiro Hashimoto
	 */
	enum State {
		
		/** The CONNECTED. */
		CONNECTED, 
		/** The HANDSHAKE. */
		HANDSHAKE, 
		/** The WAIT. */
		WAIT, 
		/** The CLOSING. */
		CLOSING,
		/** The CLOSED. */
		CLOSED;

		/** The state map. */
		private static EnumMap<State, EnumSet<State>> stateMap = new EnumMap<State, EnumSet<State>>(
				State.class);
		static {
			stateMap.put(CONNECTED, EnumSet.of(State.HANDSHAKE, State.CLOSED));
			stateMap.put(HANDSHAKE, EnumSet.of(State.WAIT, State.CLOSED));
			stateMap.put(WAIT, EnumSet.of(State.WAIT, State.CLOSING, State.CLOSED));
			stateMap.put(CLOSING, EnumSet.of(State.CLOSED));
			stateMap.put(CLOSED, EnumSet.of(State.CONNECTED, State.CLOSED));
		}

		/**
		 * Can transition to.
		 *
		 * @param state the state
		 * @return true, if successful
		 */
		boolean canTransitionTo(State state) {
			EnumSet<State> set = stateMap.get(this);
			if (set == null)
				return false;
			return set.contains(state);
		}
		
		/**
		 * Checks if is connected.
		 *
		 * @return true, if is connected
		 */
		boolean isConnected(){
			switch(this){
			case CONNECTED:
			case HANDSHAKE:
			case WAIT:
				return true;
			}
			return false;
		}
	}

	/**
	 * Transition to.
	 *
	 * @param to the to
	 * @return the state
	 */
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

	/**
	 * State.
	 *
	 * @return the state
	 */
	protected State state() {
		return state;
	}

	/**
	 * Read.
	 *
	 * @param socket the socket
	 * @param buffer the buffer
	 * @throws WebSocketException the web socket exception
	 */
	protected void read(SocketChannel socket, ByteBuffer buffer)
			throws WebSocketException {
		try {
			buffer.clear();
			if (socket.read(buffer) < 0) {
				throw new WebSocketException(3020, "Connection closed.");
			}
			buffer.flip();
		} catch (IOException ioe) {
			throw new WebSocketException(3021, "Caught IOException.", ioe);
		}
	}

	/**
	 * Creates the socket.
	 *
	 * @return the socket channel
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected SocketChannel createSocket() throws IOException {
		SocketChannel socket = SocketChannel.open();
		socket.configureBlocking(false);
		return socket;
	}
	
	/* (non-Javadoc)
	 * @see jp.a840.websocket.WebSocket#connect()
	 */
	public void connect() throws WebSocketException {
		try {
			// check connection status
			if (isConnected()){
				throw new WebSocketException(3039, "Already connected");
			}
			
			if (!state.canTransitionTo(State.CONNECTED)) {
				throw new WebSocketException(3040,
						"Can't transit state to CONNECTED. current state="
								+ state);
			}
			
			// initialize connection
			handshakeLatch = new CountDownLatch(1);
			closeLatch = new CountDownLatch(1);
			
			ProxyHandshake proxyHandshake = null;
			if(proxy != null){
				proxyHandshake = this.proxy.getProxyHandshake(this);
			}
			
			socket = SocketChannel.open();
			socket.configureBlocking(false);
			selector = Selector.open();
			socket.register(selector, OP_READ);

			long start = System.currentTimeMillis();
			InetSocketAddress remoteAddress = this.endpointAddress;
			if(proxyHandshake != null){
				remoteAddress = proxyHandshake.getProxyAddress();
			}
			
			// start connect to remote address
			if (socket.connect(remoteAddress)) {
				throw new WebSocketException(3041, "Already connected");
			}
			while (!socket.finishConnect()) {
				if ((System.currentTimeMillis() - start) > connectionTimeout) {
					throw new WebSocketException(3042, "Connection Timeout");
				}
			}
			// connect done
			// try handshakes			
			transitionTo(State.CONNECTED);
			if(proxyHandshake != null){
				proxyHandshake.doHandshake(socket);
			}
			
			if(useSsl){
				sslHandshake.doHandshake(socket);
			}
			pipeline.sendHandshakeUpstream(this, null); // send handshake request
//			socket.write(upstreamQueue.take());

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
									socket.register(selector, OP_READ);
								} else if (key.isValid() && key.isReadable()) {
									read(socket, downstreamBuffer); // read
									// response
									switch (state) {
									case HANDSHAKE: // CONNECTED -> HANDSHAKE
										pipeline.sendHandshakeDownstream(WebSocketBase.this, downstreamBuffer);
										if (getHandshake().isDone()){
											processBuffer(downstreamBuffer);
											handshakeLatch.countDown();
										}
										break;
									case WAIT: // read frames
									case CLOSING:
										processBuffer(downstreamBuffer);
										break;
									}
								}
							}
							if(!upstreamQueue.isEmpty()){
								if(state != State.CLOSED){
									socket.register(selector, OP_READ | OP_WRITE);
								} else {
									socket.register(selector, OP_WRITE);									
								}
							} else {
								if(state == State.CLOSED){
									quit = true;
								}
							}
						}
					} catch (WebSocketException we) {
						handler.onError(WebSocketBase.this, we);
					} catch (Exception e) {
						handler.onError(WebSocketBase.this,
								new WebSocketException(3043, e));
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
						handler.onClose(WebSocketBase.this);
						handshakeLatch.countDown();
						closeLatch.countDown();
						synchronized (this) {
							if(executorService != null){
								executorService.shutdown();
							}
						}
					}
				}
			};

			quit = false;
			if (blockingMode) {
				worker.run();
			} else {
				executorService = Executors
						.newSingleThreadExecutor();
				executorService.submit(worker);
				executorService.shutdown();
				handshakeLatch.await();
			}

		} catch (WebSocketException we) {
			handler.onError(this, we);
		} catch (Exception e) {
			handler.onError(this, new WebSocketException(3044, e));
		}
	}

	/**
	 * Process buffer.
	 *
	 * @param buffer the buffer
	 * @throws WebSocketException the web socket exception
	 */
	protected void processBuffer(ByteBuffer buffer) throws WebSocketException {
		while (buffer.hasRemaining()) {
			pipeline.sendDownstream(this, buffer, null);
		}
		return;
	}

	/* (non-Javadoc)
	 * @see jp.a840.websocket.WebSocket#isConnected()
	 */
	public boolean isConnected() {
		return state.isConnected();
	}
	
	/* (non-Javadoc)
	 * @see jp.a840.websocket.WebSocket#close()
	 */
	public void close() {
		try {
			if(state == State.WAIT){
				closeWebSocket();
				selector.wakeup();
//				if(state == State.CLOSING){
					try{
						closeLatch.await(30, TimeUnit.SECONDS);
					}catch(InterruptedException e){
						;
					}
//				}
				if(executorService != null){
					try{
						executorService.shutdown();
						executorService.awaitTermination(30, TimeUnit.SECONDS);
					} catch(InterruptedException e){
						;
					} finally {
						synchronized (this) {
							executorService = null;							
						}
					}
				}
			}
		} catch (WebSocketException e) {
			handler.onError(this, e);
		}
	}
	
	/**
	 * Await termination.
	 *
	 * @param timeout the timeout
	 * @param unit the unit
	 * @throws InterruptedException the interrupted exception
	 */
	public void awaitTermination(int timeout, TimeUnit unit) throws InterruptedException {
		if(executorService != null){
			executorService.awaitTermination(timeout, unit);
		}
	}
	
	/**
	 * Quit.
	 */
	protected void quit(){
		quit = true;
	}
	
	/**
	 * Close web socket.
	 *
	 * @throws WebSocketException the web socket exception
	 */
	protected void closeWebSocket() throws WebSocketException {
		transitionTo(State.CLOSED);
	}

	/* (non-Javadoc)
	 * @see jp.a840.websocket.WebSocket#createFrame(java.lang.Object)
	 */
	abstract public Frame createFrame(Object obj) throws WebSocketException;

	/* (non-Javadoc)
	 * @see jp.a840.websocket.WebSocket#createFrame(java.lang.String)
	 */
	abstract public Frame createFrame(String str) throws WebSocketException;

	/**
	 * Join.
	 *
	 * @param delim the delim
	 * @param collections the collections
	 * @return the string
	 */
	protected static String join(String delim, Collection<String> collections) {
		return StringUtil.join(delim, collections);
	}

	/**
	 * Join.
	 *
	 * @param delim the delim
	 * @param strings the strings
	 * @return the string
	 */
	protected static String join(String delim, String... strings) {
		return StringUtil.join(delim, strings);
	}

	/**
	 * Join.
	 *
	 * @param delim the delim
	 * @param start the start
	 * @param end the end
	 * @param strings the strings
	 * @return the string
	 */
	protected static String join(String delim, int start, int end,
			String... strings) {
		return StringUtil.join(delim, start, end, strings);
	}

	/**
	 * Adds the header.
	 *
	 * @param sb the sb
	 * @param key the key
	 * @param value the value
	 */
	protected static void addHeader(StringBuilder sb, String key, String value) {
		StringUtil.addHeader(sb, key, value);
	}

	/**
	 * Gets the web socket version.
	 *
	 * @return the web socket version
	 */
	abstract protected int getWebSocketVersion();

	/**
	 * New handshake instance.
	 *
	 * @return the handshake
	 */
	abstract protected Handshake newHandshakeInstance();

	/**
	 * Gets the handshake.
	 *
	 * @return the handshake
	 */
	protected synchronized Handshake getHandshake() {
		if (handshake == null) {
			handshake = newHandshakeInstance();
		}
		return handshake;
	}

	/**
	 * New frame parser instance.
	 *
	 * @return the frame parser
	 */
	abstract protected FrameParser newFrameParserInstance();

	/**
	 * Gets the frame parser.
	 *
	 * @return the frame parser
	 */
	protected synchronized FrameParser getFrameParser() {
		if (frameParser == null) {
			frameParser = newFrameParserInstance();
		}
		return frameParser;
	}

	/* (non-Javadoc)
	 * @see jp.a840.websocket.WebSocket#isBlockingMode()
	 */
	public boolean isBlockingMode() {
		return blockingMode;
	}

	/* (non-Javadoc)
	 * @see jp.a840.websocket.WebSocket#setBlockingMode(boolean)
	 */
	public void setBlockingMode(boolean blockingMode) {
		this.blockingMode = blockingMode;
	}

	/**
	 * Gets the server protocols.
	 *
	 * @return the server protocols
	 */
	public String[] getServerProtocols() {
		return serverProtocols;
	}

	/**
	 * Sets the server protocols.
	 *
	 * @param serverProtocols the new server protocols
	 */
	public void setServerProtocols(String[] serverProtocols) {
		this.serverProtocols = serverProtocols;
	}

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see jp.a840.websocket.WebSocket#getEndpoint()
	 */
	public InetSocketAddress getEndpoint() {
		return endpointAddress;
	}

	/**
	 * Gets the protocols.
	 *
	 * @return the protocols
	 */
	public String[] getProtocols() {
		return protocols;
	}

	/**
	 * Gets the origin.
	 *
	 * @return the origin
	 */
	public String getOrigin() {
		return origin;
	}

	/**
	 * Gets the response header.
	 *
	 * @return the response header
	 */
	public HttpHeader getResponseHeader() {
		return responseHeader;
	}

	/**
	 * Gets the request header.
	 *
	 * @return the request header
	 */
	public HttpHeader getRequestHeader() {
		return requestHeader;
	}

	/**
	 * Gets the response status.
	 *
	 * @return the response status
	 */
	public int getResponseStatus() {
		return responseStatus;
	}

	/* (non-Javadoc)
	 * @see jp.a840.websocket.WebSocket#getConnectionTimeout()
	 */
	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	/* (non-Javadoc)
	 * @see jp.a840.websocket.WebSocket#setConnectionTimeout(int)
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout * 1000;
	}

	/**
	 * Gets the connection read timeout.
	 *
	 * @return the connection read timeout
	 */
	public int getConnectionReadTimeout() {
		return connectionReadTimeout;
	}

	/**
	 * Sets the connection read timeout.
	 *
	 * @param connectionReadTimeout the new connection read timeout
	 */
	public void setConnectionReadTimeout(int connectionReadTimeout) {
		this.connectionReadTimeout = connectionReadTimeout * 1000;
	}

	/**
	 * Sets the origin.
	 *
	 * @param origin the new origin
	 */
	public void setOrigin(String origin) {
		this.origin = origin;
	}

	/* (non-Javadoc)
	 * @see jp.a840.websocket.WebSocket#getBufferSize()
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * Gets the packet dump mode.
	 *
	 * @return the packet dump mode
	 */
	public static int getPacketDumpMode() {
		return packetDumpMode;
	}

	/**
	 * Sets the packet dump mode.
	 *
	 * @param packetDumpMode the new packet dump mode
	 */
	public static void setPacketDumpMode(int packetDumpMode) {
		WebSocketBase.packetDumpMode = packetDumpMode;
	}

}
