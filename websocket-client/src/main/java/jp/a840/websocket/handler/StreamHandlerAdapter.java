package jp.a840.websocket.handler;

import java.nio.ByteBuffer;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.frame.Frame;

public class StreamHandlerAdapter implements StreamHandler {

	public void nextHandshakeUpstreamHandler(WebSocket ws, ByteBuffer buffer,
			StreamHandlerChain chain) throws WebSocketException {
		chain.nextHandshakeUpstreamHandler(ws, buffer);
	}

	public void nextHandshakeDownstreamHandler(WebSocket ws, ByteBuffer buffer,
			StreamHandlerChain chain) throws WebSocketException {
		chain.nextHandshakeDownstreamHandler(ws, buffer);
	}

	public void nextUpstreamHandler(WebSocket ws, ByteBuffer buffer,
			Frame frame, StreamHandlerChain chain) throws WebSocketException {
		chain.nextUpstreamHandler(ws, buffer, frame);
	}

	public void nextDownstreamHandler(WebSocket ws, ByteBuffer buffer,
			Frame frame, StreamHandlerChain chain) throws WebSocketException {
		chain.nextDownstreamHandler(ws, buffer, frame);
	}

}
