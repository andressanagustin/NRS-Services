/**
 * 
 */
package com.allc.entities;

import java.util.Date;


/**
 * @author GUSTAVOK
 * 
 */
public class ReturnTransaction {
	private Integer transactionID;
	private String numeroNotaCredito;
	private String numeroDocOriginal;
	private String numeroFac;
	private Integer nroTiendaOriginal;
	private Integer tipo;
	private Integer tax;
	private Date fechaContOrig;
	private Integer posOrig;
	private String operadorSuite = "0";
	private String supervisor = "0";
	private String motivo;
	private String submotivo;
	
	/**
	 * @return the transactionID
	 */
	public Integer getTransactionID() {
		return transactionID;
	}
	/**
	 * @param transactionID the transactionID to set
	 */
	public void setTransactionID(Integer transactionID) {
		this.transactionID = transactionID;
	}
	/**
	 * @return the numeroNotaCredito
	 */
	public String getNumeroNotaCredito() {
		return numeroNotaCredito;
	}
	/**
	 * @param numeroNotaCredito the numeroNotaCredito to set
	 */
	public void setNumeroNotaCredito(String numeroNotaCredito) {
		this.numeroNotaCredito = numeroNotaCredito;
	}
	/**
	 * @return the numeroDocOriginal
	 */
	public String getNumeroDocOriginal() {
		return numeroDocOriginal;
	}
	/**
	 * @param numeroDocOriginal the numeroDocOriginal to set
	 */
	public void setNumeroDocOriginal(String numeroDocOriginal) {
		this.numeroDocOriginal = numeroDocOriginal;
	}
	/**
	 * @return the numeroFac
	 */
	public String getNumeroFac() {
		return numeroFac;
	}
	/**
	 * @param numeroFac the numeroFac to set
	 */
	public void setNumeroFac(String numeroFac) {
		this.numeroFac = numeroFac;
	}
	/**
	 * @return the nroTiendaOriginal
	 */
	public Integer getNroTiendaOriginal() {
		return nroTiendaOriginal;
	}
	/**
	 * @param nroTiendaOriginal the nroTiendaOriginal to set
	 */
	public void setNroTiendaOriginal(Integer nroTiendaOriginal) {
		this.nroTiendaOriginal = nroTiendaOriginal;
	}
	/**
	 * @return the tipo
	 */
	public Integer getTipo() {
		return tipo;
	}
	/**
	 * @param tipo the tipo to set
	 */
	public void setTipo(Integer tipo) {
		this.tipo = tipo;
	}
	public Integer getTax() {
		if(tax == null)
			tax = 0;
		return tax;
	}
	public void setTax(Integer tax) {
		this.tax = tax;
	}
	/**
	 * @return the fechaContOrig
	 */
	public Date getFechaContOrig() {
		return fechaContOrig;
	}
	/**
	 * @param fechaContOrig the fechaContOrig to set
	 */
	public void setFechaContOrig(Date fechaContOrig) {
		this.fechaContOrig = fechaContOrig;
	}
	/**
	 * @return the posOrig
	 */
	public Integer getPosOrig() {
		return posOrig;
	}
	/**
	 * @param posOrig the posOrig to set
	 */
	public void setPosOrig(Integer posOrig) {
		this.posOrig = posOrig;
	}
	/**
	 * @return the operadorSuite
	 */
	public String getOperadorSuite() {
		return operadorSuite;
	}
	/**
	 * @param operadorSuite the operadorSuite to set
	 */
	public void setOperadorSuite(String operadorSuite) {
		this.operadorSuite = operadorSuite;
	}
	/**
	 * @return the supervisor
	 */
	public String getSupervisor() {
		return supervisor;
	}
	/**
	 * @param supervisor the supervisor to set
	 */
	public void setSupervisor(String supervisor) {
		this.supervisor = supervisor;
	}
	public String getMotivo() {
		return motivo;
	}
	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}
	public String getSubmotivo() {
		return submotivo;
	}
	public void setSubmotivo(String submotivo) {
		this.submotivo = submotivo;
	}
	
	
}
