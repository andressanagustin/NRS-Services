/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * Entidad que representa a la tabla TR_INVC donde se registra la asociacion de una factura con su cliente a la transacción.
 * 
 * @author gustavo
 *
 */
public class InvoiceAssociated implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private String invoiceNumber;
	/** representa el id de cliente que puede ser Extranjero o CedRuc */
	private Long customerID;
	/** 0=CedRuc 1=Pasaporte */
	private Integer customerType;
	private Extranjero extranjero;
	private CedRuc cedRuc;
	private String subTotal;
	private String total;
	private Integer tax;
	private String nameCustomer;
	private String identificacionCustomer;
	private String authorizationNumber;
	private String ambiente;
	private String emision;
	private Integer codigoSRI;

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
	 * @return the invoiceNumber
	 */
	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	/**
	 * @param invoiceNumber
	 *            the invoiceNumber to set
	 */
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	/**
	 * @return the customerID
	 */
	public Long getCustomerID() {
		if(extranjero!=null)
			customerID = extranjero.getId();
		else if(cedRuc != null)
			customerID = cedRuc.getId();
		return customerID;
	}

	/**
	 * @param customerID
	 *            the customerID to set
	 */
	public void setCustomerID(Long customerID) {
		this.customerID = customerID;
	}

	/**
	 * @return the customerType
	 */
	public Integer getCustomerType() {
		return customerType;
	}

	/**
	 * @param customerType
	 *            the customerType to set
	 */
	public void setCustomerType(Integer customerType) {
		this.customerType = customerType;
	}

	/**
	 * @return the extranjero
	 */
	public Extranjero getExtranjero() {
		return extranjero;
	}

	/**
	 * @param extranjero
	 *            the extranjero to set
	 */
	public void setExtranjero(Extranjero extranjero) {
		this.extranjero = extranjero;
	}

	/**
	 * @return the cedRuc
	 */
	public CedRuc getCedRuc() {
		return cedRuc;
	}

	/**
	 * @param cedRuc
	 *            the cedRuc to set
	 */
	public void setCedRuc(CedRuc cedRuc) {
		this.cedRuc = cedRuc;
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

	public Integer getTax() {
		if(tax == null)
			tax = 0;
		return tax;
	}

	public void setTax(Integer tax) {
		this.tax = tax;
	}

	public String getNameCustomer() {
		return nameCustomer;
	}

	public void setNameCustomer(String nameCustomer) {
		this.nameCustomer = nameCustomer;
	}

	public String getIdentificacionCustomer() {
		return identificacionCustomer;
	}

	public void setIdentificacionCustomer(String identificacionCustomer) {
		this.identificacionCustomer = identificacionCustomer;
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

	public Integer getCodigoSRI() {
		return codigoSRI;
	}

	public void setCodigoSRI(Integer codigoSRI) {
		this.codigoSRI = codigoSRI;
	}

}
