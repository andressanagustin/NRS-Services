/**
 * 
 */
package com.allc.arms.server.persistence.status;

import java.io.Serializable;

/**
 * @author gustavo
 *
 */
public class TerminalStatus implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer termStsID;
	private Integer period;
	private Integer terminalNumber;
	private Integer operator;
	private Integer transNum;
	private Integer numLoans;
	private Integer amtLoans;
	private Integer numPkups;
	private Integer amtPkups;
	private Integer grossPos;
	private Integer grossNeg;
	private Integer amtMisc;
	private Integer numTrans;
	private Integer tillAmtCash;
	private Integer tillAmtCheck;
	private Integer tillAmtFoods;
	private Integer tillAmtMisc1;
	private Integer tillAmtMisc2;
	private Integer tillAmtMisc3;
	private Integer tillAmtManuf;
	private Integer tillAmtStore;
	private Integer tranType;
	private Integer status;
	private Integer status2;
	private Integer storeCode;
	private String ultNumFact;
	
	/**
	 * @return the termStsID
	 */
	public Integer getTermStsID() {
		return termStsID;
	}
	/**
	 * @param termStsID the termStsID to set
	 */
	public void setTermStsID(Integer termStsID) {
		this.termStsID = termStsID;
	}
	/**
	 * @return the period
	 */
	public Integer getPeriod() {
		return period;
	}
	/**
	 * @param period the period to set
	 */
	public void setPeriod(Integer period) {
		this.period = period;
	}
	/**
	 * @return the terminalNumber
	 */
	public Integer getTerminalNumber() {
		return terminalNumber;
	}
	/**
	 * @param terminalNumber the terminalNumber to set
	 */
	public void setTerminalNumber(Integer terminalNumber) {
		this.terminalNumber = terminalNumber;
	}
	/**
	 * @return the operator
	 */
	public Integer getOperator() {
		return operator;
	}
	/**
	 * @param operator the operator to set
	 */
	public void setOperator(Integer operator) {
		this.operator = operator;
	}
	/**
	 * @return the transNum
	 */
	public Integer getTransNum() {
		return transNum;
	}
	/**
	 * @param transNum the transNum to set
	 */
	public void setTransNum(Integer transNum) {
		this.transNum = transNum;
	}
	/**
	 * @return the numLoans
	 */
	public Integer getNumLoans() {
		return numLoans;
	}
	/**
	 * @param numLoans the numLoans to set
	 */
	public void setNumLoans(Integer numLoans) {
		this.numLoans = numLoans;
	}
	/**
	 * @return the amtLoans
	 */
	public Integer getAmtLoans() {
		return amtLoans;
	}
	/**
	 * @param amtLoans the amtLoans to set
	 */
	public void setAmtLoans(Integer amtLoans) {
		this.amtLoans = amtLoans;
	}
	/**
	 * @return the numPkups
	 */
	public Integer getNumPkups() {
		return numPkups;
	}
	/**
	 * @param numPkups the numPkups to set
	 */
	public void setNumPkups(Integer numPkups) {
		this.numPkups = numPkups;
	}
	/**
	 * @return the amtPkups
	 */
	public Integer getAmtPkups() {
		return amtPkups;
	}
	/**
	 * @param amtPkups the amtPkups to set
	 */
	public void setAmtPkups(Integer amtPkups) {
		this.amtPkups = amtPkups;
	}
	/**
	 * @return the grossPos
	 */
	public Integer getGrossPos() {
		return grossPos;
	}
	/**
	 * @param grossPos the grossPos to set
	 */
	public void setGrossPos(Integer grossPos) {
		this.grossPos = grossPos;
	}
	/**
	 * @return the grossNeg
	 */
	public Integer getGrossNeg() {
		return grossNeg;
	}
	/**
	 * @param grossNeg the grossNeg to set
	 */
	public void setGrossNeg(Integer grossNeg) {
		this.grossNeg = grossNeg;
	}
	/**
	 * @return the amtMisc
	 */
	public Integer getAmtMisc() {
		return amtMisc;
	}
	/**
	 * @param amtMisc the amtMisc to set
	 */
	public void setAmtMisc(Integer amtMisc) {
		this.amtMisc = amtMisc;
	}
	/**
	 * @return the numTrans
	 */
	public Integer getNumTrans() {
		return numTrans;
	}
	/**
	 * @param numTrans the numTrans to set
	 */
	public void setNumTrans(Integer numTrans) {
		this.numTrans = numTrans;
	}
	/**
	 * @return the tillAmtCash
	 */
	public Integer getTillAmtCash() {
		return tillAmtCash;
	}
	/**
	 * @param tillAmtCash the tillAmtCash to set
	 */
	public void setTillAmtCash(Integer tillAmtCash) {
		this.tillAmtCash = tillAmtCash;
	}
	/**
	 * @return the tillAmtCheck
	 */
	public Integer getTillAmtCheck() {
		return tillAmtCheck;
	}
	/**
	 * @param tillAmtCheck the tillAmtCheck to set
	 */
	public void setTillAmtCheck(Integer tillAmtCheck) {
		this.tillAmtCheck = tillAmtCheck;
	}
	/**
	 * @return the tillAmtFoods
	 */
	public Integer getTillAmtFoods() {
		return tillAmtFoods;
	}
	/**
	 * @param tillAmtFoods the tillAmtFoods to set
	 */
	public void setTillAmtFoods(Integer tillAmtFoods) {
		this.tillAmtFoods = tillAmtFoods;
	}
	/**
	 * @return the tillAmtMisc1
	 */
	public Integer getTillAmtMisc1() {
		return tillAmtMisc1;
	}
	/**
	 * @param tillAmtMisc1 the tillAmtMisc1 to set
	 */
	public void setTillAmtMisc1(Integer tillAmtMisc1) {
		this.tillAmtMisc1 = tillAmtMisc1;
	}
	/**
	 * @return the tillAmtMisc2
	 */
	public Integer getTillAmtMisc2() {
		return tillAmtMisc2;
	}
	/**
	 * @param tillAmtMisc2 the tillAmtMisc2 to set
	 */
	public void setTillAmtMisc2(Integer tillAmtMisc2) {
		this.tillAmtMisc2 = tillAmtMisc2;
	}
	/**
	 * @return the tillAmtMisc3
	 */
	public Integer getTillAmtMisc3() {
		return tillAmtMisc3;
	}
	/**
	 * @param tillAmtMisc3 the tillAmtMisc3 to set
	 */
	public void setTillAmtMisc3(Integer tillAmtMisc3) {
		this.tillAmtMisc3 = tillAmtMisc3;
	}
	/**
	 * @return the tillAmtManuf
	 */
	public Integer getTillAmtManuf() {
		return tillAmtManuf;
	}
	/**
	 * @param tillAmtManuf the tillAmtManuf to set
	 */
	public void setTillAmtManuf(Integer tillAmtManuf) {
		this.tillAmtManuf = tillAmtManuf;
	}
	/**
	 * @return the tillAmtStore
	 */
	public Integer getTillAmtStore() {
		return tillAmtStore;
	}
	/**
	 * @param tillAmtStore the tillAmtStore to set
	 */
	public void setTillAmtStore(Integer tillAmtStore) {
		this.tillAmtStore = tillAmtStore;
	}
	/**
	 * @return the tranType
	 */
	public Integer getTranType() {
		return tranType;
	}
	/**
	 * @param tranType the tranType to set
	 */
	public void setTranType(Integer tranType) {
		this.tranType = tranType;
	}
	/**
	 * @return the status
	 */
	public Integer getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
	/**
	 * @return the status2
	 */
	public Integer getStatus2() {
		return status2;
	}
	/**
	 * @param status2 the status2 to set
	 */
	public void setStatus2(Integer status2) {
		this.status2 = status2;
	}
	public Integer getStoreCode() {
		return storeCode;
	}
	public void setStoreCode(Integer storeCode) {
		this.storeCode = storeCode;
	}
	public String getUltNumFact() {
		return ultNumFact;
	}
	public void setUltNumFact(String ultNumFact) {
		this.ultNumFact = ultNumFact;
	}
	

}
