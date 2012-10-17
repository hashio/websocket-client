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

import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.FrameHeader;

/**
 * WebSocket Frame class
 *
 * Frame (Draft76)
 *
 * Support WebSocket Draft76 specification.
 *
 * @author t-hashimoto
 *
 */
abstract public class FrameDraft76 extends Frame {
	
	/**
	 * Instantiates a new frame draft76.
	 */
	protected FrameDraft76(){
	}
	
	/**
	 * Instantiates a new frame draft76.
	 *
	 * @param header the header
	 * @param body the contents
	 */
	protected FrameDraft76(FrameHeader header, byte[] body){
		super(header, body);
	}
}
