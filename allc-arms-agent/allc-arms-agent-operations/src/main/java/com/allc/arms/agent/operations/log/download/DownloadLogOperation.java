/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.agent.operations.log.download;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileInputStream4690;
import com.ibm.OS4690.FlexosException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

/**
 *
 * @author Tyrone Lopez
 */
public class DownloadLogOperation extends AbstractOperation {

    protected static Logger log = Logger.getLogger(DownloadLogOperation.class);

    protected ConnSocketClient socketClient = null;

    protected Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);

    protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);

    private final String outFolderServer = "/usr/local/NRS/logsagentes";

    protected String storeNumber = "";

    @Override
    public boolean shutdown(long timeToWait) {
        return false;
    }

    @Override
    public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
        try {
            ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
            storeNumber = (new Integer(controllerStatusData.getStoreNumber())).toString();
            while (storeNumber.length() < 3) {
                storeNumber = "0" + storeNumber;
            }
            log.info("Inicia proceso de recoleccion de logs");
            List frm = frame.getList();
            for (Object object : frm) {
                log.info(object);
            }
            Enumeration e = Logger.getRootLogger().getAllAppenders();
            String fileName = "";
            while (e.hasMoreElements()) {
                Appender app = (Appender) e.nextElement();
                if (app instanceof FileAppender) {
                    log.info("----- File: " + ((FileAppender) app).getFile());
                    fileName = ((FileAppender) app).getFile();
                    break;
                }
            }
            if (!fileName.isEmpty()) {
                for (int i = 0; i < 3; i++) {
                    String logName = fileName;
                    if (i > 0) {
                        logName = logName + "." + i;
                    }
                    File4690 doc = new File4690(logName);
                    if (doc.exists()) {
                        log.info("File exist ---- " + doc.getName());
                        if (fileSender(doc)) {
                            String sb = frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + "0";
                            String trama = Util.addLengthStartOfString(sb, properties.getInt("serverSocket.quantityBytesLength"));
                            if (socket.writeDataSocket(trama)) {
                                // log.info("Respuesta a enviar: " + trama);
                            } else {
                                log.fatal("trama no enviada");
                            }
                        } else {
                            String sb = frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + "1";
                            String trama = Util.addLengthStartOfString(sb, properties.getInt("serverSocket.quantityBytesLength"));
                            if (socket.writeDataSocket(trama)) {
                                // log.info("Respuesta a enviar: " + trama);
                            } else {
                                log.fatal("trama no enviada");
                            }
                        }
                    }
                    if (logName.toLowerCase().contains("ope")) {
                        logName = fileName.replace("Ope", "Pro");
                        doc = new File4690(logName);
                        if (doc.exists()) {
                            log.info("File exist ---- " + doc.getName());
                            if (fileSender(doc)) {
                                String sb = frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + "0";
                                String trama = Util.addLengthStartOfString(sb, properties.getInt("serverSocket.quantityBytesLength"));

                                if (socket.writeDataSocket(trama)) {
                                    // log.info("Respuesta a enviar: " + trama);
                                } else {
                                    log.fatal("trama no enviada");
                                }
                            } else {
                                String sb = frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + "1";
                                String trama = Util.addLengthStartOfString(sb, properties.getInt("serverSocket.quantityBytesLength"));
                                if (socket.writeDataSocket(trama)) {
                                    // log.info("Respuesta a enviar: " + trama);
                                } else {
                                    log.fatal("trama no enviada");
                                }
                            }
                        }
                    }
                }
            }

        } catch (FlexosException e) {
            log.error(e.getMessage(), e);
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
        return false;
    }

    protected StringBuffer getFrameHeader() {
        StringBuffer data = new StringBuffer();
        data.append("S").append(ArmsAgentConstants.Communication.FRAME_SEP)
                .append(ArmsAgentConstants.Process.FILE_RECEIVER_OPERATION)
                .append(ArmsAgentConstants.Communication.FRAME_SEP).append("000")
                .append(ArmsAgentConstants.Communication.FRAME_SEP).append(storeNumber)
                .append(ArmsAgentConstants.Communication.FRAME_SEP).append(ArmsAgentConstants.Communication.TEMP_CONN)
                .append(ArmsAgentConstants.Communication.FRAME_SEP)
                .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
        return data;
    }

    private boolean fileSender(File4690 fileToSend) {
        String filename = fileToSend.getName();
        if (filename != null) {

            log.info("Archivo a enviar: " + filename);
            //obtenemos las subcarpetas si existen
            //String subdirs = fileToSend.getAbsolutePath().substring(inFolder.getAbsolutePath().length() + 1, fileToSend.getAbsolutePath().length() - filename.length());

            StringBuffer data = getFrameHeader();
            data.append(ArmsAgentConstants.Communication.FRAME_SEP)
                    .append(outFolderServer)
                    .append(File.separator)
                    .append(storeNumber)
                    .append(File.separator)
                    .append(filename)
                    .append(ArmsAgentConstants.Communication.FRAME_SEP).append(fileToSend.length());
            List list = Arrays.asList(p.split(data.toString()));

            Frame frame = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
                    ArmsAgentConstants.Communication.FRAME_SEP);
            if (frame.loadData()) {
                boolean send = sendFileHeader(frame) && sendFileBytes(fileToSend);

                if (send) {
                    //fileToSend.delete();
                    log.info("Archivo enviado correctamente.");
                    closeConnection();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                    return true;
                } else {
                    log.error("Error al enviar el archivo.");

                    return false;
                }
            }
        }

        return false;

    }

    protected boolean sendFileBytes(File4690 fileToSend) {

        int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");

        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream4690(fileToSend));
            long totalBytesToRead = fileToSend.length();
            byte[] byteArray = new byte[(int) (totalBytesToRead < 8192 ? totalBytesToRead : 8192)];

            while (bis.read(byteArray) != -1 && totalBytesToRead > 0) {
                socketClient.writeByteArraySocket(byteArray);
                totalBytesToRead = totalBytesToRead - 8192;
                if (totalBytesToRead < 8192 && totalBytesToRead > 0) {
                    byteArray = new byte[(int) totalBytesToRead];
                }
            }
            try {
                bis.close();
            } catch (IOException ex) {

            }
            int numberOfBytes = 0;
            int timeOutCycles = 0;
            while (numberOfBytes == 0) {
                numberOfBytes = socketClient.readLengthDataSocket();
                if (timeOutCycles == 5) {
                    // cada 5 timeouts escribimos una trama vacía para asegurarnos que el socket
                    // esté activo
                    String mje = Util.addLengthStartOfString("", qtyBytesLength);
                    if (!socketClient.writeDataSocket(mje)) {
                        socketClient.setConnected(false);
                        return false;
                    }
                    timeOutCycles = 0;
                }
                timeOutCycles++;
            }
            if (numberOfBytes > 0) {
                String str = socketClient.readDataSocket(numberOfBytes);
                if (StringUtils.isNotBlank(str)) {
                    List list = Arrays.asList(p.split(str));
                    Frame frameRpta = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
                            ArmsAgentConstants.Communication.FRAME_SEP);
                    log.info("Respuesta recibida: " + frameRpta.toString());
                    if (frameRpta.getStatusTrama() == 0) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            socketClient.setConnected(false);
        }
        return false;
    }

    protected boolean sendFileHeader(Frame frame) {
        String str;
        List list;
        Frame frameRpta;
        int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
        try {

            if (socketClient == null || !socketClient.isConnected()) {
                connectClient();
            }
            //else
            //log.info("SocketClient: " + socketClient +", isConnected: "+ socketClient.isConnected());

            String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength);
            log.info("Trama a enviar: " + trama);
            if (socketClient.writeDataSocket(trama)) {
                int numberOfBytes = 0;
                int timeOutCycles = 0;
                while (numberOfBytes == 0) {
                    numberOfBytes = socketClient.readLengthDataSocket();
                    if (timeOutCycles == 5) {
                        // cada 5 timeouts escribimos una trama vacía para asegurarnos que el socket
                        // esté activo
                        String mje = Util.addLengthStartOfString("", qtyBytesLength);
                        if (!socketClient.writeDataSocket(mje)) {
                            socketClient.setConnected(false);
                            return false;
                        }
                        timeOutCycles = 0;
                    }
                    timeOutCycles++;
                }
                if (numberOfBytes > 0) {
                    str = socketClient.readDataSocket(numberOfBytes);
                    if (StringUtils.isNotBlank(str)) {
                        list = Arrays.asList(p.split(str));
                        frameRpta = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
                                ArmsAgentConstants.Communication.FRAME_SEP);
                        log.info("Respuesta recibida: " + frameRpta.toString());
                        if (frameRpta.getStatusTrama() == 0) {
                            return true;
                        }
                    }
                }
            } else {
                socketClient.setConnected(false);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            socketClient.setConnected(false);
        }
        return false;
    }

    protected boolean connectClient() {
        log.info("IP a conectar" + properties.getObject("clientSocket.ip") + "puerto " + properties.getInt("clientSocket.port"));
        if (socketClient == null) {
            socketClient = new ConnSocketClient();
            socketClient.setIpServer(properties.getObject("clientSocket.ip"));
            socketClient.setPortServer(properties.getInt("clientSocket.port"));
            socketClient.setRetries(properties.getInt("clientSocket.retries"));
            socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
            socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
            socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
        }
        return socketClient.connectSocketUsingRetries();
    }

    private void closeConnection() {
        log.info("Intentando cerrar coneccion");
        if (socketClient != null) {
            socketClient.closeConnection();
            socketClient = null;
            log.info("Coneccion cerrada");
        }
    }

}
