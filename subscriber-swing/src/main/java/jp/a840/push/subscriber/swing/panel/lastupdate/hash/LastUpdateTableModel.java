/*
 * Created on 2004/03/22
 */
package jp.a840.push.subscriber.swing.panel.lastupdate.hash;

import java.util.Vector;

import jp.a840.push.subscriber.swing.table.RowVector;
import jp.a840.push.subscriber.swing.table.SwingClientDefaultHashTableModel;
import jp.a840.push.subscriber.swing.table.SwingClientTableModel;


/**
 * 
 * @author t_hashimoto
 * @version $Revision$
 * @modifiedBy. $Author$
 */
abstract public class LastUpdateTableModel extends SwingClientDefaultHashTableModel implements SwingClientTableModel {
	
	public void add(Object obj){
        RowVector v = createRowVector(obj);
        if (getColumnCount() == 0) {
            setHeader(obj);
        }
        String key = createRowVectorKey(obj);
        setRow(key, v);
	}

	protected void setHeader(Object obj) {
		Vector v = createHeader(obj);
		setColumnIdentifiers(v);
	}
	
	abstract protected RowVector createRowVector(Object obj);
	
	abstract protected String createRowVectorKey(Object obj);
	
	abstract protected RowVector createHeader(Object obj);
}
