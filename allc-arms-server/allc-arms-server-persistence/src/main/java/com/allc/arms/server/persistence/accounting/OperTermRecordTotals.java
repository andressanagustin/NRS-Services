package com.allc.arms.server.persistence.accounting;

import java.io.Serializable;
import java.util.Date;

public class OperTermRecordTotals implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer operTermRecordID;
	private String recordType;
	private Integer storeCode;
	private Integer accountId;
	private Date storeTimeStamp;
	private Date timeStamp;
	private Integer indicat0;
	private Integer trxNumber;
	private Integer restart;
	private Integer grossPlus;
	private Integer grossMinus;
	private Integer salesTransactionCount;
	private Integer lonAmtCash;
	private Integer lonAmtCheck;
	private Integer lonAmtFoods;
	private Integer lonAmtMisc1;
	private Integer lonAmtMisc2;
	private Integer lonAmtMisc3;
	private Integer lonAmtManuf;
	private Integer lonAmtStore;
	private Integer pkpAmtCash;
	private Integer pkpAmtCheck;
	private Integer pkpAmtFoods;
	private Integer pkpAmtMisc1;
	private Integer pkpAmtMisc2;
	private Integer pkpAmtMisc3;
	private Integer pkpAmtManuf;
	private Integer pkpAmtStore;
	private Integer cntTndAmtCash;
	private Integer cntTndAmtCheck;
	private Integer cntTndAmtFoods;
	private Integer cntTndAmtMisc1;
	private Integer cntTndAmtMisc2;
	private Integer cntTndAmtMisc3;
	private Integer cntTndAmtManuf;
	private Integer cntTndAmtStore;
	private Integer netTndAmtCash;
	private Integer netTndAmtCheck;
	private Integer netTndAmtFoods;
	private Integer netTndAmtMisc1;
	private Integer netTndAmtMisc2;
	private Integer netTndAmtMisc3;
	private Integer netTndAmtManuf;
	private Integer netTndAmtStore;
	private Integer itemSalesAmount;
	private Integer depositAmount;
	private Integer refundAmount;
	private Integer depositReturnAmount;
	private Integer miscReceiptAmount;
	private Integer miscPayoutAmount;
	private Integer discountAmount;
	private Integer taxableAmountExempt;
	private Integer itemCancelAmount;
	private Integer depositCancelAmount;
	private Integer creditTransactionAmount;
	private Integer tenderFeeAmount;
	private Integer miscTransactionAmount;
	private Integer tenderCashingAmount;
	private Integer tenderExchangeAmount;
	private Integer taxableAmountA;
	private Integer taxAmountA;
	private Integer taxableAmountB;
	private Integer taxAmountB;
	private Integer taxableAmountC;
	private Integer taxAmountC;
	private Integer taxableAmountD;
	private Integer taxAmountD;
	private Integer standaloneGrossPlus;
	private Integer standaloneGrossMinus;
	private Integer voidTransactionAmount;
	private Integer trainingTransactionAmount;
	private Integer itemSalesCount;
	private Integer itemSalesKeyedCount;
	private Integer itemSalesLookupKeysCount;
	private Integer tradingStamps;
	private Integer depositCount;
	private Integer refundCount;
	private Integer depositReturnCount;
	private Integer miscReceiptCount;
	private Integer miscPayoutCount;
	private Integer discountCount;
	private Integer taxExemptionCount;
	private Integer itemCancelCount;
	private Integer depositCancelCount;
	private Integer creditTransactionCount;
	private Integer specialSignOffCount;
	private Integer noSaleTransactionCount;
	private Integer netTndNumCash;
	private Integer netTndNumCheck;
	private Integer netTndNumFoods;
	private Integer netTndNumMisc1;
	private Integer netTndNumMisc2;
	private Integer netTndNumMisc3;
	private Integer netTndNumManuf;
	private Integer netTndNumStore;
	private Integer loanCount;
	private Integer pickupCount;
	private Integer standaloneTransactionCount;
	private Integer voidTransactionCount;
	private Integer trainingTransactionCount;
	private Integer standaloneTaxAmount;
	private Integer taxableAmountE;
	private Integer taxAmountE;
	private Integer taxableAmountF;
	private Integer taxAmountF;
	private Integer taxableAmountG;
	private Integer taxAmountG;
	private Integer taxableAmountH;
	private Integer taxAmountH;
	private Integer reserved;
	private Integer salesPoints;
	private Integer bonusPoints;
	private Integer redeemedPoints;
	private Integer manAutoCouponAmount;
	private Integer storeAutoCouponAmount;
	private Integer doubledCouponAmount;
	private Integer PCTransactionAmount;
	private Integer PCTransactionCount;
	private Integer PCAutoCouponCount;
	private Integer PCAutoCouponAmount;
	private Integer couponTier1Amount;
	private Integer couponTier2Amount;
	private Integer couponTier3Amount;
	private Integer couponTier4Amount;
	private Integer couponTier5Amount;
	private Integer couponTier6Amount;
	private Integer couponTier1Count;
	private Integer couponTier2Count;
	private Integer couponTier3Count;
	private Integer couponTier4Count;
	private Integer couponTier5Count;
	private Integer couponTier6Count;
	private String usrInteger;
	private String usrReserved;
	private String typPrd;
	
	public Integer getOperTermRecordID() {
		return operTermRecordID;
	}
	public void setOperTermRecordID(Integer operTermRecordID) {
		this.operTermRecordID = operTermRecordID;
	}
	public String getRecordType() {
		return recordType;
	}
	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}
	
	/**
	 * @return the storeCode
	 */
	public Integer getStoreCode() {
		return storeCode;
	}
	/**
	 * @param storeCode the storeCode to set
	 */
	public void setStoreCode(Integer storeCode) {
		this.storeCode = storeCode;
	}
	public Integer getAccountId() {
		return accountId;
	}
	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}
	
	/**
	 * @return the storeTimeStamp
	 */
	public Date getStoreTimeStamp() {
		return storeTimeStamp;
	}
	/**
	 * @param storeTimeStamp the storeTimeStamp to set
	 */
	public void setStoreTimeStamp(Date storeTimeStamp) {
		this.storeTimeStamp = storeTimeStamp;
	}
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	public Integer getIndicat0() {
		return indicat0;
	}
	public void setIndicat0(Integer indicat0) {
		this.indicat0 = indicat0;
	}
	public Integer getTrxNumber() {
		return trxNumber;
	}
	public void setTrxNumber(Integer trxNumber) {
		this.trxNumber = trxNumber;
	}
	public Integer getRestart() {
		return restart;
	}
	public void setRestart(Integer restart) {
		this.restart = restart;
	}
	public Integer getGrossPlus() {
		return grossPlus;
	}
	public void setGrossPlus(Integer grossPlus) {
		this.grossPlus = grossPlus;
	}
	public Integer getGrossMinus() {
		return grossMinus;
	}
	public void setGrossMinus(Integer grossMinus) {
		this.grossMinus = grossMinus;
	}
	public Integer getSalesTransactionCount() {
		return salesTransactionCount;
	}
	public void setSalesTransactionCount(Integer salesTransactionCount) {
		this.salesTransactionCount = salesTransactionCount;
	}
	public Integer getLonAmtCash() {
		return lonAmtCash;
	}
	public void setLonAmtCash(Integer lonAmtCash) {
		this.lonAmtCash = lonAmtCash;
	}
	public Integer getLonAmtCheck() {
		return lonAmtCheck;
	}
	public void setLonAmtCheck(Integer lonAmtCheck) {
		this.lonAmtCheck = lonAmtCheck;
	}
	public Integer getLonAmtFoods() {
		return lonAmtFoods;
	}
	public void setLonAmtFoods(Integer lonAmtFoods) {
		this.lonAmtFoods = lonAmtFoods;
	}
	public Integer getLonAmtMisc1() {
		return lonAmtMisc1;
	}
	public void setLonAmtMisc1(Integer lonAmtMisc1) {
		this.lonAmtMisc1 = lonAmtMisc1;
	}
	public Integer getLonAmtMisc2() {
		return lonAmtMisc2;
	}
	public void setLonAmtMisc2(Integer lonAmtMisc2) {
		this.lonAmtMisc2 = lonAmtMisc2;
	}
	public Integer getLonAmtMisc3() {
		return lonAmtMisc3;
	}
	public void setLonAmtMisc3(Integer lonAmtMisc3) {
		this.lonAmtMisc3 = lonAmtMisc3;
	}
	public Integer getLonAmtManuf() {
		return lonAmtManuf;
	}
	public void setLonAmtManuf(Integer lonAmtManuf) {
		this.lonAmtManuf = lonAmtManuf;
	}
	public Integer getLonAmtStore() {
		return lonAmtStore;
	}
	public void setLonAmtStore(Integer lonAmtStore) {
		this.lonAmtStore = lonAmtStore;
	}
	public Integer getPkpAmtCash() {
		return pkpAmtCash;
	}
	public void setPkpAmtCash(Integer pkpAmtCash) {
		this.pkpAmtCash = pkpAmtCash;
	}
	public Integer getPkpAmtCheck() {
		return pkpAmtCheck;
	}
	public void setPkpAmtCheck(Integer pkpAmtCheck) {
		this.pkpAmtCheck = pkpAmtCheck;
	}
	public Integer getPkpAmtFoods() {
		return pkpAmtFoods;
	}
	public void setPkpAmtFoods(Integer pkpAmtFoods) {
		this.pkpAmtFoods = pkpAmtFoods;
	}
	public Integer getPkpAmtMisc1() {
		return pkpAmtMisc1;
	}
	public void setPkpAmtMisc1(Integer pkpAmtMisc1) {
		this.pkpAmtMisc1 = pkpAmtMisc1;
	}
	public Integer getPkpAmtMisc2() {
		return pkpAmtMisc2;
	}
	public void setPkpAmtMisc2(Integer pkpAmtMisc2) {
		this.pkpAmtMisc2 = pkpAmtMisc2;
	}
	public Integer getPkpAmtMisc3() {
		return pkpAmtMisc3;
	}
	public void setPkpAmtMisc3(Integer pkpAmtMisc3) {
		this.pkpAmtMisc3 = pkpAmtMisc3;
	}
	public Integer getPkpAmtManuf() {
		return pkpAmtManuf;
	}
	public void setPkpAmtManuf(Integer pkpAmtManuf) {
		this.pkpAmtManuf = pkpAmtManuf;
	}
	public Integer getPkpAmtStore() {
		return pkpAmtStore;
	}
	public void setPkpAmtStore(Integer pkpAmtStore) {
		this.pkpAmtStore = pkpAmtStore;
	}
	public Integer getCntTndAmtCash() {
		return cntTndAmtCash;
	}
	public void setCntTndAmtCash(Integer cntTndAmtCash) {
		this.cntTndAmtCash = cntTndAmtCash;
	}
	public Integer getCntTndAmtCheck() {
		return cntTndAmtCheck;
	}
	public void setCntTndAmtCheck(Integer cntTndAmtCheck) {
		this.cntTndAmtCheck = cntTndAmtCheck;
	}
	public Integer getCntTndAmtFoods() {
		return cntTndAmtFoods;
	}
	public void setCntTndAmtFoods(Integer cntTndAmtFoods) {
		this.cntTndAmtFoods = cntTndAmtFoods;
	}
	public Integer getCntTndAmtMisc1() {
		return cntTndAmtMisc1;
	}
	public void setCntTndAmtMisc1(Integer cntTndAmtMisc1) {
		this.cntTndAmtMisc1 = cntTndAmtMisc1;
	}
	public Integer getCntTndAmtMisc2() {
		return cntTndAmtMisc2;
	}
	public void setCntTndAmtMisc2(Integer cntTndAmtMisc2) {
		this.cntTndAmtMisc2 = cntTndAmtMisc2;
	}
	public Integer getCntTndAmtMisc3() {
		return cntTndAmtMisc3;
	}
	public void setCntTndAmtMisc3(Integer cntTndAmtMisc3) {
		this.cntTndAmtMisc3 = cntTndAmtMisc3;
	}
	public Integer getCntTndAmtManuf() {
		return cntTndAmtManuf;
	}
	public void setCntTndAmtManuf(Integer cntTndAmtManuf) {
		this.cntTndAmtManuf = cntTndAmtManuf;
	}
	public Integer getCntTndAmtStore() {
		return cntTndAmtStore;
	}
	public void setCntTndAmtStore(Integer cntTndAmtStore) {
		this.cntTndAmtStore = cntTndAmtStore;
	}
	public Integer getNetTndAmtCash() {
		return netTndAmtCash;
	}
	public void setNetTndAmtCash(Integer netTndAmtCash) {
		this.netTndAmtCash = netTndAmtCash;
	}
	public Integer getNetTndAmtCheck() {
		return netTndAmtCheck;
	}
	public void setNetTndAmtCheck(Integer netTndAmtCheck) {
		this.netTndAmtCheck = netTndAmtCheck;
	}
	public Integer getNetTndAmtFoods() {
		return netTndAmtFoods;
	}
	public void setNetTndAmtFoods(Integer netTndAmtFoods) {
		this.netTndAmtFoods = netTndAmtFoods;
	}
	public Integer getNetTndAmtMisc1() {
		return netTndAmtMisc1;
	}
	public void setNetTndAmtMisc1(Integer netTndAmtMisc1) {
		this.netTndAmtMisc1 = netTndAmtMisc1;
	}
	public Integer getNetTndAmtMisc2() {
		return netTndAmtMisc2;
	}
	public void setNetTndAmtMisc2(Integer netTndAmtMisc2) {
		this.netTndAmtMisc2 = netTndAmtMisc2;
	}
	public Integer getNetTndAmtMisc3() {
		return netTndAmtMisc3;
	}
	public void setNetTndAmtMisc3(Integer netTndAmtMisc3) {
		this.netTndAmtMisc3 = netTndAmtMisc3;
	}
	public Integer getNetTndAmtManuf() {
		return netTndAmtManuf;
	}
	public void setNetTndAmtManuf(Integer netTndAmtManuf) {
		this.netTndAmtManuf = netTndAmtManuf;
	}
	public Integer getNetTndAmtStore() {
		return netTndAmtStore;
	}
	public void setNetTndAmtStore(Integer netTndAmtStore) {
		this.netTndAmtStore = netTndAmtStore;
	}
	public Integer getItemSalesAmount() {
		return itemSalesAmount;
	}
	public void setItemSalesAmount(Integer itemSalesAmount) {
		this.itemSalesAmount = itemSalesAmount;
	}
	public Integer getDepositAmount() {
		return depositAmount;
	}
	public void setDepositAmount(Integer depositAmount) {
		this.depositAmount = depositAmount;
	}
	public Integer getRefundAmount() {
		return refundAmount;
	}
	public void setRefundAmount(Integer refundAmount) {
		this.refundAmount = refundAmount;
	}
	public Integer getDepositReturnAmount() {
		return depositReturnAmount;
	}
	public void setDepositReturnAmount(Integer depositReturnAmount) {
		this.depositReturnAmount = depositReturnAmount;
	}
	public Integer getMiscReceiptAmount() {
		return miscReceiptAmount;
	}
	public void setMiscReceiptAmount(Integer miscReceiptAmount) {
		this.miscReceiptAmount = miscReceiptAmount;
	}
	public Integer getMiscPayoutAmount() {
		return miscPayoutAmount;
	}
	public void setMiscPayoutAmount(Integer miscPayoutAmount) {
		this.miscPayoutAmount = miscPayoutAmount;
	}
	public Integer getDiscountAmount() {
		return discountAmount;
	}
	public void setDiscountAmount(Integer discountAmount) {
		this.discountAmount = discountAmount;
	}
	public Integer getTaxableAmountExempt() {
		return taxableAmountExempt;
	}
	public void setTaxableAmountExempt(Integer taxableAmountExempt) {
		this.taxableAmountExempt = taxableAmountExempt;
	}
	public Integer getItemCancelAmount() {
		return itemCancelAmount;
	}
	public void setItemCancelAmount(Integer itemCancelAmount) {
		this.itemCancelAmount = itemCancelAmount;
	}
	public Integer getDepositCancelAmount() {
		return depositCancelAmount;
	}
	public void setDepositCancelAmount(Integer depositCancelAmount) {
		this.depositCancelAmount = depositCancelAmount;
	}
	public Integer getCreditTransactionAmount() {
		return creditTransactionAmount;
	}
	public void setCreditTransactionAmount(Integer creditTransactionAmount) {
		this.creditTransactionAmount = creditTransactionAmount;
	}
	public Integer getTenderFeeAmount() {
		return tenderFeeAmount;
	}
	public void setTenderFeeAmount(Integer tenderFeeAmount) {
		this.tenderFeeAmount = tenderFeeAmount;
	}
	public Integer getMiscTransactionAmount() {
		return miscTransactionAmount;
	}
	public void setMiscTransactionAmount(Integer miscTransactionAmount) {
		this.miscTransactionAmount = miscTransactionAmount;
	}
	public Integer getTenderCashingAmount() {
		return tenderCashingAmount;
	}
	public void setTenderCashingAmount(Integer tenderCashingAmount) {
		this.tenderCashingAmount = tenderCashingAmount;
	}
	public Integer getTenderExchangeAmount() {
		return tenderExchangeAmount;
	}
	public void setTenderExchangeAmount(Integer tenderExchangeAmount) {
		this.tenderExchangeAmount = tenderExchangeAmount;
	}
	public Integer getTaxableAmountA() {
		return taxableAmountA;
	}
	public void setTaxableAmountA(Integer taxableAmountA) {
		this.taxableAmountA = taxableAmountA;
	}
	public Integer getTaxAmountA() {
		return taxAmountA;
	}
	public void setTaxAmountA(Integer taxAmountA) {
		this.taxAmountA = taxAmountA;
	}
	public Integer getTaxableAmountB() {
		return taxableAmountB;
	}
	public void setTaxableAmountB(Integer taxableAmountB) {
		this.taxableAmountB = taxableAmountB;
	}
	public Integer getTaxAmountB() {
		return taxAmountB;
	}
	public void setTaxAmountB(Integer taxAmountB) {
		this.taxAmountB = taxAmountB;
	}
	public Integer getTaxableAmountC() {
		return taxableAmountC;
	}
	public void setTaxableAmountC(Integer taxableAmountC) {
		this.taxableAmountC = taxableAmountC;
	}
	public Integer getTaxAmountC() {
		return taxAmountC;
	}
	public void setTaxAmountC(Integer taxAmountC) {
		this.taxAmountC = taxAmountC;
	}
	public Integer getTaxableAmountD() {
		return taxableAmountD;
	}
	public void setTaxableAmountD(Integer taxableAmountD) {
		this.taxableAmountD = taxableAmountD;
	}
	public Integer getTaxAmountD() {
		return taxAmountD;
	}
	public void setTaxAmountD(Integer taxAmountD) {
		this.taxAmountD = taxAmountD;
	}
	public Integer getStandaloneGrossPlus() {
		return standaloneGrossPlus;
	}
	public void setStandaloneGrossPlus(Integer standaloneGrossPlus) {
		this.standaloneGrossPlus = standaloneGrossPlus;
	}
	public Integer getStandaloneGrossMinus() {
		return standaloneGrossMinus;
	}
	public void setStandaloneGrossMinus(Integer standaloneGrossMinus) {
		this.standaloneGrossMinus = standaloneGrossMinus;
	}
	public Integer getVoidTransactionAmount() {
		return voidTransactionAmount;
	}
	public void setVoidTransactionAmount(Integer voidTransactionAmount) {
		this.voidTransactionAmount = voidTransactionAmount;
	}
	public Integer getTrainingTransactionAmount() {
		return trainingTransactionAmount;
	}
	public void setTrainingTransactionAmount(Integer trainingTransactionAmount) {
		this.trainingTransactionAmount = trainingTransactionAmount;
	}
	public Integer getItemSalesCount() {
		return itemSalesCount;
	}
	public void setItemSalesCount(Integer itemSalesCount) {
		this.itemSalesCount = itemSalesCount;
	}
	public Integer getItemSalesKeyedCount() {
		return itemSalesKeyedCount;
	}
	public void setItemSalesKeyedCount(Integer itemSalesKeyedCount) {
		this.itemSalesKeyedCount = itemSalesKeyedCount;
	}
	public Integer getItemSalesLookupKeysCount() {
		return itemSalesLookupKeysCount;
	}
	public void setItemSalesLookupKeysCount(Integer itemSalesLookupKeysCount) {
		this.itemSalesLookupKeysCount = itemSalesLookupKeysCount;
	}
	public Integer getTradingStamps() {
		return tradingStamps;
	}
	public void setTradingStamps(Integer tradingStamps) {
		this.tradingStamps = tradingStamps;
	}
	public Integer getDepositCount() {
		return depositCount;
	}
	public void setDepositCount(Integer depositCount) {
		this.depositCount = depositCount;
	}
	public Integer getRefundCount() {
		return refundCount;
	}
	public void setRefundCount(Integer refundCount) {
		this.refundCount = refundCount;
	}
	public Integer getDepositReturnCount() {
		return depositReturnCount;
	}
	public void setDepositReturnCount(Integer depositReturnCount) {
		this.depositReturnCount = depositReturnCount;
	}
	public Integer getMiscReceiptCount() {
		return miscReceiptCount;
	}
	public void setMiscReceiptCount(Integer miscReceiptCount) {
		this.miscReceiptCount = miscReceiptCount;
	}
	public Integer getMiscPayoutCount() {
		return miscPayoutCount;
	}
	public void setMiscPayoutCount(Integer miscPayoutCount) {
		this.miscPayoutCount = miscPayoutCount;
	}
	public Integer getDiscountCount() {
		return discountCount;
	}
	public void setDiscountCount(Integer discountCount) {
		this.discountCount = discountCount;
	}
	public Integer getTaxExemptionCount() {
		return taxExemptionCount;
	}
	public void setTaxExemptionCount(Integer taxExemptionCount) {
		this.taxExemptionCount = taxExemptionCount;
	}
	public Integer getItemCancelCount() {
		return itemCancelCount;
	}
	public void setItemCancelCount(Integer itemCancelCount) {
		this.itemCancelCount = itemCancelCount;
	}
	public Integer getDepositCancelCount() {
		return depositCancelCount;
	}
	public void setDepositCancelCount(Integer depositCancelCount) {
		this.depositCancelCount = depositCancelCount;
	}
	public Integer getCreditTransactionCount() {
		return creditTransactionCount;
	}
	public void setCreditTransactionCount(Integer creditTransactionCount) {
		this.creditTransactionCount = creditTransactionCount;
	}
	public Integer getSpecialSignOffCount() {
		return specialSignOffCount;
	}
	public void setSpecialSignOffCount(Integer specialSignOffCount) {
		this.specialSignOffCount = specialSignOffCount;
	}
	public Integer getNoSaleTransactionCount() {
		return noSaleTransactionCount;
	}
	public void setNoSaleTransactionCount(Integer noSaleTransactionCount) {
		this.noSaleTransactionCount = noSaleTransactionCount;
	}
	public Integer getNetTndNumCash() {
		return netTndNumCash;
	}
	public void setNetTndNumCash(Integer netTndNumCash) {
		this.netTndNumCash = netTndNumCash;
	}
	public Integer getNetTndNumCheck() {
		return netTndNumCheck;
	}
	public void setNetTndNumCheck(Integer netTndNumCheck) {
		this.netTndNumCheck = netTndNumCheck;
	}
	public Integer getNetTndNumFoods() {
		return netTndNumFoods;
	}
	public void setNetTndNumFoods(Integer netTndNumFoods) {
		this.netTndNumFoods = netTndNumFoods;
	}
	public Integer getNetTndNumMisc1() {
		return netTndNumMisc1;
	}
	public void setNetTndNumMisc1(Integer netTndNumMisc1) {
		this.netTndNumMisc1 = netTndNumMisc1;
	}
	public Integer getNetTndNumMisc2() {
		return netTndNumMisc2;
	}
	public void setNetTndNumMisc2(Integer netTndNumMisc2) {
		this.netTndNumMisc2 = netTndNumMisc2;
	}
	public Integer getNetTndNumMisc3() {
		return netTndNumMisc3;
	}
	public void setNetTndNumMisc3(Integer netTndNumMisc3) {
		this.netTndNumMisc3 = netTndNumMisc3;
	}
	public Integer getNetTndNumManuf() {
		return netTndNumManuf;
	}
	public void setNetTndNumManuf(Integer netTndNumManuf) {
		this.netTndNumManuf = netTndNumManuf;
	}
	public Integer getNetTndNumStore() {
		return netTndNumStore;
	}
	public void setNetTndNumStore(Integer netTndNumStore) {
		this.netTndNumStore = netTndNumStore;
	}
	public Integer getLoanCount() {
		return loanCount;
	}
	public void setLoanCount(Integer loanCount) {
		this.loanCount = loanCount;
	}
	public Integer getPickupCount() {
		return pickupCount;
	}
	public void setPickupCount(Integer pickupCount) {
		this.pickupCount = pickupCount;
	}
	public Integer getStandaloneTransactionCount() {
		return standaloneTransactionCount;
	}
	public void setStandaloneTransactionCount(Integer standaloneTransactionCount) {
		this.standaloneTransactionCount = standaloneTransactionCount;
	}
	public Integer getVoidTransactionCount() {
		return voidTransactionCount;
	}
	public void setVoidTransactionCount(Integer voidTransactionCount) {
		this.voidTransactionCount = voidTransactionCount;
	}
	public Integer getTrainingTransactionCount() {
		return trainingTransactionCount;
	}
	public void setTrainingTransactionCount(Integer trainingTransactionCount) {
		this.trainingTransactionCount = trainingTransactionCount;
	}
	public Integer getStandaloneTaxAmount() {
		return standaloneTaxAmount;
	}
	public void setStandaloneTaxAmount(Integer standaloneTaxAmount) {
		this.standaloneTaxAmount = standaloneTaxAmount;
	}
	public Integer getTaxableAmountE() {
		return taxableAmountE;
	}
	public void setTaxableAmountE(Integer taxableAmountE) {
		this.taxableAmountE = taxableAmountE;
	}
	public Integer getTaxAmountE() {
		return taxAmountE;
	}
	public void setTaxAmountE(Integer taxAmountE) {
		this.taxAmountE = taxAmountE;
	}
	public Integer getTaxableAmountF() {
		return taxableAmountF;
	}
	public void setTaxableAmountF(Integer taxableAmountF) {
		this.taxableAmountF = taxableAmountF;
	}
	public Integer getTaxAmountF() {
		return taxAmountF;
	}
	public void setTaxAmountF(Integer taxAmountF) {
		this.taxAmountF = taxAmountF;
	}
	public Integer getTaxableAmountG() {
		return taxableAmountG;
	}
	public void setTaxableAmountG(Integer taxableAmountG) {
		this.taxableAmountG = taxableAmountG;
	}
	public Integer getTaxAmountG() {
		return taxAmountG;
	}
	public void setTaxAmountG(Integer taxAmountG) {
		this.taxAmountG = taxAmountG;
	}
	public Integer getTaxableAmountH() {
		return taxableAmountH;
	}
	public void setTaxableAmountH(Integer taxableAmountH) {
		this.taxableAmountH = taxableAmountH;
	}
	public Integer getTaxAmountH() {
		return taxAmountH;
	}
	public void setTaxAmountH(Integer taxAmountH) {
		this.taxAmountH = taxAmountH;
	}
	public Integer getReserved() {
		return reserved;
	}
	public void setReserved(Integer reserved) {
		this.reserved = reserved;
	}
	public Integer getSalesPoints() {
		return salesPoints;
	}
	public void setSalesPoints(Integer salesPoints) {
		this.salesPoints = salesPoints;
	}
	public Integer getBonusPoints() {
		return bonusPoints;
	}
	public void setBonusPoints(Integer bonusPoints) {
		this.bonusPoints = bonusPoints;
	}
	public Integer getRedeemedPoints() {
		return redeemedPoints;
	}
	public void setRedeemedPoints(Integer redeemedPoints) {
		this.redeemedPoints = redeemedPoints;
	}
	public Integer getManAutoCouponAmount() {
		return manAutoCouponAmount;
	}
	public void setManAutoCouponAmount(Integer manAutoCouponAmount) {
		this.manAutoCouponAmount = manAutoCouponAmount;
	}
	public Integer getStoreAutoCouponAmount() {
		return storeAutoCouponAmount;
	}
	public void setStoreAutoCouponAmount(Integer storeAutoCouponAmount) {
		this.storeAutoCouponAmount = storeAutoCouponAmount;
	}
	public Integer getDoubledCouponAmount() {
		return doubledCouponAmount;
	}
	public void setDoubledCouponAmount(Integer doubledCouponAmount) {
		this.doubledCouponAmount = doubledCouponAmount;
	}
	public Integer getPCTransactionAmount() {
		return PCTransactionAmount;
	}
	public void setPCTransactionAmount(Integer pCTransactionAmount) {
		PCTransactionAmount = pCTransactionAmount;
	}
	public Integer getPCTransactionCount() {
		return PCTransactionCount;
	}
	public void setPCTransactionCount(Integer pCTransactionCount) {
		PCTransactionCount = pCTransactionCount;
	}
	public Integer getPCAutoCouponCount() {
		return PCAutoCouponCount;
	}
	public void setPCAutoCouponCount(Integer pCAutoCouponCount) {
		PCAutoCouponCount = pCAutoCouponCount;
	}
	public Integer getPCAutoCouponAmount() {
		return PCAutoCouponAmount;
	}
	public void setPCAutoCouponAmount(Integer pCAutoCouponAmount) {
		PCAutoCouponAmount = pCAutoCouponAmount;
	}
	public Integer getCouponTier1Amount() {
		return couponTier1Amount;
	}
	public void setCouponTier1Amount(Integer couponTier1Amount) {
		this.couponTier1Amount = couponTier1Amount;
	}
	public Integer getCouponTier2Amount() {
		return couponTier2Amount;
	}
	public void setCouponTier2Amount(Integer couponTier2Amount) {
		this.couponTier2Amount = couponTier2Amount;
	}
	public Integer getCouponTier3Amount() {
		return couponTier3Amount;
	}
	public void setCouponTier3Amount(Integer couponTier3Amount) {
		this.couponTier3Amount = couponTier3Amount;
	}
	public Integer getCouponTier4Amount() {
		return couponTier4Amount;
	}
	public void setCouponTier4Amount(Integer couponTier4Amount) {
		this.couponTier4Amount = couponTier4Amount;
	}
	public Integer getCouponTier5Amount() {
		return couponTier5Amount;
	}
	public void setCouponTier5Amount(Integer couponTier5Amount) {
		this.couponTier5Amount = couponTier5Amount;
	}
	public Integer getCouponTier6Amount() {
		return couponTier6Amount;
	}
	public void setCouponTier6Amount(Integer couponTier6Amount) {
		this.couponTier6Amount = couponTier6Amount;
	}
	public Integer getCouponTier1Count() {
		return couponTier1Count;
	}
	public void setCouponTier1Count(Integer couponTier1Count) {
		this.couponTier1Count = couponTier1Count;
	}
	public Integer getCouponTier2Count() {
		return couponTier2Count;
	}
	public void setCouponTier2Count(Integer couponTier2Count) {
		this.couponTier2Count = couponTier2Count;
	}
	public Integer getCouponTier3Count() {
		return couponTier3Count;
	}
	public void setCouponTier3Count(Integer couponTier3Count) {
		this.couponTier3Count = couponTier3Count;
	}
	public Integer getCouponTier4Count() {
		return couponTier4Count;
	}
	public void setCouponTier4Count(Integer couponTier4Count) {
		this.couponTier4Count = couponTier4Count;
	}
	public Integer getCouponTier5Count() {
		return couponTier5Count;
	}
	public void setCouponTier5Count(Integer couponTier5Count) {
		this.couponTier5Count = couponTier5Count;
	}
	public Integer getCouponTier6Count() {
		return couponTier6Count;
	}
	public void setCouponTier6Count(Integer couponTier6Count) {
		this.couponTier6Count = couponTier6Count;
	}
	public String getUsrInteger() {
		return usrInteger;
	}
	public void setUsrInteger(String usrInteger) {
		this.usrInteger = usrInteger;
	}
	public String getUsrReserved() {
		return usrReserved;
	}
	public void setUsrReserved(String usrReserved) {
		this.usrReserved = usrReserved;
	}
	public String getTypPrd() {
		return typPrd;
	}
	public void setTypPrd(String typPrd) {
		this.typPrd = typPrd;
	}
	
	
	
}
