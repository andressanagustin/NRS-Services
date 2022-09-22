package com.allc.arms.server.persistence.moto.file;

import java.io.Serializable;

public class MotoFile implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer motoFileId;
	private String motoFileName;
	
	public Integer getMotoFileId() {
		return motoFileId;
	}
	public void setMotoFileId(Integer motoFileId) {
		this.motoFileId = motoFileId;
	}
	public String getMotoFileName() {
		return motoFileName;
	}
	public void setMotoFileName(String motoFileName) {
		this.motoFileName = motoFileName;
	}

	
	
}
