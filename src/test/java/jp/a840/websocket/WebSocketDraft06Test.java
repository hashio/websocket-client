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
package jp.a840.websocket;


import java.nio.ByteBuffer;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * The Class WebSocketDraft06Test.
 *
 * @author Takahiro Hashimoto
 */
public class WebSocketDraft06Test extends TestCase {
	
	/**
	 * Connect.
	 *
	 * @throws Exception the exception
	 */
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
	
	/**
	 * To byte buffer.
	 *
	 * @param str the str
	 * @return the byte buffer
	 */
	private ByteBuffer toByteBuffer(String str){
		return ByteBuffer.wrap(str.getBytes());
	}
}
