package jp.a840.websocket.frame.draft06;

import static jp.a840.websocket.frame.draft06.FrameHeaderDraft06.FIN_MASK;
import static jp.a840.websocket.frame.draft06.FrameHeaderDraft06.OPCODE_MASK;
import static jp.a840.websocket.frame.draft06.FrameHeaderDraft06.PAYLOAD_LEN_MASK;
import static jp.a840.websocket.frame.draft06.FrameHeaderDraft06.RSV1_MASK;
import static jp.a840.websocket.frame.draft06.FrameHeaderDraft06.RSV2_MASK;
import static jp.a840.websocket.frame.draft06.FrameHeaderDraft06.RSV3_MASK;
import static jp.a840.websocket.frame.draft06.FrameHeaderDraft06.RSV4_MASK;

import java.nio.ByteBuffer;

import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.FrameHeader;
import jp.a840.websocket.frame.draft06.FrameHeaderDraft06.Opcode;
import jp.a840.websocket.frame.draft06.FrameHeaderDraft06.PayloadLengthType;

public class FrameBuilderDraft06 {
	
	/**
	 * create frame header from parameter bytes
	 * if a invalid frame data received which may throw IllegalArgumentException.
	 * @param chunkData
	 * @return a sub class of Frame
	 */
	public FrameHeaderDraft06 createFrameHeader(ByteBuffer chunkData) {
		if(chunkData == null){
			throw new IllegalArgumentException("Data is null.");
		}

		int length = chunkData.limit() - chunkData.position();
		int position = chunkData.position();

		if(length < 2){
			throw new IllegalArgumentException("Too short frame data.");	
		}

	// TODO OFFSET!!!
		
		// check frameData[0]
		// No check FIN. because FIN bit permit 0 or 1.
		boolean fragmented = (chunkData.get(position) & FIN_MASK) == 0;
		
		// check reserve field.
		if((chunkData.get(position) & (RSV1_MASK | RSV2_MASK | RSV3_MASK)) != 0){
			throw new IllegalArgumentException("Found nonzero bit in reserve field. (RSV1,2,3)");
		}

		// check opcode
		int opcodeNum = chunkData.get(position) & OPCODE_MASK;
		Opcode opcode = Opcode.valueOf(opcodeNum);
		if(opcode == null){
			throw new IllegalArgumentException("Found illegal opcode " + opcodeNum + ".");
		}
	
		// check frameData[1]
		// check reserve field.
		if((chunkData.get(position + 1) & RSV4_MASK) != 0){
			throw new IllegalArgumentException("Found nonzero bit in reserve field. (RSV4)");
		}
		
		// check payload len
		long payloadLength = chunkData.get(position + 1) & PAYLOAD_LEN_MASK;
		PayloadLengthType payloadLengthType = PayloadLengthType.valueOf((int)payloadLength);
		if(payloadLengthType == null){
			throw new IllegalArgumentException("Found illegal payload length " + payloadLength + ".");
		}

		int payloadOffset = position + 2;
		if(PayloadLengthType.LEN_16.equals(payloadLengthType)){
			if(length < 4){
				throw new IllegalArgumentException("Too short frame data.");
			}
			payloadLength = chunkData.getChar(payloadOffset);
			payloadOffset += 2;
		}else if(PayloadLengthType.LEN_63.equals(payloadLengthType)){
			if(length < 10){
				throw new IllegalArgumentException("Too short frame data.");
			}
			payloadLength = chunkData.getLong(payloadOffset);
			payloadOffset += 8;
		}
		
		if(payloadLength > Integer.MAX_VALUE){
			throw new IllegalArgumentException("large data is not support yet");
		}
		
		return new FrameHeaderDraft06(fragmented, 2, payloadOffset, (int)payloadLength, opcode);
	}
	
	public Frame createFrame(FrameHeader h, byte[] bodyData){
		FrameHeaderDraft06 header = (FrameHeaderDraft06)h;
		switch(header.getOpcode()){
		case CONNECTION_CLOSE: return new ConnectionCloseFrame(header, bodyData);
		case PING:             return new PingFrame(header, bodyData);
		case PONG:             return new PongFrame(header, bodyData);
		case TEXT_FRAME:       return new TextFrame(header, bodyData);
		case BINARY_FRAME:     return new BinaryFrame(header, bodyData);
		default: throw new IllegalStateException("Not found Opcode type!");
		}
	}
}
