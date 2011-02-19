package jp.a840.push.subscriber.swing.panel.lastupdate.hash;

import javax.swing.JTable;

import jp.a840.push.subscriber.swing.listener.Filter;
import jp.a840.push.subscriber.swing.listener.ThroughFilter;
import jp.a840.push.subscriber.swing.panel.AbstractInfosTablePane;


public class LastUpdatePane extends AbstractInfosTablePane {

	public LastUpdatePane(LastUpdateTableModel lastUpdateTableModel){
		super();
		Filter filter = new ThroughFilter(lastUpdateTableModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		this.setFilter(filter);
	}
}
