/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class AssociatedData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private String storeType;
	private String personType;
	private String ctaNumber;
	private String flRedemPoint;
	private String customerID;
	private String nameCustomer;
	private String portadorID;
	private String namePortador;

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

	public String getStoreType() {
		return storeType;
	}

	public void setStoreType(String storeType) {
		this.storeType = storeType;
	}

	public String getPersonType() {
		return personType;
	}

	public void setPersonType(String personType) {
		this.personType = personType;
	}

	public String getCtaNumber() {
		return ctaNumber;
	}

	public void setCtaNumber(String ctaNumber) {
		this.ctaNumber = ctaNumber;
	}

	public String getFlRedemPoint() {
		return flRedemPoint;
	}

	public void setFlRedemPoint(String flRedemPoint) {
		this.flRedemPoint = flRedemPoint;
	}

	public String getCustomerID() {
		return customerID;
	}

	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}

	public String getNameCustomer() {
		return nameCustomer;
	}

	public void setNameCustomer(String nameCustomer) {
		this.nameCustomer = nameCustomer;
	}

	/**
	 * @return the portadorID
	 */
	public String getPortadorID() {
		return portadorID;
	}

	/**
	 * @param portadorID the portadorID to set
	 */
	public void setPortadorID(String portadorID) {
		this.portadorID = portadorID;
	}

	/**
	 * @return the namePortador
	 */
	public String getNamePortador() {
		return namePortador;
	}

	/**
	 * @param namePortador the namePortador to set
	 */
	public void setNamePortador(String namePortador) {
		this.namePortador = namePortador;
	}
	
	

}