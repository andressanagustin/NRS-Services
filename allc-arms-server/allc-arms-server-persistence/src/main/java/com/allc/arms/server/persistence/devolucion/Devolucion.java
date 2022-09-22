/**
 * 
 */
package com.allc.arms.server.persistence.devolucion;

import java.io.Serializable;

/**
 * Entidad que representa un registr de la tabla DV_TICKET.
 * 
 * @author gustavo
 *
 */
public class Devolucion implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long idDevs;
	private Long idTrx;
	private Integer idTipoDevs;
	private Integer idEstado;
	private Long idStore;

	/**
	 * 
	 */
	public Devolucion() {
		// TODO Auto-generated constructor stub
	}

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
	 * @return the idTrx
	 */
	public Long getIdTrx() {
		return idTrx;
	}

	/**
	 * @param idTrx
	 *            the idTrx to set
	 */
	public void setIdTrx(Long idTrx) {
		this.idTrx = idTrx;
	}

	/**
	 * @return the idTipoDevs
	 */
	public Integer getIdTipoDevs() {
		return idTipoDevs;
	}

	/**
	 * @param idTipoDevs
	 *            the idTipoDevs to set
	 */
	public void setIdTipoDevs(Integer idTipoDevs) {
		this.idTipoDevs = idTipoDevs;
	}

	/**
	 * @return the idEstado
	 */
	public Integer getIdEstado() {
		return idEstado;
	}

	/**
	 * @param idEstado
	 *            the idEstado to set
	 */
	public void setIdEstado(Integer idEstado) {
		this.idEstado = idEstado;
	}

	/**
	 * @return the idStore
	 */
	public Long getIdStore() {
		return idStore;
	}

	/**
	 * @param idStore
	 *            the idStore to set
	 */
	public void setIdStore(Long idStore) {
		this.idStore = idStore;
	}

}
