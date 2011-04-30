package jp.a840.websocket;

import jp.a840.websocket.frame.Frame;

abstract public class WebSocketHandlerAdapter implements WebSocketHandler {

	public void onClose(WebSocket socket) {
		;
	}

	public void onError(WebSocket socket, WebSocketException e) {
		e.printStackTrace();
	}

	public void onMessage(WebSocket socket, Frame frame) {
		;
	}

	public void onOpen(WebSocket socket) {
		;
	}

}
