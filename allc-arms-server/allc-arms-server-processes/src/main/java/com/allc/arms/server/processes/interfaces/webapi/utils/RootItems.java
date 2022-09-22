/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.processes.interfaces.webapi.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Tyrone Lopez
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"workAreasLink","totalItems","itemList"})
public class RootItems {

    private WorkAreasLink workAreasLink;
    private PageDetails pageDetails;
    private List<ItemList> itemList;

    @JsonProperty("pageDetails")
    public PageDetails getPageDetails() {
        return this.pageDetails;
    }

    public void setPageDetails(PageDetails pageDetails) {
        this.pageDetails = pageDetails;
    }

    @JsonProperty("totalItems")
    public int getTotalItems() {
        return this.totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
    int totalItems;

    @JsonProperty("itemList")
    public List<ItemList> getItemList() {
        if(itemList==null)
        {
            itemList = new LinkedList<>();
        }
        return this.itemList;
    }

    public void setItemList(List<ItemList> itemList) {
        this.itemList = itemList;
    }

    @JsonProperty("workAreasLink")
    public WorkAreasLink getWorkAreasLink() {
        return this.workAreasLink;
    }

    public void setWorkAreasLink(WorkAreasLink workAreasLink) {
        this.workAreasLink = workAreasLink;
    }

}
