package com.allc.arms.server.persistence.customer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;





@Entity
@Table (name = "OP_CLIENTE")
public class Customer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6584727071298286749L;

	public Customer() {
		super();
	}

	@Id
	@Column(name="COD_CLIENTE")
	private String customerId;
	@Column(name="NOMBRE", nullable = false)
	private String firstName;
	@Column(name="APELLIDO_P", nullable = false)
	private String lastName1;
	@Column(name="APELLIDO_M", nullable = false)
	private String lastName2;
	@Column(name="GENERO", nullable = false)
	private String Gender;
	@Column(name="FEC_NACIMIENTO")
	private Date birthDate;
	@Column(name="DIRECCION")
	private String addressLine1;
	@Column(name="COD_POSTAL")
	private String zipCode;
	@Column(name="TEL_PARTICULAR")
	private String homePhone;
	@Column(name="TEL_OFICINA")
	private String busPhone;
	@Column(name="DIRECC_LINE2")
	private String addressLine2;
	@Column(name="COD_AREA_TEL_PART")
	private String homePhoneAreaCode;
	@Column(name="COD_AREA_TEL_OFIC")
	private String busPhoneAreaCode;
	@Column(name="NUM_LIC_COND")
	private String driverLicenseNum;
	@Column(name="EXT_LIC_COND")
	private String driverLicenseNumExt;
	@Column(name="NUM_SEGURO_SOC")
	private String ssNumber;
	@Column(name="EXT_SEGURO_SOC")
	private String ssNumberExt;
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@PrimaryKeyJoinColumn
	private CustomerData customerData;
	@Column(name="DES_CIUDAD")
	private String cityName;
	@Column(name="DES_DEPARTAMENTO")
	private String stateCode;
	@Column(name="COD_POSTAL_ALFA")
	private String zipAlphanumeric;
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="COD_CLIENTE", referencedColumnName="COD_CLIENTE")
    private List<Activity> activities = new ArrayList<Activity>();

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



	public List<Activity> getActivities() {
		return activities;
	}

	public void setActivities(List<Activity> activities) {
		this.activities = activities;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Customer [customerId=");
		builder.append(customerId);
		builder.append(", firstName=");
		builder.append(firstName);
		builder.append(", lastName1=");
		builder.append(lastName1);
		builder.append(", lastName2=");
		builder.append(lastName2);
		builder.append(", Gender=");
		builder.append(Gender);
		builder.append(", birthDate=");
		builder.append(birthDate);
		builder.append(", addressLine1=");
		builder.append(addressLine1);
		builder.append(", zipCode=");
		builder.append(zipCode);
		builder.append(", homePhone=");
		builder.append(homePhone);
		builder.append(", busPhone=");
		builder.append(busPhone);
		builder.append(", addressLine2=");
		builder.append(addressLine2);
		builder.append(", homePhoneAreaCode=");
		builder.append(homePhoneAreaCode);
		builder.append(", busPhoneAreaCode=");
		builder.append(busPhoneAreaCode);
		builder.append(", driverLicenseNum=");
		builder.append(driverLicenseNum);
		builder.append(", driverLicenseNumExt=");
		builder.append(driverLicenseNumExt);
		builder.append(", ssNumber=");
		builder.append(ssNumber);
		builder.append(", ssNumberExt=");
		builder.append(ssNumberExt);
		builder.append(", customerData=");
		builder.append(customerData);
		builder.append(", cityName=");
		builder.append(cityName);
		builder.append(", stateCode=");
		builder.append(stateCode);
		builder.append(", zipAlphanumeric=");
		builder.append(zipAlphanumeric);
		builder.append(", activities=");
		builder.append(activities);
		builder.append("]");
		return builder.toString();
	}
	

	
	






	
	
	
	
	
	
	

	
	
	
	
	
	
	
	
	

	 
	
	

	
	
	
	
}
