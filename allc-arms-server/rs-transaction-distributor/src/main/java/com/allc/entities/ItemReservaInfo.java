package com.allc.entities;

import java.io.Serializable;

public class ItemReservaInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private String trxNum;
	private String codTienda;
	private String itemCode;
	private String numReserva;
	private String numSerie;
	private String fecha;
	private Double monto = new Double(0);
	
	
	public Integer getTransactionID() {
		return transactionID;
	}
	public void setTransactionID(Integer transactionID) {
		this.transactionID = transactionID;
	}
	public String getTrxNum() {
		return trxNum;
	}
	public void setTrxNum(String trxNum) {
		this.trxNum = trxNum;
	}
	public String getCodTienda() {
		return codTienda;
	}
	public void setCodTienda(String codTienda) {
		this.codTienda = codTienda;
	}
	public String getItemCode() {
		return itemCode;
	}
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}
	public String getNumReserva() {
		return numReserva;
	}
	public void setNumReserva(String numReserva) {
		this.numReserva = numReserva;
	}
	public String getNumSerie() {
		return numSerie;
	}
	public void setNumSerie(String numSerie) {
		this.numSerie = numSerie;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public Double getMonto() {
		return monto;
	}
	public void setMonto(Double monto) {
		this.monto = monto;
	}
	
	
	
	
	
	

}
