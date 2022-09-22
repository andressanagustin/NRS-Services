/**
 * 
 */
package com.allc.entities;

import java.util.Date;

/**
 * Entidad relacionada a la tabla FFQ_MESSAGE.
 * 
 * @author gustavo
 *
 */
public class MessageSource {
	private Integer idMessage;
	private Equipo equipo;
	private String idLocal;
	private Date eventDate;


	/**
	 * @return the idMessage
	 */
	public Integer getIdMessage() {
		return idMessage;
	}
	/**
	 * @param idMessage the idMessage to set
	 */
	public void setIdMessage(Integer idMessage) {
		this.idMessage = idMessage;
	}
	/**
	 * @return the equipo
	 */
	public Equipo getEquipo() {
		return equipo;
	}
	/**
	 * @param equipo the equipo to set
	 */
	public void setEquipo(Equipo equipo) {
		this.equipo = equipo;
	}
	/**
	 * @return the idLocal
	 */
	public String getIdLocal() {
		return idLocal;
	}
	/**
	 * @param idLocal the idLocal to set
	 */
	public void setIdLocal(String idLocal) {
		this.idLocal = idLocal;
	}
	/**
	 * @return the eventDate
	 */
	public Date getEventDate() {
		return eventDate;
	}
	/**
	 * @param eventDate the eventDate to set
	 */
	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}
	
	
}
