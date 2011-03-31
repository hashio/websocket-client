package jp.a840.websocket.handler;

import java.nio.ByteBuffer;
import java.util.Iterator;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.frame.Frame;

public class StreamHandlerChain {
	private Iterator<StreamHandler> it;

	public StreamHandlerChain(Iterator<StreamHandler> it) {
		this.it = it;
	}

	public void nextHandshakeUpstreamHandler(WebSocket ws, ByteBuffer buffer) throws WebSocketException {
		if(it.hasNext()) {
			this.it.next().nextHandshakeUpstreamHandler(ws, buffer, this);
		}
	}

	public void nextHandshakeDownstreamHandler(WebSocket ws, ByteBuffer buffer) throws WebSocketException {
		if(it.hasNext()){
			this.it.next().nextHandshakeDownstreamHandler(ws, buffer, this);
		}
	}

	public void nextUpstreamHandler(WebSocket ws, ByteBuffer buffer, Frame frame) throws WebSocketException {
		if(it.hasNext()) {
			this.it.next().nextUpstreamHandler(ws, buffer, frame, this);
		}
	}

	public void nextDownstreamHandler(WebSocket ws, ByteBuffer buffer, Frame frame) throws WebSocketException {
		if(it.hasNext()){
			this.it.next().nextDownstreamHandler(ws, buffer, frame, this);
		}
	}
}
