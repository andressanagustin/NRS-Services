package com.allc.entities;

import java.io.Serializable;

public class TicketPromotionData implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private String codTicket;
	private Integer tipoTicket;
	private Integer cantidadTicket;
	private String ordinalNumber;
	
	
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
	public String getCodTicket() {
		return codTicket;
	}
	public void setCodTicket(String codTicket) {
		this.codTicket = codTicket;
	}
	public Integer getTipoTicket() {
		return tipoTicket;
	}
	public void setTipoTicket(Integer tipoTicket) {
		this.tipoTicket = tipoTicket;
	}
	public Integer getCantidadTicket() {
		return cantidadTicket;
	}
	public void setCantidadTicket(Integer cantidadTicket) {
		this.cantidadTicket = cantidadTicket;
	}
	public String getOrdinalNumber() {
		return ordinalNumber;
	}
	public void setOrdinalNumber(String ordinalNumber) {
		this.ordinalNumber = ordinalNumber;
	}
	
	
	
	

}
