/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author gustavo
 *
 */
public class RecargaElec implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private String codigoArt;
	private String tienda;
	private String terminal;
	private String referencia;
	private String tipo;
	private String numero;
	private String monto;
	private String autorizacion;
	private String cancelacion;
	private String horaTrx;
	
	

	/**
	 * @return the codigoArt
	 */
	public String getCodigoArt() {
		return codigoArt;
	}

	public Integer getTransactionID() {
		return transactionID;
	}

	public void setTransactionID(Integer transactionID) {
		this.transactionID = transactionID;
	}

	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	/**
	 * @param codigoArt
	 *            the codigoArt to set
	 */
	public void setCodigoArt(String codigoArt) {
		this.codigoArt = codigoArt;
	}

	/**
	 * @return the tienda
	 */
	public String getTienda() {
		return tienda;
	}

	/**
	 * @param tienda
	 *            the tienda to set
	 */
	public void setTienda(String tienda) {
		this.tienda = tienda;
	}

	/**
	 * @return the terminal
	 */
	public String getTerminal() {
		return terminal;
	}

	/**
	 * @param terminal
	 *            the terminal to set
	 */
	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}

	/**
	 * @return the referencia
	 */
	public String getReferencia() {
		return referencia;
	}

	/**
	 * @param referencia
	 *            the referencia to set
	 */
	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

	/**
	 * @return the tipo
	 */
	public String getTipo() {
		return tipo;
	}

	/**
	 * @param tipo
	 *            the tipo to set
	 */
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	/**
	 * @return the numero
	 */
	public String getNumero() {
		return numero;
	}

	/**
	 * @param numero
	 *            the numero to set
	 */
	public void setNumero(String numero) {
		this.numero = numero;
	}

	/**
	 * @return the monto
	 */
	public String getMonto() {
		return monto;
	}

	/**
	 * @param monto
	 *            the monto to set
	 */
	public void setMonto(String monto) {
		this.monto = monto;
	}

	public String getAutorizacion() {
		return autorizacion;
	}

	public void setAutorizacion(String autorizacion) {
		this.autorizacion = autorizacion;
	}

	public String getCancelacion() {
		return cancelacion;
	}

	public void setCancelacion(String cancelacion) {
		this.cancelacion = cancelacion;
	}

	public String getHoraTrx() {
		return horaTrx;
	}

	public void setHoraTrx(String horaTrx) {
		this.horaTrx = horaTrx;
	}



}
