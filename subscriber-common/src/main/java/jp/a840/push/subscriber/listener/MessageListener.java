package jp.a840.push.subscriber.listener;

import jp.a840.push.subscriber.event.MessageEvent;

public interface MessageListener {
	/**
	 * sent message from server
	 * @param e
	 */
	public void onMessage(MessageEvent e);
}
