package com.allc.arms.server.persistence.customer;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table (name = "OP_DATACRM")
public class CustomerData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8909562462568772721L;

	public CustomerData() {
		super();
		
	}

	@Id
	@Column(name="COD_CLIENTE")
	private String customerId;
	@Column(name="COD_ESTADO")
	private Integer recordStatus;
	@Column(name="COD_TIENDA")
	private Integer homeStoreNumber;
	@Column(name="COD_INGRESO")
	private Integer income;
	@Column(name="COD_DEMOGRAF")
	private Integer customerDemo;
	@Column(name="FEC_REGISTRO")
	private Date 	enrollDate;
	@Column(name="TAM_FAMILIA")
	private Integer familiSize;
	@Column(name="EDAD_HIJO1")
	private Integer childAge1;
	@Column(name="EDAD_HIJO2")
	private Integer childAge2;
	@Column(name="EDAD_HIJO3")
	private Integer childAge3;
	@Column(name="EDAD_HIJO4")
	private Integer childAge4;
	@Column(name="EDAD_HIJO5")
	private Integer childAge5;
	@Column(name="EDAD_HIJO6")
	private Integer childAge6;
	@Column(name="EDAD_HIJO7")
	private Integer childAge7;
	@Column(name="TOT_AJUSTES")
	private Integer totalAdjustCount;
	@Column(name="TOT_REEMBOLSOS")
	private Integer totalReedemCont;
	@Column(name="TOT_AJUSTE_PTOS_SUM")
	private Integer totalPointsAdjPlus;
	@Column(name="TOT_AJUSTE_PTOS_RES")
	private Integer totalPointsAdjMinus;
	@Column(name="ULTM_NUM_AJUSTE")
	private Integer lastAdjustFormNum;
	@Column(name="ULTM_NUM_REEMBOLSO")
	private Integer lastReedemFormNum;
	@Column(name="ULTM_FEC_AJUSTE")
	private Date 	lastAdjustDate;
	@Column(name="ULTM_FEC_REEMBOLSO")
	private Date 	lastReedemDate;
	@Column(name="FEC_ACTUALIZA")
	private Date 	changeDate;
	@Column(name="FEC_RESERVA")
	private Date 	lastRainCheckDate;
	@Column(name="AUTORIZA_REF_CLIENTE")
	private String	customerAuthFlag;
	@Column(name="ALIAS_REF_CLIENTE")
	private String  aliasFlag;
	@Column(name="CUENTA_REFER")
	private String referenceAccount;
	@Column(name="NUM_MONTO_RESERVA")
	private Integer lastRainCheckAmount;

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public Integer getRecordStatus() {
		return recordStatus;
	}

	public void setRecordStatus(Integer recordStatus) {
		this.recordStatus = recordStatus;
	}

	public Integer getHomeStoreNumber() {
		return homeStoreNumber;
	}

	public void setHomeStoreNumber(Integer homeStoreNumber) {
		this.homeStoreNumber = homeStoreNumber;
	}

	public Integer getIncome() {
		return income;
	}

	public void setIncome(Integer income) {
		this.income = income;
	}

	public Integer getCustomerDemo() {
		return customerDemo;
	}

	public void setCustomerDemo(Integer customerDemo) {
		this.customerDemo = customerDemo;
	}

	public Date getEnrollDate() {
		return enrollDate;
	}

	public void setEnrollDate(Date enrollDate) {
		this.enrollDate = enrollDate;
	}

	public Integer getFamiliSize() {
		return familiSize;
	}

	public void setFamiliSize(Integer familiSize) {
		this.familiSize = familiSize;
	}

	public Integer getChildAge1() {
		return childAge1;
	}

	public void setChildAge1(Integer childAge1) {
		this.childAge1 = childAge1;
	}

	public Integer getChildAge2() {
		return childAge2;
	}

	public void setChildAge2(Integer childAge2) {
		this.childAge2 = childAge2;
	}

	public Integer getChildAge3() {
		return childAge3;
	}

	public void setChildAge3(Integer childAge3) {
		this.childAge3 = childAge3;
	}

	public Integer getChildAge4() {
		return childAge4;
	}

	public void setChildAge4(Integer childAge4) {
		this.childAge4 = childAge4;
	}

	public Integer getChildAge5() {
		return childAge5;
	}

	public void setChildAge5(Integer childAge5) {
		this.childAge5 = childAge5;
	}

	public Integer getChildAge6() {
		return childAge6;
	}

	public void setChildAge6(Integer childAge6) {
		this.childAge6 = childAge6;
	}

	public Integer getChildAge7() {
		return childAge7;
	}

	public void setChildAge7(Integer childAge7) {
		this.childAge7 = childAge7;
	}

	public Integer getTotalAdjustCount() {
		return totalAdjustCount;
	}

	public void setTotalAdjustCount(Integer totalAdjustCount) {
		this.totalAdjustCount = totalAdjustCount;
	}

	public Integer getTotalReedemCont() {
		return totalReedemCont;
	}

	public void setTotalReedemCont(Integer totalReedemCont) {
		this.totalReedemCont = totalReedemCont;
	}

	public Integer getTotalPointsAdjPlus() {
		return totalPointsAdjPlus;
	}

	public void setTotalPointsAdjPlus(Integer totalPointsAdjPlus) {
		this.totalPointsAdjPlus = totalPointsAdjPlus;
	}

	public Integer getTotalPointsAdjMinus() {
		return totalPointsAdjMinus;
	}

	public void setTotalPointsAdjMinus(Integer totalPointsAdjMinus) {
		this.totalPointsAdjMinus = totalPointsAdjMinus;
	}

	public Integer getLastAdjustFormNum() {
		return lastAdjustFormNum;
	}

	public void setLastAdjustFormNum(Integer lastAdjustFormNum) {
		this.lastAdjustFormNum = lastAdjustFormNum;
	}

	public Integer getLastReedemFormNum() {
		return lastReedemFormNum;
	}

	public void setLastReedemFormNum(Integer lastReedemFormNum) {
		this.lastReedemFormNum = lastReedemFormNum;
	}

	public Date getLastAdjustDate() {
		return lastAdjustDate;
	}

	public void setLastAdjustDate(Date lastAdjustDate) {
		this.lastAdjustDate = lastAdjustDate;
	}

	public Date getLastReedemDate() {
		return lastReedemDate;
	}

	public void setLastReedemDate(Date lastReedemDate) {
		this.lastReedemDate = lastReedemDate;
	}

	public Date getChangeDate() {
		return changeDate;
	}

	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}

	public Date getLastRainCheckDate() {
		return lastRainCheckDate;
	}

	public void setLastRainCheckDate(Date lastRainCheckDate) {
		this.lastRainCheckDate = lastRainCheckDate;
	}

	public String getCustomerAuthFlag() {
		return customerAuthFlag;
	}

	public void setCustomerAuthFlag(String customerAuthFlag) {
		this.customerAuthFlag = customerAuthFlag;
	}

	public String getAliasFlag() {
		return aliasFlag;
	}

	public void setAliasFlag(String aliasFlag) {
		this.aliasFlag = aliasFlag;
	}

	public String getReferenceAccount() {
		return referenceAccount;
	}

	public void setReferenceAccount(String referenceAccount) {
		this.referenceAccount = referenceAccount;
	}

	public Integer getLastRainCheckAmount() {
		return lastRainCheckAmount;
	}

	public void setLastRainCheckAmount(Integer lastRainCheckAmount) {
		this.lastRainCheckAmount = lastRainCheckAmount;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CustomerData [customerId=");
		builder.append(customerId);
		builder.append(", recordStatus=");
		builder.append(recordStatus);
		builder.append(", homeStoreNumber=");
		builder.append(homeStoreNumber);
		builder.append(", income=");
		builder.append(income);
		builder.append(", customerDemo=");
		builder.append(customerDemo);
		builder.append(", enrollDate=");
		builder.append(enrollDate);
		builder.append(", familiSize=");
		builder.append(familiSize);
		builder.append(", childAge1=");
		builder.append(childAge1);
		builder.append(", childAge2=");
		builder.append(childAge2);
		builder.append(", childAge3=");
		builder.append(childAge3);
		builder.append(", childAge4=");
		builder.append(childAge4);
		builder.append(", childAge5=");
		builder.append(childAge5);
		builder.append(", childAge6=");
		builder.append(childAge6);
		builder.append(", childAge7=");
		builder.append(childAge7);
		builder.append(", totalAdjustCount=");
		builder.append(totalAdjustCount);
		builder.append(", totalReedemCont=");
		builder.append(totalReedemCont);
		builder.append(", totalPointsAdjPlus=");
		builder.append(totalPointsAdjPlus);
		builder.append(", totalPointsAdjMinus=");
		builder.append(totalPointsAdjMinus);
		builder.append(", lastAdjustFormNum=");
		builder.append(lastAdjustFormNum);
		builder.append(", lastReedemFormNum=");
		builder.append(lastReedemFormNum);
		builder.append(", lastAdjustDate=");
		builder.append(lastAdjustDate);
		builder.append(", lastReedemDate=");
		builder.append(lastReedemDate);
		builder.append(", changeDate=");
		builder.append(changeDate);
		builder.append(", lastRainCheckDate=");
		builder.append(lastRainCheckDate);
		builder.append(", customerAuthFlag=");
		builder.append(customerAuthFlag);
		builder.append(", aliasFlag=");
		builder.append(aliasFlag);
		builder.append(", referenceAccount=");
		builder.append(referenceAccount);
		builder.append(", lastRainCheckAmount=");
		builder.append(lastRainCheckAmount);
		builder.append("]");
		return builder.toString();
	}

	

	
	
	
	
	
	
	
	
	
	
	
	
	
}
