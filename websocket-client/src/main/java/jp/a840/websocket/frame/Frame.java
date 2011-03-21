package jp.a840.websocket.frame;

import java.nio.ByteBuffer;

import jp.a840.websocket.frame.FrameHeader.Opcode;

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
abstract public class Frame {

	protected FrameHeader header;
	protected byte[] body;
	
	protected Frame(FrameHeader header, byte[] body){
		this.header = header;
		this.body = body;
	}

	public long getPayloadLength(){
		return header.getPayloadLength();
	}
	
	public boolean isFragmented(){
		return header.isFragmented();
	}

	public Opcode getOpcode(){
		return header.getOpcode();
	}
	
	public ByteBuffer toByteBuffer(){
		return ByteBuffer.wrap(body);
	}
}
