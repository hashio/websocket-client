package jp.a840.websocket.handler;

import java.util.Iterator;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.frame.Frame;

public class StreamHandlerChain {
	private Iterator<StreamHandler> it;

	public StreamHandlerChain(Iterator<StreamHandler> it) {
		this.it = it;
	}

	public void nextUpstreamHandler(WebSocket ws, Frame frame) {
		if(it.hasNext()){
			this.it.next().nextUpstreamHandler(ws, frame, this);
		}
	}

	public void nextDownstreamHandler(WebSocket ws, Frame frame) {
		if(it.hasNext()){
			this.it.next().nextDownstreamHandler(ws, frame, this);
		}
	}
}
