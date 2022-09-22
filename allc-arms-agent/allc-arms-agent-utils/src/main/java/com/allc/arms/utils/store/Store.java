package com.allc.arms.utils.store;

import java.io.Serializable;
import java.util.Date;



public class Store implements Serializable{
	
	
	private static final long serialVersionUID = -5594059270915229891L;
	/**
	 * 
	 */


	private Integer storeId;
	private Integer chainStoreId;
	private String name;
	private String key;
	private String address;
	private Integer stateCode;
	private Integer cityCode;
	private String ip;
	private Integer status;
	private Date   lastUpdate;

	public Integer getStoreId() {
		return storeId;
	}

	public void setStoreId(Integer storeId) {
		this.storeId = storeId;
	}

	public Integer getChainStoreId() {
		return chainStoreId;
	}

	public void setChainStoreId(Integer chainStoreId) {
		this.chainStoreId = chainStoreId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Integer getStateCode() {
		return stateCode;
	}

	public void setStateCode(Integer stateCode) {
		this.stateCode = stateCode;
	}

	public Integer getCityCode() {
		return cityCode;
	}

	public void setCityCode(Integer cityCode) {
		this.cityCode = cityCode;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Store [storeId=");
		buffer.append(storeId);
		buffer.append(", chainStoreId=");
		buffer.append(chainStoreId);
		buffer.append(", name=");
		buffer.append(name);
		buffer.append(", key=");
		buffer.append(key);
		buffer.append(", address=");
		buffer.append(address);
		buffer.append(", stateCode=");
		buffer.append(stateCode);
		buffer.append(", cityCode=");
		buffer.append(cityCode);
		buffer.append(", ip=");
		buffer.append(ip);
		buffer.append(", status=");
		buffer.append(status);
		buffer.append(", lastUpdate=");
		buffer.append(lastUpdate);
		buffer.append("]");
		return buffer.toString();
	}

	
	
}
