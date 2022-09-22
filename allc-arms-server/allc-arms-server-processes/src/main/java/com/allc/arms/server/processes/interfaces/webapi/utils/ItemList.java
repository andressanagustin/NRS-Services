/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.processes.interfaces.webapi.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Tyrone Lopez
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemList {

    private String itemCode;
    private String barcode;
    private String posBarcode;
    private String description;
    private String divisionCode;
    private String division;
    private String departmentCode;
    private String department;
    private String categoryCode;
    private String category;

    private String subclassificationCode;
    private String subclassification;
    private String itemClass;
    private String brand;
    private Integer stock;
    private Integer stockDistributionCenter;
    private boolean bulkSale;
    private boolean valueAddedTax;
    private double percentageTax;
    private Date offerStartDate;
    private Date offerEndDate;
    private Double offerPercentage;
    private Double offerPriceWithTax;
    private Double offerPriceWithoutTax;
    private double affiliatePriceWithTax;
    private double affiliatePriceWithoutTax;
    private double nonAffiliatePriceWithTax;
    private double nonAffiliatePriceWithoutTax;
    private String presentation;
    private String classification;
    private List<Image> images;
    private List<Feature> features;
    private List<Promotion> promotions;
    private List<String> locations;
    private List<String> relatedArticles;

    @JsonProperty("itemCode")
    public String getItemCode() {
        return this.itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    @JsonProperty("barcode")
    public String getBarcode() {
        return this.barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    @JsonProperty("posBarcode")
    public String getPosBarcode() {
        return this.posBarcode;
    }

    public void setPosBarcode(String posBarcode) {
        this.posBarcode = posBarcode;
    }

    @JsonProperty("description")
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("divisionCode")
    public String getDivisionCode() {
        return divisionCode;
    }

    public void setDivisionCode(String divisionCode) {
        this.divisionCode = divisionCode;
    }

    @JsonProperty("division")
    public String getDivision() {
        return this.division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    @JsonProperty("departmentCode")
    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    @JsonProperty("department")
    public String getDepartment() {
        return this.department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @JsonProperty("categoryCode")
    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    @JsonProperty("category")
    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @JsonProperty("subclassificationCode")
    public String getSubclassificationCode() {
        return subclassificationCode;
    }

    public void setSubclassificationCode(String subclassificationCode) {
        this.subclassificationCode = subclassificationCode;
    }

    @JsonProperty("subclassification")
    public String getSubclassification() {
        return subclassification;
    }

    public void setSubclassification(String subclassification) {
        this.subclassification = subclassification;
    }

    @JsonProperty("itemClass")
    public String getItemClass() {
        return this.itemClass;
    }

    public void setItemClass(String itemClass) {
        this.itemClass = itemClass;
    }

    @JsonProperty("brand")
    public String getBrand() {
        return this.brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    @JsonProperty("stock")
    public Integer getStock() {
        return this.stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    @JsonProperty("stockDistributionCenter")
    public Integer getStockDistributionCenter() {
        return this.stockDistributionCenter;
    }

    public void setStockDistributionCenter(Integer stockDistributionCenter) {
        this.stockDistributionCenter = stockDistributionCenter;
    }

    @JsonProperty("bulkSale")
    public boolean getBulkSale() {
        return this.bulkSale;
    }

    public void setBulkSale(boolean bulkSale) {
        this.bulkSale = bulkSale;
    }

    @JsonProperty("valueAddedTax")
    public boolean getValueAddedTax() {
        return this.valueAddedTax;
    }

    public void setValueAddedTax(boolean valueAddedTax) {
        this.valueAddedTax = valueAddedTax;
    }

    @JsonProperty("percentageTax")
    public double getPercentageTax() {
        return this.percentageTax;
    }

    public void setPercentageTax(double percentageTax) {
        this.percentageTax = percentageTax;
    }

    @JsonProperty("affiliatePriceWithTax")
    public double getAffiliatePriceWithTax() {
        return affiliatePriceWithTax;
    }

    public void setAffiliatePriceWithTax(double affiliatePriceWithTax) {
        this.affiliatePriceWithTax = affiliatePriceWithTax;
    }

    @JsonProperty("affiliatePriceWithoutTax")
    public double getAffiliatePriceWithoutTax() {
        return this.affiliatePriceWithoutTax;
    }

    public void setAffiliatePriceWithoutTax(double affiliatePriceWithoutTax) {
        this.affiliatePriceWithoutTax = affiliatePriceWithoutTax;
    }

    @JsonProperty("nonAffiliatePriceWithTax")
    public double getNonAffiliatePriceWithTax() {
        return this.nonAffiliatePriceWithTax;
    }

    public void setNonAffiliatePriceWithTax(double nonAffiliatePriceWithTax) {
        this.nonAffiliatePriceWithTax = nonAffiliatePriceWithTax;
    }

    @JsonProperty("nonAffiliatePriceWithoutTax")
    public double getNonAffiliatePriceWithoutTax() {
        return this.nonAffiliatePriceWithoutTax;
    }

    public void setNonAffiliatePriceWithoutTax(double nonAffiliatePriceWithoutTax) {
        this.nonAffiliatePriceWithoutTax = nonAffiliatePriceWithoutTax;
    }

    @JsonProperty("presentation")
    public String getPresentation() {
        return this.presentation;
    }

    public void setPresentation(String presentation) {
        this.presentation = presentation;
    }

    @JsonProperty("classification")
    public String getClassification() {
        return this.classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    @JsonProperty("images")
    public List<Image> getImages() {
        return this.images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    @JsonProperty("promotions")
    public List<Promotion> getPromotions() {
        return this.promotions;
    }

    public void setPromotions(List<Promotion> promotions) {
        this.promotions = promotions;
    }

    @JsonProperty("features")
    public List<Feature> getFeatures() {
        return this.features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    @JsonProperty("offerStartDate")
    public Date getOfferStartDate() {
        return this.offerStartDate;
    }

    public void setOfferStartDate(Date offerStartDate) {
        this.offerStartDate = offerStartDate;
    }

    @JsonProperty("offerEndDate")
    public Date getOfferEndDate() {
        return this.offerEndDate;
    }

    public void setOfferEndDate(Date offerEndDate) {
        this.offerEndDate = offerEndDate;
    }

    @JsonProperty("offerPercentage")
    public Double getOfferPercentage() {
        return this.offerPercentage;
    }

    public void setOfferPercentage(Double offerPercentage) {
        this.offerPercentage = offerPercentage;
    }

    @JsonProperty("offerPriceWithTax")
    public Double getOfferPriceWithTax() {
        return this.offerPriceWithTax;
    }

    public void setOfferPriceWithTax(Double offerPriceWithTax) {
        this.offerPriceWithTax = offerPriceWithTax;
    }

    @JsonProperty("offerPriceWithoutTax")
    public Double getOfferPriceWithoutTax() {
        return this.offerPriceWithoutTax;
    }

    public void setOfferPriceWithoutTax(Double offerPriceWithoutTax) {
        this.offerPriceWithoutTax = offerPriceWithoutTax;
    }

    @JsonProperty("locations")
    public List<String> getLocations() {
        return this.locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    @JsonProperty("relatedArticles")
    public List<String> getRelatedArticles() {
        return relatedArticles;
    }

    public void setRelatedArticles(List<String> relatedArticles) {
        this.relatedArticles = relatedArticles;
    }

}
