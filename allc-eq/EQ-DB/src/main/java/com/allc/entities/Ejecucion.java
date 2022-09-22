/**
 * 
 */
package com.allc.entities;

import java.util.Date;

/**
 * @author gustavo
 *
 */
public class Ejecucion {
	private Long idEjecucion;
	private Proceso proceso;
	private Date execDate;
	private Equipo equipo;
	private TipoEstado tipoEstado;
	private String data;
	private Integer codLocal;

	/**
	 * @return the idEjecucion
	 */
	public Long getIdEjecucion() {
		return idEjecucion;
	}

	/**
	 * @param idEjecucion
	 *            the idEjecucion to set
	 */
	public void setIdEjecucion(Long idEjecucion) {
		this.idEjecucion = idEjecucion;
	}

	/**
	 * @return the execDate
	 */
	public Date getExecDate() {
		return execDate;
	}

	/**
	 * @param execDate
	 *            the execDate to set
	 */
	public void setExecDate(Date execDate) {
		this.execDate = execDate;
	}

	/**
	 * @return the tipoEstado
	 */
	public TipoEstado getTipoEstado() {
		return tipoEstado;
	}

	/**
	 * @param tipoEstado
	 *            the tipoEstado to set
	 */
	public void setTipoEstado(TipoEstado tipoEstado) {
		this.tipoEstado = tipoEstado;
	}

	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(String data) {
		this.data = data;
	}

	/**
	 * @return the proceso
	 */
	public Proceso getProceso() {
		return proceso;
	}

	/**
	 * @param proceso
	 *            the proceso to set
	 */
	public void setProceso(Proceso proceso) {
		this.proceso = proceso;
	}

	/**
	 * @return the equipo
	 */
	public Equipo getEquipo() {
		return equipo;
	}

	/**
	 * @param equipo
	 *            the equipo to set
	 */
	public void setEquipo(Equipo equipo) {
		this.equipo = equipo;
	}

	/**
	 * @return the codLocal
	 */
	public Integer getCodLocal() {
		return codLocal;
	}

	/**
	 * @param codLocal the codLocal to set
	 */
	public void setCodLocal(Integer codLocal) {
		this.codLocal = codLocal;
	}

}
