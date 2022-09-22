package com.allc.arms.server.persistence.operator;

import java.io.Serializable;

public class Indicat implements Serializable {

	private static final long serialVersionUID = 3698084113453908016L;

	private Integer idIndicat;
	private Integer indicat;

	public Integer getIdIndicat() {
		return idIndicat;
	}

	public void setIdIndicat(Integer idIndicat) {
		this.idIndicat = idIndicat;
	}

	public Integer getIndicat() {
		return indicat;
	}

	public void setIndicat(Integer indicat) {
		this.indicat = indicat;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Indicat [idIndicat=");
		builder.append(idIndicat);
		builder.append(", indicat=");
		builder.append(indicat);
		builder.append("]");
		return builder.toString();
	}
}
