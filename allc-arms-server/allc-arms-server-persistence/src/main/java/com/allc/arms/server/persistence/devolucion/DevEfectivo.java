/**
 * 
 */
package com.allc.arms.server.persistence.devolucion;

import java.io.Serializable;

/**
 * Entidad que representa un registro de la tabla DV_EFEC.
 * 
 * @author gustavo
 *
 */
public class DevEfectivo implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long idDevs;
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
