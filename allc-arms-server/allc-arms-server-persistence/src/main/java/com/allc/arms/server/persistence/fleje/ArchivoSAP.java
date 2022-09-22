package com.allc.arms.server.persistence.fleje;

import java.io.Serializable;

/**
 * @author gustavo
 *
 */
public class ArchivoSAP implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private Integer codTienda;
	private Long numLote;
	private String nombreItem;
	private Integer numItems;
	private String nombreEan;
	private Integer numEans;
	private String nombreErri;
	private Integer numErris;
	private String nombreErre;
	private Integer numErres;
	private Integer status;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the codTienda
	 */
	public Integer getCodTienda() {
		return codTienda;
	}

	/**
	 * @param codTienda
	 *            the codTienda to set
	 */
	public void setCodTienda(Integer codTienda) {
		this.codTienda = codTienda;
	}

	/**
	 * @return the numLote
	 */
	public Long getNumLote() {
		return numLote;
	}

	/**
	 * @param numLote
	 *            the numLote to set
	 */
	public void setNumLote(Long numLote) {
		this.numLote = numLote;
	}

	/**
	 * @return the nombreItem
	 */
	public String getNombreItem() {
		return nombreItem;
	}

	/**
	 * @param nombreItem
	 *            the nombreItem to set
	 */
	public void setNombreItem(String nombreItem) {
		this.nombreItem = nombreItem;
	}

	/**
	 * @return the numItems
	 */
	public Integer getNumItems() {
		return numItems;
	}

	/**
	 * @param numItems
	 *            the numItems to set
	 */
	public void setNumItems(Integer numItems) {
		this.numItems = numItems;
	}

	/**
	 * @return the nombreEan
	 */
	public String getNombreEan() {
		return nombreEan;
	}

	/**
	 * @param nombreEan
	 *            the nombreEan to set
	 */
	public void setNombreEan(String nombreEan) {
		this.nombreEan = nombreEan;
	}

	/**
	 * @return the numEans
	 */
	public Integer getNumEans() {
		return numEans;
	}

	/**
	 * @param numEans
	 *            the numEans to set
	 */
	public void setNumEans(Integer numEans) {
		this.numEans = numEans;
	}

	/**
	 * @return the nombreErri
	 */
	public String getNombreErri() {
		return nombreErri;
	}

	/**
	 * @param nombreErri
	 *            the nombreErri to set
	 */
	public void setNombreErri(String nombreErri) {
		this.nombreErri = nombreErri;
	}

	/**
	 * @return the numErris
	 */
	public Integer getNumErris() {
		return numErris;
	}

	/**
	 * @param numErris
	 *            the numErris to set
	 */
	public void setNumErris(Integer numErris) {
		this.numErris = numErris;
	}

	/**
	 * @return the nombreErre
	 */
	public String getNombreErre() {
		return nombreErre;
	}

	/**
	 * @param nombreErre
	 *            the nombreErre to set
	 */
	public void setNombreErre(String nombreErre) {
		this.nombreErre = nombreErre;
	}

	/**
	 * @return the numErres
	 */
	public Integer getNumErres() {
		return numErres;
	}

	/**
	 * @param numErres
	 *            the numErres to set
	 */
	public void setNumErres(Integer numErres) {
		this.numErres = numErres;
	}

	/**
	 * @return the status
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

}
