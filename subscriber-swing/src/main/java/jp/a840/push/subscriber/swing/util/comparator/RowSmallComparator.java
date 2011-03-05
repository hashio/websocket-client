package jp.a840.push.subscriber.swing.util.comparator;

import java.util.Vector;
import java.util.Comparator;

public class RowSmallComparator implements Comparator {
	private int keyColumn = 0;
	
	public RowSmallComparator(int keyColumn){
		this.keyColumn = keyColumn;
	}

	public int compare(Object object, Object object1) {
		Vector v1 = (Vector)object;
		Vector v2 = (Vector)object1;
		Comparable o1 = (Comparable)v1.get(keyColumn);
		Comparable o2 = (Comparable)v2.get(keyColumn);
		return o2.compareTo(o1);
	}
}
