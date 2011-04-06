package jp.a840.push.subscriber.swing.table;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Vector;

import jp.a840.push.subscriber.swing.annotations.GroupingKey;
import jp.a840.push.subscriber.swing.util.SwingClientDefaultTableModelUtil;

public class HashTableModel extends SwingClientDefaultHashTableModel implements
		SwingClientTableModel {

	private Method[] methods;

	private int keyMethodIndex = -1;

	public HashTableModel(Class dataClass, String groupingKeyMethod) {
		this(dataClass, groupingKeyMethod, SwingClientDefaultTableModelUtil
				.compileData(dataClass));
	}

	public HashTableModel(Class dataClass, String groupingKeyMethod,
			String[] methodNames) {
		this(dataClass, groupingKeyMethod, SwingClientDefaultTableModelUtil
				.convertStringsToMethods(dataClass, methodNames));
	}

	public HashTableModel(Class dataClass, String groupingKeyMethod,
			Method[] methods) {
		init(groupingKeyMethod, methods);
		if (this.keyMethodIndex < 0) {
			throw new RuntimeException(
					"Not found groupingKeyMethod in methods of " + dataClass);
		}
	}

	public HashTableModel(Class dataClass) {
		this(dataClass, SwingClientDefaultTableModelUtil.compileData(dataClass));
	}

	public HashTableModel(Class dataClass, String[] methodNames) {
		this(dataClass, SwingClientDefaultTableModelUtil
				.convertStringsToMethods(dataClass, methodNames));
	}

	public HashTableModel(Class dataClass, Method[] methods) {
		String keyMethodName = null;
		for (Method method : dataClass.getMethods()) {
			if (method.getAnnotation(GroupingKey.class) != null) {
				keyMethodName = method.getName();
				break;
			}
		}
		init(keyMethodName, methods);
		if (this.keyMethodIndex < 0) {
			throw new RuntimeException("Not found @GropingKey in methods of "
					+ dataClass);
		}
	}

	private void init(String groupingKeyMethod, Method[] methods) {
		this.methods = methods;
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equalsIgnoreCase(groupingKeyMethod)
					|| methods[i].getName().equalsIgnoreCase(
							"get" + groupingKeyMethod)) {
				keyMethodIndex = i;
				break;
			}
		}
	}

	public void add(Vector v) {
		String key = v.get(keyMethodIndex).toString();
		if (getColumnCount() == 0) {
			setHeader();
		}
		setRow(key, new RowVector(v));
	}

	private void setHeader() {
		Vector v = SwingClientDefaultTableModelUtil.createHeader(methods);
		setColumnIdentifiers(v);
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
		}
		return new Vector();
	}
}
