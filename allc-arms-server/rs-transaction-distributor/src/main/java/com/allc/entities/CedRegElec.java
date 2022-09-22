/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * Entidad asociada a un registro de la tabla CO_REL_CER que son datos de Registro Electoral.
 * 
 * @author gustavo
 *
 */
public class CedRegElec implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;
	private String provincia;
	private String canton;
	private String parroquia;
	private String zona;
	private String recinto;
	private String mesa;
	private String junta;
	private String circunscripcion;
	private String funcion;
	private String genero;
	
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
	 * @return the provincia
	 */
	public String getProvincia() {
		return provincia;
	}

	/**
	 * @param provincia
	 *            the provincia to set
	 */
	public void setProvincia(String provincia) {
		this.provincia = provincia;
	}

	/**
	 * @return the canton
	 */
	public String getCanton() {
		return canton;
	}

	/**
	 * @param canton
	 *            the canton to set
	 */
	public void setCanton(String canton) {
		this.canton = canton;
	}

	/**
	 * @return the parroquia
	 */
	public String getParroquia() {
		return parroquia;
	}

	/**
	 * @param parroquia
	 *            the parroquia to set
	 */
	public void setParroquia(String parroquia) {
		this.parroquia = parroquia;
	}

	/**
	 * @return the zona
	 */
	public String getZona() {
		return zona;
	}

	/**
	 * @param zona
	 *            the zona to set
	 */
	public void setZona(String zona) {
		this.zona = zona;
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
	 * @return the circunscripcion
	 */
	public String getCircunscripcion() {
		return circunscripcion;
	}

	/**
	 * @param circunscripcion
	 *            the circunscripcion to set
	 */
	public void setCircunscripcion(String circunscripcion) {
		this.circunscripcion = circunscripcion;
	}

	/**
	 * @return the funcion
	 */
	public String getFuncion() {
		return funcion;
	}

	/**
	 * @param funcion the funcion to set
	 */
	public void setFuncion(String funcion) {
		this.funcion = funcion;
	}

	/**
	 * @return the genero
	 */
	public String getGenero() {
		return genero;
	}

	/**
	 * @param genero the genero to set
	 */
	public void setGenero(String genero) {
		this.genero = genero;
	}

}
