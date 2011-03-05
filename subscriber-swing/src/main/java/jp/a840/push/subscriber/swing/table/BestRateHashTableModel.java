package jp.a840.push.subscriber.swing.table;

import java.lang.reflect.Method;
import java.util.Vector;

import jp.a840.push.beans.RateBean;
import jp.a840.push.subscriber.swing.util.SwingClientDefaultTableModelUtil;

public class BestRateHashTableModel extends SwingClientDefaultHashTableModel implements SwingClientTableModel {

	private Method[] methods;

	public void add(Object obj) {
		try {
			RateBean dto = (RateBean)obj;
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
	
	private String getGroupingKey(RateBean dto){
		return String.valueOf(dto.getCurrencyPair());
	}

	private void setHeader(RateBean dto) {
		Vector v = SwingClientDefaultTableModelUtil.createHeader(dto);
		setColumnIdentifiers(v);
	}
}
