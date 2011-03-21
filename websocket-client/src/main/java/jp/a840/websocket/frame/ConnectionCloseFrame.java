package jp.a840.websocket.frame;

public class ConnectionCloseFrame extends Frame {

	protected ConnectionCloseFrame(FrameHeader header, byte[] bodyData) {
		super(header, bodyData);
	}

}
