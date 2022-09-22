package com.allc.oswin.event;

import java.util.regex.Pattern;


public class EventWin {
	
	private String fileSeekEventWin;
	private String fileStoredEventWin;
	private String crlf;
	private Pattern p;
	private String ip;
	private String storeNumber;
	private String ctrlNode;
	private String desCadena;
	
	
	public void setFileStoredEventWin(String fileStoredEventWin) {
		this.fileStoredEventWin = fileStoredEventWin;
	}
	public String getDesCadena() {
		return desCadena;
	}
	public void setDesCadena(String desCadena) {
		this.desCadena = desCadena;
	}
	public String getIp() {
		return ip;
	}
	public String getStoreNumber() {
		return storeNumber;
	}
	public String getCtrlNode() {
		return ctrlNode;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public void setStoreNumber(String storeNumber) {
		this.storeNumber = storeNumber;
	}
	public void setCtrlNode(String ctrlNode) {
		this.ctrlNode = ctrlNode;
	}
	public Pattern getP() {
		return p;
	}
	public String getCrlf() {
		return crlf;
	}

	public String getFileSeekEventWin() {
		return fileSeekEventWin;
	}
	public String getFileStoredEventWin() {
		return fileStoredEventWin;
	}
	
	
	public void setFileSeekEventWin(String fileSeekEventWin) {
		this.fileSeekEventWin = fileSeekEventWin;
	}
	public void setFileStoreEventWin(String fileStoreEventWin) {
		this.fileStoredEventWin = fileStoreEventWin;
	}
	public void setCrlf(String crlf) {
		this.crlf = crlf;
	}
	public void setP(Pattern p) {
		this.p = p;
	}
	
	
}
