package jp.a840.push.subscriber.listener;

import jp.a840.push.subscriber.event.MessageEvent;

public interface MessageListener {
	/**
	 * メッセージを受けた時に呼ばれます.
	 * 
	 * @param e
	 */
	public void onMessage(MessageEvent e);
}
