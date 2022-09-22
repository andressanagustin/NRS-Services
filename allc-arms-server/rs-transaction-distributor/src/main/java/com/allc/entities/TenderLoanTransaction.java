/**
 * 
 */
package com.allc.entities;

/**
 * @author GUSTAVOK
 * 
 */
public class TenderLoanTransaction {
	private Integer transactionID;
	private String outbound;
	private String inbound;
	private String operatorCode;

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
	 * @return the outbound
	 */
	public String getOutbound() {
		return outbound;
	}

	/**
	 * @param outbound
	 *            the outbound to set
	 */
	public void setOutbound(String outbound) {
		this.outbound = outbound;
	}

	/**
	 * @return the inbound
	 */
	public String getInbound() {
		return inbound;
	}

	/**
	 * @param inbound
	 *            the inbound to set
	 */
	public void setInbound(String inbound) {
		this.inbound = inbound;
	}

	public String getOperatorCode() {
		return operatorCode;
	}

	public void setOperatorCode(String operatorCode) {
		this.operatorCode = operatorCode;
	}
	
	
}
