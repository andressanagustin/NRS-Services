package com.allc.arms.utils.customer;

import java.io.Serializable;


public class City implements Serializable{ 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2015718651990166024L;
	public City() {
		super();
	} 
	
	
    private int stateId;
    private int cityId;
	private String name;
	private Integer regId;
	private State state;

	public int getStateId() {
		return stateId;
	}
	public void setStateId(int stateId) {
		this.stateId = stateId;
	}
	public int getCityId() {
		return cityId;
	}
	public void setCityId(int cityId) {
		this.cityId = cityId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getRegId() {
		return regId;
	}
	public void setRegId(Integer regId) {
		this.regId = regId;
	}
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("City [stateId=");
		buffer.append(stateId);
		buffer.append(", cityId=");
		buffer.append(cityId);
		buffer.append(", name=");
		buffer.append(name);
		buffer.append(", regId=");
		buffer.append(regId);
		buffer.append(", state=");
		buffer.append(state);
		buffer.append("]");
		return buffer.toString();
	}
	
	
	

	
	
	
	

	
	
	

	
	
	
	
}
