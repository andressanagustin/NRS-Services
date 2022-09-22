/**
 * 
 */
package com.allc.entities;

/**
 * Entidad que se relaciona con la tabla MN_TIENDA.
 * 
 * @author Francisco
 *
 */
public class Tienda {
	private Integer codTienda;
	private String desClave;
	private String ip;
	/**
	 * @return the idEquipo
	 */
	public Integer getCodTienda() {
		return codTienda;
	}
	/**
	 * @param idEquipo the idEquipo to set
	 */
	public void setCodTienda(Integer codTienda) {
		this.codTienda = codTienda;
	}
	/**
	 * @return the desClave
	 */
	public String getDesClave() {
		return desClave;
	}
	/**
	 * @param desClave the desClave to set
	 */
	public void setDesClave(String desClave) {
		this.desClave = desClave;
	}
	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}
	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}	
}
