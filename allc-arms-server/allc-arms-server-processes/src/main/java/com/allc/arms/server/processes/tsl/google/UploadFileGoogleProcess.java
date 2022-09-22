/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.processes.tsl.google;

import com.allc.arms.utils.ArmsServerConstants;
import com.allc.core.process.AbstractProcess;
import com.allc.properties.PropFile;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.log4j.Logger;

/**
 *
 * @author Tyrone Lopez
 */
public class UploadFileGoogleProcess extends AbstractProcess {

    private static final Logger LOGGER = Logger.getLogger(UploadFileGoogleProcess.class);

    protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);

    private String filesPath;

    private String credentialPath;

    private String projectId;

    private String bucketName;

    protected boolean finished = false;

    public boolean isEnd = false;

    private void init() {
        filesPath = properties.getObject("google.storage.file.path");
        credentialPath = properties.getObject("google.storage.credentials");
        projectId = properties.getObject("google.storage.projectId");
        bucketName = properties.getObject("google.storage.bucketName");
    }

    @Override
    public void run() {
        LOGGER.info("Inicia proceso de UploadFileGoogleProcess");

        while (!isEnd) {
            try {
                init();
                //tiendas = new LinkedList<>();
                SimpleDateFormat sdfMount = new SimpleDateFormat("yyyy/MM/dd");
                SimpleDateFormat sdfFolder = new SimpleDateFormat("yyyyMMdd");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

                File directory = new File(filesPath);
                String bucketName1 = bucketName.split("/")[0];

                String bucketPath = bucketName.replace(bucketName1, "").replaceFirst("/", "");
                bucketPath = bucketPath.isEmpty() ? bucketPath : bucketPath.concat("/");
                if (directory.exists() && directory.isDirectory()) {
                    Storage storage = StorageOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(new FileInputStream(credentialPath))).setProjectId(projectId).build().getService();
                    String[] jsons = directory.list();
                    for (String jsonName : jsons) {
                        File json = new File(filesPath + File.separator + jsonName);
                        if (json.isFile() && json.exists()) {
                            try {
                                LOGGER.info("File to send to google si existe " + json.getName());
                                Date date = new Date();
                                String mouth = sdfMount.format(date) + "/" + Integer.parseInt(json.getName().split("-")[0]);
                                BlobId blobId = BlobId.of(bucketName1, bucketPath + mouth + "/" + json.getName());
                                BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
                                storage.create(blobInfo, Files.readAllBytes(Paths.get(json.getAbsolutePath())));
                                if (json.renameTo(new File(filesPath + File.separator + "procesados" + File.separator + json.getName()))) {
                                    LOGGER.info("File " + json.getName() + " Moved");
                                }
                            } catch (Exception e) {
                                LOGGER.error(e.getMessage(), e);
                            }
                        }
                    }
                    LOGGER.info("Terminado directorio");
                } else {
                    LOGGER.info("No es directorio -- ");
                }
            } catch (FileNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
            try {
                LOGGER.info("Durmiendo proceso google " + properties.getInt("google.storage.timer"));
                Thread.sleep(properties.getInt("google.storage.timer"));
            } catch (InterruptedException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        LOGGER.info("Finaliza proceso google +++");
        finished = true;
    }

    @Override
    public boolean shutdown(long timeToWait) {
        isEnd = true;
        long startTime = Calendar.getInstance().getTimeInMillis();
        LOGGER.info("Deteniendo UploadFileGoogleProcess...");
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
        LOGGER.info("Finalizó el Proceso de storage.");
        return true;
    }

}
