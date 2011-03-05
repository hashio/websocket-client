package jp.a840.push.subscriber.listener;

import jp.a840.push.subscriber.event.ExceptionEvent;

public interface ExceptionListener {
	/**
	 * If occurred exception in server. sent a ExceptionEvent to client.
	 * @param e
	 */
	public void onException(ExceptionEvent e);
}
