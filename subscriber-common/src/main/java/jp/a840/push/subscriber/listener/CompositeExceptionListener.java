package jp.a840.push.subscriber.listener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jp.a840.push.subscriber.event.ExceptionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeExceptionListener implements ExceptionListener {
	private Logger log = LoggerFactory
			.getLogger(CompositeExceptionListener.class);

	private List<ExceptionListener> exceptionListenerList = new CopyOnWriteArrayList<ExceptionListener>();

	public CompositeExceptionListener() {
		super();
	}

	public void onException(ExceptionEvent e) {
		for (ExceptionListener l : exceptionListenerList) {
			if (log.isTraceEnabled()) {
				log.trace("fire `onException' event to listener: "
						+ l.toString());
			}
			l.onException(e);
		}
	}

	public void addExceptionListener(ExceptionListener listener) {
		if (listener == null) {
			return;
		}
		if (log.isTraceEnabled()) {
			log.trace("add exception listener: " + listener.toString());
		}
		synchronized (exceptionListenerList) {
			exceptionListenerList.add(listener);
		}
	}

	public void removeExceptionListener(ExceptionListener listener) {
		if (listener == null) {
			return;
		}
		if (exceptionListenerList.size() == 0) {
			log.warn("Can't removed. listeners list is empty");
			return;
		}
		if (log.isTraceEnabled()) {
			log.trace("remove exception listener: " + listener.toString());
		}
		if (!exceptionListenerList.remove(listener)) {
			log.warn("Can't removed. listener not found in list");
		}
		if (exceptionListenerList.size() == 0) {
			log.warn("removed last message listener");
		}
	}

	public List<ExceptionListener> getExceptionListenerList() {
		return exceptionListenerList;
	}

}
