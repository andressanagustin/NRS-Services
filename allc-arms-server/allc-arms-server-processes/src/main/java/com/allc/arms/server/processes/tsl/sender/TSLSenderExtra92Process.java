package com.allc.arms.server.processes.tsl.sender;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;

import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;

public class TSLSenderExtra92Process extends TSLSenderProcess {

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
			inFolder = new File(properties.getObject("TSL.defaultLocalRepositoryToStore") + File.separator +properties.getObject("tslSender.in.folder.path") + File.separator + "extra92" + File.separator);
			outFolder = properties.getObject("TSL.defaultLocalRepositoryToStore") + File.separator + "extra92" ;
			inFolder.mkdirs();
			sleepTime = properties.getInt("tslSender.sleeptime");
			Store store = storeDAO.getStoreByCode(session, 0);
			ip = store.getIp();
			port = properties.getObject("serverSocket.port");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
