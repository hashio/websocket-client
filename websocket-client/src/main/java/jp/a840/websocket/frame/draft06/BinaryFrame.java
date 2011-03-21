package jp.a840.websocket.frame.draft06;

import java.nio.ByteBuffer;

import jp.a840.websocket.frame.FrameHeader;

public class BinaryFrame extends FrameDraft06 {

	public BinaryFrame(byte[] bodyData){
		super(bodyData);
	}
	
	protected BinaryFrame(FrameHeader header, byte[] bodyData) {
		super(header, bodyData);
	}
}
