package com.allc.arms.server.persistence.pinpad;

import java.io.Serializable;
import java.util.Date;

public class PinPad implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Integer idPinpad;
	private Integer codTienda;
	private String direccionIP;
	private String mascara;
	private String gateway;
	private String dirIPHostPRedA;
	private String puertoTcpHostPRedA;
	private String dirIPHostARedA;
	private String puertoTcpHostARedA;
	private String dirIPHostPRedB;
	private String puertoTcpHostPRedB;
	private String dirIPHostARedB;
	private String puertoTcpHostARedB;
	private String puertoEscucha;
	private String tidDatafast;
	private String tidMedianet;
	private String cidCaja;
	private Integer actualizar = new Integer(0);
	private Date fechaUltimoCierre;
	
	public Integer getIdPinpad() {
		return idPinpad;
	}
	public void setIdPinpad(Integer idPinpad) {
		this.idPinpad = idPinpad;
	}
	
	public Integer getCodTienda() {
		return codTienda;
	}
	public void setCodTienda(Integer codTienda) {
		this.codTienda = codTienda;
	}
	public String getDireccionIP() {
		return direccionIP;
	}
	public void setDireccionIP(String direccionIP) {
		this.direccionIP = direccionIP;
	}
	public String getMascara() {
		return mascara;
	}
	public void setMascara(String mascara) {
		this.mascara = mascara;
	}
	public String getGateway() {
		return gateway;
	}
	public void setGateway(String gateway) {
		this.gateway = gateway;
	}
	public String getDirIPHostPRedA() {
		return dirIPHostPRedA;
	}
	public void setDirIPHostPRedA(String dirIPHostPRedA) {
		this.dirIPHostPRedA = dirIPHostPRedA;
	}
	public String getPuertoTcpHostPRedA() {
		return puertoTcpHostPRedA;
	}
	public void setPuertoTcpHostPRedA(String puertoTcpHostPRedA) {
		this.puertoTcpHostPRedA = puertoTcpHostPRedA;
	}
	public String getDirIPHostARedA() {
		return dirIPHostARedA;
	}
	public void setDirIPHostARedA(String dirIPHostARedA) {
		this.dirIPHostARedA = dirIPHostARedA;
	}
	public String getPuertoTcpHostARedA() {
		return puertoTcpHostARedA;
	}
	public void setPuertoTcpHostARedA(String puertoTcpHostARedA) {
		this.puertoTcpHostARedA = puertoTcpHostARedA;
	}
	public String getDirIPHostPRedB() {
		return dirIPHostPRedB;
	}
	public void setDirIPHostPRedB(String dirIPHostPRedB) {
		this.dirIPHostPRedB = dirIPHostPRedB;
	}
	public String getPuertoTcpHostPRedB() {
		return puertoTcpHostPRedB;
	}
	public void setPuertoTcpHostPRedB(String puertoTcpHostPRedB) {
		this.puertoTcpHostPRedB = puertoTcpHostPRedB;
	}
	public String getDirIPHostARedB() {
		return dirIPHostARedB;
	}
	public void setDirIPHostARedB(String dirIPHostARedB) {
		this.dirIPHostARedB = dirIPHostARedB;
	}
	public String getPuertoTcpHostARedB() {
		return puertoTcpHostARedB;
	}
	public void setPuertoTcpHostARedB(String puertoTcpHostARedB) {
		this.puertoTcpHostARedB = puertoTcpHostARedB;
	}
	public String getPuertoEscucha() {
		return puertoEscucha;
	}
	public void setPuertoEscucha(String puertoEscucha) {
		this.puertoEscucha = puertoEscucha;
	}
	public String getTidDatafast() {
		return tidDatafast;
	}
	public void setTidDatafast(String tidDatafast) {
		this.tidDatafast = tidDatafast;
	}
	public String getTidMedianet() {
		return tidMedianet;
	}
	public void setTidMedianet(String tidMedianet) {
		this.tidMedianet = tidMedianet;
	}
	public String getCidCaja() {
		return cidCaja;
	}
	public void setCidCaja(String cidCaja) {
		this.cidCaja = cidCaja;
	}
	public Integer getActualizar() {
		return actualizar;
	}
	public void setActualizar(Integer actualizar) {
		this.actualizar = actualizar;
	}
	public Date getFechaUltimoCierre() {
		return fechaUltimoCierre;
	}
	public void setFechaUltimoCierre(Date fechaUltimoCierre) {
		this.fechaUltimoCierre = fechaUltimoCierre;
	}
	
	
	

}
