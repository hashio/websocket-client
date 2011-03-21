package jp.a840.websocket.frame.draft06;

import java.nio.ByteBuffer;

import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.FrameHeader;

/**
 *  WebSocket Frame class
 *
 *  Frame (Draft06)
 *
 *    0               1               2               3
 *    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *   +-+-+-+-+-------+-+-------------+-------------------------------+
 *   |F|R|R|R| opcode|R| Payload len |    Extended payload length    |
 *   |I|S|S|S|  (4)  |S|     (7)     |             (16/63)           |
 *   |N|V|V|V|       |V|             |   (if payload len==126/127)   |
 *   | |1|2|3|       |4|             |                               |
 *   +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
 *   |     Extended payload length continued, if payload len == 127  |
 *   + - - - - - - - - - - - - - - - +-------------------------------+
 *   |                               |         Extension data        |
 *   +-------------------------------+ - - - - - - - - - - - - - - - +
 *   :                                                               :
 *   +---------------------------------------------------------------+
 *   :                       Application data                        :
 *   +---------------------------------------------------------------+
 *
 * payload length = extention data length + application data length.
 * the extention data length may be zero.
 *
 * Support WebSocket Draft06 specification.
 *
 * @author t-hashimoto
 *
 */
abstract public class FrameDraft06 extends Frame {
	
	private FrameBuilderDraft06 builder = new FrameBuilderDraft06();
	
	protected FrameDraft06(){
	}

	protected FrameDraft06(byte[] bodyData){
		super();
		FrameHeader header = builder.createFrameHeader(ByteBuffer.wrap(bodyData));
		setHeader(header);
		setBody(bodyData);
	}

	protected FrameDraft06(FrameHeader header, byte[] bodyData){
		super(header, bodyData);
	}
	
	public ByteBuffer toByteBuffer(){
		ByteBuffer headerBuffer = header.toByteBuffer();
		ByteBuffer buf = ByteBuffer.allocate(headerBuffer.limit() + body.length);
		buf.put(headerBuffer);
		buf.put(body);
		return buf;
	}
}
