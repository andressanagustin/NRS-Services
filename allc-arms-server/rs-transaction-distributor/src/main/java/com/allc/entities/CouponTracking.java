/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class CouponTracking implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Double logFlags = new Double(0);
	private Double campaignNumber = new Double(0);
	private Double mfgNumber = new Double(0);
	private Double promotionCode = new Double(0);

	/**
	 * @return the transactionID
	 */
	public Integer getTransactionID() {
		return transactionID;
	}

	/**
	 * @param transactionID
	 *            the transactionID to set
	 */
	public void setTransactionID(Integer transactionID) {
		this.transactionID = transactionID;
	}

	/**
	 * @return the logFlags
	 */
	public Double getLogFlags() {
		return logFlags;
	}

	/**
	 * @param logFlags
	 *            the logFlags to set
	 */
	public void setLogFlags(Double logFlags) {
		this.logFlags = logFlags;
	}

	/**
	 * @return the campaignNumber
	 */
	public Double getCampaignNumber() {
		return campaignNumber;
	}

	/**
	 * @param campaignNumber
	 *            the campaignNumber to set
	 */
	public void setCampaignNumber(Double campaignNumber) {
		this.campaignNumber = campaignNumber;
	}

	/**
	 * @return the mfgNumber
	 */
	public Double getMfgNumber() {
		return mfgNumber;
	}

	/**
	 * @param mfgNumber
	 *            the mfgNumber to set
	 */
	public void setMfgNumber(Double mfgNumber) {
		this.mfgNumber = mfgNumber;
	}

	/**
	 * @return the promotionCode
	 */
	public Double getPromotionCode() {
		return promotionCode;
	}

	/**
	 * @param promotionCode
	 *            the promotionCode to set
	 */
	public void setPromotionCode(Double promotionCode) {
		this.promotionCode = promotionCode;
	}

}
