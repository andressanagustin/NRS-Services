package com.allc.arms.server.persistence.store.horas;

import java.io.Serializable;
import java.util.Date;
public class StoreTime implements Serializable{
	private static final long serialVersionUID = -5594059270915229891L;
    
	private Integer id;
	private Integer idLocal;
	private Date startDate;
	private Date endDate;
	private String days;
	private Integer minutos;
	private Boolean modificado;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getIdLocal() {
		return idLocal;
	}
	public void setIdLocal(Integer idLocal) {
		this.idLocal = idLocal;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public String getDays() {
		return days;
	}
	public void setDays(String days) {
		this.days = days;
	}
	public Integer getMinutos() {
		return minutos;
	}
	public void setMinutos(Integer minutos) {
		this.minutos = minutos;
	}
	public Boolean getModificado() {
		return modificado;
	}
	public void setModificado(Boolean modificado) {
		this.modificado = modificado;
	}
	
	
}
