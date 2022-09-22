package com.allc.arms.server.persistence.operator;

import java.io.Serializable;
import java.util.Date;

public class OperatorStore implements Serializable {
	
	private static final long serialVersionUID = 7717786930989252429L;

	public OperatorStore() {
		super();
	}
	
	private Long	operadorId;
	private Integer codTienda;
	private Integer tipoModelo;
	private String ipTienda;
	private Integer status;
	private Integer subscribe;
	private Date fecha;
	private Integer download;
	
	
	public Long getOperadorId() {
		return operadorId;
	}
	public void setOperadorId(Long operadorId) {
		this.operadorId = operadorId;
	}
	public Integer getCodTienda() {
		return codTienda;
	}
	public void setCodTienda(Integer codTienda) {
		this.codTienda = codTienda;
	}
	public String getIpTienda() {
		return ipTienda;
	}
	public void setIpTienda(String ipTienda) {
		this.ipTienda = ipTienda;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getSubscribe() {
		return subscribe;
	}
	public void setSubscribe(Integer subscribe) {
		this.subscribe = subscribe;
	}
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Operator [operadorId=");
		builder.append(operadorId);
		builder.append(", codTienda=");
		builder.append(codTienda);
		builder.append(", status=");
		builder.append(status);
		builder.append(", subscribe=");
		builder.append(subscribe);
		builder.append(", tienda=");
		builder.append(ipTienda);
		builder.append(", download=");
		builder.append(download);
		builder.append("]");
		return builder.toString();
	}
	public Integer getTipoModelo() {
		return tipoModelo;
	}
	public void setTipoModelo(Integer tipoModelo) {
		this.tipoModelo = tipoModelo;
	}
	public Integer getDownload() {
		return download;
	}
	public void setDownload(Integer download) {
		this.download = download;
	}
	
}
