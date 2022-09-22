/**
 * 
 */
package com.allc.arms.server.params;

import java.math.BigInteger;

/**
 * Entidad que representa a un registro de la tabla PM_PARAM.
 * 
 * @author gustavo
 *
 */
public class Param {

	protected Long codigo;
	protected String descripcion;
	protected String clave;
	protected Integer ambito;

	/**
	 * @return the codigo
	 */
	public Long getCodigo() {
		return codigo;
	}

	/**
	 * @param codigo
	 *            the codigo to set
	 */
	public void setCodigo(Long codigo) {
		this.codigo = codigo;
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

	/**
	 * @return the clave
	 */
	public String getClave() {
		return clave;
	}

	/**
	 * @param clave
	 *            the clave to set
	 */
	public void setClave(String clave) {
		this.clave = clave;
	}

	/**
	 * @return the ambito
	 */
	public Integer getAmbito() {
		return ambito;
	}

	/**
	 * @param ambito
	 *            the ambito to set
	 */
	public void setAmbito(Integer ambito) {
		this.ambito = ambito;
	}

}
