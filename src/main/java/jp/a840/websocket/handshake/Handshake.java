package jp.a840.websocket.handshake;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.a840.websocket.BufferManager;
import jp.a840.websocket.WebSocketException;

/**
 * handshake
 * 
 * <pre>
 * Sample (Draft06)
 * client => server
 * GET /chat HTTP/1.1
 * Host: server.example.com
 * Upgrade: websocket
 * Connection: Upgrade
 * Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
 * Sec-WebSocket-Origin: http://example.com
 * Sec-WebSocket-Protocol: chat, superchat
 * Sec-WebSocket-Version:6
 * 
 * server => client
 * HTTP/1.1 101 Switching Protocols
 * Upgrade: websocket
 * Connection: Upgrade
 * Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=
 * Sec-WebSocket-Protocol: chat
 * </pre>
 */
public abstract class Handshake {
	private static Logger logger = Logger.getLogger(Handshake.class
			.getName());

	private int responseStatus;	

	private Map<String, String> responseHeaderMap;

	enum State {
		METHOD, HEADER, BODY, DONE;
		
		private static EnumMap<State, EnumSet<State>> stateMap = new EnumMap<State, EnumSet<State>>(State.class);
		static {
			stateMap.put(METHOD,   EnumSet.of(State.HEADER));
			stateMap.put(HEADER,    EnumSet.of(State.BODY, State.DONE));
			stateMap.put(BODY,    EnumSet.of(State.DONE));
			stateMap.put(DONE,     EnumSet.of(State.METHOD));
		}
		
		boolean canTransitionTo(State state){
			EnumSet<State> set = stateMap.get(this);
			if(set == null) return false;
			return set.contains(state);
		}
	}
	
	protected State transitionTo(State to){
		if(state.canTransitionTo(to)){
			State old = state;
			state = to;
			return old;
		}else{
			throw new IllegalStateException("Couldn't transtion from " + state + " to " + to);
		}
	}
	
	volatile private State state = State.DONE;
	
	protected State state(){
		return state;
	}

	protected BufferManager bufferManager = new BufferManager();
	
	public void handshake(SocketChannel socket) throws WebSocketException {
		try {
			ByteBuffer request = createHandshakeRequest();
			socket.write(request);
		} catch (IOException ioe) {
			throw new WebSocketException(3100, ioe);
		}
	}

	final public boolean handshakeResponse(ByteBuffer downloadBuffer) throws WebSocketException {
		ByteBuffer buffer = null;
		try{
			if (State.DONE.equals(state)) {
				transitionTo(State.METHOD);
				responseStatus = -1;
				responseHeaderMap = new HashMap<String, String>();
				bufferManager.init();
				buffer = downloadBuffer;
			} else {
				buffer = bufferManager.getBuffer(downloadBuffer);
			}

			if (State.METHOD.equals(state) || State.HEADER.equals(state)) {
				int position = buffer.position();
				if (!parseHandshakeResponseHeader(buffer)) {
					buffer.position(position);
					bufferManager.storeFragmentBuffer(buffer);
					return false;
				}
				transitionTo(State.BODY);
			}

			if (State.BODY.equals(state)) {
				int position = buffer.position();
				if (!parseHandshakeResponseBody(buffer)) {
					buffer.position(position);
					bufferManager.storeFragmentBuffer(buffer);
					return false;
				}
			}

			return done();
		}finally{
			if(buffer != null && buffer != downloadBuffer){
				downloadBuffer.position(downloadBuffer.limit() - buffer.remaining());
			}
		}
	}

	protected boolean done(){
		transitionTo(State.DONE);
		return true;
	}
	
	public boolean isDone(){
		return State.DONE.equals(state);
	}
		
	protected boolean parseHandshakeResponseBody(ByteBuffer buffer) throws WebSocketException {
		return true;
	}
	
	protected boolean parseHandshakeResponseHeader(ByteBuffer buffer)
			throws WebSocketException {

		if (State.METHOD.equals(state)) {
			// METHOD
			// HTTP/1.1 101 Switching Protocols
			String line = readLine(buffer);
			if(line == null){
				return false;
			}
			if (!line.startsWith("HTTP/1.1")) {
				throw new WebSocketException(3101,
						"Invalid server response.(HTTP version) " + line);
			}
			responseStatus = Integer.valueOf(line.substring(9, 12));
			if (responseStatus != 101) {
				throw new WebSocketException(3102,
						"Invalid server response.(Status Code) " + line);
			}
			transitionTo(State.HEADER);
		}

		if (State.HEADER.equals(state)) {
			// header lines
			do {
				String line = readLine(buffer);
				if(line == null){
					return false;
				}
				if (line.indexOf(':') > 0) {
					String[] keyValue = line.split(":", 2);
					if (keyValue.length > 1) {
						responseHeaderMap.put(keyValue[0].trim().toLowerCase(),
								keyValue[1].trim().toLowerCase());
					}
				}
				if ("\r\n".compareTo(line) == 0) {
					return true;
				}
				if (!buffer.hasRemaining()) {
					return false;
				}
			} while (true);
		}

		return true;
	}

	protected String readLine(ByteBuffer buf) {
		boolean completed = false;
		buf.mark();
		while (buf.hasRemaining() && !completed) {
			byte b = buf.get();
			if (b == '\r') {
				if(buf.hasRemaining() && buf.get() == '\n'){
					completed = true;
				}
			}
		}

		if(!completed){
			return null;
		}

		int limit = buf.position();
		buf.reset();
		int length = limit - buf.position();
		byte[] tmp = new byte[length];
		buf.get(tmp, 0, length);
		try {
			String line = new String(tmp, "US-ASCII");
			if (logger.isLoggable(Level.FINE)) {
				logger.fine(line.trim());
			}
			return line;
		} catch (UnsupportedEncodingException e) {
			;
		}
		return null;
	}

	abstract public ByteBuffer createHandshakeRequest()
			throws WebSocketException;

	public int getResponseStatus() {
		return responseStatus;
	}

	public Map<String, String> getResponseHeaderMap() {
		return responseHeaderMap;
	} 
}
