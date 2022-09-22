/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class AutomaticCouponData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Double mfgCouponAmount = new Double(0);
	private Double storeCouponAmount = new Double(0);
	private Double mfgCouponCount = new Double(0);
	private Double storeCouponCount = new Double(0);

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
	 * @return the mfgCouponAmount
	 */
	public Double getMfgCouponAmount() {
		return mfgCouponAmount;
	}

	/**
	 * @param mfgCouponAmount
	 *            the mfgCouponAmount to set
	 */
	public void setMfgCouponAmount(Double mfgCouponAmount) {
		this.mfgCouponAmount = mfgCouponAmount;
	}

	/**
	 * @return the storeCouponAmount
	 */
	public Double getStoreCouponAmount() {
		return storeCouponAmount;
	}

	/**
	 * @param storeCouponAmount
	 *            the storeCouponAmount to set
	 */
	public void setStoreCouponAmount(Double storeCouponAmount) {
		this.storeCouponAmount = storeCouponAmount;
	}

	/**
	 * @return the mfgCouponCount
	 */
	public Double getMfgCouponCount() {
		return mfgCouponCount;
	}

	/**
	 * @param mfgCouponCount
	 *            the mfgCouponCount to set
	 */
	public void setMfgCouponCount(Double mfgCouponCount) {
		this.mfgCouponCount = mfgCouponCount;
	}

	/**
	 * @return the storeCouponCount
	 */
	public Double getStoreCouponCount() {
		return storeCouponCount;
	}

	/**
	 * @param storeCouponCount
	 *            the storeCouponCount to set
	 */
	public void setStoreCouponCount(Double storeCouponCount) {
		this.storeCouponCount = storeCouponCount;
	}

}
