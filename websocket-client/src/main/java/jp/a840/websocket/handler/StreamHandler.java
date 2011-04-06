package jp.a840.websocket.handler;

import java.nio.ByteBuffer;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.frame.Frame;

public interface StreamHandler {
	public void nextUpstreamHandler(WebSocket ws, ByteBuffer buffer,
			Frame frame, StreamHandlerChain chain);

	public void nextDownstreamHandler(WebSocket ws, ByteBuffer buffer,
			Frame frame, StreamHandlerChain chain);
}
