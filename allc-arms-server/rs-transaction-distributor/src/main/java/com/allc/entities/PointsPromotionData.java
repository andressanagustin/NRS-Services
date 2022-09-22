package com.allc.entities;

import java.io.Serializable;

public class PointsPromotionData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private Integer lineaNegocio;
	private Integer cantidad;
	
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
	public Integer getLineaNegocio() {
		return lineaNegocio;
	}
	public void setLineaNegocio(Integer lineaNegocio) {
		this.lineaNegocio = lineaNegocio;
	}
	public Integer getCantidad() {
		return cantidad;
	}
	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}
	
	
	
	

}
