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
public class TenderControlTransaction {
	private Integer transactionID;
	private String tenderControlTypeCode;
	private String reasonCode;
	/**
	 * De las variables que se encuentran definidas debajo, solo una puede ser
	 * distinta de null
	 */
	private TenderPickupTransaction pickup;
	private TenderLoanTransaction loan;
	private TenderCountTransaction count;
	private TenderDepositTransaction deposit;
	private TenderReceiptTransaction receipt;

	private GastoEfectivo gastoEfectivo;
	private ValeEmpleado valeEmpleado;
	private ReverseLineItemInfo rvItemInfo;
	private List lineItems;
	private List retencionData;

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
	 * @return the tenderControlTypeCode
	 */
	public String getTenderControlTypeCode() {
		return tenderControlTypeCode;
	}

	/**
	 * @param tenderControlTypeCode
	 *            the tenderControlTypeCode to set
	 */
	public void setTenderControlTypeCode(String tenderControlTypeCode) {
		this.tenderControlTypeCode = tenderControlTypeCode;
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
	 * @return the pickup
	 */
	public TenderPickupTransaction getPickup() {
		if (pickup != null)
			pickup.setTransactionID(transactionID);
		return pickup;
	}

	/**
	 * @param pickup
	 *            the pickup to set
	 */
	public void setPickup(TenderPickupTransaction pickup) {
		this.pickup = pickup;
	}

	/**
	 * @return the loan
	 */
	public TenderLoanTransaction getLoan() {
		if (loan != null)
			loan.setTransactionID(transactionID);
		return loan;
	}

	/**
	 * @param loan
	 *            the loan to set
	 */
	public void setLoan(TenderLoanTransaction loan) {
		this.loan = loan;
	}

	/**
	 * @return the count
	 */
	public TenderCountTransaction getCount() {
		if (count != null)
			count.setTransactionID(transactionID);
		return count;
	}

	/**
	 * @param count
	 *            the count to set
	 */
	public void setCount(TenderCountTransaction count) {
		this.count = count;
	}

	/**
	 * @return the deposit
	 */
	public TenderDepositTransaction getDeposit() {
		if (deposit != null)
			deposit.setTransactionID(transactionID);
		return deposit;
	}

	/**
	 * @param deposit
	 *            the deposit to set
	 */
	public void setDeposit(TenderDepositTransaction deposit) {
		this.deposit = deposit;
	}

	/**
	 * @return the receipt
	 */
	public TenderReceiptTransaction getReceipt() {
		if (receipt != null)
			receipt.setTransactionID(transactionID);
		return receipt;
	}

	/**
	 * @param receipt
	 *            the receipt to set
	 */
	public void setReceipt(TenderReceiptTransaction receipt) {
		this.receipt = receipt;
	}

	/**
	 * @return the lineItems
	 */
	public List getLineItems() {
		if (lineItems != null) {
			Iterator itLineItems = lineItems.iterator();
			while (itLineItems.hasNext())
				((TenderControlTransactionLineItem) itLineItems.next())
						.setTransactionID(transactionID);
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
	 * @return the gastoEfectivo
	 */
	public GastoEfectivo getGastoEfectivo() {
		if (gastoEfectivo != null)
			gastoEfectivo.setTransactionID(transactionID);
		return gastoEfectivo;
	}

	/**
	 * @param gastoEfectivo the gastoEfectivo to set
	 */
	public void setGastoEfectivo(GastoEfectivo gastoEfectivo) {
		this.gastoEfectivo = gastoEfectivo;
	}
	
	

	public ReverseLineItemInfo getRvItemInfo() {
		if (rvItemInfo != null)
			rvItemInfo.setTransactionID(transactionID);
		return rvItemInfo;
	}

	public void setRvItemInfo(ReverseLineItemInfo rvItemInfo) {
		this.rvItemInfo = rvItemInfo;
	}

	/**
	 * @return the valeEmpleado
	 */
	public ValeEmpleado getValeEmpleado() {
		if (valeEmpleado != null)
			valeEmpleado.setTransactionID(transactionID);
		return valeEmpleado;
	}

	/**
	 * @param valeEmpleado the valeEmpleado to set
	 */
	public void setValeEmpleado(ValeEmpleado valeEmpleado) {
		this.valeEmpleado = valeEmpleado;
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
}
