package jp.a840.websocket.frame.draft76;

import java.nio.ByteBuffer;

import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.FrameHeader;

/**
 * WebSocket Frame class
 *
 * Frame (Draft76)
 *
 * Support WebSocket Draft76 specification.
 *
 * @author t-hashimoto
 *
 */
abstract public class FrameDraft76 extends Frame {
	protected FrameDraft76(){
	}
	
	protected FrameDraft76(FrameHeader header, byte[] body){
		super(header, body);
	}

}
