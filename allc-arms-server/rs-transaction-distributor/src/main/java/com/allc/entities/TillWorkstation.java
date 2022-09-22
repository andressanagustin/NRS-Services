/**
 * 
 */
package com.allc.entities;

/**
 * @author GUSTAVOK
 * 
 */
public class TillWorkstation extends Till {

	private Integer tenderRepositoryID;
	private Integer workstationID;
	private Workstation workstation;

	/**
	 * @return the workstationID
	 */
	public Integer getWorkstationID() {
		return workstationID;
	}

	/**
	 * @param workstationID
	 *            the workstationID to set
	 */
	public void setWorkstationID(Integer workstationID) {
		this.workstationID = workstationID;
	}

	/**
	 * @return the tenderRepositoryID
	 */
	public Integer getTenderRepositoryID() {
		return tenderRepositoryID;
	}

	/**
	 * @param tenderRepositoryID
	 *            the tenderRepositoryID to set
	 */
	public void setTenderRepositoryID(Integer tenderRepositoryID) {
		this.tenderRepositoryID = tenderRepositoryID;
	}

	/**
	 * @return the workstation
	 */
	public Workstation getWorkstation() {
		return workstation;
	}

	/**
	 * @param workstation
	 *            the workstation to set
	 */
	public void setWorkstation(Workstation workstation) {
		this.workstation = workstation;
	}

}
