package jp.a840.websocket.frame;

public class PingFrame extends Frame {

	public PingFrame(FrameHeader header, byte[] bodyData) {
		super(header, bodyData);
	}

}
