/**
 * 
 */
package com.allc.arms.server.persistence.devolucion;

import java.io.Serializable;

/**
 * Entidad que representa un registro de la tabla DV_FACT.
 * 
 * @author gustavo
 *
 */
public class DevFactura implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long idDevs;
	private Integer estado;
	private Long monto;

	/**
	 * @return the idDevs
	 */
	public Long getIdDevs() {
		return idDevs;
	}

	/**
	 * @param idDevs
	 *            the idDevs to set
	 */
	public void setIdDevs(Long idDevs) {
		this.idDevs = idDevs;
	}

	/**
	 * @return the estado
	 */
	public Integer getEstado() {
		return estado;
	}

	/**
	 * @param estado
	 *            the estado to set
	 */
	public void setEstado(Integer estado) {
		this.estado = estado;
	}

	/**
	 * @return the monto
	 */
	public Long getMonto() {
		return monto;
	}

	/**
	 * @param monto
	 *            the monto to set
	 */
	public void setMonto(Long monto) {
		this.monto = monto;
	}

}
