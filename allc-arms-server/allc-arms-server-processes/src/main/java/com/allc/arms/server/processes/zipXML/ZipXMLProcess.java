package com.allc.arms.server.processes.zipXML;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.core.process.AbstractProcess;
import com.allc.properties.PropFile;
import java.io.IOException;
import java.util.Arrays;

public class ZipXMLProcess extends AbstractProcess {

	protected Logger log;
	protected int sleepTime;
	public boolean isEnd = false;
	private Session session = null;
	protected boolean finished = false;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	String[] directoriesToZip;
        int waitTimeNextExecution;
        String processStartTime;
        String processEndTime;
        int waitTimeNextRetry;
	String backupDir;
        
	public void run() {
		initialize();
		try {
			perform();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finished = true;
	}

	protected void initialize() {
		log = Logger.getLogger(ZipXMLProcess.class);
		isEnd = false;
		try {
			log.info("Iniciando ZipXMLProcess...");
			directoriesToZip = properties.getObject("XML.directories.to.zip").split(",");
                        waitTimeNextExecution = properties.getInt("XML.wait.time.next.execution");
                        waitTimeNextRetry = properties.getInt("XML.wait.time.next.retry");
			processStartTime = properties.getObject("XML.process.start.time");
			processEndTime = properties.getObject("XML.process.end.time");
			backupDir = properties.getObject("XML.backup.directory");
                        
			log.info("Directorio de las Carpetas a Zipear: " + Arrays.toString(directoriesToZip));
			log.info("Directorio de Respaldo: "+ backupDir);

                        logIn("Saadmin");

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	protected void perform() throws Exception {
            while (!isEnd) {
                try {
                    
                    SimpleDateFormat HourFormat = new SimpleDateFormat("HH:mm");
                    Date startTime = HourFormat.parse(processStartTime);
                    Date endTime = HourFormat.parse(processEndTime);
                    Date currentTime = HourFormat.parse(HourFormat.format(new Date()));

                    if (currentTime.after(startTime) && currentTime.before(endTime) ) {

                        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
                        Date date = new Date(System.currentTimeMillis());
                        String currentDate = formatter.format(date);
                        log.info("Inicio del Zipeo - fecha: " + currentDate);
                        
                        for (String directorytoZip : directoriesToZip) {
                            
                            log.info("Directorio a Zipear: "+ directorytoZip);
                            File file = new File(directorytoZip);
                            String folderName = file.getParentFile().getName() + "_" + file.getName();
                           
                            if (!file.exists()){
                                log.info("No Existe el Directorio a Zipear");
                                continue;
                            }
                            
                            int numberFiles = file.list().length;
                            
                            if (numberFiles <= 0){
                                log.info("No hay Archivos para Zipear en el Directorio");
                                insertaRegistroZipeadoXML("No hay Archivos en el Directorio" , folderName, date, "omitido", 
                                                          "Directorio Omitido: "+ directorytoZip);
                                continue;
                            }
                            
                            String zipName = folderName.concat("_").concat(currentDate).concat(".zip");
                            String newBackupDir = backupDir.replace("{currentDate}", currentDate);
                            File zipFile = new File(newBackupDir.concat(zipName)); 
                            
                            if (zipFile.exists()){
                                log.info("El Archivo .zip ya Existe: "+ zipFile);
                                continue;
                            }  
                            
                            try{
                                
                                ZipDir.zipearDir(directorytoZip, newBackupDir, zipName);
                                insertaRegistroZipeadoXML("Archivos Zipeados Correctamente", folderName, date, "procesado", 
                                                           directorytoZip + " --> " + newBackupDir + zipName);
                                ZipDir.deleteDir(file);                                    
                            } catch (IOException | SQLException e) {
                                log.error(e);
                                insertaRegistroZipeadoXML("Error al Zipear el Directorio: "+directorytoZip , folderName, 
                                                           date, "fallido", e.toString());
                            }
                        }
                        log.info("Proceso finalizado, esperar " + (waitTimeNextExecution/3600000) + " hora(s) para la siguiente ejecucion");
                        Thread.sleep(waitTimeNextExecution); 
                    } else {
                            log.info("La hora no es la correcta para realizar la ejecucion, esperar " + (waitTimeNextRetry/3600000) + " hora(s)");
                            Thread.sleep(waitTimeNextRetry); 
                    }
                    
                } catch (InterruptedException | SQLException e) {
                        log.error(e);
                        Date date = new Date(System.currentTimeMillis());
                        insertaRegistroZipeadoXML("Error en el Metodo perform(): ", null, date, "fallido", e.toString());
                }
            }
	}

	/*private int validMoment() {
		int moment = 1;
		try {
			Date lastRecord = getLastRecord();
                        
                        if (lastRecord == null)
                                return 0;
                        
			Date currentDate = new Date(System.currentTimeMillis());
			long diff = currentDate.getTime() - lastRecord.getTime();
			TimeUnit time = TimeUnit.DAYS;
			long difference = time.convert(diff, TimeUnit.MILLISECONDS);
                        
			if (difference >= intervalDay) 
				moment = 0;
			else 
				moment = (int) (intervalDay - difference);
			
		} catch (Exception e) {
			log.info("Error en validMoment(): " + e);
			return 0;
		}
		return moment;
	}*/

	private Date getLastRecord() {

		try {
			SQLQuery query = session.createSQLQuery("SELECT fecha FROM saadmin.ctr_cln_xml order by fecha desc limit 1");
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return (Date) rows.get(0);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo ZipXMLProcess...");
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
		log.info("Finalizó el Proceso de compresion de trx XML.");
		return true;
	}

	protected void logIn(String name) {
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

	public boolean insertaRegistroZipeadoXML(String detalle, String folder, Date fecha, String estado, String observacion)
			throws SQLException {

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createSQLQuery(
					"INSERT INTO saadmin.ctr_cln_xml (detalle, dir_zip, fecha_zip, estado, observacion) "
                                      + "values (:valor1, :valor2, :valor3, :valor4, :valor5)");
			query.setParameter("valor1", detalle);			
                        query.setParameter("valor2", folder);
			query.setParameter("valor3", fecha);
			query.setParameter("valor4", estado);
			query.setParameter("valor5", observacion);
			query.executeUpdate();
                        
			tx.commit();
                        log.info("Registro exitoso en Base de Datos...");
                        
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}

}
