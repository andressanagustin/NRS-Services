/**
 * 
 */
package com.allc.entities;

/**
 * @author GUSTAVOK
 * 
 */
public class MerchandiseHierarchyGroup {
	private Integer id;
	private String name;
	private String description;
	private String codMRHCer;
	private Integer porcentajeRecargo;
	private POSDepartment posDepartment;
	private Integer code;

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
	 * @return the posDepartment
	 */
	public POSDepartment getPosDepartment() {
		return posDepartment;
	}

	/**
	 * @param posDepartment the posDepartment to set
	 */
	public void setPosDepartment(POSDepartment posDepartment) {
		this.posDepartment = posDepartment;
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public String getCodMRHCer() {
		return codMRHCer;
	}

	public void setCodMRHCer(String codMRHCer) {
		this.codMRHCer = codMRHCer;
	}

	public Integer getPorcentajeRecargo() {
		return porcentajeRecargo;
	}

	public void setPorcentajeRecargo(Integer porcentajeRecargo) {
		this.porcentajeRecargo = porcentajeRecargo;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}


}
