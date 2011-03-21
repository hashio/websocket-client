package jp.a840.websocket.frame.draft76;

import java.nio.ByteBuffer;

import jp.a840.websocket.frame.FrameHeader;

public class BinaryFrame extends FrameDraft76 {

	public BinaryFrame(byte[] bodyData) {
		super();
		FrameHeaderDraft76 header = new FrameHeaderDraft76((byte)0xFF, bodyData.length);
		setHeader(header);
		setBody(bodyData);
	}
	
	protected BinaryFrame(FrameHeader header, byte[] bodyData){
		super(header, bodyData);
	}

	@Override
	public ByteBuffer toByteBuffer() {
		byte[] bodyLengthBuf = getBodyLength(body);
		ByteBuffer buf = ByteBuffer.allocate(1 + bodyLengthBuf.length + body.length);
		buf.put(header.toByteBuffer());
		buf.put(bodyLengthBuf);
		buf.put(body);
		buf.flip();
		return buf;
	}
	
	private static byte[] getBodyLength(byte[] body){
		byte[] tmp = new byte[body.length / 7 + 1];
		int length = body.length;
		int i = 0;
		while(length != 0){
			tmp[i] = (byte)((length | 0x7F) | 0x80);
			length = length >> 7;
			i++;
		}
		i--;
		tmp[i] = (byte)(tmp[i] | 0x7F);
		byte[] bodyLengthBuf = new byte[i];
		System.arraycopy(tmp, 0, bodyLengthBuf, 0, bodyLengthBuf.length);
		return bodyLengthBuf;
	}
}
