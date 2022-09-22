/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.processes.interfaces.webapi.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

/**
 *
 * @author Tyrone Lopez
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Promotion {

    private String promotionCode;

    private String promotionTypeName;

    private String promotionName;

    private int carry;

    private int pay;

    private int percentage;

    private String multiplier;

    private Date startDatePromotion;

    private Date endDatePromotion;

    private String validationType;

    @JsonProperty("promotionCode")
    public String getPromotionCode() {
        return this.promotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }

    @JsonProperty("promotionTypeName")
    public String getPromotionTypeName() {
        return this.promotionTypeName;
    }

    public void setPromotionTypeName(String promotionTypeName) {
        this.promotionTypeName = promotionTypeName;
    }

    @JsonProperty("promotionName")
    public String getPromotionName() {
        return this.promotionName;
    }

    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }

    @JsonProperty("carry")
    public int getCarry() {
        return this.carry;
    }

    public void setCarry(int carry) {
        this.carry = carry;
    }

    @JsonProperty("pay")
    public int getPay() {
        return this.pay;
    }

    public void setPay(int pay) {
        this.pay = pay;
    }

    @JsonProperty("percentage")
    public int getPercentage() {
        return this.percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    @JsonProperty("multiplier")
    public String getMultiplier() {
        return this.multiplier;
    }

    public void setMultiplier(String multiplier) {
        this.multiplier = multiplier;
    }

    @JsonProperty("startDatePromotion")
    public Date getStartDatePromotion() {
        return this.startDatePromotion;
    }

    public void setStartDatePromotion(Date startDatePromotion) {
        this.startDatePromotion = startDatePromotion;
    }

    @JsonProperty("endDatePromotion")
    public Date getEndDatePromotion() {
        return this.endDatePromotion;
    }

    public void setEndDatePromotion(Date endDatePromotion) {
        this.endDatePromotion = endDatePromotion;
    }

    @JsonProperty("validationType")
    public String getValidationType() {
        return this.validationType;
    }

    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }

}
