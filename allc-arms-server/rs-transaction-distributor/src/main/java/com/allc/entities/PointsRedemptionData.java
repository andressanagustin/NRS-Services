/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * Entidad asociada a una Redencion de Puntos.
 * 
 * @author gustavo
 *
 */
public class PointsRedemptionData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private String time;
	private Integer discount;
	private String codNegocio;
	private String referenceNbr;
	private String authorizationNbr;
	private String tipo;
	private String bin;
	private Integer puntos;
	private Long codItem;
	private Integer ordinalNumber;

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
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * @return the discount
	 */
	public Integer getDiscount() {
		return discount;
	}

	/**
	 * @param discount the discount to set
	 */
	public void setDiscount(Integer discount) {
		this.discount = discount;
	}

	/**
	 * @return the codNegocio
	 */
	public String getCodNegocio() {
		return codNegocio;
	}

	/**
	 * @param codNegocio the codNegocio to set
	 */
	public void setCodNegocio(String codNegocio) {
		this.codNegocio = codNegocio;
	}

	/**
	 * @return the referenceNbr
	 */
	public String getReferenceNbr() {
		return referenceNbr;
	}

	/**
	 * @param referenceNbr the referenceNbr to set
	 */
	public void setReferenceNbr(String referenceNbr) {
		this.referenceNbr = referenceNbr;
	}

	/**
	 * @return the authorizationNbr
	 */
	public String getAuthorizationNbr() {
		return authorizationNbr;
	}

	/**
	 * @param authorizationNbr the authorizationNbr to set
	 */
	public void setAuthorizationNbr(String authorizationNbr) {
		this.authorizationNbr = authorizationNbr;
	}

	/**
	 * @return the tipo
	 */
	public String getTipo() {
		return tipo;
	}

	/**
	 * @param tipo the tipo to set
	 */
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	/**
	 * @return the bin
	 */
	public String getBin() {
		return bin;
	}

	/**
	 * @param bin the bin to set
	 */
	public void setBin(String bin) {
		this.bin = bin;
	}

	/**
	 * @return the puntos
	 */
	public Integer getPuntos() {
		return puntos;
	}

	/**
	 * @param puntos the puntos to set
	 */
	public void setPuntos(Integer puntos) {
		this.puntos = puntos;
	}

	public Long getCodItem() {
		return codItem;
	}

	public void setCodItem(Long codItem) {
		this.codItem = codItem;
	}

	/**
	 * @return the ordinalNumber
	 */
	public Integer getOrdinalNumber() {
		return ordinalNumber;
	}

	/**
	 * @param ordinalNumber the ordinalNumber to set
	 */
	public void setOrdinalNumber(Integer ordinalNumber) {
		this.ordinalNumber = ordinalNumber;
	}

}
