package jp.a840.push.subscriber.swing.listener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import jp.a840.push.subscriber.event.MessageEvent;
import jp.a840.push.subscriber.listener.MessageListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class RealtimeTableModelManager implements MessageListener, Runnable {
	protected Log log = LogFactory.getLog(this.getClass());

	private static RealtimeTableModelManager instance = new RealtimeTableModelManager();

	private List<Filter> filterList = new CopyOnWriteArrayList<Filter>();
	private BlockingQueue<FilterObject> queue = new LinkedBlockingQueue<FilterObject>();

	private ExecutorService executorService = Executors.newCachedThreadPool();

	private Map<Method[], Vector<?>> recordCache = new HashMap<Method[], Vector<?>>();

	
	private RealtimeTableModelManager() {
		executorService.submit(this);
	}

	public static RealtimeTableModelManager getInstance() {
		return instance;
	}

	public void onMessage(MessageEvent msg) {
		Object obj = msg.getMessage().getBody();
		for(Filter filter : filterList){
			Vector record = filter.createRecord(obj, recordCache);
			queue.offer(new FilterObject(filter, record));			
		}
		recordCache.clear();
	}

	public void addFilter(Filter filter) {
		filterList.add(filter);
	}

	public void removeFilter(Filter filter) {
		filterList.remove(filter);
	}
	
	public void clear(Filter filter){
		filter.getModel().clear();
	}
	
	public void clearAll(){
		for(Filter filter: filterList){
			filter.getModel().clear();
		}
	}

	public void run() {
		while(true){
			try {
				FilterObject obj = queue.take();
				Map<Method[], Vector<?>> recordCache = new HashMap<Method[], Vector<?>>();
				obj.filter.add(obj.record);
				recordCache.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	class FilterObject {
		Filter filter;
		Vector record;
		public FilterObject(Filter filter, Vector record){
			this.filter = filter;
			this.record = record;
		}
	}
}
