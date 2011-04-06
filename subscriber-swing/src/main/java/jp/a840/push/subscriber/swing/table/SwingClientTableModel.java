package jp.a840.push.subscriber.swing.table;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Vector;

import javax.swing.table.TableModel;

public interface SwingClientTableModel extends TableModel {
	public Vector createRecord(Object obj, Map<Method[], Vector<?>> recordCache);
	public void add(Vector v);
	public void clear();
}
