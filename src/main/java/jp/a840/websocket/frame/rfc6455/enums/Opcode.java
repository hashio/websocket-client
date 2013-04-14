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
 * The Class Opcode.
 *
 * @author Takahiro Hashimoto
 */
/**
 * The Enum Opcode.
 *
 * @author Takahiro Hashimoto
 */
public enum Opcode {

    /**
     * The CONTINUATION.
     */
    CONTINUATION(0x0),
    /**
     * The TEXT_FRAME.
     */
    TEXT_FRAME(0x1),
    /**
     * The BINARY_FRAME.
     */
    BINARY_FRAME(0x2),
    // 0x3-7 reserved
    /**
     * The CONNECTION_CLOSE.
     */
    CONNECTION_CLOSE(0x8),
    /**
     * The PING.
     */
    PING(0x9),
    /**
     * The PONG.
     */
    PONG(0xA);
    // 0xB-F reserved

    /**
     * The opcode.
     */
    private final int opcode;

    /**
     * Instantiates a new opcode.
     *
     * @param opcode the opcode
     */
    private Opcode(int opcode) {
        this.opcode = opcode;
    }

    /**
     * Int value.
     *
     * @return the int
     */
    public int intValue() {
        return opcode;
    }

    /**
     * Value of.
     *
     * @param opc the opc
     * @return the opcode
     */
    public static Opcode valueOf(int opc) {
        switch (opc) {
            case 0x0:
                return CONTINUATION;
            case 0x1:
                return TEXT_FRAME;
            case 0x2:
                return BINARY_FRAME;
            case 0x8:
                return CONNECTION_CLOSE;
            case 0x9:
                return PING;
            case 0xA:
                return PONG;
            default:
                return null;
        }
    }
}
