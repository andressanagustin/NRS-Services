package com.allc.arms.server.persistence.customer;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table (name = "PM_DEPARTAMENTO")
public class State implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4156144234829906149L;
	
	public State() {
		super();
	}

	@Id
	@Column(name="COD_DEPARTAMENTO")
	private Integer stateId;
	@Column(name="DES_DEPARTAMENTO")
	private String  name;
	@Column(name="DES_ABREVIATURA")
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("State [stateId=").append(stateId).append(", name=")
				.append(name).append(", abbreviation=").append(abbreviation)
				.append("]");
		return builder.toString();
	}


	
	
	
}
