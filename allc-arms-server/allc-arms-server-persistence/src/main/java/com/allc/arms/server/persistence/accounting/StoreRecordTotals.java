package com.allc.arms.server.persistence.accounting;

import java.io.Serializable;
import java.util.Date;

public class StoreRecordTotals implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer storeRecordID;
	private String recordType;
	private Integer storeCode;
	private Integer storeId;
	private Date timeStamp;
	private Integer lastTerminal;
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
	private Integer opnTndAmtCash;
	private Integer opnTndAmtCheck;
	private Integer opnTndAmtFoods;
	private Integer opnTndAmtMisc1;
	private Integer opnTndAmtMisc2;
	private Integer opnTndAmtMisc3;
	private Integer opnTndAmtManuf;
	private Integer opnTndAmtStore;
	private Integer miscTransactionAmount;
	private String EFTData;
	private Date periodTimeStamp;
	private Integer taxableExemptAmount;
	private Integer taxExemptAmountA;
	private Integer taxExemptAmountB;
	private Integer taxExemptAmountC;
	private Integer taxExemptAmountD;
	private Integer taxableExemptAmountA;
	private Integer taxableExemptAmountB;
	private Integer taxableExemptAmountC;
	private Integer taxableExemptAmountD;
	private Integer taxExemptAmountE;
	private Integer taxExemptAmountF;
	private Integer taxExemptAmountG;
	private Integer taxExemptAmountH;
	private Integer taxableExemptAmountE;
	private Integer taxableExemptAmountF;
	private Integer taxableExemptAmountG;
	private Integer taxableExemptAmountH;
	private String reserved;
	private Integer manAutoCouponAmount;
	private Integer storeAutoCouponAmount;
	private Integer doubledCouponAmount;
	private Integer PCTransactionAmount;
	private Integer PCTransactionCount;
	private Integer PCAutoCouponCount;
	private Integer PCAutoCouponAmount;
	private String usrInteger;
	private String usrReserved;
	private boolean flReconciled;
	private boolean flPrinted;
	private String typPrd;
	
	
	public Integer getStoreRecordID() {
		return storeRecordID;
	}
	public void setStoreRecordID(Integer storeRecordID) {
		this.storeRecordID = storeRecordID;
	}
	public String getRecordType() {
		return recordType;
	}
	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}
	public Integer getStoreId() {
		return storeId;
	}
	public void setStoreId(Integer storeId) {
		this.storeId = storeId;
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
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	public Integer getLastTerminal() {
		return lastTerminal;
	}
	public void setLastTerminal(Integer lastTerminal) {
		this.lastTerminal = lastTerminal;
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
	public Integer getOpnTndAmtCash() {
		return opnTndAmtCash;
	}
	public void setOpnTndAmtCash(Integer opnTndAmtCash) {
		this.opnTndAmtCash = opnTndAmtCash;
	}
	public Integer getOpnTndAmtCheck() {
		return opnTndAmtCheck;
	}
	public void setOpnTndAmtCheck(Integer opnTndAmtCheck) {
		this.opnTndAmtCheck = opnTndAmtCheck;
	}
	public Integer getOpnTndAmtFoods() {
		return opnTndAmtFoods;
	}
	public void setOpnTndAmtFoods(Integer opnTndAmtFoods) {
		this.opnTndAmtFoods = opnTndAmtFoods;
	}
	public Integer getOpnTndAmtMisc1() {
		return opnTndAmtMisc1;
	}
	public void setOpnTndAmtMisc1(Integer opnTndAmtMisc1) {
		this.opnTndAmtMisc1 = opnTndAmtMisc1;
	}
	public Integer getOpnTndAmtMisc2() {
		return opnTndAmtMisc2;
	}
	public void setOpnTndAmtMisc2(Integer opnTndAmtMisc2) {
		this.opnTndAmtMisc2 = opnTndAmtMisc2;
	}
	public Integer getOpnTndAmtMisc3() {
		return opnTndAmtMisc3;
	}
	public void setOpnTndAmtMisc3(Integer opnTndAmtMisc3) {
		this.opnTndAmtMisc3 = opnTndAmtMisc3;
	}
	public Integer getOpnTndAmtManuf() {
		return opnTndAmtManuf;
	}
	public void setOpnTndAmtManuf(Integer opnTndAmtManuf) {
		this.opnTndAmtManuf = opnTndAmtManuf;
	}
	public Integer getOpnTndAmtStore() {
		return opnTndAmtStore;
	}
	public void setOpnTndAmtStore(Integer opnTndAmtStore) {
		this.opnTndAmtStore = opnTndAmtStore;
	}
	public Integer getMiscTransactionAmount() {
		return miscTransactionAmount;
	}
	public void setMiscTransactionAmount(Integer miscTransactionAmount) {
		this.miscTransactionAmount = miscTransactionAmount;
	}
	public String getEFTData() {
		return EFTData;
	}
	public void setEFTData(String eFTData) {
		EFTData = eFTData;
	}
	public Date getPeriodTimeStamp() {
		return periodTimeStamp;
	}
	public void setPeriodTimeStamp(Date periodTimeStamp) {
		this.periodTimeStamp = periodTimeStamp;
	}
	public Integer getTaxableExemptAmount() {
		return taxableExemptAmount;
	}
	public void setTaxableExemptAmount(Integer taxableExemptAmount) {
		this.taxableExemptAmount = taxableExemptAmount;
	}
	public Integer getTaxExemptAmountA() {
		return taxExemptAmountA;
	}
	public void setTaxExemptAmountA(Integer taxExemptAmountA) {
		this.taxExemptAmountA = taxExemptAmountA;
	}
	public Integer getTaxExemptAmountB() {
		return taxExemptAmountB;
	}
	public void setTaxExemptAmountB(Integer taxExemptAmountB) {
		this.taxExemptAmountB = taxExemptAmountB;
	}
	public Integer getTaxExemptAmountC() {
		return taxExemptAmountC;
	}
	public void setTaxExemptAmountC(Integer taxExemptAmountC) {
		this.taxExemptAmountC = taxExemptAmountC;
	}
	public Integer getTaxExemptAmountD() {
		return taxExemptAmountD;
	}
	public void setTaxExemptAmountD(Integer taxExemptAmountD) {
		this.taxExemptAmountD = taxExemptAmountD;
	}
	public Integer getTaxableExemptAmountA() {
		return taxableExemptAmountA;
	}
	public void setTaxableExemptAmountA(Integer taxableExemptAmountA) {
		this.taxableExemptAmountA = taxableExemptAmountA;
	}
	public Integer getTaxableExemptAmountB() {
		return taxableExemptAmountB;
	}
	public void setTaxableExemptAmountB(Integer taxableExemptAmountB) {
		this.taxableExemptAmountB = taxableExemptAmountB;
	}
	public Integer getTaxableExemptAmountC() {
		return taxableExemptAmountC;
	}
	public void setTaxableExemptAmountC(Integer taxableExemptAmountC) {
		this.taxableExemptAmountC = taxableExemptAmountC;
	}
	public Integer getTaxableExemptAmountD() {
		return taxableExemptAmountD;
	}
	public void setTaxableExemptAmountD(Integer taxableExemptAmountD) {
		this.taxableExemptAmountD = taxableExemptAmountD;
	}
	public Integer getTaxExemptAmountE() {
		return taxExemptAmountE;
	}
	public void setTaxExemptAmountE(Integer taxExemptAmountE) {
		this.taxExemptAmountE = taxExemptAmountE;
	}
	public Integer getTaxExemptAmountF() {
		return taxExemptAmountF;
	}
	public void setTaxExemptAmountF(Integer taxExemptAmountF) {
		this.taxExemptAmountF = taxExemptAmountF;
	}
	public Integer getTaxExemptAmountG() {
		return taxExemptAmountG;
	}
	public void setTaxExemptAmountG(Integer taxExemptAmountG) {
		this.taxExemptAmountG = taxExemptAmountG;
	}
	public Integer getTaxExemptAmountH() {
		return taxExemptAmountH;
	}
	public void setTaxExemptAmountH(Integer taxExemptAmountH) {
		this.taxExemptAmountH = taxExemptAmountH;
	}
	public Integer getTaxableExemptAmountE() {
		return taxableExemptAmountE;
	}
	public void setTaxableExemptAmountE(Integer taxableExemptAmountE) {
		this.taxableExemptAmountE = taxableExemptAmountE;
	}
	public Integer getTaxableExemptAmountF() {
		return taxableExemptAmountF;
	}
	public void setTaxableExemptAmountF(Integer taxableExemptAmountF) {
		this.taxableExemptAmountF = taxableExemptAmountF;
	}
	public Integer getTaxableExemptAmountG() {
		return taxableExemptAmountG;
	}
	public void setTaxableExemptAmountG(Integer taxableExemptAmountG) {
		this.taxableExemptAmountG = taxableExemptAmountG;
	}
	public Integer getTaxableExemptAmountH() {
		return taxableExemptAmountH;
	}
	public void setTaxableExemptAmountH(Integer taxableExemptAmountH) {
		this.taxableExemptAmountH = taxableExemptAmountH;
	}
	public String getReserved() {
		return reserved;
	}
	public void setReserved(String reserved) {
		this.reserved = reserved;
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
	/**
	 * @return the flReconciled
	 */
	public boolean isFlReconciled() {
		return flReconciled;
	}
	/**
	 * @param flReconciled the flReconciled to set
	 */
	public void setFlReconciled(boolean flReconciled) {
		this.flReconciled = flReconciled;
	}
	/**
	 * @return the flPrinted
	 */
	public boolean isFlPrinted() {
		return flPrinted;
	}
	/**
	 * @param flPrinted the flPrinted to set
	 */
	public void setFlPrinted(boolean flPrinted) {
		this.flPrinted = flPrinted;
	}
	public String getTypPrd() {
		return typPrd;
	}
	public void setTypPrd(String tyPrd) {
		this.typPrd = tyPrd;
	}

	
}
