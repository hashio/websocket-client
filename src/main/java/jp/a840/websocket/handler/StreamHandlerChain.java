package jp.a840.websocket.handler;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.frame.Frame;

public class StreamHandlerChain {
	private ListIterator<StreamHandler> it;
	private List<StreamHandler> streamHandlerList;

	public StreamHandlerChain(List<StreamHandler> streamHandlerList) {
		this.streamHandlerList = streamHandlerList;
		this.it = streamHandlerList.listIterator();
	}

	private StreamHandlerChain() {
	}

	public void nextHandshakeUpstreamHandler(WebSocket ws, ByteBuffer buffer) throws WebSocketException {
		if(it.hasNext()) {
			it.next().nextHandshakeUpstreamHandler(ws, buffer, this);
		}		
	}

	public void nextHandshakeDownstreamHandler(WebSocket ws, ByteBuffer buffer) throws WebSocketException {
		if(it.hasNext()){
			it.next().nextHandshakeDownstreamHandler(ws, buffer, this);
		}
	}

	public void nextUpstreamHandler(WebSocket ws, ByteBuffer buffer, Frame frame) throws WebSocketException {
		if(it.hasNext()) {
			it.next().nextUpstreamHandler(ws, buffer, frame, this);
		}
	}

	public void nextDownstreamHandler(WebSocket ws, ByteBuffer buffer, Frame frame) throws WebSocketException {
		if(it.hasNext()){
			it.next().nextDownstreamHandler(ws, buffer, frame, this);
		}
	}
	
	public StreamHandlerChain reverse(){
		StreamHandlerChain chain = new StreamHandlerChain();
		chain.streamHandlerList = streamHandlerList;
		chain.it = new ReverseIterator<StreamHandler>(chain.streamHandlerList.listIterator(it.previousIndex()));
		return chain;
	}
	
	private class ReverseIterator<E> implements ListIterator<E> {
		private final ListIterator<E> lit; 
		public ReverseIterator(ListIterator<E> lit){
			this.lit = lit;
		}
		public boolean hasNext() {
			return lit.hasPrevious();
		}
		public E next() {
			return lit.previous();
		}
		public boolean hasPrevious() {
			return lit.hasNext();
		}
		public E previous() {
			return lit.next();
		}
		public int nextIndex() {
			return lit.previousIndex();
		}
		public int previousIndex() {
			return lit.nextIndex();
		}
		public void remove() {
			throw new UnsupportedOperationException("Not implemented");
		}
		public void set(E o) {
			throw new UnsupportedOperationException("Not implemented");
		}
		public void add(E o) {
			throw new UnsupportedOperationException("Not implemented");
		}
	}
}
