package jp.a840.push.publisher;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jp.a840.push.beans.RateBean;
import jp.a840.push.subscriber.exception.InitializeException;
import jp.a840.push.subscriber.grizzly.RateWebSocket;

import org.apache.commons.lang.math.RandomUtils;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.websockets.ServerWebSocketMeta;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.frame.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


	public class DummyRateApplication extends WebSocketApplication<RateWebSocket> {

	private Logger log = LoggerFactory.getLogger(DummyRateApplication.class);
	
	private ExecutorService executorService;
	
	private AtomicInteger updateInterval = new AtomicInteger(1000);
	
	private ConcurrentHashMap<String, RateGenerateIterator> its = new ConcurrentHashMap<String, RateGenerateIterator>();

	public DummyRateApplication() throws InitializeException {
		for(int i = 0; i < 10; i++){
			its.put(String.valueOf(i), new RateGenerateIterator(String.valueOf(i)));
		}
	}	
	
	@Override
	public void onAccept(RateWebSocket websocket) throws IOException {
		super.onAccept(websocket);
		log.info("accept");
	}



	@Override
	public void onClose(RateWebSocket websocket) throws IOException {
		log.info("close");
		super.onClose(websocket);
	}

	public void onMessage(RateWebSocket websocket, Frame frame)
			throws IOException {
		log.info("message");
		String text = frame.getAsText();
		String[] params = text.split(":");
		if("UPDATE INTERVAL".equalsIgnoreCase(params[0])){
			updateInterval.set(Integer.valueOf(params[1]));
		}else if("ADD PAIR".equalsIgnoreCase(params[0])){
			String pair = params[1];
			RateGenerateIterator it = new RateGenerateIterator(pair);
			if(its.putIfAbsent(pair, it) == null){
				doExecuteService(it);	
			}
		}else if("REMOVE PAIR".equalsIgnoreCase(params[0])){
			String pair = params[1];
			RateGenerateIterator it = its.remove(pair);
			if(it != null){
				it.stop();
			}
		}
	}
	
	@Override
	protected RateWebSocket createWebSocket(Connection connection,
			ServerWebSocketMeta meta) {
		return new RateWebSocket(connection, meta, this);
	}

	public void startSubscribe() throws Exception {
		executorService = Executors.newFixedThreadPool(30);
		
		for(final Iterator<RateBean> it : its.values()){
			doExecuteService(it);
		}
	}
	
	private void doExecuteService(final Iterator<RateBean> it){
		executorService.execute(new Runnable() {
			public void run() {
				try{
					while(it.hasNext()){
						for(final RateWebSocket rws : getWebSockets()){
							rws.sendRate(it.next());
						}
						Thread.sleep(RandomUtils.nextInt(updateInterval.get()));
					}
				}catch(InterruptedException e){
					;
				}
			}
		});		
	}
	
	public void stopSubscribe(){
	}
	
	public void addSubscribe(String destination, String messageSelector){
	}

	public class RateGenerateIterator implements Iterator<RateBean> {
		private RateBean currentDto;
		
		volatile private boolean hasNextFlg = true;
		
		public RateGenerateIterator(String currencyPair){
			RateBean dto = new RateBean();
			dto.setCurrencyPair(currencyPair);
			dto.setAsk(new BigDecimal("100.00"));
			dto.setBid(new BigDecimal("100.00"));
			currentDto = dto;
		}
		
		public boolean hasNext() {
			return hasNextFlg;
		}

		synchronized public RateBean next() {
			BigDecimal ask = generateRate(currentDto.getAsk());
			currentDto.setAsk(ask);
			
			BigDecimal bid = generateRate(currentDto.getBid());
			currentDto.setBid(bid);

			currentDto.setUpdateTime(new Date());
			return currentDto;
		}

		public void remove() {
		}
		
		public void stop(){
			hasNextFlg = false;
		}
		
		private BigDecimal generateRate(BigDecimal rate){
			int num = RandomUtils.nextInt(200) - 100;
			double amount = Math.round((num > 0? 1: -1) * num * num / 1000);
			int scale = (int)Math.pow(10, rate.scale());
			return rate.add(BigDecimal.valueOf(amount / scale));
		}
	}
}
