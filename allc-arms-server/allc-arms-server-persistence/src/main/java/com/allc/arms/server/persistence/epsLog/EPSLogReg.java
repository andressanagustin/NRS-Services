/**
 * 
 */
package com.allc.arms.server.persistence.epsLog;

import java.io.Serializable;
import java.util.Date;

/**
 * Entidad que representa un registro en el Log de EPS.
 * 
 * @author gustavo
 *
 */
public class EPSLogReg implements Serializable {

	private static final long serialVersionUID = 1L;
	protected Integer id;
	protected int storeNumber;
	protected Date dateTime;
	protected Date businessDate;
	protected String header;
	protected String data;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the storeNumber
	 */
	public int getStoreNumber() {
		return storeNumber;
	}

	/**
	 * @param storeNumber
	 *            the storeNumber to set
	 */
	public void setStoreNumber(int storeNumber) {
		this.storeNumber = storeNumber;
	}

	/**
	 * @return the dateTime
	 */
	public Date getDateTime() {
		return dateTime;
	}

	/**
	 * @param dateTime
	 *            the dateTime to set
	 */
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	/**
	 * @return the businessDate
	 */
	public Date getBusinessDate() {
		return businessDate;
	}

	/**
	 * @param businessDate
	 *            the businessDate to set
	 */
	public void setBusinessDate(Date businessDate) {
		this.businessDate = businessDate;
	}

	/**
	 * @return the header
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * @param header
	 *            the header to set
	 */
	public void setHeader(String header) {
		this.header = header;
	}

	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(String data) {
		this.data = data;
	}

}
