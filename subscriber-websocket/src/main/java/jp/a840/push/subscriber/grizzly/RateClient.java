package jp.a840.push.subscriber.grizzly;

import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.TransportFactory;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketConnectorHandler;

public class RateClient {

	public static void main(String[] args) throws Exception {
		final TCPNIOTransport transport = TransportFactory.getInstance().createTCPTransport();
		WebSocket websocket = null;

		try {
			transport.start();
			
			WebSocketConnectorHandler connectorHandler =
				new WebSocketConnectorHandler(transport);

			final RateClientHandler clientHandler = new RateClientHandler();
			
			Future<WebSocket> connectFuture = connectorHandler.connect(
					new URI("ws://localhost:8088/rate"), clientHandler);

			websocket = (WebSocket) connectFuture.get(1000, TimeUnit.SECONDS);
			while(true){
				Thread.sleep(1000);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}
