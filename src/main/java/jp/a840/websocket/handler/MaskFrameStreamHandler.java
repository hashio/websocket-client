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
package jp.a840.websocket.handler;

import java.nio.ByteBuffer;
import java.util.Random;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.draft76.CloseFrame;

/**
 * The Class MaskFrameStreamHandler.
 *
 * @author Takahiro Hashimoto
 */
public class MaskFrameStreamHandler extends StreamHandlerAdapter {
	/** The random. */
	private static Random random = new Random();

	/* (non-Javadoc)
	 * @see jp.a840.websocket.handler.StreamHandlerAdapter#nextUpstreamHandler(jp.a840.websocket.WebSocket, java.nio.ByteBuffer, jp.a840.websocket.frame.Frame, jp.a840.websocket.handler.StreamHandlerChain)
	 */
	@Override
	public void nextUpstreamHandler(WebSocket ws, ByteBuffer buffer,
			Frame frame, StreamHandlerChain chain) throws WebSocketException {
		ByteBuffer buf = ByteBuffer.allocate(4 + buffer.remaining()); // mask-key + header + body
		buf.putInt(random.nextInt());
		buf.put(buffer);
		buf.flip();

		byte[] maskkey = new byte[4];
		buf.get(maskkey, 0, 4);
		int m = 0;
		while (buf.hasRemaining()) {
			int position = buf.position();
			buf.put((byte) (buf.get(position) ^ maskkey[m++ % 4]));
		}
		buf.flip();
		chain.nextUpstreamHandler(ws, buf, frame);
	}
}
