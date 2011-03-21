package jp.a840.websocket.handler;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.WebSocketHandler;
import jp.a840.websocket.frame.Frame;

public class WebSocketStreamHandler implements StreamHandler {

	private final WebSocketHandler handler;
	
	public WebSocketStreamHandler(WebSocketHandler handler){
		this.handler = handler;
	}
	
	@Override
	public void nextUpstreamHandler(WebSocket ws, Frame frame,
			StreamHandlerChain chain) {
		chain.nextUpstreamHandler(ws, frame);
	}

	@Override
	public void nextDownstreamHandler(WebSocket ws, Frame frame,
			StreamHandlerChain chain) {
		handler.onMessage(ws, frame);
		chain.nextDownstreamHandler(ws, frame);
	}

}
