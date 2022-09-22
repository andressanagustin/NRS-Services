package com.allc.entities;

import java.io.Serializable;
import java.util.Date;

public class ItemBalanza implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String articulo;
	private String descripcion;
	private Double precio_pub;
	private Double precio_com;
	private Double  porc_recargo;
	private char  ind_iva;
	private char  ind_al_peso;
	private String  cod_seccion; 
	private String  cod_subsec;
	private String  jerarquia_sap;
	private String  nombre_grupo;
	private Date  fch_ult_cambio;
	private Integer  dias_refriger = new Integer(0);
	private Integer  dias_congelac = new Integer(0);
	private char  ind_empacado;
	private char  procesado;
	private Integer  des_clave = new Integer(0);
	private Integer id_itm;
	private char  fechaEmision;
	
	
	public String getArticulo() {
		return articulo;
	}
	public void setArticulo(String articulo) {
		this.articulo = articulo;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public Double getPrecio_pub() {
		return precio_pub;
	}
	public void setPrecio_pub(Double precio_pub) {
		this.precio_pub = precio_pub;
	}
	public Double getPrecio_com() {
		return precio_com;
	}
	public void setPrecio_com(Double precio_com) {
		this.precio_com = precio_com;
	}
	public Double getPorc_recargo() {
		return porc_recargo;
	}
	public void setPorc_recargo(Double porc_recargo) {
		this.porc_recargo = porc_recargo;
	}

	public String getCod_seccion() {
		return cod_seccion;
	}
	public void setCod_seccion(String cod_seccion) {
		this.cod_seccion = cod_seccion;
	}
	public String getCod_subsec() {
		return cod_subsec;
	}
	public void setCod_subsec(String cod_subsec) {
		this.cod_subsec = cod_subsec;
	}
	public String getJerarquia_sap() {
		return jerarquia_sap;
	}
	public void setJerarquia_sap(String jerarquia_sap) {
		this.jerarquia_sap = jerarquia_sap;
	}
	public String getNombre_grupo() {
		return nombre_grupo;
	}
	public void setNombre_grupo(String nombre_grupo) {
		this.nombre_grupo = nombre_grupo;
	}
	public Date getFch_ult_cambio() {
		return fch_ult_cambio;
	}
	public void setFch_ult_cambio(Date fch_ult_cambio) {
		this.fch_ult_cambio = fch_ult_cambio;
	}
	public Integer getDias_refriger() {
		return dias_refriger;
	}
	public void setDias_refriger(Integer dias_refriger) {
		this.dias_refriger = dias_refriger;
	}
	public Integer getDias_congelac() {
		return dias_congelac;
	}
	public void setDias_congelac(Integer dias_congelac) {
		this.dias_congelac = dias_congelac;
	}

	public Integer getDes_clave() {
		return des_clave;
	}
	public void setDes_clave(Integer des_clave) {
		this.des_clave = des_clave;
	}
	public char getInd_iva() {
		return ind_iva;
	}
	public void setInd_iva(char ind_iva) {
		this.ind_iva = ind_iva;
	}
	public char getInd_al_peso() {
		return ind_al_peso;
	}
	public void setInd_al_peso(char ind_al_peso) {
		this.ind_al_peso = ind_al_peso;
	}
	public char getInd_empacado() {
		return ind_empacado;
	}
	public void setInd_empacado(char ind_empacado) {
		this.ind_empacado = ind_empacado;
	}
	public char getProcesado() {
		return procesado;
	}
	public void setProcesado(char procesado) {
		this.procesado = procesado;
	}
	/**
	 * @return the id_itm
	 */
	public Integer getId_itm() {
		return id_itm;
	}
	/**
	 * @param id_itm the id_itm to set
	 */
	public void setId_itm(Integer id_itm) {
		this.id_itm = id_itm;
	}
	public char getFechaEmision() {
		return fechaEmision;
	}
	public void setFechaEmision(char fechaEmision) {
		this.fechaEmision = fechaEmision;
	}
	
	

}
