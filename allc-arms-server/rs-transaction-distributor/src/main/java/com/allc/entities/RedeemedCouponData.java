package com.allc.entities;

import java.io.Serializable;

public class RedeemedCouponData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private String barcode;
	private Double valueDisc = new Double(0);
	private String timestamp;
	private Integer transactionNumber;
	private String workstation;
	private String store;

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
	 * @return the barcode
	 */
	public String getBarcode() {
		return barcode;
	}

	/**
	 * @param barcode
	 *            the barcode to set
	 */
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	/**
	 * @return the valueDisc
	 */
	public Double getValueDisc() {
		return valueDisc;
	}

	/**
	 * @param valueDisc
	 *            the valueDisc to set
	 */
	public void setValueDisc(Double valueDisc) {
		this.valueDisc = valueDisc;
	}

	/**
	 * @return the timestamp
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the transactionNumber
	 */
	public Integer getTransactionNumber() {
		return transactionNumber;
	}

	/**
	 * @param transactionNumber
	 *            the transactionNumber to set
	 */
	public void setTransactionNumber(Integer transactionNumber) {
		this.transactionNumber = transactionNumber;
	}

	/**
	 * @return the workstation
	 */
	public String getWorkstation() {
		return workstation;
	}

	/**
	 * @param workstation
	 *            the workstation to set
	 */
	public void setWorkstation(String workstation) {
		this.workstation = workstation;
	}

	/**
	 * @return the store
	 */
	public String getStore() {
		return store;
	}

	/**
	 * @param store
	 *            the store to set
	 */
	public void setStore(String store) {
		this.store = store;
	}

}
