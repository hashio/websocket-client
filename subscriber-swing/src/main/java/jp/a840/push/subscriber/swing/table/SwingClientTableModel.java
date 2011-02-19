package jp.a840.push.subscriber.swing.table;

import javax.swing.table.TableModel;

public interface SwingClientTableModel extends TableModel {
	public void add(Object obj);
	public void clear();
}
