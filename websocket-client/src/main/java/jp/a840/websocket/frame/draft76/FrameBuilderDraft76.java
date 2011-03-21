package jp.a840.websocket.frame.draft76;

import java.nio.ByteBuffer;

import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.FrameHeader;

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
		}else{
			while(chunkData.hasRemaining()){
				int lengthByte = chunkData.get();
				int length7Bit = lengthByte & 0x7F;
				payloadLength = (payloadLength << 7) | length7Bit;
				if(length7Bit == lengthByte){
					break;
				}
			}
		}
		return new FrameHeaderDraft76((byte)type, payloadLength);
	}
	
	public Frame createFrame(FrameHeader h, byte[] bodyData){
		FrameHeaderDraft76 header = (FrameHeaderDraft76)h;
		switch(header.getFrameType()){
		case (byte)0x80:     return new TextFrame(header, bodyData);
		case (byte)0xFF:     return new BinaryFrame(header, bodyData);
		default: throw new IllegalStateException("Not found Opcode type!");
		}
	}
}
