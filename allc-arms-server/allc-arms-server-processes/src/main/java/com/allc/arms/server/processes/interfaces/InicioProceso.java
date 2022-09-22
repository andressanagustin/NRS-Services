/*
 *  ***************************************************************
 * NUO - 2020
 * Creado por:      Jes�s Guerra Cede�o
 * Motivo:          Procesos a ejecutarse en el demonio
 * Fecha Creacion:  2020-06-20
 *  ***************************************************************
 */

package com.allc.arms.server.processes.interfaces;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.arms.server.processes.utils.AbstractProcessPrincipal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 *
 * @author Andres Sanagustin
 */
public class InicioProceso extends AbstractProcessPrincipal{

	protected static Logger log = Logger.getLogger(InicioProceso.class.getName());
	protected PropFile prop = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	private Session sessionArtsEc = null;
	protected boolean finished = false;
	public boolean isEnd = false;
	
	
	private void iniciarArtsEcSesion() {
		while (sessionArtsEc == null && !isEnd) {
			try {
				sessionArtsEc = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionArtsEc == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

        @Override
	public void run() {
		// agregar registro para seguimiento de eyes.
		// UtilityFile.createWriteDataFile(getEyesFileName(), "OPER_UPD_P|"+prop.getHostName()+"|3|"+prop.getHostAddress()+"|"+store+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Iniciando procesamiento del Operador: "+operador.getIdentityDocument()+".\n", true);
		log.info(" ---------------------------- PROCESO de carga Archivo Maestro de Items -------------------------------");
		
        while (!isEnd) {
            if(isPrincipal()){
                iniciarArtsEcSesion();
                int cantidad = 0;
                try {
                    UtilityFile.createWriteDataFile(getEyesFileName(), "MAESTRO_ART_P|"+prop.getHostName()+"|3|"+prop.getHostAddress()+"|TINDAS_TODAS|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Iniciando proceso de carga de Maestros de Articulos.\n", true);
                    SimpleDateFormat HourFormat = new SimpleDateFormat("HH:mm");
                    Date StartTime = HourFormat.parse("06:00");
                    Date CurrentTime = HourFormat.parse(HourFormat.format(new Date()));

                        log.info(" ---------------------------- INICIO PROCESO CODIPDV -------------------------------");
                        try {
                        if (CurrentTime.after(StartTime)) {
                                        ProcesoDat procDat = new ProcesoDat(sessionArtsEc,prop);
                                        //Busca y carga archivos .Dat para procesar en la tabla AS_ITM_LOAD_FILE
                                        //procDat.cargaArchivosDB();
                                        procDat.descargaArchivosFtp();
                                        procDat.procesaArhivos();     
                        }
                                UtilityFile.createWriteDataFile(getEyesFileName(), "MAESTRO_ART_P|" + prop.getHostName() + "|3|" + prop.getHostAddress() + "|TINDAS_TODAS|PRC|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Se proceso CODIPDV.\n", true);
                        }catch(Exception ex) {
                                UtilityFile.createWriteDataFile(getEyesFileName(), "MAESTRO_ART_P|"+prop.getHostName()+"|3|"+prop.getHostAddress()+"|TINDAS_TODAS|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo procesar CODIPDV.\n", true);
                                log.error(ex.getMessage(), ex);
                        }
                        log.info(" ---------------------------- FIN PROCESO CODIPDV -------------------------------");


                        log.info(" ---------------------------- INICIO PROCESO STOCK -------------------------------");
                        try {
                        if (CurrentTime.after(StartTime)) {
                                ProcesoStock procStock = new ProcesoStock(sessionArtsEc,prop);
                                procStock.descargaArchivosFtp();
                                //procStock.cargaArchivosDB();
                                procStock.procesaArchivos();
                        }
                        UtilityFile.createWriteDataFile(getEyesFileName(), "MAESTRO_ART_P|" + prop.getHostName() + "|3|" + prop.getHostAddress() + "|TINDAS_TODAS|PRC|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Se proceso STOCK.\n", true);
                        }catch(Exception ex) {
                                UtilityFile.createWriteDataFile(getEyesFileName(), "MAESTRO_ART_P|"+prop.getHostName()+"|3|"+prop.getHostAddress()+"|TINDAS_TODAS|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo procesar STOCK.\n", true);
                                log.error(ex.getMessage(), ex);
                        }
                        log.info(" ---------------------------- FIN PROCESO STOCK -------------------------------");


                        log.info(" ---------------------------- INICIO PROCESO JUGUETON EXCEL -------------------------------");
                        try {
                                ProcesoJugueton procJug = new ProcesoJugueton(sessionArtsEc,prop);
                                procJug.cargaArchivosDB();
                                procJug.procesaArhivos();	
                                cantidad = procJug.cantProcesada;
                                UtilityFile.createWriteDataFile(getEyesFileName(), "MAESTRO_ART_P|" + prop.getHostName() + "|3|" + prop.getHostAddress() + "|TINDAS_TODAS|PRC|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Se proceso JUGUETON EXCEL.\n", true);
                        }catch(Exception ex) {
                                UtilityFile.createWriteDataFile(getEyesFileName(), "MAESTRO_ART_P|"+prop.getHostName()+"|3|"+prop.getHostAddress()+"|TINDAS_TODAS|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo procesar JUGUETON EXCEL.\n", true);
                                log.error(ex.getMessage(), ex);
                        }
                        log.info(" ---------------------------- FIN PROCESO JUGUETON EXCEL -------------------------------");


                        log.info(" ---------------------------- INICIO PROCESO MOBLAR EXCEL -------------------------------");
                        try {
                                ProcesoMoblar procMob = new ProcesoMoblar(sessionArtsEc,prop);
                                procMob.cargaArchivosDB();
                                procMob.procesaArhivos();
                                cantidad = procMob.cantProcesada;
                                UtilityFile.createWriteDataFile(getEyesFileName(), "MAESTRO_ART_P|" + prop.getHostName() + "|3|" + prop.getHostAddress() + "|TINDAS_TODAS|PRC|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Se proceso MOBLAR EXCEL.\n", true);
                        }catch(Exception ex) {
                                UtilityFile.createWriteDataFile(getEyesFileName(), "MAESTRO_ART_P|"+prop.getHostName()+"|3|"+prop.getHostAddress()+"|TINDAS_TODAS|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo procesar MOBLAR EXCEL.\n", true);
                                log.error(ex.getMessage(), ex);
                        }
                        log.info(" ---------------------------- FIN PROCESO MOBLAR EXCEL(Item procesados: "+ cantidad +") -------------------------------");

                        log.info(" ---------------------------- FIN PROCESEO Carga Archivo Maestro de Item ---------------------------");		

                        UtilityFile.createWriteDataFile(getEyesFileName(), "MAESTRO_ART_P|"+prop.getHostName()+"|3|"+prop.getHostAddress()+"|TINDAS_TODAS|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|FinalizÃ³ el proceso de carga de Maestros de Articulos.\n", true);				
                } catch (Exception ex) {
                        //finished = true;
                        //sessionArtsEc.close();
                        UtilityFile.createWriteDataFile(getEyesFileName(), "MAESTRO_ART_P|"+prop.getHostName()+"|3|"+prop.getHostAddress()+"|TINDAS_TODAS|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al iniciar el proceso de carga de Maestros de Articulos.\n", true);
                        log.error(ex.getMessage(), ex);
                        log.info(" ---------------------------- FIN PROCESO Carga Archivo Maestro de Item ---------------------------");
                }
                sessionArtsEc.close();
                sessionArtsEc = null;
            }else
            {
                log.info("Proceso archivos no se ejecuta no es Servidor princial");
            }
            try {
                    log.info("Duermo: " + this.prop.getLong("interfaceMaestroItem.timesleep"));		
                    Thread.sleep(this.prop.getLong("interfaceMaestroItem.timesleep"));
            } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
            } 

            }
            finished = true;
	}
	
        @Override
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		//closeConnection();
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo Interfaces de carga de maestro de items ...");
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
		log.info("Finalizó el Proceso de Interfaces de carga de maestro de items.");
		return true;
	}
	
	private String getEyesFileName(){
		return prop.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
	
}
