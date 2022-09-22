/**
 * 
 */
package com.allc.entities;

/**
 * @author gustavo
 *
 */
public class TipoEstado {
	private Long idTipoEstado;
	private String abreviatura;
	private String descripcion;

	/**
	 * @return the idTipoEstado
	 */
	public Long getIdTipoEstado() {
		return idTipoEstado;
	}

	/**
	 * @param idTipoEstado the idTipoEstado to set
	 */
	public void setIdTipoEstado(Long idTipoEstado) {
		this.idTipoEstado = idTipoEstado;
	}

	/**
	 * @return the abreviatura
	 */
	public String getAbreviatura() {
		return abreviatura;
	}

	/**
	 * @param abreviatura the abreviatura to set
	 */
	public void setAbreviatura(String abreviatura) {
		this.abreviatura = abreviatura;
	}

	/**
	 * @return the descripcion
	 */
	public String getDescripcion() {
		return descripcion;
	}

	/**
	 * @param descripcion
	 *            the descripcion to set
	 */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

}
