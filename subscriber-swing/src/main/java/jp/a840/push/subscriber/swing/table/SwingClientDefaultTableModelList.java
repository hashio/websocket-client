package jp.a840.push.subscriber.swing.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SwingClientDefaultTableModelList {
	private static SwingClientDefaultTableModelList instance;

	private List modelList = new ArrayList();

	private SwingClientDefaultTableModelList() {
	}

	public static SwingClientDefaultTableModelList getInstance() {
		return instance;
	}

	public SwingClientDefaultTableModel get(String key, Class clazz) {
		synchronized (modelList) {
			SwingClientDefaultTableModel m = null;
			for (Iterator it = modelList.iterator(); it.hasNext();) {
				m = (SwingClientDefaultTableModel) it.next();
				if (m.getKey() != null && m.getKey().equals(key)) {
					if (m.getClass().equals(clazz)) {
						return m;
					}
				}
			}
			try {
				m = (SwingClientDefaultTableModel) clazz.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			m.setKey(key);
			modelList.add(m);
			return m;
		}
	}
}
