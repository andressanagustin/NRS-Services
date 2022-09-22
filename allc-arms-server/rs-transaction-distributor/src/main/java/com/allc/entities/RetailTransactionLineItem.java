/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author GUSTAVOK
 * 
 */
public class RetailTransactionLineItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private Integer itemTypeCode;
	private Date beginDateTime;
	private String beginDateTimeString;
	private Boolean voidFlag;
	private Integer voidLine;
	private Date endDateTime;
	private String endDateTimeString;
	private String entryMethod;
	/**
	 * De las variables que se encuentran definidas debajo, solo una puede ser
	 * distinta de null
	 */
	private SaleReturnLineItem saleLI;
	private SaleReturnLineItem returnLI;
	private TenderLineItem tender;
	private PriceModificationLineItem priceModification;
	private TaxLineItem tax;
	private MiscellaneousFeeLineItem miscellaneousFee;

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
	 * @return the itemTypeCode
	 */
	public Integer getItemTypeCode() {
		return itemTypeCode;
	}

	/**
	 * @param itemTypeCode
	 *            the itemTypeCode to set
	 */
	public void setItemTypeCode(Integer itemTypeCode) {
		this.itemTypeCode = itemTypeCode;
	}

	/**
	 * @return the beginDateTime
	 * @throws ParseException
	 */
	public Date getBeginDateTime() throws ParseException {
		if (beginDateTime == null && beginDateTimeString != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
			try {
				beginDateTime = sdf.parse(beginDateTimeString);
			} catch (ParseException e) {
				throw e;
			}
		}
		return beginDateTime;
	}

	/**
	 * @param beginDateTime
	 *            the beginDateTime to set
	 */
	public void setBeginDateTime(Date beginDateTime) {
		this.beginDateTime = beginDateTime;
	}

	/**
	 * @return the voidFlag
	 */
	public Boolean getVoidFlag() {
		return voidFlag;
	}

	/**
	 * @param voidFlag
	 *            the voidFlag to set
	 */
	public void setVoidFlag(Boolean voidFlag) {
		this.voidFlag = voidFlag;
	}

	/**
	 * @return the endDateTime
	 * @throws ParseException
	 */
	public Date getEndDateTime() throws ParseException {
		if (endDateTime == null && endDateTimeString != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
			try {
				endDateTime = sdf.parse(endDateTimeString);
			} catch (ParseException e) {
				throw e;
			}
		}
		return endDateTime;
	}

	/**
	 * @param endDateTime
	 *            the endDateTime to set
	 */
	public void setEndDateTime(Date endDateTime) {
		this.endDateTime = endDateTime;
	}

	/**
	 * @return the entryMethod
	 */
	public String getEntryMethod() {
		return entryMethod;
	}

	/**
	 * @param entryMethod
	 *            the entryMethod to set
	 */
	public void setEntryMethod(String entryMethod) {
		this.entryMethod = entryMethod;
	}

	/**
	 * @return the saleLI
	 */
	public SaleReturnLineItem getSaleLI() {
		if (saleLI != null) {
			saleLI.setTransactionID(transactionID);
			saleLI.setSequenceNumber(sequenceNumber);
		}
		return saleLI;
	}

	/**
	 * @param saleLI
	 *            the saleLI to set
	 */
	public void setSaleLI(SaleReturnLineItem saleLI) {
		this.saleLI = saleLI;
	}

	/**
	 * @return the returnLI
	 */
	public SaleReturnLineItem getReturnLI() {
		if (returnLI != null) {
			returnLI.setTransactionID(transactionID);
			returnLI.setSequenceNumber(sequenceNumber);
		}
		return returnLI;
	}

	/**
	 * @param returnLI
	 *            the returnLI to set
	 */
	public void setReturnLI(SaleReturnLineItem returnLI) {
		this.returnLI = returnLI;
	}

	/**
	 * @return the tender
	 */
	public TenderLineItem getTender() {
		if (tender != null) {
			tender.setTransactionID(transactionID);
			tender.setSequenceNumber(sequenceNumber);
		}
		return tender;
	}

	/**
	 * @param tender
	 *            the tender to set
	 */
	public void setTender(TenderLineItem tender) {
		this.tender = tender;
	}

	/**
	 * @return the priceModification
	 */
	public PriceModificationLineItem getPriceModification() {
		if (priceModification != null) {
			priceModification.setTransactionID(transactionID);
			priceModification.setSequenceNumber(sequenceNumber);
		}
		return priceModification;
	}

	/**
	 * @param priceModification
	 *            the priceModification to set
	 */
	public void setPriceModification(PriceModificationLineItem priceModification) {
		this.priceModification = priceModification;
	}

	/**
	 * @return the tax
	 */
	public TaxLineItem getTax() {
		if (tax != null) {
			tax.setTransactionID(transactionID);
			tax.setSequenceNumber(sequenceNumber);
		}
		return tax;
	}

	/**
	 * @param tax
	 *            the tax to set
	 */
	public void setTax(TaxLineItem tax) {
		this.tax = tax;
	}

	/**
	 * @return the miscellaneousFee
	 */
	public MiscellaneousFeeLineItem getMiscellaneousFee() {
		return miscellaneousFee;
	}

	/**
	 * @param miscellaneousFee
	 *            the miscellaneousFee to set
	 */
	public void setMiscellaneousFee(MiscellaneousFeeLineItem miscellaneousFee) {
		this.miscellaneousFee = miscellaneousFee;
	}

	/**
	 * @return the beginDateTimeString
	 */
	public String getBeginDateTimeString() {
		return beginDateTimeString;
	}

	/**
	 * @param beginDateTimeString
	 *            the beginDateTimeString to set
	 */
	public void setBeginDateTimeString(String beginDateTimeString) {
		this.beginDateTimeString = beginDateTimeString;
	}

	/**
	 * @return the endDateTimeString
	 */
	public String getEndDateTimeString() {
		return endDateTimeString;
	}

	/**
	 * @param endDateTimeString
	 *            the endDateTimeString to set
	 */
	public void setEndDateTimeString(String endDateTimeString) {
		this.endDateTimeString = endDateTimeString;
	}

	/**
	 * @return the voidLine
	 */
	public Integer getVoidLine() {
		return voidLine;
	}

	/**
	 * @param voidLine
	 *            the voidLine to set
	 */
	public void setVoidLine(Integer voidLine) {
		this.voidLine = voidLine;
	}

}
