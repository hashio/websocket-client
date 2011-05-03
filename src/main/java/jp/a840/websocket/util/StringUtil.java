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
package jp.a840.websocket.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class StringUtil.
 *
 * @author Takahiro Hashimoto
 */
public class StringUtil {
	
	/** The log. */
	private static Logger log = Logger.getLogger(StringUtil.class.getName());
	
	/**
	 * Join.
	 *
	 * @param delim the delim
	 * @param collections the collections
	 * @return the string
	 */
	public static String join(String delim, Collection<String> collections) {
		String[] values = new String[collections.size()];
		collections.toArray(values);
		return join(delim, 0, collections.size(), values);
	}

	/**
	 * Join.
	 *
	 * @param delim the delim
	 * @param strings the strings
	 * @return the string
	 */
	public static String join(String delim, String... strings) {
		return join(delim, 0, strings.length, strings);
	}

	/**
	 * Join.
	 *
	 * @param delim the delim
	 * @param start the start
	 * @param end the end
	 * @param strings the strings
	 * @return the string
	 */
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

	/**
	 * Adds the header.
	 *
	 * @param sb the sb
	 * @param key the key
	 * @param value the value
	 */
	public static void addHeader(StringBuilder sb, String key, String value) {
		// TODO need folding?
		sb.append(key + ": " + value + "\r\n");
	}
	
	/**
	 * Read line.
	 *
	 * @param buf the buf
	 * @return the string
	 */
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
