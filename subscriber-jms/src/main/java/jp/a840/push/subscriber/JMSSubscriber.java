package jp.a840.push.subscriber;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import jp.a840.push.subscriber.AbstractSubscriber;
import jp.a840.push.subscriber.Message;
import jp.a840.push.subscriber.event.MessageEvent;
import jp.a840.push.subscriber.exception.ConnectionException;
import jp.a840.push.subscriber.exception.InitializeException;
import jp.a840.push.subscriber.exception.TimeoutException;
import jp.a840.push.subscriber.listener.CompositeMessageListener;
import jp.a840.push.subscriber.listener.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * リアルタイムデータ受信用クライアントマネージャ。<br>
 * JMSを使用したデータの受信をコントロールする。<br>
 * ユーザはこのクラスを介してサーバとのデータのやり取りを行う。
 * 
 * @author t-hashimoto
 */
public class JMSSubscriber extends AbstractSubscriber {
	private Logger log = LoggerFactory.getLogger(JMSSubscriber.class);

	private static final String NAMING_FACTORY_INITIAL_KEY = "java.naming.factory.initial";

	private String namingFactoryInitial = "org.jnp.interfaces.NamingContextFactory";

	private static final String NAMING_FACTORY_URL_PKGS_KEY = "java.naming.factory.url.pkgs";

	private String namingFactoryUrlPkgs = "org.jboss.naming:org.jnp.interfaces";

	private static final String NAMING_PROVIDER_URL_KEY = "java.naming.provider.url";

	InitialContext ctx = null;

	/** HOST:1099とか */
	private String namingProviderUrl = null;

	// JMS
	protected Connection connection = null;

	protected Session session = null;

	protected javax.jms.MessageListener jmsMessageListener;

	protected CompositeMessageListener messageListener = new CompositeMessageListener();

	protected List<Subscriber> subscriberList = new ArrayList<Subscriber>();
	
	protected List<MessageConsumer> messageConsumerList = new ArrayList<MessageConsumer>();
	
	protected Hashtable envContext = new Hashtable();

	public Object lock = new Object();

	/**
	 * デフォルトコンストラクタ
	 */
	public JMSSubscriber() {
		super();
	}

	public JMSSubscriber(String jmsPropertyPath) throws InitializeException {
		super();
		try{
			ctx = this.getInitialContext(jmsPropertyPath);
		}catch(Exception e){
			throw new InitializeException(e);
		}
	}

	/**
	 * 初期処理。<br>
	 * コネクション、セッション、トピックサブスクライバ、キューセンダーの作成。
	 * 
	 */
	public void init() throws InitializeException {
		if(ctx != null){
			return;
		}
		try {
			// INITIALIZE JMS
			// JNDI コンテキストの作成
			if (namingFactoryInitial == null) {
				throw new InitializeException("NamingFactoryInitialが初期化されていません");
			}
			if (namingFactoryUrlPkgs == null) {
				throw new InitializeException("NamingFactoryUrlPkgsが初期化されていません");
			}
			if (namingProviderUrl == null) {
				throw new InitializeException("NamingProviderUrlが初期化されていません");
			}

			envContext.put(NAMING_FACTORY_INITIAL_KEY, namingFactoryInitial);
			envContext.put(NAMING_FACTORY_URL_PKGS_KEY, namingFactoryUrlPkgs);
			envContext.put(NAMING_PROVIDER_URL_KEY, namingProviderUrl);
			ctx = new InitialContext(envContext);
		} catch (InitializeException e) {
			throw e;
		} catch (Exception e) {
			throw new InitializeException(e);
		}
	}

    /* -------------------------------------------------------- *
     *                J N D I
     * -------------------------------------------------------- */
	/**
	 * JNDIコンテキストを作成する。
	 * 
	 * @param propertieFileName
	 * @return 作成されたJNDIコンテキスト
	 * @throws IOException
	 * @throws NamingException
	 */
	protected InitialContext getInitialContext() throws IOException, NamingException {
		return getInitialContext("jndi.properties");
	}
	
	/**
	 * JNDIコンテキストをクラスパス上のプロパティファイルから作成する。
	 * 
	 * @param propertieFileName
	 * @return 作成されたJNDIコンテキスト
	 * @throws IOException
	 * @throws NamingException
	 */
	protected InitialContext getInitialContext(String fileName) throws IOException, NamingException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
		Properties properties = new Properties();
		properties.load(is);
		return new InitialContext(properties);
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
	 * JMS用のサブスクライバーを追加する
	 * 
	 * @param topic
	 * @param messageSelector
	 */
	public void addSubscribe(String destination, String messageSelector) {
		if (connected == false) {
			subscriberList.add(new Subscriber(destination, messageSelector));
		} else {
			throw new IllegalStateException("クライアントマネージャ起動中にリクエストを変更することはできません。");
		}
	}

	/**
	 * RealtimeRequestをリストから削除する. start()呼び出し前に行ってください.
	 * start()呼び出し後はstop()を呼ばれるまでここの変更を行うと例外が発生します.
	 * 
	 * @param request
	 * @throws RequestException
	 *             start()呼び出し後からstop()を呼ばれるまでにリストを変更しようした場合に発生します.
	 */
	public void removeSubscribe(String destination, String messageSelector) {
		if (connected == false) {
			Subscriber removeTarget = null;
			for(Subscriber subscriber : subscriberList){
				if(subscriber.getDestination().equals(destination)
				|| subscriber.getMessageSelector().equals(messageSelector)){
					removeTarget = subscriber;
					break;
				}
			}
			subscriberList.remove(removeTarget);
		} else {
			throw new IllegalStateException("クライアントマネージャ起動中にリクエストを変更することはできません。");
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
		super.connect();

		// メッセージの配送をスタート
		connection.start();
	}

	protected void prepareConnect() throws Exception{
		// コネクションを作成
		// トピックコネクションファクトリーをルックアップ
		ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup(JMS_FACTORY);
		connection = connectionFactory.createConnection();
		connection.setExceptionListener(new javax.jms.ExceptionListener() {			
			@Override
			public void onException(JMSException e) {
				fireException(e);
				quit();
			}
		});
		
		// セッションを作成
		session = connection.createSession(false, Session.DUPS_OK_ACKNOWLEDGE);

		for(Subscriber subscriber : subscriberList){
			MessageConsumer consumer;
			// サブスクライバを作成（条件文を指定）
			Destination destination = (Destination) ctx.lookup(subscriber.getDestination());
			if (destination == null) {
				throw new JMSException("Can't find topic: " + subscriber.getDestination());
			}

			// 通常の値段が配信されるサブスクライバ(Feeder,Datasetで絞り込み可)
			consumer = session.createConsumer(destination, subscriber.getMessageSelector(), false);
			jmsMessageListener = new JMSMessageListener();
			consumer.setMessageListener(jmsMessageListener);
			messageConsumerList.add(consumer);
		}		
	}
	
	private class Subscriber {
		private String destination;
		private String messageSelector;
		
		public Subscriber(String destination, String messageSelector){
			this.destination = destination;
			this.messageSelector = messageSelector;
		}
		
		public String getDestination() {
			return destination;
		}
		public String getMessageSelector() {
			return messageSelector;
		}
	}

	protected void disconnect() {
		if (!connected) {
			return;
		}

		// サブスクライバをクローズ
		while(messageConsumerList.size() > 0){
			MessageConsumer consumer = (MessageConsumer)messageConsumerList.remove(0);
			if (consumer != null) {
				try {
					consumer.close();
				} catch (Exception e) {
					log.error("Can't close consumer.", e);
				}
			}
		}
		// セッションをクローズ
		if (session != null) {
			try {
				session.close();
			} catch (Exception e) {
				log.error("Can't close session.", e);
			}
		}
		// コネクションをクローズ
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
				log.error("Can't close connection.", e);
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
     *       J M S   M E S S A G E   L I S T E N E R
     * -------------------------------------------------------- */
    private class JMSMessageListener implements javax.jms.MessageListener {
        private Hashtable exceptionTable = new Hashtable();
        
        /**
         * トピックにデータが到着した際にデータを受け渡されるメソッド
         * 終了メッセージを受信したかチェックを行い、このメソッドから
         * fireResponceを実行する。
         * 
         * @param msg
         * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
         */
        public void onMessage(javax.jms.Message msg) {
            try {
                if(quit){
                    return;
                }
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

	protected void fireMessage(javax.jms.Message msg) {
		MessageEvent me = createMessageEvent(msg);
		messageListener.onMessage(me);
	}
	
	protected MessageEvent createMessageEvent(javax.jms.Message m){
		return new MessageEvent(new JMSMessageWrapper(m));
	}

	private class JMSMessageWrapper implements Message {
		private final javax.jms.Message msg;
		private Object body;
		public JMSMessageWrapper(javax.jms.Message m){
			this.msg = m;
			if(m instanceof ObjectMessage){
				try{
					body = ((ObjectMessage)m).getObject();
				}catch(JMSException e){
					throw new RuntimeException(e);
				}	
			}else if(m instanceof BytesMessage
					){
				throw new RuntimeException("Not supported");
			}
		}
		@Override
		public Object getBody() {
			return body;
		}
		@Override
		public Object getProperty(String key) {
			try{
				return msg.getObjectProperty(key);
			}catch(JMSException e){
				throw new RuntimeException(e);
			}
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

	public String getNamingFactoryInitial() {
		return namingFactoryInitial;
	}

	public void setNamingFactoryInitial(String namingFactoryInitial) {
		this.namingFactoryInitial = namingFactoryInitial;
	}

	public String getNamingFactoryUrlPkgs() {
		return namingFactoryUrlPkgs;
	}

	public void setNamingFactoryUrlPkgs(String namingFactoryUrlPkgs) {
		this.namingFactoryUrlPkgs = namingFactoryUrlPkgs;
	}

	/**
	 * 接続先のサーバ(JBossのJMS用のポート)へのアドレスを返します. フォーマットはIPアドレス:PORTです. 例:
	 * xxx.xxx.xxx.xxx:1099
	 * 
	 * @return
	 */
	public String getNamingProviderUrl() {
		return namingProviderUrl;
	}

	/**
	 * 接続先のサーバ(JBossのJMS用のポート)へのアドレスを指定します. フォーマットはIPアドレス:PORTです. 例:
	 * xxx.xxx.xxx.xxx:1099
	 * 
	 * @param namingProviderUrl
	 *            IPアドレス:PORT
	 */
	public void setNamingProviderUrl(String namingProviderUrl) {
		this.namingProviderUrl = namingProviderUrl;
	}
}
