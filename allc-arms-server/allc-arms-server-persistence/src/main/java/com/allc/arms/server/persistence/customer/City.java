package com.allc.arms.server.persistence.customer;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table (name = "PM_CIUDAD")
public class City implements Serializable{ 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2015718651990166024L;
	public City() {
		super();
	} 
	

	@Id
    @Column(name = "COD_DEPARTAMENTO", nullable = false)
    private int stateId;
	@Id
    @Column(name = "COD_CIUDAD", nullable = false)
    private int cityId;
	@Column(name="DES_CIUDAD")
	private String name;
	@Column(name="IDREG")
	private Integer regId;
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="COD_DEPARTAMENTO", referencedColumnName="COD_DEPARTAMENTO")
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
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("City [stateId=").append(stateId).append(", cityId=")
				.append(cityId).append(", name=").append(name)
				.append(", regId=").append(regId).append(", state=")
				.append(state).append("]");
		return builder.toString();
	}
	

	
	
	
	

	
	
	

	
	
	
	
}
