package jp.a840.push.subscriber.swing.util.comparator;

import java.util.Vector;
import java.util.Comparator;

public class RowLargeComparator implements Comparator {
	private int keyColumn = 0;
	
	public RowLargeComparator(int keyColumn){
		this.keyColumn = keyColumn;
	}

	public int compare(Object object, Object object1) {
		Vector v1 = (Vector)object;
		Vector v2 = (Vector)object1;
		Comparable o1 = (Comparable)v1.get(keyColumn);
		Comparable o2 = (Comparable)v2.get(keyColumn);
		return o1.compareTo(o2);
	}
}
