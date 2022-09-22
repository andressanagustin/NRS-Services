/**
 * 
 */
package com.allc.entities;

/**
 * @author GUSTAVOK
 * 
 */
public class TenderRepository {
	protected Integer tenderRepositoryID;
	protected String typeCode;
	protected String name;
	protected String description;
	private RetailStore businessUnit;
	private String code;

	/**
	 * @return the tenderRepositoryID
	 */
	public Integer getTenderRepositoryID() {
		return tenderRepositoryID;
	}

	/**
	 * @param tenderRepositoryID
	 *            the tenderRepositoryID to set
	 */
	public void setTenderRepositoryID(Integer tenderRepositoryID) {
		this.tenderRepositoryID = tenderRepositoryID;
	}

	/**
	 * @return the typeCode
	 */
	public String getTypeCode() {
		return typeCode;
	}

	/**
	 * @param typeCode
	 *            the typeCode to set
	 */
	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
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

	/**
	 * @return the businessUnit
	 */
	public RetailStore getBusinessUnit() {
		return businessUnit;
	}

	/**
	 * @param businessUnit
	 *            the businessUnit to set
	 */
	public void setBusinessUnit(RetailStore businessUnit) {
		this.businessUnit = businessUnit;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

}
