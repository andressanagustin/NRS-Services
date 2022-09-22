package com.allc.arms.server.processes.syncRMA;

import java.io.File;
import java.util.Calendar;

import org.apache.log4j.Logger;
import com.allc.arms.server.processes.file.sender.FileSenderProcess;
import com.allc.comm.socket.ConnSocketClientProxy;
import com.ibm.OS4690.File4690;

public class RMASenderProcess extends FileSenderProcess {

//	private Session session = null;

	protected void inicializar() {
		log = Logger.getLogger(RMASenderProcess.class);
		log.info("INICIA PROCESO RMA_SYNC V1...");
		descriptorProceso = "RMA_SEND_P";
		isEnd = false;
		try {
			log.info("Iniciando RMASenderProcess...");

//			inFolder = new File("/usr/local/NRS/WWW/EYES/RMA/TEST"); // Ruta del server
			inFolder = new File("/usr/local/NRS/WWW/ALLC_DAT/IN/update/CmdFiles"); // Ruta del server
			outFolder = ("F:/ALLC_DAT/IN/update/CmdFiles"); // Ruta donde se guarda en el Controlador CC
			log.info("File4690 separator: " + File4690.separator);
			log.info("inFolder.getAbsolutePath(): " +inFolder.getAbsolutePath());
			 
			inFolder.mkdirs();
			sleepTime = 3000;
			ip = "172.20.105.10"; //"172.20.103.93";
			port = "2040"; //serverSocket.port
			
//			UtilityFile.createWriteDataFile(getEyesFileName(), descriptorProceso+"|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode + "|STR|"+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Iniciando envío de archivos.\n", true);
//			StoreDAO storeDAO = new StoreDAO();
//			iniciarSesion("Saadmin");			
//			inFolder = new File(properties.getObject("TSL.defaultLocalRepositoryToStore") + File.separator +properties.getObject("tslSender.in.folder.path") + File.separator);
//			outFolder = new File(properties.getObject("TSL.defaultLocalRepositoryToStore"));
//			storeCode = "000";
//					while(storeCode.length() < 3)
//						storeCode = "0" + storeCode;
//					Store store = storeDAO.getStoreByCode(session, 0);
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	protected boolean connectClient() {
		if (socketClient == null) {
			log.info("ip: " + ip);
			log.info("port: " + port);
			socketClient = new ConnSocketClientProxy();
			socketClient.setIpServer(ip);
			socketClient.setIpServer2(ip);
			socketClient.setPortServer(Integer.valueOf(port));
			socketClient.setPortServer2(Integer.valueOf(port));
			socketClient.setRetries(properties.getInt("clientSocket.retries"));
			socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}

//	protected void iniciarSesion(String name) {
//		while (session == null) {
//			try {
//				session = HibernateSessionFactoryContainer.getSessionFactory(name).openSession();
//			} catch (Exception e) {
//				log.error(e.getMessage(), e);
//			}
//			if (session == null)
//				try {
//					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
//					Thread.sleep(3000);
//				} catch (InterruptedException e) {
//					log.error(e.getMessage(), e);
//				}
//		}
//	}

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo TSLSenderProcess...");
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
