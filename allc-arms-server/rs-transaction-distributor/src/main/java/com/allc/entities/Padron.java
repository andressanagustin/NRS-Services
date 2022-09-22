/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * Entidad asociada a un registro de la tabla CO_PAD_CER que es Padron.
 * 
 * @author gustavo
 *
 */
public class Padron implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;
	private String codigo;
	private String recinto;
	private String direccion;
	private String mesa;
	private String junta;
	private String funcion;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the codigo
	 */
	public String getCodigo() {
		return codigo;
	}

	/**
	 * @param codigo
	 *            the codigo to set
	 */
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	/**
	 * @return the recinto
	 */
	public String getRecinto() {
		return recinto;
	}

	/**
	 * @param recinto
	 *            the recinto to set
	 */
	public void setRecinto(String recinto) {
		this.recinto = recinto;
	}

	/**
	 * @return the direccion
	 */
	public String getDireccion() {
		return direccion;
	}

	/**
	 * @param direccion
	 *            the direccion to set
	 */
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	/**
	 * @return the mesa
	 */
	public String getMesa() {
		return mesa;
	}

	/**
	 * @param mesa
	 *            the mesa to set
	 */
	public void setMesa(String mesa) {
		this.mesa = mesa;
	}

	/**
	 * @return the junta
	 */
	public String getJunta() {
		return junta;
	}

	/**
	 * @param junta
	 *            the junta to set
	 */
	public void setJunta(String junta) {
		this.junta = junta;
	}

	/**
	 * @return the funcion
	 */
	public String getFuncion() {
		return funcion;
	}

	/**
	 * @param funcion
	 *            the funcion to set
	 */
	public void setFuncion(String funcion) {
		this.funcion = funcion;
	}

}
