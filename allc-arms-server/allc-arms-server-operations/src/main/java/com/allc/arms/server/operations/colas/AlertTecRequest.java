package com.allc.arms.server.operations.colas;

public class AlertTecRequest {
	private Integer idLocal;
	private Integer caja;
	private Integer tipoAlerta;
	private String valor;
	private String tituloAlerta;
	private String descripcionAlerta;
	public Integer getIdLocal() {
		return idLocal;
	}
	public void setIdLocal(Integer idLocal) {
		this.idLocal = idLocal;
	}
	public Integer getCaja() {
		return caja;
	}
	public void setCaja(Integer caja) {
		this.caja = caja;
	}
	public Integer getTipoAlerta() {
		return tipoAlerta;
	}
	public void setTipoAlerta(Integer tipoAlerta) {
		this.tipoAlerta = tipoAlerta;
	}
	public String getValor() {
		return valor;
	}
	public void setValor(String valor) {
		this.valor = valor;
	}
	public String getTituloAlerta() {
		return tituloAlerta;
	}
	public void setTituloAlerta(String tituloAlerta) {
		this.tituloAlerta = tituloAlerta;
	}
	public String getDescripcionAlerta() {
		return descripcionAlerta;
	}
	public void setDescripcionAlerta(String descripcionAlerta) {
		this.descripcionAlerta = descripcionAlerta;
	}
	
	
}
