package jp.a840.websocket.handler;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.handshake.SSLHandshake;

public class SSLStreamHandler implements StreamHandler {

	private static Logger log = Logger.getLogger(SSLStreamHandler.class.getName());
	
	private ByteBuffer sslUpstreamBuffer;
	private ByteBuffer sslDownstreamBuffer;
	
	private SSLHandshake handshake;
	
	public SSLStreamHandler(SSLHandshake handshake, int bufferSize) throws WebSocketException {
		this.handshake = handshake;
		this.sslUpstreamBuffer = ByteBuffer.allocate(bufferSize);
		this.sslDownstreamBuffer = ByteBuffer.allocate(bufferSize);
	}
	
	public void nextHandshakeUpstreamHandler(WebSocket ws, ByteBuffer buffer,
			StreamHandlerChain chain) throws WebSocketException {
		handshake.wrap(buffer, sslUpstreamBuffer);
		chain.nextHandshakeUpstreamHandler(ws, sslUpstreamBuffer);
	}
	
	public void nextHandshakeDownstreamHandler(WebSocket ws, ByteBuffer buffer,
			StreamHandlerChain chain) throws WebSocketException {
		handshake.unwrap(buffer, sslDownstreamBuffer);
		chain.nextHandshakeDownstreamHandler(ws, sslDownstreamBuffer);
	}

	public void nextUpstreamHandler(WebSocket ws, ByteBuffer buffer,
			Frame frame, StreamHandlerChain chain) throws WebSocketException {
		handshake.wrap(buffer, sslUpstreamBuffer);
		chain.nextUpstreamHandler(ws, sslUpstreamBuffer, frame);
	}

	public void nextDownstreamHandler(WebSocket ws, ByteBuffer buffer,
			Frame frame, StreamHandlerChain chain) throws WebSocketException {
		handshake.unwrap(buffer, sslDownstreamBuffer);
		chain.nextDownstreamHandler(ws, sslDownstreamBuffer, frame);
	}

}
