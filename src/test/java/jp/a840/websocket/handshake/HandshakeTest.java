package jp.a840.websocket.handshake;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import jp.a840.websocket.WebSocketException;

import org.junit.Assert;
import org.junit.Test;


public class HandshakeTest {

	@Test
	public void handshake1() throws Exception {
		String request = "Test Request";
		SocketChannel socket = mock(SocketChannel.class);

		when(socket.write(any(ByteBuffer.class))).thenReturn(request.length());
		
		TestHandshake handshake = new TestHandshake(request);
		handshake.handshake(socket);
		
		verify(socket).write(ByteBuffer.wrap(request.getBytes()));
	}

	@Test
	public void handshakeError1() throws Exception {
		String request = "Test Request";
		SocketChannel socket = mock(SocketChannel.class);

		when(socket.write(any(ByteBuffer.class))).thenThrow(new IOException());
		
		TestHandshake handshake = new TestHandshake(request);
		try{
			handshake.handshake(socket);
		}catch(WebSocketException e){
			Assert.assertEquals(3100, e.getStatusCode());
		}
	}
	
	@Test
	public void handshakeResponse1() throws Exception {
		TestHandshake handshake = new TestHandshake();
		Assert.assertTrue(handshake.handshakeResponse(toByteBuffer(
					"HTTP/1.1 101 Switching Protocols\r\n" +
					"Upgrade: websocket\r\n" +
					"Connection: Upgrade\r\n" +
					"Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=\r\n" +
					"Sec-WebSocket-Protocol: chat\r\n\r\n")));
	}
	
	@Test
	public void handshakeResponse2() throws Exception {
		TestHandshake handshake = new TestHandshake();
		Assert.assertFalse(handshake.handshakeResponse(toByteBuffer(
					"HTTP/1.1 101 Switching Protocols\r\n" +
					"Upgrade: websocket\r\n" +
					"Connection: Upgrade\r\n" +
					"Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=\r\n" +
					"Sec-WebSocket-Protocol: chat\r\n")));
		Assert.assertTrue(handshake.handshakeResponse(toByteBuffer(
				"\r\n")));
	}
	
	@Test
	public void handshakeResponse3() throws Exception {
		TestHandshake handshake = new TestHandshake();
		Assert.assertFalse(handshake.handshakeResponse(toByteBuffer("H")));
		Assert.assertFalse(handshake.handshakeResponse(toByteBuffer(
					"TTP/1.1 101 Switching Protocols\r\n" +
					"Upgrade: websocket\r\n" +
					"Connection: Upgrade\r\n" +
					"Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=\r\n" +
					"Sec-WebSocket-Protocol: chat\r\n")));
		Assert.assertTrue(handshake.handshakeResponse(toByteBuffer(
				"\r\n")));
	}
	
	@Test
	public void handshakeResponseError1() throws Exception {
		TestHandshake handshake = new TestHandshake();
		try{
			handshake.handshakeResponse(toByteBuffer(
					"HTTP/1.0 101 Switching Protocols\r\n" +
					"Upgrade: websocket\r\n" +
					"Connection: Upgrade\r\n" +
					"Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=\r\n" +
					"Sec-WebSocket-Protocol: chat\r\n"));
		}catch(WebSocketException e){
			Assert.assertEquals(3101, e.getStatusCode());
		}
	}
	
	@Test
	public void handshakeResponseError2() throws Exception {
		TestHandshake handshake = new TestHandshake();
		Assert.assertFalse(handshake.handshakeResponse(toByteBuffer("H")));
		try{
			handshake.handshakeResponse(toByteBuffer(
					"TTP/1.1 999 Switching Protocols\r\n" +
					"Upgrade: websocket\r\n" +
					"Connection: Upgrade\r\n" +
					"Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=\r\n" +
					"Sec-WebSocket-Protocol: chat\r\n\r\n"));
		}catch(WebSocketException e){
			Assert.assertEquals(3102, e.getStatusCode());
		}
	}

	private class TestHandshake extends Handshake {
		private String request_;
		
		public TestHandshake(){
		}
		
		public TestHandshake(String request){
			request_ = request;
		}
		
		@Override
		public ByteBuffer createHandshakeRequest() throws WebSocketException {
			return toByteBuffer(request_);
		}
	}
	
	private ByteBuffer toByteBuffer(String str){
		return ByteBuffer.wrap(str.getBytes());
	}
}
