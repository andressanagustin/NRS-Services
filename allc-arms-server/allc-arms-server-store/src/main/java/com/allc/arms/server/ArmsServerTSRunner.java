/**
 * 
 */
package com.allc.arms.server;

import java.util.Timer;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.allc.arms.utils.ArmsServerConstants;
import com.allc.core.process.Interrupcion;
import com.allc.core.receiver.ReceiverManager;

/**
 * @author gustavo
 *
 */
public class ArmsServerTSRunner {
	protected static Logger logger;
	protected static ReceiverManager receiverManager;
	/**
	 * 
	 */
	public ArmsServerTSRunner() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure(ArmsServerConstants.LOG4J_PROP_FILE_NAME);
		logger = Logger.getLogger(ArmsServerTSRunner.class);
		logger.info(ArmsServerTSRunner.class.getName() + " starting service");
		final Timer queueRunner = new Timer();
		
		receiverManager = new ReceiverManager(ArmsServerConstants.PROP_FILE_NAME);
		Runtime.getRuntime().addShutdownHook(new Interrupcion(queueRunner, receiverManager));
		receiverManager.start();
	}

}
