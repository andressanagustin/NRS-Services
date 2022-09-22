package com.allc.entities;

import java.util.Date;

public class BusinessDay {
	private Date businessDayDate;
	private Date beginDateTime;
	private Date endDateTime;

	/**
	 * @return the businessDayDate
	 */
	public Date getBusinessDayDate() {
		return businessDayDate;
	}

	/**
	 * @param businessDayDate
	 *            the businessDayDate to set
	 */
	public void setBusinessDayDate(Date businessDayDate) {
		this.businessDayDate = businessDayDate;
	}

	/**
	 * @return the beginDateTime
	 */
	public Date getBeginDateTime() {
		return beginDateTime;
	}

	/**
	 * @param beginDateTime
	 *            the beginDateTime to set
	 */
	public void setBeginDateTime(Date beginDateTime) {
		this.beginDateTime = beginDateTime;
	}

	/**
	 * @return the endDateTime
	 */
	public Date getEndDateTime() {
		return endDateTime;
	}

	/**
	 * @param endDateTime
	 *            the endDateTime to set
	 */
	public void setEndDateTime(Date endDateTime) {
		this.endDateTime = endDateTime;
	}

}
