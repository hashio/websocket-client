package jp.a840.websocket.frame.draft06;

import java.nio.ByteBuffer;

import jp.a840.websocket.frame.Frame;

public class FrameBuilderDraft06 {
	protected enum Opcode {
		CONTINUATION(0), CONNECTION_CLOSE(1), PING(2), PONG(3), TEXT_FRAME(4), BINARY_FRAME(5);
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
			case 0:
				return CONTINUATION;
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
		LEN_SHORT((byte)0x7D, 0), // 0x00 - 0x7D
		LEN_16((byte)0x7E, 2), // 0x0000 - 0xFFFF
		LEN_63((byte)0x7F, 8); // 0x0000000000000000 - 0x7FFFFFFFFFFFFFFF
		private final byte payloadLengthType;
		private final int offset;

		private PayloadLengthType(byte payloadLengthType, int offset) {
			this.payloadLengthType = payloadLengthType;
			this.offset = offset;
		}

		public byte byteValue() {
			return payloadLengthType;
		}
		
		public int offset(){
			return offset;
		}

		public static PayloadLengthType valueOf(byte plt) {
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
		
		public static PayloadLengthType valueOf(long payloadLength){
			if(payloadLength <= PayloadLengthType.LEN_SHORT.byteValue()){
				return PayloadLengthType.LEN_SHORT;
			}else if(payloadLength <= MAX_FRAME_LENGTH_16){
				return PayloadLengthType.LEN_16;
			}else if(payloadLength <= MAX_FRAME_LENGTH_63){
				return PayloadLengthType.LEN_63;
			}else{
				throw new IllegalArgumentException("Overflow payload length. payloadLength: " + payloadLength);
			}				
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


	/**
	 * create frame header from parameter bytes
	 * if a invalid frame data received which may throw IllegalArgumentException.
	 * @param chunkData
	 * @return a sub class of Frame
	 */
	public static FrameHeaderDraft06 createFrameHeader(ByteBuffer chunkData, FrameHeaderDraft06 previousHeader) {
		if(chunkData == null){
			throw new IllegalArgumentException("Data is null.");
		}

		int length = chunkData.limit() - chunkData.position();

		if(length < 2){
			return null;
		}

		// check frameData[0]
		byte hb1 = chunkData.get();
		// No check FIN. because FIN bit permit 0 or 1.
		boolean fragmented = (hb1 & FIN_MASK) == 0;
		
		// check reserve field.
		if((hb1 & (RSV1_MASK | RSV2_MASK | RSV3_MASK)) != 0){
			throw new IllegalArgumentException("Found nonzero bit in reserve field. (RSV1,2,3)");
		}

		// check opcode
		int opcodeNum = hb1 & OPCODE_MASK;
		Opcode opcode = Opcode.valueOf(opcodeNum);
		if(opcode == null){
			throw new IllegalArgumentException("Found illegal opcode " + opcodeNum + ".");
		}
	
		// check frameData[1]
		byte hb2 = chunkData.get();
		// check reserve field.
		if((hb2 & RSV4_MASK) != 0){
			throw new IllegalArgumentException("Found nonzero bit in reserve field. (RSV4)");
		}
		
		// check payload len
		byte payloadLength1 = (byte)(hb2 & PAYLOAD_LEN_MASK);
		PayloadLengthType payloadLengthType = PayloadLengthType.valueOf(payloadLength1);
		if(payloadLengthType == null){
			throw new IllegalArgumentException("Found illegal payload length " + payloadLength1 + ".");
		}

		if(length < 2 + payloadLengthType.offset()){
			return null;
		}
		
		long payloadLength2 = payloadLength1;
		switch(payloadLengthType){
		case LEN_16: payloadLength2 = chunkData.getShort();
			break;
		case LEN_63: payloadLength2 = chunkData.getLong();
			break;
		}

		if(payloadLength2 > Integer.MAX_VALUE){
			throw new IllegalArgumentException("large data is not support yet");
		}
		
		if(Opcode.CONTINUATION.equals(opcode) && previousHeader != null){
			return new FrameHeaderDraft06(fragmented, 2, payloadLengthType, (int)payloadLength2, opcode, previousHeader.getOpcode());			
		} else {
			return new FrameHeaderDraft06(fragmented, 2, payloadLengthType, (int)payloadLength2, opcode);
		}
	}
	
	public static FrameHeaderDraft06 createFrameHeader(byte[] body, boolean fragmented, Opcode opcode) {
		int payloadLength = body.length;
		PayloadLengthType payloadLengthType = PayloadLengthType.valueOf(payloadLength);
		return new FrameHeaderDraft06(false, 2, payloadLengthType, (int)payloadLength, opcode);
	}
	
	public static Frame createFrame(FrameHeaderDraft06 header, byte[] bodyData){
		Opcode opcode = header.getRealOpcode();
		if(opcode == null){
			opcode = header.getOpcode();
		}
		switch(opcode){
		case CONNECTION_CLOSE: return new ConnectionCloseFrame(header, bodyData);
		case PING:             return new PingFrame(header, bodyData);
		case PONG:             return new PongFrame(header, bodyData);
		case TEXT_FRAME:       return new TextFrame(header, bodyData);
		case BINARY_FRAME:     return new BinaryFrame(header, bodyData);
		default: throw new IllegalStateException("Not found Opcode type!");
		}
	}
}
