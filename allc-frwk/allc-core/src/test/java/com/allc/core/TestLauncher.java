/**
 * 
 */
package com.allc.core;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.allc.core.process.LauncherProcess;

/**
 * @author gustavo
 *
 */
public class TestLauncher {
	private static Logger log;
	private static LauncherProcess launcherProcess;

	/**
	 * 
	 */
	public TestLauncher() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/** configure the log **/
		PropertyConfigurator.configure("log4j.properties");
		log = Logger.getLogger(TestLauncher.class);
		log.info(TestLauncher.class.getName() + " starting service");
		launcherProcess = new LauncherProcess("configurator.properties");
		launcherProcess.start();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		launcherProcess.shutdown();
//		ReceiverProcess receiverProcess = new ReceiverProcess("configurator.properties");
	}

}
