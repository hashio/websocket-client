package jp.a840.push.subscriber.listener;

import jp.a840.push.subscriber.event.ExceptionEvent;

public interface ExceptionListener {
	/**
	 * JMSサーバとの接続中の例外やJMSサーバ内で発生した例外が来た時に呼ばれます.
	 * 
	 * @param e
	 */
	public void onException(ExceptionEvent e);
}
