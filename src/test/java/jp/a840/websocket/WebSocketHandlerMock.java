package jp.a840.websocket;

import java.util.ArrayList;
import java.util.List;

import jp.a840.websocket.frame.Frame;

public class WebSocketHandlerMock implements WebSocketHandler {
	private List<List<Object>> onOpenList = new ArrayList<List<Object>>();
	private List<List<Object>> onMessageList = new ArrayList<List<Object>>();
	private List<List<Object>> onErrorList = new ArrayList<List<Object>>();
	private List<List<Object>> onCloseList = new ArrayList<List<Object>>();
	
	public void onOpen(WebSocket socket) {
		List<Object> args = new ArrayList<Object>();
		args.add(socket);
		
		onOpenList.add(args);
	}
	
	public void onMessage(WebSocket socket, Frame frame) {
		List<Object> args = new ArrayList<Object>();
		args.add(socket);
		args.add(frame);

		onMessageList.add(args);
	}

	public void onError(WebSocket socket, WebSocketException e) {
		List<Object> args = new ArrayList<Object>();
		args.add(socket);
		args.add(e);

		onErrorList.add(args);
	}
	
	public void onClose(WebSocket socket) {
		List<Object> args = new ArrayList<Object>();
		args.add(socket);		

		onCloseList.add(args);
	}

	public List<List<Object>> getOnOpenList() {
		return onOpenList;
	}

	public List<List<Object>> getOnMessageList() {
		return onMessageList;
	}

	public List<List<Object>> getOnErrorList() {
		return onErrorList;
	}

	public List<List<Object>> getOnCloseList() {
		return onCloseList;
	}

}
