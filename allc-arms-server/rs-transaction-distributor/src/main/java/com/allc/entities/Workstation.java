/**
 * 
 */
package com.allc.entities;

/**
 * @author GUSTAVOK
 *
 */
public class Workstation {
	private Integer workstationID;
	private String code;
	private String name;
	private String manufacturerName;
	private String modelNumber;
	private Integer type;
	private RetailStore store;
	/**
	 * @return the workstationID
	 */
	public Integer getWorkstationID() {
		return workstationID;
	}
	/**
	 * @param workstationID the workstationID to set
	 */
	public void setWorkstationID(Integer workstationID) {
		this.workstationID = workstationID;
	}
	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the manufacturerName
	 */
	public String getManufacturerName() {
		return manufacturerName;
	}
	/**
	 * @param manufacturerName the manufacturerName to set
	 */
	public void setManufacturerName(String manufacturerName) {
		this.manufacturerName = manufacturerName;
	}
	/**
	 * @return the modelNumber
	 */
	public String getModelNumber() {
		return modelNumber;
	}
	/**
	 * @param modelNumber the modelNumber to set
	 */
	public void setModelNumber(String modelNumber) {
		this.modelNumber = modelNumber;
	}
	public RetailStore getStore() {
		return store;
	}
	public void setStore(RetailStore store) {
		this.store = store;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	
}
