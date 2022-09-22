/**
 * 
 */
package com.allc.entities;

/**
 * @author GUSTAVOK
 * 
 */
public class POSDepartment {
	private Integer id;
	private String name;
	private String codNegocio;
	private String codDptoCer;
	private Integer porcentajeRecargo;
	private Integer porcentajeDscEmp;
	private Integer porcentajeBonSol;
	private Integer qtyFlias = new Integer(0);

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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the codNegocio
	 */
	public String getCodNegocio() {
		return codNegocio;
	}

	/**
	 * @param codNegocio
	 *            the codNegocio to set
	 */
	public void setCodNegocio(String codNegocio) {
		this.codNegocio = codNegocio;
	}

	public String getCodDptoCer() {
		return codDptoCer;
	}

	public void setCodDptoCer(String codDptoCer) {
		this.codDptoCer = codDptoCer;
	}

	/**
	 * @return the porcentajeRecargo
	 */
	public Integer getPorcentajeRecargo() {
		return porcentajeRecargo;
	}

	/**
	 * @param porcentajeRecargo the porcentajeRecargo to set
	 */
	public void setPorcentajeRecargo(Integer porcentajeRecargo) {
		this.porcentajeRecargo = porcentajeRecargo;
	}

	public Integer getPorcentajeDscEmp() {
		return porcentajeDscEmp;
	}

	public void setPorcentajeDscEmp(Integer porcentajeDscEmp) {
		this.porcentajeDscEmp = porcentajeDscEmp;
	}

	public Integer getPorcentajeBonSol() {
		return porcentajeBonSol;
	}

	public void setPorcentajeBonSol(Integer porcentajeBonSol) {
		this.porcentajeBonSol = porcentajeBonSol;
	}

	public Integer getQtyFlias() {
		return qtyFlias;
	}

	public void setQtyFlias(Integer qtyFlias) {
		this.qtyFlias = qtyFlias;
	}

	
}
