package com.allc.os4690.exclog;

import java.util.Hashtable;
import java.util.regex.Pattern;


public class ExcLog {
	
	private String fileNameExcLog;
	private String fileSeekExcLog;
	private String fileStoreExcLog;
	private String crlf;
	private Pattern p;
	private String ip;
	private String storeNumber;
	private String ctrlNode;
	private Hashtable hash;
	
	
	public Hashtable getHash() {
		return hash;
	}
	public void setHash(Hashtable hash) {
		this.hash = hash;
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
	public String getFileNameExcLog() {
		return fileNameExcLog;
	}
	public String getFileSeekExcLog() {
		return fileSeekExcLog;
	}
	public String getFileStoreExcLog() {
		return fileStoreExcLog;
	}
	
	
	public void setFileNameExcLog(String fileNameExcLog) {
		this.fileNameExcLog = fileNameExcLog;
	}
	public void setFileSeekExcLog(String fileSeekExcLog) {
		this.fileSeekExcLog = fileSeekExcLog;
	}
	public void setFileStoreExcLog(String fileStoreExcLog) {
		this.fileStoreExcLog = fileStoreExcLog;
	}
	public void setCrlf(String crlf) {
		this.crlf = crlf;
	}
	public void setP(Pattern p) {
		this.p = p;
	}
	
	
}
