package com.allc.arms.server.persistence.customer.data;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CO_CLIENTE")
public class CustomerData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8909562462568772721L;

	public CustomerData() {
		super();

	}

	@Id
	@Column(name = "COD_CLIENTE")
	private Long codCliente;
	@Column(name = "IDENTIFICACION")
	private String identificacion;
	@Column(name = "NOMBRE")
	private String nombre;
	@Column(name = "APELLIDO_P")
	private String apellidoP;
	@Column(name = "APELLIDO_M")
	private String apellidoM;
	@Column(name = "GENERO")
	private String genero;
	@Column(name = "FEC_NACIMIENTO")
	private Date fecNacimiento;
	@Column(name = "DIRECCION")
	private String direccion;
	@Column(name = "COD_DEPARTAMENTO")
	private Integer codDepartamento;
	@Column(name = "COD_CIUDAD")
	private Integer codCiudad;
	@Column(name = "TELEFONO")
	private String telefono;
	@Column(name = "EMAIL")
	private String email;
	@Column(name = "TIPOID")
	private Integer tipoId;

	/**
	 * @return the codCliente
	 */
	public Long getCodCliente() {
		return codCliente;
	}

	/**
	 * @param codCliente
	 *            the codCliente to set
	 */
	public void setCodCliente(Long codCliente) {
		this.codCliente = codCliente;
	}

	/**
	 * @return the identificacion
	 */
	public String getIdentificacion() {
		return identificacion;
	}

	/**
	 * @param identificacion
	 *            the identificacion to set
	 */
	public void setIdentificacion(String identificacion) {
		this.identificacion = identificacion;
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
	 * @return the apellidoP
	 */
	public String getApellidoP() {
		return apellidoP;
	}

	/**
	 * @param apellidoP
	 *            the apellidoP to set
	 */
	public void setApellidoP(String apellidoP) {
		this.apellidoP = apellidoP;
	}

	/**
	 * @return the apellidoM
	 */
	public String getApellidoM() {
		return apellidoM;
	}

	/**
	 * @param apellidoM
	 *            the apellidoM to set
	 */
	public void setApellidoM(String apellidoM) {
		this.apellidoM = apellidoM;
	}

	/**
	 * @return the genero
	 */
	public String getGenero() {
		return genero;
	}

	/**
	 * @param genero
	 *            the genero to set
	 */
	public void setGenero(String genero) {
		this.genero = genero;
	}

	/**
	 * @return the fecNacimiento
	 */
	public Date getFecNacimiento() {
		return fecNacimiento;
	}

	/**
	 * @param fecNacimiento
	 *            the fecNacimiento to set
	 */
	public void setFecNacimiento(Date fecNacimiento) {
		this.fecNacimiento = fecNacimiento;
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
	 * @return the codDepartamento
	 */
	public Integer getCodDepartamento() {
		return codDepartamento;
	}

	/**
	 * @param codDepartamento
	 *            the codDepartamento to set
	 */
	public void setCodDepartamento(Integer codDepartamento) {
		this.codDepartamento = codDepartamento;
	}

	/**
	 * @return the codCiudad
	 */
	public Integer getCodCiudad() {
		return codCiudad;
	}

	/**
	 * @param codCiudad
	 *            the codCiudad to set
	 */
	public void setCodCiudad(Integer codCiudad) {
		this.codCiudad = codCiudad;
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
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the tipoId
	 */
	public Integer getTipoId() {
		return tipoId;
	}

	/**
	 * @param tipoId
	 *            the tipoId to set
	 */
	public void setTipoId(Integer tipoId) {
		this.tipoId = tipoId;
	}

}
