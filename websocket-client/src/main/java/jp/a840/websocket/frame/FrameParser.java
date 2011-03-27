package jp.a840.websocket.frame;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import jp.a840.websocket.WebSocketException;

public abstract class FrameParser {

	enum State {
		HEADER, FRAME, DONE;
		
		private static EnumMap<State, EnumSet<State>> stateMap = new EnumMap<State, EnumSet<State>>(State.class);
		static {
			stateMap.put(HEADER,   EnumSet.of(State.HEADER, State.FRAME));
			stateMap.put(FRAME,    EnumSet.of(State.FRAME, State.DONE));
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
	
	private static ByteBuffer nullBuffer = ByteBuffer.allocate(0);
	
//	private ByteBuffer fragmentBuf;
	
	ByteBuffer bodyBuf;
	
	public void init(){
		state = State.HEADER;
//		fragmentBuf = nullBuffer;
		bodyBuf = null;
	}

//	protected void getBuffer(ByteBuffer buffer, byte[] buf, int offset, int length){
//		if (fragmentBuf.remaining() > offset + length) {
//			fragmentBuf.get(buf, offset, length);
//		} else if (fragmentBuf.remaining() < offset) {
//			buffer.get(buf, offset - fragmentBuf.remaining(), length);
//		} else {
//			fragmentBuf.get(buf, offset, fragmentBuf.remaining() - offset);
//			int len = length - (fragmentBuf.remaining() - offset);
//			byte[] tmp = new byte[len];
//			buffer.get(tmp, 0, len);
//		}
//	}
	
	public Frame parse(ByteBuffer buffer) throws WebSocketException {
		FrameHeader header = null;
//		if (fragmentBuf.remaining() > 0) {
//			int len = fragmentBuf.remaining() + buffer.remaining();
//			byte[] buf = new byte[len];
//			getBuffer(buffer, buf, 0, len);
//			buffer = ByteBuffer.wrap(buf);
//		}

		header = createFrameHeader(buffer);
		if (header == null) {
			return null;
		}
		if (header.getBodyLength() - 1 > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("large data is not support yet");
		}
		bodyBuf = ByteBuffer.allocate((int) (header.getBodyLength()));
		transitionTo(State.FRAME);

		int currentLimit = buffer.limit();
		buffer.limit(buffer.position()
				+ Math.min(bodyBuf.remaining(), buffer.remaining()));
		bodyBuf.put(buffer);
		buffer.limit(currentLimit);

		if (!bodyBuf.hasRemaining()) {
			Frame frame = createFrame(header, bodyBuf.array());
			transitionTo(State.DONE);
			return frame;
		}else{
			buffer.flip();
//			fragmentBuf = buffer;
//			fragmentBuf.flip();
		}
		return null;
	}

	abstract protected FrameHeader createFrameHeader(ByteBuffer chunkData);
	abstract protected Frame createFrame(FrameHeader h, byte[] bodyData);
}
