package com.allc.entities;

import java.io.Serializable;

public class DatosEmpleados implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer transactionID;
	private Integer sequenceNumber;
	private String numTerminal;
	private String numTrx;
	private String numEmpleado;
	private String idPortador;
	private String nombrePortador;
	private String tarjetaPistaA;
	private String tarjetaPistaB;
	
	public Integer getTransactionID() {
		return transactionID;
	}
	public void setTransactionID(Integer transactionID) {
		this.transactionID = transactionID;
	}
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	public String getNumTerminal() {
		return numTerminal;
	}
	public void setNumTerminal(String numTerminal) {
		this.numTerminal = numTerminal;
	}
	public String getNumTrx() {
		return numTrx;
	}
	public void setNumTrx(String numTrx) {
		this.numTrx = numTrx;
	}
	public String getNumEmpleado() {
		return numEmpleado;
	}
	public void setNumEmpleado(String numEmpleado) {
		this.numEmpleado = numEmpleado;
	}
	public String getIdPortador() {
		return idPortador;
	}
	public void setIdPortador(String idPortador) {
		this.idPortador = idPortador;
	}
	public String getNombrePortador() {
		return nombrePortador;
	}
	public void setNombrePortador(String nombrePortador) {
		this.nombrePortador = nombrePortador;
	}
	public String getTarjetaPistaA() {
		return tarjetaPistaA;
	}
	public void setTarjetaPistaA(String tarjetaPistaA) {
		this.tarjetaPistaA = tarjetaPistaA;
	}
	public String getTarjetaPistaB() {
		return tarjetaPistaB;
	}
	public void setTarjetaPistaB(String tarjetaPistaB) {
		this.tarjetaPistaB = tarjetaPistaB;
	}
	
	
	

}
