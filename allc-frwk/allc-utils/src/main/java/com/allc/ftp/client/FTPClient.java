/**
 *
 */
package com.allc.ftp.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import com.allc.files.helper.FilesHelper;
import java.io.FileInputStream;

/**
 * @author GUSTAVOK
 *
 */
public class FTPClient {

    static Logger logger = Logger.getLogger(FTPClient.class);
    private org.apache.commons.net.ftp.FTPClient client = new org.apache.commons.net.ftp.FTPClient();
    String ip;
    String user;
    String pass;

    public FTPClient() {
        this.ip = null;
        this.user = null;
        this.pass = null;
    }

    public FTPClient(String pIp, String pUser, String pPass) {
        this.ip = pIp;
        this.user = pUser;
        this.pass = pPass;
    }

    public boolean connectToServer(String pIp, String pUser, String pPass) {
        if (this.ip == null) {
            this.ip = pIp;
        }
        if (this.user == null) {
            this.user = pUser;
        }
        if (this.pass == null) {
            this.pass = pPass;
        }

        return this.connectToServer();
    }

    public boolean connectToServer() {
        try {
            client.connect(this.ip);
            client.login(this.user, this.pass);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    public boolean disconnectToServer() {
        try {
            client.disconnect();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    //agregar check conection 
    public boolean checkConnection() {
        return !client.isConnected() ? connectToServer() : true;
    }

    public boolean removeFile(String originDir, String filename) {
        boolean exit = true;
        try {
            client.deleteFile(originDir + filename);
            logger.debug("El archivo " + filename + " se ha borrado de la carpeta origen (en el Server FTP).");
        } catch (Exception e) {
            logger.error("Error eliminando archivos por FTP.", e);
            exit = false;
        }
        return exit;
    }

    public boolean retrieveFile(String originDir, String destDir, String filename) {
        boolean exit = true;
        FileOutputStream fos = null;
        logger.debug("Buscando archivo:" + filename);
        try {
            File destDirectory = new File(FilesHelper.replaceFileSeparator(destDir));
            destDirectory.mkdirs();
            fos = new FileOutputStream(FilesHelper.replaceFileSeparator(destDir) + filename);

            client.retrieveFile(originDir + filename, fos);
            File file = new File(originDir + filename);
            logger.debug("Archivo transferido: " + filename + "	(" + file.length() + " bytes).");

            // if (filesSizes.get(filename).compareTo(file.length()) == 0) {
            // logger.info("La transferencia del archivo " + filename
            // + " ha sido exitosa.");
            // client.deleteFile(config.getOriginFolder() + filename);
            // logger.debug("El archivo " + filename
            // + " se ha borrado de la carpeta origen.");
            // } else {
            // logger.error("La transferencia del archivo " + filename
            // + " ha fallado.");
            // }
            // }
        } catch (Exception e) {
            logger.error("Error descargando archivos por FTP.", e);
            exit = false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                logger.error("Error descargando archivos por FTP.", e);
            }
        }
        return exit;
    }

    public boolean downloadFtpFile(String originDir, String destDir, String fileName) {
        return downloadFtpFile(originDir, destDir, fileName, fileName);
    }

    public void upload(String directory, File file) {
        try {
            client.makeDirectory(directory);
            client.changeWorkingDirectory(directory); // Cambie el directorio de trabajo al directorio donde se carga el archivo (asegúrese de que este directorio exista en el servidor; de lo contrario, primero debe crear el directorio)
            client.storeFile(file.getName(), new FileInputStream(file)); //Subir
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public boolean downloadFtpFile(String originDir, String destDir, String fileNameRemoto, String fileNameLocal) {
        boolean exit = true;
        FileOutputStream fos = null;
        logger.debug("Buscando archivo:" + fileNameRemoto);
        try {
            client.enterLocalPassiveMode();

            FTPFile[] files = client.listFiles(originDir + fileNameRemoto);
            if (files == null || files.length == 0) {
                // si no hay archivos que descargar
                // return false;
            } else {
                for (FTPFile ftpFile : files) {
                    if (fileNameRemoto.equals(".") || fileNameRemoto.equals("..")) {
                        continue;
                    }

                    if (!ftpFile.isDirectory()) {
                        File destDirectory = new File(FilesHelper.replaceFileSeparator(destDir));
                        //destDirectory.mkdirs();
                        if (!destDirectory.exists()) {
                            destDirectory.mkdir();
                        }
                        fos = new FileOutputStream(FilesHelper.replaceFileSeparator(destDir) + fileNameLocal);
                        client.retrieveFile(originDir + fileNameRemoto, fos);
                        File file = new File(originDir + fileNameRemoto);
                        logger.debug("Archivo transferido: " + fileNameRemoto + "	(" + file.length() + " bytes).");

                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error descargando archivos por FTP.", e);
            exit = false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                logger.error("Error descargando archivos por FTP.", e);
            }
        }
        return exit;
    }
}
