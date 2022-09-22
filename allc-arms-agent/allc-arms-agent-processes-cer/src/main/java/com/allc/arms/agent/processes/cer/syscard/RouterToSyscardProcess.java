/**
 * 
 */
package com.allc.arms.agent.processes.cer.syscard;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.process.AbstractProcess;
import com.allc.core.socket.OperationsServer;
import com.allc.properties.PropFile;

/**
 * Proceso encargado de enrutar a Syscard los requerimientos de los POS.
 * 
 * @author gustavo
 *
 */
public class RouterToSyscardProcess extends AbstractProcess {
	private int sleepTime;
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	private static Logger log = Logger.getLogger(RouterToSyscardProcess.class);
	private boolean isEnd = false;
	protected boolean finished = false;
	private ExecutorService executor;
	private ServerSocket serverSocket = null;

	protected void inicializar() {
		try {
			int maxThreads = properties.getInt("core.threadPool.max");
			executor = Executors.newFixedThreadPool(maxThreads);
//			sleepTime = properties.getInt("updateCedPadRuc.sleeptime");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void run() {
		log.info("Iniciando RouterToSyscardProcess...");
		inicializar();
		String store = properties.getObject("eyes.store.code");
		
		Runnable worker;
		int quantityBytesLength = 2;
		int timeOutConnection = 20000;
		int timeOutSleep = 600;
		openSocket();
		try {
			while (!isEnd) {
				worker = new RouterToSyscardOperationsServer(new ConnSocketServer(serverSocket.accept(), quantityBytesLength, timeOutConnection,
						timeOutSleep), properties);
				executor.execute(worker);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		
		finished = true;
	}
	

	/**
	 * Method to open a socket
	 */
	public boolean openSocket() {
		boolean result = false;
		try {
			serverSocket = new ServerSocket(9005, 100);
			log.info("Server Socket escuchando en el puerto: " + 9005);
			result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
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
			log.info("Server detenido en el puerto: " + 9005);
			result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * shutdown the thread pool
	 */
	public boolean shutdown(long timeToWait) {
		boolean result = false;
		try {
			log.info("Deteniendo RouterToSyscardProcess...");
			isEnd = true;
			stopProcess();

			if (ObjectUtils.notEqual(executor, null)) {
				executor.shutdown();
				executor.awaitTermination(properties.getInt("serverSocket.timeOutSleep"), TimeUnit.MILLISECONDS);
				executor.shutdownNow();
			}
			closeSocket();
			result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info("Finalizó el Proceso de Enrutamiento de tramas a Syscard.");
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
			log.error(e.getMessage(), e);
		}
		return result;
	}
	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}

}
