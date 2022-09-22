/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * Entidad asociada a un canje de retención por dinero.
 * 
 * @author gustavo
 *
 */
public class RetencionData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	/** 0=Canje de Retencion, 1=Pago de Retencion */
	private Integer indicador;
	private String terminal;
	private String tiquete;
	private String fecha;
	private String voucher;
	private String numeroSRI;
	private Integer monto;
	private String baseImp;
	private Integer porcentaje;
	private String idCliente;
	private String nombre;
	private Integer tipoPag;
	private Integer tipo;
	private String tender;

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
	 * @return the sequenceNumber
	 */
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * @param sequenceNumber
	 *            the sequenceNumber to set
	 */
	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	/**
	 * @return the indicador
	 */
	public Integer getIndicador() {
		return indicador;
	}

	/**
	 * @param indicador
	 *            the indicador to set
	 */
	public void setIndicador(Integer indicador) {
		this.indicador = indicador;
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
	 * @return the tiquete
	 */
	public String getTiquete() {
		return tiquete;
	}

	/**
	 * @param tiquete
	 *            the tiquete to set
	 */
	public void setTiquete(String tiquete) {
		this.tiquete = tiquete;
	}

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
	 * @return the voucher
	 */
	public String getVoucher() {
		return voucher;
	}

	/**
	 * @param voucher
	 *            the voucher to set
	 */
	public void setVoucher(String voucher) {
		this.voucher = voucher;
	}

	/**
	 * @return the numeroSRI
	 */
	public String getNumeroSRI() {
		return numeroSRI;
	}

	/**
	 * @param numeroSRI
	 *            the numeroSRI to set
	 */
	public void setNumeroSRI(String numeroSRI) {
		this.numeroSRI = numeroSRI;
	}

	/**
	 * @return the monto
	 */
	public Integer getMonto() {
		return monto;
	}

	/**
	 * @param monto
	 *            the monto to set
	 */
	public void setMonto(Integer monto) {
		this.monto = monto;
	}

	/**
	 * @return the baseImp
	 */
	public String getBaseImp() {
		return baseImp;
	}

	/**
	 * @param baseImp
	 *            the baseImp to set
	 */
	public void setBaseImp(String baseImp) {
		this.baseImp = baseImp;
	}

	/**
	 * @return the porcentaje
	 */
	public Integer getPorcentaje() {
		return porcentaje;
	}

	/**
	 * @param porcentaje
	 *            the porcentaje to set
	 */
	public void setPorcentaje(Integer porcentaje) {
		this.porcentaje = porcentaje;
	}

	/**
	 * @return the idCliente
	 */
	public String getIdCliente() {
		return idCliente;
	}

	/**
	 * @param idCliente
	 *            the idCliente to set
	 */
	public void setIdCliente(String idCliente) {
		this.idCliente = idCliente;
	}

	/**
	 * @return the nombre
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * @param nombre
	 *            the nombre to set
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * @return the tipoPag
	 */
	public Integer getTipoPag() {
		return tipoPag;
	}

	/**
	 * @param tipoPag the tipoPag to set
	 */
	public void setTipoPag(Integer tipoPag) {
		this.tipoPag = tipoPag;
	}

	/**
	 * @return the tipo
	 */
	public Integer getTipo() {
		return tipo;
	}

	/**
	 * @param tipo
	 *            the tipo to set
	 */
	public void setTipo(Integer tipo) {
		this.tipo = tipo;
	}

	/**
	 * @return the tender
	 */
	public String getTender() {
		return tender;
	}

	/**
	 * @param tender the tender to set
	 */
	public void setTender(String tender) {
		this.tender = tender;
	}

}
