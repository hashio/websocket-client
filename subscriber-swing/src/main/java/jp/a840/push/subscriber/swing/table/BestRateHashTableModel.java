/*
 * Created on 2004/03/22
 */
package jp.a840.push.subscriber.swing.table;

import java.lang.reflect.Method;
import java.util.Vector;

import jp.a840.push.beans.BestRateBean;
import jp.a840.push.subscriber.swing.util.SwingClientDefaultTableModelUtil;


/**
 * 
 * @author t_hashimoto
 * @version $Revision$
 * @modifiedBy. $Author$
 */
public class BestRateHashTableModel extends SwingClientDefaultHashTableModel implements SwingClientTableModel {

	private Method[] methods;

	public void add(Object obj) {
		try {
			BestRateBean dto = (BestRateBean)obj;
			if (methods == null) {
				methods = SwingClientDefaultTableModelUtil.compileData(dto);
			}
			RowVector v = new RowVector();
			for (int i = 0; i < methods.length; i++) {
				v.add(methods[i].invoke(dto, null));
			}
			if (getColumnCount() == 0) {
				setHeader(dto);
			}
			String key = getGroupingKey(dto);
			setRow(key, v);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getGroupingKey(BestRateBean dto){
		return String.valueOf(dto.getProductCode());
	}

	private void setHeader(BestRateBean dto) {
		Vector v = SwingClientDefaultTableModelUtil.createHeader(dto);
		setColumnIdentifiers(v);
	}
}
