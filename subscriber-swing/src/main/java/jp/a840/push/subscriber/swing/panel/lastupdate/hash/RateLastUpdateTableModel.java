package jp.a840.push.subscriber.swing.panel.lastupdate.hash;

import jp.a840.push.beans.BestRateBean;
import jp.a840.push.subscriber.swing.table.RowVector;


public class RateLastUpdateTableModel extends LastUpdateTableModel {

	@Override
	protected RowVector createHeader(Object obj) {
		RowVector rv = new RowVector();
		if(obj instanceof BestRateBean){
			rv.add("通貨ペア");
			rv.add("Bid");
			rv.add("Ask");
			rv.add("受信日時");
		}
		return rv;
	}

	@Override
	protected RowVector createRowVector(Object obj) {
		RowVector rv = new RowVector();
		if(obj instanceof BestRateBean){
			BestRateBean dto = (BestRateBean)obj;
			rv.add(dto.getProductCode());
			rv.add(dto.getBid());
			rv.add(dto.getAsk());
			rv.add(dto.getMarketUpdateDatetime());
		}
		return rv;
	}

	@Override
	protected String createRowVectorKey(Object obj) {
		if(obj instanceof BestRateBean){
			BestRateBean dto = (BestRateBean)obj;
			return String.valueOf(dto.getProductCode());
		}
		return "";
	}

}
