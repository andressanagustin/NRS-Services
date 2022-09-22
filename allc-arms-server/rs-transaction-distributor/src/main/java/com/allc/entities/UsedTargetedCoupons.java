/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class UsedTargetedCoupons implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Double customerAccountID = new Double(0);
	private Double targetedCoupon = new Double(0);

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
	 * @return the customerAccountID
	 */
	public Double getCustomerAccountID() {
		return customerAccountID;
	}

	/**
	 * @param customerAccountID
	 *            the customerAccountID to set
	 */
	public void setCustomerAccountID(Double customerAccountID) {
		this.customerAccountID = customerAccountID;
	}

	/**
	 * @return the targetedCoupon
	 */
	public Double getTargetedCoupon() {
		return targetedCoupon;
	}

	/**
	 * @param targetedCoupon
	 *            the targetedCoupon to set
	 */
	public void setTargetedCoupon(Double targetedCoupon) {
		this.targetedCoupon = targetedCoupon;
	}

}
