package jp.a840.websocket;

import jp.a840.websocket.frame.Frame;

public interface WebSocketHandler {
	public void onOpen(WebSocket socket);
	public void onMessage(WebSocket socket, Frame frame);
	public void onError(WebSocket socket, WebSocketException e);
	public void onClose(WebSocket socket);

}
