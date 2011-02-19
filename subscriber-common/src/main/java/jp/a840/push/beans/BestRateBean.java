package jp.a840.push.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class BestRateBean implements Serializable {
	/** */
	private static final long serialVersionUID = 1L;

	/** 業務日付 */
	private String businessDate;

	/** 銘柄コード */
	private Integer productCode;

	/** 処理通番 */
	private Long processSeq;

	/** 取引所更新日時 */
	private Date marketUpdateDatetime;

	/** 買最良気配値 */
	private BigDecimal bid;

	/** 買最良気配数量 */
	private Integer bidSize;

	/** 買無効フラグ */
	private boolean bidInvalidFlag;

	/** 買最新有効最良気配値 */
	private BigDecimal bidLatestValid;

	/** 売最良気配値 */
	private BigDecimal ask;

	/** 売最良気配数量 */
	private Integer askSize;

	/** 売無効フラグ */
	private boolean askInvalidFlag;

	/** 売最新有効最良気配値 */
	private BigDecimal askLatestValid;

	/** 基準価格 */
	private BigDecimal limitCheckPrice;


	/**
	 * @return productCode
	 */
	public Integer getProductCode() {
		return productCode;
	}

	/**
	 * @param productCode
	 *            セットする productCode
	 */
	public void setProductCode(Integer productCode) {
		this.productCode = productCode;
	}

	/**
	 * @return marketUpdateDatetime
	 */
	public Date getMarketUpdateDatetime() {
		return marketUpdateDatetime;
	}

	/**
	 * @param marketUpdateDatetime
	 *            セットする marketUpdateDatetime
	 */
	public void setMarketUpdateDatetime(Date marketUpdateDatetime) {
		this.marketUpdateDatetime = marketUpdateDatetime;
	}

	/**
	 * @return bid
	 */
	public BigDecimal getBid() {
		return bid;
	}

	/**
	 * @param bid
	 *            セットする bid
	 */
	public void setBid(BigDecimal bid) {
		this.bid = bid;
	}

	/**
	 * @return bidSize
	 */
	public Integer getBidSize() {
		return bidSize;
	}

	/**
	 * @param bidSize
	 *            セットする bidSize
	 */
	public void setBidSize(Integer bidSize) {
		this.bidSize = bidSize;
	}

	/**
	 * @return bidInvalidFlag
	 */
	public boolean isBidInvalidFlag() {
		return bidInvalidFlag;
	}

	/**
	 * @param bidInvalidFlag
	 *            セットする bidInvalidFlag
	 */
	public void setBidInvalidFlag(boolean bidInvalidFlag) {
		this.bidInvalidFlag = bidInvalidFlag;
	}

	/**
	 * @return askSize
	 */
	public Integer getAskSize() {
		return askSize;
	}

	/**
	 * @param askSize
	 *            セットする askSize
	 */
	public void setAskSize(Integer askSize) {
		this.askSize = askSize;
	}

	/**
	 * @return ask
	 */
	public BigDecimal getAsk() {
		return ask;
	}

	/**
	 * @param ask
	 *            セットする ask
	 */
	public void setAsk(BigDecimal ask) {
		this.ask = ask;
	}

	/**
	 * @return askInvalidFlag
	 */
	public boolean isAskInvalidFlag() {
		return askInvalidFlag;
	}

	/**
	 * @param askInvalidFlag
	 *            セットする askInvalidFlag
	 */
	public void setAskInvalidFlag(boolean askInvalidFlag) {
		this.askInvalidFlag = askInvalidFlag;
	}

	/**
	 * @return limitCheckPrice
	 */
	public BigDecimal getLimitCheckPrice() {
		return limitCheckPrice;
	}

	/**
	 * @param limitCheckPrice
	 *            セットする limitCheckPrice
	 */
	public void setLimitCheckPrice(BigDecimal limitCheckPrice) {
		this.limitCheckPrice = limitCheckPrice;
	}

	/**
	 * @return serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @return businessDate
	 */
	public String getBusinessDate() {
		return businessDate;
	}

	/**
	 * @param businessDate
	 *            セットする businessDate
	 */
	public void setBusinessDate(String businessDate) {
		this.businessDate = businessDate;
	}

	/**
	 * @return bidLatestValid
	 */
	public BigDecimal getBidLatestValid() {
		if (bid != null && (bidLatestValid == null || bidLatestValid.doubleValue() == 0)) {
			throw new RuntimeException("あり得ないエラー bid:" + bid + " bidLatestValid:" + bidLatestValid);
		}

		return bidLatestValid;
	}

	/**
	 * @param bidLatestValid
	 *            セットする bidLatestValid
	 */
	public void setBidLatestValid(BigDecimal bidLatestValid) {
		this.bidLatestValid = bidLatestValid;
	}

	/**
	 * @return askLatestValid
	 */
	public BigDecimal getAskLatestValid() {
		if (ask != null && (askLatestValid == null || askLatestValid.doubleValue() == 0)) {
			throw new RuntimeException("あり得ないエラー ask" + ask + " askLatestValid:" + askLatestValid);
		}

		return askLatestValid;
	}

	/**
	 * @param askLatestValid
	 *            セットする askLatestValid
	 */
	public void setAskLatestValid(BigDecimal askLatestValid) {
		this.askLatestValid = askLatestValid;
	}

	/**
	 * @return processSeq
	 */
	public Long getProcessSeq() {
		return processSeq;
	}

	/**
	 * @param processSeq
	 *            セットする processSeq
	 */
	public void setProcessSeq(Long processSeq) {
		this.processSeq = processSeq;
	}
}
