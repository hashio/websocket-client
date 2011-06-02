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


import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

import jp.a840.websocket.frame.draft06.ConnectionCloseFrame;
import jp.a840.websocket.handler.MaskFrameStreamHandler;
import jp.a840.websocket.handler.PacketDumpStreamHandler;
import jp.a840.websocket.util.PacketDumpUtil;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import util.Base64;

/**
 * The Class WebSocketDraft06Test.
 *
 * @author Takahiro Hashimoto
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Base64.class)
public class WebSocketDraft06Test extends TestCase {
	
	/**
	 * Connect.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void connect() throws Exception {
		System.setProperty("websocket.packatdump", String.valueOf(
			PacketDumpUtil.ALL
		));

		Mockito.mock(MaskFrameStreamHandler.class);
		
		PowerMockito.mockStatic(Base64.class);
		when(Base64.encodeToString(any(byte[].class), anyBoolean())).thenReturn("TESTKEY");
		
		PowerMockito.mockStatic(Random.class);
		
		MockServer ms = new MockServer(9999);
		// handshake request
		ms.addRequest(new MockServer.VerifyRequest() {
			public void verify(ByteBuffer request) {
			}
		});
		// handshake response
		ms.addResponse(toByteBuffer(
				"HTTP/1.1 101 Switching Protocols\r\n" +
				"Upgrade: websocket\r\n" +
				"Connection: Upgrade\r\n" +
				"Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=\r\n" +
				"Sec-WebSocket-Protocol: chat\r\n\r\n"));
		// send close frame
		ms.addRequest(new MockServer.VerifyRequest() {
			public void verify(ByteBuffer request) {
				ByteBuffer expected = ByteBuffer.allocate(6);
				expected.put(request.slice().array(), 0, 4);
				expected.put((byte)(request.get(0) ^ 0x81));
				expected.put((byte)(request.get(1) ^ 0x00));
				expected.flip();
				Assert.assertEquals("Not equals close frame.", expected, request.slice());
			}
		});
		ms.addConnectionClose(new ConnectionCloseFrame().toByteBuffer());

		ms.start();
		
		WebSocketHandlerMock handler = new WebSocketHandlerMock();
		WebSocketDraft06 ws = new WebSocketDraft06("ws://localhost:9999", handler, null);
		ws.setBlockingMode(false);
		ws.connect();
		ws.close();

		if(!handler.getOnErrorList().isEmpty()){
			for(List l : handler.getOnErrorList()){
				((WebSocketException)l.get(1)).printStackTrace();
			}
			Assert.fail();
		}
		Throwable t = ms.getThrowable();
		if(t != null){
			t.printStackTrace();
			Assert.fail(t.getMessage());
		}
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
