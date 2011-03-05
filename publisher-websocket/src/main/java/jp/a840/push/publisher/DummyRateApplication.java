package jp.a840.push.publisher;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	
	public DummyRateApplication() throws InitializeException {
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
	}

	
	
	@Override
	protected RateWebSocket createWebSocket(Connection connection,
			ServerWebSocketMeta meta) {
		return new RateWebSocket(connection, meta, this);
	}

	public void startSubscribe() throws Exception {
		executorService = Executors.newFixedThreadPool(30);
		final List<Iterator<RateBean>> its = new ArrayList<Iterator<RateBean>>();
		its.add(new RateGenerateIterator("USD/JPY"));
		its.add(new RateGenerateIterator("EUR/JPY"));
		its.add(new RateGenerateIterator("EUR/USD"));
		
		for(final Iterator<RateBean> it : its){
			executorService.execute(new Runnable() {
				public void run() {
					try{
						while(true){
							for(final RateWebSocket rws : getWebSockets()){
								rws.sendRate(it.next());
							}
							Thread.sleep(RandomUtils.nextInt(1000));
						}
					}catch(InterruptedException e){
						;
					}
				}
			});
		}
	}
	
	public void stopSubscribe(){
	}
	
	public void addSubscribe(String destination, String messageSelector){
	}

	public class RateGenerateIterator implements Iterator<RateBean> {
		private RateBean currentDto;
		
		public RateGenerateIterator(String currencyPair){
			RateBean dto = new RateBean();
			dto.setCurrencyPair(currencyPair);
			dto.setAsk(new BigDecimal("100.00"));
			dto.setBid(new BigDecimal("100.00"));
			currentDto = dto;
		}
		
		public boolean hasNext() {
			return true;
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
		
		private BigDecimal generateRate(BigDecimal rate){
			int num = RandomUtils.nextInt(200) - 100;
			double amount = Math.round((num > 0? 1: -1) * num * num / 1000);
			int scale = (int)Math.pow(10, rate.scale());
			return rate.add(BigDecimal.valueOf(amount / scale));
		}
	}
}
