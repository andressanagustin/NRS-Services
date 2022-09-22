/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * Entidad que representa un registro de un Gasto de Efectivo.
 * 
 * @author gustavo
 *
 */
public class ValeEmpleado implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private String cedula;
	private String codSocSap;
	private String comprobante;
	private String codigo;
	private String cuotas;
	private Integer valor;
	private String observacion;

	/**
	 * @return the transactionID
	 */
	public Integer getTransactionID() {
		return transactionID;
	}

	/**
	 * @param transactionID
	 *            the transactionID to set
	 */
	public void setTransactionID(Integer transactionID) {
		this.transactionID = transactionID;
	}

	/**
	 * @return the cedula
	 */
	public String getCedula() {
		return cedula;
	}

	/**
	 * @param cedula
	 *            the cedula to set
	 */
	public void setCedula(String cedula) {
		this.cedula = cedula;
	}

	/**
	 * @return the comprobante
	 */
	public String getComprobante() {
		return comprobante;
	}

	/**
	 * @param comprobante
	 *            the comprobante to set
	 */
	public void setComprobante(String comprobante) {
		this.comprobante = comprobante;
	}

	/**
	 * @return the codigo
	 */
	public String getCodigo() {
		return codigo;
	}

	/**
	 * @param codigo
	 *            the codigo to set
	 */
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	/**
	 * @return the cuotas
	 */
	public String getCuotas() {
		return cuotas;
	}

	/**
	 * @param cuotas
	 *            the cuotas to set
	 */
	public void setCuotas(String cuotas) {
		this.cuotas = cuotas;
	}

	/**
	 * @return the valor
	 */
	public Integer getValor() {
		return valor;
	}

	/**
	 * @param valor
	 *            the valor to set
	 */
	public void setValor(Integer valor) {
		this.valor = valor;
	}

	/**
	 * @return the observacion
	 */
	public String getObservacion() {
		return observacion;
	}

	/**
	 * @param observacion
	 *            the observacion to set
	 */
	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}

	public String getCodSocSap() {
		return codSocSap;
	}

	public void setCodSocSap(String codSocSap) {
		this.codSocSap = codSocSap;
	}



	

}
