package jp.a840.websocket.frame;

public class PongFrame extends Frame {

	public PongFrame(FrameHeader header, byte[] bodyData) {
		super(header, bodyData);
	}

}
