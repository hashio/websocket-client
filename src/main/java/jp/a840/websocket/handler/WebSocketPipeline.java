package jp.a840.websocket.handler;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.frame.Frame;

public class WebSocketPipeline {

	private List<StreamHandler> upstreamHandlerList = new CopyOnWriteArrayList<StreamHandler>();
	private List<StreamHandler> downstreamHandlerList = new CopyOnWriteArrayList<StreamHandler>();

	public void sendHandshakeUpstream(WebSocket ws, ByteBuffer buffer) throws WebSocketException {
		StreamHandlerChain chain = new StreamHandlerChain(upstreamHandlerList);
		chain.nextHandshakeUpstreamHandler(ws, buffer);
	}
	
	public void sendHandshakeDownstream(WebSocket ws, ByteBuffer buffer) throws WebSocketException {
		StreamHandlerChain chain = new StreamHandlerChain(downstreamHandlerList);
		chain.nextHandshakeDownstreamHandler(ws, buffer);
	}
	
	public void sendUpstream(WebSocket ws, ByteBuffer buffer, Frame frame) throws WebSocketException {
		StreamHandlerChain chain = new StreamHandlerChain(upstreamHandlerList);
		chain.nextUpstreamHandler(ws, buffer, frame);
	}
	
	public void sendDownstream(WebSocket ws, ByteBuffer buffer, Frame frame) throws WebSocketException {
		StreamHandlerChain chain = new StreamHandlerChain(downstreamHandlerList);
		chain.nextDownstreamHandler(ws, buffer,frame);
	}
	
	public void addStreamHandler(StreamHandler handler){
		upstreamHandlerList.add(0, handler);
		downstreamHandlerList.add(handler);
	}

	public void removeStreamHandler(StreamHandler handler){
		upstreamHandlerList.remove(handler);
		downstreamHandlerList.remove(handler);
	}
}
