package com.allc.saf;

import java.util.regex.Pattern;

import com.allc.conexion.Conexion;


/**
 * Pojo SAF utilizado para el paso de atributos
 * @author Alexander Padilla
 *
 */
public class SAF {


	public SAF() {
		super();
	}
	
	private String fileStore;
	private String fileSeek;
	private int	timeSAF;
	private int cantBytesLongitud;
	private int cantDatosHeader;
	private String car;
	private Pattern p;
	private Conexion conexion;
	private String crlf;


	public String getCrlf() {
		return crlf;
	}
	public void setCrlf(String crlf) {
		this.crlf = crlf;
	}
	public Conexion getConexion() {
		return conexion;
	}
	public void setConexion(Conexion conexion) {
		this.conexion = conexion;
	}
	public int getCantBytesLongitud() {
		return cantBytesLongitud;
	}
	public int getCantDatosHeader() {
		return cantDatosHeader;
	}
	public String getCar() {
		return car;
	}
	public Pattern getP() {
		return p;
	}
	public void setCantBytesLongitud(int cantBytesLongitud) {
		this.cantBytesLongitud = cantBytesLongitud;
	}
	public void setCantDatosHeader(int cantDatosHeader) {
		this.cantDatosHeader = cantDatosHeader;
	}
	public void setCar(String car) {
		this.car = car;
	}
	public void setP(Pattern p) {
		this.p = p;
	}
	public String getFileStore() {
		return fileStore;
	}
	public String getFileSeek() {
		return fileSeek;
	}
	public void setFileStore(String fileStore) {
		this.fileStore = fileStore;
	}
	public void setFileSeek(String fileSeek) {
		this.fileSeek = fileSeek;
	}
	public int getTimeSAF() {
		return timeSAF;
	}
	public void setTimeSAF(int timeSAF) {
		this.timeSAF = timeSAF;
	}
	
	public String toString() {
		return "Saf ["
				+ (fileStore != null ? "fileStore=" + fileStore + ", " : "")
				+ (fileSeek != null ? "fileSeek=" + fileSeek + ", " : "")
				+ "timeSAF=" + timeSAF + ", cantBytesLongitud="
				+ cantBytesLongitud + ", cantDatosHeader=" + cantDatosHeader
				+ ", " + (car != null ? "car=" + car + ", " : "")
				+ (p != null ? "p=" + p + ", " : "")
				+ (conexion != null ? "conexion=" + conexion + ", " : "")
				+ (crlf != null ? "crlf=" + crlf : "") + "]";
	}
	
}
