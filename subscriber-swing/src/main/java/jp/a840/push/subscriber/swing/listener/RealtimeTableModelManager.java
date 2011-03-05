package jp.a840.push.subscriber.swing.listener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import jp.a840.push.subscriber.event.MessageEvent;
import jp.a840.push.subscriber.listener.MessageListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class RealtimeTableModelManager implements MessageListener {
	protected Log log = LogFactory.getLog(this.getClass());

	private Set filterSet = new HashSet();

	private static RealtimeTableModelManager instance = new RealtimeTableModelManager();

	private String keyword;
	
	private Filter[] filters = new Filter[0];
	
	private RealtimeTableModelManager() {
	}

	public static RealtimeTableModelManager getInstance() {
		return instance;
	}

	public void onMessage(MessageEvent msg) {
		try {
			Object obj = msg.getMessage().getBody();			
			for(int i = 0; i < filters.length; i++){
				filters[i].add(keyword, obj);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void addFilter(Filter filter) {
		filterSet.add(filter);
		synchronizeList();
	}

	private void synchronizeList(){
		Filter[] tmp = new Filter[filterSet.size()];
		filterSet.toArray(tmp);
		filters = tmp;
	}
	
	public void removeFilter(Filter filter) {
		filterSet.remove(filter);
		synchronizeList();
	}
	
	public void clear(Filter filter){
		filter.getModel().clear();
	}
	
	public void clearAll(){
		for(Iterator it = filterSet.iterator(); it.hasNext();){
			Filter f = (Filter)it.next();
			f.getModel().clear();
		}
	}
}
