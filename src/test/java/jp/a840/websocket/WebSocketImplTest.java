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


import jp.a840.websocket.exception.WebSocketException;
import jp.a840.websocket.frame.rfc6455.BinaryFrame;
import jp.a840.websocket.frame.rfc6455.CloseFrame;
import jp.a840.websocket.frame.rfc6455.TextFrame;
import jp.a840.websocket.impl.WebSocketImpl;
import jp.a840.websocket.impl.WebSocketImpl;
import jp.a840.websocket.proxy.Proxy;
import jp.a840.websocket.util.PacketDumpUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import util.Base64;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

/**
 * The Class WebSocketImplTest.
 *
 * @author Takahiro Hashimoto
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Base64.class)
public class WebSocketImplTest extends TestCase {
	
	/** The ms. */
	MockServer ms;

    int version = 13;

	/**
	 * Start mock server.
	 */
	@Before
	public void startMockServer(){
		ms = new MockServer(9999, this.version);
	}
	
	/**
	 * Stop mock server.
	 *
	 * @throws Exception the exception
	 */
	@After
	public void stopMockServer() throws Exception {
		ms.join(100000);
		Assert.assertFalse(ms.isAlive());
	}
	
	/**
	 * Connect.
	 * connect -> handshake req -> handshake res -> close req -> close res -> terminate
	 * @throws Exception the exception
	 */
	@Test
	public void connect1() throws Exception {
		System.setProperty("websocket.packatdump", String.valueOf(
			PacketDumpUtil.ALL
		));

		PowerMockito.mockStatic(Base64.class);
		when(Base64.encodeToString(any(byte[].class), anyBoolean())).thenReturn("TESTKEY");
				
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
				ByteBuffer expected = ByteBuffer.allocate(2);
				expected.put((byte)(0x88));
				expected.put((byte)(0x00));
				expected.flip();
				Assert.assertEquals("Not equal close frame.", expected, request.slice());
			}
		});
		ms.addResponse(new CloseFrame().toByteBuffer());
		ms.addClose((ByteBuffer)null);

		ms.start();
		
		WebSocketHandlerMock handler = new WebSocketHandlerMock();
		WebSocketImpl ws = new WebSocketImpl("ws://localhost:9999", handler);
		ws.setBlockingMode(false);
		ws.connect();
		ws.close();

        Throwable t = ms.getThrowable();
      		if(t != null){
      			t.printStackTrace();
      			Assert.fail();
      		}
		if(!handler.getOnErrorList().isEmpty()){
			for(List l : handler.getOnErrorList()){
				((WebSocketException)l.get(1)).printStackTrace();
			}
			Assert.fail();
		}
		Assert.assertEquals(1, handler.getOnOpenList().size());
		Assert.assertEquals(0, handler.getOnMessageList().size());
		Assert.assertEquals(0, handler.getOnErrorList().size());
		Assert.assertEquals(1, handler.getOnCloseList().size());
	}
	
	/**
	 * Connect.
	 * connect -> handshake req -> handshake res -> frame response -> close req -> close res -> terminate
	 * @throws Exception the exception
	 */
	@Test
	public void connect2() throws Exception {
		System.setProperty("websocket.packatdump", String.valueOf(
			PacketDumpUtil.ALL
		));

//		PowerMockito.mockStatic(Base64.class);
//		when(Base64.encodeToString(any(byte[].class), anyBoolean())).thenReturn("TESTKEY");
				
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
		ms.addResponse(new TextFrame("TEST FRAMEテストフレーム"));
		// send close frame
		ms.addRequest(new MockServer.VerifyRequest() {
			public void verify(ByteBuffer request) {
				ByteBuffer expected = ByteBuffer.allocate(2);
				expected.put((byte)(0x88));
				expected.put((byte)(0x80));
				expected.flip();
				Assert.assertEquals("Not equal close frame.", expected, request.slice());
			}
		});
		ms.addClose(new CloseFrame());

		ms.start();
		
		WebSocketHandlerMock handler = new WebSocketHandlerMock();
		WebSocketImpl ws = new WebSocketImpl("ws://localhost:9999", handler);
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
		Assert.assertEquals(1, handler.getOnMessageList().size());
		Assert.assertEquals(handler.getOnMessageList().get(0).get(1).toString(), "TEST FRAMEテストフレーム");
		Assert.assertEquals(0, handler.getOnErrorList().size());
		Assert.assertEquals(1, handler.getOnCloseList().size());
	}
	
	/**
	 * Connect.
	 * connect -> handshake req -> handshake res -> close res -> close req -> terminate
	 * @throws Exception the exception
	 */
	@Test
	public void connect3() throws Exception {
		System.setProperty("websocket.packatdump", String.valueOf(
			PacketDumpUtil.ALL
		));

		PowerMockito.mockStatic(Base64.class);
		when(Base64.encodeToString(any(byte[].class), anyBoolean())).thenReturn("TESTKEY");
				
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
		// close frame response
		ms.addResponse(new CloseFrame().toByteBuffer());
		// reply close frame request
		ms.addRequest(new MockServer.VerifyRequest() {
			public void verify(ByteBuffer request) {
				ByteBuffer expected = ByteBuffer.allocate(2);
				expected.put((byte)(0x88));
				expected.put((byte)(0x80));
				expected.flip();
				Assert.assertEquals("Not equal close frame.", expected, request.slice());
			}
		});
		// close frame response
		ms.addClose((ByteBuffer)null);

		ms.start();
		
		WebSocketHandlerMock handler = new WebSocketHandlerMock();
		WebSocketImpl ws = new WebSocketImpl("ws://localhost:9999", handler);
		ws.setBlockingMode(false);
		ws.connect();
		ws.awaitClose();

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
		Assert.assertEquals(1, handler.getOnCloseList().size());
	}
	
	/**
	 * Connect.
	 * connect -> handshake req -> handshake res -> frame req -> frame res -> close res -> close req -> terminate
	 * @throws Exception the exception
	 */
	@Test
	public void connect4() throws Exception {
		System.setProperty("websocket.packatdump", String.valueOf(
			PacketDumpUtil.ALL
		));

		PowerMockito.mockStatic(Base64.class);
		when(Base64.encodeToString(any(byte[].class), anyBoolean())).thenReturn("TESTKEY");
				
		// handshake request
		ms.addRequest(new MockServer.VerifyRequest() {
			public void verify(ByteBuffer request) {
			}
		}, version);
		// handshake response
		ms.addResponse(toByteBuffer(
				"HTTP/1.1 101 Switching Protocols\r\n" +
				"Upgrade: websocket\r\n" +
				"Connection: Upgrade\r\n" +
				"Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=\r\n" +
				"Sec-WebSocket-Protocol: chat\r\n\r\n"));
		// binary frame request
		final BinaryFrame testRequestFrame = new BinaryFrame("TEST FRAME".getBytes());
		ms.addRequest(new MockServer.VerifyRequest() {
			public void verify(ByteBuffer request) {
				Assert.assertEquals(testRequestFrame.toByteBuffer().slice(), request.slice());
			}
		});
		// binary frame response
		BinaryFrame testResponseFrame = new BinaryFrame("TEST FRAME-RES".getBytes());
		CloseFrame closeFrame = new CloseFrame();
		int size = testResponseFrame.toByteBuffer().remaining();
		size += closeFrame.toByteBuffer().remaining();
		ByteBuffer buf = ByteBuffer.allocate(size);
		
		buf.put(testResponseFrame.toByteBuffer());
		buf.put(closeFrame.toByteBuffer());
		buf.flip();
		// binary frame and close frame response
		ms.addResponse(buf);
		
		// reply close frame request
		ms.addRequest(new MockServer.VerifyRequest() {
			public void verify(ByteBuffer request) {
				ByteBuffer expected = ByteBuffer.allocate(2);
				expected.put((byte)(0x88));
				expected.put((byte)(request.get(1) ^ 0x00));
				expected.flip();
				Assert.assertEquals("Not equal close frame.", expected, request.slice());
			}
		});
		// close frame response
		ms.addClose((ByteBuffer)null);

		ms.start();
		
		WebSocketHandlerMock handler = new WebSocketHandlerMock();
		WebSocketImpl ws = new WebSocketImpl("ws://localhost:9999", handler);
		ws.setBlockingMode(false);
		ws.connect();
		ws.send(testRequestFrame);
		ws.awaitClose();
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
		Assert.assertEquals(1, handler.getOnMessageList().size());
		Assert.assertArrayEquals("TEST FRAME-RES".getBytes(), ((BinaryFrame)handler.getOnMessageList().get(0).get(1)).getContents().array());
		Assert.assertEquals(0, handler.getOnErrorList().size());
		Assert.assertEquals(1, handler.getOnCloseList().size());
		
	}
	
	/**
	 * Connect.
	 * connect -> handshake req -> handshake res -> frame req -> frame res -> close res -> close req -> terminate
	 * @throws Exception the exception
	 */
	@Test
	public void connect5() throws Exception {
		System.setProperty("websocket.packatdump", String.valueOf(
			PacketDumpUtil.ALL
		));

		PowerMockito.mockStatic(Base64.class);
		when(Base64.encodeToString(any(byte[].class), anyBoolean())).thenReturn("TESTKEY");
				
		// handshake request
		ms.addRequest(new MockServer.VerifyRequest() {
			public void verify(ByteBuffer request) {
			}
		}, this.version);
		// handshake response
		ms.addResponse(toByteBuffer(
				"HTTP/1.1 101 Switching Protocols\r\n" +
				"Upgrade: websocket\r\n" +
				"Connection: Upgrade\r\n" +
				"Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=\r\n" +
				"Sec-WebSocket-Protocol: chat\r\n\r\n"));
		// binary frame request
		final BinaryFrame testRequestFrame = new BinaryFrame("TEST FRAME".getBytes());
		ms.addRequest(new MockServer.VerifyRequest() {
			public void verify(ByteBuffer request) {
				Assert.assertEquals(testRequestFrame.toByteBuffer().slice(), request.slice());
			}
		});
		// binary frame response
		BinaryFrame testResponseFrame = new BinaryFrame("TEST FRAME-RES".getBytes());
		ms.addResponse(testResponseFrame.toByteBuffer());
		// close frame response
		ms.addResponse(new CloseFrame().toByteBuffer());
		
		// reply close frame request
		ms.addRequest(new MockServer.VerifyRequest() {
			public void verify(ByteBuffer request) {
				ByteBuffer expected = ByteBuffer.allocate(2);
				expected.put((byte)(0x88));
				expected.put((byte)(request.get(1) ^ 0x00));
				expected.flip();
				Assert.assertEquals("Not equal close frame.", expected, request.slice());
			}
		});
		// close frame response
		ms.addClose((ByteBuffer)null);

		ms.start();
		
		WebSocketHandlerMock handler = new WebSocketHandlerMock();
		WebSocketImpl ws = new WebSocketImpl("ws://localhost:9999", handler);
		ws.setBlockingMode(false);
		ws.connect();
		ws.send(testRequestFrame);
		ws.awaitClose();
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
		Assert.assertEquals(1, handler.getOnMessageList().size());
		Assert.assertArrayEquals("TEST FRAME-RES".getBytes(), ((BinaryFrame)handler.getOnMessageList().get(0).get(1)).getContents().array());
		Assert.assertEquals(0, handler.getOnErrorList().size());
		Assert.assertEquals(1, handler.getOnCloseList().size());
		
	}

	/**
	 * Connect.
	 * connect -> proxy req -> proxy res -> handshake req -> handshake res -> close req -> close res -> terminate
	 * @throws Exception the exception
	 */
	@Test
	public void connectProxy1() throws Exception {
		System.setProperty("websocket.packatdump", String.valueOf(
			PacketDumpUtil.ALL
		));

		PowerMockito.mockStatic(Base64.class);
		when(Base64.encodeToString(any(byte[].class), anyBoolean())).thenReturn("TESTKEY");
				
		// proxy request
		ms.addHttpRequest(new MockServer.VerifyRequest() {
			public void verify(ByteBuffer request) {
				Assert.assertEquals(toByteBuffer(
						"CONNECT localhost:9999 HTTP/1.1\r\n" +
						"Host: localhost:9999\r\n\r\n"
						),
						request.slice());
			}
		},this.version);
		// proxy response
		ms.addResponse(toByteBuffer(
				"HTTP/1.0 200 Connection Established\r\n" +
				"Proxy-agent: Mock Proxy Server\r\n\r\n"
				));
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
				ByteBuffer expected = ByteBuffer.allocate(2);
				expected.put((byte)(0x88));
				expected.put((byte)(0x80));
				expected.flip();
				Assert.assertEquals("Not equal close frame.", expected, request.slice());
			}
		});
		ms.addResponse(new CloseFrame().toByteBuffer());
		ms.addClose((ByteBuffer)null);

		ms.start();
		
		WebSocketHandlerMock handler = new WebSocketHandlerMock();
		InetSocketAddress proxy = new InetSocketAddress("localhost", 9999);
		WebSocketImpl ws = new WebSocketImpl("ws://localhost:9999", null, new Proxy(proxy), handler, (String)null);
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
		Assert.assertEquals(1, handler.getOnCloseList().size());
	}

	/**
	 * Connect.
	 * connect -> proxy req -> proxy auth require res -> proxy authorize req -> proxy authorize res -> handshake req -> handshake res -> close req -> close res -> terminate
	 * @throws Exception the exception
	 */
	@Test
	public void connectProxyAuth1() throws Exception {
		System.setProperty("websocket.packatdump", String.valueOf(
			PacketDumpUtil.ALL
		));

		PowerMockito.mockStatic(Base64.class);
		when(Base64.encodeToString(any(byte[].class), anyBoolean())).thenReturn("TESTKEY");
				
		// proxy handshake request
		ms.addHttpRequest(new MockServer.VerifyRequest() {
			public void verify(ByteBuffer request) {
				Assert.assertEquals(toByteBuffer(
						"CONNECT localhost:9999 HTTP/1.1\r\n" +
						"Host: localhost:9999\r\n\r\n"
						),
						request.slice());
			}
		},this.version);
		// handshake response
		ms.addResponse(toByteBuffer(
				"HTTP/1.1 407 Proxy Authentication Required\r\n" +
				"Proxy-Authenticate: Basic realm=\"Test Realm\"\r\n\r\n"
			));
		// proxy handshake request
		ms.addHttpRequest(new MockServer.VerifyRequest() {
			public void verify(ByteBuffer request) {
				Assert.assertEquals(toByteBuffer(
						"CONNECT localhost:9999 HTTP/1.1\r\n" +
						"Host: localhost:9999\r\n" +
						"Proxy-Authorization: Basic TESTKEY\r\n\r\n"
						),
						request.slice());
			}
		},this.version);
		// proxy response
		ms.addResponse(toByteBuffer(
				"HTTP/1.0 200 Connection Established\r\n" +
				"Proxy-agent: Mock Proxy Server\r\n\r\n"
				));
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
				ByteBuffer expected = ByteBuffer.allocate(2);
				expected.put((byte)(0x88));
				expected.put((byte)(0x80));
				expected.flip();
				Assert.assertEquals("Not equal close frame.", expected, request.slice());
			}
		});
		ms.addResponse(new CloseFrame());
		ms.addClose((ByteBuffer)null);

		ms.start();
		
		WebSocketHandlerMock handler = new WebSocketHandlerMock();
		InetSocketAddress proxy = new InetSocketAddress("localhost", 9999);
		WebSocketImpl ws = new WebSocketImpl("ws://localhost:9999", null, new Proxy(proxy, "test", "test"), handler, (String)null);
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
		Assert.assertEquals(1, handler.getOnCloseList().size());
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
