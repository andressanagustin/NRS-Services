/**
 * 
 */
package com.allc.entities;

import java.io.Serializable;

/**
 * @author GUSTAVOK
 * 
 */
public class RetailStore implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer retailStoreID;
	private Integer code;
	private String description;
        private String ivatax;
	private String includeTax;
	private Integer tax1;
	private Integer tax2;
	private Integer tax3;
	private Integer tax4;
	private Integer tax5;
	private Integer tax6;
	private Integer tax7;
	private Integer tax8;
	private String ceCobe;
	private Integer noAfiliadoFlag;
	private Integer idCtab;
	private String distDir;
	private Boolean flagStockLoad;
	private Integer idRetailStoreGroup;
	private RetailStoreStatus status;

        public String getIvatax() {
            return ivatax;
        }

        public void setIvatax(String ivatax) {
            this.ivatax = ivatax;
        }
        
	/**
	 * @return the retailStoreID
	 */
	public Integer getRetailStoreID() {
		return retailStoreID;
	}

	/**
	 * @param retailStoreID
	 *            the retailStoreID to set
	 */
	public void setRetailStoreID(Integer retailStoreID) {
		this.retailStoreID = retailStoreID;
	}

	/**
	 * @return the code
	 */
	public Integer getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(Integer code) {
		this.code = code;
	}

	/**
	 * @return the includeTax
	 */
	public String getIncludeTax() {
		return includeTax;
	}

	/**
	 * @param includeTax
	 *            the includeTax to set
	 */
	public void setIncludeTax(String includeTax) {
		this.includeTax = includeTax;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the tax1
	 */
	public Integer getTax1() {
		return tax1;
	}

	/**
	 * @param tax1
	 *            the tax1 to set
	 */
	public void setTax1(Integer tax1) {
		this.tax1 = tax1;
	}

	/**
	 * @return the tax2
	 */
	public Integer getTax2() {
		return tax2;
	}

	/**
	 * @param tax2
	 *            the tax2 to set
	 */
	public void setTax2(Integer tax2) {
		this.tax2 = tax2;
	}

	/**
	 * @return the tax3
	 */
	public Integer getTax3() {
		return tax3;
	}

	/**
	 * @param tax3
	 *            the tax3 to set
	 */
	public void setTax3(Integer tax3) {
		this.tax3 = tax3;
	}

	/**
	 * @return the tax4
	 */
	public Integer getTax4() {
		return tax4;
	}

	/**
	 * @param tax4
	 *            the tax4 to set
	 */
	public void setTax4(Integer tax4) {
		this.tax4 = tax4;
	}

	/**
	 * @return the tax5
	 */
	public Integer getTax5() {
		return tax5;
	}

	/**
	 * @param tax5
	 *            the tax5 to set
	 */
	public void setTax5(Integer tax5) {
		this.tax5 = tax5;
	}

	/**
	 * @return the tax6
	 */
	public Integer getTax6() {
		return tax6;
	}

	/**
	 * @param tax6
	 *            the tax6 to set
	 */
	public void setTax6(Integer tax6) {
		this.tax6 = tax6;
	}

	/**
	 * @return the tax7
	 */
	public Integer getTax7() {
		return tax7;
	}

	/**
	 * @param tax7
	 *            the tax7 to set
	 */
	public void setTax7(Integer tax7) {
		this.tax7 = tax7;
	}

	/**
	 * @return the tax8
	 */
	public Integer getTax8() {
		return tax8;
	}

	/**
	 * @param tax8
	 *            the tax8 to set
	 */
	public void setTax8(Integer tax8) {
		this.tax8 = tax8;
	}

	/**
	 * @return the noAfiliadoFlag
	 */
	public Integer getNoAfiliadoFlag() {
		return noAfiliadoFlag;
	}

	/**
	 * @param noAfiliadoFlag the noAfiliadoFlag to set
	 */
	public void setNoAfiliadoFlag(Integer noAfiliadoFlag) {
		this.noAfiliadoFlag = noAfiliadoFlag;
	}

	/**
	 * @return the status
	 */
	public RetailStoreStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(RetailStoreStatus status) {
		this.status = status;
	}

	public String getDistDir() {
		return distDir;
	}

	public void setDistDir(String distDir) {
		this.distDir = distDir;
	}
        
        /**
	 * @return the ceCobe
	 */
	public String getCeCobe() {
		return ceCobe;
	}
	/**
	 * @param ceCobe
	 *            the ceCobe to set
	 */
	public void setCeCobe(String ceCobe) {
		this.ceCobe = ceCobe;
	}
        
        /**
	 * @return the idCtab
	 */
	public Integer getIdCtab() {
		return idCtab;
	}
	/**
	 * @param idCtab
	 *            the idCtab to set
	 */
	public void setIdCtab(Integer idCtab) {
		this.idCtab = idCtab;
	}
        
        /**
	 * @return the flagStockLoad
	 */
	public Boolean getFlagStockLoad() {
		return flagStockLoad;
	}
	/**
	 * @param flagStockLoad
	 *            the flagStockLoad to set
	 */
	public void setFlagStockLoad(Boolean flagStockLoad) {
		this.flagStockLoad = flagStockLoad;
	}
        
        /**
	 * @return the idRetailStoreGroup
	 */
	public Integer getIdRetailStoreGroup() {
		return idRetailStoreGroup;
	}
	/**
	 * @param idRetailStoreGroup
	 *            the idRetailStoreGroup to set
	 */
	public void setIdRetailStoreGroup(Integer idRetailStoreGroup) {
		this.idRetailStoreGroup = idRetailStoreGroup;
	}

    @Override
    public String toString() {
        return "RetailStore{" + "retailStoreID=" + retailStoreID + ", code=" + code + ", description=" + description + ", ivatax=" + ivatax + ", includeTax=" + includeTax + ", tax1=" + tax1 + ", tax2=" + tax2 + ", tax3=" + tax3 + ", tax4=" + tax4 + ", tax5=" + tax5 + ", tax6=" + tax6 + ", tax7=" + tax7 + ", tax8=" + tax8 + ", coCobe=" + ceCobe + ", noAfiliadoFlag=" + noAfiliadoFlag + ", idCtab=" + idCtab + ", distDir=" + distDir + ", flagStockLoad=" + flagStockLoad + ", idRetailStoreGroup=" + idRetailStoreGroup + ", status=" + status + '}';
    }
        
        
	
}
