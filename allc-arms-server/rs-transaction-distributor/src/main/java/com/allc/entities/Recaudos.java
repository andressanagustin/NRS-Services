package com.allc.entities;

import java.io.Serializable;

public class Recaudos implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer transactionID;
	private Integer sequenceNumber;
	private String tipoTienda;
	private String tipoRecaudo;
	private String codArticulo;
	private String monto;
	
	
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
	public String getTipoTienda() {
		return tipoTienda;
	}
	public void setTipoTienda(String tipoTienda) {
		this.tipoTienda = tipoTienda;
	}
	public String getTipoRecaudo() {
		return tipoRecaudo;
	}
	public void setTipoRecaudo(String tipoRecaudo) {
		this.tipoRecaudo = tipoRecaudo;
	}
	public String getCodArticulo() {
		return codArticulo;
	}
	public void setCodArticulo(String codArticulo) {
		this.codArticulo = codArticulo;
	}
	public String getMonto() {
		return monto;
	}
	public void setMonto(String monto) {
		this.monto = monto;
	}

	
	
	

}
