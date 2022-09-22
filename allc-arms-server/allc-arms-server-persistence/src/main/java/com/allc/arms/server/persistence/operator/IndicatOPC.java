package com.allc.arms.server.persistence.operator;

import java.io.Serializable;

public class IndicatOPC implements Serializable {

	private static final long serialVersionUID = -6232290578751970540L;

	private Integer idIndicatOPC;
	private Integer bitPos;
	private Indicat indicat;

	public Integer getIdIndicatOPC() {
		return idIndicatOPC;
	}

	public void setIdIndicatOPC(Integer idIndicatOPC) {
		this.idIndicatOPC = idIndicatOPC;
	}

	public Integer getBitPos() {
		return bitPos;
	}

	public void setBitPos(Integer bitPos) {
		this.bitPos = bitPos;
	}

	public Indicat getIndicat() {
		return indicat;
	}

	public void setIndicat(Indicat indicat) {
		this.indicat = indicat;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IndicatOPC [idIndicatOPC=");
		builder.append(idIndicatOPC);
		builder.append(", bitPos=");
		builder.append(bitPos);
		builder.append(", indicat=");
		builder.append(indicat);
		builder.append("]");
		return builder.toString();
	}

}
