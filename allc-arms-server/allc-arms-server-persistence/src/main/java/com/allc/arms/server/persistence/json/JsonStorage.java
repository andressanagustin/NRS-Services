/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.persistence.json;

import java.util.Date;

/**
 *
 * @author Tyrone Lopez
 */
public class JsonStorage {
    
    private Long id;
    
    private String store;
    
    private Date upload;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public Date getUpload() {
        return upload;
    }

    public void setUpload(Date upload) {
        this.upload = upload;
    }
    
    
}
