package com.allc.arms.agent.processes.colas;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClientProxy;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;
import com.ibm.OS4690.POSFile;
import com.ibm.OS4690.RandomAccessFile4690;

public class RegistroPersonasEnColaProcessV2 extends AbstractProcess {

    protected Logger log = Logger.getLogger(RegistroPersonasEnColaProcessV2.class);
    protected PropFile properties;
    protected String AlertSeekFileName;
    protected POSFile posFileSeekWriter = null;
    protected long timeSleep;
    protected boolean endAlertProcess;
    protected boolean finished;
    protected String resp;
    protected ConnSocketClientProxy socketClient;
    protected String storeNumber = "";
    protected RandomAccessFile4690 randSeekRead = null;
    protected RandomAccessFile4690 randFileRead = null;
    protected String descriptorProceso;
    public static boolean storeClosePassed;
    protected static Pattern p;

    protected boolean init() {
        boolean result = false;
        try {
            storeClosePassed = false;
            ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
            storeNumber = controllerStatusData.getStoreNumber();
            while (storeNumber.length() < 3) {
                storeNumber = "0" + storeNumber;
            }
            descriptorProceso = "ALERT_PERS_COLA_P";
            properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
            Files.creaEscribeDataArchivo4690(getEyesFileName(), descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|STR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Iniciando proceso de lectura de Alertas Personas en Colas.\n", true);
            AlertSeekFileName = properties.getObject("alertaPersonaCola.file.seek");
            timeSleep = Long.parseLong(properties.getObject("alertaPersonaCola.timeSleep").toString());
            endAlertProcess = false;
            finished = false;

            p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
            result = true;
            log.info("Version compilada:" + ArmsAgentConstants.VERSION_COMPILADO);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public void run() {
        try {
            if (init()) {
                while (!endAlertProcess) {
                    if (readAlertReg()) {
                        Thread.sleep(timeSleep);
                    } else {
                        if (!endAlertProcess) {
                            Thread.sleep(timeSleep * 2);
                        }
                    }
                }
                closeClient();
                Files.creaEscribeDataArchivo4690(getEyesFileName(), descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|END|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Finalizó el proceso de lectura de Alertas Personas en Colas.\n", true);
            } else {
                Files.creaEscribeDataArchivo4690(getEyesFileName(), descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|ERR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error al iniciar el proceso de lectura de Alertas Personas en Colas.\n", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Files.creaEscribeDataArchivo4690(getEyesFileName(), descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|ERR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error en el proceso de lectura de Alertas Personas en Colas.\n", true);
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
            }
            log.error(e.getMessage(), e);
        }
        finished = true;
    }

    protected boolean readAlertReg() {
        //boolean isStoreClosed = false;
        String linea = "";
        boolean sent = true;
        long tmp = 0;
        String valorPosicion;
        try {
            if (!Files.fileExists4690(AlertSeekFileName)) {
                return false;
            }

            String newAlertFileName = obtieneNombreSeek();
            long punteroFile = obtieneOffsetSeek();
            log.info("newAlertFileName:" + newAlertFileName + ", puntero:" + punteroFile);
            if (newAlertFileName == null) {
                return false;
            }

            if (punteroFile >= 0) {
                randFileRead = new RandomAccessFile4690(newAlertFileName, "r");
                posFileSeekWriter = new POSFile(AlertSeekFileName, "rw", POSFile.SHARED_READ_WRITE_ACCESS);

                boolean errorReadAlert = false;

                while (!endAlertProcess && !errorReadAlert && !storeClosePassed) {
                    resp = "2";
                    try {
                        linea = Files.readLineByBytesPositionOfFileWriteIn4690(randFileRead, punteroFile);
                    } catch (Exception e) {
                        //agregamos esto para que cuando hagan un cierre "forzado" no quede en un bucle infinito
                        linea = null;
                        errorReadAlert = true;
                        log.error(e.getMessage(), e);
                        Files.deleteFile4690(AlertSeekFileName);
                    }

                    if (null != linea && !linea.trim().equals("")) {
                        log.info("Linea: " + linea + ", puntero: " + punteroFile);
                        try {
                            List list = Arrays.asList(p.split(linea));

                            Frame frameS = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
                                    ArmsAgentConstants.Communication.FRAME_SEP);

                            if (frameS.loadData()) {
                                try {
                                    sent = sendFileHeader(frameS);
                                    if (sent) {
                                        Thread.sleep(10000);
                                        log.info("Si pudo enviar mensaje.");
                                    } else {
                                        Files.creaEscribeDataArchivo4690(getEyesFileName(), descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|WAR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|No pudo enviar el mensaje de alerta al server.\n", true);
                                        log.info("No pudo enviar el mensaje.");
                                    }
                                } catch (InterruptedException e) {
                                    Files.creaEscribeDataArchivo4690(getEyesFileName(), descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|WAR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|No pudo enviar el mensaje de alerta al server, Exeption:" + e.getMessage() + ".\n", true);
                                    log.error(e.getMessage(), e);
                                }
                            }
                            log.info("resp: " + resp);
                        } catch (Exception e) {
                            Files.creaEscribeDataArchivo4690(getEyesFileName(), descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|ERR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|No pudo enviar el mensaje de alerta al server, Exeption:" + e.getMessage() + ".\n", true);
                            log.error(e.getMessage(), e);
                        }
                        if (sent) {
                            punteroFile = randFileRead.getFilePointer();
                            log.info("punteroFile: " + punteroFile);
                            tmp = punteroFile++;
                            log.info("tmp: " + tmp);
                            valorPosicion = Util.rpad(String.valueOf(tmp), ArmsAgentConstants.Communication.SPACE, 20)
                                    + ArmsAgentConstants.Communication.CRLF;
                            log.info("valorPosicion: " + valorPosicion.toString() + ",valorPosicion.length():" + valorPosicion.length());
                            posFileSeekWriter.write(valorPosicion.getBytes(), 0, POSFile.FROM_START_OF_FILE, POSFile.FLUSH,
                                    valorPosicion.length());
                            log.info("posFileSeekWriter: " + posFileSeekWriter.toString());
                        }
                    }

                    Thread.sleep(100);
                }
                try {
                    Thread.sleep(timeSleep);
                } catch (InterruptedException e) {
                    Files.creaEscribeDataArchivo4690(getEyesFileName(), descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|ERR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error durmiendo el proceso.\n", true);
                    log.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (posFileSeekWriter != null) {
                    posFileSeekWriter.closeFull();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            try {
                if (randFileRead != null) {
                    randFileRead.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            try {
                if (randSeekRead != null) {
                    randSeekRead.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return false;

    }

    protected boolean sendFileHeader(Frame frame) {
        String str;
        List list;

        int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
        try {
            if (socketClient == null || !socketClient.isConnected()) {
                log.info("socket 1");
                connectClient();
            }
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
                        log.info("Trama recibida str " + str);
                        list = Arrays.asList(p.split(str));
                        resp = list.get(0).toString();
                        String r = list.get(list.size() - 1).toString();
                        if (list.get(list.size() - 1).toString().trim().equals("0")) {
                            closeClient();
                            return true;
                        } else {
                            closeClient();
                        }
                    }
                }
            } else {
                closeClient();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            closeClient();
        }

        return false;
    }

    protected boolean connectClient() {
        if (socketClient == null) {
            socketClient = new ConnSocketClientProxy();
            socketClient.setIpServer(properties.getObject("clientSocket.ip"));
            socketClient.setPortServer(properties.getInt("clientSocket.port"));
            socketClient.setIpServer2(properties.getObject("clientSocket.ip2"));
            socketClient.setPortServer2(properties.getInt("clientSocket.port2"));
            socketClient.setRetries(properties.getInt("clientSocket.retries"));
            socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
            socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
            socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
        }
        return socketClient.connectSocketUsingRetries();
    }

    protected String obtieneNombreSeek() {
        String data = null;
        try {
            randSeekRead = new RandomAccessFile4690(AlertSeekFileName, "r");
            randSeekRead.readLine();
            //la linea que contiene el nombre es la segunda
            data = randSeekRead.readLine();
            randSeekRead.seek(0);
            randSeekRead.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return data;
    }

    protected long obtieneOffsetSeek() {
        long punteroFile = 0l;
        try {
            randSeekRead = new RandomAccessFile4690(AlertSeekFileName, "r");
            String data;
            try {
                data = randSeekRead.readLine();
                randSeekRead.seek(0);
                if (null == data) {
                    punteroFile = 0;
                } else {
                    punteroFile = Long.parseLong(data.replaceAll(" ", ""));
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                punteroFile = -1;
            } catch (NumberFormatException e) {
                log.error(e.getMessage(), e);
                punteroFile = -1;
            }
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        return punteroFile;
    }

    protected void closeClient() {
        if (socketClient != null && socketClient.isConnected()) {
            socketClient.closeConnection();
            socketClient = null;
        }
    }

    private String getEyesFileName() {
        return properties.getObject("eyes.ups.file.name") + "_" + ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
    }

    public boolean shutdown(long timeToWait) {
        endAlertProcess = true;
        closeClient();
        long startTime = Calendar.getInstance().getTimeInMillis();
        log.info("Deteniendo TSLReader...");
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
        return true;
    }

}
