package jp.a840.push.subscriber.listener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jp.a840.push.subscriber.event.ConnectionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeConnectionListener implements ConnectionListener {
	private Logger log = LoggerFactory
			.getLogger(CompositeConnectionListener.class);

	private List<ConnectionListener> connectionListenerList = new CopyOnWriteArrayList<ConnectionListener>();

	public CompositeConnectionListener() {
		super();
	}

	public void onConnected(ConnectionEvent e) {
		for (ConnectionListener l : connectionListenerList) {
			if (log.isTraceEnabled()) {
				log.trace("fire `onConnected' event to listener: "
						+ l.toString());
			}
			l.onConnected(e);
		}
	}

	public void onDisonnected(ConnectionEvent e) {
		for (ConnectionListener l : connectionListenerList) {
			if (log.isTraceEnabled()) {
				log.trace("fire `onDisconnected' event to listener: "
						+ l.toString());
			}
			l.onDisonnected(e);
		}
	}

	public void addConnectionListener(ConnectionListener listener) {
		if (listener == null) {
			return;
		}
		if (log.isTraceEnabled()) {
			log.trace("add connection listener: " + listener.toString());
		}

		connectionListenerList.add(listener);
	}

	public void removeConnectionListener(ConnectionListener listener) {
		if (listener == null) {
			return;
		}
		if (connectionListenerList.size() == 0) {
			log.warn("Can't removed. listeners list is empty");
			return;
		}
		if (log.isTraceEnabled()) {
			log.trace("remove connection listener: " + listener.toString());
		}
		if (!connectionListenerList.remove(listener)) {
			log.warn("Can't removed. listener not found in list");
		}
		if (connectionListenerList.size() == 0) {
			log.warn("removed last message listener of realtime client manager");
		}
	}

	public List<ConnectionListener> getConnectionListenerList() {
		return connectionListenerList;
	}
}
