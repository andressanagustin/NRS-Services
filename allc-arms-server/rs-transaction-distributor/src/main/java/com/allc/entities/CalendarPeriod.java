/**
 * 
 */
package com.allc.entities;

import java.util.Date;

/**
 * @author GUSTAVOK
 * 
 */
public class CalendarPeriod {
	private Integer calendarID;
	private Integer calendarLevelID;
	private Integer calendarPeriodID;
	private Date startDateTime;
	private Date endDateTime;

	/**
	 * @return the calendarID
	 */
	public Integer getCalendarID() {
		return calendarID;
	}

	/**
	 * @param calendarID
	 *            the calendarID to set
	 */
	public void setCalendarID(Integer calendarID) {
		this.calendarID = calendarID;
	}

	/**
	 * @return the calendarLevelID
	 */
	public Integer getCalendarLevelID() {
		return calendarLevelID;
	}

	/**
	 * @param calendarLevelID
	 *            the calendarLevelID to set
	 */
	public void setCalendarLevelID(Integer calendarLevelID) {
		this.calendarLevelID = calendarLevelID;
	}

	/**
	 * @return the calendarPeriodID
	 */
	public Integer getCalendarPeriodID() {
		return calendarPeriodID;
	}

	/**
	 * @param calendarPeriodID
	 *            the calendarPeriodID to set
	 */
	public void setCalendarPeriodID(Integer calendarPeriodID) {
		this.calendarPeriodID = calendarPeriodID;
	}

	/**
	 * @return the startDateTime
	 */
	public Date getStartDateTime() {
		return startDateTime;
	}

	/**
	 * @param startDateTime
	 *            the startDateTime to set
	 */
	public void setStartDateTime(Date startDateTime) {
		this.startDateTime = startDateTime;
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
