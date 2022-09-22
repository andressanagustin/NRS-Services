/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * Entidad asociada a un registro de la tabla CO_CPR_CER que puede ser un RUC o una Cedula.
 * 
 * @author gustavo
 *
 */
public class CedRuc implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;
	private String codigo;
	/** C= Cedula, R = RUC */
	private String tipo;
	private Integer regInter = 0;
	private String nombre;
	private String direccion;
	private String telefono;
	private String correo;
	private String genero;
	private CedRegElec registroElec;

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
	 * @return the tipo
	 */
	public String getTipo() {
		return tipo;
	}

	/**
	 * @param tipo
	 *            the tipo to set
	 */
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	/**
	 * @return the regInter
	 */
	public Integer getRegInter() {
		return regInter;
	}

	/**
	 * @param regInter
	 *            the regInter to set
	 */
	public void setRegInter(Integer regInter) {
		this.regInter = regInter;
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
	 * @return the telefono
	 */
	public String getTelefono() {
		return telefono;
	}

	/**
	 * @param telefono
	 *            the telefono to set
	 */
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	/**
	 * @return the correo
	 */
	public String getCorreo() {
		return correo;
	}

	/**
	 * @param correo
	 *            the correo to set
	 */
	public void setCorreo(String correo) {
		this.correo = correo;
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

	/**
	 * @return the registroElec
	 */
	public CedRegElec getRegistroElec() {
		if (registroElec != null)
			registroElec.setId(id);
		return registroElec;
	}

	/**
	 * @param registroElec
	 *            the registroElec to set
	 */
	public void setRegistroElec(CedRegElec registroElec) {
		this.registroElec = registroElec;
	}

}
