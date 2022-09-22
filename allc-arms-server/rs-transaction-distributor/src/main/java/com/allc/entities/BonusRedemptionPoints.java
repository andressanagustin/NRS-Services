/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class BonusRedemptionPoints implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private Double itemCode = new Double(0);
	private Double value = new Double(0);
	private Double moreFlags = new Double(0);
	private Double families = new Double(0);

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
	 * @return the sequenceNumber
	 */
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * @param sequenceNumber
	 *            the sequenceNumber to set
	 */
	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	/**
	 * @return the itemCode
	 */
	public Double getItemCode() {
		return itemCode;
	}

	/**
	 * @param itemCode
	 *            the itemCode to set
	 */
	public void setItemCode(Double itemCode) {
		this.itemCode = itemCode;
	}

	/**
	 * @return the value
	 */
	public Double getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Double value) {
		this.value = value;
	}

	/**
	 * @return the moreFlags
	 */
	public Double getMoreFlags() {
		return moreFlags;
	}

	/**
	 * @param moreFlags
	 *            the moreFlags to set
	 */
	public void setMoreFlags(Double moreFlags) {
		this.moreFlags = moreFlags;
	}

	/**
	 * @return the families
	 */
	public Double getFamilies() {
		return families;
	}

	/**
	 * @param families
	 *            the families to set
	 */
	public void setFamilies(Double families) {
		this.families = families;
	}

}
