/**
 * 
 */
package com.allc.arms.server.persistence.fleje;

import java.io.Serializable;

/**
 * @author gustavo
 *
 */
public class ArchivoException implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer flejeId;
	private Fleje fleje;
	private String nombre;
	private Integer numItems;

	/**
	 * @return the flejeId
	 */
	public Integer getFlejeId() {
		return flejeId;
	}

	/**
	 * @param flejeId
	 *            the flejeId to set
	 */
	public void setFlejeId(Integer flejeId) {
		this.flejeId = flejeId;
	}

	/**
	 * @return the fleje
	 */
	public Fleje getFleje() {
		return fleje;
	}

	/**
	 * @param fleje
	 *            the fleje to set
	 */
	public void setFleje(Fleje fleje) {
		this.fleje = fleje;
	}

	/**
	 * @return the nombre
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * @param nombre
	 *            the nombre to set
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
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

}
