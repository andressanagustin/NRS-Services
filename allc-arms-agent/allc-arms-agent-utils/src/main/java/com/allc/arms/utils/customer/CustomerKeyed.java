package com.allc.arms.utils.customer;

import java.io.Serializable;
import java.util.Date;

public class CustomerKeyed implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -232800059765126203L;

	
	private String customerId;
	private String  firstName;
	private String  lastName1;
	private String  lastName2;
	private String  Gender;
	private Date    birthDate;
	private String  addressLine1;
	private String  zipCode;
	private String  homePhone;
	private String  busHome;
	private String  addressLine2;
	private String  homePhoneAreaCode;
	private String  busPhoneAreaCode;
	private String  driverLicenseNum;
	private String  driverLicenseNumExt;
	private String  ssNumber;
	private String  ssNumberExt;
	private String  stateAbbreviation;;
	private String  cityName;
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
	private Date 	lastRainChainDate;
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
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName1() {
		return lastName1;
	}
	public void setLastName1(String lastName1) {
		this.lastName1 = lastName1;
	}
	public String getLastName2() {
		return lastName2;
	}
	public void setLastName2(String lastName2) {
		this.lastName2 = lastName2;
	}
	public String getGender() {
		return Gender;
	}
	public void setGender(String gender) {
		Gender = gender;
	}
	public Date getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	public String getAddressLine1() {
		return addressLine1;
	}
	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public String getHomePhone() {
		return homePhone;
	}
	public void setHomePhone(String homePhone) {
		this.homePhone = homePhone;
	}
	public String getBusHome() {
		return busHome;
	}
	public void setBusHome(String busHome) {
		this.busHome = busHome;
	}
	public String getAddressLine2() {
		return addressLine2;
	}
	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}
	public String getHomePhoneAreaCode() {
		return homePhoneAreaCode;
	}
	public void setHomePhoneAreaCode(String homePhoneAreaCode) {
		this.homePhoneAreaCode = homePhoneAreaCode;
	}
	public String getBusPhoneAreaCode() {
		return busPhoneAreaCode;
	}
	public void setBusPhoneAreaCode(String busPhoneAreaCode) {
		this.busPhoneAreaCode = busPhoneAreaCode;
	}
	public String getDriverLicenseNum() {
		return driverLicenseNum;
	}
	public void setDriverLicenseNum(String driverLicenseNum) {
		this.driverLicenseNum = driverLicenseNum;
	}
	public String getDriverLicenseNumExt() {
		return driverLicenseNumExt;
	}
	public void setDriverLicenseNumExt(String driverLicenseNumExt) {
		this.driverLicenseNumExt = driverLicenseNumExt;
	}
	public String getSsNumber() {
		return ssNumber;
	}
	public void setSsNumber(String ssNumber) {
		this.ssNumber = ssNumber;
	}
	public String getSsNumberExt() {
		return ssNumberExt;
	}
	public void setSsNumberExt(String ssNumberExt) {
		this.ssNumberExt = ssNumberExt;
	}
	public String getStateAbbreviation() {
		return stateAbbreviation;
	}
	public void setStateAbbreviation(String stateAbbreviation) {
		this.stateAbbreviation = stateAbbreviation;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
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
	public Date getLastRainChainDate() {
		return lastRainChainDate;
	}
	public void setLastRainChainDate(Date lastRainChainDate) {
		this.lastRainChainDate = lastRainChainDate;
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
		buffer.append("CustomerKeyed [customerId=");
		buffer.append(customerId);
		buffer.append(", firstName=");
		buffer.append(firstName);
		buffer.append(", lastName1=");
		buffer.append(lastName1);
		buffer.append(", lastName2=");
		buffer.append(lastName2);
		buffer.append(", Gender=");
		buffer.append(Gender);
		buffer.append(", birthDate=");
		buffer.append(birthDate);
		buffer.append(", addressLine1=");
		buffer.append(addressLine1);
		buffer.append(", zipCode=");
		buffer.append(zipCode);
		buffer.append(", homePhone=");
		buffer.append(homePhone);
		buffer.append(", busHome=");
		buffer.append(busHome);
		buffer.append(", addressLine2=");
		buffer.append(addressLine2);
		buffer.append(", homePhoneAreaCode=");
		buffer.append(homePhoneAreaCode);
		buffer.append(", busPhoneAreaCode=");
		buffer.append(busPhoneAreaCode);
		buffer.append(", driverLicenseNum=");
		buffer.append(driverLicenseNum);
		buffer.append(", driverLicenseNumExt=");
		buffer.append(driverLicenseNumExt);
		buffer.append(", ssNumber=");
		buffer.append(ssNumber);
		buffer.append(", ssNumberExt=");
		buffer.append(ssNumberExt);
		buffer.append(", stateAbbreviation=");
		buffer.append(stateAbbreviation);
		buffer.append(", cityName=");
		buffer.append(cityName);
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
		buffer.append(", lastRainChainDate=");
		buffer.append(lastRainChainDate);
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
