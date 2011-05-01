package jp.a840.websocket.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StringUtil {
	private static Logger log = Logger.getLogger(StringUtil.class.getName());
	
	public static String join(String delim, Collection<String> collections) {
		String[] values = new String[collections.size()];
		collections.toArray(values);
		return join(delim, 0, collections.size(), values);
	}

	public static String join(String delim, String... strings) {
		return join(delim, 0, strings.length, strings);
	}

	public static String join(String delim, int start, int end,
			String... strings) {
		if (strings.length == 1) {
			return strings[0];
		}
		StringBuilder sb = new StringBuilder(strings[start]);
		for (int i = start + 1; i < end; i++) {
			sb.append(delim).append(strings[i]);
		}
		return sb.toString();
	}

	public static void addHeader(StringBuilder sb, String key, String value) {
		// TODO need folding?
		sb.append(key + ": " + value + "\r\n");
	}
	
	public static String readLine(ByteBuffer buf) {
		boolean completed = false;
		buf.mark();
		while (buf.hasRemaining() && !completed) {
			byte b = buf.get();
			if (b == '\r') {
				if(buf.hasRemaining() && buf.get() == '\n'){
					completed = true;
				}
			}
		}

		if(!completed){
			return null;
		}

		int limit = buf.position();
		buf.reset();
		int length = limit - buf.position();
		byte[] tmp = new byte[length];
		buf.get(tmp, 0, length);
		try {
			String line = new String(tmp, "US-ASCII");
			if (log.isLoggable(Level.FINE)) {
				log.fine(line.trim());
			}
			return line;
		} catch (UnsupportedEncodingException e) {
			;
		}
		return null;
	}

}
