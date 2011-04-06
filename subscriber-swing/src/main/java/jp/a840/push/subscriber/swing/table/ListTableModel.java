package jp.a840.push.subscriber.swing.table;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Vector;

import jp.a840.push.subscriber.swing.util.SwingClientDefaultTableModelUtil;

public class ListTableModel extends SwingClientDefaultTableModel implements
		SwingClientTableModel {

	private Method[] methods;

	public ListTableModel(Class dataClass, String[] methodNames) {
		this(dataClass, SwingClientDefaultTableModelUtil
				.convertStringsToMethods(dataClass, methodNames));
	}

	public ListTableModel(Class dataClass) {
		this(dataClass, SwingClientDefaultTableModelUtil.compileData(dataClass));
	}

	public ListTableModel(Class dataClass, Method[] methods) {
		init(methods);
	}

	private void init(Method[] methods) {
		this.methods = methods;
	}

	public void add(Vector v) {
		if (getColumnCount() == 0) {
			setHeader();
		}
		insertRow(0, v);
	}

	private void setHeader() {
		setColumnIdentifiers(SwingClientDefaultTableModelUtil
				.createHeader(methods));
	}

	public Vector createRecord(Object obj, Map<Method[], Vector<?>> recordCache) {
		try {
			Vector v = recordCache.get(methods);
			if (v == null) {
				v = new Vector();
				for (int i = 0; i < methods.length; i++) {
					v.add(methods[i].invoke(obj, null));
				}
				recordCache.put(methods, v);
			}
			return v;
		} catch (Exception e) {
			e.printStackTrace();
			return new Vector();
		}
	}
}
