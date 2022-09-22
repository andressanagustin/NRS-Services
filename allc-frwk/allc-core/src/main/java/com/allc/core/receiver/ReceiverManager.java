/**
 * 
 */
package com.allc.core.receiver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import com.allc.core.socket.OperationsServer;
import com.allc.properties.PropFile;

/**
 * @author gustavo
 *
 */
public class ReceiverManager extends Thread {
	private static Logger logger = Logger.getLogger(ReceiverManager.class);
	private static ExecutorService executor;
	private PropFile properties = null;
	private Map commTypeEnabled = null;
	private ReceiverSocket receiverSocket = null;
	private ReceiverPipe receiverPipe = null;
	private Map staticProperties = new HashMap();

	/**
	 * 
	 */
	public ReceiverManager(String propFileName) {
		properties = PropFile.getInstance(propFileName);
	}

	public void run() {
		int maxThreads = properties.getInt("core.threadPool.max");
		commTypeEnabled = new HashMap();
		List commTypes = properties.getList("core.communicationTypes.enabled");
		if (commTypes != null && commTypes.size() > 0) {
			for (int i = 0; i < commTypes.size(); i++) {
				commTypeEnabled.put(commTypes.get(i).toString().toUpperCase(), Boolean.TRUE);
			}
		}
		executor = Executors.newFixedThreadPool(maxThreads);
		try {
			if (commTypeEnabled.containsKey("SOCKET")) {
				receiverSocket = new ReceiverSocket(properties, executor, staticProperties);
				receiverSocket.start();
			}
			if (commTypeEnabled.containsKey("PIPE")) {
				receiverPipe = new ReceiverPipe(properties, executor, staticProperties);
				receiverPipe.start();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * shutdown the thread pool
	 */
	public boolean shutdown() {
		boolean result = false;
		try {
			stopProcess();

			if (ObjectUtils.notEqual(executor, null)) {
				executor.shutdown();
				executor.awaitTermination(properties.getInt("serverSocket.timeOutSleep"), TimeUnit.MILLISECONDS);
				executor.shutdownNow();
			}
			if (receiverSocket != null)
				receiverSocket.shutdown();
			if (receiverPipe != null)
				receiverPipe.shutdown();
			result = true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Stop Process fired by LauncherProcess class
	 * 
	 * @return
	 */
	private boolean stopProcess() {
		boolean result = false;
		try {
			OperationsServer.isEnd = true;
			result = true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}
}
