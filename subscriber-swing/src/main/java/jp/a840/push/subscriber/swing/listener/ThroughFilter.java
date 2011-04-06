package jp.a840.push.subscriber.swing.listener;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Vector;

import jp.a840.push.subscriber.swing.table.SwingClientTableModel;

public class ThroughFilter implements Filter {

	private SwingClientTableModel model;
	
	public ThroughFilter(SwingClientTableModel model) {
		this.model = model;
	}

	public SwingClientTableModel getModel() {
		return model;
	}

	public void add(Vector v) {
		model.add(v);
	}

	public Vector createRecord(Object obj, Map<Method[], Vector<?>> recordCache) {
		return model.createRecord(obj, recordCache);
	}
}
