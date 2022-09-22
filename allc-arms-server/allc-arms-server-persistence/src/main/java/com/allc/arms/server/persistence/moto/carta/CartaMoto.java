package com.allc.arms.server.persistence.moto.carta;

import java.io.Serializable;
import java.util.Date;

public class CartaMoto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer idCarta;
	private Integer idTrx;
	private Date fechaCreacion;
	private Integer estado;
	private String serie;
	private Integer gerente;
	private Integer tienda;
	private Date fechaAceptacion;
	private String nombArchXML;
	
	
	public Integer getIdCarta() {
		return idCarta;
	}
	public void setIdCarta(Integer idCarta) {
		this.idCarta = idCarta;
	}
	public Integer getIdTrx() {
		return idTrx;
	}
	public void setIdTrx(Integer idTrx) {
		this.idTrx = idTrx;
	}
	public Date getFechaCreacion() {
		return fechaCreacion;
	}
	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}
	public Integer getEstado() {
		return estado;
	}
	public void setEstado(Integer estado) {
		this.estado = estado;
	}
	public String getSerie() {
		return serie;
	}
	public void setSerie(String serie) {
		this.serie = serie;
	}
	public Integer getGerente() {
		return gerente;
	}
	public void setGerente(Integer gerente) {
		this.gerente = gerente;
	}
	public Integer getTienda() {
		return tienda;
	}
	public void setTienda(Integer tienda) {
		this.tienda = tienda;
	}
	public Date getFechaAceptacion() {
		return fechaAceptacion;
	}
	public void setFechaAceptacion(Date fechaAceptacion) {
		this.fechaAceptacion = fechaAceptacion;
	}
	public String getNombArchXML() {
		return nombArchXML;
	}
	public void setNombArchXML(String nombArchXML) {
		this.nombArchXML = nombArchXML;
	}
	
	
	
}
