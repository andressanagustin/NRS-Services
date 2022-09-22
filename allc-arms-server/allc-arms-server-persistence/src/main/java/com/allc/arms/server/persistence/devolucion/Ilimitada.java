/**
 * 
 */
package com.allc.arms.server.persistence.devolucion;

import java.io.Serializable;

/**
 * @author gustavo
 *
 */
public class Ilimitada implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer id;
	private String responseCodeConsulta;
	private String responseCodeReverso;
	/**
	 * 
	 */
	public Ilimitada() {
	}

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
	 * @return the responseCodeConsulta
	 */
	public String getResponseCodeConsulta() {
		return responseCodeConsulta;
	}

	/**
	 * @param responseCodeConsulta the responseCodeConsulta to set
	 */
	public void setResponseCodeConsulta(String responseCodeConsulta) {
		this.responseCodeConsulta = responseCodeConsulta;
	}

	/**
	 * @return the responseCodeReverso
	 */
	public String getResponseCodeReverso() {
		return responseCodeReverso;
	}

	/**
	 * @param responseCodeReverso the responseCodeReverso to set
	 */
	public void setResponseCodeReverso(String responseCodeReverso) {
		this.responseCodeReverso = responseCodeReverso;
	}


}
