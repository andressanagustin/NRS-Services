/**
 * 
 */
package com.allc.arms.server.persistence.fleje;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * @author gustavo
 *
 */
public class Fleje implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer flejesId;
	private String name;
	private Integer numItems;
	private Integer store;
	private Integer status;
	private String lote;
	private Integer codNegocio;
	private Integer codDepto;
	private ArchivoSAP archivo;
	private ArchivoException archivoException;
	private List archivoImpList;

	/**
	 * @return the flejesId
	 */
	public Integer getFlejesId() {
		return flejesId;
	}

	/**
	 * @param flejesId
	 *            the flejesId to set
	 */
	public void setFlejesId(Integer flejesId) {
		this.flejesId = flejesId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the store
	 */
	public Integer getStore() {
		return store;
	}

	/**
	 * @param store
	 *            the store to set
	 */
	public void setStore(Integer store) {
		this.store = store;
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

	/**
	 * @return the lote
	 */
	public String getLote() {
		return lote;
	}

	/**
	 * @param lote
	 *            the lote to set
	 */
	public void setLote(String lote) {
		this.lote = lote;
	}

	/**
	 * @return the codNegocio
	 */
	public Integer getCodNegocio() {
		return codNegocio;
	}

	/**
	 * @param codNegocio
	 *            the codNegocio to set
	 */
	public void setCodNegocio(Integer codNegocio) {
		this.codNegocio = codNegocio;
	}

	/**
	 * @return the codDepto
	 */
	public Integer getCodDepto() {
		return codDepto;
	}

	/**
	 * @param codDepto
	 *            the codDepto to set
	 */
	public void setCodDepto(Integer codDepto) {
		this.codDepto = codDepto;
	}

	/**
	 * @return the archivo
	 */
	public ArchivoSAP getArchivo() {
		return archivo;
	}

	/**
	 * @param archivo
	 *            the archivo to set
	 */
	public void setArchivo(ArchivoSAP archivo) {
		this.archivo = archivo;
	}

	/**
	 * @return the archivoException
	 */
	public ArchivoException getArchivoException() {
		return archivoException;
	}

	/**
	 * @param archivoException
	 *            the archivoException to set
	 */
	public void setArchivoException(ArchivoException archivoException) {
		this.archivoException = archivoException;
	}

	/**
	 * @return the archivoImpList
	 */
	public List getArchivoImpList() {
		if (archivoImpList != null) {
			Iterator itLineItems = archivoImpList.iterator();
			while (itLineItems.hasNext())
				((ArchivoImp) itLineItems.next()).setFlejesId(flejesId);
		}
		return archivoImpList;
	}

	/**
	 * @param archivoImpList the archivoImpList to set
	 */
	public void setArchivoImpList(List archivoImpList) {
		this.archivoImpList = archivoImpList;
	}

}
