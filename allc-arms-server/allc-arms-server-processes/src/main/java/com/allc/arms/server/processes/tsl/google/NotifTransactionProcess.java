/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.processes.tsl.google;

import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.mail.EmailSender;
import com.allc.core.process.AbstractProcess;
import com.allc.properties.PropFile;
import java.io.File;
import java.util.Calendar;
import org.apache.log4j.Logger;

/**
 *
 * @author Tyrone Lopez
 */
public class NotifTransactionProcess extends AbstractProcess {

    private static final Logger LOGGER = Logger.getLogger(NotifTransactionProcess.class);

    protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);

    protected boolean finished = false;

    public boolean isEnd = false;

    @Override
    public void run() {
        while (!isEnd) {
            try {
                LOGGER.info(" ---------------------------- INICIO PROCESO NOTIFICACIONES GOOGLE -------------------------------");

                String filesPath = properties.getObject("google.storage.file.path");
                File directory = new File(filesPath);
                if (directory.exists()) {
                    int size = directory.listFiles().length;
                    if (size > 100) {
                        String codeGroup = properties.getObject("eyes.store.code.group") == null ? "Central" : properties.getObject("eyes.store.code.group");
                        EmailSender.sendMail(
                                properties.getObject("smtp.user"),
                                properties.getObject("smtp.password"),
                                properties.getObject("smtp.server"),
                                properties.getObject("smtp.user"),
                                properties.getObject("administrator.alert.mail.to"),
                                "Alerta Transacción google storage.",
                                "El servidor: " + codeGroup + ", Tiene " + size + " archivos encolados"
                        );
                    }
                }

                LOGGER.info(" ---------------------------- FIN PROCESO NOTIFICACIONES GOOGLE -------------------------------");
                LOGGER.info("Duermo NotifTransactionProcess" + 30 * 60000);
                Thread.sleep(30 * 60000);
            } catch (InterruptedException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        finished = true;
    }

    @Override
    public boolean shutdown(long timeToWait) {
        isEnd = true;
        long startTime = Calendar.getInstance().getTimeInMillis();
        LOGGER.info("Deteniendo NotifTransactionProcess...");
        while (!finished) {
            try {
                long dif = Calendar.getInstance().getTimeInMillis() - startTime;
                if (dif >= timeToWait) {
                    return false;
                }
                Thread.sleep(600);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.info("Finalizó de Proceso notificacion de storage.");
        return true;
    }
}
