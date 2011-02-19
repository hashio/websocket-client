package jp.a840.push.subscriber.swing.panel.set.list;

import jp.a840.push.subscriber.swing.listener.Filter;
import jp.a840.push.subscriber.swing.listener.ThroughFilter;
import jp.a840.push.subscriber.swing.panel.AbstractInfosTablePane;
import jp.a840.push.subscriber.swing.table.BestRateListTableModel;

public class BestRateListTablePane extends AbstractInfosTablePane {
	public BestRateListTablePane() {
		super();
		BestRateListTableModel model = new BestRateListTableModel();
		Filter filter = new ThroughFilter(model);
		setFilter(filter);
	}

}
