package com.allc.entities;

import java.io.Serializable;
import java.util.Date;

public class Reserva implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer idReserva;
	private Integer itemID;
	private Integer retailStoreID;
	private Long codReserva;
	private Date fechaDesde;
	private Date fechaHasta;
	private String numSerie;
	/** Flag que indica si la reserva fue utilizada*/
	private Integer rsvUsada = new Integer(0);
	
	
	
	
	public Integer getItemID() {
		return itemID;
	}
	public void setItemID(Integer itemID) {
		this.itemID = itemID;
	}
	public Integer getRetailStoreID() {
		return retailStoreID;
	}
	public void setRetailStoreID(Integer retailStoreID) {
		this.retailStoreID = retailStoreID;
	}
	public Integer getIdReserva() {
		return idReserva;
	}
	public void setIdReserva(Integer idReserva) {
		this.idReserva = idReserva;
	}
	public Long getCodReserva() {
		return codReserva;
	}
	public void setCodReserva(Long codReserva) {
		this.codReserva = codReserva;
	}
	public Date getFechaDesde() {
		return fechaDesde;
	}
	public void setFechaDesde(Date fechaDesde) {
		this.fechaDesde = fechaDesde;
	}
	public Date getFechaHasta() {
		return fechaHasta;
	}
	public void setFechaHasta(Date fechaHasta) {
		this.fechaHasta = fechaHasta;
	}
	public String getNumSerie() {
		return numSerie;
	}
	public void setNumSerie(String numSerie) {
		this.numSerie = numSerie;
	}
	public Integer getRsvUsada() {
		return rsvUsada;
	}
	public void setRsvUsada(Integer rsvUsada) {
		this.rsvUsada = rsvUsada;
	}
	
	
	

}
