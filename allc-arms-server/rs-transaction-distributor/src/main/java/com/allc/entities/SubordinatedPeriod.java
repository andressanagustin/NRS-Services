/**
 * 
 */
package com.allc.entities;

/**
 * @author GUSTAVOK
 * 
 */
public class SubordinatedPeriod {
	private Integer calendarID;
	private Integer calendarLevelID;
	private Integer periodNumber;

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
	 * @return the periodNumber
	 */
	public Integer getPeriodNumber() {
		return periodNumber;
	}

	/**
	 * @param periodNumber
	 *            the periodNumber to set
	 */
	public void setPeriodNumber(Integer periodNumber) {
		this.periodNumber = periodNumber;
	}
}
