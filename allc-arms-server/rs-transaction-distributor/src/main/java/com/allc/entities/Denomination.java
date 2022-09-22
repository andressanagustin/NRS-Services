/**
 * 
 */
package com.allc.entities;

/**
 * @author GUSTAVOK
 * 
 */
public class Denomination {
	private Integer denominationID;
	private String description;
	private Integer denomination;
	private Integer currency;

	/**
	 * @return the denominationID
	 */
	public Integer getDenominationID() {
		return denominationID;
	}

	/**
	 * @param denominationID
	 *            the denominationID to set
	 */
	public void setDenominationID(Integer denominationID) {
		this.denominationID = denominationID;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the denomination
	 */
	public Integer getDenomination() {
		return denomination;
	}

	/**
	 * @param denomination
	 *            the denomination to set
	 */
	public void setDenomination(Integer denomination) {
		this.denomination = denomination;
	}

	/**
	 * @return the currency
	 */
	public Integer getCurrency() {
		return currency;
	}

	/**
	 * @param currency
	 *            the currency to set
	 */
	public void setCurrency(Integer currency) {
		this.currency = currency;
	}
}
