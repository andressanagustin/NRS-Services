/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class EcommerceData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer orden;
	private String factura;
	private String hora;
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

	public Integer getOrden() {
		return orden;
	}

	public void setOrden(Integer orden) {
		this.orden = orden;
	}

	public String getFactura() {
		return factura;
	}

	public void setFactura(String factura) {
		this.factura = factura;
	}

	public String getHora() {
		return hora;
	}

	public void setHora(String hora) {
		this.hora = hora;
	}

	
	
}
