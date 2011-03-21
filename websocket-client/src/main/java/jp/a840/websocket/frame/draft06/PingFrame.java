package jp.a840.websocket.frame.draft06;


public class PingFrame extends FrameDraft06 {

	public PingFrame(FrameHeaderDraft06 header, byte[] bodyData) {
		super(header, bodyData);
	}

}
