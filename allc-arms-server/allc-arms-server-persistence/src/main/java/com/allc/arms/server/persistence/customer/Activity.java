package com.allc.arms.server.persistence.customer;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table (name = "OP_ACTIVIDAD")
public class Activity implements Serializable{

	
	/**
	 * 
	 */
	
	private static final long serialVersionUID = -2230427189192952655L;
	

	@Id
	@Column(name = "COD_CLIENTE", nullable = false)
	private String customerId;
	@Id
	@Column(name = "COD_CAMPANIA", nullable = false)
	private String codCampana;
	@Column(name = "TOT_PUNTOS")
	private Integer totalPoints;
	@Column(name = "TOT_TRANS")
	private Integer totalTrans;
	@Column(name = "PTOS_REEMBOLSO")
	private Integer redeemedPoints;
	@Column(name = "AUTO_MONTO_TIENDA")
	private Integer autoCouponAmount;
	@Column(name = "ULT_FECHA_COMPRA")
	private Date	lastDate;
	@Column(name = "ULT_ASIGNA_PUNTO")
	private Integer lastPoints;
	@Column(name = "NIVEL_CLIENTE")
	private Integer statusLevel;
	@Column(name = "NUM_MENSAJE")
	private Integer messageNum;
	//@Column(name = "")
	
	//private Integer optionFlags;
	@Column(name = "COD_GRPDSCTO")
	private Integer discountGroupId;
	@Column(name = "MULTIPLICADOR")
	private Integer multiplier;
	@Column(name = "TAR_COUPONID")
	private String targetedCouponIds;
	@Column(name = "ALT_NUM_CLIENTE")
	private String altCustomerNum;
	@Column(name = "FEC_INI_CAMPANIA")
	private Integer periodStartDate;
	@Column(name = "TOT_PTOS_CAMPANIA")
	private Integer periodPoints;
	@Column(name = "NUM_TRAN_CAMPANIA")
	private Integer periodTransCount;
	@Column(name = "TOT_REEMB_CAMPANIA")
	private Integer periodRedeemPoints;
	@Column(name = "ULT_FEC_REEMBOLSO")
	private Date	lastRedeemDate;
	@Column(name = "SALDO_VTAS_CAMPANIA")
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


	
	/*public boolean init(){
		boolean result = false;
		try {
			
			totalPoints = 0;
			totalTrans = 0;
			redeemedPoints = 0;
			autoCouponAmount = 0;
			lastDate = new Date();
			lastPoints = 0;
			statusLevel = 0;
			messageNum = 10;
			optionFlags = 0;
			discountGroupId = 1;
			multiplier= 0;
			targetedCouponIds= "0";
			altCustomerNum= "0";
			periodStartDate = 1013 ;
			periodPoints= 0;
			periodTransCount= 0;
			periodRedeemPoints= 0;
			lastRedeemDate= new Date();
			cumSalesTotal = 0;
			filler = " ";
			result = true;
		} catch (Exception e) {
			
		}
		return result;
	}*/

}