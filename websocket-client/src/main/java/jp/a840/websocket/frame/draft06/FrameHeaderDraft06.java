package jp.a840.websocket.frame.draft06;

import java.nio.ByteBuffer;

import jp.a840.websocket.frame.FrameHeader;
import jp.a840.websocket.frame.draft06.FrameBuilderDraft06.Opcode;
import jp.a840.websocket.frame.draft06.FrameBuilderDraft06.PayloadLengthType;


public class FrameHeaderDraft06 implements FrameHeader {
	protected final long payloadLength;

	protected final PayloadLengthType payloadLengthType;
	
	protected final int headerLength;
	
	protected final boolean fragmented;

	protected final Opcode opcode;
	
	protected Opcode realOpcode;

	public FrameHeaderDraft06(boolean fragmented, int headerLength, PayloadLengthType payloadLengthType, long payloadLength, Opcode opcode) {
		this.headerLength = headerLength + payloadLengthType.offset();
		this.payloadLengthType = payloadLengthType;
		this.payloadLength = payloadLength;
		this.fragmented = fragmented;
		this.opcode = opcode;
	}
	
	public FrameHeaderDraft06(boolean fragmented, int headerLength, PayloadLengthType payloadLengthType, long payloadLength, Opcode opcode, Opcode realOpcode) {
		this.headerLength = headerLength + payloadLengthType.offset();
		this.payloadLengthType = payloadLengthType;
		this.payloadLength = payloadLength;
		this.fragmented = fragmented;
		this.opcode = opcode;
		this.realOpcode = realOpcode;
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
	
	public boolean isContinuation(){
		return Opcode.CONTINUATION.equals(opcode);
	}
	
	public Opcode getOpcode(){
		return opcode;
	}
	
	/**
	 * If opcode is CONTINUATION, real opcode has previous non-CONTINUATION opcode
	 * or not real opcode is null
	 * @return previous non-CONTINUATION opcode
	 */
	public Opcode getRealOpcode(){
		return realOpcode;
	}
	
	public ByteBuffer toByteBuffer(){
			ByteBuffer buf = ByteBuffer.allocate(2 + payloadLengthType.offset());
			buf.put((byte)((fragmented ? 0 : 0x80) | opcode.intValue()));
			switch(payloadLengthType){
			case LEN_SHORT:
				buf.put((byte)payloadLength);
				break;
			case LEN_16:
				buf.put((byte)payloadLengthType.byteValue());
				buf.putShort((short)payloadLength);
				break;
			case LEN_63:
				buf.put((byte)payloadLengthType.byteValue());
				buf.putLong(payloadLength);
				break;
			}
			buf.flip();
			return buf;
	}
}
