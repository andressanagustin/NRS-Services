/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Entidad relacionada a la tabla FE_MESSAGE.
 * 
 * @author gustavo
 *
 */
public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer idMessage;
	private Date eventDate;
	private String channel;
	private String pmSource;
	private String messageGroup;
	private Integer messageNumber;
	private String sourceNumber;
	private String eventNumber;
	private Integer severity;
	private String data;
	private Equipo equipo;
	private Integer codLocal;
//	private MessageSource messageSource;
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
	/**
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}
	/**
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}
	/**
	 * @return the pmSource
	 */
	public String getPmSource() {
		return pmSource;
	}
	/**
	 * @param pmSource the pmSource to set
	 */
	public void setPmSource(String pmSource) {
		this.pmSource = pmSource;
	}
	/**
	 * @return the messageGroup
	 */
	public String getMessageGroup() {
		return messageGroup;
	}
	/**
	 * @param messageGroup the messageGroup to set
	 */
	public void setMessageGroup(String messageGroup) {
		this.messageGroup = messageGroup;
	}
	/**
	 * @return the messageNumber
	 */
	public Integer getMessageNumber() {
		return messageNumber;
	}
	/**
	 * @param messageNumber the messageNumber to set
	 */
	public void setMessageNumber(Integer messageNumber) {
		this.messageNumber = messageNumber;
	}
	/**
	 * @return the sourceNumber
	 */
	public String getSourceNumber() {
		return sourceNumber;
	}
	/**
	 * @param sourceNumber the sourceNumber to set
	 */
	public void setSourceNumber(String sourceNumber) {
		this.sourceNumber = sourceNumber;
	}
	/**
	 * @return the eventNumber
	 */
	public String getEventNumber() {
		return eventNumber;
	}
	/**
	 * @param eventNumber the eventNumber to set
	 */
	public void setEventNumber(String eventNumber) {
		this.eventNumber = eventNumber;
	}
	/**
	 * @return the severity
	 */
	public Integer getSeverity() {
		return severity;
	}
	/**
	 * @param severity the severity to set
	 */
	public void setSeverity(Integer severity) {
		this.severity = severity;
	}
	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}
	/**
	 * @param data the data to set
	 */
	public void setData(String data) {
		this.data = data;
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
	 * @return the codLocal
	 */
	public Integer getCodLocal() {
		return codLocal;
	}
	/**
	 * @param codLocal the codLocal to set
	 */
	public void setCodLocal(Integer codLocal) {
		this.codLocal = codLocal;
	}
	
//	/**
//	 * @return the messageSource
//	 */
//	public MessageSource getMessageSource() {
//		if(messageSource != null)
//			messageSource.setIdMessage(idMessage);
//		return messageSource;
//	}
//	/**
//	 * @param messageSource the messageSource to set
//	 */
//	public void setMessageSource(MessageSource messageSource) {
//		this.messageSource = messageSource;
//	}
	
}
