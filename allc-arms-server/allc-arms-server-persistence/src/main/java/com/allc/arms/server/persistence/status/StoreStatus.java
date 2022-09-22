/**
 * 
 */
package com.allc.arms.server.persistence.status;

import java.io.Serializable;
import java.util.Date;

/**
 * Entidad que representa un registro de tipo StoreRecord dentro de archivo EAMTERMS.
 * 
 * @author gustavo
 *
 */
public class StoreStatus implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer strStsID;
	private Integer terminal;
	private String sLogName;
	private Integer numClose;
	private Integer closeFlg;
	private Date dateTime;
	private Integer monitor;
	private Integer closeControlFlg;
	private Integer indicat0;
	private Integer indicat1;
	private String reserved;
	private Integer storeCode;
	/**
	 * @return the strStsID
	 */
	public Integer getStrStsID() {
		return strStsID;
	}
	/**
	 * @param strStsID the strStsID to set
	 */
	public void setStrStsID(Integer strStsID) {
		this.strStsID = strStsID;
	}
	/**
	 * @return the terminal
	 */
	public Integer getTerminal() {
		return terminal;
	}
	/**
	 * @param terminal the terminal to set
	 */
	public void setTerminal(Integer terminal) {
		this.terminal = terminal;
	}
	/**
	 * @return the sLogName
	 */
	public String getsLogName() {
		return sLogName;
	}
	/**
	 * @param sLogName the sLogName to set
	 */
	public void setsLogName(String sLogName) {
		this.sLogName = sLogName;
	}
	/**
	 * @return the numClose
	 */
	public Integer getNumClose() {
		return numClose;
	}
	/**
	 * @param numClose the numClose to set
	 */
	public void setNumClose(Integer numClose) {
		this.numClose = numClose;
	}
	/**
	 * @return the closeFlg
	 */
	public Integer getCloseFlg() {
		return closeFlg;
	}
	/**
	 * @param closeFlg the closeFlg to set
	 */
	public void setCloseFlg(Integer closeFlg) {
		this.closeFlg = closeFlg;
	}
	/**
	 * @return the dateTime
	 */
	public Date getDateTime() {
		return dateTime;
	}
	/**
	 * @param dateTime the dateTime to set
	 */
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
	/**
	 * @return the monitor
	 */
	public Integer getMonitor() {
		return monitor;
	}
	/**
	 * @param monitor the monitor to set
	 */
	public void setMonitor(Integer monitor) {
		this.monitor = monitor;
	}
	/**
	 * @return the closeControlFlg
	 */
	public Integer getCloseControlFlg() {
		return closeControlFlg;
	}
	/**
	 * @param closeControlFlg the closeControlFlg to set
	 */
	public void setCloseControlFlg(Integer closeControlFlg) {
		this.closeControlFlg = closeControlFlg;
	}
	/**
	 * @return the indicat0
	 */
	public Integer getIndicat0() {
		return indicat0;
	}
	/**
	 * @param indicat0 the indicat0 to set
	 */
	public void setIndicat0(Integer indicat0) {
		this.indicat0 = indicat0;
	}
	/**
	 * @return the indicat1
	 */
	public Integer getIndicat1() {
		return indicat1;
	}
	/**
	 * @param indicat1 the indicat1 to set
	 */
	public void setIndicat1(Integer indicat1) {
		this.indicat1 = indicat1;
	}
	/**
	 * @return the reserved
	 */
	public String getReserved() {
		return reserved;
	}
	/**
	 * @param reserved the reserved to set
	 */
	public void setReserved(String reserved) {
		this.reserved = reserved;
	}
	public Integer getStoreCode() {
		return storeCode;
	}
	public void setStoreCode(Integer storeCode) {
		this.storeCode = storeCode;
	}
	
	
}
