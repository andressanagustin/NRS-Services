/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.operations.reservas;

import java.util.Date;

/**
 *
 * @author Tyrone Lopez
 */
public class ItemBean {

    private String llaveItem;
    private String cod_Item;
    private String fecha;
    private String contrato;
    private Integer cantidad;

//    private String codItem;
//    private double valorInicial;
//    private Date fechaItem;
//    private String almacen;
//    private String terinal;
//    private String tramsicion;
//    private String cajero;
//    private String userDataItem;
    public String getLlaveItem() {
        return llaveItem;
    }

    public void setLlaveItem(String llaveItem) {
        this.llaveItem = llaveItem;
    }

    public String getCod_Item() {
        return cod_Item;
    }

    public void setCod_Item(String cod_Item) {
        this.cod_Item = cod_Item;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getContrato() {
        return contrato;
    }

    public void setContrato(String contrato) {
        this.contrato = contrato;
    }

}
