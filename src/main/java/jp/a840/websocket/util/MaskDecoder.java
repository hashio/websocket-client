/*
 * The MIT License
 *
 * Copyright (c) 2013 Takahiro Hashimoto
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

package jp.a840.websocket.util;

import java.nio.ByteBuffer;

/**
 * The Class MaskDecoder.
 *
 * @author Takahiro Hashimoto
 */
public class MaskDecoder {

    private byte[] seed;

    private int c = 0;

    public MaskDecoder(byte[] seed){
        this.seed = seed;
    }

    public void decode(ByteBuffer masked, int offset, int length){
        if(length <= 0){
            return;
        }
        int idx = masked.position() + offset;
        masked.position(idx);
        masked.limit(idx + length);
        c = (c + offset) % 4;
        for(int i = 0; i < length; i++){
            masked.put(idx++, (byte) (masked.get() ^ seed[c++ % 4]));
        }
    }

    public void decode(ByteBuffer masked){
        decode(masked, 0, masked.remaining());
    }
}
