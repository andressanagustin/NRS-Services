/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class SaleReturnLineItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private Long posItemID;
	private Long itemCode;
	private Item item;
	private Integer unitOfMeasure;
	private Integer itemType;
	private Double quantity = new Double(0);
	private Double units = new Double(0);
	private Double regularSalesUnitPrice = new Double(0);
	private Double actualUnitPrice = new Double(0);
	private Double extendedAmount = new Double(0);
	private Double unitDiscountAmount = new Double(0);
	private Double extendedDiscountAmount = new Double(0);
	private String reasonCode;
	private String entryMethodCode;
	private String sellUnitPriceEntryMethodCode;
	private String actionCode;
	private String taxType;
	private Integer posDepartmentCode;
	private Integer merchandiseHierarchyGroupCode;
	private Integer priceEntered = new Integer(0);
	private Double appliedTax;
	private Integer ordinalNumber;
	private Boolean isPromo;
	private Boolean isPorMayor;
	private Boolean isDescEmp;
	private Boolean isPorRedencion;
	private ResumenItem resumenItem;

	/**
	 * @return the transactionID
	 */
	public Integer getTransactionID() {
		return transactionID;
	}

	/**
	 * @param transactionID
	 *            the transactionID to set
	 */
	public void setTransactionID(Integer transactionID) {
		this.transactionID = transactionID;
	}

	/**
	 * @return the sequenceNumber
	 */
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * @param sequenceNumber
	 *            the sequenceNumber to set
	 */
	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	/**
	 * @return the posItemID
	 */
	public Long getPosItemID() {
		return posItemID;
	}

	/**
	 * @param posItemID
	 *            the posItemID to set
	 */
	public void setPosItemID(Long posItemID) {
		this.posItemID = posItemID;
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
	 * @return the unitOfMeasure
	 */
	public Integer getUnitOfMeasure() {
		return unitOfMeasure;
	}

	/**
	 * @param unitOfMeasure
	 *            the unitOfMeasure to set
	 */
	public void setUnitOfMeasure(Integer unitOfMeasure) {
		this.unitOfMeasure = unitOfMeasure;
	}

	/**
	 * @return the itemType
	 */
	public Integer getItemType() {
		return itemType;
	}

	/**
	 * @param itemType
	 *            the itemType to set
	 */
	public void setItemType(Integer itemType) {
		this.itemType = itemType;
	}

	/**
	 * @return the quantity
	 */
	public Double getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity
	 *            the quantity to set
	 */
	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the units
	 */
	public Double getUnits() {
		return units;
	}

	/**
	 * @param units
	 *            the units to set
	 */
	public void setUnits(Double units) {
		this.units = units;
	}

	/**
	 * @return the regularSalesUnitPrice
	 */
	public Double getRegularSalesUnitPrice() {
		return regularSalesUnitPrice;
	}

	/**
	 * @param regularSalesUnitPrice
	 *            the regularSalesUnitPrice to set
	 */
	public void setRegularSalesUnitPrice(Double regularSalesUnitPrice) {
		this.regularSalesUnitPrice = regularSalesUnitPrice;
	}

	/**
	 * @return the actualUnitPrice
	 */
	public Double getActualUnitPrice() {
		return actualUnitPrice;
	}

	/**
	 * @param actualUnitPrice
	 *            the actualUnitPrice to set
	 */
	public void setActualUnitPrice(Double actualUnitPrice) {
		this.actualUnitPrice = actualUnitPrice;
	}

	/**
	 * @return the extendedAmount
	 */
	public Double getExtendedAmount() {
		return extendedAmount;
	}

	/**
	 * @param extendedAmount
	 *            the extendedAmount to set
	 */
	public void setExtendedAmount(Double extendedAmount) {
		this.extendedAmount = extendedAmount;
	}

	/**
	 * @return the unitDiscountAmount
	 */
	public Double getUnitDiscountAmount() {
		return unitDiscountAmount;
	}

	/**
	 * @param unitDiscountAmount
	 *            the unitDiscountAmount to set
	 */
	public void setUnitDiscountAmount(Double unitDiscountAmount) {
		this.unitDiscountAmount = unitDiscountAmount;
	}

	/**
	 * @return the extendedDiscountAmount
	 */
	public Double getExtendedDiscountAmount() {
		return extendedDiscountAmount;
	}

	/**
	 * @param extendedDiscountAmount
	 *            the extendedDiscountAmount to set
	 */
	public void setExtendedDiscountAmount(Double extendedDiscountAmount) {
		this.extendedDiscountAmount = extendedDiscountAmount;
	}

	/**
	 * @return the reasonCode
	 */
	public String getReasonCode() {
		return reasonCode;
	}

	/**
	 * @param reasonCode
	 *            the reasonCode to set
	 */
	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}

	/**
	 * @return the entryMethodCode
	 */
	public String getEntryMethodCode() {
		return entryMethodCode;
	}

	/**
	 * @param entryMethodCode
	 *            the entryMethodCode to set
	 */
	public void setEntryMethodCode(String entryMethodCode) {
		this.entryMethodCode = entryMethodCode;
	}

	/**
	 * @return the sellUnitPriceEntryMethodCode
	 */
	public String getSellUnitPriceEntryMethodCode() {
		return sellUnitPriceEntryMethodCode;
	}

	/**
	 * @param sellUnitPriceEntryMethodCode
	 *            the sellUnitPriceEntryMethodCode to set
	 */
	public void setSellUnitPriceEntryMethodCode(String sellUnitPriceEntryMethodCode) {
		this.sellUnitPriceEntryMethodCode = sellUnitPriceEntryMethodCode;
	}

	/**
	 * @return the actionCode
	 */
	public String getActionCode() {
		return actionCode;
	}

	/**
	 * @param actionCode
	 *            the actionCode to set
	 */
	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}

	/**
	 * @return the item
	 */
	public Item getItem() {
		return item;
	}

	/**
	 * @param item
	 *            the item to set
	 */
	public void setItem(Item item) {
		this.item = item;
	}

	/**
	 * @return the taxType
	 */
	public String getTaxType() {
		return taxType;
	}

	/**
	 * @param taxType
	 *            the taxType to set
	 */
	public void setTaxType(String taxType) {
		this.taxType = taxType;
	}

	/**
	 * @return the posDepartmentCode
	 */
	public Integer getPosDepartmentCode() {
		return posDepartmentCode;
	}

	/**
	 * @param posDepartmentCode
	 *            the posDepartmentCode to set
	 */
	public void setPosDepartmentCode(Integer posDepartmentCode) {
		this.posDepartmentCode = posDepartmentCode;
	}

	/**
	 * @return the merchandiseHierarchyGroupCode
	 */
	public Integer getMerchandiseHierarchyGroupCode() {
		return merchandiseHierarchyGroupCode;
	}

	/**
	 * @param merchandiseHierarchyGroupCode
	 *            the merchandiseHierarchyGroupCode to set
	 */
	public void setMerchandiseHierarchyGroupCode(Integer merchandiseHierarchyGroupCode) {
		this.merchandiseHierarchyGroupCode = merchandiseHierarchyGroupCode;
	}

	/**
	 * @return the priceEntered
	 */
	public Integer getPriceEntered() {
		return priceEntered;
	}

	/**
	 * @param priceEntered
	 *            the priceEntered to set
	 */
	public void setPriceEntered(Integer priceEntered) {
		this.priceEntered = priceEntered;
	}

	/**
	 * @return the appliedTax
	 */
	public Double getAppliedTax() {
		return appliedTax;
	}

	/**
	 * @param appliedTax the appliedTax to set
	 */
	public void setAppliedTax(Double appliedTax) {
		this.appliedTax = appliedTax;
	}

	/**
	 * @return the isPromo
	 */
	public Boolean getIsPromo() {
		if(isPromo == null)
			isPromo = Boolean.FALSE;
		return isPromo;
	}

	/**
	 * @param isPromo the isPromo to set
	 */
	public void setIsPromo(Boolean isPromo) {
		this.isPromo = isPromo;
	}

	/**
	 * @return the isPorMayor
	 */
	public Boolean getIsPorMayor() {
		if(isPorMayor == null)
			isPorMayor = Boolean.FALSE;
		return isPorMayor;
	}

	/**
	 * @param isPorMayor the isPorMayor to set
	 */
	public void setIsPorMayor(Boolean isPorMayor) {
		this.isPorMayor = isPorMayor;
	}
	
	/**
	 * @return the isPorRedencion
	 */
	public Boolean getIsPorRedencion() {
		if(isPorRedencion == null)
			isPorRedencion = Boolean.FALSE;
		return isPorRedencion;
	}

	public Boolean getIsDescEmp() {
		if(isDescEmp == null)
			isDescEmp = Boolean.FALSE;
		return isDescEmp;
	}

	public void setIsDescEmp(Boolean isDescEmp) {
		this.isDescEmp = isDescEmp;
	}

	/**
	 * @param isPorRedencion the isPorRedencion to set
	 */
	public void setIsPorRedencion(Boolean isPorRedencion) {
		this.isPorRedencion = isPorRedencion;
	}

	/**
	 * @return the ordinalNumber
	 */
	public Integer getOrdinalNumber() {
		return ordinalNumber;
	}

	/**
	 * @param ordinalNumber the ordinalNumber to set
	 */
	public void setOrdinalNumber(Integer ordinalNumber) {
		this.ordinalNumber = ordinalNumber;
	}

	public ResumenItem getResumenItem() {
		if (resumenItem != null) {
			resumenItem.setTransactionID(transactionID);
			resumenItem.setSequenceNumber(sequenceNumber);
		}
		return resumenItem;
	}

	public void setResumenItem(ResumenItem resumenItem) {
		this.resumenItem = resumenItem;
	}

}
