package jp.a840.websocket.frame.draft76;

import java.nio.ByteBuffer;

import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.FrameHeader;

public class FrameBuilderDraft76 {
	
	/**
	 * create frame header from parameter bytes
	 * if a invalid frame data received which may throw IllegalArgumentException.
	 * @param chunkData
	 * @return a sub class of FrameHeader or null if not enough buffer
	 */
	public static FrameHeader createFrameHeader(ByteBuffer chunkData) {
		if(chunkData == null){
			throw new IllegalArgumentException("Data is null.");
		}

		int length = chunkData.limit() - chunkData.position();
		if(length < 2){
			return null;
		}

		int payloadLength = 0;
		byte type = chunkData.get();
		int position = chunkData.position();
		if((byte)0x00 <= type && type <= (byte)0x7F){
			chunkData.mark();
			boolean completed = false;
			while(chunkData.hasRemaining() && !completed){
				if(chunkData.get() == (byte)0xFF){
					completed = true;
					continue;
				}
			}
			if(!completed){
				return null;
			}
			payloadLength = chunkData.position() - position;
			chunkData.reset();
		}else if((byte)0x80 <= type && type <= (byte)0xFF){
			boolean completed = false;
			while(chunkData.hasRemaining() && !completed){
				int lengthByte = chunkData.get();
				int length7Bit = lengthByte & 0x7F;
				payloadLength = (payloadLength << 7) | length7Bit;
				if(length7Bit == lengthByte){
					completed = true;
					continue;
				}
			}
			if(!completed){
				return null;
			}
		}else{
			throw new IllegalStateException("Not found Opcode type! (" + type + ")");			
		}
		return new FrameHeaderDraft76((byte)type, payloadLength);
	}
	
	public static Frame createFrame(FrameHeader h, byte[] bodyData){
		FrameHeaderDraft76 header = (FrameHeaderDraft76)h;
		if((byte)0x00 <= header.getFrameType() && header.getFrameType() <= (byte)0x7F){
			return new TextFrame(header, bodyData);
		}else if((byte)0x80 <= header.getFrameType() && header.getFrameType() <= (byte)0xFF){
		    return new BinaryFrame(header, bodyData);
		}else{
			throw new IllegalStateException("Not found Opcode type! (" + header.getFrameType() + ")");
		}
	}
}
