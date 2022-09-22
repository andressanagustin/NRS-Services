/**
 * 
 */
package com.allc.entities;

import java.util.Iterator;
import java.util.List;

/**
 * @author GUSTAVOK
 * 
 */
public class ControlTransaction {
	private Integer transactionID;
	private String typeCode;

	private SignOffTransaction signOff;
	private SignOnTransaction signOn;
	private BusinessEODTransaction businessEOD;
	private CarryForwardTransaction carryForward;

	private List managerOverrides;

	/**
	 * @return the transactionID
	 */
	public Integer getTransactionID() {
		return transactionID;
	}

	/**
	 * @param transactionID
	 *            the transactionID to set
	 */
	public void setTransactionID(Integer transactionID) {
		this.transactionID = transactionID;
	}

	/**
	 * @return the typeCode
	 */
	public String getTypeCode() {
		return typeCode;
	}

	/**
	 * @param typeCode
	 *            the typeCode to set
	 */
	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	/**
	 * @return the signOff
	 */
	public SignOffTransaction getSignOff() {
		if (signOff != null)
			signOff.setTransactionID(transactionID);
		return signOff;
	}

	/**
	 * @param signOff
	 *            the signOff to set
	 */
	public void setSignOff(SignOffTransaction signOff) {
		this.signOff = signOff;
	}

	/**
	 * @return the signOn
	 */
	public SignOnTransaction getSignOn() {
		if (signOn != null)
			signOn.setTransactionID(transactionID);
		return signOn;
	}

	/**
	 * @param signOn
	 *            the signOn to set
	 */
	public void setSignOn(SignOnTransaction signOn) {
		this.signOn = signOn;
	}

	/**
	 * @return the businessEOD
	 */
	public BusinessEODTransaction getBusinessEOD() {
		if (businessEOD != null)
			businessEOD.setTransactionID(transactionID);
		return businessEOD;
	}

	/**
	 * @param businessEOD
	 *            the businessEOD to set
	 */
	public void setBusinessEOD(BusinessEODTransaction businessEOD) {
		this.businessEOD = businessEOD;
	}

	/**
	 * @return the managerOverrides
	 */
	public List getManagerOverrides() {
		if (managerOverrides != null) {
			Iterator itManagerOverrides = managerOverrides.iterator();
			while (itManagerOverrides.hasNext())
				((ManagerOverride) itManagerOverrides.next())
						.setTransactionID(transactionID);
		}
		return managerOverrides;
	}

	/**
	 * @param managerOverrides
	 *            the managerOverrides to set
	 */
	public void setManagerOverrides(List managerOverrides) {
		this.managerOverrides = managerOverrides;
	}

	/**
	 * @return the carryForward
	 */
	public CarryForwardTransaction getCarryForward() {
		if (carryForward != null)
			carryForward.setTransactionID(transactionID);
		return carryForward;
	}

	/**
	 * @param carryForward the carryForward to set
	 */
	public void setCarryForward(CarryForwardTransaction carryForward) {
		this.carryForward = carryForward;
	}

}
