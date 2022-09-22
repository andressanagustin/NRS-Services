/**
 * 
 */
package com.allc.core.receiver;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import com.allc.comm.pipe.ConnPipeServer;
import com.allc.core.socket.OperationsServer;
import com.allc.properties.PropFile;
import com.ibm.OS4690.POSPipeInputStream;

/**
 * @author gustavo
 *
 */
public class ReceiverPipe extends Thread {
	private static Logger logger = Logger.getLogger(ReceiverPipe.class);
	private ExecutorService executor;
	private boolean end = false;
	private PropFile properties = null;
	private POSPipeInputStream pipeInputStream;
	public static boolean waitAvailable = false;
	private Map staticProperties = new HashMap();

	/**
	 * 
	 */
	public ReceiverPipe(PropFile properties, ExecutorService executor, Map staticProperties) {
		this.properties = properties;
		this.executor = executor;
		this.staticProperties = staticProperties;
	}

	public void run() {
		Runnable worker;
		int quantityBytesLength = properties.getInt("serverPipe.quantityBytesLength");
		int timeOutConnection = properties.getInt("serverPipe.timeOutConnection");
		int timeOutSleep = properties.getInt("serverPipe.timeOutSleep");
		openPipeLetter();
		try {
			while (!end) {
				if (pipeInputStream.available() > 0 && !waitAvailable) {
					if(pipeInputStream.available() < quantityBytesLength)
					{
						Thread.sleep(timeOutSleep);
					}
					if(pipeInputStream.available() >= quantityBytesLength)
					{
						waitAvailable = true;
						worker = new OperationsServer(
								new ConnPipeServer(pipeInputStream, quantityBytesLength, timeOutConnection,
								timeOutSleep), properties);
						((OperationsServer) worker).staticProperties = staticProperties;
						executor.execute(worker);						
					}
					else
					{
						//si luego de un timeout aun tenemos menos de la cantidad minima, reiniciamos el pipe para limpiar la basura
						logger.info("Reiniciamos Pipe para limpiar basura.");
						closePipeLetter();
						openPipeLetter();
					}
				} else {
					Thread.sleep(timeOutSleep);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * open a letter pipe
	 */
	public boolean openPipeLetter() {
		boolean result = false;
		try {
			int length = properties.getInt("serverPipe.length");
			String name = properties.getObject("serverPipe.name");
			pipeInputStream = new POSPipeInputStream(length, name.charAt(0));
			logger.info("Server Pipe escuchando en: " + properties.getObject("serverPipe.name"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Close a letter pipe
	 * 
	 * @return
	 */
	public boolean closePipeLetter() {
		boolean result = false;
		try {
			if (pipeInputStream != null)
				pipeInputStream.close();
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
				executor.awaitTermination(properties.getInt("serverPipe.timeOutSleep"), TimeUnit.MILLISECONDS);
				executor.shutdownNow();
			}
			closePipeLetter();
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
