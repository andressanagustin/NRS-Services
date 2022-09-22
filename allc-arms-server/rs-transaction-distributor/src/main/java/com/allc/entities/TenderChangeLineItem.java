/**
 * 
 */
package com.allc.entities;

/**
 * @author GUSTAVOK
 * 
 */
public class TenderChangeLineItem extends TenderLineItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TenderChangeLineItem() {
		super();
		setIsChangeFlag(Boolean.TRUE);
	}
}
