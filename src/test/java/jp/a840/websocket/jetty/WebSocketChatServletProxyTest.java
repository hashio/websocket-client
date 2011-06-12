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
package jp.a840.websocket.jetty;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.WebSocketHandler;
import jp.a840.websocket.WebSockets;
import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.proxy.Proxy;
import jp.a840.websocket.proxy.ProxyBasicCredentials;
import jp.a840.websocket.util.PacketDumpUtil;


/**
 * The Class WebSocketChatServletProxyTest.
 *
 * @author Takahiro Hashimoto
 */
public class WebSocketChatServletProxyTest {
	
	/**
	 * The main method.
	 *
	 * @param argv the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] argv) throws Exception {
		System.setProperty("websocket.packatdump", String.valueOf(
				PacketDumpUtil.ALL
		));
//		System.setProperty("javax.net.debug", "all");
		System.setProperty("java.util.logging.config.file", "logging.properties");
		Proxy proxy = new Proxy(new ProxyBasicCredentials("basic auth required", "test", "test"));
		WebSocket socket = WebSockets.createDraft06("ws://192.168.0.51:8080/ws/", proxy, new WebSocketHandler() {
			
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
