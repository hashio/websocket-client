package jp.a840.websocket.handler;

import java.nio.ByteBuffer;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.WebSocketHandler;
import jp.a840.websocket.frame.Frame;

public class WebSocketStreamHandler implements StreamHandler {

	private final WebSocketHandler handler;

	public WebSocketStreamHandler(WebSocketHandler handler) {
		this.handler = handler;
	}

	public void nextUpstreamHandler(WebSocket ws, ByteBuffer buffer,
			Frame frame, StreamHandlerChain chain) {
		chain.nextUpstreamHandler(ws, frame);
	}

	public void nextDownstreamHandler(WebSocket ws, ByteBuffer buffer,
			Frame frame, StreamHandlerChain chain) {
		Frame frame = getFrameParser().parse(buffer);
		if(frame != null){
			getFrameParser().init();
		}
		
		handler.onMessage(ws, frame);
	}

}
