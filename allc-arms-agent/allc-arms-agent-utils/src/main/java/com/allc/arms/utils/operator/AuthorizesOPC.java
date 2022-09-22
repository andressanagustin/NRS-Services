package com.allc.arms.utils.operator;

import java.io.Serializable;

public class AuthorizesOPC implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7334079066389092721L;
	
	private String idModAutoriza;
//	private String idIndicatOpc;
	private String idIndicat;
	private String indActivo;
	private IndicatOPC indicatOPC;
	
	public String getIdModAutoriza() {
		return idModAutoriza;
	}
	public void setIdModAutoriza(String idModAutoriza) {
		this.idModAutoriza = idModAutoriza;
	}
//	public String getIdIndicatOpc() {
//		return idIndicatOpc;
//	}
//	public void setIdIndicatOpc(String idIndicatOpc) {
//		this.idIndicatOpc = idIndicatOpc;
//	}
	public String getIdIndicat() {
		return idIndicat;
	}
	public void setIdIndicat(String idIndicat) {
		this.idIndicat = idIndicat;
	}
	public String getIndActivo() {
		return indActivo;
	}
	public void setIndActivo(String indActivo) {
		this.indActivo = indActivo;
	}
	public IndicatOPC getIndicatOPC() {
		return indicatOPC;
	}
	public void setIndicatOPC(IndicatOPC indicatOPC) {
		this.indicatOPC = indicatOPC;
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("AuthorizesOPC [idModAutoriza=");
		buffer.append(idModAutoriza);
//		buffer.append(", idIndicatOpc=");
//		buffer.append(idIndicatOpc);
		buffer.append(", idIndicat=");
		buffer.append(idIndicat);
		buffer.append(", indActivo=");
		buffer.append(indActivo);
		buffer.append(", indicatOPC=");
		buffer.append(indicatOPC);
		buffer.append("]");
		return buffer.toString();
	}

}
