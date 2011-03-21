package jp.a840.websocket.frame;

public class BinaryFrame extends Frame {

	public BinaryFrame(FrameHeader header, byte[] bodyData) {
		super(header, bodyData);
	}

}
