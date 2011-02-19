package jp.a840.push.publisher;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.a840.push.beans.BestRateBean;
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



	@Override
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
		final List<Iterator<BestRateBean>> its = new ArrayList<Iterator<BestRateBean>>();
		its.add(new RateGenerateIterator(10201));
		its.add(new RateGenerateIterator(10301));
		its.add(new RateGenerateIterator(10401));
		its.add(new RateGenerateIterator(10501));
		its.add(new RateGenerateIterator(10601));
		its.add(new RateGenerateIterator(10701));
		its.add(new RateGenerateIterator(10801));
		its.add(new RateGenerateIterator(10901));
		its.add(new RateGenerateIterator(11001));
		its.add(new RateGenerateIterator(11101));
		its.add(new RateGenerateIterator(11201));
		its.add(new RateGenerateIterator(11301));
		its.add(new RateGenerateIterator(11401));
		its.add(new RateGenerateIterator(11501));
		its.add(new RateGenerateIterator(10302));
		its.add(new RateGenerateIterator(10502));
		its.add(new RateGenerateIterator(10603));
		
		for(final Iterator<BestRateBean> it : its){
			executorService.execute(new Runnable() {
				@Override
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

	public class RateGenerateIterator implements Iterator<BestRateBean> {
		private BestRateBean currentDto;
		
		public RateGenerateIterator(Integer productCode){
			BestRateBean dto = new BestRateBean();
			dto.setProductCode(productCode);
			dto.setAsk(new BigDecimal("100.00"));
			dto.setBid(new BigDecimal("100.00"));
			currentDto = dto;
		}
		
		@Override
		public boolean hasNext() {
			return true;
		}

		@Override
		synchronized public BestRateBean next() {
			BigDecimal ask = generateRate(currentDto.getAsk());
			currentDto.setAsk(ask);
			currentDto.setAskLatestValid(ask);
			
			BigDecimal bid = generateRate(currentDto.getBid());
			currentDto.setBid(bid);
			currentDto.setBidLatestValid(bid);

			currentDto.setMarketUpdateDatetime(new Date());
			return currentDto;
		}

		@Override
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
