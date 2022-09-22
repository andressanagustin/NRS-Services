/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class CarryForwardTransaction implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer idBusinessEOD;
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
	 * @return the idBusinessEOD
	 */
	public Integer getIdBusinessEOD() {
		return idBusinessEOD;
	}

	/**
	 * @param idBusinessEOD the idBusinessEOD to set
	 */
	public void setIdBusinessEOD(Integer idBusinessEOD) {
		this.idBusinessEOD = idBusinessEOD;
	}
	
}
