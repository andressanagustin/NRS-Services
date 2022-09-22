package com.allc.arms.server.persistence.server;

import java.io.Serializable;

public class ServerRelation  implements Serializable {
	
	private static final long serialVersionUID = 2804677598659403144L;

	private Integer id_srv_srv;
	private Integer id_srv_a;
	private Integer id_srv_b;
	private Integer estado;
	
	public Integer getId_srv_srv() {
		return id_srv_srv;
	}
	public void setId_srv_srv(Integer id_srv_srv) {
		this.id_srv_srv = id_srv_srv;
	}
	public Integer getId_srv_a() {
		return id_srv_a;
	}
	public void setId_srv_a(Integer id_srv_a) {
		this.id_srv_a = id_srv_a;
	}
	public Integer getId_srv_b() {
		return id_srv_b;
	}
	public void setId_srv_b(Integer id_srv_b) {
		this.id_srv_b = id_srv_b;
	}
	public Integer getEstado() {
		return estado;
	}
	public void setEstado(Integer estado) {
		this.estado = estado;
	}
	
}
