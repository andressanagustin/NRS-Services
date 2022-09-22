package com.allc.arms.agent.processes.syncSender;
//com.allc.arms.agent.processes.syncSender.AEFSyncSenderProcessPPT
import java.util.Calendar;

import org.apache.log4j.Logger;

import com.allc.arms.agent.processes.syncRMA.AEFSenderProcessPPT;
import com.allc.comm.socket.ConnSocketClient;
import com.ibm.OS4690.File4690;

public class AEFSyncSenderProcessPPT extends FileSyncSenderProcess {
	
	protected void inicializar() {
		log = Logger.getLogger(AEFSenderProcessPPT.class);
		log.info("INICIA PROCESO TEST AEF_SYNC_PPT V0.001...");
		descriptorProceso = "FILESYNC_SEND_P";
		isEnd = false;
		try {
			log.info("Iniciando RMASenderProcess...");

			inFolder =  new File4690("F:/ALLC/AEF");      // Ruta de origen en el Controlador CC
			searchFolder = ("/usr/local/NRS/AEF"); // Ruta de destino en el server
			log.info("File4690 separator: " + File4690.separator);
			log.info("Infolder.getAbsolutePath: "+inFolder.getAbsolutePath());
			extensions = new String[]{"PRT","PPT","STS"}; 
			inFolder.mkdirs();
			sleepTime = 10000; //solo para pruebas
			ip = "10.122.5.150";
			port = 8000; 
 

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	protected boolean connectClient() {
		if (socketClient == null) {
			log.info("ip: " + ip);
			log.info("port: " + port);
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(ip);
			socketClient.setPortServer(port);
			socketClient.setRetries(properties.getInt("clientSocket.retries"));
			socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	} 

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo RMASenderProcess 1...");
		while (!finished)
			try {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= timeToWait) {
					return false;
				}
				Thread.sleep(500);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		log.info("Finalizó el Proceso de Envío de archivos de Transacciones.");
		return true;
	}



}
