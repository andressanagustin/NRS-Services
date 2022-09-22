/**
 * 
 */
package com.allc.entities;

/**
 * @author GUSTAVOK
 * 
 */
public class TillOperator extends Till {
	private Integer tenderRepositoryID;
	private Integer operatorID;
	private Operator operator;

	/**
	 * @return the operatorID
	 */
	public Integer getOperatorID() {
		return operatorID;
	}

	/**
	 * @param operatorID
	 *            the operatorID to set
	 */
	public void setOperatorID(Integer operatorID) {
		this.operatorID = operatorID;
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
	 * @return the operator
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * @param operator
	 *            the operator to set
	 */
	public void setOperator(Operator operator) {
		this.operator = operator;
	}

}
