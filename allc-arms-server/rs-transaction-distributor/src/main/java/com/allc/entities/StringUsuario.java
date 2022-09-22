/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * Entidad que representa a todos los strings de usuario (99) asociados a una
 * transacción.
 * 
 * @author gustavo
 *
 */
public class StringUsuario implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private String cadena;

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
	 * @return the cadena
	 */
	public String getCadena() {
		return cadena;
	}

	/**
	 * @param cadena
	 *            the cadena to set
	 */
	public void setCadena(String cadena) {
		this.cadena = cadena;
	}

}
