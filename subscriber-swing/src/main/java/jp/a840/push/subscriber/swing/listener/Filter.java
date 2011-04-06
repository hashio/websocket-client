package jp.a840.push.subscriber.swing.listener;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Vector;

import jp.a840.push.subscriber.swing.table.SwingClientTableModel;

public interface Filter {
	public Vector createRecord(Object obj, Map<Method[], Vector<?>> recordCache);
	public void add(Vector v);
	public SwingClientTableModel getModel();
}
