/**
 * 
 */
package com.allc.core;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.allc.core.process.AbstractProcess;

/**
 * @author gustavo
 *
 */
public class TestLogger extends AbstractProcess {
	private static Logger logger = Logger.getLogger(TestLogger.class);
	private boolean isEnd = false;
	private boolean finished = false;
	/**
	 * 
	 */
	public TestLogger() {
	}

	public void run() {
		process();
	}

	public void process() {
		logger.info("LEVANTÓ EL HILO!!");
		try {
			while (!isEnd) {
				logger.info("isEnd: " + isEnd);
				Thread.sleep(2000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Bajó EL HILO!!");
	}

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		
		while(!finished)
			try {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= timeToWait) {
					finished = true;
				}
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return true;
	}

	public void interrupt() {
		logger.info("Interrumpió EL HILO!!");
		super.interrupt();
	}
}
