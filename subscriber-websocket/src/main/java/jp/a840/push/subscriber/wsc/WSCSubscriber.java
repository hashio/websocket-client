package jp.a840.push.subscriber.wsc;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

import jp.a840.push.beans.RateBean;
import jp.a840.push.subscriber.AbstractSubscriber;
import jp.a840.push.subscriber.Message;
import jp.a840.push.subscriber.event.ExceptionEvent;
import jp.a840.push.subscriber.event.MessageEvent;
import jp.a840.push.subscriber.exception.ConnectionException;
import jp.a840.push.subscriber.exception.InitializeException;
import jp.a840.push.subscriber.exception.TimeoutException;
import jp.a840.push.subscriber.listener.CompositeMessageListener;
import jp.a840.push.subscriber.listener.ExceptionListener;
import jp.a840.push.subscriber.listener.MessageListener;
import jp.a840.websocket.WebSocket;
import jp.a840.websocket.WebSocketDraft76;
import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.WebSocketHandlerAdapter;
import jp.a840.websocket.frame.Frame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * リアルタイムデータ受信用クライアントマネージャ。<br>
 * JMSを使用したデータの受信をコントロールする。<br>
 * ユーザはこのクラスを介してサーバとのデータのやり取りを行う。
 * 
 * @author t-hashimoto
 */
public class WSCSubscriber extends AbstractSubscriber {
	private Logger log = LoggerFactory.getLogger(WSCSubscriber.class);

	// WebSocket Client
	private WebSocket websocket;
	
	private String location;
	
	private long connectionTimeout = 60;
	
	protected CompositeMessageListener messageListener = new CompositeMessageListener();

	/**
	 * デフォルトコンストラクタ
	 */
	public WSCSubscriber() {
		super();
	}

	public WSCSubscriber(String propertyPath) throws FileNotFoundException,IOException {
		this();
		Properties props = new Properties();
		FileInputStream fis = new FileInputStream(propertyPath);
		props.load(fis);

		location = props.getProperty("location");
	}

	/**
	 * 初期処理。<br>
	 * コネクション、セッション、トピックサブスクライバ、キューセンダーの作成。
	 * 
	 */
	public void init() throws InitializeException {
		try{
			websocket = new WebSocketDraft76(location,new WSCSubscriberClientHandler());
			websocket.setBlockingMode(false);
		}catch(Exception e){
				throw new InitializeException(e);
		}
	}

	/**
	 * クライアントマネージャを起動し、サーバからデータの受信を開始します.
	 * addSubscribe(),addSubscribeList(),setSubscribeList()のいずれかでRealtimeRequestを登録しておく必要があります.
	 * 
	 * @throws InitializeException
	 *             RealtimeRequestが1つも登録されていなかった時等に発生します.
	 * @throws ConnectionException
	 *             JBoss,情報サーバとの接続ができなった時に発生します.
	 * @throws TimeoutException
	 *             情報サーバとの接続がタイムアウトした時に発生します.
	 */
	public void start() throws InitializeException {
		boolean failFlag = true;
		try {
			super.start();
			init();
			connect();
			failFlag = false;
		} catch (InitializeException e) {
			throw e;
		} catch (ConnectionException e) {
			throw e;
		} catch (Exception e) {
			throw new ConnectionException(e);
		} finally {
			if (failFlag) {
				quit();
			}
		}
	}

	/**
	 * リスナを登録しデータの受信を開始する。
	 * 
	 * @throws Exception
	 */
	@Override
	protected void connect() throws Exception {
		if (!started || connected) {
			return;
		}

		prepareConnect();
		// メッセージの配送をスタート
		websocket.connect();
		super.connect();
	}

	protected void prepareConnect() throws Exception{
	}
	
	protected void disconnect() {

		// WebSocketをクローズ
		if (websocket != null && websocket.isConnected()) {
			try {
				websocket.close();
			} catch (Exception e) {
				log.error("Can't close websocket.", e);
			}
		}
		super.disconnect();
	}

	/**
	 * 登録されたリスナを解除し受信を終了する。
	 */
	public void stop() {
		super.stop();
	}

    /* -------------------------------------------------------- *
     *       WebSocket Client Handler 
     * -------------------------------------------------------- */
	public class WSCSubscriberClientHandler extends WebSocketHandlerAdapter {

		@Override
		public void onError(WebSocket socket, WebSocketException e) {
			log.error("Caught Exception.", e);
		}

		@Override
		public void onOpen(WebSocket socket) {
			log.debug("OPEN!");
		}

		@Override
		public void onClose(WebSocket websocket) {
			log.debug("CLOSE!");
		}

		@Override
		public void onMessage(WebSocket websocket, Frame frame) {
            try {
                if(quit){
                    return;
                }
                ByteBuffer buffer = frame.getRawBody();
                byte[] bytes = new byte[buffer.limit() - buffer.position()];
                buffer.get(bytes);
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais);
                Object msg = ois.readObject();
                healthCheckTouch();
                if(connected){
                	fireMessage(msg);
                }
            } catch (Exception e) {
            	fireException(e);
            	quit();
            }
		}
	}
	
	protected void fireMessage(Object msg) {
		MessageEvent me = createMessageEvent(msg);
		messageListener.onMessage(me);
	}
	
	protected MessageEvent createMessageEvent(Object m){
		return new MessageEvent(new ObjectMessageWrapper(m));
	}

	private class ObjectMessageWrapper implements Message {
		private Object msg;
		private Object body;
		public ObjectMessageWrapper(Object m){
			this.msg = m;
			this.body = m;
		}
		public Object getBody() {
			return body;
		}
		public Object getProperty(String key) {
			throw new RuntimeException("Not implemented");
		}
	}
	
	/**
	 * MessageListenerを追加します. この変更はすぐに適用されます.
	 * 
	 * @param listener
	 *            リストへ追加するRealtimeMessageListener
	 */
	public void addMessageListener(MessageListener listener) {
		messageListener.addMessageListener(listener);
	}

	/**
	 * MessageListenerを削除します. この変更はすぐに適用されます.
	 * 
	 * @param listener
	 *            リストから削除するRealtimeMessageListener
	 */
	public void removeMessageListener(MessageListener listener) {
		messageListener.removeMessageListener(listener);
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public long getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(long connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}
	
	public static void main(String[] args) throws Exception {
		WSCSubscriber sub = new WSCSubscriber();
		sub.setLocation("ws://localhost:8088/rate");
		sub.start();
		sub.addExceptionListener(new ExceptionListener() {
			public void onException(ExceptionEvent e) {
				e.getException().printStackTrace();
			}
		});
		sub.addMessageListener(new MessageListener() {			
			public void onMessage(MessageEvent e) {
				Message msg = e.getMessage();
				RateBean dto = (RateBean)msg.getBody();
				System.out.println(dto.getBid());
			}
		});
		while(true){
			Thread.sleep(1000);
//			sub.websocket.send(sub.websocket.createFrame("TEST"));
//			System.out.println("Sent");
		}
	}

}
