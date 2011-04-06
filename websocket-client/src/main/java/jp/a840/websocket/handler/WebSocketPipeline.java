package jp.a840.websocket.handler;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.frame.Frame;

public class WebSocketPipeline {

	private List<StreamHandler> upstreamHandlerList = new CopyOnWriteArrayList<StreamHandler>();
	private List<StreamHandler> downstreamHandlerList = new CopyOnWriteArrayList<StreamHandler>();

	public void sendUpstream(WebSocket ws, ByteBuffer buffer, Frame frame){
		StreamHandlerChain chain = new StreamHandlerChain(upstreamHandlerList.iterator());
		chain.nextUpstreamHandler(ws, buffer, frame);
	}
	
	public void sendDownstream(WebSocket ws, ByteBuffer buffer, Frame frame){
		StreamHandlerChain chain = new StreamHandlerChain(downstreamHandlerList.iterator());
		chain.nextDownstreamHandler(ws, buffer, frame);
	}
	
	public void addStreamHandler(StreamHandler handler){
		upstreamHandlerList.add(handler);
		downstreamHandlerList.add(0, handler);
	}

	public void removeStreamHandler(StreamHandler handler){
		upstreamHandlerList.remove(handler);
		downstreamHandlerList.remove(handler);
	}
}
