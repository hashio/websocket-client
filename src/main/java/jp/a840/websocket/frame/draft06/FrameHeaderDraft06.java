/*
 * The MIT License
 * 
 * Copyright (c) 2011 Takahiro Hashimoto
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jp.a840.websocket.frame.draft06;

import java.nio.ByteBuffer;

import jp.a840.websocket.frame.FrameHeader;
import jp.a840.websocket.frame.draft06.FrameBuilderDraft06.Opcode;
import jp.a840.websocket.frame.draft06.FrameBuilderDraft06.PayloadLengthType;



/**
 * The Class FrameHeaderDraft06.
 *
 * @author Takahiro Hashimoto
 */
public class FrameHeaderDraft06 implements FrameHeader {
	
	/** The payload length. */
	protected final long payloadLength;

	/** The payload length type. */
	protected final PayloadLengthType payloadLengthType;
	
	/** The header length. */
	protected final int headerLength;
	
	/** The fragmented. */
	protected final boolean fragmented;

	/** The opcode. */
	protected final Opcode opcode;
	
	/** The continuation flag. */
	protected boolean isContinuation;

	/**
	 * Instantiates a new frame header draft06.
	 *
	 * @param fragmented the fragmented
	 * @param headerLength the header length
	 * @param payloadLengthType the payload length type
	 * @param payloadLength the payload length
	 * @param opcode the opcode
	 */
	public FrameHeaderDraft06(boolean fragmented, int headerLength, PayloadLengthType payloadLengthType, long payloadLength, Opcode opcode) {
		this.headerLength = headerLength + payloadLengthType.offset();
		this.payloadLengthType = payloadLengthType;
		this.payloadLength = payloadLength;
		this.fragmented = fragmented;
		this.opcode = opcode;
        this.isContinuation = false;
	}
	
	/**
	 * Instantiates a new frame header draft06.
	 *
	 * @param fragmented the fragmented
	 * @param headerLength the header length
	 * @param payloadLengthType the payload length type
	 * @param payloadLength the payload length
	 * @param opcode the opcode
	 * @param isContinuation
	 */
	public FrameHeaderDraft06(boolean fragmented, int headerLength, PayloadLengthType payloadLengthType, long payloadLength, Opcode opcode, boolean isContinuation) {
		this.headerLength = headerLength + payloadLengthType.offset();
		this.payloadLengthType = payloadLengthType;
		this.payloadLength = payloadLength;
		this.fragmented = fragmented;
		this.opcode = opcode;
		this.isContinuation = isContinuation;
	}
	
	/* (non-Javadoc)
	 * @see jp.a840.websocket.frame.FrameHeader#getFrameLength()
	 */
	public long getFrameLength(){
		return headerLength + payloadLength;
	}
	
	/**
	 * Gets the header length.
	 *
	 * @return the header length
	 */
	public int getHeaderLength(){
		return headerLength;
	}
	
	/* (non-Javadoc)
	 * @see jp.a840.websocket.frame.FrameHeader#getBodyLength()
	 */
	public long getBodyLength(){
		return payloadLength;
	}
	
	/**
	 * Checks if is fragmented.
	 *
	 * @return true, if is fragmented
	 */
	public boolean isFragmented(){
		return fragmented;
	}
	
	/**
	 * Checks if is continuation.
	 *
	 * @return true, if is continuation
	 */
	public boolean isContinuation(){
		return isContinuation;
	}
	
	/**
	 * Gets the opcode.
	 *
	 * @return the opcode
	 */
	public Opcode getOpcode(){
		return opcode;
	}
	
	/* (non-Javadoc)
	 * @see jp.a840.websocket.frame.FrameHeader#toByteBuffer()
	 */
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
