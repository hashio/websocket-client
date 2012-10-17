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
package jp.a840.websocket.websocketorg;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.exception.WebSocketException;
import jp.a840.websocket.handler.WebSocketHandler;
import jp.a840.websocket.WebSockets;
import jp.a840.websocket.auth.Credentials;
import jp.a840.websocket.auth.DefaultAuthenticator;
import jp.a840.websocket.auth.win32.Mechanism;
import jp.a840.websocket.auth.win32.SpengoAuthenticator;
import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.proxy.Proxy;
import jp.a840.websocket.util.PacketDumpUtil;

import java.net.InetSocketAddress;


/**
 * The Class WebSocketChatServletProxyTest.
 *
 * @author Takahiro Hashimoto
 */
public class WebSocketEchoProxyTest {
	
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
//		Proxy proxy = new Proxy(new Credentials("test", "test"), new BasicAuthenticator());
        SpengoAuthenticator.setDefaultMechanism(Mechanism.NTLM);
		Proxy proxy = new Proxy(new InetSocketAddress("192.168.100.230", 8080),new Credentials("test", "test"), new DefaultAuthenticator());
//		Proxy proxy = new Proxy(new Credentials("test", "test"), new DigestAuthenticator());
		WebSocket socket = WebSockets.create("wss://echo.websocket.org", "http://www.websocket.org", proxy, new WebSocketHandler() {
			
			public void onOpen(WebSocket socket) {
				System.err.println("Open");
				try {
					socket.send(socket.createFrame("Test"));
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			public void onMessage(WebSocket socket, Frame frame) {
				System.out.println(frame);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change contents of catch statement use File | Settings | File Templates.
                }
                try {
                    socket.send(socket.createFrame("Boo"));
                } catch (WebSocketException e) {
                    e.printStackTrace();  //To change contents of catch statement use File | Settings | File Templates.
                }
            }
			
			public void onError(WebSocket socket, WebSocketException e) {
				e.printStackTrace();
			}
			
			public void onClose(WebSocket socket) {
				System.err.println("Closed");
			}
		});
		socket.setBlockingMode(true);
		socket.connect();
	}
}
