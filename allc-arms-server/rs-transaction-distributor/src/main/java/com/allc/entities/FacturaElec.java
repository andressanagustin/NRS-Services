/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author gustavo
 *
 */
public class FacturaElec implements Serializable {

	private static final long serialVersionUID = 1L;
	private String fecha;
	private String hora;
	private String numeroDoc;
	private String tipoDoc;
	private String estado;
	private String numeroFac;
	private String subTotal;
	private String total;

	private String authorizationNumber;
	private String ambiente;
	private String emision;
	/**
	 * @return the fecha
	 */
	public String getFecha() {
		return fecha;
	}

	/**
	 * @param fecha
	 *            the fecha to set
	 */
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	/**
	 * @return the hora
	 */
	public String getHora() {
		return hora;
	}

	/**
	 * @param hora
	 *            the hora to set
	 */
	public void setHora(String hora) {
		this.hora = hora;
	}

	/**
	 * @return the numeroDoc
	 */
	public String getNumeroDoc() {
		return numeroDoc;
	}

	/**
	 * @param numeroDoc
	 *            the numeroDoc to set
	 */
	public void setNumeroDoc(String numeroDoc) {
		this.numeroDoc = numeroDoc;
	}

	/**
	 * @return the tipoDoc
	 */
	public String getTipoDoc() {
		return tipoDoc;
	}

	/**
	 * @param tipoDoc
	 *            the tipoDoc to set
	 */
	public void setTipoDoc(String tipoDoc) {
		this.tipoDoc = tipoDoc;
	}

	/**
	 * @return the estado
	 */
	public String getEstado() {
		return estado;
	}

	/**
	 * @param estado
	 *            the estado to set
	 */
	public void setEstado(String estado) {
		this.estado = estado;
	}

	/**
	 * @return the numeroFac
	 */
	public String getNumeroFac() {
		return numeroFac;
	}

	/**
	 * @param numeroFac
	 *            the numeroFac to set
	 */
	public void setNumeroFac(String numeroFac) {
		this.numeroFac = numeroFac;
	}

	

	public String getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(String subTotal) {
		this.subTotal = subTotal;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}


	public String getAuthorizationNumber() {
		return authorizationNumber;
	}

	public void setAuthorizationNumber(String authorizationNumber) {
		this.authorizationNumber = authorizationNumber;
	}

	public String getAmbiente() {
		return ambiente;
	}

	public void setAmbiente(String ambiente) {
		this.ambiente = ambiente;
	}

	public String getEmision() {
		return emision;
	}

	public void setEmision(String emision) {
		this.emision = emision;
	}
	
	

}
