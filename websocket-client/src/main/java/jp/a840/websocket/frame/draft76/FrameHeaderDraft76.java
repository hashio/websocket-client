package jp.a840.websocket.frame.draft76;

import java.nio.ByteBuffer;

import jp.a840.websocket.frame.FrameHeader;

public class FrameHeaderDraft76 implements FrameHeader {
	protected final long bodyLength;

	protected byte frameType;
	
	protected final int headerLength = 1;
	
	public FrameHeaderDraft76(byte frameType, long payloadLength) {
		this.bodyLength = payloadLength;
		this.frameType = frameType;
	}
	
	public long getFrameLength(){
		return headerLength + bodyLength;
	}
	
	public int getHeaderLength(){
		return headerLength;
	}
	
	public long getBodyLength(){
		return bodyLength;
	}

	public byte getFrameType() {
		return frameType;
	}
	
	public ByteBuffer toByteBuffer(){
		ByteBuffer buf = ByteBuffer.allocate(1);
		buf.put(frameType);
		return buf;
	}
}
