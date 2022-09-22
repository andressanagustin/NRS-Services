package com.allc.arms.server.persistence.store;

import java.io.Serializable;

/**
 *
 * @author ruben.gomez
 */
public class BusinessStore implements Serializable{
    
    private static final long serialVersionUID = -5594059270915229881L;

     private Integer storeId;
     private Integer businessId;
     
    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    @Override
    public String toString() {
        return "BusinessStore{" + "storeId=" + storeId + ", businessId=" + businessId + '}';
    }
     
     
}
