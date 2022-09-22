package com.allc.arms.server.persistence.server;

import java.io.Serializable;

public class Server  implements Serializable {

	private static final long serialVersionUID = -6563613321123573836L;

	private Integer id_srv;
	private String detalle;
	private String ip;
	private boolean primario_app;
	private boolean primario_bd;
	private boolean regional;
	private boolean central;
	private Integer idNodo;
	private Integer id_srv_prn_chl;
	private Integer id_bsn_un_gp;
	private Integer estado;
	public Integer getId_srv() {
		return id_srv;
	}
	public void setId_srv(Integer id_srv) {
		this.id_srv = id_srv;
	}
	public String getDetalle() {
		return detalle;
	}
	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public boolean isPrimario_app() {
		return primario_app;
	}
	public void setPrimario_app(boolean primario_app) {
		this.primario_app = primario_app;
	}
	public boolean isPrimario_bd() {
		return primario_bd;
	}
	public void setPrimario_bd(boolean primario_bd) {
		this.primario_bd = primario_bd;
	}
	public boolean isRegional() {
		return regional;
	}
	public void setRegional(boolean regional) {
		this.regional = regional;
	}
	public boolean isCentral() {
		return central;
	}
	public void setCentral(boolean central) {
		this.central = central;
	}
        public Integer getIdNodo() {
            return idNodo;
        }
        public void setIdNodo(Integer idNodo) {
            this.idNodo = idNodo;
        }
	public Integer getId_srv_prn_chl() {
		return id_srv_prn_chl;
	}
	public void setId_srv_prn_chl(Integer id_srv_prn_chl) {
		this.id_srv_prn_chl = id_srv_prn_chl;
	}
	public Integer getId_bsn_un_gp() {
		return id_bsn_un_gp;
	}
	public void setId_bsn_un_gp(Integer id_bsn_un_gp) {
		this.id_bsn_un_gp = id_bsn_un_gp;
	}
	public Integer getEstado() {
		return estado;
	}
	public void setEstado(Integer estado) {
		this.estado = estado;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	
}
