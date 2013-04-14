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

package jp.a840.websocket.frame.rfc6455.enums;

/**
 * The Enum PayloadLengthType.
 *
 * @author Takahiro Hashimoto
 */
public enum PayloadLengthType {
    /**
     * The LEN_SHORT.
     */
    LEN_SHORT((byte) 0x7D, 0), // 0x00 - 0x7D
    /**
     * The LEN_16.
     */
    LEN_16((byte) 0x7E, 2), // 0x0000 - 0xFFFF
    /**
     * The LEN_63.
     */
    LEN_63((byte) 0x7F, 8); // 0x0000000000000000 - 0x7FFFFFFFFFFFFFFF

    /**
     * The Constant MAX_FRAME_LENGTH_16.
     */
    private static final int MAX_FRAME_LENGTH_16 = 0xFFFF;

    /**
     * The Constant MAX_FRAME_LENGTH_63.
     */
    private static final long MAX_FRAME_LENGTH_63 = 0x7FFFFFFFFFFFFFFFL;

    /**
     * The payload length type.
     */
    private final byte payloadLengthType;

    /**
     * The offset.
     */
    private final int offset;

    /**
     * Instantiates a new payload length type.
     *
     * @param payloadLengthType the payload length type
     * @param offset            the offset
     */
    private PayloadLengthType(byte payloadLengthType, int offset) {
        this.payloadLengthType = payloadLengthType;
        this.offset = offset;
    }

    /**
     * Byte value.
     *
     * @return the byte
     */
    public byte byteValue() {
        return payloadLengthType;
    }

    /**
     * Offset.
     *
     * @return the int
     */
    public int offset() {
        return offset;
    }

    /**
     * Value of.
     *
     * @param plt the plt
     * @return the payload length type
     */
    public static PayloadLengthType valueOf(byte plt) {
        plt = (byte)(plt & 0x7F);
        switch (plt) {
            case 0x7E:
                return LEN_16;
            case 0x7F:
                return LEN_63;
        }
        if (0 <= plt && plt <= 0x7D) {
            return LEN_SHORT;
        }
        return null;
    }

    /**
     * Value of.
     *
     * @param payloadLength the payload length
     * @return the payload length type
     */
    public static PayloadLengthType valueOf(long payloadLength) {
        if (payloadLength <= PayloadLengthType.LEN_SHORT.byteValue()) {
            return PayloadLengthType.LEN_SHORT;
        } else if (payloadLength <= MAX_FRAME_LENGTH_16) {
            return PayloadLengthType.LEN_16;
        } else if (payloadLength <= MAX_FRAME_LENGTH_63) {
            return PayloadLengthType.LEN_63;
        } else {
            throw new IllegalArgumentException("Overflow payload length. payloadLength: " + payloadLength);
        }
    }

}
