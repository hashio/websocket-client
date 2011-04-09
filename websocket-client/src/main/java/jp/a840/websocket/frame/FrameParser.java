package jp.a840.websocket.frame;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.EnumSet;

import jp.a840.websocket.BufferManager;
import jp.a840.websocket.WebSocketException;

public abstract class FrameParser {

	enum State {
		HEADER, FRAME, DONE;
		
		private static EnumMap<State, EnumSet<State>> stateMap = new EnumMap<State, EnumSet<State>>(State.class);
		static {
			stateMap.put(HEADER,   EnumSet.of(State.FRAME));
			stateMap.put(FRAME,    EnumSet.of(State.DONE));
			stateMap.put(DONE,     EnumSet.of(State.HEADER));
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
	
	private FrameHeader header;

	private BufferManager bufferManager = new BufferManager();
	
	public Frame parse(ByteBuffer downloadBuffer) throws WebSocketException {
		ByteBuffer buffer = null;
		try {
			if (State.DONE.equals(state)) {
				transitionTo(State.HEADER);
				buffer = downloadBuffer;
			} else {
				buffer = bufferManager.getBuffer(downloadBuffer);
			}

			if (State.HEADER.equals(state)) {
				int position = buffer.position();
				header = createFrameHeader(buffer);
				if (header == null) {
					buffer.position(position);
					bufferManager.storeFragmentBuffer(buffer);
					return null;
				}
				if (header.getBodyLength() - 1 > Integer.MAX_VALUE) {
					throw new IllegalArgumentException(
							"large data is not support yet");
				}
				transitionTo(State.FRAME);
			}

			if (State.FRAME.equals(state)) {
				if (header.getBodyLength() > buffer.remaining()) {
					bufferManager.storeFragmentBuffer(buffer);
					return null;
				}

				byte[] bodyBuf = new byte[(int) header.getBodyLength()];
				buffer.get(bodyBuf, 0, bodyBuf.length);
				Frame frame = createFrame(header, bodyBuf);
				transitionTo(State.DONE);
				bufferManager.init();

				header = null;
				return frame;
			}
			return null;
		} finally {
			if (buffer != null && buffer != downloadBuffer) {
				downloadBuffer.position(downloadBuffer.limit()
						- buffer.remaining());
			}
		}
	}

	abstract protected FrameHeader createFrameHeader(ByteBuffer chunkData);
	abstract protected Frame createFrame(FrameHeader h, byte[] bodyData);
}
