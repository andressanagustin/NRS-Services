package com.allc.arms.server.persistence.operator;

import java.io.Serializable;

public class AuthorizesOPC implements Serializable {

	private static final long serialVersionUID = 617112676340665798L;
	private Long idOperador;
//	private String idIndicatOpc;
	private Integer idIndicat;
	private Integer value;
	private IndicatOPC indicatOPC;
//
//	public String getIdIndicatOpc() {
//		return idIndicatOpc;
//	}
//
//	public void setIdIndicatOpc(String idIndicatOpc) {
//		this.idIndicatOpc = idIndicatOpc;
//	}

	public Integer getIdIndicat() {
		return idIndicat;
	}

	public void setIdIndicat(Integer idIndicat) {
		this.idIndicat = idIndicat;
	}

	public IndicatOPC getIndicatOPC() {
		return indicatOPC;
	}

	public void setIndicatOPC(IndicatOPC indicatOPC) {
		this.indicatOPC = indicatOPC;
	}

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
	 * @return the value
	 */
	public Integer getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Integer value) {
		this.value = value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AuthorizesOPC [idIndicatOpc=");
//		builder.append(idIndicatOpc);
		builder.append(", idIndicat=");
		builder.append(idIndicat);
		builder.append(", value=");
		builder.append(value);
		builder.append(", indicatOPC=");
		builder.append(indicatOPC);
		builder.append("]");
		return builder.toString();
	}

}
