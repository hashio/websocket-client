package jp.a840.push.publisher;

import org.glassfish.grizzly.TransportFactory;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.http.HttpServerFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.glassfish.grizzly.websockets.WebSocketFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class Main 
{
	private static Logger log = LoggerFactory.getLogger(Main.class);
	
	public static final int PORT = 8088;
	
    public static void main( String[] args ) throws Exception
    {
    	FilterChainBuilder serverFilterChainBuilder = FilterChainBuilder.stateless();
    	serverFilterChainBuilder.add(new TransportFilter());
    	serverFilterChainBuilder.add(new HttpServerFilter());
    	serverFilterChainBuilder.add(new WebSocketFilter());
    	
    	TCPNIOTransport transport = TransportFactory.getInstance().createTCPTransport();
    	transport.setProcessor(serverFilterChainBuilder.build());
    	
//    	final RateApplication application = new RateApplication();
    	final DummyRateApplication application = new DummyRateApplication();
    	
    	WebSocketEngine.getEngine().registerApplication("/rate", application);
    	try{
    		transport.bind(PORT);
    		transport.start();
    		application.startSubscribe();
    		
    		while(true)
    			Thread.sleep(1000);
    	}catch(Exception e){
    		log.error("Caught exception.", e);
    	}finally{
    		application.stopSubscribe();
    		transport.stop();
    		TransportFactory.getInstance().close();
    	}
    }
}
