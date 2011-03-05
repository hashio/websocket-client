package jp.a840.push.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class RateBean implements Serializable {
	/** */
	private static final long serialVersionUID = 1L;

	private String currencyPair;

	private BigDecimal bid;

	private BigDecimal ask;
	
	private Date updateTime;

	public BigDecimal getBid() {
		return bid;
	}

	public void setBid(BigDecimal bid) {
		this.bid = bid;
	}

	public BigDecimal getAsk() {
		return ask;
	}

	public void setAsk(BigDecimal ask) {
		this.ask = ask;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getCurrencyPair() {
		return currencyPair;
	}

	public void setCurrencyPair(String currencyPair) {
		this.currencyPair = currencyPair;
	}
}
