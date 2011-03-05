package jp.a840.push.subscriber.grizzly;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import jp.a840.push.beans.RateBean;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.memory.ByteBufferManager;
import org.glassfish.grizzly.websockets.WebSocketBase;
import org.glassfish.grizzly.websockets.WebSocketHandler;
import org.glassfish.grizzly.websockets.WebSocketMeta;
import org.glassfish.grizzly.websockets.frame.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RateWebSocket extends WebSocketBase {

	private Logger log = LoggerFactory.getLogger(RateWebSocket.class);
	
	private ByteBufferManager byteBufferManager = new ByteBufferManager();
	
	public RateWebSocket(Connection connection, WebSocketMeta meta,
			WebSocketHandler handler) {
		super(connection, meta, handler);
	}

	public void sendRate(RateBean rate){
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(rate);

			Frame frame = Frame.createFrame(0xFF, byteBufferManager.wrap(baos.toByteArray()));
			send(frame);
		}catch(IOException e){
			log.error("Caught exception.", e);
		}
	}
}
