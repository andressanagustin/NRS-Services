/**
 * 
 */
package com.allc.entities;

import java.util.Iterator;
import java.util.List;

/**
 * @author GUSTAVOK
 * 
 */
public class Item {
	private Integer itemID;
	private Long itemCode;
	private Integer itemType;
	/** Flag que indica si el item es pesable o medible */
	private Integer flWorM = new Integer(0);
	/** Flag que indica si el item requiere precio */
	private Integer flPriceReq = new Integer(0);
	/** Flag que indica si el item permite el ingreso de cantidad */
	private Integer flQuantityAllw = new Integer(0);
	/** Flag que indica si el item requiere el ingreso de cantidad */
	private Integer flQuantityReq = new Integer(0);
	/** Flag que indica si se pueden aplicar descuentos al item */
	private Integer flDscItm = new Integer(0);
	private String name;
	private String description;
	private String referenciaSAP;
	private Long codigoSAP;
	private String jerarquia;
	private List itemsStore;
	private MerchandiseHierarchyGroup merchandiseHierarchyGroup;

	/**
	 * @return the itemID
	 */
	public Integer getItemID() {
		return itemID;
	}

	/**
	 * @param itemID
	 *            the itemID to set
	 */
	public void setItemID(Integer itemID) {
		this.itemID = itemID;
	}

	/**
	 * @return the itemCode
	 */
	public Long getItemCode() {
		return itemCode;
	}

	/**
	 * @param itemCode
	 *            the itemCode to set
	 */
	public void setItemCode(Long itemCode) {
		this.itemCode = itemCode;
	}
	

	/**
	 * @return the itemType
	 */
	public Integer getItemType() {
		return itemType;
	}

	/**
	 * @param itemType the itemType to set
	 */
	public void setItemType(Integer itemType) {
		this.itemType = itemType;
	}

	public Integer getFlWorM() {
		return flWorM;
	}

	public void setFlWorM(Integer flWorM) {
		this.flWorM = flWorM;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List getItemsStore() {
		if (itemsStore != null) {
			Iterator itLineItems = itemsStore.iterator();
			while (itLineItems.hasNext())
				((ItemStore) itLineItems.next()).setItemID(itemID);
		}
		return itemsStore;
	}

	public void setItemsStore(List itemsStore) {
		this.itemsStore = itemsStore;
	}

	/**
	 * @return the merchandiseHierarchyGroup
	 */
	public MerchandiseHierarchyGroup getMerchandiseHierarchyGroup() {
		return merchandiseHierarchyGroup;
	}

	/**
	 * @param merchandiseHierarchyGroup
	 *            the merchandiseHierarchyGroup to set
	 */
	public void setMerchandiseHierarchyGroup(MerchandiseHierarchyGroup merchandiseHierarchyGroup) {
		this.merchandiseHierarchyGroup = merchandiseHierarchyGroup;
	}

	/**
	 * @return the codigoSAP
	 */
	public Long getCodigoSAP() {
		return codigoSAP;
	}

	/**
	 * @param codigoSAP
	 *            the codigoSAP to set
	 */
	public void setCodigoSAP(Long codigoSAP) {
		this.codigoSAP = codigoSAP;
	}

	/**
	 * @return the flPriceReq
	 */
	public Integer getFlPriceReq() {
		return flPriceReq;
	}

	/**
	 * @param flPriceReq the flPriceReq to set
	 */
	public void setFlPriceReq(Integer flPriceReq) {
		this.flPriceReq = flPriceReq;
	}

	/**
	 * @return the flQuantityAllw
	 */
	public Integer getFlQuantityAllw() {
		return flQuantityAllw;
	}

	/**
	 * @param flQuantityAllw the flQuantityAllw to set
	 */
	public void setFlQuantityAllw(Integer flQuantityAllw) {
		this.flQuantityAllw = flQuantityAllw;
	}

	/**
	 * @return the flQuantityReq
	 */
	public Integer getFlQuantityReq() {
		return flQuantityReq;
	}

	/**
	 * @param flQuantityReq the flQuantityReq to set
	 */
	public void setFlQuantityReq(Integer flQuantityReq) {
		this.flQuantityReq = flQuantityReq;
	}

	public Integer getFlDscItm() {
		return flDscItm;
	}

	public void setFlDscItm(Integer flDscItm) {
		this.flDscItm = flDscItm;
	}

	public String getReferenciaSAP() {
		return referenciaSAP;
	}

	public void setReferenciaSAP(String referenciaSAP) {
		this.referenciaSAP = referenciaSAP;
	}

	/**
	 * @return the jerarquia
	 */
	public String getJerarquia() {
		return jerarquia;
	}

	/**
	 * @param jerarquia the jerarquia to set
	 */
	public void setJerarquia(String jerarquia) {
		this.jerarquia = jerarquia;
	}

	
}
