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
package jp.a840.websocket.frame.draft76;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import jp.a840.websocket.frame.FrameHeader;

/**
 * The Class TextFrame.
 *
 * @author Takahiro Hashimoto
 */
public class TextFrame extends FrameDraft76 {

	/** The converted string. */
	private String convertedString;

	/**
	 * Instantiates a new text frame.
	 *
	 * @param str the str
	 */
	public TextFrame(String str) {
		super();
		byte[] body = convertStringToByteArray(str);
		FrameHeaderDraft76 header = new FrameHeaderDraft76((byte)0x00, body.length + 1);
		setHeader(header);
		setBody(body);
	}
	
	/**
	 * Instantiates a new text frame.
	 *
	 * @param header the header
	 * @param body the body
	 */
	public TextFrame(FrameHeader header, byte[] body){
		super(header, stripTerminateFlag(body));
	}

	/**
	 * Strip terminate flag.
	 *
	 * @param body the body
	 * @return the byte[]
	 */
	private static byte[] stripTerminateFlag(byte[] body){
		if(body[body.length - 1] == (byte)0xFF){
			byte[] tmp = new byte[body.length - 1];
			System.arraycopy(body, 0, tmp, 0, body.length - 2);
			body = tmp;
		}
		return body;
	}
	
	/**
	 * Convert string to byte array.
	 *
	 * @param str the str
	 * @return the byte[]
	 */
	private static byte[] convertStringToByteArray(String str){
		try{
			return str.getBytes("UTF-8");
		}catch(UnsupportedEncodingException e){
			;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see jp.a840.websocket.frame.Frame#toByteBuffer()
	 */
	public ByteBuffer toByteBuffer() {
		ByteBuffer buf = ByteBuffer.allocate(1 + body.length + 1);
		buf.put(header.toByteBuffer());
		buf.put(body);
		buf.put((byte)0xFF);
		buf.flip();
		return buf;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		if(convertedString == null){
			try{
				convertedString = new String(body, "UTF-8");
			}catch(UnsupportedEncodingException e){
				convertedString = "";
			}
		}
		return convertedString;
	}
}
