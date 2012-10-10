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
package jp.a840.websocket.frame.rfc6455;

import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.FrameHeader;
import jp.a840.websocket.frame.rfc6455.FrameBuilderRfc6455.Opcode;

import java.nio.ByteBuffer;

/**
 *  WebSocket Frame class
 *
 *  Frame (RFC6455)
 *  <pre>
 *  0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-------+-+-------------+-------------------------------+
 * |F|R|R|R| opcode|M| Payload len |    Extended payload length    |
 * |I|S|S|S|  (4)  |A|     (7)     |             (16/63)           |
 * |N|V|V|V|       |S|             |   (if payload len==126/127)   |
 * | |1|2|3|       |K|             |                               |
 * +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
 * |     Extended payload length continued, if payload len == 127  |
 * + - - - - - - - - - - - - - - - +-------------------------------+
 * |                               |Masking-key, if MASK set to 1  |
 * +-------------------------------+-------------------------------+
 * | Masking-key (continued)       |          Payload Data         |
 * +-------------------------------- - - - - - - - - - - - - - - - +
 * :                     Payload Data continued ...                :
 * + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
 * |                     Payload Data continued ...                |
 * +---------------------------------------------------------------+
 * </pre>
 * payload length = extention data length + application data length.
 * the extention data length may be zero.
 *
 * Support WebSocket Draft06 specification.
 *
 * @author t-hashimoto
 *
 */
abstract public class FrameRfc6455 extends Frame {

	/**
	 * Instantiates a new frame draft06.
	 */
	protected FrameRfc6455(){
	}

	/**
	 * Instantiates a new frame draft06.
	 *
	 * @param header the header
	 * @param bodyData the contents data
	 */
	protected FrameRfc6455(FrameHeader header, byte[] bodyData){
		super(header, bodyData);
	}
	
	/* (non-Javadoc)
	 * @see jp.a840.websocket.frame.Frame#toByteBuffer()
	 */
	public ByteBuffer toByteBuffer(){
		ByteBuffer headerBuffer = header.toByteBuffer();
		int bodyLength = 0;
		if(contents != null){
			bodyLength = contents.length;
		}
		ByteBuffer buf = ByteBuffer.allocate(headerBuffer.limit() + bodyLength); // mask-key + header + contents
		buf.put(headerBuffer);
		if(contents != null){
			buf.put(contents);
		}
		buf.flip();
		return buf;
	}
	
	/**
	 * Checks if is continuation frame.
	 *
	 * @return true, if is continuation frame
	 */
	public boolean isContinuationFrame(){
		return ((FrameHeaderRfc6455)header).getOpcode().equals(Opcode.CONTINUATION);
	}
}
