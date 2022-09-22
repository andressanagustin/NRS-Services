/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.processes.interfaces.webapi.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Tyrone Lopez
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageDetails {

    private int offset;
    
    private int limit;
    
    private boolean enableCount;
    
    private boolean dailyInventoryMovement;
    
    private int startDate;
    
    private int endDate;

    @JsonProperty("offset")
    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @JsonProperty("limit")
    public int getLimit() {
        return this.limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    @JsonProperty("enableCount")
    public boolean getEnableCount() {
        return this.enableCount;
    }

    public void setEnableCount(boolean enableCount) {
        this.enableCount = enableCount;
    }

    @JsonProperty("dailyInventoryMovement")
    public boolean getDailyInventoryMovement() {
        return this.dailyInventoryMovement;
    }

    public void setDailyInventoryMovement(boolean dailyInventoryMovement) {
        this.dailyInventoryMovement = dailyInventoryMovement;
    }

    @JsonProperty("startDate")
    public int getStartDate() {
        return this.startDate;
    }

    public void setStartDate(int startDate) {
        this.startDate = startDate;
    }

    @JsonProperty("endDate")
    public int getEndDate() {
        return this.endDate;
    }

    public void setEndDate(int endDate) {
        this.endDate = endDate;
    }

}
