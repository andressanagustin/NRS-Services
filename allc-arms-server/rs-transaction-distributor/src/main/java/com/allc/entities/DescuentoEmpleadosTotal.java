package com.allc.entities;

import java.io.Serializable;

public class DescuentoEmpleadosTotal implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private String trxNum;
	private Double montoTotalDescuento = new Double(0);
	
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
	public Double getMontoTotalDescuento() {
		return montoTotalDescuento;
	}
	public void setMontoTotalDescuento(Double montoTotalDescuento) {
		this.montoTotalDescuento = montoTotalDescuento;
	}

	
	

}
