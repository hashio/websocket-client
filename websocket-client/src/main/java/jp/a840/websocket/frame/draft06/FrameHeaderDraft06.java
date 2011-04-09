package jp.a840.websocket.frame.draft06;

import java.nio.ByteBuffer;

import jp.a840.websocket.frame.FrameHeader;


public class FrameHeaderDraft06 implements FrameHeader {
	protected enum Opcode {
		CONNECTION_CLOSE(1), PING(2), PONG(3), TEXT_FRAME(4), BINARY_FRAME(5);
		// 0x6-F reserved

		private final int opcode;

		private Opcode(int opcode) {
			this.opcode = opcode;
		}

		public int intValue() {
			return opcode;
		}

		public static Opcode valueOf(int opc) {
			switch (opc) {
			case 1:
				return CONNECTION_CLOSE;
			case 2:
				return PING;
			case 3:
				return PONG;
			case 4:
				return TEXT_FRAME;
			case 5:
				return BINARY_FRAME;
			default:
				return null;
			}
		}
	}
	
	protected enum Fin {
		MORE_FRAME(0), FINAL(1);
		private final int fin;

		private Fin(int fin) {
			this.fin = fin;
		}

		public int intValue() {
			return fin;
		}
	}

	protected enum Rsv {
		RESERVE(0);
		private final int rsv;

		private Rsv(int rsv) {
			this.rsv = rsv;
		}

		public int intValue() {
			return rsv;
		}
	}

	protected enum PayloadLengthType {
		LEN_SHORT(1), // 0x00 - 0x7D
		LEN_16(0x7E), // 0x0000 - 0xFFFF
		LEN_63(0x7F); // 0x0000000000000000 - 0x7FFFFFFFFFFFFFFF
		private final int payloadLengthType;

		private PayloadLengthType(int payloadLengthType) {
			this.payloadLengthType = payloadLengthType;
		}

		public int intValue() {
			return payloadLengthType;
		}

		public static PayloadLengthType valueOf(int plt) {
			switch (plt) {
			case 0x7E:
				return LEN_16;
			case 0x7F:
				return LEN_63;
			}
			if (0 <= plt && plt <= 0x7D) {
				return LEN_SHORT;
			}
			return null;
		}
	}

	protected static final int MAX_FRAME_LENGTH_16 = 0xFFFF;
	protected static final long MAX_FRAME_LENGTH_63 = 0x7FFFFFFFFFFFFFFFL;

	protected static final int FIN_MASK = 1 << 7;
	protected static final byte RSV1_MASK = 1 << 6;
	protected static final byte RSV2_MASK = 1 << 5;
	protected static final byte RSV3_MASK = 1 << 4;
	protected static final byte OPCODE_MASK = 0xF;

	protected static final int RSV4_MASK = 1 << 7;
	protected static final int PAYLOAD_LEN_MASK = 0x7F;

	protected final long payloadLength;

	protected final int payloadOffset;
	
	protected final int headerLength;
	
	protected final boolean fragmented;

	protected final Opcode opcode;

	public FrameHeaderDraft06(boolean fragmented, int headerLength, int payloadOffset, long payloadLength, Opcode opcode) {
		this.headerLength = headerLength + payloadOffset;
		this.payloadOffset = payloadOffset;
		this.payloadLength = payloadLength;
		this.fragmented = fragmented;
		this.opcode = opcode;
	}
	
	public long getFrameLength(){
		return headerLength + payloadLength;
	}
	
	public int getHeaderLength(){
		return headerLength;
	}
	
	public long getBodyLength(){
		return payloadLength;
	}
	
	public boolean isFragmented(){
		return fragmented;
	}
	
	public Opcode getOpcode(){
		return opcode;
	}
	
	public ByteBuffer toByteBuffer(){
		int payloadLen = 0;
		int headerExtendLen = 0;
		if(payloadLen <= 0x7D){
			payloadLen = (int)this.payloadLength;
		}else if(payloadLength <= MAX_FRAME_LENGTH_16){
			payloadLen = 0x7E;
			headerExtendLen = 2;
		}else if(payloadLength <= MAX_FRAME_LENGTH_63){
			payloadLen = 0x7F;
			headerExtendLen = 8;
		}
		
		ByteBuffer buf = ByteBuffer.allocate(2 + headerExtendLen);
		buf.put((byte)((fragmented ? 0 : 0x80) | opcode.intValue()));
		buf.put((byte)payloadLen);
		if(payloadLen == 0x7E){
			buf.putShort((short)payloadLength);
		}else if(payloadLen == 0x7F){
			buf.putLong(payloadLength);
		}
		buf.flip();
		return buf;
	}
}
