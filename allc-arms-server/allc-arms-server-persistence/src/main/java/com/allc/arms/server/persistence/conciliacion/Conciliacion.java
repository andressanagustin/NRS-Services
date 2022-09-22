package com.allc.arms.server.persistence.conciliacion;

import java.util.Date;

public class Conciliacion {
	
	private Integer id;
	protected String proveedorName;
	protected Integer codTienda;
	protected Date lastConciliacionDate;
	
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getProveedorName() {
		return proveedorName;
	}
	public void setProveedorName(String proveedorName) {
		this.proveedorName = proveedorName;
	}
	

	public Integer getCodTienda() {
		return codTienda;
	}
	public void setCodTienda(Integer codTienda) {
		this.codTienda = codTienda;
	}
	public Date getLastConciliacionDate() {
		return lastConciliacionDate;
	}
	public void setLastConciliacionDate(Date lastConciliacionDate) {
		this.lastConciliacionDate = lastConciliacionDate;
	}


}
