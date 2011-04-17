package jp.a840.websocket.jetty;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.WebSocketHandler;
import jp.a840.websocket.WebSockets;
import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.handler.PacketDumpStreamHandler;

import org.junit.Test;

public class WebSocketChatServletTest {
	@Test(expected=Exception.class)
	public void testChat() throws Exception {
		System.setProperty("websocket.packatdump", String.valueOf(
				PacketDumpStreamHandler.ALL
		));
		WebSocket socket = WebSockets.createDraft06("ws://localhost:8080/ws/", new WebSocketHandler() {
			
			public void onOpen(WebSocket socket) {
				System.err.println("Open");
				try {
					socket.send(socket.createFrame(System.getenv("USER") + ":has joined!"));
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			public void onMessage(WebSocket socket, Frame frame) {
				if(!frame.toString().startsWith(System.getenv("USER"))){
					try {
						socket.send(socket.createFrame(System.getenv("USER") + ":(echo)" + frame.toString()));
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				System.out.println(frame);
			}
			
			public void onError(WebSocket socket, WebSocketException e) {
				e.printStackTrace();
			}
			
			public void onClose(WebSocket socket) {
				System.err.println("Closed");
			}
		}, "chat");
		socket.setBlockingMode(true);
		socket.connect();
	}
}
