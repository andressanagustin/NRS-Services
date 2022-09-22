package com.allc.os4690.event;

import java.util.Hashtable;

import com.allc.os4690.pipe.Pi2DAO;

public class Event {

	private int cantBytesLeePipe;
	private int cantDatosHeader;
	private String desCadena;
	private String car;
	private long timeOutSleep;
	private String fileStore;
	private String crlf;
	private String pipeName;
	private String applTxtFileName;
	private String termTxtFileName;
	private String cntrTxtFileName;
	private String controllerId;
	private String storeNumber;
	private String ip;
	private String programSource;
	private Pi2DAO pi2Redirected;
	private int redirectedEvents;
	private Pi2DAO pi24690;
	private Hashtable hash;

	
	
	
	public Hashtable getHash() {
		return hash;
	}
	public void setHash(Hashtable hash) {
		this.hash = hash;
	}
	public Pi2DAO getPi2Redirected() {
		return pi2Redirected;
	}
	public Pi2DAO getPi24690() {
		return pi24690;
	}
	public void setPi2Redirected(Pi2DAO pi2Redirected) {
		this.pi2Redirected = pi2Redirected;
	}
	public void setPi24690(Pi2DAO pi24690) {
		this.pi24690 = pi24690;
	}
	public String getIp() {
		return ip;
	}
	public String getProgramSource() {
		return programSource;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public void setProgramSource(String programSource) {
		this.programSource = programSource;
	}
	public int getCantBytesLeePipe() {
		return cantBytesLeePipe;
	}
	public int getCantDatosHeader() {
		return cantDatosHeader;
	}
	public String getDesCadena() {
		return desCadena;
	}
	public String getCar() {
		return car;
	}
	public long getTimeOutSleep() {
		return timeOutSleep;
	}
	public String getFileStore() {
		return fileStore;
	}
	public String getPipeName() {
		return pipeName;
	}
	public String getApplTxtFileName() {
		return applTxtFileName;
	}
	public String getTermTxtFileName() {
		return termTxtFileName;
	}
	public String getCntrTxtFileName() {
		return cntrTxtFileName;
	}
	public String getControllerId() {
		return controllerId;
	}
	public String getStoreNumber() {
		return storeNumber;
	}
	
	
	
	public String getCrlf() {
		return crlf;
	}
	public void setCrlf(String crlf) {
		this.crlf = crlf;
	}
	public void setCantBytesLeePipe(int cantBytesLeePipe) {
		this.cantBytesLeePipe = cantBytesLeePipe;
	}
	public void setCantDatosHeader(int cantDatosHeader) {
		this.cantDatosHeader = cantDatosHeader;
	}
	public void setDesCadena(String desCadena) {
		this.desCadena = desCadena;
	}
	public void setCar(String car) {
		this.car = car;
	}
	public void setTimeOutSleep(long timeOutSleep) {
		this.timeOutSleep = timeOutSleep;
	}
	public void setFileStore(String fileStore) {
		this.fileStore = fileStore;
	}
	public void setPipeName(String pipeName) {
		this.pipeName = pipeName;
	}
	public void setApplTxtFileName(String applTxtFileName) {
		this.applTxtFileName = applTxtFileName;
	}
	public void setTermTxtFileName(String termTxtFileName) {
		this.termTxtFileName = termTxtFileName;
	}
	public void setCntrTxtFileName(String cntrTxtFileName) {
		this.cntrTxtFileName = cntrTxtFileName;
	}
	public void setControllerId(String controllerId) {
		this.controllerId = controllerId;
	}
	public void setStoreNumber(String storeNumber) {
		this.storeNumber = storeNumber;
	}
	public int getRedirectedEvents() {
		return redirectedEvents;
	}
	public void setRedirectedEvents(int redirectedEvents) {
		this.redirectedEvents = redirectedEvents;
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Event [cantBytesLeePipe=");
		buffer.append(cantBytesLeePipe);
		buffer.append(", cantDatosHeader=");
		buffer.append(cantDatosHeader);
		buffer.append(", ");
		if (desCadena != null) {
			buffer.append("desCadena=");
			buffer.append(desCadena);
			buffer.append(", ");
		}
		if (car != null) {
			buffer.append("car=");
			buffer.append(car);
			buffer.append(", ");
		}
		buffer.append("timeOutSleep=");
		buffer.append(timeOutSleep);
		buffer.append(", ");
		if (fileStore != null) {
			buffer.append("fileStore=");
			buffer.append(fileStore);
			buffer.append(", ");
		}
		if (crlf != null) {
			buffer.append("crlf=");
			buffer.append(crlf);
			buffer.append(", ");
		}
		if (pipeName != null) {
			buffer.append("pipeName=");
			buffer.append(pipeName);
			buffer.append(", ");
		}
		if (applTxtFileName != null) {
			buffer.append("applTxtFileName=");
			buffer.append(applTxtFileName);
			buffer.append(", ");
		}
		if (termTxtFileName != null) {
			buffer.append("termTxtFileName=");
			buffer.append(termTxtFileName);
			buffer.append(", ");
		}
		if (cntrTxtFileName != null) {
			buffer.append("cntrTxtFileName=");
			buffer.append(cntrTxtFileName);
			buffer.append(", ");
		}
		if (controllerId != null) {
			buffer.append("controllerId=");
			buffer.append(controllerId);
			buffer.append(", ");
		}
		if (storeNumber != null) {
			buffer.append("storeNumber=");
			buffer.append(storeNumber);
			buffer.append(", ");
		}
		if (ip != null) {
			buffer.append("ip=");
			buffer.append(ip);
			buffer.append(", ");
		}
		if (programSource != null) {
			buffer.append("programSource=");
			buffer.append(programSource);
			buffer.append(", ");
		}
		if (pi2Redirected != null) {
			buffer.append("pi2Redirected=");
			buffer.append(pi2Redirected);
			buffer.append(", ");
		}
		buffer.append("redirectedEvents=");
		buffer.append(redirectedEvents);
		buffer.append(", ");
		if (pi24690 != null) {
			buffer.append("pi24690=");
			buffer.append(pi24690);
		}
		buffer.append("]");
		return buffer.toString();
	}








	
	
	
	
}
