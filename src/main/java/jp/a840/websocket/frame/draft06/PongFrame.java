package jp.a840.websocket.frame.draft06;

import jp.a840.websocket.frame.FrameHeader;

public class PongFrame extends FrameDraft06 {

	public PongFrame(FrameHeader header, byte[] bodyData) {
		super(header, bodyData);
	}

}
