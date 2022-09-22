/**
 * 
 */
package com.allc.entities;

/**
 * @author GUSTAVOK
 * 
 */
public class Currency {
	private Integer currencyID;
	private String description;
	private String symbol;
	private String issuingCountryCode;

	/**
	 * @return the currencyID
	 */
	public Integer getCurrencyID() {
		return currencyID;
	}

	/**
	 * @param currencyID
	 *            the currencyID to set
	 */
	public void setCurrencyID(Integer currencyID) {
		this.currencyID = currencyID;
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
	 * @return the symbol
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * @param symbol
	 *            the symbol to set
	 */
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	/**
	 * @return the issuingCountryCode
	 */
	public String getIssuingCountryCode() {
		return issuingCountryCode;
	}

	/**
	 * @param issuingCountryCode
	 *            the issuingCountryCode to set
	 */
	public void setIssuingCountryCode(String issuingCountryCode) {
		this.issuingCountryCode = issuingCountryCode;
	}
}
