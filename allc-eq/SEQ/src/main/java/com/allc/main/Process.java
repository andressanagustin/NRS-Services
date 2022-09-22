/**
 * 
 */
package com.allc.main;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import ServiceEQtoNagios.RecibeTramas;

import com.allc.main.properties.PropertiesSEQ;
import com.allc.main.task.Task;

/**
 * @author GUSTAVOK
 * 
 */
public class Process extends Thread {

	private static Logger log = Logger.getLogger(Process.class);;
	private static ServerSocket socket = null;	
	private static ExecutorService EXECUTOR;
	private static RecibeTramas recibeTramas;
	private boolean isEnd = false;
	
	public void run() {
		try{
			/***********************************************
			 * Open the socket channel
			 **********************************************/
			openSocket();
			/**to send the data to nagios**/
			recibeTramas =  new RecibeTramas();
			/***********************************************
			 * Fire the processes into a ThreadPool
			 **********************************************/	
			EXECUTOR = Executors.newFixedThreadPool(PropertiesSEQ.Comunication.CANT_THREAD);
			log.info("Limit of concurrent processes: " + PropertiesSEQ.Comunication.CANT_THREAD);

			while (!isEnd) {
				Runnable worker = new Task(socket.accept(), recibeTramas, PropertiesSEQ.Parametros.REGISTER_IN_BD);
				EXECUTOR.execute(worker);
			}
			log.info("Process finalizado.");
		} catch(Exception e){
			log.error(e);
		}

	}


	/**
	 * Open a Socket
	 */
	private static void openSocket() {
		try {
			socket = new ServerSocket(PropertiesSEQ.Comunication.LOCAL_SERVER_PORT, 100);
			//socket.setSoTimeout(TIMEOUTSOCK);

			log.info("==================================");
			log.info("Listen on port: " + PropertiesSEQ.Comunication.LOCAL_SERVER_PORT);
			log.info("==================================");
		}catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void stopProcess() {
		try{
			isEnd = true;
			EXECUTOR.shutdown();
			while (!EXECUTOR.isTerminated()) {

		    }
			log.info("Finished all threads");
			socket.close();
			log.info("Socket cerrado.");
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}
	}

	public boolean isEnd() {
		return isEnd;
	}

}
