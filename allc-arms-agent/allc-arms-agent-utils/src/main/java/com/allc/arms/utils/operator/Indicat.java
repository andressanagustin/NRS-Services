package com.allc.arms.utils.operator;

import java.io.Serializable;



public class Indicat implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1116079593934356751L;
	
	private String idIndicat;
	private String indicat;
	//private String descrip_en;
	public String getIdIndicat() {
		return idIndicat;
	}
	public void setIdIndicat(String idIndicat) {
		this.idIndicat = idIndicat;
	}
	public String getIndicat() {
		return indicat;
	}
	public void setIndicat(String indicat) {
		this.indicat = indicat;
	}
/*	public String getDescrip_en() {
		return descrip_en;
	}
	public void setDescrip_en(String descrip_en) {
		this.descrip_en = descrip_en;
	}*/
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Indicat [idIndicat=");
		buffer.append(idIndicat);
		buffer.append(", indicat=");
		buffer.append(indicat);
//		buffer.append(", descrip_en=");
//		buffer.append(descrip_en);
		buffer.append("]");
		return buffer.toString();
	}
	
	
}
