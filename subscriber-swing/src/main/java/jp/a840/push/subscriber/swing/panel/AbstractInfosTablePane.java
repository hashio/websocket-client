package jp.a840.push.subscriber.swing.panel;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import jp.a840.push.subscriber.swing.listener.Filter;
import jp.a840.push.subscriber.swing.listener.RealtimeTableModelManager;
import jp.a840.push.subscriber.swing.table.SortInfosTableModelRecordMouseHandler;
import jp.a840.push.subscriber.swing.table.SwingClientDefaultHashTableModel;
import jp.a840.push.subscriber.swing.table.blink.BlinkableRenderer;
import jp.a840.push.subscriber.swing.table.renderer.DefaultProductCellRenderer;


public abstract class AbstractInfosTablePane extends JScrollPane implements AncestorListener {
	protected JTable table;

	protected Filter filter;
	
	public AbstractInfosTablePane() {
		this.table = new JTable();

		table.setShowGrid(true);
		table.setGridColor(Color.GRAY);

		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(true);

		JTableHeader h = table.getTableHeader();
		h.addMouseListener(new SortInfosTableModelRecordMouseHandler());
		table.setBorder(BorderFactory.createLineBorder(Color.black));
		// table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.getViewport().setView(table);
		this.setPreferredSize(new Dimension(640, 480));
		this.addAncestorListener(this);
	}

	public void setFilter(Filter filter){
		this.filter = filter;
		TableModel model = filter.getModel();
		table.setModel(model);

		TableCellRenderer renderer = createTableCellRenderer();
		if (model instanceof SwingClientDefaultHashTableModel) {
			table.setDefaultRenderer(Object.class, new BlinkableRenderer(renderer));
		} else {
			table.setDefaultRenderer(Object.class, renderer);
		}
	}
	
	protected TableCellRenderer createTableCellRenderer() {
		return new DefaultProductCellRenderer();
	}

	public void ancestorAdded(AncestorEvent arg0) {
		RealtimeTableModelManager tmm = RealtimeTableModelManager.getInstance();
		tmm.addFilter(filter);
	}

	public void ancestorMoved(AncestorEvent arg0) {
	}

	public void ancestorRemoved(AncestorEvent arg0) {
		RealtimeTableModelManager tmm = RealtimeTableModelManager.getInstance();
		tmm.removeFilter(filter);
	}

	public Filter getFilter() {
		return filter;
	}

	public JTable getTable() {
		return table;
	}
	
	
}
