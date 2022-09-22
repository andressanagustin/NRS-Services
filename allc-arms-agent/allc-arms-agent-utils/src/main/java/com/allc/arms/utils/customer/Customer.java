package com.allc.arms.utils.customer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;






public class Customer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6584727071298286749L;
	
	public Customer() {
		super();
	}

	private String customerId;
	private String firstName;
	private String lastName1;
	private String lastName2;
	private String Gender;
	private Date birthDate;
	private String addressLine1;
	private String zipCode;
	private String homePhone;
	private String busPhone;
	private String addressLine2;
	private String homePhoneAreaCode;
	private String busPhoneAreaCode;
	private String driverLicenseNum;
	private String driverLicenseNumExt;
	private String ssNumber;
	private String ssNumberExt;
	private CustomerData customerData;
	private String cityName;
	private String stateCode;
	private String zipAlphanumeric;
	private List activities;

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
	public String getBusPhone() {
		return busPhone;
	}
	public void setBusPhone(String busPhone) {
		this.busPhone = busPhone;
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
	public CustomerData getCustomerData() {
		return customerData;
	}
	public void setCustomerData(CustomerData customerData) {
		this.customerData = customerData;
	}

	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getStateCode() {
		return stateCode;
	}
	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}
	public String getZipAlphanumeric() {
		return zipAlphanumeric;
	}
	public void setZipAlphanumeric(String zipAlphanumeric) {
		this.zipAlphanumeric = zipAlphanumeric;
	}
	public List getActivities() {
		return activities;
	}
	public void setActivities(List activities) {
		this.activities = activities;
	}

	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Customer [customerId=");
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
		buffer.append(", busPhone=");
		buffer.append(busPhone);
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
		buffer.append(", customerData=");
		buffer.append(customerData);
		buffer.append(", cityName=");
		buffer.append(cityName);
		buffer.append(", stateCode=");
		buffer.append(stateCode);
		buffer.append(", zipAlphanumeric=");
		buffer.append(zipAlphanumeric);
		buffer.append(", activities=");
		buffer.append(activities);
		buffer.append("]");
		return buffer.toString();
	}
	
	




	
	
	
	
	
	
	
	

	
	
	
	
	
	
	
	
	

	 
	
	

	
	
	
	
}
