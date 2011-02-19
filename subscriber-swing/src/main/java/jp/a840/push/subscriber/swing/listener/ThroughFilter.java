package jp.a840.push.subscriber.swing.listener;

import jp.a840.push.subscriber.swing.table.SwingClientTableModel;

public class ThroughFilter implements Filter {

	private SwingClientTableModel model;

	public ThroughFilter(SwingClientTableModel model) {
		this.model = model;
	}

	public void add(String k, Object obj) {
		model.add(obj);
	}
	
	public SwingClientTableModel getModel() {
		return model;
	}
}
