/**
 * 
 */
package com.allc.core.receiver;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.socket.OperationsServer;
import com.allc.properties.PropFile;

/**
 * @author gustavo
 *
 */
public class ReceiverSocket extends Thread {
	private static Logger logger = Logger.getLogger(ReceiverSocket.class);
	private ExecutorService executor;
	private boolean end = false;
	private PropFile properties = null;
	private ServerSocket serverSocket = null;
	private Map staticProperties = new HashMap();
	/**
	 * 
	 */
	public ReceiverSocket(PropFile properties, ExecutorService executor, Map staticProperties) {
		this.properties = properties;
		this.executor = executor;
		this.staticProperties = staticProperties;
	}

	public void run() {
		Runnable worker;
		int quantityBytesLength = properties.getInt("serverSocket.quantityBytesLength");
		int timeOutConnection = properties.getInt("serverSocket.timeOutConnection");
		int timeOutSleep = properties.getInt("serverSocket.timeOutSleep");
		openSocket();
		try {
			while (!end) {
				worker = new OperationsServer(new ConnSocketServer(serverSocket.accept(), quantityBytesLength, timeOutConnection,
						timeOutSleep), properties);
				((OperationsServer) worker).staticProperties = staticProperties;
				executor.execute(worker);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Method to open a socket
	 */
	public boolean openSocket() {
		boolean result = false;
		try {
			serverSocket = new ServerSocket(properties.getInt("serverSocket.port"), 100);
			logger.info("Server Socket escuchando en el puerto: " + properties.getInt("serverSocket.port"));
			result = true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Method to close a socket
	 */
	public boolean closeSocket() {
		boolean result = false;
		try {
			serverSocket.close();
			logger.info("Server detenido en el puerto: " + properties.getInt("serverSocket.port"));
			result = true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * shutdown the thread pool
	 */
	public boolean shutdown() {
		boolean result = false;
		try {
			end = true;
			/** stop the internal loop for the instances from Process Class running **/
			stopProcess();

			if (ObjectUtils.notEqual(executor, null)) {
				executor.shutdown();
				executor.awaitTermination(properties.getInt("serverSocket.timeOutSleep"), TimeUnit.MILLISECONDS);
				executor.shutdownNow();
			}
			closeSocket();
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
