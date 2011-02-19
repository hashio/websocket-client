package jp.a840.push.subscriber.swing.table;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import jp.a840.push.subscriber.swing.util.comparator.RowLargeComparator;
import jp.a840.push.subscriber.swing.util.comparator.RowSmallComparator;

public class SortInfosTableModelRecordMouseHandler extends MouseAdapter {
	boolean toggle = false;
	
	public SortInfosTableModelRecordMouseHandler(){
	}

	/**
	 * Describe <code>mouseClicked</code> method here.
	 *
	 * @param mouseEvent a <code>MouseEvent</code> value
	 */
	public void mouseClicked(MouseEvent mouseEvent) {
		JTableHeader h = (JTableHeader)mouseEvent.getSource();
		SwingClientDefaultTableModel tm = (SwingClientDefaultTableModel)h.getTable().getModel();
		TableColumn c = getColumn(mouseEvent);
		Comparator comparator;
		if (toggle) {
			comparator = new RowSmallComparator(c.getModelIndex());
		}else {
			comparator = new RowLargeComparator(c.getModelIndex());
		} // end of else
		tm.sort(comparator);
		toggle = !toggle;
			
		System.out.println("Clicked: " + c.getHeaderValue());
			
	}
		
	/**
	 * Describe <code>mouseEntered</code> method here.
	 *
	 * @param mouseEvent a <code>MouseEvent</code> value
	 */
	/* for debug
	   public void mouseEntered(MouseEvent mouseEvent) {
	   TableColumn c = getColumn(mouseEvent);
	   System.out.println("Entered: " + c.getHeaderValue());
	   }
	*/
		
	public TableColumn getColumn(MouseEvent e){
		JTableHeader h = (JTableHeader)e.getSource();
		TableColumnModel cm = h.getColumnModel();
		return cm.getColumn(cm.getColumnIndexAtX(e.getX()));
	}
}
