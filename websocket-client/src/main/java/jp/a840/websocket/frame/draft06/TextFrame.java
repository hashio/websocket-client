package jp.a840.websocket.frame.draft06;

import java.io.UnsupportedEncodingException;


public class TextFrame extends FrameDraft06 {

	public TextFrame(FrameHeaderDraft06 header, byte[] bodyData) {
		super(header, bodyData);
	}

	public TextFrame(String str){
		super(convertStringToByteArray(str));
	}
	
	private static byte[] convertStringToByteArray(String str){
		try{
			return str.getBytes("UTF-8");
		}catch(UnsupportedEncodingException e){
			;
		}
		return null;
	}

}
