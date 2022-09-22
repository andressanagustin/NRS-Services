package com.allc.arms.agent.processes.syncRMA;
 
import java.util.Calendar;

import org.apache.log4j.Logger;

import com.allc.arms.agent.processes.file.sender.FileSenderProcess;
import com.allc.comm.socket.ConnSocketClient;
import com.ibm.OS4690.File4690;

public class AEFSenderProcessSTS extends FileSenderProcess {

	protected void inicializar() {
		log = Logger.getLogger(AEFSenderProcessSTS.class);
		log.info("INICIA PROCESO AEF_SYNC_STS V1...");
		descriptorProceso = "AEF_SEND_P";
		isEnd = false;
		try {
			log.info("Iniciando RMASenderProcess...");

			inFolder =  new File4690("F:/ALLC/AEF/STS");      // Ruta de origen en el Controlador CC
			outFolder = ("/usr/local/NRS/ALCEYES/MON_POS/210/020/STS"); // Ruta de destino en el server
			log.info("File4690 separator: " + File4690.separator);
			log.info("Infolder.getAbsolutePath: "+inFolder.getAbsolutePath());
			 
			inFolder.mkdirs();
			sleepTime = 3000;
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
