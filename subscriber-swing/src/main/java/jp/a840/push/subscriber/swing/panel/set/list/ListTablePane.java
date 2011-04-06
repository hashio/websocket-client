package jp.a840.push.subscriber.swing.panel.set.list;

import jp.a840.push.subscriber.swing.listener.Filter;
import jp.a840.push.subscriber.swing.listener.ThroughFilter;
import jp.a840.push.subscriber.swing.panel.AbstractInfosTablePane;
import jp.a840.push.subscriber.swing.table.ListTableModel;

public class ListTablePane extends AbstractInfosTablePane {
	private ListTableModel model;
	
	public ListTablePane(ListTableModel model) {
		super();
		this.model = model;
		Filter filter = new ThroughFilter(model);
		setFilter(filter);
	}

}
