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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.frame.Frame;

/**
 * The Class StreamHandlerChain.
 *
 * @author Takahiro Hashimoto
 */
public class StreamHandlerChain {
	
	/** The it. */
	private ListIterator<StreamHandler> it;
	
	/** The stream handler list. */
	private List<StreamHandler> streamHandlerList;

	/**
	 * Instantiates a new stream handler chain.
	 *
	 * @param streamHandlerList the stream handler list
	 */
	public StreamHandlerChain(List<StreamHandler> streamHandlerList) {
		this.streamHandlerList = streamHandlerList;
		this.it = streamHandlerList.listIterator();
	}

	/**
	 * Instantiates a new stream handler chain.
	 */
	private StreamHandlerChain() {
	}

	/**
	 * Next handshake upstream handler.
	 *
	 * @param ws the ws
	 * @param buffer the buffer
	 * @throws WebSocketException the web socket exception
	 */
	public void nextHandshakeUpstreamHandler(WebSocket ws, ByteBuffer buffer) throws WebSocketException {
		if(it.hasNext()) {
			it.next().nextHandshakeUpstreamHandler(ws, buffer, this);
		}		
	}

	/**
	 * Next handshake downstream handler.
	 *
	 * @param ws the ws
	 * @param buffer the buffer
	 * @throws WebSocketException the web socket exception
	 */
	public void nextHandshakeDownstreamHandler(WebSocket ws, ByteBuffer buffer) throws WebSocketException {
		if(it.hasNext()){
			it.next().nextHandshakeDownstreamHandler(ws, buffer, this);
		}
	}

	/**
	 * Next upstream handler.
	 *
	 * @param ws the ws
	 * @param buffer the buffer
	 * @param frame the frame
	 * @throws WebSocketException the web socket exception
	 */
	public void nextUpstreamHandler(WebSocket ws, ByteBuffer buffer, Frame frame) throws WebSocketException {
		if(it.hasNext()) {
			it.next().nextUpstreamHandler(ws, buffer, frame, this);
		}
	}

	/**
	 * Next downstream handler.
	 *
	 * @param ws the ws
	 * @param buffer the buffer
	 * @param frame the frame
	 * @throws WebSocketException the web socket exception
	 */
	public void nextDownstreamHandler(WebSocket ws, ByteBuffer buffer, Frame frame) throws WebSocketException {
		if(it.hasNext()){
			it.next().nextDownstreamHandler(ws, buffer, frame, this);
		}
	}
	
	/**
	 * Reverse.
	 *
	 * @return the stream handler chain
	 */
	public StreamHandlerChain reverse(){
		StreamHandlerChain chain = new StreamHandlerChain();
		chain.streamHandlerList = streamHandlerList;
		chain.it = new ReverseIterator<StreamHandler>(chain.streamHandlerList.listIterator(it.previousIndex()));
		return chain;
	}
	
	/**
	 * The Class ReverseIterator.
	 *
	 * @param <E> the element type
	 * @author Takahiro Hashimoto
	 */
	private class ReverseIterator<E> implements ListIterator<E> {
		
		/** The lit. */
		private final ListIterator<E> lit; 
		
		/**
		 * Instantiates a new reverse iterator.
		 *
		 * @param lit the lit
		 */
		public ReverseIterator(ListIterator<E> lit){
			this.lit = lit;
		}
		
		/* (non-Javadoc)
		 * @see java.util.ListIterator#hasNext()
		 */
		public boolean hasNext() {
			return lit.hasPrevious();
		}
		
		/* (non-Javadoc)
		 * @see java.util.ListIterator#next()
		 */
		public E next() {
			return lit.previous();
		}
		
		/* (non-Javadoc)
		 * @see java.util.ListIterator#hasPrevious()
		 */
		public boolean hasPrevious() {
			return lit.hasNext();
		}
		
		/* (non-Javadoc)
		 * @see java.util.ListIterator#previous()
		 */
		public E previous() {
			return lit.next();
		}
		
		/* (non-Javadoc)
		 * @see java.util.ListIterator#nextIndex()
		 */
		public int nextIndex() {
			return lit.previousIndex();
		}
		
		/* (non-Javadoc)
		 * @see java.util.ListIterator#previousIndex()
		 */
		public int previousIndex() {
			return lit.nextIndex();
		}
		
		/* (non-Javadoc)
		 * @see java.util.ListIterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException("Not implemented");
		}
		
		/* (non-Javadoc)
		 * @see java.util.ListIterator#set(java.lang.Object)
		 */
		public void set(E o) {
			throw new UnsupportedOperationException("Not implemented");
		}
		
		/* (non-Javadoc)
		 * @see java.util.ListIterator#add(java.lang.Object)
		 */
		public void add(E o) {
			throw new UnsupportedOperationException("Not implemented");
		}
	}
}
