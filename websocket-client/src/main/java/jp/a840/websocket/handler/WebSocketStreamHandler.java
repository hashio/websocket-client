package jp.a840.websocket.handler;

import java.nio.ByteBuffer;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.FrameParser;
import jp.a840.websocket.handshake.Handshake;

public class WebSocketStreamHandler implements StreamHandler {

	private final Handshake handshake;
	private final FrameParser frameParser;
	
	public WebSocketStreamHandler(Handshake handshake, FrameParser frameParser){
		this.handshake = handshake;
		this.frameParser = frameParser;
	}
	
	
	
	public void nextUpstreamHandler(WebSocket ws, ByteBuffer buffer, Frame frame,
			StreamHandlerChain chain) throws WebSocketException {
		chain.nextUpstreamHandler(ws, frame.toByteBuffer(), null);
	}

	public void nextDownstreamHandler(WebSocket ws, ByteBuffer buffer, Frame nullFrame,
			StreamHandlerChain chain) throws WebSocketException {
		while (buffer.hasRemaining()) {
			Frame frame = frameParser.parse(buffer);
			if(frame != null){
				chain.nextDownstreamHandler(ws, buffer, frame);
			}
		}
	}



	public void nextHandshakeUpstreamHandler(WebSocket ws, ByteBuffer buffer,
			StreamHandlerChain chain) throws WebSocketException {
		ByteBuffer request = handshake.createHandshakeRequest();
		chain.nextHandshakeUpstreamHandler(ws, request);
	}



	public void nextHandshakeDownstreamHandler(WebSocket ws, ByteBuffer buffer,
			StreamHandlerChain chain) throws WebSocketException {
		if(handshake.handshakeResponse(buffer)){
			chain.nextHandshakeDownstreamHandler(ws, buffer);
		}
	}

}
