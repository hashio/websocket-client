package jp.a840.push.subscriber.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jp.a840.push.subscriber.event.MessageEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeMessageListener implements MessageListener {
	private Logger log = LoggerFactory
			.getLogger(CompositeMessageListener.class);

	protected List<MessageListener> messageListenerList = new CopyOnWriteArrayList<MessageListener>();

	public CompositeMessageListener() {
		super();
	}

	public void onMessage(MessageEvent m) {
		for (MessageListener l : messageListenerList) {
			if (log.isTraceEnabled()) {
				log
						.trace("fire `onMessage' event to listener: "
								+ l.toString());
			}
			l.onMessage(m);
		}
	}

	public void addMessageListener(MessageListener listener) {
		if (listener == null) {
			return;
		}
		if (log.isTraceEnabled()) {
			log.trace("add  message listener: " + listener.toString());
		}
		messageListenerList.add(listener);
	}

	public void removeMessageListener(MessageListener listener) {
		if (listener == null) {
			return;
		}
		if (messageListenerList.size() == 0) {
			log.warn("Can't removed. listeners list is empty");
			return;
		}
		if (log.isTraceEnabled()) {
			log.trace("remove  message listener: " + listener.toString());
		}
		if (!messageListenerList.remove(listener)) {
			log.warn("Can't removed. listener not found in list");
		}
		if (messageListenerList.size() == 0) {
			log.warn("removed last message listener of  client manager");
		}
	}

	public List<MessageListener> getMessageListenerList() {
		return messageListenerList;
	}
}
