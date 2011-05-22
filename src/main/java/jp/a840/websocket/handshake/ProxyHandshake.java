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
package jp.a840.websocket.handshake;

import static java.nio.channels.SelectionKey.OP_READ;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.logging.Logger;

import jp.a840.websocket.BufferManager;
import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.proxy.ProxyCredentials;
import jp.a840.websocket.util.PacketDumpUtil;
import jp.a840.websocket.util.StringUtil;

/**
 * HTTP Proxy Handshake class
 * 
 * <pre>
 * client => server
 * CONNECT ws.example.com HTTP/1.1
 * Host: ws.example.com
 * 
 * server => client
 * HTTP/1.1 200 Connection established
 * Proxy-agent:
 * </pre>
 *
 * @author Takahiro Hashimoto
 */
public class ProxyHandshake {
	
	/** The log. */
	private static Logger log = Logger.getLogger(ProxyHandshake.class
			.getName());

	/** The proxy. */
	private final InetSocketAddress proxyAddress;
	
	/** The origin. */
	private final InetSocketAddress originAddress;
	
	/** connection read timeout(second). */
	private int connectionReadTimeout = 0;
	
	private boolean needAuthorize;
	
	/** The credentials. */
	private ProxyCredentials credentials;
	
	/** The selector. */
	private Selector selector;
	
	private HttpResponseHeaderParser httpResponseHeaderParser;

	/**
	 * The Enum State.
	 */
	enum State {
		INIT,
		/** The METHOD. */
		METHOD, 
		/** The HEADER. */
		HEADER, 
		/** The BODY. */
		AUTH,
		/** The DONE. */
		DONE;
		
		/** The state map. */
		private static EnumMap<State, EnumSet<State>> stateMap = new EnumMap<State, EnumSet<State>>(State.class);
		static {
			stateMap.put(INIT,   EnumSet.of(State.METHOD));
			stateMap.put(METHOD,   EnumSet.of(State.HEADER));
			stateMap.put(HEADER,    EnumSet.of(State.AUTH, State.DONE));
			stateMap.put(AUTH,    EnumSet.of(State.METHOD));
			stateMap.put(DONE,     EnumSet.of(State.METHOD));
		}
		
		/**
		 * Can transition to.
		 *
		 * @param state the state
		 * @return true, if successful
		 */
		boolean canTransitionTo(State state){
			EnumSet<State> set = stateMap.get(this);
			if(set == null) return false;
			return set.contains(state);
		}
	}
	
	/**
	 * Transition to.
	 *
	 * @param to the to
	 * @return the state
	 */
	protected State transitionTo(State to){
		if(state.canTransitionTo(to)){
			State old = state;
			state = to;
			return old;
		}else{
			throw new IllegalStateException("Couldn't transtion from " + state + " to " + to);
		}
	}
	
	/** The state. */
	volatile private State state = State.INIT;
	
	/**
	 * State.
	 *
	 * @return the state
	 */
	protected State state(){
		return state;
	}
	
	/**
	 * Instantiates a new proxy handshake.
	 *
	 * @param origin the origin
	 */
	public ProxyHandshake(InetSocketAddress proxy, InetSocketAddress origin){
		this.proxyAddress = proxy;
		this.originAddress = origin;
	}

	public ProxyHandshake(InetSocketAddress proxy, InetSocketAddress origin, ProxyCredentials credentials){
		this.proxyAddress = proxy;
		this.originAddress = origin;
		this.credentials = credentials;
	}

	/**
	 * Do handshake.
	 *
	 * @param socket the socket
	 * @throws WebSocketException the web socket exception
	 */
	public void doHandshake(SocketChannel socket) throws WebSocketException {
		try {
			BufferManager bufferManager = new BufferManager();
			httpResponseHeaderParser = new HttpResponseHeaderParser();

			// create selector for Proxy handshake
			selector = Selector.open();
			socket.register(selector, OP_READ);

			ByteBuffer request = createHandshakeRequest();
			if(PacketDumpUtil.isDump(PacketDumpUtil.HS_UP)){
				PacketDumpUtil.printPacketDump("PROXY_HS_UP", request);
			}
			socket.write(request);
			transitionTo(State.METHOD);
			
			ByteBuffer responseBuffer = ByteBuffer.allocate(8192);
			String creadectialsStr = null;
			boolean completed = false;
			do {
				selector.select(connectionReadTimeout);

				responseBuffer.clear();
				socket.read(responseBuffer);
				responseBuffer.flip();
				if(PacketDumpUtil.isDump(PacketDumpUtil.HS_DOWN)){
					PacketDumpUtil.printPacketDump("PROXY_HS_DOWN", responseBuffer);
				}
				
				responseBuffer = bufferManager.getBuffer(responseBuffer);
				
				switch(state){
				case METHOD:
				case HEADER:
					needAuthorize = false;
					completed = parseHandshakeResponseHeader(responseBuffer);
					if(!completed){
						bufferManager.storeFragmentBuffer(responseBuffer);
					}else{
						if(needAuthorize){
							if(credentials == null){
								throw new WebSocketException(3999, "Need proxy authenticate. But not set a ProxyCredentials");
							}
							creadectialsStr = credentials.getCredentials(httpResponseHeaderParser.getResponseHeader());
							if(creadectialsStr != null){
								transitionTo(State.AUTH);				
							}else{
								throw new WebSocketException(3999, "Have not support proxy authenticate schemes.");
							}
						} else {
							transitionTo(State.DONE);
						}
					}
					break;
				case AUTH:
					ByteBuffer buffer = createAuthorizeRequest(creadectialsStr);
					if(PacketDumpUtil.isDump(PacketDumpUtil.HS_UP)){
						PacketDumpUtil.printPacketDump("PROXY_HS_UP", buffer);
					}
					socket.write(buffer);
					transitionTo(State.METHOD);
					break;
				}
			} while(!completed);
		} catch (IOException ioe) {
			throw new WebSocketException(3100, ioe);
		} finally {
			try{
				if(selector != null){
					selector.close();
				}
			}catch(IOException e){
				;
			}
		}
	}

	/**
	 * Creates the handshake request.
	 *
	 * @return the byte buffer
	 */
	public ByteBuffer createHandshakeRequest() {		
		// Send GET request to server
		StringBuilder sb = new StringBuilder();
		String host = originAddress.getHostName() + ":" + originAddress.getPort();
		sb.append("CONNECT " + host + " HTTP/1.1\r\n");
		StringUtil.addHeader(sb, "Host", host);
		sb.append("\r\n");

		try{
			return ByteBuffer.wrap(sb.toString().getBytes("US-ASCII"));
		}catch(UnsupportedEncodingException e){
			return null;
		}
	}

	public ByteBuffer createAuthorizeRequest(String creadectialsStr){
		// Send GET request to server
		StringBuilder sb = new StringBuilder();
		String host = originAddress.getHostName() + ":" + originAddress.getPort();
		sb.append("CONNECT " + host + " HTTP/1.1\r\n");
		StringUtil.addHeader(sb, "Host", host);
		StringUtil.addHeader(sb, "Proxy-Authorization", creadectialsStr);
		sb.append("\r\n");

		try{
			return ByteBuffer.wrap(sb.toString().getBytes("US-ASCII"));
		}catch(UnsupportedEncodingException e){
			return null;
		}
		
	}
	
	/**
	 * Parses the handshake response header.
	 *
	 * @param buffer the buffer
	 * @return true, if successful
	 * @throws WebSocketException the web socket exception
	 */
	protected boolean parseHandshakeResponseHeader(ByteBuffer buffer)
			throws WebSocketException {
		if (state == State.METHOD) {
			// METHOD
			// HTTP/1.1 101 Switching Protocols
			String line = StringUtil.readLine(buffer);
			if (line == null) {
				return false;
			}
			if (!(line.startsWith("HTTP/1.0") || line.startsWith("HTTP/1.1"))) {
				throw new WebSocketException(3101,
						"Invalid server response.(HTTP version) " + line);
			}
			int responseStatus = Integer.valueOf(line.substring(9, 12));
			if (responseStatus != 200) {
				if (responseStatus == 407) {
					needAuthorize = true;
				}else{
					throw new WebSocketException(3102,
							"Invalid server response.(Status Code) " + line);
				}
			}
			transitionTo(State.HEADER);
		}
		if (state == State.HEADER) {
			httpResponseHeaderParser.parse(buffer);
			return httpResponseHeaderParser.isCompleted();
		}
		return true;
	}

	public ProxyCredentials getCredentials() {
		return credentials;
	}

	public void setCredentials(ProxyCredentials credentials) {
		this.credentials = credentials;
	}

	public int getConnectionReadTimeout() {
		return connectionReadTimeout;
	}

	public void setConnectionReadTimeout(int connectionReadTimeout) {
		this.connectionReadTimeout = connectionReadTimeout;
	}

	public InetSocketAddress getProxyAddress() {
		return proxyAddress;
	}

	public InetSocketAddress getOriginAddress() {
		return originAddress;
	}

}
