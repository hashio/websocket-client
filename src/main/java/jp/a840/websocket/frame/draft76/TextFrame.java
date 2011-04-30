package jp.a840.websocket.frame.draft76;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import jp.a840.websocket.frame.FrameHeader;

public class TextFrame extends FrameDraft76 {

	private String convertedString;

	public TextFrame(String str) {
		super();
		byte[] body = convertStringToByteArray(str);
		FrameHeaderDraft76 header = new FrameHeaderDraft76((byte)0x00, body.length + 1);
		setHeader(header);
		setBody(body);
	}
	
	public TextFrame(FrameHeader header, byte[] body){
		super(header, stripTerminateFlag(body));
	}

	private static byte[] stripTerminateFlag(byte[] body){
		if(body[body.length - 1] == (byte)0xFF){
			byte[] tmp = new byte[body.length - 1];
			System.arraycopy(body, 0, tmp, 0, body.length - 2);
			body = tmp;
		}
		return body;
	}
	
	private static byte[] convertStringToByteArray(String str){
		try{
			return str.getBytes("UTF-8");
		}catch(UnsupportedEncodingException e){
			;
		}
		return null;
	}

	public ByteBuffer toByteBuffer() {
		ByteBuffer buf = ByteBuffer.allocate(1 + body.length + 1);
		buf.put(header.toByteBuffer());
		buf.put(body);
		buf.put((byte)0xFF);
		buf.flip();
		return buf;
	}
	
	public String toString(){
		if(convertedString == null){
			try{
				convertedString = new String(body, "UTF-8");
			}catch(UnsupportedEncodingException e){
				convertedString = "";
			}
		}
		return convertedString;
	}
}
