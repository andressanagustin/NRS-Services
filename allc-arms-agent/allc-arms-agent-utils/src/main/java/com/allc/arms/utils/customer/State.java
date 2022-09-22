package com.allc.arms.utils.customer;

import java.io.Serializable;


public class State implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4156144234829906149L;
	
	public State() {
		super();
	}

	private Integer stateId;
	private String  name;
	private String  abbreviation;
	
	public Integer getStateId() {
		return stateId;
	}

	public void setStateId(Integer stateId) {
		this.stateId = stateId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("State [stateId=");
		buffer.append(stateId);
		buffer.append(", name=");
		buffer.append(name);
		buffer.append(", abbreviation=");
		buffer.append(abbreviation);
		buffer.append("]");
		return buffer.toString();
	}




	
	
	
}
