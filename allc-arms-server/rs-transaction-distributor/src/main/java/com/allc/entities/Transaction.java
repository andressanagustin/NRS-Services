/**
 * 
 */
package com.allc.entities;

import java.text.ParseException;
import java.util.Date;

import com.allc.util.ConstantsUtil;

/**
 * 
 * @author GUSTAVOK
 * 
 */
public class Transaction {
	private Integer transactionID;
	private Operator operator;
	private String operatorCode;
	private Workstation workstation;
	private String workstationCode;
	private RetailStore retailStore;
	private String retailStoreCode;
	private Date businessDayDate;
	private String businessDayDateString;
	private Integer period;
	private Integer subperiod;
	private Integer sequenceNumber;
	private Integer transactionTypeCode;
	private Date beginDateTime;
	private String beginDateTimeString;
	private Date endDateTime;
	private String endDateTimeString;
	private Boolean trainingModeFlag;
	private Boolean keyedOfflineFlag;
	private Boolean cancelFlag;
	private Boolean voidedFlag;
	private Boolean suspendedFlag;
	/**
	 * De las variables que se encuentran definidas debajo, solo una puede ser
	 * distinta de null
	 */
	private RetailTransaction retailTransaction;
	private TenderControlTransaction tenderControlTransaction;
	private ControlTransaction controlTransaction;

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
	 * @return the businessDayDate
	 * @throws ParseException
	 */
	public Date getBusinessDayDate() throws ParseException {
		if (businessDayDate == null && businessDayDateString != null) {
			try {
				businessDayDate = ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER
						.parse(businessDayDateString);
			} catch (ParseException e) {
				throw e;
			}
		}
		return businessDayDate;
	}

	/**
	 * @param businessDayDate
	 *            the businessDayDate to set
	 */
	public void setBusinessDayDate(Date businessDayDate) {
		this.businessDayDate = businessDayDate;
	}

	/**
	 * @return the sequenceNumber
	 */
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * @return the period
	 */
	public Integer getPeriod() {
		if (period == null)
			period = new Integer(1);
		return period;
	}

	/**
	 * @param period
	 *            the period to set
	 */
	public void setPeriod(Integer period) {
		this.period = period;
	}

	/**
	 * @return the subperiod
	 */
	public Integer getSubperiod() {
		if (subperiod == null)
			subperiod = new Integer(1);
		return subperiod;
	}

	/**
	 * @param subperiod
	 *            the subperiod to set
	 */
	public void setSubperiod(Integer subperiod) {
		this.subperiod = subperiod;
	}

	/**
	 * @param sequenceNumber
	 *            the sequenceNumber to set
	 */
	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	/**
	 * @return the transactionTypeCode
	 */
	public Integer getTransactionTypeCode() {
		return transactionTypeCode;
	}

	/**
	 * @param transactionTypeCode
	 *            the transactionTypeCode to set
	 */
	public void setTransactionTypeCode(Integer transactionTypeCode) {
		this.transactionTypeCode = transactionTypeCode;
	}

	/**
	 * @return the beginDateTime
	 * @throws ParseException
	 */
	public Date getBeginDateTime() throws ParseException {
		if (beginDateTime == null && beginDateTimeString != null) {
			try {
				beginDateTime = ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER
						.parse(beginDateTimeString);
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
	 * @param beginDateTimestampString
	 *            The beginDateTimestampString to set.
	 * @throws Exception
	 *             If a ParseException occurs.
	 */
	public void setBeginDateTimeString(String beginDateTimestampString)
			throws Exception {
		this.beginDateTimeString = beginDateTimestampString;
	}

	/**
	 * @return the endDateTime
	 * @throws ParseException
	 */
	public Date getEndDateTime() throws ParseException {
		if (endDateTime == null && endDateTimeString != null) {
			try {
				endDateTime = ConstantsUtil.Formatters.IXRETAIL_DATE_TIME_FORMATTER
						.parse(endDateTimeString);
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
	 * @return the businessDayDateString
	 */
	public String getBusinessDayDateString() {
		return businessDayDateString;
	}

	/**
	 * @param businessDayDateString
	 *            the businessDayDateString to set
	 */
	public void setBusinessDayDateString(String businessDayDateString) {
		this.businessDayDateString = businessDayDateString;
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
	 * @return the trainingModeFlag
	 */
	public Boolean getTrainingModeFlag() {
		return trainingModeFlag;
	}

	/**
	 * @param trainingModeFlag
	 *            the trainingModeFlag to set
	 */
	public void setTrainingModeFlag(Boolean trainingModeFlag) {
		this.trainingModeFlag = trainingModeFlag;
	}

	/**
	 * @return the keyedOfflineFlag
	 */
	public Boolean getKeyedOfflineFlag() {
		return keyedOfflineFlag;
	}

	/**
	 * @param keyedOfflineFlag
	 *            the keyedOfflineFlag to set
	 */
	public void setKeyedOfflineFlag(Boolean keyedOfflineFlag) {
		this.keyedOfflineFlag = keyedOfflineFlag;
	}

	/**
	 * @return the cancelFlag
	 */
	public Boolean getCancelFlag() {
		return cancelFlag;
	}

	/**
	 * @param cancelFlag
	 *            the cancelFlag to set
	 */
	public void setCancelFlag(Boolean cancelFlag) {
		this.cancelFlag = cancelFlag;
	}

	/**
	 * @return the voidedFlag
	 */
	public Boolean getVoidedFlag() {
		return voidedFlag;
	}

	/**
	 * @param voidedFlag
	 *            the voidedFlag to set
	 */
	public void setVoidedFlag(Boolean voidedFlag) {
		this.voidedFlag = voidedFlag;
	}

	/**
	 * @return the suspendedFlag
	 */
	public Boolean getSuspendedFlag() {
		return suspendedFlag;
	}

	/**
	 * @param suspendedFlag
	 *            the suspendedFlag to set
	 */
	public void setSuspendedFlag(Boolean suspendedFlag) {
		this.suspendedFlag = suspendedFlag;
	}

	/**
	 * @return the retailTransaction
	 */
	public RetailTransaction getRetailTransaction() {
		if (retailTransaction != null)
			retailTransaction.setTransactionID(transactionID);
		return retailTransaction;
	}

	/**
	 * @param retailTransaction
	 *            the retailTransaction to set
	 */
	public void setRetailTransaction(RetailTransaction retailTransaction) {
		this.retailTransaction = retailTransaction;
	}

	/**
	 * @return the beginDateTimeString
	 */
	public String getBeginDateTimeString() {
		return beginDateTimeString;
	}

	/**
	 * @return the tenderControlTransaction
	 */
	public TenderControlTransaction getTenderControlTransaction() {
		if (tenderControlTransaction != null)
			tenderControlTransaction.setTransactionID(transactionID);
		return tenderControlTransaction;
	}

	/**
	 * @param tenderControlTransaction
	 *            the tenderControlTransaction to set
	 */
	public void setTenderControlTransaction(
			TenderControlTransaction tenderControlTransaction) {
		this.tenderControlTransaction = tenderControlTransaction;
	}

	/**
	 * @return the controlTransaction
	 */
	public ControlTransaction getControlTransaction() {
		if (controlTransaction != null)
			controlTransaction.setTransactionID(transactionID);
		return controlTransaction;
	}

	/**
	 * @param controlTransaction
	 *            the controlTransaction to set
	 */
	public void setControlTransaction(ControlTransaction controlTransaction) {
		this.controlTransaction = controlTransaction;
	}

	/**
	 * @return the operator
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * @param operator
	 *            the operator to set
	 */
	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	/**
	 * @return the operatorCode
	 */
	public String getOperatorCode() {
		return operatorCode;
	}

	/**
	 * @param operatorCode
	 *            the operatorCode to set
	 */
	public void setOperatorCode(String operatorCode) {
		this.operatorCode = operatorCode;
	}

	/**
	 * @return the workstation
	 */
	public Workstation getWorkstation() {
		return workstation;
	}

	/**
	 * @param workstation
	 *            the workstation to set
	 */
	public void setWorkstation(Workstation workstation) {
		this.workstation = workstation;
	}

	/**
	 * @return the workstationCode
	 */
	public String getWorkstationCode() {
		return workstationCode;
	}

	/**
	 * @param workstationCode
	 *            the workstationCode to set
	 */
	public void setWorkstationCode(String workstationCode) {
		this.workstationCode = workstationCode;
	}

	/**
	 * @return the retailStoreCode
	 */
	public String getRetailStoreCode() {
		return retailStoreCode;
	}

	/**
	 * @param retailStoreCode
	 *            the retailStoreCode to set
	 */
	public void setRetailStoreCode(String retailStoreCode) {
		this.retailStoreCode = retailStoreCode;
	}

	/**
	 * @return the retailStore
	 */
	public RetailStore getRetailStore() {
		return retailStore;
	}

	/**
	 * @param retailStore
	 *            the retailStore to set
	 */
	public void setRetailStore(RetailStore retailStore) {
		this.retailStore = retailStore;
	}

}
