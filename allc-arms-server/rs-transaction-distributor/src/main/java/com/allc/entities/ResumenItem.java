/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class ResumenItem  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private Integer resumenItemID;
	private Long itemCode;
	private Double cantidad;
	private Double precioSinIva;
	private Double descuentoSinIva;
	private Double precioItemSinIva;
	private Double precioNetoSinIva;
	private Double tarifaIva;
	private Double valorIvaCanastas;
	private Double valorSinIvaCanastas;
	private char tipoProducto;


	public Integer getTransactionID() {
		return transactionID;
	}
	public void setTransactionID(Integer transactionID) {
		this.transactionID = transactionID;
	}
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	public Integer getResumenItemID() {
		return resumenItemID;
	}
	public void setResumenItemID(Integer resumenItemID) {
		this.resumenItemID = resumenItemID;
	}
	public Long getItemCode() {
		return itemCode;
	}
	public void setItemCode(Long itemCode) {
		this.itemCode = itemCode;
	}
	public Double getCantidad() {
		return cantidad;
	}
	public void setCantidad(Double cantidad) {
		this.cantidad = cantidad;
	}
	public Double getPrecioSinIva() {
		return precioSinIva;
	}
	public void setPrecioSinIva(Double precioSinIva) {
		this.precioSinIva = precioSinIva;
	}
	public Double getDescuentoSinIva() {
		return descuentoSinIva;
	}
	public void setDescuentoSinIva(Double descuentoSinIva) {
		this.descuentoSinIva = descuentoSinIva;
	}
	public Double getPrecioItemSinIva() {
		return precioItemSinIva;
	}
	public void setPrecioItemSinIva(Double precioItemSinIva) {
		this.precioItemSinIva = precioItemSinIva;
	}
	public Double getPrecioNetoSinIva() {
		return precioNetoSinIva;
	}
	public void setPrecioNetoSinIva(Double precioNetoSinIva) {
		this.precioNetoSinIva = precioNetoSinIva;
	}
	public Double getTarifaIva() {
		return tarifaIva;
	}
	public void setTarifaIva(Double tarifaIva) {
		this.tarifaIva = tarifaIva;
	}
	public Double getValorIvaCanastas() {
		return valorIvaCanastas;
	}
	public void setValorIvaCanastas(Double valorIvaCanastas) {
		this.valorIvaCanastas = valorIvaCanastas;
	}
	public Double getValorSinIvaCanastas() {
		return valorSinIvaCanastas;
	}
	public void setValorSinIvaCanastas(Double valorSinIvaCanastas) {
		this.valorSinIvaCanastas = valorSinIvaCanastas;
	}
	public char getTipoProducto() {
		return tipoProducto;
	}
	public void setTipoProducto(char tipoProducto) {
		this.tipoProducto = tipoProducto;
	}

	
}
