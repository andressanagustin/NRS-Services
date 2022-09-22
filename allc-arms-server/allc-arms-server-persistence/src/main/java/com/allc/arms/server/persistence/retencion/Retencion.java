/**
 * 
 */
package com.allc.arms.server.persistence.retencion;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author gustavo
 *
 */
@Entity
@Table(name = "RTNC_USD")
public class Retencion implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ID_TRN")
	private Integer trxID;
	@Column(name = "USD_DT")
	private Date usedDate;
	
	
	public Integer getTrxID() {
		return trxID;
	}
	public void setTrxID(Integer trxID) {
		this.trxID = trxID;
	}
	public Date getUsedDate() {
		return usedDate;
	}
	public void setUsedDate(Date usedDate) {
		this.usedDate = usedDate;
	}
	 
}
