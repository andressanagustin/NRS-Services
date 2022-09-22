package com.allc.arms.server.persistence.sociedad;

import java.io.Serializable;

public class Sociedad implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer idSociedad;
	private String claveContabilidad;
	
	
	public Sociedad() {
	
	}


	public Integer getIdSociedad() {
		return idSociedad;
	}


	public void setIdSociedad(Integer idSociedad) {
		this.idSociedad = idSociedad;
	}


	public String getClaveContabilidad() {
		return claveContabilidad;
	}


	public void setClaveContabilidad(String claveContabilidad) {
		this.claveContabilidad = claveContabilidad;
	}


	
	
	
	

}
