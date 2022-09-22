/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.processes.interfaces.webapi;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.processes.utils.AbstractProcessPrincipal;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 *
 * @author Tyrone Lopez
 */
public class InicioWebApiProceso extends AbstractProcessPrincipal {

    private static Logger log = Logger.getLogger(InicioWebApiProceso.class.getName());
    private PropFile prop = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
    private Session sessionArtsEc = null;

    private boolean finished = false;
    private boolean isEnd = false;

    private void iniciarArtsEcSesion() {
        while (sessionArtsEc == null && !isEnd) {
            try {
                sessionArtsEc = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            if (sessionArtsEc == null) {
                try {
                    log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void run() {
        log.info(" ---------------------------- PROCESO de carga Archivo Maestro de Items -------------------------------");
        while (!isEnd) {
            if (isPrincipal()) {
                iniciarArtsEcSesion();
                try {
                    UtilityFile.createWriteDataFile(getEyesFileName(), "WEB_API|" + prop.getHostName() + "|3|" + prop.getHostAddress() + "|0|STR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Iniciando proceso de revision WEB-API / ECOMMERCE.\n", true);
                    SimpleDateFormat HourFormat = new SimpleDateFormat("HH:mm");
                    Date StartTime = HourFormat.parse("07:00");
                    Date CurrentTime = HourFormat.parse(HourFormat.format(new Date()));

                    log.info(" ---------------------------- INICIO PROCESO WEBAPI -------------------------------");
                    if (CurrentTime.after(StartTime)) {
                        ProcesoWebApi webApi = new ProcesoWebApi(prop, sessionArtsEc);
                        webApi.procesarDatos();

                    }
                    log.info(" ---------------------------- FIN PROCESO WEBAPI -------------------------------");
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                sessionArtsEc.close();
                sessionArtsEc = null;
            }
            try {
                log.info("Duermo: " + this.prop.getLong("interfaceMaestroItem.timesleep.webapi"));
                Thread.sleep(this.prop.getLong("interfaceMaestroItem.timesleep.webapi"));
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
        while (!finished) {
            try {
                long dif = Calendar.getInstance().getTimeInMillis() - startTime;
                if (dif >= timeToWait) {
                    return false;
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        log.info("Finalizó el Proceso de Interfaces de carga de maestro de items.");
        return true;
    }

    private String getEyesFileName() {
        return prop.getObject("eyes.ups.file.name") + "_" + ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
    }

}
