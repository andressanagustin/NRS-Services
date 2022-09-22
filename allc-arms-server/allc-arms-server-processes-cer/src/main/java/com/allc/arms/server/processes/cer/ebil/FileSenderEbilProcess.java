package com.allc.arms.server.processes.cer.ebil;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.params.ParamValue;
import com.allc.arms.server.params.ParamsDAO;
import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.server.processes.file.sender.FileSenderProcess;
import com.allc.arms.server.processes.tsl.sender.TSLSenderProcess;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;



/**
 * @author maxi_
 * 
 * Proceso encargado de enviar archivos de factura electrónica desde ArmsServerLocal a ArmsServerCentral
 *
 */
public class FileSenderEbilProcess extends FileSenderProcess{
	
	private Session session = null;
	
	
	protected void inicializar() {
		log = Logger.getLogger(FileSenderEbilProcess.class);
		descriptorProceso = "EBIL_SEND_P";
		isEnd = false;
		try {
			log.info("Iniciando FileSenderEbilProcess...");
			iniciarSesion("Saadmin");
			ParamsDAO paramsDAO = new ParamsDAO();
			StoreDAO storeDAO = new StoreDAO();
			ParamValue paravalue = null;
			storeCode = properties.getObject("eyes.store.code");
			while(storeCode.length() < 3)
				storeCode = "0" + storeCode;
			UtilityFile.createWriteDataFile(getEyesFileName(), descriptorProceso+"|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode + "|STR|"+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Iniciando envío de archivos.\n", true);
			inFolder = new File(properties.getObject("searchEbil.out.folder.path") + File.separator + properties.getObject("searchEbil.sync.folder.path"));
			paravalue = paramsDAO.getParamByClave(session, Integer.valueOf(storeCode).toString(),
					ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "SYNC_OUT");
			outFolder = (properties.getObject("SUITE_ROOT") + paravalue.getValor() + storeCode
					+ File.separator + properties.getObject("searchEbil.in.folder"));
			inFolder.mkdirs();
			sleepTime = properties.getInt("ebilSender.sleeptime");
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

}
