package jp.a840.push.subscriber.swing.listener;

import jp.a840.push.subscriber.swing.table.SwingClientTableModel;

public interface Filter {
	public void add(String filter, Object obj);
	public SwingClientTableModel getModel();
}
