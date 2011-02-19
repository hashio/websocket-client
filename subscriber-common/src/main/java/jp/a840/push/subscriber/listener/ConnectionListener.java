package jp.a840.push.subscriber.listener;

import jp.a840.push.subscriber.event.ConnectionEvent;

public interface ConnectionListener {
	/**
	 * JMSサーバと接続した時に呼ばれます.
	 * 
	 * @param e
	 */
	public void onConnected(ConnectionEvent e);

	/**
	 * JMSサーバとの接続が切断(サーバ、クライアントどちらからでも)した時に呼ばれます.
	 * 
	 * @param e
	 */
	public void onDisonnected(ConnectionEvent e);
}
