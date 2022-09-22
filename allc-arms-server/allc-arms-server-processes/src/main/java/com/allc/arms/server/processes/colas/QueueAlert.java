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

public class QueueAlert {
	private Integer store_id;
	private List<queue_list> queue_list;
	public Integer getStore_id() {
		return store_id;
	}
	public void setStore_id(Integer store_id) {
		this.store_id = store_id;
	}
	public List<queue_list> getQueue_list() {
		return queue_list;
	}
	public void setQueue_list(List<queue_list> queue_list) {
		this.queue_list = queue_list;
	}
	
	

}
