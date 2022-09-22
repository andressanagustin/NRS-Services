/**
 * 
 */
package com.allc.core.process;

/**
 * @author gustavo
 *
 */
public abstract class AbstractProcess extends Thread {
	public abstract boolean shutdown(long timeToWait);

}
