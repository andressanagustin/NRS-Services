/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class MotoData implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Long itemCode;
	private Integer isVoid = new Integer(0);
	private Integer sequenceNumber;
	private String serialNumber;
	private Integer codGerente;
	private String direccion;
	private String ciudad;
	private String telefono;
	

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
	 * @return the itemCode
	 */
	public Long getItemCode() {
		return itemCode;
	}

	/**
	 * @param itemCode the itemCode to set
	 */
	public void setItemCode(Long itemCode) {
		this.itemCode = itemCode;
	}

	/**
	 * @return the isVoid
	 */
	public Integer getIsVoid() {
		return isVoid;
	}

	/**
	 * @param isVoid the isVoid to set
	 */
	public void setIsVoid(Integer isVoid) {
		this.isVoid = isVoid;
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
	 * @return the serialNumber
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * @param serialNumber
	 *            the serialNumber to set
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	/**
	 * @return the codGerente
	 */
	public Integer getCodGerente() {
		return codGerente;
	}

	/**
	 * @param codGerente the codGerente to set
	 */
	public void setCodGerente(Integer codGerente) {
		this.codGerente = codGerente;
	}

	/**
	 * @return the direccion
	 */
	public String getDireccion() {
		return direccion;
	}

	/**
	 * @param direccion the direccion to set
	 */
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	/**
	 * @return the ciudad
	 */
	public String getCiudad() {
		return ciudad;
	}

	/**
	 * @param ciudad the ciudad to set
	 */
	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}

	/**
	 * @return the telefono
	 */
	public String getTelefono() {
		return telefono;
	}

	/**
	 * @param telefono the telefono to set
	 */
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

}
