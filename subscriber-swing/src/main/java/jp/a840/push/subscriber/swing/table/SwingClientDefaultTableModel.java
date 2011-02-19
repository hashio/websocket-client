/*
 * Created on 2004/03/22
 */
package jp.a840.push.subscriber.swing.table;

import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * 
 * @author t_hashimoto
 * @version $Revision$
 * @modifiedBy. $Author$
 */
public class SwingClientDefaultTableModel extends DefaultTableModel implements javax.swing.table.TableModel {

	protected Hashtable orderHeaderColumnMap = new Hashtable();

	protected int maxRowSize = 1000;

	private Comparator comparator;

	private String key;
		
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	public void setMaxRowSize(int maxRowSize) {
		this.maxRowSize = maxRowSize;
	}

	public void insertRow(int row, Vector v) {
		Vector rv = this.getDataVector();
		synchronized (rv) {
			super.insertRow(row, v);
			int overSize = rv.size() - maxRowSize;
			int size = rv.size();
			for (int i = 0; i < overSize; i++) {
				rv.remove(rv.size() - 1);
			}
			if(overSize > 0){
				fireTableRowsDeleted(size - 1, size - 1 + overSize);
			}
		}
	}
	
	public Vector getRow(int index) {
		Vector rv = this.getDataVector();
		return (Vector) rv.get(index);
	}

	public int getColumnIndex(String columnIdentifier) {
		Integer index = (Integer) orderHeaderColumnMap.get(columnIdentifier);
		return index.intValue();
	}

	/**
	 * Describe <code>addColumn</code> method here.
	 * 
	 * @param object
	 *            an <code>Object</code> value
	 */
	public void addColumn(Object object) {
		int size = orderHeaderColumnMap.size();
		orderHeaderColumnMap.put(object, new Integer(size + 1));
		super.addColumn(object);
	}

	public void remove(Object obj) {
		Vector rv = this.getDataVector();
		synchronized (rv) {
			if (obj != null) {
				int idx = rv.indexOf(obj);
				if(idx >= 0){
					rv.remove(idx);
					fireTableRowsDeleted(idx, idx);
				}
			}
		}
	}

	public void clear() {
		Vector rv = this.getDataVector();
		synchronized (rv) {
			rv.clear();
			fireTableDataChanged();
		}
	}

	/**
	 * Describe <code>setColumnIdentifiers</code> method here.
	 * 
	 * @param colmns
	 *            an <code>Vector</code> value
	 */
	public void setColumnIdentifiers(Vector v) {
		orderHeaderColumnMap.clear();
		int index = 0;
		for (Iterator it = v.iterator(); it.hasNext();) {
			orderHeaderColumnMap.put(it.next(), new Integer(index));
			index++;
		}
		super.setColumnIdentifiers(v);
	}

	public void addRow(Vector v){
		Vector rv = this.getDataVector();
		synchronized (rv) {
			setRow(rv.size(), v);
		}
	}
	
	public void setRow(int row, Vector v) {
		Vector rv = this.getDataVector();
		synchronized (rv) {
			boolean isInserted = false;
			int size = rv.size();
			if(row >= size){
				if(row > size){
					row = size;
				}
				isInserted = true;
			}
			if(size == 0){
				super.insertRow(0, v);
			} else if (comparator != null) {
				if (0 <= row && row < rv.size()) {
					rv.remove(row);
				}else{
					isInserted = true;
				}
				int idx = Collections.binarySearch(rv, v, comparator);
				if (idx < 0) {
					idx = (-1 * idx) - 1;
					rv.add(row, v);
					if(row == idx){
						if(isInserted){
							fireTableRowsInserted(row, row);
						}else{
							fireTableRowsUpdated(row, row);
						}
					}else{
						moveRow(row, row, idx);
					}
				}else{
					rv.add(row, v);
					int lastIndex = rv.size() - 1;
					if(row < lastIndex){
						if(comparator.compare(rv.get(row), v) == 0
						|| comparator.compare(rv.get(row + 1), v) == 0){
							fireTableRowsUpdated(row, row);
						}else{
							moveRow(row, row, idx);
						}
					}else if(row == lastIndex){
						if(isInserted){
							fireTableRowsInserted(row, row);
						}else{
							fireTableRowsUpdated(row, row);
						}
					}else{
						moveRow(row, row, idx);
					}
				}
			} else {
				if(isInserted){
					rv.add(row, v);
					fireTableRowsInserted(row, row);
				}else{
					rv.set(row, v);
					fireTableRowsUpdated(row, row);
				}
			}
		}
	}

	public void sort(Comparator comparator) {
		Vector rv = this.getDataVector();
		synchronized (rv) {
			Collections.sort(rv, comparator);
			this.comparator = comparator;
			fireTableDataChanged();
		}
	}

	public Comparator getComparator() {
		return comparator;
	}

}
