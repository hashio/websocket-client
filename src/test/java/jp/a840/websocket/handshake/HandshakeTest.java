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



/**
 * The Class HandshakeTest.
 *
 * @author Takahiro Hashimoto
 */
public class HandshakeTest {

	/**
	 * Handshake1.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void handshake1() throws Exception {
		String request = "Test Request";
		
		TestHandshake handshake = new TestHandshake(request);
		Assert.assertEquals(request.getBytes(), handshake.createHandshakeRequest().array());
	}
	
	/**
	 * Handshake response1.
	 *
	 * @throws Exception the exception
	 */
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
	
	/**
	 * Handshake response2.
	 *
	 * @throws Exception the exception
	 */
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
	
	/**
	 * Handshake response3.
	 *
	 * @throws Exception the exception
	 */
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
	
	/**
	 * Handshake response error1.
	 *
	 * @throws Exception the exception
	 */
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
	
	/**
	 * Handshake response error2.
	 *
	 * @throws Exception the exception
	 */
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

	/**
	 * The Class TestHandshake.
	 *
	 * @author Takahiro Hashimoto
	 */
	private class TestHandshake extends Handshake {
		
		/** The request_. */
		private String request_;
		
		/**
		 * Instantiates a new test handshake.
		 */
		public TestHandshake(){
		}
		
		/**
		 * Instantiates a new test handshake.
		 *
		 * @param request the request
		 */
		public TestHandshake(String request){
			request_ = request;
		}
		
		/* (non-Javadoc)
		 * @see jp.a840.websocket.handshake.Handshake#createHandshakeRequest()
		 */
		@Override
		public ByteBuffer createHandshakeRequest() throws WebSocketException {
			return toByteBuffer(request_);
		}
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
