package jp.a840.push.subscriber.swing.panel.set.hash;

import jp.a840.push.subscriber.swing.listener.Filter;
import jp.a840.push.subscriber.swing.listener.ThroughFilter;
import jp.a840.push.subscriber.swing.panel.AbstractInfosTablePane;
import jp.a840.push.subscriber.swing.table.BestRateHashTableModel;
import jp.a840.push.subscriber.swing.table.SwingClientDefaultHashTableModel;

public class BestRateHashTablePane extends AbstractInfosTablePane {

	private Filter filter;
	
	public BestRateHashTablePane() {
		super();
		BestRateHashTableModel model = new BestRateHashTableModel();
		Filter filter = new ThroughFilter(model);
		setFilter(filter);
	}

}
