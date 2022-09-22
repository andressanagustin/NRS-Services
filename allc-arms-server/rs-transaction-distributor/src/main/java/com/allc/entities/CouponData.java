package com.allc.entities;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

import com.allc.util.ConstantsUtil;

public class CouponData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private String promoID;
	private String customerID;
	private String formatID;
	private Integer impreso = new Integer(0);
	private String itemcode;
	private String barcode;
	private Double valueDisc= new Double(0);
	private String apply;
	private Date initDate;
	private String initDateString;
	private Date expDate;
	private String expDateString;
	private Integer separator = new Integer(0);
	private String logoID;
	private Integer estado = new Integer(0);

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
	 * @return the promoID
	 */
	public String getPromoID() {
		return promoID;
	}

	/**
	 * @param promoID
	 *            the promoID to set
	 */
	public void setPromoID(String promoID) {
		this.promoID = promoID;
	}

	/**
	 * @return the customerID
	 */
	public String getCustomerID() {
		return customerID;
	}

	/**
	 * @param customerID
	 *            the customerID to set
	 */
	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}

	/**
	 * @return the formatID
	 */
	public String getFormatID() {
		return formatID;
	}

	/**
	 * @param formatID
	 *            the formatID to set
	 */
	public void setFormatID(String formatID) {
		this.formatID = formatID;
	}

	/**
	 * @return the impreso
	 */
	public Integer getImpreso() {
		return impreso;
	}

	/**
	 * @param impreso
	 *            the impreso to set
	 */
	public void setImpreso(Integer impreso) {
		this.impreso = impreso;
	}

	/**
	 * @return the itemcode
	 */
	public String getItemcode() {
		return itemcode;
	}

	/**
	 * @param itemcode
	 *            the itemcode to set
	 */
	public void setItemcode(String itemcode) {
		this.itemcode = itemcode;
	}

	/**
	 * @return the barcode
	 */
	public String getBarcode() {
		return barcode;
	}

	/**
	 * @param barcode
	 *            the barcode to set
	 */
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	/**
	 * @return the valueDisc
	 */
	public Double getValueDisc() {
		return valueDisc;
	}

	/**
	 * @param valueDisc
	 *            the valueDisc to set
	 */
	public void setValueDisc(Double valueDisc) {
		this.valueDisc = valueDisc;
	}

	/**
	 * @return the apply
	 */
	public String getApply() {
		return apply;
	}

	/**
	 * @param apply
	 *            the apply to set
	 */
	public void setApply(String apply) {
		this.apply = apply;
	}

	/**
	 * @return the initDate
	 * @throws ParseException
	 */
	public Date getInitDate() throws ParseException {
		if (initDate == null && initDateString != null) {
			try {
				initDate = ConstantsUtil.Formatters.DDMMYYYY_SUFIX_DATE_TIME_FORMATTER
						.parse(initDateString);
			} catch (ParseException e) {
				throw e;
			}
		}
		return initDate;
	}

	/**
	 * @param initDate
	 *            the initDate to set
	 */
	public void setInitDate(Date initDate) {
		this.initDate = initDate;
	}

	/**
	 * @return the initDateString
	 */
	public String getInitDateString() {
		return initDateString;
	}

	/**
	 * @param initDateString
	 *            the initDateString to set
	 */
	public void setInitDateString(String initDateString) {
		this.initDateString = initDateString;
	}

	/**
	 * @return the expDate
	 * @throws ParseException
	 */
	public Date getExpDate() throws ParseException {
		if (expDate == null && expDateString != null) {
			try {
				expDate = ConstantsUtil.Formatters.DDMMYYYY_SUFIX_DATE_TIME_FORMATTER
						.parse(expDateString);
			} catch (ParseException e) {
				throw e;
			}
		}
		return expDate;
	}

	/**
	 * @param expDate
	 *            the expDate to set
	 */
	public void setExpDate(Date expDate) {
		this.expDate = expDate;
	}

	/**
	 * @return the expDateString
	 */
	public String getExpDateString() {
		return expDateString;
	}

	/**
	 * @param expDateString
	 *            the expDateString to set
	 */
	public void setExpDateString(String expDateString) {
		this.expDateString = expDateString;
	}

	/**
	 * @return the separator
	 */
	public Integer getSeparator() {
		return separator;
	}

	/**
	 * @param separator
	 *            the separator to set
	 */
	public void setSeparator(Integer separator) {
		this.separator = separator;
	}

	/**
	 * @return the logoID
	 */
	public String getLogoID() {
		return logoID;
	}

	/**
	 * @param logoID
	 *            the logoID to set
	 */
	public void setLogoID(String logoID) {
		this.logoID = logoID;
	}

	/**
	 * @return the estado
	 */
	public Integer getEstado() {
		return estado;
	}

	/**
	 * @param estado
	 *            the estado to set
	 */
	public void setEstado(Integer estado) {
		this.estado = estado;
	}

}
