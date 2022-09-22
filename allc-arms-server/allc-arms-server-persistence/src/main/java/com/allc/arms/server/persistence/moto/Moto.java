/**
 * 
 */
package com.allc.arms.server.persistence.moto;

import java.io.Serializable;

/**
 * @author gustavo
 *
 */
public class Moto implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer idMoto;
	private String mandante;
	//codigo SAP
	private String material;
	private String motor;
	private String serie;
	private String numSRI;
	private String chasis;
	private String anoFabricacion;
	private String clase;
	private String color;
	private String cilindraje;
	private String capAsiento;
	private String numCPN;
	private String tieneNumCPN;
	private String fechaCPN;
	private String subcategoria;
	private String tipoCombustible;
	private String tipoCarroceria;
	private String numCKD;
	private String marca;
	private String modelo;
	private String paisOrigen;
	private String tonelaje;
	private String centro;
	private String status;
	private String ejes;
	private String ruedas;
	private Integer itemID;

	/**
	 * 
	 */
	public Moto() {
	}

	/**
	 * @return the idMoto
	 */
	public Integer getIdMoto() {
		return idMoto;
	}

	/**
	 * @param idMoto
	 *            the idMoto to set
	 */
	public void setIdMoto(Integer idMoto) {
		this.idMoto = idMoto;
	}

	/**
	 * @return the mandante
	 */
	public String getMandante() {
		return mandante;
	}

	/**
	 * @param mandante
	 *            the mandante to set
	 */
	public void setMandante(String mandante) {
		this.mandante = mandante;
	}

	/**
	 * @return the material
	 */
	public String getMaterial() {
		return material;
	}

	/**
	 * @param material
	 *            the material to set
	 */
	public void setMaterial(String material) {
		this.material = material;
	}

	/**
	 * @return the motor
	 */
	public String getMotor() {
		return motor;
	}

	/**
	 * @param motor
	 *            the motor to set
	 */
	public void setMotor(String motor) {
		this.motor = motor;
	}

	/**
	 * @return the serie
	 */
	public String getSerie() {
		return serie;
	}

	/**
	 * @param serie
	 *            the serie to set
	 */
	public void setSerie(String serie) {
		this.serie = serie;
	}

	/**
	 * @return the numSRI
	 */
	public String getNumSRI() {
		return numSRI;
	}

	/**
	 * @param numSRI
	 *            the numSRI to set
	 */
	public void setNumSRI(String numSRI) {
		this.numSRI = numSRI;
	}

	/**
	 * @return the chasis
	 */
	public String getChasis() {
		return chasis;
	}

	/**
	 * @param chasis
	 *            the chasis to set
	 */
	public void setChasis(String chasis) {
		this.chasis = chasis;
	}

	/**
	 * @return the anoFabricacion
	 */
	public String getAnoFabricacion() {
		return anoFabricacion;
	}

	/**
	 * @param anoFabricacion
	 *            the anoFabricacion to set
	 */
	public void setAnoFabricacion(String anoFabricacion) {
		this.anoFabricacion = anoFabricacion;
	}

	/**
	 * @return the clase
	 */
	public String getClase() {
		return clase;
	}

	/**
	 * @param clase
	 *            the clase to set
	 */
	public void setClase(String clase) {
		this.clase = clase;
	}

	/**
	 * @return the color
	 */
	public String getColor() {
		return color;
	}

	/**
	 * @param color
	 *            the color to set
	 */
	public void setColor(String color) {
		this.color = color;
	}

	/**
	 * @return the cilindraje
	 */
	public String getCilindraje() {
		return cilindraje;
	}

	/**
	 * @param cilindraje
	 *            the cilindraje to set
	 */
	public void setCilindraje(String cilindraje) {
		this.cilindraje = cilindraje;
	}

	/**
	 * @return the capAsiento
	 */
	public String getCapAsiento() {
		return capAsiento;
	}

	/**
	 * @param capAsiento
	 *            the capAsiento to set
	 */
	public void setCapAsiento(String capAsiento) {
		this.capAsiento = capAsiento;
	}

	/**
	 * @return the numCPN
	 */
	public String getNumCPN() {
		return numCPN;
	}

	/**
	 * @param numCPN
	 *            the numCPN to set
	 */
	public void setNumCPN(String numCPN) {
		this.numCPN = numCPN;
	}

	/**
	 * @return the tieneNumCPN
	 */
	public String getTieneNumCPN() {
		return tieneNumCPN;
	}

	/**
	 * @param tieneNumCPN
	 *            the tieneNumCPN to set
	 */
	public void setTieneNumCPN(String tieneNumCPN) {
		this.tieneNumCPN = tieneNumCPN;
	}

	/**
	 * @return the fechaCPN
	 */
	public String getFechaCPN() {
		return fechaCPN;
	}

	/**
	 * @param fechaCPN
	 *            the fechaCPN to set
	 */
	public void setFechaCPN(String fechaCPN) {
		this.fechaCPN = fechaCPN;
	}

	/**
	 * @return the subcategoria
	 */
	public String getSubcategoria() {
		return subcategoria;
	}

	/**
	 * @param subcategoria
	 *            the subcategoria to set
	 */
	public void setSubcategoria(String subcategoria) {
		this.subcategoria = subcategoria;
	}

	/**
	 * @return the tipoCombustible
	 */
	public String getTipoCombustible() {
		return tipoCombustible;
	}

	/**
	 * @param tipoCombustible
	 *            the tipoCombustible to set
	 */
	public void setTipoCombustible(String tipoCombustible) {
		this.tipoCombustible = tipoCombustible;
	}

	/**
	 * @return the tipoCarroceria
	 */
	public String getTipoCarroceria() {
		return tipoCarroceria;
	}

	/**
	 * @param tipoCarroceria
	 *            the tipoCarroceria to set
	 */
	public void setTipoCarroceria(String tipoCarroceria) {
		this.tipoCarroceria = tipoCarroceria;
	}

	/**
	 * @return the numCKD
	 */
	public String getNumCKD() {
		return numCKD;
	}

	/**
	 * @param numCKD
	 *            the numCKD to set
	 */
	public void setNumCKD(String numCKD) {
		this.numCKD = numCKD;
	}

	/**
	 * @return the marca
	 */
	public String getMarca() {
		return marca;
	}

	/**
	 * @param marca
	 *            the marca to set
	 */
	public void setMarca(String marca) {
		this.marca = marca;
	}

	/**
	 * @return the modelo
	 */
	public String getModelo() {
		return modelo;
	}

	/**
	 * @param modelo
	 *            the modelo to set
	 */
	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	/**
	 * @return the paisOrigen
	 */
	public String getPaisOrigen() {
		return paisOrigen;
	}

	/**
	 * @param paisOrigen
	 *            the paisOrigen to set
	 */
	public void setPaisOrigen(String paisOrigen) {
		this.paisOrigen = paisOrigen;
	}

	/**
	 * @return the tonelaje
	 */
	public String getTonelaje() {
		return tonelaje;
	}

	/**
	 * @param tonelaje
	 *            the tonelaje to set
	 */
	public void setTonelaje(String tonelaje) {
		this.tonelaje = tonelaje;
	}

	/**
	 * @return the centro
	 */
	public String getCentro() {
		return centro;
	}

	/**
	 * @param centro
	 *            the centro to set
	 */
	public void setCentro(String centro) {
		this.centro = centro;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the itemID
	 */
	public Integer getItemID() {
		return itemID;
	}

	/**
	 * @param itemID the itemID to set
	 */
	public void setItemID(Integer itemID) {
		this.itemID = itemID;
	}

	public String getEjes() {
		return ejes;
	}

	public void setEjes(String ejes) {
		this.ejes = ejes;
	}

	public String getRuedas() {
		return ruedas;
	}

	public void setRuedas(String ruedas) {
		this.ruedas = ruedas;
	}
	
	

}
