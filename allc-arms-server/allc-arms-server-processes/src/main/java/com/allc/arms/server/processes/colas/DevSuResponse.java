/*
 *  ***************************************************************
 * NUO - 2020
 * Creado por:      Ing. Joel Jalon Gomez 
 * Motivo:          representacion json de respuesta servicio alertas tec
 * Fecha Creacion:  2020-11-26
 *  ***************************************************************
 */
package com.allc.arms.server.processes.colas;

public class DevSuResponse {
	
	private boolean status;
	private String errorMessage;
	//private DsError error;
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	
	

}
