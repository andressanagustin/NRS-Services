/**
 * 
 */
package com.allc.entities;

/**
 * @author GUSTAVOK
 * 
 */
public class ItemType {
	private Integer itemTypeID;
	private String code;
	private String name;

	/**
	 * @return the itemTypeID
	 */
	public Integer getItemTypeID() {
		return itemTypeID;
	}

	/**
	 * @param itemTypeID
	 *            the itemTypeID to set
	 */
	public void setItemTypeID(Integer itemTypeID) {
		this.itemTypeID = itemTypeID;
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
}
