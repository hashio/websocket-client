package jp.a840.websocket.frame.draft76;

import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.FrameHeader;
import jp.a840.websocket.frame.draft06.BinaryFrame;
import jp.a840.websocket.frame.draft06.TextFrame;

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

	public static BinaryFrame createBinaryFrame(byte[] body){
		return new BinaryFrame(body);
	}
	
	public static TextFrame createTextFrame(String str){
		return new TextFrame(str);
	}
}
