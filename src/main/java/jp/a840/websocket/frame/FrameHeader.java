package jp.a840.websocket.frame;

import java.nio.ByteBuffer;

public interface FrameHeader {
	public long getFrameLength();
	public long getBodyLength();
	public ByteBuffer toByteBuffer();
}
