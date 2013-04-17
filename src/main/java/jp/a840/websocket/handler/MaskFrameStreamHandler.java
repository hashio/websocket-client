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

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.exception.WebSocketException;
import jp.a840.websocket.frame.Frame;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * The Class MaskFrameStreamHandler.
 *
 * @author Takahiro Hashimoto
 */
public class MaskFrameStreamHandler extends StreamHandlerAdapter {
    /**
     * The random.
     */
    private Random random = new Random();

    /* (non-Javadoc)
     * @see jp.a840.websocket.handler.StreamHandlerAdapter#nextUpstreamHandler(jp.a840.websocket.WebSocket, java.nio.ByteBuffer, jp.a840.websocket.frame.Frame, jp.a840.websocket.handler.StreamHandlerChain)
     */
    @Override
    public void nextUpstreamHandler(WebSocket ws, ByteBuffer buffer,
                                    Frame frame, StreamHandlerChain chain) throws WebSocketException {
        buffer.put(1, (byte) (buffer.get(1) | 0x80)); // force set mask bit;
        int maskkeyOffset = 2;
        if (buffer.get(buffer.position() + 1) == (byte) 0xFE) {
            maskkeyOffset = 4;
        } else if (buffer.get(buffer.position() + 1) == 0xFF) {
            maskkeyOffset = 10;
        }

        // avoid copy buffer
        // buffer already reserved mask key field in FrameRfc6455
        ByteBuffer buf = ByteBuffer.wrap(buffer.array());
        int limit = buffer.limit();
        buffer.limit(buffer.position() + maskkeyOffset);
        buf.put(buffer);
        buffer.limit(limit);
        buf.putInt(random.nextInt());

        byte[] maskkey = new byte[4];
        buf.position(maskkeyOffset);
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
