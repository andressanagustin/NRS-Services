/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.operations.tsl.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Tyrone Lopez
 */
public class Item {

    private Long itemId;
    private String posBarCode;
    private int quantity;
    private String tipoProducto;

    public Item(Long itemId, int quantity, String tipoProducto) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.tipoProducto = tipoProducto;
    }

    @JsonProperty("item_id")
    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    @JsonProperty("quantity")
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @JsonProperty("tipoProducto")
    public String getTipoProducto() {
        return tipoProducto;
    }

    public void setTipoProducto(String tipoProducto) {
        this.tipoProducto = tipoProducto;
    }
    
//    @JsonProperty("pos_barCode")
//    public String getPosBarCode() {
//        return posBarCode;
//    }
//
//    public void setPosBarCode(String posBarCode) {
//        this.posBarCode = posBarCode;
//    }
    
    

}
