/*
 *  ***************************************************************
 * NUO - 2020
 * Creado por:      Ing. Joel Jalon Gomez 
 * Motivo:          xxxxx
 * Fecha Creacion:  YYYY-MM-DD
 *  ***************************************************************
 */
package com.allc.arms.server.processes.colas;

import java.util.List;

public class DevSuRequest {
	private String timeSpan;
	private List<QueueAlert> queuesAlert;
	public String getTimeSpan() {
		return timeSpan;
	}
	public void setTimeSpan(String timeSpan) {
		this.timeSpan = timeSpan;
	}
	public List<QueueAlert> getQueuesAlert() {
		return queuesAlert;
	}
	public void setQueuesAlert(List<QueueAlert> queuesAlert) {
		this.queuesAlert = queuesAlert;
	}
	
	
	

}
