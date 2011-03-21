package jp.a840.push.subscriber;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jp.a840.push.subscriber.event.ConnectionEvent;
import jp.a840.push.subscriber.event.ExceptionEvent;
import jp.a840.push.subscriber.exception.TimeoutException;
import jp.a840.push.subscriber.listener.CompositeConnectionListener;
import jp.a840.push.subscriber.listener.CompositeExceptionListener;
import jp.a840.push.subscriber.listener.ConnectionListener;
import jp.a840.push.subscriber.listener.ExceptionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class AbstractSubscriber implements Subscriber {
	private Logger log = LoggerFactory.getLogger(AbstractSubscriber.class);

	protected final static String JMS_FACTORY = "ConnectionFactory";
	    
	/* for listener */
	protected CompositeExceptionListener  exceptionListener;
	protected CompositeConnectionListener connectionListener;
	    
	/* for health check */
	protected ClientManagerHealthChecker healthChecker;
	protected long healthCheckInterval = 30000L;
	protected boolean listening = false;
	private long lastModified;
	    
	/* for reconnect */
	protected ClientManagerReconnecter reconnecter;
	protected long reconnectInterval = 0L;
	    
	protected boolean connected = false;
	protected boolean started = false;

	protected boolean quit = false;
	    
	private ClientManagerDisconnecter disconnecter;
	private Thread disconnecterThread = null;
	    	    
	public AbstractSubscriber(){
		connectionListener = new CompositeConnectionListener();
		exceptionListener = new CompositeExceptionListener();

		disconnecter = new ClientManagerDisconnecter();
		healthChecker = new ClientManagerHealthChecker();
		reconnecter = new ClientManagerReconnecter();
	}
	    
	public void start() throws Exception {
		started = true;
	}

	public void stop() {
		started = false;
		quit();
		disconnecter.stop();
		reconnecter.stop();
		fireDisconnected();
	}
	    
	protected void connect() throws Exception {
		reconnecter.stop();
		if (listening == false) {
			if(log.isTraceEnabled()){
				log.trace(log("connect"));
			}
	            
			disconnecter.start();
			quit = false;
	            
			healthChecker.start();
			if(connected){
				revival();
			}else{
				connected = true;
				fireConnected();
			}
		}
	}
	    
	protected void disconnect() {
		if(log.isTraceEnabled()){
			log.trace(log("disconnect"));
		}
		if(connected){
			connected = false;
			fireDisconnected();
		}
		if (listening == true) {
			healthChecker.stop();
		}
		if(started){
			reconnecter.start();
		}
	}

	protected void revival() throws Exception {
		if(log.isTraceEnabled()){
			log.trace(log("revival"));
		}
		reconnecter.stop();
	}

	protected void quit(){
		if(disconnecter == null){
			disconnect();
		}else{
			synchronized (disconnecter) {
				if(!quit){
					disconnecter.notifyAll();
					quit = true;
				}
			}
		}
	}
	    
	/* -------------------------------------------------------- *
	 *                L I S T E N E R S
	 * -------------------------------------------------------- */
	public void addConnectionListener(ConnectionListener listener){
		connectionListener.addConnectionListener(listener);
	}

	public void removeConnectionListener(ConnectionListener listener){
		connectionListener.removeConnectionListener(listener);
	}

	public List getConnectionListenerList() {
		return connectionListener.getConnectionListenerList();
	}
	    
	public void addExceptionListener(ExceptionListener listener){
		exceptionListener.addExceptionListener(listener);
	}

	public void removeExceptionListener(ExceptionListener listener){
		exceptionListener.removeExceptionListener(listener);
	}

	public List getExceptionListenerList() {
		return exceptionListener.getExceptionListenerList();
	}
	    
	/* -------------------------------------------------------- *
	 *             E V E N T    T R I G G E R S
	 * -------------------------------------------------------- */
	protected void fireDisconnected() {
		ConnectionEvent e = new ConnectionEvent(this);
		connectionListener.onDisonnected(e);
	}
	    
	protected void fireConnected() {            
		ConnectionEvent e = new ConnectionEvent(this);
		connectionListener.onConnected(e);
	}
	    
	protected void fireException(Exception ex){
		ExceptionEvent e = new ExceptionEvent(this, ex);
		exceptionListener.onException(e);
	}
	    
	/* -------------------------------------------------------- *
	 *                H E A L T H   C H E C K
	 * -------------------------------------------------------- */
	protected class ClientManagerHealthChecker {
		private ScheduledExecutorService scheduler;
	        
		public ClientManagerHealthChecker(){
		}
	        
		public void start() {
			scheduler = Executors.newSingleThreadScheduledExecutor();
			if(started){
				if(healthCheckInterval > 0){
					log.info(log("health check start"));
					lastModified = System.currentTimeMillis();
					scheduler.scheduleAtFixedRate(new Runnable() {
								public void run() {
	            	            if (started && connected) {
	            	                synchronized(this) {
	            	                    long now = System.currentTimeMillis();
	            	                    if ((now - lastModified) > healthCheckInterval) {
	            	                        try{
	            	                            quit();
	            	                        }catch(Exception e){
	            	                            ;
	            	                        }
	            	                        fireException(new TimeoutException("Connection Timeout. (" + healthCheckInterval + "s)" ));
	            	                        log.debug(log("Connection Timeout"));
	            	                    } else {
	            	                        log.debug(log("Health Check"));
	            	                    }
	            	                }
	            	            }}
						}, healthCheckInterval, healthCheckInterval, TimeUnit.MILLISECONDS);
				}
				listening = true;
			}
		}
	        
		public void stop() {
			listening = false;
			scheduler.shutdown();
			try{
				scheduler.awaitTermination(30, TimeUnit.SECONDS);
				scheduler.shutdownNow();
			}catch(InterruptedException e){
				;
			}
			log.info(log("health check stop"));
		}
	               
	}
	    
	/* -------------------------------------------------------- *
	 *                    D I S C O N N E C T E R
	 * -------------------------------------------------------- */
	protected class ClientManagerDisconnecter {
		private volatile CountDownLatch disconnectLatch;
		private volatile CountDownLatch stopWaitLatch;
		    
		private ExecutorService executor;
		    
		public void start(){
			disconnectLatch = new CountDownLatch(1);
			executor = Executors.newSingleThreadExecutor();
			executor.submit(new Runnable() {
						public void run() {
			            try{
			                synchronized (this) {
			                	disconnectLatch.await();
			                }
			            }catch(InterruptedException e){
			                ;
			            }
			            disconnect();
			            stopWaitLatch.countDown();
					}
				});
		}
	        
		public void stop(){
			try{
				stopWaitLatch = new CountDownLatch(1);
				disconnectLatch.countDown();
				stopWaitLatch.await(60, TimeUnit.SECONDS);
			}catch(InterruptedException e){
				;
			}
		}
	}
	    
	/* -------------------------------------------------------- *
	 *                    R E C O N N E C T E R
	 * -------------------------------------------------------- */
	protected class ClientManagerReconnecter {
		private ScheduledExecutorService scheduler;
	        
		public ClientManagerReconnecter(){
			scheduler = Executors.newSingleThreadScheduledExecutor();
		}
	        
		public void start() {
			if(started){
				if(reconnectInterval > 0){
					scheduler.scheduleAtFixedRate(new Runnable() {
							public void run() {
					            if(started){
						            try{
						                connect();
						            }catch(Exception e){
						                log.info(log("Can't recconect"), e);
						            }
					            }							
							}
						}, reconnectInterval, reconnectInterval, TimeUnit.MICROSECONDS);
					log.debug(log("try reconnect start with interval: " + reconnectInterval));
				}else{
					log.trace(log("Don't try reconnect."));
				}
			}
		}
	        
		public void stop() {
			scheduler.shutdown();
		}
	               
	}

	protected void healthCheckTouch(){
		lastModified = System.currentTimeMillis();
	}

	protected void healthCheckResponse() {
		;
	}
	    
	public long getLastModified() {
		return lastModified;
	}
	    
	public long getHealthCheckInterval() {
		return this.healthCheckInterval;
	}

	public void setHealthCheckInterval(long healthCheckInterval) {
		this.healthCheckInterval = healthCheckInterval;
	}
	    
	/* -------------------------------------------------------- *
	 *            C O N N E C T I O N   C H E C K
	 * -------------------------------------------------------- */
	public boolean isListening() {
		return listening;
	}
	    
	public boolean isAlive() {
		return connected;
	}
	    
	protected String log(String str){
		return "[" + this.toString() + "] " + str;
	}
	    
	public String toString(){
		return this.getName();
	}
	    
	public String getName(){
		return this.getClass().getName();
	}

	public long getReconnectInterval() {
		return reconnectInterval;
	}

	public void setReconnectInterval(long reconnectInterval) {
		this.reconnectInterval = reconnectInterval;
	}
}
