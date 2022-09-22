/**
 * 
 */
package com.allc.entities;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author GUSTAVOK
 * 
 */
public class RetailTransaction {
	private Integer transactionID;
	private Integer retailTransactionTypeCode;
	private Integer tillID;
	private Integer currencyID;
	private Integer ringElapsedTime = new Integer(0);
	private Integer tenderElapsedTime = new Integer(0);
	private Integer idleElapsedTime = new Integer(0);
	private Integer lockElapsedTime = new Integer(0);
	private Double unitCount = new Double(0);
	private Integer customerID;
	private Double lineItemsScannedCount = new Double(0);
	private Double lineItemsScannedPercent = new Double(0);
	private Double lineItemsKeyedCount = new Double(0);
	private Double lineItemsKeyedPercent = new Double(0);
	private Double keyDepartmentCount = new Double(0);
	private Double keyDepartmentPercent = new Double(0);
	private Date receiptDateTime;
	private boolean includeTax;

	private List lineItems;
	private List totalItems;
	private List managerOverrides;
	private List exceptionLogs;
	private List associatedCouponsToRedemptions;
	private AutomaticCouponData automaticCouponData;
	private List bonusRedempPtsList;
	private UsedTargetedCoupons usedTargetedCoupons;
	private CouponTracking couponTracking;
	private PreferredCustomerSecPtsData preferredCustSecPtsData;
	private PreferredCustomerData preferredCustData;
	private List aliasElecCoupICList;
	private List couponsDataList;
	private List redeemedCouponsDataList;
	private InvoiceData invoiceData;
	private FacturaElec facturaElec;
	private InvoiceAssociated invoiceAssoc;
	private FerricardData ferricardData;
	private DeducibleData deducibleData;
	private List ilimitadaData;
	private PasswTemporal passwTemporal;
	private List motosData;
	private List retencionData;
	private List stringsUsuario;
	private List cardData;
	private List checkData;
	private List billData;
	private List bonoSolidario;
	private List recargaElectronica;
	private List recaudos;
	private List transferenciasBancarias;
	private List pagosConPinpad;
	private List dscEmpleados;
	private List datosEmpleados;
	private DescuentoEmpleadosTotal dscEmpTotal;
	private List promotionDiscs;
	private ReturnTransaction notaCredito;
	private List returnLineList;
	private AssociatedData associatedData;
	private List couponsPromotionData;
	private List pointsPromotionData;
	private List giftcardData;
	private List pointsRedemptionData;
	private List ticketsPromotionData;
	private List itemsVentaMayoreo;
	private ItemReservaInfo itemRsvInfo;
	private EcommerceData ecommerceData;

	public List getStringsUsuario() {
		if (stringsUsuario != null) {
			Iterator itStringUsuarioList = stringsUsuario.iterator();
			while (itStringUsuarioList.hasNext())
				((StringUsuario) itStringUsuarioList.next()).setTransactionID(transactionID);
		}
		return stringsUsuario;
		
	}

	public void setStringsUsuario(List stringsUsuario) {
		this.stringsUsuario = stringsUsuario;
	}

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
	 * @return the retailTransactionTypeCode
	 */
	public Integer getRetailTransactionTypeCode() {
		return retailTransactionTypeCode;
	}

	/**
	 * @param retailTransactionTypeCode
	 *            the retailTransactionTypeCode to set
	 */
	public void setRetailTransactionTypeCode(Integer retailTransactionTypeCode) {
		this.retailTransactionTypeCode = retailTransactionTypeCode;
	}

	/**
	 * @return the tillID
	 */
	public Integer getTillID() {
		return tillID;
	}

	/**
	 * @param tillID
	 *            the tillID to set
	 */
	public void setTillID(Integer tillID) {
		this.tillID = tillID;
	}

	/**
	 * @return the currencyID
	 */
	public Integer getCurrencyID() {
		return currencyID;
	}

	/**
	 * @param currencyID
	 *            the currencyID to set
	 */
	public void setCurrencyID(Integer currencyID) {
		this.currencyID = currencyID;
	}

	/**
	 * @return the ringElapsedTime
	 */
	public Integer getRingElapsedTime() {
		if (ringElapsedTime == null)
			ringElapsedTime = new Integer(0);
		return ringElapsedTime;
	}

	/**
	 * @param ringElapsedTime
	 *            the ringElapsedTime to set
	 */
	public void setRingElapsedTime(Integer ringElapsedTime) {
		this.ringElapsedTime = ringElapsedTime;
	}

	/**
	 * @return the tenderElapsedTime
	 */
	public Integer getTenderElapsedTime() {
		if (tenderElapsedTime == null)
			tenderElapsedTime = new Integer(0);
		return tenderElapsedTime;
	}

	/**
	 * @param tenderElapsedTime
	 *            the tenderElapsedTime to set
	 */
	public void setTenderElapsedTime(Integer tenderElapsedTime) {
		this.tenderElapsedTime = tenderElapsedTime;
	}

	/**
	 * @return the idleElapsedTime
	 */
	public Integer getIdleElapsedTime() {
		if (idleElapsedTime == null)
			idleElapsedTime = new Integer(0);
		return idleElapsedTime;
	}

	/**
	 * @param idleElapsedTime
	 *            the idleElapsedTime to set
	 */
	public void setIdleElapsedTime(Integer idleElapsedTime) {
		this.idleElapsedTime = idleElapsedTime;
	}

	/**
	 * @return the lockElapsedTime
	 */
	public Integer getLockElapsedTime() {
		if (lockElapsedTime == null)
			lockElapsedTime = new Integer(0);
		return lockElapsedTime;
	}

	/**
	 * @param lockElapsedTime
	 *            the lockElapsedTime to set
	 */
	public void setLockElapsedTime(Integer lockElapsedTime) {
		this.lockElapsedTime = lockElapsedTime;
	}

	/**
	 * @return the unitCount
	 */
	public Double getUnitCount() {
		return unitCount;
	}

	/**
	 * @param unitCount
	 *            the unitCount to set
	 */
	public void setUnitCount(Double unitCount) {
		this.unitCount = unitCount;
	}

	/**
	 * @return the customerID
	 */
	public Integer getCustomerID() {
		return customerID;
	}

	/**
	 * @param customerID
	 *            the customerID to set
	 */
	public void setCustomerID(Integer customerID) {
		this.customerID = customerID;
	}

	/**
	 * @return the lineItemsScannedCount
	 */
	public Double getLineItemsScannedCount() {
		if (lineItemsScannedCount == null)
			lineItemsScannedCount = new Double(0);
		return lineItemsScannedCount;
	}

	/**
	 * @param lineItemsScannedCount
	 *            the lineItemsScannedCount to set
	 */
	public void setLineItemsScannedCount(Double lineItemsScannedCount) {
		this.lineItemsScannedCount = lineItemsScannedCount;
	}

	/**
	 * @return the lineItemsScannedPercent
	 */
	public Double getLineItemsScannedPercent() {
		if (lineItemsScannedPercent == null)
			lineItemsScannedPercent = new Double(0);
		return lineItemsScannedPercent;
	}

	/**
	 * @param lineItemsScannedPercent
	 *            the lineItemsScannedPercent to set
	 */
	public void setLineItemsScannedPercent(Double lineItemsScannedPercent) {
		this.lineItemsScannedPercent = lineItemsScannedPercent;
	}

	/**
	 * @return the lineItemsKeyedCount
	 */
	public Double getLineItemsKeyedCount() {
		if (lineItemsKeyedCount == null)
			lineItemsKeyedCount = new Double(0);
		return lineItemsKeyedCount;
	}

	/**
	 * @param lineItemsKeyedCount
	 *            the lineItemsKeyedCount to set
	 */
	public void setLineItemsKeyedCount(Double lineItemsKeyedCount) {
		this.lineItemsKeyedCount = lineItemsKeyedCount;
	}

	/**
	 * @return the lineItemsKeyedPercent
	 */
	public Double getLineItemsKeyedPercent() {
		if (lineItemsKeyedPercent == null)
			lineItemsKeyedPercent = new Double(0);
		return lineItemsKeyedPercent;
	}

	/**
	 * @param lineItemsKeyedPercent
	 *            the lineItemsKeyedPercent to set
	 */
	public void setLineItemsKeyedPercent(Double lineItemsKeyedPercent) {
		this.lineItemsKeyedPercent = lineItemsKeyedPercent;
	}

	/**
	 * @return the keyDepartmentCount
	 */
	public Double getKeyDepartmentCount() {
		if (keyDepartmentCount == null)
			keyDepartmentCount = new Double(0);
		return keyDepartmentCount;
	}

	/**
	 * @param keyDepartmentCount
	 *            the keyDepartmentCount to set
	 */
	public void setKeyDepartmentCount(Double keyDepartmentCount) {
		this.keyDepartmentCount = keyDepartmentCount;
	}

	/**
	 * @return the keyDepartmentPercent
	 */
	public Double getKeyDepartmentPercent() {
		if (keyDepartmentPercent == null)
			keyDepartmentPercent = new Double(0);
		return keyDepartmentPercent;
	}

	/**
	 * @param keyDepartmentPercent
	 *            the keyDepartmentPercent to set
	 */
	public void setKeyDepartmentPercent(Double keyDepartmentPercent) {
		this.keyDepartmentPercent = keyDepartmentPercent;
	}

	/**
	 * @return the receiptDateTime
	 */
	public Date getReceiptDateTime() {
		if (receiptDateTime == null)
			receiptDateTime = new Date();
		return receiptDateTime;
	}

	/**
	 * @param receiptDateTime
	 *            the receiptDateTime to set
	 */
	public void setReceiptDateTime(Date receiptDateTime) {
		this.receiptDateTime = receiptDateTime;
	}

	/**
	 * @return the includeTax
	 */
	public boolean isIncludeTax() {
		return includeTax;
	}

	/**
	 * @param includeTax the includeTax to set
	 */
	public void setIncludeTax(boolean includeTax) {
		this.includeTax = includeTax;
	}

	/**
	 * @return the lineItems
	 */
	public List getLineItems() {
		if (lineItems != null) {
			Iterator itLineItems = lineItems.iterator();
			while (itLineItems.hasNext())
				((RetailTransactionLineItem) itLineItems.next()).setTransactionID(transactionID);
		}
		return lineItems;
	}

	/**
	 * @param lineItems
	 *            the lineItems to set
	 */
	public void setLineItems(List lineItems) {
		this.lineItems = lineItems;
	}

	/**
	 * @return the totalItems
	 */
	public List getTotalItems() {
		if (totalItems != null) {
			Iterator itTotalItems = totalItems.iterator();
			while (itTotalItems.hasNext())
				((RetailTransactionTotal) itTotalItems.next()).setTransactionID(transactionID);
		}
		return totalItems;
	}

	/**
	 * @param totalItems
	 *            the totalItems to set
	 */
	public void setTotalItems(List totalItems) {
		this.totalItems = totalItems;
	}

	/**
	 * @return the managerOverrides
	 */
	public List getManagerOverrides() {
		if (managerOverrides != null) {
			Iterator itManagerOverrides = managerOverrides.iterator();
			while (itManagerOverrides.hasNext())
				((ManagerOverride) itManagerOverrides.next()).setTransactionID(transactionID);
		}
		return managerOverrides;
	}

	/**
	 * @param managerOverrides
	 *            the managerOverrides to set
	 */
	public void setManagerOverrides(List managerOverrides) {
		this.managerOverrides = managerOverrides;
	}

	/**
	 * @return the exceptionLogs
	 */
	public List getExceptionLogs() {
		if (exceptionLogs != null) {
			Iterator itExceptionLogs = exceptionLogs.iterator();
			while (itExceptionLogs.hasNext())
				((ExceptionLog) itExceptionLogs.next()).setTransactionID(transactionID);
		}
		return exceptionLogs;
	}

	/**
	 * @param exceptionLogs
	 *            the exceptionLogs to set
	 */
	public void setExceptionLogs(List exceptionLogs) {
		this.exceptionLogs = exceptionLogs;
	}

	/**
	 * @return the associatedCouponsToRedemptions
	 */
	public List getAssociatedCouponsToRedemptions() {
		if (associatedCouponsToRedemptions != null) {
			Iterator itCoupons = associatedCouponsToRedemptions.iterator();
			while (itCoupons.hasNext())
				((AssociatedCoupon) itCoupons.next()).setTransactionID(transactionID);
		}
		return associatedCouponsToRedemptions;
	}

	/**
	 * @param associatedCouponsToRedemptions the associatedCouponsToRedemptions to set
	 */
	public void setAssociatedCouponsToRedemptions(List associatedCouponsToRedemptions) {
		this.associatedCouponsToRedemptions = associatedCouponsToRedemptions;
	}

	/**
	 * @return the automaticCouponData
	 */
	public AutomaticCouponData getAutomaticCouponData() {
		if (automaticCouponData != null)
			automaticCouponData.setTransactionID(transactionID);
		return automaticCouponData;
	}

	/**
	 * @param automaticCouponData
	 *            the automaticCouponData to set
	 */
	public void setAutomaticCouponData(AutomaticCouponData automaticCouponData) {
		this.automaticCouponData = automaticCouponData;
	}

	/**
	 * @return the invoiceData
	 */
	public InvoiceData getInvoiceData() {
		if (invoiceData != null)
			invoiceData.setTransactionID(transactionID);
		return invoiceData;
	}

	/**
	 * @param invoiceData
	 *            the invoiceData to set
	 */
	public void setInvoiceData(InvoiceData invoiceData) {
		this.invoiceData = invoiceData;
	}

	/**
	 * @return the invoiceAssoc
	 */
	public InvoiceAssociated getInvoiceAssoc() {
		if (invoiceAssoc != null)
			invoiceAssoc.setTransactionID(transactionID);
		return invoiceAssoc;
	}

	/**
	 * @param invoiceAssoc
	 *            the invoiceAssoc to set
	 */
	public void setInvoiceAssoc(InvoiceAssociated invoiceAssoc) {
		this.invoiceAssoc = invoiceAssoc;
	}

	/**
	 * @return the facturaElec
	 */
	public FacturaElec getFacturaElec() {
		return facturaElec;
	}

	/**
	 * @param facturaElec
	 *            the facturaElec to set
	 */
	public void setFacturaElec(FacturaElec facturaElec) {
		this.facturaElec = facturaElec;
	}

	/**
	 * @return the bonusRedempPtsList
	 */
	public List getBonusRedempPtsList() {
		if (bonusRedempPtsList != null) {
			Iterator itBonusRedempPtsList = bonusRedempPtsList.iterator();
			while (itBonusRedempPtsList.hasNext())
				((BonusRedemptionPoints) itBonusRedempPtsList.next()).setTransactionID(transactionID);
		}
		return bonusRedempPtsList;
	}

	/**
	 * @param bonusRedempPtsList
	 *            the bonusRedempPtsList to set
	 */
	public void setBonusRedempPtsList(List bonusRedempPtsList) {
		this.bonusRedempPtsList = bonusRedempPtsList;
	}

	/**
	 * @return the usedTargetedCoupons
	 */
	public UsedTargetedCoupons getUsedTargetedCoupons() {
		if (usedTargetedCoupons != null)
			usedTargetedCoupons.setTransactionID(transactionID);
		return usedTargetedCoupons;
	}

	/**
	 * @param usedTargetedCoupons
	 *            the usedTargetedCoupons to set
	 */
	public void setUsedTargetedCoupons(UsedTargetedCoupons usedTargetedCoupons) {
		this.usedTargetedCoupons = usedTargetedCoupons;
	}

	/**
	 * @return the couponTracking
	 */
	public CouponTracking getCouponTracking() {
		if (couponTracking != null)
			couponTracking.setTransactionID(transactionID);
		return couponTracking;
	}

	/**
	 * @param couponTracking
	 *            the couponTracking to set
	 */
	public void setCouponTracking(CouponTracking couponTracking) {
		this.couponTracking = couponTracking;
	}

	/**
	 * @return the preferredCustSecPtsData
	 */
	public PreferredCustomerSecPtsData getPreferredCustSecPtsData() {
		if (preferredCustSecPtsData != null)
			preferredCustSecPtsData.setTransactionID(transactionID);
		return preferredCustSecPtsData;
	}

	/**
	 * @param preferredCustSecPtsData
	 *            the preferredCustSecPtsData to set
	 */
	public void setPreferredCustSecPtsData(PreferredCustomerSecPtsData preferredCustSecPtsData) {
		this.preferredCustSecPtsData = preferredCustSecPtsData;
	}

	/**
	 * @return the preferredCustData
	 */
	public PreferredCustomerData getPreferredCustData() {
		if (preferredCustData != null)
			preferredCustData.setTransactionID(transactionID);
		return preferredCustData;
	}

	/**
	 * @param preferredCustData
	 *            the preferredCustData to set
	 */
	public void setPreferredCustData(PreferredCustomerData preferredCustData) {
		this.preferredCustData = preferredCustData;
	}

	/**
	 * @return the aliasElecCoupICList
	 */
	public List getAliasElecCoupICList() {
		if (aliasElecCoupICList != null) {
			Iterator itAliasElecCoupICList = aliasElecCoupICList.iterator();
			while (itAliasElecCoupICList.hasNext())
				((AliasElecCouponItemCode) itAliasElecCoupICList.next()).setTransactionID(transactionID);
		}
		return aliasElecCoupICList;
	}

	/**
	 * @param aliasElecCoupICList
	 *            the aliasElecCoupICList to set
	 */
	public void setAliasElecCoupICList(List aliasElecCoupICList) {
		this.aliasElecCoupICList = aliasElecCoupICList;
	}

	/**
	 * @return the motosData
	 */
	public List getMotosData() {
		if (motosData != null) {
			Iterator itMotosDataList = motosData.iterator();
			while (itMotosDataList.hasNext())
				((MotoData) itMotosDataList.next()).setTransactionID(transactionID);
		}
		return motosData;
	}

	/**
	 * @param motosData
	 *            the motosData to set
	 */
	public void setMotosData(List motosData) {
		this.motosData = motosData;
	}

	/**
	 * @return the retencionData
	 */
	public List getRetencionData() {
		if (retencionData != null) {
			Iterator itRetencionDataList = retencionData.iterator();
			while (itRetencionDataList.hasNext())
				((RetencionData) itRetencionDataList.next()).setTransactionID(transactionID);
		}
		return retencionData;
	}

	/**
	 * @param retencionData the retencionData to set
	 */
	public void setRetencionData(List retencionData) {
		this.retencionData = retencionData;
	}

	/**
	 * @return the couponsDataList
	 */
	public List getCouponsDataList() {
		if (couponsDataList != null) {
			Iterator itCouponDataList = couponsDataList.iterator();
			while (itCouponDataList.hasNext())
				((CouponData) itCouponDataList.next()).setTransactionID(transactionID);
		}
		return couponsDataList;
	}

	/**
	 * @param couponsDataList
	 *            the couponsDataList to set
	 */
	public void setCouponsDataList(List couponsDataList) {
		this.couponsDataList = couponsDataList;
	}

	/**
	 * @return the redeemedCouponsDataList
	 */
	public List getRedeemedCouponsDataList() {
		if (redeemedCouponsDataList != null) {
			Iterator itCouponDataList = redeemedCouponsDataList.iterator();
			while (itCouponDataList.hasNext())
				((RedeemedCouponData) itCouponDataList.next()).setTransactionID(transactionID);
		}
		return redeemedCouponsDataList;
	}

	/**
	 * @param redeemedCouponsDataList
	 *            the redeemedCouponsDataList to set
	 */
	public void setRedeemedCouponsDataList(List redeemedCouponsDataList) {
		this.redeemedCouponsDataList = redeemedCouponsDataList;
	}

	public List getCardData() {
		
		if (cardData != null) {
			Iterator itCardDataList = cardData.iterator();
			while (itCardDataList.hasNext())
				((CardData) itCardDataList.next()).setTransactionID(transactionID);
		}
		return cardData;
	}

	public void setCardData(List cardData) {
		this.cardData = cardData;
	}

	public List getCheckData() {
		if (checkData != null) {
			Iterator itCheckDataList = checkData.iterator();
			while (itCheckDataList.hasNext())
				((CheckData) itCheckDataList.next()).setTransactionID(transactionID);
		}
		return checkData;
	}

	public void setCheckData(List checkData) {
		this.checkData = checkData;
	}

	public List getBillData() {
		if (billData != null) {
			Iterator itCashDataList = billData.iterator();
			while (itCashDataList.hasNext())
				((BillData) itCashDataList.next()).setTransactionID(transactionID);
		}
		return billData;
	}

	public void setBillData(List billData) {
		this.billData = billData;
	}

	public List getBonoSolidario() {
		if (bonoSolidario != null) {
			Iterator itBonoSolidarioList = bonoSolidario.iterator();
			while (itBonoSolidarioList.hasNext())
				((BonoSolidario) itBonoSolidarioList.next()).setTransactionID(transactionID);
		}
		
		return bonoSolidario;
	}

	public void setBonoSolidario(List bonoSolidario) {
		this.bonoSolidario = bonoSolidario;
	}

	public List getRecargaElectronica() {
		if (recargaElectronica != null) {
			Iterator itRecargaElectronicaList = recargaElectronica.iterator();
			while (itRecargaElectronicaList.hasNext())
				((RecargaElec) itRecargaElectronicaList.next()).setTransactionID(transactionID);
		}
		return recargaElectronica;
	}

	public void setRecargaElectronica(List recargaElectronica) {
		this.recargaElectronica = recargaElectronica;
	}

	public List getRecaudos() {
		if (recaudos != null) {
			Iterator itRecaudosList = recaudos.iterator();
			while (itRecaudosList.hasNext())
				((Recaudos) itRecaudosList.next()).setTransactionID(transactionID);
		}
		return recaudos;
	}

	public void setRecaudos(List recaudos) {
		this.recaudos = recaudos;
	}

	public List getTransferenciasBancarias() {
		if (transferenciasBancarias != null) {
			Iterator itTransBancList = transferenciasBancarias.iterator();
			while (itTransBancList.hasNext())
				((TransferenciaBancaria) itTransBancList.next()).setTransactionID(transactionID);
		}
		return transferenciasBancarias;
	}

	public void setTransferenciasBancarias(List transferenciasBancarias) {
		this.transferenciasBancarias = transferenciasBancarias;
	}
	
	public List getPagosConPinpad() {
		if (pagosConPinpad != null) {
			Iterator itPagosPinPadList = pagosConPinpad.iterator();
			while (itPagosPinPadList.hasNext())
				((TenderPinpadInfo) itPagosPinPadList.next()).setTransactionID(transactionID);
		}
		return pagosConPinpad;
	}

	public void setPagosConPinpad(List pagosConPinpad) {
		this.pagosConPinpad = pagosConPinpad;
	}
	
	

	public List getDscEmpleados() {
		if (dscEmpleados != null) {
			Iterator itDscEmpList = dscEmpleados.iterator();
			while (itDscEmpList.hasNext())
				((DescuentoEmpleados) itDscEmpList.next()).setTransactionID(transactionID);
		}
		return dscEmpleados;
	}

	public void setDscEmpleados(List dscEmpleados) {
		this.dscEmpleados = dscEmpleados;
	}
	
	

	public List getDatosEmpleados() {
		if (datosEmpleados != null) {
			Iterator itDatosEmpList = datosEmpleados.iterator();
			while (itDatosEmpList.hasNext())
				((DatosEmpleados) itDatosEmpList.next()).setTransactionID(transactionID);
		}
		return datosEmpleados;
	}

	public void setDatosEmpleados(List datosEmpleados) {
		this.datosEmpleados = datosEmpleados;
	}

	/**
	 * @return the promotionDiscs
	 */
	public List getPromotionDiscs() {
		if (promotionDiscs != null) {
			Iterator itPromotionDiscs = promotionDiscs.iterator();
			while (itPromotionDiscs.hasNext())
				((PromotionDiscount) itPromotionDiscs.next()).setTransactionID(transactionID);
		}
		return promotionDiscs;
	}

	/**
	 * @param promotionDiscs the promotionDiscs to set
	 */
	public void setPromotionDiscs(List promotionDiscs) {
		this.promotionDiscs = promotionDiscs;
	}
	
	
	public List getCouponsPromotionData() {
		if (couponsPromotionData != null) {
			Iterator itCouponPromotionData = couponsPromotionData.iterator();
			while (itCouponPromotionData.hasNext())
				((CouponPromotionData) itCouponPromotionData.next()).setTransactionID(transactionID);
		}
		return couponsPromotionData;
	}

	public void setCouponsPromotionData(List couponsPromotionData) {
		this.couponsPromotionData = couponsPromotionData;
	}

	public List getPointsPromotionData() {
		if (pointsPromotionData != null) {
			Iterator itPointPromotionData = pointsPromotionData.iterator();
			while (itPointPromotionData.hasNext())
				((PointsPromotionData) itPointPromotionData.next()).setTransactionID(transactionID);
		}
		return pointsPromotionData;
	}

	public void setPointsPromotionData(List pointsPromotionData) {
		this.pointsPromotionData = pointsPromotionData;
	}

	public FerricardData getFerricardData() {
		if (ferricardData != null)
			ferricardData.setTransactionID(transactionID);
		return ferricardData;
	}

	public void setFerricardData(FerricardData ferricardData) {
		this.ferricardData = ferricardData;
	}

	/**
	 * @return the deducibleData
	 */
	public DeducibleData getDeducibleData() {
		if (deducibleData != null)
			deducibleData.setTransactionID(transactionID);
		return deducibleData;
	}

	/**
	 * @param deducibleData the deducibleData to set
	 */
	public void setDeducibleData(DeducibleData deducibleData) {
		this.deducibleData = deducibleData;
	}

	/**
	 * @return the ilimitadaData
	 */
	public List getIlimitadaData() {
		if (ilimitadaData != null) {
			Iterator itIlimitadaDataList = ilimitadaData.iterator();
			while (itIlimitadaDataList.hasNext())
				((IlimitadaData) itIlimitadaDataList.next()).setTransactionID(transactionID);
		}
		return ilimitadaData;
	}

	/**
	 * @param ilimitadaData the ilimitadaData to set
	 */
	public void setIlimitadaData(List ilimitadaData) {
		this.ilimitadaData = ilimitadaData;
	}

	public DescuentoEmpleadosTotal getDscEmpTotal() {
		if (dscEmpTotal != null)
			dscEmpTotal.setTransactionID(transactionID);
		return dscEmpTotal;
	}

	public void setDscEmpTotal(DescuentoEmpleadosTotal dscEmpTotal) {
		this.dscEmpTotal = dscEmpTotal;
	}
	
	

	public ItemReservaInfo getItemRsvInfo() {
		if (itemRsvInfo != null)
			itemRsvInfo.setTransactionID(transactionID);
		return itemRsvInfo;
	}

	public void setItemRsvInfo(ItemReservaInfo itemRsvInfo) {
		this.itemRsvInfo = itemRsvInfo;
	}

	public AssociatedData getAssociatedData() {
		if (associatedData != null)
			associatedData.setTransactionID(transactionID);
		return associatedData;
	}

	public void setAssociatedData(AssociatedData associatedData) {
		this.associatedData = associatedData;
	}

	/**
	 * @return the passwTemporal
	 */
	public PasswTemporal getPasswTemporal() {
		return passwTemporal;
	}

	/**
	 * @param passwTemporal the passwTemporal to set
	 */
	public void setPasswTemporal(PasswTemporal passwTemporal) {
		this.passwTemporal = passwTemporal;
	}

	/**
	 * @return the notaCredito
	 */
	public ReturnTransaction getNotaCredito() {
		if (notaCredito != null)
			notaCredito.setTransactionID(transactionID);
		return notaCredito;
	}

	/**
	 * @param notaCredito the notaCredito to set
	 */
	public void setNotaCredito(ReturnTransaction notaCredito) {
		this.notaCredito = notaCredito;
	}

	/**
	 * @return the returnLineList
	 */
	public List getReturnLineList() {
		if (returnLineList != null) {
			Iterator itReturnLineList = returnLineList.iterator();
			while (itReturnLineList.hasNext())
				((ReturnLineItem) itReturnLineList.next()).setTransactionID(transactionID);
		}
		return returnLineList;
	}

	/**
	 * @param returnLineList the returnLineList to set
	 */
	public void setReturnLineList(List returnLineList) {
		this.returnLineList = returnLineList;
	}

	/**
	 * @return the giftcardData
	 */
	public List getGiftcardData() {
		if (giftcardData != null) {
			Iterator itGiftcardDataList = giftcardData.iterator();
			while (itGiftcardDataList.hasNext())
				((GiftcardData) itGiftcardDataList.next()).setTransactionID(transactionID);
		}
		return giftcardData;
	}

	/**
	 * @param giftcardData the giftcardData to set
	 */
	public void setGiftcardData(List giftcardData) {
		this.giftcardData = giftcardData;
	}

	/**
	 * @return the pointsRedemptionData
	 */
	public List getPointsRedemptionData() {
		if (pointsRedemptionData != null) {
			Iterator itPointsRedemptionDataList = pointsRedemptionData.iterator();
			while (itPointsRedemptionDataList.hasNext())
				((PointsRedemptionData) itPointsRedemptionDataList.next()).setTransactionID(transactionID);
		}
		return pointsRedemptionData;
	}

	/**
	 * @param pointsRedemptionData the pointsRedemptionData to set
	 */
	public void setPointsRedemptionData(List pointsRedemptionData) {
		this.pointsRedemptionData = pointsRedemptionData;
	}

	public List getTicketsPromotionData() {
		if (ticketsPromotionData != null) {
			Iterator itTicketPromotionDataList = ticketsPromotionData.iterator();
			while (itTicketPromotionDataList.hasNext())
				((TicketPromotionData) itTicketPromotionDataList.next()).setTransactionID(transactionID);
		}
		return ticketsPromotionData;
	}

	public void setTicketsPromotionData(List ticketsPromotionData) {
		this.ticketsPromotionData = ticketsPromotionData;
	}

	public List getItemsVentaMayoreo() {
		if (itemsVentaMayoreo != null) {
			Iterator itItemsVentaMayoreoList = itemsVentaMayoreo.iterator();
			while (itItemsVentaMayoreoList.hasNext())
				((VentaMayoreoItem) itItemsVentaMayoreoList.next()).setTransactionID(transactionID);
		}
		return itemsVentaMayoreo;
	}

	public void setItemsVentaMayoreo(List itemsVentaMayoreo) {
		this.itemsVentaMayoreo = itemsVentaMayoreo;
	}

	public EcommerceData getEcommerceData() {
		if (ecommerceData != null)
			ecommerceData.setTransactionID(transactionID);
		return ecommerceData;
	}

	public void setEcommerceData(EcommerceData ecommerceData) {
		this.ecommerceData = ecommerceData;
	}
	
	
}
