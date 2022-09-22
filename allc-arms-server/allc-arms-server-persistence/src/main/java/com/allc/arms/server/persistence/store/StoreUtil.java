/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.persistence.store;

import com.allc.arms.server.persistence.equipo.Equipo;
import com.allc.entities.RetailStore;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.List;

/**
 *
 * @author ruben.gomez
 */
public class StoreUtil {
    @XStreamAlias("store")
    private Store store;
    @XStreamAlias(impl = List.class, value = "equipos")
    private List<Equipo> equipos;
    @XStreamAlias("retailStore")
    private RetailStore retailStore;
    @XStreamAlias(impl = List.class, value ="businessStores")
    private List<BusinessStore> businessStores;
    
    public List<BusinessStore> getBusinessStores() {
        return businessStores;
    }

    public void setBusinessStores(List<BusinessStore> businessStores) {
        this.businessStores = businessStores;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public List<Equipo> getEquipos() {
        return equipos;
    }

    public void setEquipos(List<Equipo> equipos) {
        this.equipos = equipos;
    }

    public RetailStore getRetailStore() {
        return retailStore;
    }

    public void setRetailStore(RetailStore retailStore) {
        this.retailStore = retailStore;
    }

}
