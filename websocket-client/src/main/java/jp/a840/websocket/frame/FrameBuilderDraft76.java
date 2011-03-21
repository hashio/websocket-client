package jp.a840.websocket.frame;

import static jp.a840.websocket.frame.FrameHeader.*;

import java.nio.ByteBuffer;

import jp.a840.websocket.frame.FrameHeader.Opcode;
import jp.a840.websocket.frame.FrameHeader.PayloadLengthType;

public class FrameBuilderDraft76 {
	
	/**
	 * create frame header from parameter bytes
	 * if a invalid frame data received which may throw IllegalArgumentException.
	 * @param chunkData
	 * @return a sub class of Frame
	 */
	public FrameHeader createFrameHeader(ByteBuffer chunkData) {
		if(chunkData == null){
			throw new IllegalArgumentException("Data is null.");
		}

		int length = chunkData.limit() - chunkData.position();
		int position = chunkData.position();

		if(length < 2){
			throw new IllegalArgumentException("Too short frame data.");	
		}

		int payloadLength = 0;
		int type = chunkData.get();
		if(type == 0x80){
			while(chunkData.get() != 0xFF);
			payloadLength = chunkData.position() - position;
			return new FrameHeader(false, 1, 0, payloadLength, Opcode.TEXT_FRAME);
		}else{
			int i = 1;
			while(chunkData.hasRemaining()){
				int lengthByte = chunkData.get();
				int length7Bit = lengthByte & 0x7F;
				payloadLength = (payloadLength << 7) | length7Bit;
				if(length7Bit == lengthByte){
					break;
				}
			}
			return new FrameHeader(false, 1, 0, payloadLength, Opcode.BINARY_FRAME);
		}
		
	}
	
	public Frame createFrame(FrameHeader header, byte[] bodyData){
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
