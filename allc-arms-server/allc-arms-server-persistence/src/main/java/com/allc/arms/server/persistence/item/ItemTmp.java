/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.persistence.item;

import java.util.Date;

/**
 *
 * @author Tyrone Lopez
 */
public class ItemTmp {

    private Integer idItmTmp;
    private Long idItm;
    private Integer idBsnUn;
    private Date fecha;
    private Date fechaDesde;
    private Date fechaHasta;
    private Integer stock;
    private String imagen;
    private String imagenSm;

    public Integer getIdItmTmp() {
        return idItmTmp;
    }

    public void setIdItmTmp(Integer idItmTmp) {
        this.idItmTmp = idItmTmp;
    }

    public Long getIdItm() {
        return idItm;
    }

    public void setIdItm(Long idItm) {
        this.idItm = idItm;
    }

    public Integer getIdBsnUn() {
        return idBsnUn;
    }

    public void setIdBsnUn(Integer idBsnUn) {
        this.idBsnUn = idBsnUn;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Date getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public Date getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getImagenSm() {
        return imagenSm;
    }

    public void setImagenSm(String imagenSm) {
        this.imagenSm = imagenSm;
    }

}
