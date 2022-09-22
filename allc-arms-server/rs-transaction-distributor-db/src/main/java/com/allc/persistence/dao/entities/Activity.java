package com.allc.persistence.dao.entities;

import java.io.Serializable;
import java.util.Date;

public class Activity implements Serializable{

	
	/**
	 * 
	 */
	
	private static final long serialVersionUID = -2230427189192952655L;
	

	private String customerId;
	private String codCampana;
	private Integer totalPoints;
	private Integer totalTrans;
	private Integer redeemedPoints;
	private Integer autoCouponAmount;
	private Date	lastDate;
	private Integer lastPoints;
	private Integer statusLevel;
	private Integer messageNum;
	private Integer discountGroupId;
	private Integer multiplier;
	private String targetedCouponIds;
	private String altCustomerNum;
	private Date periodStartDate;
	private Integer periodPoints;
	private Integer periodTransCount;
	private Integer periodRedeemPoints;
	private Date	lastRedeemDate;
	private Integer cumSalesTotal;
	
	
	
	public Activity() {
		super();
		totalPoints = 0;
		totalTrans = 0;
		redeemedPoints = 0;
		autoCouponAmount = 0;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public String getCodCampana() {
		return codCampana;
	}
	public void setCodCampana(String codCampana) {
		this.codCampana = codCampana;
	}
	public Integer getTotalPoints() {
		return totalPoints;
	}
	public void setTotalPoints(Integer totalPoints) {
		this.totalPoints = totalPoints;
	}
	public Integer getTotalTrans() {
		return totalTrans;
	}
	public void setTotalTrans(Integer totalTrans) {
		this.totalTrans = totalTrans;
	}
	public Integer getRedeemedPoints() {
		return redeemedPoints;
	}
	public void setRedeemedPoints(Integer redeemedPoints) {
		this.redeemedPoints = redeemedPoints;
	}
	public Integer getAutoCouponAmount() {
		return autoCouponAmount;
	}
	public void setAutoCouponAmount(Integer autoCouponAmount) {
		this.autoCouponAmount = autoCouponAmount;
	}
	public Date getLastDate() {
		return lastDate;
	}
	public void setLastDate(Date lastDate) {
		this.lastDate = lastDate;
	}
	public Integer getLastPoints() {
		return lastPoints;
	}
	public void setLastPoints(Integer lastPoints) {
		this.lastPoints = lastPoints;
	}
	public Integer getStatusLevel() {
		return statusLevel;
	}
	public void setStatusLevel(Integer statusLevel) {
		this.statusLevel = statusLevel;
	}
	public Integer getMessageNum() {
		return messageNum;
	}
	public void setMessageNum(Integer messageNum) {
		this.messageNum = messageNum;
	}
	public Integer getDiscountGroupId() {
		return discountGroupId;
	}
	public void setDiscountGroupId(Integer discountGroupId) {
		this.discountGroupId = discountGroupId;
	}
	public Integer getMultiplier() {
		return multiplier;
	}
	public void setMultiplier(Integer multiplier) {
		this.multiplier = multiplier;
	}
	public String getTargetedCouponIds() {
		return targetedCouponIds;
	}
	public void setTargetedCouponIds(String targetedCouponIds) {
		this.targetedCouponIds = targetedCouponIds;
	}
	public String getAltCustomerNum() {
		return altCustomerNum;
	}
	public void setAltCustomerNum(String altCustomerNum) {
		this.altCustomerNum = altCustomerNum;
	}
	public Date getPeriodStartDate() {
		return periodStartDate;
	}
	public void setPeriodStartDate(Date periodStartDate) {
		this.periodStartDate = periodStartDate;
	}
	public Integer getPeriodPoints() {
		return periodPoints;
	}
	public void setPeriodPoints(Integer periodPoints) {
		this.periodPoints = periodPoints;
	}
	public Integer getPeriodTransCount() {
		return periodTransCount;
	}
	public void setPeriodTransCount(Integer periodTransCount) {
		this.periodTransCount = periodTransCount;
	}
	public Integer getPeriodRedeemPoints() {
		return periodRedeemPoints;
	}
	public void setPeriodRedeemPoints(Integer periodRedeemPoints) {
		this.periodRedeemPoints = periodRedeemPoints;
	}
	public Date getLastRedeemDate() {
		return lastRedeemDate;
	}
	public void setLastRedeemDate(Date lastRedeemDate) {
		this.lastRedeemDate = lastRedeemDate;
	}
	public Integer getCumSalesTotal() {
		return cumSalesTotal;
	}
	public void setCumSalesTotal(Integer cumSalesTotal) {
		this.cumSalesTotal = cumSalesTotal;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Activity [customerId=");
		builder.append(customerId);
		builder.append(", codCampana=");
		builder.append(codCampana);
		builder.append(", totalPoints=");
		builder.append(totalPoints);
		builder.append(", totalTrans=");
		builder.append(totalTrans);
		builder.append(", redeemedPoints=");
		builder.append(redeemedPoints);
		builder.append(", autoCouponAmount=");
		builder.append(autoCouponAmount);
		builder.append(", lastDate=");
		builder.append(lastDate);
		builder.append(", lastPoints=");
		builder.append(lastPoints);
		builder.append(", statusLevel=");
		builder.append(statusLevel);
		builder.append(", messageNum=");
		builder.append(messageNum);
		builder.append(", discountGroupId=");
		builder.append(discountGroupId);
		builder.append(", multiplier=");
		builder.append(multiplier);
		builder.append(", targetedCouponIds=");
		builder.append(targetedCouponIds);
		builder.append(", altCustomerNum=");
		builder.append(altCustomerNum);
		builder.append(", periodStartDate=");
		builder.append(periodStartDate);
		builder.append(", periodPoints=");
		builder.append(periodPoints);
		builder.append(", periodTransCount=");
		builder.append(periodTransCount);
		builder.append(", periodRedeemPoints=");
		builder.append(periodRedeemPoints);
		builder.append(", lastRedeemDate=");
		builder.append(lastRedeemDate);
		builder.append(", cumSalesTotal=");
		builder.append(cumSalesTotal);
		builder.append("]");
		return builder.toString();
	}

}
