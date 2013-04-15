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
import jp.a840.websocket.frame.rfc6455.enums.Opcode;
import jp.a840.websocket.frame.rfc6455.enums.PayloadLengthType;

import java.nio.ByteBuffer;


/**
 * The Class FrameBuilderRfc6455.
 *
 * @author Takahiro Hashimoto
 */
public class FrameBuilderRfc6455 {


    /**
     * The Constant FIN_MASK.
     */
    protected static final int FIN_MASK = 1 << 7;

    /**
     * The Constant RSV1_MASK.
     */
    protected static final byte RSV1_MASK = 1 << 6;

    /**
     * The Constant RSV2_MASK.
     */
    protected static final byte RSV2_MASK = 1 << 5;

    /**
     * The Constant RSV3_MASK.
     */
    protected static final byte RSV3_MASK = 1 << 4;

    /**
     * The Constant OPCODE_MASK.
     */
    protected static final byte OPCODE_MASK = 0xF;

    /**
     * The Constant MASK_MASK.
     */
    protected static final int MASK_MASK = 1 << 7;

    /**
     * The Constant PAYLOAD_LEN_MASK.
     */
    protected static final int PAYLOAD_LEN_MASK = 0x7F;


    /**
     * create frame header from parameter bytes
     * if a invalid frame data received which may throw IllegalArgumentException.
     *
     * @param chunkData      the chunk data
     * @param previousHeader the previous header
     * @return a sub class of Frame
     */
    public static FrameHeaderRfc6455 createFrameHeader(ByteBuffer chunkData, FrameHeaderRfc6455 previousHeader) {
        if (chunkData == null) {
            throw new IllegalArgumentException("Data is null.");
        }

        int length = chunkData.limit() - chunkData.position();

        if (length < 2) {
            return null;
        }

        // check frameData[0]
        byte hb1 = chunkData.get();
        // No check FIN. because FIN bit permit 0 or 1.
        boolean fragmented = (hb1 & FIN_MASK) == 0;

        // check reserve field.
        if ((hb1 & (RSV1_MASK | RSV2_MASK | RSV3_MASK)) != 0) {
            throw new IllegalArgumentException("Found nonzero bit in reserve field. (RSV1,2,3)");
        }

        // check opcode
        int opcodeNum = hb1 & OPCODE_MASK;
        Opcode opcode = Opcode.valueOf(opcodeNum);
        if (opcode == null) {
            throw new IllegalArgumentException("Found illegal opcode " + opcodeNum + ".");
        }

        // check frameData[1]
        byte hb2 = chunkData.get();
        // check reserve field.
        if ((hb2 & MASK_MASK) != 0) {
            throw new IllegalArgumentException("Found mask bit field. (MASK)");
        }

        // check payload len
        byte payloadLength1 = (byte) (hb2 & PAYLOAD_LEN_MASK);
        PayloadLengthType payloadLengthType = PayloadLengthType.valueOf(payloadLength1);
        if (payloadLengthType == null) {
            throw new IllegalArgumentException("Found illegal payload length " + payloadLength1 + ".");
        }

        if (length < 2 + payloadLengthType.offset()) {
            return null;
        }

        long payloadLength2 = payloadLength1;
        switch (payloadLengthType) {
            case LEN_16:
                payloadLength2 = 0xFFFF & (chunkData.get() << 8 | chunkData.get());
                break;
            case LEN_63:
                payloadLength2 = 0x7FFFFFFFFFFFFFFFL & chunkData.getLong();
                break;
        }

        if (payloadLength2 > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("large data is not support yet");
        }

        if (Opcode.CONTINUATION.equals(opcode) && previousHeader != null) {
            return new FrameHeaderRfc6455(fragmented, 2, payloadLengthType, (int) payloadLength2, opcode, previousHeader.getOpcode());
        } else {
            return new FrameHeaderRfc6455(fragmented, 2, payloadLengthType, (int) payloadLength2, opcode);
        }
    }

    /**
     * Creates the frame header.
     *
     * @param body       the contents
     * @param fragmented the fragmented
     * @param opcode     the opcode
     * @return the frame header draft06
     */
    public static FrameHeaderRfc6455 createFrameHeader(byte[] body, boolean fragmented, Opcode opcode) {
        int payloadLength = 0;
        if (body != null) {
            payloadLength = body.length;
        }
        return createFrameHeader(payloadLength, fragmented, opcode);
    }

    /**
     * Creates the frame header.
     *
     * @param body       the contents
     * @param fragmented the fragmented
     * @param opcode     the opcode
     * @return the frame header draft06
     */
    public static FrameHeaderRfc6455 createFrameHeader(ByteBuffer body, boolean fragmented, Opcode opcode) {
        int payloadLength = 0;
        if (body != null) {
            payloadLength = body.remaining();
        }
        return createFrameHeader(payloadLength, fragmented, opcode);
    }

    /**
     * Creates the frame header.
     *
     * @param payloadLength the length of contents
     * @param fragmented    the fragmented
     * @param opcode        the opcode
     * @return the frame header draft06
     */
    public static FrameHeaderRfc6455 createFrameHeader(int payloadLength, boolean fragmented, Opcode opcode) {
        PayloadLengthType payloadLengthType = PayloadLengthType.valueOf(payloadLength);
        return new FrameHeaderRfc6455(fragmented, 2, payloadLengthType, (int) payloadLength, opcode);
    }

    /**
     * Creates the frame.
     *
     * @param header   the header
     * @param bodyData the contents data
     * @return the frame
     */
    public static Frame createFrame(FrameHeaderRfc6455 header, byte[] bodyData) {
        Opcode opcode = header.getRealOpcode();
        if (opcode == null) {
            opcode = header.getOpcode();
        }
        switch (opcode) {
            case CONNECTION_CLOSE:
                return new CloseFrame(header, bodyData);
            case PING:
                return new PingFrame(header, bodyData);
            case PONG:
                return new PongFrame(header, bodyData);
            case TEXT_FRAME:
                return new TextFrame(header, bodyData);
            case BINARY_FRAME:
                return new BinaryFrame(header, bodyData);
            default:
                throw new IllegalStateException("Not found Opcode type!");
        }
    }
}
