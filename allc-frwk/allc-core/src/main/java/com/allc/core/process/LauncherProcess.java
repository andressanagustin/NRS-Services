/**
 * 
 */
package com.allc.core.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.allc.core.receiver.ReceiverSocket;
import com.allc.properties.PropFile;

/**
 * @author gustavo
 *
 */
public class LauncherProcess extends Thread {
	private static Logger logger = Logger.getLogger(ReceiverSocket.class);
	private static boolean isEnd = false;
	private PropFile properties = null;
	private List processes = new ArrayList();

	/**
	 * 
	 */
	public LauncherProcess(String propFileName) {
		properties = PropFile.getInstance(propFileName);
	}

	public void run() {
		List processes = properties.getList("core.processes");
		Iterator itProcesses = processes.iterator();
		while (itProcesses.hasNext() && !isEnd) {
			try {
				Class clase = Class.forName((String) itProcesses.next());
				AbstractProcess process = (AbstractProcess) clase.newInstance();
				process.start();
				this.processes.add(process);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public void shutdown() {
		long timeToWait = properties.getLong("core.process.timeToWait");
		Iterator itProcesses = processes.iterator();
		while (itProcesses.hasNext()) {
			try {
				AbstractProcess process = (AbstractProcess) itProcesses.next();
				if (!process.shutdown(timeToWait)) {
					process.interrupt();
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		this.isEnd = true;
	}

}
