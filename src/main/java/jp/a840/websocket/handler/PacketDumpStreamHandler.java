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
import java.nio.CharBuffer;
import java.util.logging.Logger;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.frame.Frame;

/**
 * Packet Dump Utility
 * 
 * <pre>
 * Dump mode:
 * HS_UP  : dump buffer of handshake upstream
 * HS_DOWN: dump buffer of handshake downstream
 * FR_UP  : dump buffer of frame upstream
 * FR_DOWN: dump buffer of frame downstream
 * ALL    : dump these streams
 * </pre>.
 *
 * @author t-hashimoto
 */
public class PacketDumpStreamHandler implements StreamHandler {

	/** The Constant FR_DOWN. */
	public static final int FR_DOWN = 1 << 0;
	
	/** The Constant HS_DOWN. */
	public static final int HS_DOWN = 1 << 1;
	
	/** The Constant FR_UP. */
	public static final int FR_UP   = 1 << 2;
	
	/** The Constant HS_UP. */
	public static final int HS_UP   = 1 << 3;
	
	/** The Constant ALL. */
	public static final int ALL     = FR_DOWN | HS_DOWN | FR_UP | HS_UP;
	
	/** The log. */
	private Logger log = Logger.getLogger(PacketDumpStreamHandler.class.getName());

	/** The packet dump mode. */
	private int packetDumpMode;
	
	/**
	 * Instantiates a new packet dump stream handler.
	 *
	 * @param packetDumpMode the packet dump mode
	 */
	public PacketDumpStreamHandler(int packetDumpMode){
		this.packetDumpMode = packetDumpMode;
	}
	
	/**
	 * Instantiates a new packet dump stream handler.
	 */
	public PacketDumpStreamHandler(){
		this.packetDumpMode = ALL;
	}
	
	/* (non-Javadoc)
	 * @see jp.a840.websocket.handler.StreamHandler#nextDownstreamHandler(jp.a840.websocket.WebSocket, java.nio.ByteBuffer, jp.a840.websocket.frame.Frame, jp.a840.websocket.handler.StreamHandlerChain)
	 */
	public void nextDownstreamHandler(WebSocket ws, ByteBuffer buffer,
			Frame frame, StreamHandlerChain chain) throws WebSocketException {
		if(isDump(FR_DOWN)){
			printPacketDump("FR Downstream",buffer);
		}
		chain.nextDownstreamHandler(ws, buffer, frame);
	}

	/* (non-Javadoc)
	 * @see jp.a840.websocket.handler.StreamHandler#nextHandshakeDownstreamHandler(jp.a840.websocket.WebSocket, java.nio.ByteBuffer, jp.a840.websocket.handler.StreamHandlerChain)
	 */
	public void nextHandshakeDownstreamHandler(WebSocket ws, ByteBuffer buffer,
			StreamHandlerChain chain) throws WebSocketException {
		if(isDump(HS_DOWN)){
			printPacketDump("HS Downstream",buffer);
		}
		chain.nextHandshakeDownstreamHandler(ws, buffer);
	}

	/* (non-Javadoc)
	 * @see jp.a840.websocket.handler.StreamHandler#nextHandshakeUpstreamHandler(jp.a840.websocket.WebSocket, java.nio.ByteBuffer, jp.a840.websocket.handler.StreamHandlerChain)
	 */
	public void nextHandshakeUpstreamHandler(WebSocket ws, ByteBuffer buffer,
			StreamHandlerChain chain) throws WebSocketException {
		if(isDump(HS_UP)){
			printPacketDump("HS Upstream", buffer);
		}
		chain.nextHandshakeUpstreamHandler(ws, buffer);
	}

	/* (non-Javadoc)
	 * @see jp.a840.websocket.handler.StreamHandler#nextUpstreamHandler(jp.a840.websocket.WebSocket, java.nio.ByteBuffer, jp.a840.websocket.frame.Frame, jp.a840.websocket.handler.StreamHandlerChain)
	 */
	public void nextUpstreamHandler(WebSocket ws, ByteBuffer buffer,
			Frame frame, StreamHandlerChain chain) throws WebSocketException {
		if(isDump(FR_UP)){
			printPacketDump("FR Upstream", buffer);
		}
		chain.nextUpstreamHandler(ws, buffer, frame);
	}

	/**
	 * Checks if is dump.
	 *
	 * @param mode the mode
	 * @return true, if is dump
	 */
	private boolean isDump(int mode){
		return (this.packetDumpMode & mode) > 0;
	}
	
	/**
	 * Prints the packet dump.
	 *
	 * @param title the title
	 * @param buffer the buffer
	 */
	private void printPacketDump(String title, ByteBuffer buffer){
		buffer.mark();
		
		// 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F
		int count = 0;
		String header = "               4         8        12        16";
		
		StringBuilder dump = new StringBuilder();
		dump.append(title);
		dump.append("\n");
		dump.append(header);
		dump.append("\n");
		while(buffer.hasRemaining()){
			byte[] line = new byte[16];
			StringBuilder dumpLine = new StringBuilder();
			dumpLine.append(lpad(Integer.toHexString(16 * count++), 5, "0"));
			dumpLine.append(":");
			int length = Math.min(buffer.remaining(), line.length);
			buffer.get(line, 0, length);
			for(int i = 0; i < length; i++){
				if(i % 2 == 0){
					dumpLine.append(" ");
				}
				dumpLine.append(lpad(hex(line[i]), 2, "0"));
			}
			dumpLine.append(" ");
			dump.append(rpad(dumpLine, header.length() + 3, " ") + dumpStr(line, length));
			dump.append("\n");
		}
		log.info(dump.toString());
		buffer.reset();
	}
	
	/** The hex table. */
	private static char[] hexTable = new char[]{'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	
	/**
	 * Hex.
	 *
	 * @param b the b
	 * @return the string
	 */
	private static String hex(byte b){
		char[] chars = new char[2];
		int d = (b & 0xF0) >> 4;
		int m =  b & 0x0F;
		chars[0] = hexTable[d];
		chars[1] = hexTable[m];
		return new String(chars);
	}
	
	/**
	 * Dump str.
	 *
	 * @param bytes the bytes
	 * @param length the length
	 * @return the string
	 */
	private static String dumpStr(byte[] bytes, int length){
		CharBuffer buf = CharBuffer.allocate(length);
		
		for(int i = 0; i < length; i++){
			if((0x00 <= bytes[i] && bytes[i] <= 0x1F) || bytes[i] == 0x7F || (bytes[i] & 0x80) > 0){
				buf.put(i, (char)0x2E);
			}else{
				buf.put(i, (char)bytes[i]);				
			}
		}
		return buf.toString();
	}
	
	/**
	 * Lpad.
	 *
	 * @param str the str
	 * @param len the len
	 * @param padding the padding
	 * @return the string
	 */
	private static String lpad(Object str, int len, String padding){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < len - str.toString().length(); i++){
			sb.append(padding);
		}
		sb.append(str);
		return sb.toString();
	}
	
	/**
	 * Rpad.
	 *
	 * @param str the str
	 * @param len the len
	 * @param padding the padding
	 * @return the string
	 */
	private static String rpad(Object str, int len, String padding){
		StringBuilder sb = new StringBuilder();
		sb.append(str);
		for(int i = 0; i < len - str.toString().length(); i++){
			sb.append(padding);
		}
		return sb.toString();
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		ByteBuffer buf = ByteBuffer.allocate(64);
		buf.put("01234567890-+=\n\rabcdefg".getBytes());
		buf.flip();
		PacketDumpStreamHandler p = new PacketDumpStreamHandler();
		p.printPacketDump("TEST", buf);
	}
}
