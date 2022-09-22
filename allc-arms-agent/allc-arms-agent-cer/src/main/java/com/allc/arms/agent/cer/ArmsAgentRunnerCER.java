package com.allc.arms.agent.cer;

import java.util.Timer;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.allc.arms.agent.ArmsAgentRunner;
import com.allc.arms.agent.processes.params.LoadParamsProcess;
import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.core.process.Interrupcion;
import com.allc.core.process.LauncherProcess;
import com.allc.core.receiver.ReceiverManager;
import com.allc.properties.PropFile;

/**
 * @author gustavo
 *
 */
public class ArmsAgentRunnerCER extends ArmsAgentRunner {
	protected static Logger logger;

	public static void main(String[] args) {
		/** configure the log **/
		PropertyConfigurator.configure(ArmsAgentConstants.LOG4J_PROP_FILE_NAME);
		logger = Logger.getLogger(ArmsAgentRunnerCER.class);
		logger.info(ArmsAgentRunnerCER.class.getName() + " starting service");
		
		LoadParamsProcess loadParamsProcess = new LoadParamsProcess();
		loadParamsProcess.run();
		
		PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
		
		launcherProcess = new LauncherProcess(ArmsAgentConstants.PROP_FILE_NAME);
		final Timer queueRunner = new Timer();
		Runtime.getRuntime().addShutdownHook(new Interrupcion(queueRunner, launcherProcess));
		launcherProcess.start();

		receiverManager = new ReceiverManager(ArmsAgentConstants.PROP_FILE_NAME);
		Runtime.getRuntime().addShutdownHook(new Interrupcion(queueRunner, receiverManager));
		receiverManager.start();
		
	}

}
