package jp.a840.websocket.handshake;

import java.nio.ByteBuffer;

import jp.a840.websocket.HttpHeader;
import jp.a840.websocket.util.StringUtil;

public class HttpResponseHeaderParser {
	private HttpHeader responseHeader = new HttpHeader();

	private boolean complete = false;
	
	public void parse(ByteBuffer buffer){
		if(complete){
			throw new IllegalStateException("Parser already parse completed");
		}
		do {
			String line = StringUtil.readLine(buffer);
			if(line == null){				
				return;
			}
			if (line.indexOf(':') > 0) {
				String[] keyValue = line.split(":", 2);
				if (keyValue.length > 1) {
					responseHeader.addHeader(keyValue[0].trim().toLowerCase(),
							keyValue[1].trim().toLowerCase());
				}
			}
			if ("\r\n".compareTo(line) == 0) {
				complete = true;
				return;
			}
			if (!buffer.hasRemaining()) {
				return;
			}
		} while (true);
	}
	
	public boolean isCompleted(){
		return complete;
	}
	
	public HttpHeader getResponseHeader(){
		return responseHeader;
	}
}
