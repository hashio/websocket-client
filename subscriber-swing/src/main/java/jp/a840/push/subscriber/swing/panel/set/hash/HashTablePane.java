package jp.a840.push.subscriber.swing.panel.set.hash;

import jp.a840.push.beans.RateBean;
import jp.a840.push.subscriber.swing.listener.Filter;
import jp.a840.push.subscriber.swing.listener.ThroughFilter;
import jp.a840.push.subscriber.swing.panel.AbstractInfosTablePane;
import jp.a840.push.subscriber.swing.table.HashTableModel;
import jp.a840.push.subscriber.swing.table.SwingClientDefaultHashTableModel;

public class HashTablePane extends AbstractInfosTablePane {

	private HashTableModel model;
	
	public HashTablePane(HashTableModel model) {
		super();
		this.model = model;
		Filter filter = new ThroughFilter(model);
		setFilter(filter);
	}

}
