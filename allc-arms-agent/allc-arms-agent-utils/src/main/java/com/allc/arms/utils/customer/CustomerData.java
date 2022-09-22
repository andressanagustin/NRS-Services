package com.allc.arms.utils.customer;

import java.io.Serializable;
import java.util.Date;


public class CustomerData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8909562462568772721L;

	public CustomerData() {
		super();
		
	}

	private String customerId;
	private Integer recordStatus;
	private Integer homeStoreNumber;
	private Integer income;
	private Integer customerDemo;
	private Date 	enrollDate;
	private Integer familiSize;
	private Integer childAge1;
	private Integer childAge2;
	private Integer childAge3;
	private Integer childAge4;
	private Integer childAge5;
	private Integer childAge6;
	private Integer childAge7;
	private Integer totalAdjustCount;
	private Integer totalReedemCont;
	private Integer totalPointsAdjPlus;
	private Integer totalPointsAdjMinus;
	private Integer lastAdjustFormNum;
	private Integer lastReedemFormNum;
	private Date 	lastAdjustDate;
	private Date 	lastReedemDate;
	private Date 	changeDate;
	private Date 	lastRainCheckDate;
	private String	customerAuthFlag;
	private String  aliasFlag;
	private String referenceAccount;
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

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("CustomerData [customerId=");
		buffer.append(customerId);
		buffer.append(", recordStatus=");
		buffer.append(recordStatus);
		buffer.append(", homeStoreNumber=");
		buffer.append(homeStoreNumber);
		buffer.append(", income=");
		buffer.append(income);
		buffer.append(", customerDemo=");
		buffer.append(customerDemo);
		buffer.append(", enrollDate=");
		buffer.append(enrollDate);
		buffer.append(", familiSize=");
		buffer.append(familiSize);
		buffer.append(", childAge1=");
		buffer.append(childAge1);
		buffer.append(", childAge2=");
		buffer.append(childAge2);
		buffer.append(", childAge3=");
		buffer.append(childAge3);
		buffer.append(", childAge4=");
		buffer.append(childAge4);
		buffer.append(", childAge5=");
		buffer.append(childAge5);
		buffer.append(", childAge6=");
		buffer.append(childAge6);
		buffer.append(", childAge7=");
		buffer.append(childAge7);
		buffer.append(", totalAdjustCount=");
		buffer.append(totalAdjustCount);
		buffer.append(", totalReedemCont=");
		buffer.append(totalReedemCont);
		buffer.append(", totalPointsAdjPlus=");
		buffer.append(totalPointsAdjPlus);
		buffer.append(", totalPointsAdjMinus=");
		buffer.append(totalPointsAdjMinus);
		buffer.append(", lastAdjustFormNum=");
		buffer.append(lastAdjustFormNum);
		buffer.append(", lastReedemFormNum=");
		buffer.append(lastReedemFormNum);
		buffer.append(", lastAdjustDate=");
		buffer.append(lastAdjustDate);
		buffer.append(", lastReedemDate=");
		buffer.append(lastReedemDate);
		buffer.append(", changeDate=");
		buffer.append(changeDate);
		buffer.append(", lastRainCheckDate=");
		buffer.append(lastRainCheckDate);
		buffer.append(", customerAuthFlag=");
		buffer.append(customerAuthFlag);
		buffer.append(", aliasFlag=");
		buffer.append(aliasFlag);
		buffer.append(", referenceAccount=");
		buffer.append(referenceAccount);
		buffer.append(", lastRainCheckAmount=");
		buffer.append(lastRainCheckAmount);
		buffer.append("]");
		return buffer.toString();
	}

	


	
	
	
	
	
	
	
	
	
	
	
	
	
}
