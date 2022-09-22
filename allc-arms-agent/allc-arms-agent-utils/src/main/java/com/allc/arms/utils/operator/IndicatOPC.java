package com.allc.arms.utils.operator;

import java.io.Serializable;

public class IndicatOPC implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 514754998494628191L;
	
	private String idIndicatOPC;
	private String bitPos;
//	private String descripEn;
	private Indicat indicat;
	public String getIdIndicatOPC() {
		return idIndicatOPC;
	}
	public void setIdIndicatOPC(String idIndicatOPC) {
		this.idIndicatOPC = idIndicatOPC;
	}
	public String getBitPos() {
		return bitPos;
	}
	public void setBitPos(String bitPos) {
		this.bitPos = bitPos;
	}
/*	public String getDescripEn() {
		return descripEn;
	}
	public void setDescripEn(String descripEn) {
		this.descripEn = descripEn;
	}*/
	public Indicat getIndicat() {
		return indicat;
	}
	public void setIndicat(Indicat indicat) {
		this.indicat = indicat;
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("IndicatOPC [idIndicatOPC=");
		buffer.append(idIndicatOPC);
		buffer.append(", bitPos=");
		buffer.append(bitPos);
//		buffer.append(", descripEn=");
//		buffer.append(descripEn);
		buffer.append(", indicat=");
		buffer.append(indicat);
		buffer.append("]");
		return buffer.toString();
	}

}
