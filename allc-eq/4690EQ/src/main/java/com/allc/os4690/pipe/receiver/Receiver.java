package com.allc.os4690.pipe.receiver;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.ibm.OS4690.POSFile;
import com.ibm.OS4690.POSPipeInputStream;

public class Receiver {


	static Logger log = Logger.getLogger(Receiver.class);

	private int cantBytesLongitud;
	private int cantDatosHeader;
	private POSPipeInputStream posPipeInputStream;
	private POSFile posFile;
	private String car;
	private long timeOutSleep;
	private Pattern p;
	private String pipeName;

		
	public Receiver() {
		super();
	}
	
	public int getCantBytesLongitud() {
		return cantBytesLongitud;
	}
	public int getCantDatosHeader() {
		return cantDatosHeader;
	}
	public POSPipeInputStream getPosPipeInputStream() {
		return posPipeInputStream;
	}
	public POSFile getPosFile() {
		return posFile;
	}
	public String getCar() {
		return car;
	}
	public long getTimeOutSleep() {
		return timeOutSleep;
	}
	public Pattern getP() {
		return p;
	}
	public String getNombrePipe() {
		return pipeName;
	}

	
	public void setCantBytesLongitud(int cantBytesLongitud) {
		this.cantBytesLongitud = cantBytesLongitud;
	}
	public void setCantDatosHeader(int cantDatosHeader) {
		this.cantDatosHeader = cantDatosHeader;
	}
	public void setPosPipeInputStream(POSPipeInputStream posPipeInputStream) {
		this.posPipeInputStream = posPipeInputStream;
	}
	public void setPosFile(POSFile posFile) {
		this.posFile = posFile;
	}
	public void setCar(String car) {
		this.car = car;
	}
	public void setTimeOutSleep(long timeOutSleep) {
		this.timeOutSleep = timeOutSleep;
	}
	public void setP(Pattern p) {
		this.p = p;
	}
	public void setNombrePipe(String nombrePipe) {
		this.pipeName = nombrePipe;
	}
	
	
	
}
