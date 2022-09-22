/**
 * 
 */
package com.allc.arms.server;

import java.util.Timer;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;

import com.allc.arms.server.processes.params.LoadParamsProcess;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.core.process.Interrupcion;
import com.allc.core.process.LauncherProcess;
import com.allc.core.receiver.ReceiverManager;
import com.allc.properties.PropFile;

/**
 * @author gustavo
 *
 */
public class ArmsServerRunner extends AbstractService {
	protected static Logger logger;
	protected static LauncherProcess launcherProcess;
	protected static ReceiverManager receiverManager;
	/**
	 * 
	 */
	public ArmsServerRunner() {
		// TODO Auto-generated constructor stub
	}
	public int serviceMain(String[] arg0) throws ServiceException {
		try {
			execute();
			while(!shutdown){
				Thread.sleep(5000);
			}
			if(shutdown){
				logger.info("Bajando servicio");
				stopService();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}
	
	public static void stopService(){
		if (launcherProcess != null)
			launcherProcess.shutdown();
		if (receiverManager != null)
			receiverManager.shutdown();
	}
	private static void execute(){
		PropertyConfigurator.configure(ArmsServerConstants.LOG4J_PROP_FILE_NAME);
		logger = Logger.getLogger(ArmsServerRunner.class);
		logger.info(ArmsServerRunner.class.getName() + " starting service");
		
		LoadParamsProcess loadParamsProcess = new LoadParamsProcess();
		loadParamsProcess.run();
		
		PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
		boolean cargarProperties = Boolean.parseBoolean(properties.getObject("server.params.loaded"));

		if (cargarProperties) {
			launcherProcess = new LauncherProcess(ArmsServerConstants.PROP_FILE_NAME);
			final Timer queueRunner = new Timer();
			Runtime.getRuntime().addShutdownHook(new Interrupcion(queueRunner, launcherProcess));
			launcherProcess.start();
			
			receiverManager = new ReceiverManager(ArmsServerConstants.PROP_FILE_NAME);
			Runtime.getRuntime().addShutdownHook(new Interrupcion(queueRunner, receiverManager));
			receiverManager.start();
		} else
			logger.info("Error al iniciar la aplicación.");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		execute();
	}


}
