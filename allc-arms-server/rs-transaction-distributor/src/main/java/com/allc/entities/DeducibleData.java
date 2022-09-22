package com.allc.entities;

import java.io.Serializable;

/**
 * Entidad que representa un registro de la tabla CO_DEDUCIBLE.
 * 
 * @author gustavo
 *
 */
public class DeducibleData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private String comestible;
	private String ropa;
	private String escolar;
	private String medicina;

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
	 * @return the comestible
	 */
	public String getComestible() {
		return comestible;
	}

	/**
	 * @param comestible
	 *            the comestible to set
	 */
	public void setComestible(String comestible) {
		this.comestible = comestible;
	}

	/**
	 * @return the ropa
	 */
	public String getRopa() {
		return ropa;
	}

	/**
	 * @param ropa
	 *            the ropa to set
	 */
	public void setRopa(String ropa) {
		this.ropa = ropa;
	}

	/**
	 * @return the escolar
	 */
	public String getEscolar() {
		return escolar;
	}

	/**
	 * @param escolar
	 *            the escolar to set
	 */
	public void setEscolar(String escolar) {
		this.escolar = escolar;
	}

	public String getMedicina() {
		return medicina;
	}

	public void setMedicina(String medicina) {
		this.medicina = medicina;
	}

}
