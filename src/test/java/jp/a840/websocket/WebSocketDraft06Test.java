package jp.a840.websocket;


import java.nio.ByteBuffer;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class WebSocketDraft06Test extends TestCase {
	@Test
	public void connect() throws Exception {
		MockServer ms = new MockServer(9999);
		ms.addRequest(new MockServer.VerifyRequest() {
			public boolean verify(byte[] request) {
				return true;
			}
		});
		ms.addResponse(toByteBuffer(
				"HTTP/1.1 101 Switching Protocols\r\n" +
				"Upgrade: websocket\r\n" +
				"Connection: Upgrade\r\n" +
				"Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=\r\n" +
				"Sec-WebSocket-Protocol: chat\r\n\r\n").array());
		ms.start();
		
		WebSocketHandlerMock handler = new WebSocketHandlerMock();
		WebSocketDraft06 ws = new WebSocketDraft06("ws://localhost:9999", handler, null);
		ws.setBlockingMode(false);
		ws.connect();
		Thread.sleep(3000);
		ws.close();

		if(!handler.getOnErrorList().isEmpty()){
			for(List l : handler.getOnErrorList()){
				((WebSocketException)l.get(1)).printStackTrace();
			}
			Assert.fail();
		}
		Assert.assertNull(ms.getThrowable());
		Assert.assertEquals(1, handler.getOnOpenList().size());
		Assert.assertEquals(0, handler.getOnMessageList().size());
		Assert.assertEquals(0, handler.getOnErrorList().size());
	}
	
	private ByteBuffer toByteBuffer(String str){
		return ByteBuffer.wrap(str.getBytes());
	}
}
