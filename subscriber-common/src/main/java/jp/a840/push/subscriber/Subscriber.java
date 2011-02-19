package jp.a840.push.subscriber;

import jp.a840.push.subscriber.exception.InitializeException;
import jp.a840.push.subscriber.listener.ConnectionListener;
import jp.a840.push.subscriber.listener.ExceptionListener;
import jp.a840.push.subscriber.listener.MessageListener;

public interface Subscriber {

	public abstract void start() throws Exception;

	public abstract void stop();
	
	public void init() throws InitializeException;

	/* -------------------------------------------------------- *
	 *                L I S T E N E R S
	 * -------------------------------------------------------- */
	public abstract void addMessageListener(MessageListener listener);

	public abstract void removeMessageListener(MessageListener listener);
	
	public abstract void addConnectionListener(ConnectionListener listener);

	public abstract void removeConnectionListener(ConnectionListener listener);

	public abstract void addExceptionListener(ExceptionListener listener);

	public abstract void removeExceptionListener(ExceptionListener listener);

	/* -------------------------------------------------------- *
	 *            C O N N E C T I O N   C H E C K
	 * -------------------------------------------------------- */
	/**
	 * リスナの状態を返却する。
	 * 
	 * @return
	 */
	public abstract boolean isListening();

	/**
	 * コネクションが終了しているかを返却する。
	 * 正確ではないので注意！
	 * 
	 * @return
	 */
	public abstract boolean isAlive();

	public abstract long getReconnectInterval();

	public abstract void setReconnectInterval(long reconnectInterval);

	public abstract long getLastModified();

	public abstract long getHealthCheckInterval();

	public abstract void setHealthCheckInterval(long healthCheckInterval);

}