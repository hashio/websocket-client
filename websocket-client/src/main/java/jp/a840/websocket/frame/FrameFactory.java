package jp.a840.websocket.frame;

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
public class FrameFactory {
	private enum Opcode {
		CONNECTION_CLOSE(1),
		PING(2),
		PONG(3),
		TEXT_FRAME(4),
		BINARY_FRAME(5);
		// 0x6-F reserved
		
		private final int opcode;
		private Opcode(int opcode){
			this.opcode = opcode;
		}
		public int intValue(){
			return opcode;
		}
	}

	private static final int  FIN_MASK  = 1 << 7;
	private static final byte RSV1_MASK = 1 << 6;
	private static final byte RSV2_MASK = 1 << 5;
	private static final byte RSV3_MASK = 1 << 4;
	private static final byte OPCODE_MASK = 0xF;

	private static final int  RSV4_MASK  = 1 << 7;
	private static final int  PAYLOAD_LEN_MASK  = 0x7F;

	private byte[] frame;
	private int extendedPayloadLength;
	
	public FrameFactory(){
	}

	public boolean isBinaryFrame(){
		return Opcode.BINARY_FRAME.intValue() == getOpcode();
	}
	
	public int getOpcode(){
		return frame[0] & OPCODE_MASK;
	}
	
	private void parseFrame(){
		
	}
}
