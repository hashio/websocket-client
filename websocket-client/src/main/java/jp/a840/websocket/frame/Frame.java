package jp.a840.websocket.frame;

import java.nio.ByteBuffer;

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
}
