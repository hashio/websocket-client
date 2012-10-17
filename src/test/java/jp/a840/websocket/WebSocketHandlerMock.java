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
package jp.a840.websocket;

import java.util.ArrayList;
import java.util.List;

import jp.a840.websocket.exception.WebSocketException;
import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.handler.WebSocketHandler;
import jp.a840.websocket.util.PacketDumpUtil;


/**
 * The Class WebSocketHandlerMock.
 *
 * @author Takahiro Hashimoto
 */
public class WebSocketHandlerMock implements WebSocketHandler {
	
	/** The on open list. */
	private List<List<Object>> onOpenList = new ArrayList<List<Object>>();
	
	/** The on message list. */
	private List<List<Object>> onMessageList = new ArrayList<List<Object>>();
	
	/** The on error list. */
	private List<List<Object>> onErrorList = new ArrayList<List<Object>>();
	
	/** The on close list. */
	private List<List<Object>> onCloseList = new ArrayList<List<Object>>();
	
	/* (non-Javadoc)
	 * @see jp.a840.websocket.handler.WebSocketHandler#onOpen(jp.a840.websocket.WebSocket)
	 */
	public void onOpen(WebSocket socket) {
		List<Object> args = new ArrayList<Object>();
		args.add(socket);
		
		onOpenList.add(args);
	}
	
	/* (non-Javadoc)
	 * @see jp.a840.websocket.handler.WebSocketHandler#onMessage(jp.a840.websocket.WebSocket, jp.a840.websocket.frame.Frame)
	 */
	public void onMessage(WebSocket socket, Frame frame) {
		List<Object> args = new ArrayList<Object>();
		args.add(socket);
		args.add(frame);
		
		PacketDumpUtil.printPacketDump("frame", frame.getContents());

		onMessageList.add(args);
	}

	/* (non-Javadoc)
	 * @see jp.a840.websocket.handler.WebSocketHandler#onError(jp.a840.websocket.WebSocket, jp.a840.websocket.exception.WebSocketException)
	 */
	public void onError(WebSocket socket, WebSocketException e) {
		List<Object> args = new ArrayList<Object>();
		args.add(socket);
		args.add(e);

		onErrorList.add(args);
	}
	
	/* (non-Javadoc)
	 * @see jp.a840.websocket.handler.WebSocketHandler#onClose(jp.a840.websocket.WebSocket)
	 */
	public void onClose(WebSocket socket) {
		List<Object> args = new ArrayList<Object>();
		args.add(socket);		

		onCloseList.add(args);
	}

	/**
	 * Gets the on open list.
	 *
	 * @return the on open list
	 */
	public List<List<Object>> getOnOpenList() {
		return onOpenList;
	}

	/**
	 * Gets the on message list.
	 *
	 * @return the on message list
	 */
	public List<List<Object>> getOnMessageList() {
		return onMessageList;
	}

	/**
	 * Gets the on error list.
	 *
	 * @return the on error list
	 */
	public List<List<Object>> getOnErrorList() {
		return onErrorList;
	}

	/**
	 * Gets the on close list.
	 *
	 * @return the on close list
	 */
	public List<List<Object>> getOnCloseList() {
		return onCloseList;
	}

}
