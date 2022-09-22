/**
 * 
 */
package com.allc.arms.server.processes.tsl.sender;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.server.processes.file.sender.FileSenderProcess;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;

/**
 * Proceso encargado de enviar los tlogs desde los Locales a Central.
 * 
 * @author gustavo
 *com.allc.arms.server.processes.tsl.sender.
 */
public class TSLSenderProcess extends FileSenderProcess {
	
	protected Session session = null;
	
	protected void inicializar() {
		log = Logger.getLogger(TSLSenderProcess.class);
		descriptorProceso = "TSL_SEND_P";
		isEnd = false;
		try {
			log.info("Iniciando TSLSenderProcess...");
			storeCode = properties.getObject("eyes.store.code");
			while(storeCode.length() < 3)
				storeCode = "0" + storeCode;
			UtilityFile.createWriteDataFile(getEyesFileName(), descriptorProceso+"|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode + "|STR|"+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Iniciando envío de archivos.\n", true);
			StoreDAO storeDAO = new StoreDAO();
			//RetailStoreDAO retailStoreDAO = new RetailStoreDAO();
			iniciarSesion("Saadmin");
			//RetailStore retailStore = retailStoreDAO.getRetailStoreByCode(session, tienda); //code=731
			//String extraDir = retailStore.getDistDir() != null && !retailStore.getDistDir().isEmpty() ? retailStore.getDistDir() + File.separator : "";
			inFolder = new File(properties.getObject("TSL.defaultLocalRepositoryToStore") + File.separator +properties.getObject("tslSender.in.folder.path") + File.separator);
			outFolder = (properties.getObject("TSL.defaultLocalRepositoryToStore") );
			inFolder.mkdirs();
			sleepTime = properties.getInt("tslSender.sleeptime");
			Store store = storeDAO.getStoreByCode(session, 0);
			ip = store.getIp();
			port = properties.getObject("serverSocket.port");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	protected void iniciarSesion(String name) {
		while (session == null) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory(name).openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (session == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

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
