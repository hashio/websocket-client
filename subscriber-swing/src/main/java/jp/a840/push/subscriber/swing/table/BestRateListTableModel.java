package jp.a840.push.subscriber.swing.table;

import java.lang.reflect.Method;
import java.util.Vector;

import jp.a840.push.beans.RateBean;
import jp.a840.push.subscriber.swing.util.SwingClientDefaultTableModelUtil;


public class BestRateListTableModel extends SwingClientDefaultTableModel implements SwingClientTableModel {

	private Method[] methods;

	public void add(Object obj) {
		try {
			RateBean dto = (RateBean)obj;
			if (methods == null) {
				methods = SwingClientDefaultTableModelUtil.compileData(dto);
			}
			Vector v = new Vector();
			for (int i = 0; i < methods.length; i++) {
				v.add(methods[i].invoke(dto, null));
			}
			if (getColumnCount() == 0) {
				setHeader(dto);
			}
			insertRow(0, v);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setHeader(RateBean dto) {
		setColumnIdentifiers(SwingClientDefaultTableModelUtil.createHeader(dto));
	}
}
