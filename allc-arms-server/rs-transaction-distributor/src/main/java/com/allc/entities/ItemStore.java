/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class ItemStore implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer itemID;
	private Integer retailStoreID;
	private Double salesPrice;
	private Integer taxA;
	private Integer taxB;
	private Integer taxC;
	private Integer taxD;
	private Integer taxE;
	private Integer taxF;
	private Integer taxG;
	private Integer taxH;
	private Integer flAuthorizedForSale;
	private Integer specialFamily;
	private Double precioMayoreo;
	private Integer cantidadMayoreo;
	private Integer flagMayoreo;
	private String proveedor;
	private String deducible;
	private String color;
	private String medida;
	private String diseno;
	private String marca;
	private String presentacion;
	private Integer flAcumMovDat;
	private Integer flCouponComUsed;
	private Integer flMultVales;
	private Integer flSaleItemExcepLog;
	private Integer flLogtochangefile;
	private Integer flPointsOnlyItemCoupon;
	private Integer flPointsApplytoItem;
	private Integer flItemLinkstoDeposit;
	private Integer flRestrictedSale;
	private Integer flFuelVolumeRequired;
	private Integer restricSaleType;

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

	public Integer getRetailStoreID() {
		return retailStoreID;
	}

	public void setRetailStoreID(Integer retailStoreID) {
		this.retailStoreID = retailStoreID;
	}

	public Double getSalesPrice() {
		return salesPrice;
	}

	public void setSalesPrice(Double salesPrice) {
		this.salesPrice = salesPrice;
	}

	public Integer getFlAuthorizedForSale() {
		return flAuthorizedForSale;
	}

	public void setFlAuthorizedForSale(Integer flAuthorizedForSale) {
		this.flAuthorizedForSale = flAuthorizedForSale;
	}

	/**
	 * @return the taxA
	 */
	public Integer getTaxA() {
		return taxA;
	}

	/**
	 * @param taxA
	 *            the taxA to set
	 */
	public void setTaxA(Integer taxA) {
		this.taxA = taxA;
	}

	/**
	 * @return the taxB
	 */
	public Integer getTaxB() {
		return taxB;
	}

	/**
	 * @param taxB
	 *            the taxB to set
	 */
	public void setTaxB(Integer taxB) {
		this.taxB = taxB;
	}

	/**
	 * @return the taxC
	 */
	public Integer getTaxC() {
		return taxC;
	}

	/**
	 * @param taxC
	 *            the taxC to set
	 */
	public void setTaxC(Integer taxC) {
		this.taxC = taxC;
	}

	/**
	 * @return the taxD
	 */
	public Integer getTaxD() {
		return taxD;
	}

	/**
	 * @param taxD
	 *            the taxD to set
	 */
	public void setTaxD(Integer taxD) {
		this.taxD = taxD;
	}

	/**
	 * @return the taxE
	 */
	public Integer getTaxE() {
		return taxE;
	}

	/**
	 * @param taxE the taxE to set
	 */
	public void setTaxE(Integer taxE) {
		this.taxE = taxE;
	}

	/**
	 * @return the taxF
	 */
	public Integer getTaxF() {
		return taxF;
	}

	/**
	 * @param taxF the taxF to set
	 */
	public void setTaxF(Integer taxF) {
		this.taxF = taxF;
	}

	/**
	 * @return the taxG
	 */
	public Integer getTaxG() {
		return taxG;
	}

	/**
	 * @param taxG the taxG to set
	 */
	public void setTaxG(Integer taxG) {
		this.taxG = taxG;
	}

	/**
	 * @return the taxH
	 */
	public Integer getTaxH() {
		return taxH;
	}

	/**
	 * @param taxH the taxH to set
	 */
	public void setTaxH(Integer taxH) {
		this.taxH = taxH;
	}

	/**
	 * @return the specialFamily
	 */
	public Integer getSpecialFamily() {
		return specialFamily;
	}

	/**
	 * @param specialFamily
	 *            the specialFamily to set
	 */
	public void setSpecialFamily(Integer specialFamily) {
		this.specialFamily = specialFamily;
	}

	public Double getPrecioMayoreo() {
		return precioMayoreo;
	}

	public void setPrecioMayoreo(Double precioMayoreo) {
		this.precioMayoreo = precioMayoreo;
	}

	public Integer getCantidadMayoreo() {
		return cantidadMayoreo;
	}

	public void setCantidadMayoreo(Integer cantidadMayoreo) {
		this.cantidadMayoreo = cantidadMayoreo;
	}

	public Integer getFlagMayoreo() {
		return flagMayoreo;
	}

	public void setFlagMayoreo(Integer flagMayoreo) {
		this.flagMayoreo = flagMayoreo;
	}

	public String getProveedor() {
		return proveedor;
	}

	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}

	public String getDeducible() {
		return deducible;
	}

	public void setDeducible(String deducible) {
		this.deducible = deducible;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getMedida() {
		return medida;
	}

	public void setMedida(String medida) {
		this.medida = medida;
	}

	public String getDiseno() {
		return diseno;
	}

	public void setDiseno(String diseno) {
		this.diseno = diseno;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public String getPresentacion() {
		return presentacion;
	}

	public void setPresentacion(String presentacion) {
		this.presentacion = presentacion;
	}

	public Integer getFlAcumMovDat() {
		return flAcumMovDat;
	}

	public void setFlAcumMovDat(Integer flAcumMovDat) {
		this.flAcumMovDat = flAcumMovDat;
	}

	public Integer getFlCouponComUsed() {
		return flCouponComUsed;
	}

	public void setFlCouponComUsed(Integer flCouponComUsed) {
		this.flCouponComUsed = flCouponComUsed;
	}

	public Integer getFlMultVales() {
		return flMultVales;
	}

	public void setFlMultVales(Integer flMultVales) {
		this.flMultVales = flMultVales;
	}

	public Integer getFlSaleItemExcepLog() {
		return flSaleItemExcepLog;
	}

	public void setFlSaleItemExcepLog(Integer flSaleItemExcepLog) {
		this.flSaleItemExcepLog = flSaleItemExcepLog;
	}

	public Integer getFlLogtochangefile() {
		return flLogtochangefile;
	}

	public void setFlLogtochangefile(Integer flLogtochangefile) {
		this.flLogtochangefile = flLogtochangefile;
	}

	public Integer getFlPointsOnlyItemCoupon() {
		return flPointsOnlyItemCoupon;
	}

	public void setFlPointsOnlyItemCoupon(Integer flPointsOnlyItemCoupon) {
		this.flPointsOnlyItemCoupon = flPointsOnlyItemCoupon;
	}

	public Integer getFlPointsApplytoItem() {
		return flPointsApplytoItem;
	}

	public void setFlPointsApplytoItem(Integer flPointsApplytoItem) {
		this.flPointsApplytoItem = flPointsApplytoItem;
	}

	public Integer getFlItemLinkstoDeposit() {
		return flItemLinkstoDeposit;
	}

	public void setFlItemLinkstoDeposit(Integer flItemLinkstoDeposit) {
		this.flItemLinkstoDeposit = flItemLinkstoDeposit;
	}

	public Integer getFlRestrictedSale() {
		return flRestrictedSale;
	}

	public void setFlRestrictedSale(Integer flRestrictedSale) {
		this.flRestrictedSale = flRestrictedSale;
	}

	public Integer getFlFuelVolumeRequired() {
		return flFuelVolumeRequired;
	}

	public void setFlFuelVolumeRequired(Integer flFuelVolumeRequired) {
		this.flFuelVolumeRequired = flFuelVolumeRequired;
	}

	public Integer getRestricSaleType() {
		return restricSaleType;
	}

	public void setRestricSaleType(Integer restricSaleType) {
		this.restricSaleType = restricSaleType;
	}
	
	

}
