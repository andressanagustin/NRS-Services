package com.allc.entities;

import java.io.Serializable;

/**
 * Entidad que representa un registro de la tabla CO_ILIM_DT.
 * 
 * @author gustavo
 *
 */
public class IlimitadaData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private String time;
	private Integer flVoid;
	private String monto;
	private String refNum;
	private String terminalId;
	private String merchantId;
	private String autNum;
	private String ilimData;

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
	 * @param sequenceNumber the sequenceNumber to set
	 */
	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * @return the flVoid
	 */
	public Integer getFlVoid() {
		return flVoid;
	}

	/**
	 * @param flVoid the flVoid to set
	 */
	public void setFlVoid(Integer flVoid) {
		this.flVoid = flVoid;
	}

	/**
	 * @return the monto
	 */
	public String getMonto() {
		return monto;
	}

	/**
	 * @param monto the monto to set
	 */
	public void setMonto(String monto) {
		this.monto = monto;
	}

	/**
	 * @return the refNum
	 */
	public String getRefNum() {
		return refNum;
	}

	/**
	 * @param refNum the refNum to set
	 */
	public void setRefNum(String refNum) {
		this.refNum = refNum;
	}

	/**
	 * @return the terminalId
	 */
	public String getTerminalId() {
		return terminalId;
	}

	/**
	 * @param terminalId the terminalId to set
	 */
	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	/**
	 * @return the merchantId
	 */
	public String getMerchantId() {
		return merchantId;
	}

	/**
	 * @param merchantId the merchantId to set
	 */
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	/**
	 * @return the autNum
	 */
	public String getAutNum() {
		return autNum;
	}

	/**
	 * @param autNum the autNum to set
	 */
	public void setAutNum(String autNum) {
		this.autNum = autNum;
	}

	/**
	 * @return the ilimData
	 */
	public String getIlimData() {
		return ilimData;
	}

	/**
	 * @param ilimData the ilimData to set
	 */
	public void setIlimData(String ilimData) {
		this.ilimData = ilimData;
	}

}
