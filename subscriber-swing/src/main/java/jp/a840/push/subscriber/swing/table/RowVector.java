package jp.a840.push.subscriber.swing.table;

import java.util.Vector;


public class RowVector extends Vector {
	private RowAttribute attribute;

	public RowVector() {
	}

	public RowVector(Vector v) {
		super(v);
	}

	public RowAttribute getAttribute() {
		return attribute;
	}

	public void setAttribute(RowAttribute attr) {
		attribute = attr;
	}	
}
