/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author gustavo
 *
 */
public class POSIdentity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long posIdentityID;
	private Integer itemID;
	private String priority;
	private Integer retailStoreID;
	
	public Long getPosIdentityID() {
		return posIdentityID;
	}
	public void setPosIdentityID(Long posIdentityID) {
		this.posIdentityID = posIdentityID;
	}
	public Integer getItemID() {
		return itemID;
	}
	public void setItemID(Integer itemID) {
		this.itemID = itemID;
	}
	/**
	 * @return the priority
	 */
	public String getPriority() {
		return priority;
	}
	/**
	 * @param priority the priority to set
	 */
	public void setPriority(String priority) {
		this.priority = priority;
	}
	public Integer getRetailStoreID() {
		return retailStoreID;
	}
	public void setRetailStoreID(Integer retailStoreID) {
		this.retailStoreID = retailStoreID;
	}
	
	
}
