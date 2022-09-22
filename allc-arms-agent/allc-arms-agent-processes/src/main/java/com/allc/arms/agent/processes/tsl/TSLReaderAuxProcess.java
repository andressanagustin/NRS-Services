package com.allc.arms.agent.processes.tsl;

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
import com.ibm.OS4690.FlexosException;
import com.ibm.OS4690.RandomAccessFile4690;

/**
 * @author Tyrone Lopez com.allc.arms.agent.processes.tls.TSLReaderAuxProcess
 */
public class TSLReaderAuxProcess extends AbstractProcess {

    private static final Logger LOG = Logger.getLogger(TSLReaderAuxProcess.class);
    private PropFile properties;
    private String auxSeekFileName;
    private long timeSleep;
    private boolean endAlertProcess;
    private boolean finished;
    private String resp;
    private ConnSocketClientProxy socketClient;
    private String storeNumber = "";
    private RandomAccessFile4690 randSeekRead = null;
    private RandomAccessFile4690 randFileRead = null;
    private String descriptorProceso;
    public static boolean storeClosePassed;
    private static Pattern p;
    private final String valorEnCero = Util.rpad(ArmsAgentConstants.Communication.CERO, ArmsAgentConstants.Communication.SPACE, 20)
            + ArmsAgentConstants.Communication.CRLF;

    private boolean init() {
        boolean result = false;
        try {
            properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
            storeClosePassed = false;
            ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
            storeNumber = controllerStatusData.getStoreNumber();
            while (storeNumber.length() < 3) {
                storeNumber = "0" + storeNumber;
            }
            LOG.info("store number ++ " + storeNumber);
            descriptorProceso = "TSL_AUX_P";
            Files.creaEscribeDataArchivo4690(getEyesFileName(), descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|STR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Iniciando proceso de lectura de Alertas Personas en Colas.\n", true);
            auxSeekFileName = properties.getObject("tslaux.file.seek");
            timeSleep = Long.parseLong(properties.getObject("alertaPersonaCola.timeSleep"));
            endAlertProcess = false;
            finished = false;

            p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
            result = true;
            LOG.info("Version compilada: 18/08/2022");
        } catch (FlexosException e) {
            LOG.error(e.getMessage(), e);
        } catch (NumberFormatException e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
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
        } catch (InterruptedException e) {
            try {
                Files.creaEscribeDataArchivo4690(getEyesFileName(), descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|ERR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error en el proceso de lectura de Alertas Personas en Colas.\n", true);
            } catch (Exception e1) {
                LOG.error(e1.getMessage(), e1);
            }
            LOG.error(e.getMessage(), e);
        }
        finished = true;
    }

    private boolean readAlertReg() {
        //boolean isStoreClosed = false;
        String linea;
        boolean sent = true;
        long tmp;
        String valorPosicion;
        String newAlertFileName = "";
        try {
            if (!Files.fileExists4690(auxSeekFileName)) {
                LOG.info("No existe el archivo se lo crea");
                Files.creaEscribeDataArchivo4690(auxSeekFileName, valorEnCero, false);
                String tslReaderAux = properties.getObject("tslReader.file.seek.aux");
                Files.creaEscribeDataArchivo4690(auxSeekFileName, tslReaderAux + ArmsAgentConstants.Communication.CRLF, true);
            }

            newAlertFileName = obtieneNombreSeek();
            long punteroFile = obtieneOffsetSeek();
            LOG.info("newAlertFileName:" + newAlertFileName + ", puntero:" + punteroFile);
            if (newAlertFileName == null) {
                return false;
            }

            if (punteroFile >= 0) {

                boolean errorReadAlert = false;

                while (!endAlertProcess && !errorReadAlert && !storeClosePassed) {
                    randFileRead = new RandomAccessFile4690(newAlertFileName, "r");
                    resp = "2";
                    try {
                        linea = Files.readLineByBytesPositionOfFile4690(randFileRead, punteroFile);
                        LOG.info("Linea: " + linea + ", puntero: " + punteroFile);
                    } catch (Exception e) {
                        //agregamos esto para que cuando hagan un cierre "forzado" no quede en un bucle infinito
                        linea = null;
                        errorReadAlert = true;
                        LOG.error(e.getMessage(), e);
                        Files.deleteFile4690(auxSeekFileName);
                    }

                    if (null != linea && !linea.trim().equals("")) {

                        try {
                            List list = Arrays.asList(p.split(linea));

                            Frame frameS = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
                                    ArmsAgentConstants.Communication.FRAME_SEP);

                            if (frameS.loadData()) {
                                try {
                                    sent = sendFileHeader(frameS);
                                    if (sent) {
                                        Thread.sleep(10000);
                                        LOG.info("Si pudo enviar mensaje.");
                                    } else {
                                        Files.creaEscribeDataArchivo4690(getEyesFileName(), descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|WAR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|No pudo enviar el mensaje de alerta al server.\n", true);
                                        LOG.info("No pudo enviar el mensaje.");
                                    }
                                } catch (InterruptedException e) {
                                    Files.creaEscribeDataArchivo4690(getEyesFileName(), descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|WAR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|No pudo enviar el mensaje de alerta al server, Exeption:" + e.getMessage() + ".\n", true);
                                    LOG.error(e.getMessage(), e);
                                }
                            }
                            LOG.info("resp: " + resp);
                        } catch (Exception e) {
                            Files.creaEscribeDataArchivo4690(getEyesFileName(), descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|ERR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|No pudo enviar el mensaje de alerta al server, Exeption:" + e.getMessage() + ".\n", true);
                            LOG.error(e.getMessage(), e);
                        }
                        if (sent) {
                            punteroFile = randFileRead.getFilePointer();
                            LOG.info("punteroFile: " + punteroFile);
                            tmp = punteroFile++;
                            LOG.info("tmp: " + tmp);
                            valorPosicion = Util.rpad(String.valueOf(tmp), ArmsAgentConstants.Communication.SPACE, 20)
                                    + ArmsAgentConstants.Communication.CRLF;
                            LOG.info("valorPosicion: " + valorPosicion + ",valorPosicion.length():" + valorPosicion.length());
                            Files.creaEscribeDataArchivo4690ByPos(auxSeekFileName, valorPosicion + "", 0);

                        }
                    }
                    randFileRead.close();
                    LOG.info("Durmiendo: 100");
                    Thread.sleep(100);
                }
                try {
                    Thread.sleep(timeSleep);
                } catch (InterruptedException e) {
                    Files.creaEscribeDataArchivo4690(getEyesFileName(), descriptorProceso + "|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber + "|ERR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error durmiendo el proceso.\n", true);
                    LOG.error(e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            LOG.info("Terminando proceso");
            try {
                if (randFileRead != null) {
                    randFileRead.close();
                    if (StringUtils.isNotBlank(newAlertFileName)) {
                        Files.deleteFile4690(newAlertFileName);
                    }
                }
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
            try {
                if (randSeekRead != null) {
                    randSeekRead.close();
                    if (StringUtils.isNotEmpty(auxSeekFileName)) {
                        Files.deleteFile4690(auxSeekFileName);
                    }
                }
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return false;

    }

    private boolean sendFileHeader(Frame frame) {
        String str;
        List list;

        int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
        try {
            if (socketClient == null || !socketClient.isConnected()) {
                LOG.info("socket 1");
                connectClient();
            }
            String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength);
            LOG.info("Trama a enviar: " + trama);
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
                        LOG.info("Trama recibida str " + str);
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
            LOG.error(e.getMessage(), e);
            closeClient();
        }

        return false;
    }

    private boolean connectClient() {
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

    private String obtieneNombreSeek() {
        String data = null;
        try {
            randSeekRead = new RandomAccessFile4690(auxSeekFileName, "r");
            randSeekRead.readLine();
            //la linea que contiene el nombre es la segunda
            data = randSeekRead.readLine();
            randSeekRead.seek(0);
            randSeekRead.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return data;
    }

    private long obtieneOffsetSeek() {
        long punteroFile = 0l;
        try {
            randSeekRead = new RandomAccessFile4690(auxSeekFileName, "r");
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
                LOG.error(e.getMessage(), e);
                punteroFile = -1;
            } catch (NumberFormatException e) {
                LOG.error(e.getMessage(), e);
                punteroFile = -1;
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return punteroFile;
    }

    private void closeClient() {
        if (socketClient != null && socketClient.isConnected()) {
            socketClient.closeConnection();
            socketClient = null;
        }
    }

    private String getEyesFileName() {
        return properties.getObject("eyes.ups.file.name") + "_" + ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
    }

    @Override
    public boolean shutdown(long timeToWait) {
        endAlertProcess = true;
        closeClient();
        long startTime = Calendar.getInstance().getTimeInMillis();
        LOG.info("Deteniendo TSLReader...");
        while (!finished) {
            try {
                long dif = Calendar.getInstance().getTimeInMillis() - startTime;
                if (dif >= timeToWait) {
                    return false;
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return true;
    }

}
