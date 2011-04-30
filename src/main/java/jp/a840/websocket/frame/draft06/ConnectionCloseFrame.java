package jp.a840.websocket.frame.draft06;


public class ConnectionCloseFrame extends FrameDraft06 {

	protected ConnectionCloseFrame(FrameHeaderDraft06 header, byte[] bodyData) {
		super(header, bodyData);
	}

}
