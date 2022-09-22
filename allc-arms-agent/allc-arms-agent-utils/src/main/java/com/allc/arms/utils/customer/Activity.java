package com.allc.arms.utils.customer;

import java.util.Date;

public class Activity {

	
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
	//private Integer optionFlags;
	private Integer discountGroupId;
	private Integer multiplier;
	private String targetedCouponIds;
	private String altCustomerNum;
	private Integer periodStartDate;
	private Integer periodPoints;
	private Integer periodTransCount;
	private Integer periodRedeemPoints;
	private Date	lastRedeemDate;
	private Integer cumSalesTotal;
	
	
	
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
	public Integer getPeriodStartDate() {
		return periodStartDate;
	}
	public void setPeriodStartDate(Integer periodStartDate) {
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
	/*public Integer getOptionFlags() {
		return optionFlags;
	}
	public void setOptionFlags(Integer optionFlags) {
		this.optionFlags = optionFlags;
	}*/
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Activity [customerId=");
		buffer.append(customerId);
		buffer.append(", codCampana=");
		buffer.append(codCampana);
		buffer.append(", totalPoints=");
		buffer.append(totalPoints);
		buffer.append(", totalTrans=");
		buffer.append(totalTrans);
		buffer.append(", redeemedPoints=");
		buffer.append(redeemedPoints);
		buffer.append(", autoCouponAmount=");
		buffer.append(autoCouponAmount);
		buffer.append(", lastDate=");
		buffer.append(lastDate);
		buffer.append(", lastPoints=");
		buffer.append(lastPoints);
		buffer.append(", statusLevel=");
		buffer.append(statusLevel);
		buffer.append(", messageNum=");
		buffer.append(messageNum);
		buffer.append(", discountGroupId=");
		buffer.append(discountGroupId);
		buffer.append(", multiplier=");
		buffer.append(multiplier);
		buffer.append(", targetedCouponIds=");
		buffer.append(targetedCouponIds);
		buffer.append(", altCustomerNum=");
		buffer.append(altCustomerNum);
		buffer.append(", periodStartDate=");
		buffer.append(periodStartDate);
		buffer.append(", periodPoints=");
		buffer.append(periodPoints);
		buffer.append(", periodTransCount=");
		buffer.append(periodTransCount);
		buffer.append(", periodRedeemPoints=");
		buffer.append(periodRedeemPoints);
		buffer.append(", lastRedeemDate=");
		buffer.append(lastRedeemDate);
		buffer.append(", cumSalesTotal=");
		buffer.append(cumSalesTotal);
		buffer.append("]");
		return buffer.toString();
	}
	
	
	
	
	
	
	
	
	

	
	
	
	
}
