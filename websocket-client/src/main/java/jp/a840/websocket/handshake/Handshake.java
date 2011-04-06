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

	private StringBuilder lineBuf;

	private int responseStatus;	

	private Map<String, String> responseHeaderMap;

	enum State {
		METHOD, HEADER, BODY, DONE;
		
		private static EnumMap<State, EnumSet<State>> stateMap = new EnumMap<State, EnumSet<State>>(State.class);
		static {
			stateMap.put(METHOD,   EnumSet.of(State.METHOD, State.HEADER));
			stateMap.put(HEADER,    EnumSet.of(State.HEADER, State.BODY, State.DONE));
			stateMap.put(BODY,    EnumSet.of(State.BODY, State.DONE));
			stateMap.put(DONE,     EnumSet.of(State.DONE));
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
	
	volatile private State state;
	
	protected State state(){
		return state;
	}

	public void init(){
		responseStatus = -1;
		lineBuf = new StringBuilder();
		responseHeaderMap = new HashMap<String, String>();
		state = State.METHOD;
	}

	public void handshake(SocketChannel socket) throws WebSocketException {
		try {
			
			ByteBuffer request = createHandshakeRequest();
			socket.write(request);
		} catch (IOException ioe) {
			throw new WebSocketException(3000, ioe);
		}
	}

	final public boolean handshakeResponse(ByteBuffer buffer) throws WebSocketException {
		if(!parseHandshakeResponseHeader(buffer)){
			return false;
		}
		transitionTo(State.BODY);
		if(!parseHandshakeResponseBody(buffer)){
			return false;
		}
		return done();
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
				throw new WebSocketException(3001,
						"Invalid server response.(HTTP version) " + line);
			}
			responseStatus = Integer.valueOf(line.substring(9, 12));
			if (responseStatus != 101) {
				throw new WebSocketException(3001,
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

	protected String readLine(ByteBuffer buffer){
		boolean complete = readLine(lineBuf, buffer);
		if (!complete) {
			return null;
		}
		String line = lineBuf.toString();
		lineBuf = new StringBuilder();

		if (logger.isLoggable(Level.FINE)) {
			logger.fine(line);
		}
		return line;
	}
	
	protected boolean readLine(StringBuilder sb, ByteBuffer buf) {
		int position = buf.position();
		int limit = buf.limit() - buf.position();
		int i = 0;
		boolean completed = false;
		for (; i < limit; i++) {
			if (buf.get(position + i) == '\r') {
				if(buf.get(position + i + 1) == '\n'){
					i++;
					completed = true;
					break;
				}
			}
			if (buf.get(position + i) == '\n') {
				completed = true;
				break;
			}
		}

		byte[] tmp = new byte[i + 1];
		buf.get(tmp);
		try {
			String line = new String(tmp, "US-ASCII");
			if (logger.isLoggable(Level.FINE)) {
				logger.fine(line.trim());
			}
			sb.append(line);
		} catch (UnsupportedEncodingException e) {
			;
		}
		return completed;
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
