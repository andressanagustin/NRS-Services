/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;
import java.util.Date;

/**
 * @author GUSTAVOK
 * 
 */
public class RetailStoreStatus implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer retailStoreID;
	private Date businessDayDate;
	private Integer period;
	/**
	 * @return the retailStoreID
	 */
	public Integer getRetailStoreID() {
		return retailStoreID;
	}
	/**
	 * @param retailStoreID the retailStoreID to set
	 */
	public void setRetailStoreID(Integer retailStoreID) {
		this.retailStoreID = retailStoreID;
	}
	/**
	 * @return the businessDayDate
	 */
	public Date getBusinessDayDate() {
		return businessDayDate;
	}
	/**
	 * @param businessDayDate the businessDayDate to set
	 */
	public void setBusinessDayDate(Date businessDayDate) {
		this.businessDayDate = businessDayDate;
	}
	/**
	 * @return the period
	 */
	public Integer getPeriod() {
		return period;
	}
	/**
	 * @param period the period to set
	 */
	public void setPeriod(Integer period) {
		this.period = period;
	}
	
	
}
