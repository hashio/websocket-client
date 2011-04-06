package jp.a840.websocket.handler;

import java.nio.ByteBuffer;
import java.util.Iterator;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.frame.Frame;

public class StreamHandlerChain {
	private Iterator<StreamHandler> it;

	public StreamHandlerChain(Iterator<StreamHandler> it) {
		this.it = it;
	}

	public void nextUpstreamHandler(WebSocket ws, ByteBuffer buffer, Frame frame) {
		if(it.hasNext()){
			this.it.next().nextUpstreamHandler(ws, buffer, frame, this);
		}
	}

	public void nextDownstreamHandler(WebSocket ws, ByteBuffer buffer, Frame frame) {
		if(it.hasNext()){
			this.it.next().nextDownstreamHandler(ws, buffer, frame, this);
		}
	}
}
