/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class CouponToRedemption implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String couponCode;
	private String timeStamp;
	private String status;
	private String fechaInicial;
	private String fechaExpiracion;
	private String maxRedemptions;
	private String nRedemptions;
	private String lastRedemption;
	private String percentOff;

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
	 * @return the couponCode
	 */
	public String getCouponCode() {
		return couponCode;
	}

	/**
	 * @param couponCode
	 *            the couponCode to set
	 */
	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	/**
	 * @return the timeStamp
	 */
	public String getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @param timeStamp
	 *            the timeStamp to set
	 */
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the fechaInicial
	 */
	public String getFechaInicial() {
		return fechaInicial;
	}

	/**
	 * @param fechaInicial
	 *            the fechaInicial to set
	 */
	public void setFechaInicial(String fechaInicial) {
		this.fechaInicial = fechaInicial;
	}

	/**
	 * @return the fechaExpiracion
	 */
	public String getFechaExpiracion() {
		return fechaExpiracion;
	}

	/**
	 * @param fechaExpiracion
	 *            the fechaExpiracion to set
	 */
	public void setFechaExpiracion(String fechaExpiracion) {
		this.fechaExpiracion = fechaExpiracion;
	}

	/**
	 * @return the maxRedemptions
	 */
	public String getMaxRedemptions() {
		return maxRedemptions;
	}

	/**
	 * @param maxRedemptions
	 *            the maxRedemptions to set
	 */
	public void setMaxRedemptions(String maxRedemptions) {
		this.maxRedemptions = maxRedemptions;
	}

	/**
	 * @return the nRedemptions
	 */
	public String getnRedemptions() {
		return nRedemptions;
	}

	/**
	 * @param nRedemptions
	 *            the nRedemptions to set
	 */
	public void setnRedemptions(String nRedemptions) {
		this.nRedemptions = nRedemptions;
	}

	/**
	 * @return the lastRedemption
	 */
	public String getLastRedemption() {
		return lastRedemption;
	}

	/**
	 * @param lastRedemption
	 *            the lastRedemption to set
	 */
	public void setLastRedemption(String lastRedemption) {
		this.lastRedemption = lastRedemption;
	}

	/**
	 * @return the percentOff
	 */
	public String getPercentOff() {
		return percentOff;
	}

	/**
	 * @param percentOff
	 *            the percentOff to set
	 */
	public void setPercentOff(String percentOff) {
		this.percentOff = percentOff;
	}

}
