package jp.a840.push.subscriber.swing.panel.lastupdate.hash;

import javax.swing.JTable;

import jp.a840.push.subscriber.swing.listener.Filter;
import jp.a840.push.subscriber.swing.listener.ThroughFilter;
import jp.a840.push.subscriber.swing.panel.AbstractInfosTablePane;
import jp.a840.push.subscriber.swing.table.HashTableModel;


public class LastUpdatePane extends AbstractInfosTablePane {

	public LastUpdatePane(HashTableModel hashTableModel){
		super();
		Filter filter = new ThroughFilter(hashTableModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		this.setFilter(filter);
	}
}
