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
package jp.a840.websocket.frame;

import java.nio.ByteBuffer;

import jp.a840.websocket.frame.rfc6455.TextFrame;


/**
 * The Class Frame.
 *
 * @author Takahiro Hashimoto
 */
abstract public class Frame {

	/** The header. */
	protected FrameHeader header;
	
	/** The body. */
	protected byte[] body;
	
	/**
	 * Instantiates a new frame.
	 */
	protected Frame(){
	}
	
	/**
	 * Instantiates a new frame.
	 *
	 * @param header the header
	 * @param body the body
	 */
	protected Frame(FrameHeader header, byte[] body){
		this.header = header;
		this.body = body;
	}

	/**
	 * Gets the body length.
	 *
	 * @return the body length
	 */
	public long getBodyLength(){
		return header.getBodyLength();
	}
	
	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	public FrameHeader getHeader(){
		return header;
	}
	
	/**
	 * convert frame to byte buffer.
	 *
	 * @return the byte buffer
	 */
	abstract public ByteBuffer toByteBuffer();

	/**
	 * Gets the raw body.
	 *
	 * @return the raw body
	 */
	public ByteBuffer getRawBody(){
		return ByteBuffer.wrap(body);
	}

	/**
	 * Sets the header.
	 *
	 * @param header the new header
	 */
	protected void setHeader(FrameHeader header) {
		this.header = header;
	}

	/**
	 * Sets the body.
	 *
	 * @param body the new body
	 */
	protected void setBody(byte[] body) {
		this.body = body;
	}
}
