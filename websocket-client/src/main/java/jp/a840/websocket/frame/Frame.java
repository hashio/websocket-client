package jp.a840.websocket.frame;

import java.nio.ByteBuffer;

import jp.a840.websocket.frame.draft06.BinaryFrame;
import jp.a840.websocket.frame.draft06.TextFrame;

abstract public class Frame {

	protected FrameHeader header;
	protected byte[] body;
	
	protected Frame(){
	}
	
	protected Frame(FrameHeader header, byte[] body){
		this.header = header;
		this.body = body;
	}

	public long getBodyLength(){
		return header.getBodyLength();
	}
	
	public FrameHeader getHeader(){
		return header;
	}
	
	/**
	 * convert frame to byte buffer
	 * 
	 * @return
	 */
	abstract public ByteBuffer toByteBuffer();

	public ByteBuffer getRawBody(){
		return ByteBuffer.wrap(body);
	}

	protected void setHeader(FrameHeader header) {
		this.header = header;
	}

	protected void setBody(byte[] body) {
		this.body = body;
	}
	
	public static BinaryFrame createBinaryFrame(byte[] body){
		return new BinaryFrame(body);
	}
	
	public static TextFrame createTextFrame(String str){
		return new TextFrame(str);
	}
}
