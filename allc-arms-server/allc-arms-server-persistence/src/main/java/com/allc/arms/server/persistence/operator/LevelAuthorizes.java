package com.allc.arms.server.persistence.operator;

import java.io.Serializable;

public class LevelAuthorizes implements Serializable{
	/**
	 *  
	 */
	private static final long serialVersionUID = 617112676340665798L;
	private Long idOperador;
	private Integer idNvautoriza;
	private String value;
	

	/**
	 * @return the idOperador
	 */
	public Long getIdOperador() {
		return idOperador;
	}
	/**
	 * @param idOperador the idOperador to set
	 */
	public void setIdOperador(Long idOperador) {
		this.idOperador = idOperador;
	}

	/**
	 * @return the idNvautoriza
	 */
	public Integer getIdNvautoriza() {
		return idNvautoriza;
	}
	/**
	 * @param idNvautoriza the idNvautoriza to set
	 */
	public void setIdNvautoriza(Integer idNvautoriza) {
		this.idNvautoriza = idNvautoriza;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AuthorizesOPC [idOperador=");
		builder.append(idOperador);
		builder.append(", idNvautoriza=");
		builder.append(idNvautoriza);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}
	
	
}
