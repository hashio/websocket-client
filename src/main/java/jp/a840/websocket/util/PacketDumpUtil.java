package jp.a840.websocket.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.logging.Logger;

import jp.a840.websocket.WebSocketBase;

public class PacketDumpUtil {
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
	private static Logger log = Logger.getLogger(PacketDumpUtil.class.getName());

	/**
	 * Checks if is dump.
	 *
	 * @param mode the mode
	 * @return true, if is dump
	 */
	public static boolean isDump(int mode){
		return (WebSocketBase.getPacketDumpMode() & mode) > 0;
	}

	/**
	 * Prints the packet dump.
	 *
	 * @param title the title
	 * @param buffer the buffer
	 */
	public static void printPacketDump(String title, ByteBuffer buffer){
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
		printPacketDump("TEST", buf);
	}

}
