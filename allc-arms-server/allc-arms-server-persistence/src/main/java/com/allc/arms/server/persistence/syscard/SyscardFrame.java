package com.allc.arms.server.persistence.syscard;

import java.io.Serializable;
import java.util.Date;

public class SyscardFrame implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Integer idSysFrame;
	private String messageType;
	private String bitMap1;
	private String bitMap2;
	private String primaryAccNum;
	private String processingCode;
	private String trxAmount;
	private String systemTraceAuditNum;
	private String localTrxTime;
	private String localTrxDate;
	private String expirationDate;
	private String posEntryMode;
	private String networkIntID;
	private String posConditionCode;
	private String track2;
	private String retrievalRefNum;
	private String autorizationNum;
	private String responseCode;
	private String terminalID;
	private String merchantID;
	private String track1;
	private String numLoteAct;
	private String additionalData105;
	private String additionalData112;
	private String additionalData114;
	private String reservedNatUse;
	private String additionalData120;
	private String additionalData122;
	private String reservedPrivUse;
	private String reserved;
	private Date regDate;
	
	public SyscardFrame(){
		regDate = new Date();
	}
	
	public String toString(){
		return "MT:"+messageType+"-BM1:"+bitMap1+"-BM2:"+bitMap2+"-PAC:"+primaryAccNum+"-PC:"+processingCode
				+"-TA:"+trxAmount+"-STA:"+systemTraceAuditNum+"-LTT:"+localTrxTime+"-LTD:"+localTrxDate+"-ED:"+expirationDate
				+"-PEM:"+posEntryMode+"-NII:"+networkIntID+"-PCC:"+posConditionCode+"-TK2:"+track2+"-RRN:"+retrievalRefNum
				+"-AN:"+autorizationNum+"-RC:"+responseCode+"-TI:"+terminalID+"-MI:"+merchantID+"-TK1:"+track1+"-NLA:"+numLoteAct
				+"-AD105:"+additionalData105+"-AD112:"+additionalData112+"-AD114:"+additionalData114+"-RNU:"+reservedNatUse
				+"-AD120:"+additionalData120+"-AD122:"+additionalData122+"-RPU:"+reservedPrivUse+"-RV:"+reserved+"-RD:"+regDate;
	}
	/**
	 * @return the idSysFrame
	 */
	public Integer getIdSysFrame() {
		return idSysFrame;
	}
	/**
	 * @param idSysFrame the idSysFrame to set
	 */
	public void setIdSysFrame(Integer idSysFrame) {
		this.idSysFrame = idSysFrame;
	}
	/**
	 * @return the messageType
	 */
	public String getMessageType() {
		return messageType;
	}
	/**
	 * @param messageType the messageType to set
	 */
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	/**
	 * @return the bitMap1
	 */
	public String getBitMap1() {
		return bitMap1;
	}
	/**
	 * @param bitMap1 the bitMap1 to set
	 */
	public void setBitMap1(String bitMap1) {
		this.bitMap1 = bitMap1;
	}
	/**
	 * @return the bitMap2
	 */
	public String getBitMap2() {
		return bitMap2;
	}
	/**
	 * @param bitMap2 the bitMap2 to set
	 */
	public void setBitMap2(String bitMap2) {
		this.bitMap2 = bitMap2;
	}
	/**
	 * @return the primaryAccNum
	 */
	public String getPrimaryAccNum() {
		return primaryAccNum;
	}
	/**
	 * @param primaryAccNum the primaryAccNum to set
	 */
	public void setPrimaryAccNum(String primaryAccNum) {
		this.primaryAccNum = primaryAccNum;
	}
	/**
	 * @return the processingCode
	 */
	public String getProcessingCode() {
		return processingCode;
	}
	/**
	 * @param processingCode the processingCode to set
	 */
	public void setProcessingCode(String processingCode) {
		this.processingCode = processingCode;
	}
	/**
	 * @return the trxAmount
	 */
	public String getTrxAmount() {
		return trxAmount;
	}
	/**
	 * @param trxAmount the trxAmount to set
	 */
	public void setTrxAmount(String trxAmount) {
		this.trxAmount = trxAmount;
	}
	/**
	 * @return the systemTraceAuditNum
	 */
	public String getSystemTraceAuditNum() {
		return systemTraceAuditNum;
	}
	/**
	 * @param systemTraceAuditNum the systemTraceAuditNum to set
	 */
	public void setSystemTraceAuditNum(String systemTraceAuditNum) {
		this.systemTraceAuditNum = systemTraceAuditNum;
	}
	/**
	 * @return the localTrxTime
	 */
	public String getLocalTrxTime() {
		return localTrxTime;
	}
	/**
	 * @param localTrxTime the localTrxTime to set
	 */
	public void setLocalTrxTime(String localTrxTime) {
		this.localTrxTime = localTrxTime;
	}
	/**
	 * @return the localTrxDate
	 */
	public String getLocalTrxDate() {
		return localTrxDate;
	}
	/**
	 * @param localTrxDate the localTrxDate to set
	 */
	public void setLocalTrxDate(String localTrxDate) {
		this.localTrxDate = localTrxDate;
	}
	/**
	 * @return the expirationDate
	 */
	public String getExpirationDate() {
		return expirationDate;
	}
	/**
	 * @param expirationDate the expirationDate to set
	 */
	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
	}
	/**
	 * @return the posEntryMode
	 */
	public String getPosEntryMode() {
		return posEntryMode;
	}
	/**
	 * @param posEntryMode the posEntryMode to set
	 */
	public void setPosEntryMode(String posEntryMode) {
		this.posEntryMode = posEntryMode;
	}
	/**
	 * @return the networkIntID
	 */
	public String getNetworkIntID() {
		return networkIntID;
	}
	/**
	 * @param networkIntID the networkIntID to set
	 */
	public void setNetworkIntID(String networkIntID) {
		this.networkIntID = networkIntID;
	}
	/**
	 * @return the posConditionCode
	 */
	public String getPosConditionCode() {
		return posConditionCode;
	}
	/**
	 * @param posConditionCode the posConditionCode to set
	 */
	public void setPosConditionCode(String posConditionCode) {
		this.posConditionCode = posConditionCode;
	}
	/**
	 * @return the track2
	 */
	public String getTrack2() {
		return track2;
	}
	/**
	 * @param track2 the track2 to set
	 */
	public void setTrack2(String track2) {
		this.track2 = track2;
	}
	/**
	 * @return the retrievalRefNum
	 */
	public String getRetrievalRefNum() {
		return retrievalRefNum;
	}
	/**
	 * @param retrievalRefNum the retrievalRefNum to set
	 */
	public void setRetrievalRefNum(String retrievalRefNum) {
		this.retrievalRefNum = retrievalRefNum;
	}
	/**
	 * @return the autorizationNum
	 */
	public String getAutorizationNum() {
		return autorizationNum;
	}
	/**
	 * @param autorizationNum the autorizationNum to set
	 */
	public void setAutorizationNum(String autorizationNum) {
		this.autorizationNum = autorizationNum;
	}
	/**
	 * @return the responseCode
	 */
	public String getResponseCode() {
		return responseCode;
	}
	/**
	 * @param responseCode the responseCode to set
	 */
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	/**
	 * @return the terminalID
	 */
	public String getTerminalID() {
		return terminalID;
	}
	/**
	 * @param terminalID the terminalID to set
	 */
	public void setTerminalID(String terminalID) {
		this.terminalID = terminalID;
	}
	/**
	 * @return the merchantID
	 */
	public String getMerchantID() {
		return merchantID;
	}
	/**
	 * @param merchantID the merchantID to set
	 */
	public void setMerchantID(String merchantID) {
		this.merchantID = merchantID;
	}
	/**
	 * @return the track1
	 */
	public String getTrack1() {
		return track1;
	}
	/**
	 * @param track1 the track1 to set
	 */
	public void setTrack1(String track1) {
		this.track1 = track1;
	}
	/**
	 * @return the numLoteAct
	 */
	public String getNumLoteAct() {
		return numLoteAct;
	}
	/**
	 * @param numLoteAct the numLoteAct to set
	 */
	public void setNumLoteAct(String numLoteAct) {
		this.numLoteAct = numLoteAct;
	}
	/**
	 * @return the additionalData105
	 */
	public String getAdditionalData105() {
		return additionalData105;
	}
	/**
	 * @param additionalData105 the additionalData105 to set
	 */
	public void setAdditionalData105(String additionalData105) {
		this.additionalData105 = additionalData105;
	}
	/**
	 * @return the additionalData112
	 */
	public String getAdditionalData112() {
		return additionalData112;
	}
	/**
	 * @param additionalData112 the additionalData112 to set
	 */
	public void setAdditionalData112(String additionalData112) {
		this.additionalData112 = additionalData112;
	}
	/**
	 * @return the additionalData114
	 */
	public String getAdditionalData114() {
		return additionalData114;
	}
	/**
	 * @param additionalData114 the additionalData114 to set
	 */
	public void setAdditionalData114(String additionalData114) {
		this.additionalData114 = additionalData114;
	}
	/**
	 * @return the reservedNatUse
	 */
	public String getReservedNatUse() {
		return reservedNatUse;
	}
	/**
	 * @param reservedNatUse the reservedNatUse to set
	 */
	public void setReservedNatUse(String reservedNatUse) {
		this.reservedNatUse = reservedNatUse;
	}
	/**
	 * @return the additionalData120
	 */
	public String getAdditionalData120() {
		return additionalData120;
	}
	/**
	 * @param additionalData120 the additionalData120 to set
	 */
	public void setAdditionalData120(String additionalData120) {
		this.additionalData120 = additionalData120;
	}
	/**
	 * @return the additionalData122
	 */
	public String getAdditionalData122() {
		return additionalData122;
	}
	/**
	 * @param additionalData122 the additionalData122 to set
	 */
	public void setAdditionalData122(String additionalData122) {
		this.additionalData122 = additionalData122;
	}
	/**
	 * @return the reservedPrivUse
	 */
	public String getReservedPrivUse() {
		return reservedPrivUse;
	}
	/**
	 * @param reservedPrivUse the reservedPrivUse to set
	 */
	public void setReservedPrivUse(String reservedPrivUse) {
		this.reservedPrivUse = reservedPrivUse;
	}
	/**
	 * @return the reserved
	 */
	public String getReserved() {
		return reserved;
	}
	/**
	 * @param reserved the reserved to set
	 */
	public void setReserved(String reserved) {
		this.reserved = reserved;
	}
	/**
	 * @return the regDate
	 */
	public Date getRegDate() {
		return regDate;
	}
	/**
	 * @param regDate the regDate to set
	 */
	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}
	

}
