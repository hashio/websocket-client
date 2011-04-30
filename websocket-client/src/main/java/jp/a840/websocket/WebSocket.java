package jp.a840.websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;

import jp.a840.websocket.frame.Frame;

public interface WebSocket {
	public void send(Frame frame) throws WebSocketException;
	public void connect() throws WebSocketException, IOException;
	public boolean isConnected();
	public void close();
	public boolean isBlockingMode();
	public void setBlockingMode(boolean blockingMode);
	public URI getLocation();
	public InetSocketAddress getEndpoint();
	public int getBufferSize();
	public int getConnectionTimeout();
	public void setConnectionTimeout(int connectionTimeout);
	public Frame createFrame(Object obj) throws WebSocketException;
	public Frame createFrame(String str) throws WebSocketException;
}
