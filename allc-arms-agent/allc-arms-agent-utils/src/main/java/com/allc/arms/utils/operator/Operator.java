package com.allc.arms.utils.operator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.allc.arms.utils.store.Store;

public class Operator implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3996803149882623605L;

	private String operadorId;
	private String password;
	private String Name;
	private String idModAutoriza;
	private Date fecha;
	private Store tienda;
	private Store tiendaAnt;
    private List authorizations = new ArrayList();
    
	public String getOperadorId() {
		return operadorId;
	}
	public void setOperadorId(String operadorId) {
		this.operadorId = operadorId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}

	public String getIdModAutoriza() {
		return idModAutoriza;
	}
	public void setIdModAutoriza(String idModAutoriza) {
		this.idModAutoriza = idModAutoriza;
	}
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public Store getTienda() {
		return tienda;
	}
	public void setTienda(Store tienda) {
		this.tienda = tienda;
	}
	public Store getTiendaAnt() {
		return tiendaAnt;
	}
	public void setTiendaAnt(Store tiendaAnt) {
		this.tiendaAnt = tiendaAnt;
	}
	public List getAuthorizations() {
		return authorizations;
	}
	public void setAuthorizations(List authorizations) {
		this.authorizations = authorizations;
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Operator [operadorId=");
		buffer.append(operadorId);
		buffer.append(", password=");
		buffer.append(password);
		buffer.append(", Name=");
		buffer.append(Name);
		buffer.append(", idModAutoriza=");
		buffer.append(idModAutoriza);
		buffer.append(", fecha=");
		buffer.append(fecha);
		buffer.append(", tienda=");
		buffer.append(tienda);
		buffer.append(", tiendaAnt=");
		buffer.append(tiendaAnt);
		buffer.append(", authorizations=");
		buffer.append(authorizations);
		buffer.append("]");
		return buffer.toString();
	}	
	
    
}
