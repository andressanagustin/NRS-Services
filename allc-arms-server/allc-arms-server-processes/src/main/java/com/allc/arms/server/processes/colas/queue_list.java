/*
 *  ***************************************************************
 * NUO - 2020
 * Creado por:      Ing. Joel Jalon Gomez 
 * Motivo:          xxxxx
 * Fecha Creacion:  2020-11-26
 *  ***************************************************************
 */
package com.allc.arms.server.processes.colas;

public class queue_list {
	private Integer queue_id;
	private Integer quantity;
	private Integer alert_level;
	private String description;
	public Integer getQueue_id() {
		return queue_id;
	}
	public void setQueue_id(Integer queue_id) {
		this.queue_id = queue_id;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public Integer getAlert_level() {
		return alert_level;
	}
	public void setAlert_level(Integer alert_level) {
		this.alert_level = alert_level;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	

}
