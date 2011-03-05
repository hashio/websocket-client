package jp.a840.push.subscriber.swing.panel.lastupdate.hash;

import jp.a840.push.beans.RateBean;
import jp.a840.push.subscriber.swing.table.RowVector;


public class RateLastUpdateTableModel extends LastUpdateTableModel {

	@Override
	protected RowVector createHeader(Object obj) {
		RowVector rv = new RowVector();
		if(obj instanceof RateBean){
			rv.add("Pair");
			rv.add("Bid");
			rv.add("Ask");
			rv.add("UpdateTime");
		}
		return rv;
	}

	@Override
	protected RowVector createRowVector(Object obj) {
		RowVector rv = new RowVector();
		if(obj instanceof RateBean){
			RateBean dto = (RateBean)obj;
			rv.add(dto.getCurrencyPair());
			rv.add(dto.getBid());
			rv.add(dto.getAsk());
			rv.add(dto.getUpdateTime());
		}
		return rv;
	}

	@Override
	protected String createRowVectorKey(Object obj) {
		if(obj instanceof RateBean){
			RateBean dto = (RateBean)obj;
			return String.valueOf(dto.getCurrencyPair());
		}
		return "";
	}

}
