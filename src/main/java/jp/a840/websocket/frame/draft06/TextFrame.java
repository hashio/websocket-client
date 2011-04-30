package jp.a840.websocket.frame.draft06;

import java.io.UnsupportedEncodingException;

import jp.a840.websocket.frame.draft06.FrameBuilderDraft06.Opcode;


public class TextFrame extends FrameDraft06 {

	private String convertedString;
	
	public TextFrame(FrameHeaderDraft06 header, byte[] bodyData) {
		super(header, bodyData);
	}

	public TextFrame(String str){
		super();
		byte[] body = convertStringToByteArray(str);
		setHeader(FrameBuilderDraft06.createFrameHeader(body, false, Opcode.TEXT_FRAME));
		setBody(body);
	}
	
	private static byte[] convertStringToByteArray(String str){
		try{
			return str.getBytes("UTF-8");
		}catch(UnsupportedEncodingException e){
			;
		}
		return null;
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
