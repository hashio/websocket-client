package jp.a840.push.subscriber.listener;

import jp.a840.push.subscriber.event.ConnectionEvent;

public interface ConnectionListener {
	/**
	 * connected to server
	 * @param e
	 */
	public void onConnected(ConnectionEvent e);

	/**
	 * disconnect by client or server
	 * @param e
	 */
	public void onDisonnected(ConnectionEvent e);
}
