package jp.a840.websocket;

import java.nio.ByteBuffer;

/**
 * Fragmented buffer management class
 * management fragment buffer and recycle it if occur fragmented buffer when received websocket data.
 * 
 * @author t-hashimoto
 */
public class BufferManager {
	private ByteBuffer fragmentBuf;

	/**
	 * init management fragment buffer
	 */
	public void init(){
		if(this.fragmentBuf != null){
			this.fragmentBuf.clear();
		}
		this.fragmentBuf = null;
	}

	/**
	 * Get merged fragmented buffer and argument buffer.
	 * if does not have a management fragment buffer then return take a argument buffer
	 * @param buffer
	 * @return merged buffer management(if have) and argument.
	 */
	public ByteBuffer getBuffer(ByteBuffer buffer){
		if (fragmentBuf != null) {
			fragmentBuf.rewind();
			int len = fragmentBuf.remaining() + buffer.remaining();
			byte[] buf = new byte[len];
			fragmentBuf.get(buf, 0, fragmentBuf.remaining());
			buffer.get(buf, fragmentBuf.position(), buffer.remaining());
			buffer = ByteBuffer.wrap(buf);
		}
		return buffer;
	}
	
	/**
	 * set buffer to management buffer
	 * @param buffer
	 */
	public void storeFragmentBuffer(ByteBuffer buffer){
		this.fragmentBuf = ByteBuffer.allocate(buffer.limit() - buffer.position());
		this.fragmentBuf.put(buffer);
	}
}
