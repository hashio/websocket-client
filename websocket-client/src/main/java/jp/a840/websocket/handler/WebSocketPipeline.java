package jp.a840.websocket.handler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.frame.Frame;

public class WebSocketPipeline {

	private List<StreamHandler> upstreamHandlerList = new CopyOnWriteArrayList<StreamHandler>();
	private List<StreamHandler> downstreamHandlerList = new CopyOnWriteArrayList<StreamHandler>();

	public void sendUpstream(WebSocket ws, Frame frame){
		StreamHandlerChain chain = new StreamHandlerChain(upstreamHandlerList.iterator());
		chain.nextUpstreamHandler(ws, frame);
	}
	
	public void sendDownstream(WebSocket ws, Frame frame){
		StreamHandlerChain chain = new StreamHandlerChain(downstreamHandlerList.iterator());
		chain.nextDownstreamHandler(ws, frame);
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
