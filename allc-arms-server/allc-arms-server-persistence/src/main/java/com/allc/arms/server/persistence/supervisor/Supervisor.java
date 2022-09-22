package com.allc.arms.server.persistence.supervisor;

import java.io.Serializable;
import java.util.Date;

import com.allc.arms.server.persistence.operator.Operator;

public class Supervisor implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer idOperadorSupervisor;
	
	private String clave;
	
	private String clave128;
	
	private Integer idRegistro;
	
	private Integer estado;
	
	private Date fechaInicio;
	
	private Date fechaFin;
	
	private Operator operador;

	public Integer getIdOperadorSupervisor() {
		return idOperadorSupervisor;
	}

	public void setIdOperadorSupervisor(Integer idOperadorSupervisor) {
		this.idOperadorSupervisor = idOperadorSupervisor;
	}



	public Integer getEstado() {
		return estado;
	}

	public void setEstado(Integer estado) {
		this.estado = estado;
	}

	public Date getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(Date fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public Date getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(Date fechaFin) {
		this.fechaFin = fechaFin;
	}

	public Operator getOperador() {
		return operador;
	}

	public void setOperador(Operator operador) {
		this.operador = operador;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getClave128() {
		return clave128;
	}

	public void setClave128(String clave128) {
		this.clave128 = clave128;
	}

	public Integer getIdRegistro() {
		return idRegistro;
	}

	public void setIdRegistro(Integer idRegistro) {
		this.idRegistro = idRegistro;
	}
	
	
	
	

}
