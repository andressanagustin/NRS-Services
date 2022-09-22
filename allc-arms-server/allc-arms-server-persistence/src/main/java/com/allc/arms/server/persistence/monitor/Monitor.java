package com.allc.arms.server.persistence.monitor;

import java.io.Serializable;
import java.util.Date;

public class Monitor implements Serializable{
	private static final long serialVersionUID = 1L;
	private Integer id_local;
	private String des_clave;
	private Date fec_actualizacion;
	private Integer cantidad;
    private Float monto;
    private Integer cantidad_bd;
    private Float monto_bd;
    private Integer idTrn;
    private Date fechaIdTrn;
    private Date fechaEtl;
    private Date fechaR;
    private Date fechaPromociones;
    private Date fechaControlador;
    
    public Monitor() {
		// TODO Auto-generated constructor stub
	}

	public Integer getId_local() {
		return id_local;
	}

	public void setId_local(Integer id_local) {
		this.id_local = id_local;
	}

	public String getDes_clave() {
		return des_clave;
	}

	public void setDes_clave(String des_clave) {
		this.des_clave = des_clave;
	}

	public Date getFec_actualizacion() {
		return fec_actualizacion;
	}

	public void setFec_actualizacion(Date fec_actualizacion) {
		this.fec_actualizacion = fec_actualizacion;
	}

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public Float getMonto() {
		return monto;
	}

	public void setMonto(Float monto) {
		this.monto = monto;
	}

	public Integer getCantidad_bd() {
		return cantidad_bd;
	}

	public void setCantidad_bd(Integer cantidad_bd) {
		this.cantidad_bd = cantidad_bd;
	}

	public Float getMonto_bd() {
		return monto_bd;
	}

	public void setMonto_bd(Float monto_bd) {
		this.monto_bd = monto_bd;
	}

	public Integer getIdTrn() {
		return idTrn;
	}

	public void setIdTrn(Integer id_trn) {
		this.idTrn = id_trn;
	}

	public Date getFechaIdTrn() {
		return fechaIdTrn;
	}

	public void setFechaIdTrn(Date fechaIdTrn) {
		this.fechaIdTrn = fechaIdTrn;
	}

	public Date getFechaEtl() {
		return fechaEtl;
	}

	public void setFechaEtl(Date fechaEtl) {
		this.fechaEtl = fechaEtl;
	}

	public Date getFechaR() {
		return fechaR;
	}

	public void setFechaR(Date fechaR) {
		this.fechaR = fechaR;
	}

	public Date getFechaPromociones() {
		return fechaPromociones;
	}

	public void setFechaPromociones(Date fechaPromociones) {
		this.fechaPromociones = fechaPromociones;
	}

	public Date getFechaControlador() {
		return fechaControlador;
	}

	public void setFechaControlador(Date fechaControlador) {
		this.fechaControlador = fechaControlador;
	}
    
    
}
