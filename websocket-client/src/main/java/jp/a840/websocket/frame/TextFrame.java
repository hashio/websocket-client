package jp.a840.websocket.frame;

public class TextFrame extends Frame {

	public TextFrame(FrameHeader header, byte[] bodyData) {
		super(header, bodyData);
	}

}
