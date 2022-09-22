/**
 * 
 */
package com.allc.persistence.process;

import java.io.File;
import java.util.ArrayList;
import org.apache.log4j.Logger;

import com.allc.converters.ConverterFromXML;
import com.allc.entities.Transaction;
import com.allc.persistence.dao.TransactionDAO;

/**
 * @author GUSTAVOK
 * 
 */
public class Process extends Thread {

	private static Logger log = Logger.getLogger(Process.class);;
	private TlogReaderProcess tlogReaderProcess = null;
	private boolean isEnd = false;
	
        public void run() {
		try {
			log.info("Configurando directorios de tlogs...");
			tlogReaderProcess = new TlogReaderProcess();
			log.info("Configuraci�n de directorios de tlogs finalizada...");
			log.info("Configurando mapeo de xml...");
			ConverterFromXML converterFromXML = new ConverterFromXML();
			log.info("Configuraci�n de mapeo de xml finalizada...");
			TransactionDAO transactionDAO = new TransactionDAO();
//			UpdateCRMData updateCRMData = new UpdateCRMData();
			ArrayList<File> Tlogs = new ArrayList<File>();
                        ArrayList<String> listTlog = new ArrayList<String>();
			boolean finish = false;
                        while (!finish) {
				try {
					log.info("Buscando archivos de tlog...");
                                        Tlogs = tlogReaderProcess.getTransactionalFiles();
                                        if (Tlogs != null){
                                                for (File currentTlog: Tlogs) {
                                                        log.info("Tlog a distribuir: "+currentTlog.getName());
                                                        Transaction trx = null;
                                                        int retries = 3;
                                                        while(retries > 0){
                                                                try {
                                                                        retries--;
                                                                        trx = converterFromXML.getTransaction(currentTlog);
                                                                        log.debug("Tlog leido correctamente.");
                                                                        retries = 0;
                                                                } catch (Exception ex){
                                                                        if(retries == 0){
                                                                                log.error("Error al parsear el tlog. Se mover� al directorio de tlogs err�neos.", ex);
                                                                                while(!tlogReaderProcess.moveToErrorTlogFolders(currentTlog));
                                                                        } else {
                                                                                log.error("Error al parsear el tlog. Se reintentara...", ex);
                                                                                Thread.sleep(2000);
                                                                        }
                                                                }
                                                        }
                                                        if(trx != null) {
                                                                boolean trxRegistrada = false;
                                                                try {
                                                                        log.debug("Guardando transacci�n en BD...");
                                                                        trxRegistrada = transactionDAO.saveTransaction(trx);							
                //							log.debug("Guardando datos de cliente en BD...");
                //							updateCRMData.update(trx);
                //							log.debug("Datos de cliente guardados correctamente...");
                                                                        if(trxRegistrada){
                                                                                log.debug("Moviendo Tlog a carpeta de procesados...");
                                                                                String nomArch = tlogReaderProcess.getRenamedTlog(currentTlog.getName());
                                                                                while(!tlogReaderProcess.moveToProcessedTlogFolders(currentTlog, nomArch));
                                                                                listTlog.add(nomArch);
                                                                                log.debug("Tlog movido a carpeta de procesados.");
                                                                                log.info("Tlog distribuido OK!");
                                                                        } else {
                                                                                log.debug("Moviendo Tlog a carpeta de duplicados...");
                                                                                while(!tlogReaderProcess.moveToDuplicatedTlogFolders(currentTlog));
                                                                                log.debug("Tlog movido a carpeta de duplicados.");
                                                                        }
                                                                } catch (Throwable t) {
                                                                        log.error("Error al guardar la transacci�n en BD. Se mover� al directorio de tlogs err�neos.", t);
                                                                        while(!tlogReaderProcess.moveToErrorTlogFolders(currentTlog));
                                                                }
                                                        }
                                                };
                                            transactionDAO.commitTransaction();
                                            listTlog.clear();
                                        } else  
                                            finish = true;
				} catch (Exception ex){
					log.error("Error al Procesar el tlog.", ex);
                                        transactionDAO.cierraOperacion();
                                        tlogReaderProcess.moveToExtra(listTlog);
                                        transactionDAO.iniciaOperacion();
				} 
			}
			log.info("Finalizó aplicación!");
			isEnd = true;
		} catch (Exception e) {
			log.error("Error al ejecutar la aplicaci�n.", e);
		}
	}
        
	public void stopProcess() {
		if(tlogReaderProcess!=null){
			tlogReaderProcess.setEnd(true);
		}
	}

	public boolean isEnd() {
		return isEnd;
	}

}
