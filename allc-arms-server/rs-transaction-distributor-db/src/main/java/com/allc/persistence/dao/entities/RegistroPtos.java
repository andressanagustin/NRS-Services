package com.allc.persistence.dao.entities;

import java.io.Serializable;
import java.util.Date;

public class RegistroPtos implements Serializable {

	private static final long serialVersionUID = -2230427189192952655L;

	private String customerId;
	private String tipoTrans;
	private Date fechaTrans;
	private String horaTrans;
	private Integer numTerminal;
	private Integer nroTrans;
	private Integer operador;
	private Integer sumPtos;
	private Integer resPtos;
	private Integer codNegocio;
	private Integer codTienda;
	private Integer idReg;
	private Date fecha;

	public RegistroPtos() {
		super();
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getTipoTrans() {
		return tipoTrans;
	}

	public void setTipoTrans(String tipoTrans) {
		this.tipoTrans = tipoTrans;
	}

	public Date getFechaTrans() {
		return fechaTrans;
	}

	public void setFechaTrans(Date fechaTrans) {
		this.fechaTrans = fechaTrans;
	}

	public String getHoraTrans() {
		return horaTrans;
	}

	public void setHoraTrans(String horaTrans) {
		this.horaTrans = horaTrans;
	}

	public Integer getNumTerminal() {
		return numTerminal;
	}

	public void setNumTerminal(Integer numTerminal) {
		this.numTerminal = numTerminal;
	}

	public Integer getNroTrans() {
		return nroTrans;
	}

	public void setNroTrans(Integer nroTrans) {
		this.nroTrans = nroTrans;
	}

	public Integer getOperador() {
		return operador;
	}

	public void setOperador(Integer operador) {
		this.operador = operador;
	}

	public Integer getSumPtos() {
		return sumPtos;
	}

	public void setSumPtos(Integer sumPtos) {
		this.sumPtos = sumPtos;
	}

	public Integer getResPtos() {
		return resPtos;
	}

	public void setResPtos(Integer resPtos) {
		this.resPtos = resPtos;
	}

	public Integer getCodNegocio() {
		return codNegocio;
	}

	public void setCodNegocio(Integer codNegocio) {
		this.codNegocio = codNegocio;
	}

	public Integer getCodTienda() {
		return codTienda;
	}

	public void setCodTienda(Integer codTienda) {
		this.codTienda = codTienda;
	}

	public Integer getIdReg() {
		return idReg;
	}

	public void setIdReg(Integer idReg) {
		this.idReg = idReg;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	
}
