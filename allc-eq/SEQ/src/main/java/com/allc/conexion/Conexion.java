package com.allc.conexion;

public class Conexion {

	private String ip;
	private int puerto;
	private int reintentos;
	private int timeOutConexion;
	private int timeOutSleep;
	private int cantidadBytesLongitud;
	
	public Conexion() {
		super();
	}

	public String getIp() {
		return ip;
	}
	public int getPuerto() {
		return puerto;
	}
	public int getReintentos() {
		return reintentos;
	}
	public int getTimeOutConexion() {
		return timeOutConexion;
	}
	public int getTimeOutSleep() {
		return timeOutSleep;
	}
	public int getCantidadBytesLongitud() {
		return cantidadBytesLongitud;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	public void setPuerto(int puerto) {
		this.puerto = puerto;
	}
	public void setReintentos(int reintentos) {
		this.reintentos = reintentos;
	}
	public void setTimeOutConexion(int timeOutConexion) {
		this.timeOutConexion = timeOutConexion;
	}
	public void setTimeOutSleep(int timeOutSleep) {
		this.timeOutSleep = timeOutSleep;
	}
	public void setCantidadBytesLongitud(int cantidadBytesLongitud) {
		this.cantidadBytesLongitud = cantidadBytesLongitud;
	}
	
	
	
}
