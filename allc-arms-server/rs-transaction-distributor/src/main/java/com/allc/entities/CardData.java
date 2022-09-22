package com.allc.entities;

import java.io.Serializable;

public class CardData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer transactionID;
	private Integer sequenceNumber;
	private String cardNumber;
	private String cardType;
	private Integer flagAutoSyscard;
	private String activationCode;
	private String sequenceTrx;
	private String hora;
	private String idTitular;
	private Double monto = new Double(0);
	private Double cupoDisponible = new Double(0);
	private String stsTarjeta;
	private String codRta;
	
	
	public Integer getTransactionID() {
		return transactionID;
	}
	public void setTransactionID(Integer transactionID) {
		this.transactionID = transactionID;
	}
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	public String getCardNumber() {
		return cardNumber;
	}
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	
	public Integer getFlagAutoSyscard() {
		return flagAutoSyscard;
	}
	public void setFlagAutoSyscard(Integer flagAutoSyscard) {
		this.flagAutoSyscard = flagAutoSyscard;
	}
	public String getActivationCode() {
		return activationCode;
	}
	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}
	/**
	 * @return the sequenceTrx
	 */
	public String getSequenceTrx() {
		return sequenceTrx;
	}
	/**
	 * @param sequenceTrx the sequenceTrx to set
	 */
	public void setSequenceTrx(String sequenceTrx) {
		this.sequenceTrx = sequenceTrx;
	}
	public String getHora() {
		return hora;
	}
	public void setHora(String hora) {
		this.hora = hora;
	}
	public String getIdTitular() {
		return idTitular;
	}
	public void setIdTitular(String idTitular) {
		this.idTitular = idTitular;
	}
	public Double getMonto() {
		return monto;
	}
	public void setMonto(Double monto) {
		this.monto = monto;
	}
	public Double getCupoDisponible() {
		return cupoDisponible;
	}
	public void setCupoDisponible(Double cupoDisponible) {
		this.cupoDisponible = cupoDisponible;
	}
	public String getStsTarjeta() {
		return stsTarjeta;
	}
	public void setStsTarjeta(String stsTarjeta) {
		this.stsTarjeta = stsTarjeta;
	}
	public String getCodRta() {
		return codRta;
	}
	public void setCodRta(String codRta) {
		this.codRta = codRta;
	}
	
	
	
}
