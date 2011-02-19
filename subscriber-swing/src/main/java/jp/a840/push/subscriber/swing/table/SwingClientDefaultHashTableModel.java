/*
 * Created on 2004/03/22
 */
package jp.a840.push.subscriber.swing.table;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Timer;

import jp.a840.push.subscriber.swing.table.blink.BlinkRow;


/**
 * 
 * @author t_hashimoto
 * @version $Revision$
 * @modifiedBy. $Author$
 */
public class SwingClientDefaultHashTableModel extends SwingClientDefaultTableModel {

	protected Hashtable orderKeyMap = new Hashtable();

	public RowAttribute getAttribute(int row){
		return ((RowVector)super.getRow(row)).getAttribute();
	}
	
	public void setRow(String key, RowVector v) {
		Vector dv = this.getDataVector();
		synchronized (dv) {
			int order = getOrderNumber(key);
			RowAttribute attr = null;
			if (order < 0) {
				attr = new RowAttribute();
				BlinkRow row = new BlinkRow(this);
				attr.setBlinkRow(row);
				attr.setBlinkTimer(new Timer(100, row));
				attr.setKey(key);
				v.setAttribute(attr);
				addRow(v);
			} else {
				RowVector rv = (RowVector)dv.get(order);
				attr = rv.getAttribute();
				attr.getBlinkRow().init();
				v.setAttribute(attr);
				setRow(order, v);
			}

			refreshOrderMap();
			attr.getBlinkTimer().start();
		}
	}

	public int getOrderNumber(String key) {
		if (orderKeyMap.containsKey(key)) {
			return ((Integer) orderKeyMap.get(key)).intValue();
		} else {
			return -1;
		}
	}

	private void refreshOrderMap(){
		int i = 0;
		Vector rv = this.getDataVector();
		for (Iterator it = rv.iterator(); it.hasNext();) {
			RowVector v = (RowVector)it.next();
			RowAttribute ra = v.getAttribute();
			ra.getBlinkRow().setRow(i);
			orderKeyMap.put(ra.getKey(), new Integer(i));
			i++;
		}		
	}
	
	public void sort(Comparator comparator) {
		Vector rv = this.getDataVector();
		synchronized (rv) {
			super.sort(comparator);
			refreshOrderMap();
		}
	}

	public synchronized void clear() {
		orderKeyMap.clear();
		super.clear();
	}
}
