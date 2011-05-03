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
package jp.a840.websocket.handler;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.frame.Frame;

/**
 * The Class WebSocketPipeline.
 *
 * @author Takahiro Hashimoto
 */
public class WebSocketPipeline {

	/** The upstream handler list. */
	private List<StreamHandler> upstreamHandlerList = new CopyOnWriteArrayList<StreamHandler>();
	
	/** The downstream handler list. */
	private List<StreamHandler> downstreamHandlerList = new CopyOnWriteArrayList<StreamHandler>();

	/**
	 * Send handshake upstream.
	 *
	 * @param ws the ws
	 * @param buffer the buffer
	 * @throws WebSocketException the web socket exception
	 */
	public void sendHandshakeUpstream(WebSocket ws, ByteBuffer buffer) throws WebSocketException {
		StreamHandlerChain chain = new StreamHandlerChain(upstreamHandlerList);
		chain.nextHandshakeUpstreamHandler(ws, buffer);
	}
	
	/**
	 * Send handshake downstream.
	 *
	 * @param ws the ws
	 * @param buffer the buffer
	 * @throws WebSocketException the web socket exception
	 */
	public void sendHandshakeDownstream(WebSocket ws, ByteBuffer buffer) throws WebSocketException {
		StreamHandlerChain chain = new StreamHandlerChain(downstreamHandlerList);
		chain.nextHandshakeDownstreamHandler(ws, buffer);
	}
	
	/**
	 * Send upstream.
	 *
	 * @param ws the ws
	 * @param buffer the buffer
	 * @param frame the frame
	 * @throws WebSocketException the web socket exception
	 */
	public void sendUpstream(WebSocket ws, ByteBuffer buffer, Frame frame) throws WebSocketException {
		StreamHandlerChain chain = new StreamHandlerChain(upstreamHandlerList);
		chain.nextUpstreamHandler(ws, buffer, frame);
	}
	
	/**
	 * Send downstream.
	 *
	 * @param ws the ws
	 * @param buffer the buffer
	 * @param frame the frame
	 * @throws WebSocketException the web socket exception
	 */
	public void sendDownstream(WebSocket ws, ByteBuffer buffer, Frame frame) throws WebSocketException {
		StreamHandlerChain chain = new StreamHandlerChain(downstreamHandlerList);
		chain.nextDownstreamHandler(ws, buffer,frame);
	}
	
	/**
	 * Adds the stream handler.
	 *
	 * @param handler the handler
	 */
	public void addStreamHandler(StreamHandler handler){
		upstreamHandlerList.add(0, handler);
		downstreamHandlerList.add(handler);
	}

	/**
	 * Removes the stream handler.
	 *
	 * @param handler the handler
	 */
	public void removeStreamHandler(StreamHandler handler){
		upstreamHandlerList.remove(handler);
		downstreamHandlerList.remove(handler);
	}
}
