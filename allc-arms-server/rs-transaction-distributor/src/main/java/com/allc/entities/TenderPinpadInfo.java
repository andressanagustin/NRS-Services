package com.allc.entities;

import java.io.Serializable;

public class TenderPinpadInfo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer transactionID;
	private Integer sequenceNumber;
	private String codAdquiriente;
	private String codDiferido;
	private String plazoDiferido;
	private String mesesGracia;
	private Double montoTrx = new Double(0);
	private Double montoBaseGrabaIva = new Double(0);
	private Double montoBaseNoGrabaIva = new Double(0);
	private Double ivaTrx = new Double(0);
	private Double interes = new Double(0);
	private String seqTrx;
	private String horaTrx;
	private String fechaTrx;
	private String numAutorizacion;
	private String mid;
	private String tid;
	private String cid;
	private Integer tndRV = new Integer(0);
	private String arqc;
	private String aid;
	private String bankName;
	private String brandName;
	private String lote;
	private String numTarjeta;
	
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
	public String getCodAdquiriente() {
		return codAdquiriente;
	}
	public void setCodAdquiriente(String codAdquiriente) {
		this.codAdquiriente = codAdquiriente;
	}
	public String getCodDiferido() {
		return codDiferido;
	}
	public void setCodDiferido(String codDiferido) {
		this.codDiferido = codDiferido;
	}
	public String getPlazoDiferido() {
		return plazoDiferido;
	}
	public void setPlazoDiferido(String plazoDiferido) {
		this.plazoDiferido = plazoDiferido;
	}
	public String getMesesGracia() {
		return mesesGracia;
	}
	public void setMesesGracia(String mesesGracia) {
		this.mesesGracia = mesesGracia;
	}
	
	public Double getMontoTrx() {
		return montoTrx;
	}
	public void setMontoTrx(Double montoTrx) {
		this.montoTrx = montoTrx;
	}
	public Double getMontoBaseGrabaIva() {
		return montoBaseGrabaIva;
	}
	public void setMontoBaseGrabaIva(Double montoBaseGrabaIva) {
		this.montoBaseGrabaIva = montoBaseGrabaIva;
	}
	public Double getMontoBaseNoGrabaIva() {
		return montoBaseNoGrabaIva;
	}
	public void setMontoBaseNoGrabaIva(Double montoBaseNoGrabaIva) {
		this.montoBaseNoGrabaIva = montoBaseNoGrabaIva;
	}
	public Double getIvaTrx() {
		return ivaTrx;
	}
	public void setIvaTrx(Double ivaTrx) {
		this.ivaTrx = ivaTrx;
	}
	public String getSeqTrx() {
		return seqTrx;
	}
	public void setSeqTrx(String seqTrx) {
		this.seqTrx = seqTrx;
	}
	public String getHoraTrx() {
		return horaTrx;
	}
	public void setHoraTrx(String horaTrx) {
		this.horaTrx = horaTrx;
	}
	public String getFechaTrx() {
		return fechaTrx;
	}
	public void setFechaTrx(String fechaTrx) {
		this.fechaTrx = fechaTrx;
	}
	public String getNumAutorizacion() {
		return numAutorizacion;
	}
	public void setNumAutorizacion(String numAutorizacion) {
		this.numAutorizacion = numAutorizacion;
	}
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public String getTid() {
		return tid;
	}
	public void setTid(String tid) {
		this.tid = tid;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public Double getInteres() {
		return interes;
	}
	public void setInteres(Double interes) {
		this.interes = interes;
	}
	public Integer getTndRV() {
		return tndRV;
	}
	public void setTndRV(Integer tndRV) {
		this.tndRV = tndRV;
	}
	/**
	 * @return the arqc
	 */
	public String getArqc() {
		return arqc;
	}
	/**
	 * @param arqc the arqc to set
	 */
	public void setArqc(String arqc) {
		this.arqc = arqc;
	}
	/**
	 * @return the aid
	 */
	public String getAid() {
		return aid;
	}
	/**
	 * @param aid the aid to set
	 */
	public void setAid(String aid) {
		this.aid = aid;
	}
	/**
	 * @return the bankName
	 */
	public String getBankName() {
		return bankName;
	}
	/**
	 * @param bankName the bankName to set
	 */
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	/**
	 * @return the brandName
	 */
	public String getBrandName() {
		return brandName;
	}
	/**
	 * @param brandName the brandName to set
	 */
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	public String getLote() {
		return lote;
	}
	public void setLote(String lote) {
		this.lote = lote;
	}
	public String getNumTarjeta() {
		return numTarjeta;
	}
	public void setNumTarjeta(String numTarjeta) {
		this.numTarjeta = numTarjeta;
	}
	
	

}
