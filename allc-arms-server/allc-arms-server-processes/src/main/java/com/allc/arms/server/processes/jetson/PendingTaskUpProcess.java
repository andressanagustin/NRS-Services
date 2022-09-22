/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.processes.jetson;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.equipo.EquipoDAO;
import com.allc.arms.server.persistence.equipo.Jetson;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.thoughtworks.xstream.XStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

/**
 *
 * @author Tyrone Lopez
 */
public class PendingTaskUpProcess extends AbstractProcess {

    protected static Logger log = Logger.getLogger(PendingTaskUpProcess.class.getName());
    protected PropFile prop = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
    public boolean isEnd = false;
    protected boolean finished = false;
    private Session sessionCtEquipos = null;
    private Session sessionSaadmin = null;
    private Session sessionEyes = null;
    public final Integer STATUS_INICIAL = 0;
    public final Integer STATUS_PROCESAR = 1;
    public final Integer STATUS_EN_PROCESO = 2;
    public final Integer STATUS_ACTUALIZADO = 3;
    private static final int BATCH = 50; // Cantidad de un lote
    protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
    protected ConnSocketClient socketClient = null;
    protected String descriptorProceso = "JT_PDN_UP_P";
    private Integer port = null;
    protected String storeNumber = "";
    private final String outFolderServer = "/usr/local/NRS/WWW/nrs/ALCEYES/jetson/entrenamiento";
    private EquipoDAO equipoDAO;
    protected String storeCodeLocal;
    protected String storeCodeRegional;
    protected String ipCentral;

    private void iniciarCtEquiposSesion() {
        while (sessionCtEquipos == null && !isEnd) {
            try {
                log.info("Iniciando Equipos j");
                sessionCtEquipos = HibernateSessionFactoryContainer.getSessionFactory("EquiposJ").openSession();
            } catch (HibernateException e) {
                log.error(e.getMessage(), e);
            }
            if (sessionCtEquipos == null) {
                try {
                    log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private void iniciarSaadminSesion() {
        while (sessionSaadmin == null && !isEnd) {
            try {
                log.info("Iniciando Saadmin");
                sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
            } catch (HibernateException e) {
                log.error(e.getMessage(), e);
            }
            if (sessionSaadmin == null) {
                try {
                    log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private void iniciarSesionEyes() {
        while (sessionEyes == null && !isEnd) {
            try {
                log.info("Iniciando Eeyes");
                sessionEyes = HibernateSessionFactoryContainer.getSessionFactory("Eyes").openSession();
            } catch (HibernateException e) {
                log.error(e.getMessage(), e);
            }
            if (sessionEyes == null) {
                try {
                    log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    sleep(3000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void run() {
        log.info("Inicia proceso " + descriptorProceso);
        while (!isEnd) {
            try {
                log.info("Inicia proceso ");
                iniciarCtEquiposSesion();
                iniciarSaadminSesion();
                iniciarSesionEyes();
                equipoDAO = new EquipoDAO();
                // si es distinto de 000 es un servidor local
                storeCodeLocal = prop.getObject("eyes.store.code");
                // si es dintinto de 000 es un servidor regional
                storeCodeRegional = prop.getObject("eyes.store.code.group");
                log.info("Code " + storeCodeLocal + " Group " + storeCodeRegional);
                if (Integer.parseInt(storeCodeLocal) == 0 && Integer.parseInt(storeCodeRegional) != 0) {
                    ipCentral = prop.getObject("clientSocket.ipCentral");
                    port = prop.getInt("serverSocket.port");
                    log.info("Es servidor regional");
                    log.info("--- buscando Jetsdon para enviar al central AGREGAR ---");
                    List<Jetson> jetsonsProcesar = equipoDAO.getJetsonSenderToRegisterUp(sessionCtEquipos, STATUS_PROCESAR);
                    XStream xstreamAux = new XStream();
                    xstreamAux.alias("JetsonList", List.class);
                    xstreamAux.alias("Jetson", Jetson.class);
                    if (jetsonsProcesar != null && !jetsonsProcesar.isEmpty()) {
                        int sum = jetsonsProcesar.size(); // total
                        List<Jetson> tmpList = new ArrayList<>(); // Almacena un lote de datos
                        for (int i = 0; i < sum; i++) {
                            tmpList.add(jetsonsProcesar.get(i)); // Almacenamiento de datos
                            if (BATCH == tmpList.size() || i == sum - 1) { // Cuando el contenedor temporal almacena suficiente para un lote o no hay datos, realice el procesamiento por lotes y luego vacíe el contenedor
                                // Procesar un lote listo
                                String opXML = xstreamAux.toXML(tmpList);
                                StringBuffer data = createHeadreDta("N");
                                data.append(ArmsServerConstants.Communication.FRAME_SEP).append(opXML);
                                log.info("XML: " + opXML);
                                log.info("Ip: " + ipCentral + ", port: " + port);
                                if (sendToLocal(data, ipCentral)) {
                                    tmpList.forEach((jetson) -> {
                                        try {
                                            jetson.setStatusRegistrarUp(STATUS_ACTUALIZADO);
                                            equipoDAO.updateJetson(sessionCtEquipos, jetson);
                                        } catch (Exception e) {
                                            log.error("Error al cambiar estado ping up " + jetson.getMacEqu());
                                        }
                                    });
                                    closeConnection();
                                } else {
                                    log.info("Error al enviar requermiento N ");
                                }
                                tmpList.clear(); // Vacío, guarda el siguiente lote
                            }
                        }
                    }
                    log.info("--- buscando Jetsdon para enviar al central STATUS ---");
                    jetsonsProcesar = equipoDAO.getJetsonSenderToPingUp(sessionCtEquipos, STATUS_PROCESAR);
                    if (jetsonsProcesar != null && !jetsonsProcesar.isEmpty()) {
                        int sum = jetsonsProcesar.size(); // total
                        List<Jetson> tmpList = new ArrayList<>(); // Almacena un lote de datos
                        for (int i = 0; i < sum; i++) {
                            tmpList.add(jetsonsProcesar.get(i)); // Almacenamiento de datos
                            if (BATCH == tmpList.size() || i == sum - 1) { // Cuando el contenedor temporal almacena suficiente para un lote o no hay datos, realice el procesamiento por lotes y luego vacíe el contenedor
                                // Procesar un lote listo
                                String opXML = xstreamAux.toXML(tmpList);
                                StringBuffer data = createHeadreDta("S");
                                data.append(ArmsServerConstants.Communication.FRAME_SEP)
                                        .append(opXML);
                                log.info("XML: " + opXML);
                                log.info("Ip: " + ipCentral + ", port: " + port);

                                if (sendToLocal(data, ipCentral)) {
                                    tmpList.forEach((jetson) -> {
                                        try {
                                            jetson.setPingStatusUp(STATUS_ACTUALIZADO);
                                            equipoDAO.updateJetson(sessionCtEquipos, jetson);
                                        } catch (Exception e) {
                                            log.error("Error al cambiar estado ping up " + jetson.getMacEqu());
                                        }
                                    });
                                    closeConnection();
                                } else {
                                    log.info("Error al enviar requermiento s ");
                                }
                                tmpList.clear(); // Vacío, guarda el siguiente lote
                            }
                        }
                    }
                    log.info("--- buscando Jetson para enviar al central UPLOAD ---");
                    jetsonsProcesar = equipoDAO.getJetsonSenderToUploadUp(sessionCtEquipos, STATUS_PROCESAR);
                    if (jetsonsProcesar != null && !jetsonsProcesar.isEmpty()) {
                        int sum = jetsonsProcesar.size(); // total
                        List<Jetson> tmpList = new ArrayList<>(); // Almacena un lote de datos
                        for (int i = 0; i < sum; i++) {
                            File archivo = new File(outFolderServer + File.separator + jetsonsProcesar.get(i).getFileUploadUp());
                            log.info("Archivo para enviar:" + archivo);
                            String dir = "";
                            String[] dirs = jetsonsProcesar.get(i).getFileUploadUp().split("/");
                            for (int j = 0; j < dirs.length - 1; j++) {
                                dir = dir.concat(dirs[j]) + File.separator;
                            }
                            if (fileSender(archivo, ipCentral, outFolderServer + File.separator + dir)) {
                                tmpList.add(jetsonsProcesar.get(i)); // Almacenamiento de datos
                            }
                            if (BATCH == tmpList.size() || i == sum - 1) {
                                String opXML = xstreamAux.toXML(tmpList);
                                StringBuffer data = createHeadreDta("U");
                                data.append(ArmsServerConstants.Communication.FRAME_SEP)
                                        .append(opXML);
                                log.info("XML: " + opXML);
                                log.info("Ip: " + ipCentral + ", port: " + port);
                                if (sendToLocal(data, ipCentral)) {
                                    tmpList.forEach((jetson) -> {
                                        try {
                                            jetson.setStatusUploadUp(STATUS_ACTUALIZADO);
                                            jetson.setFileUploadUp(null);
                                            equipoDAO.updateJetson(sessionCtEquipos, jetson);
                                        } catch (Exception e) {
                                            log.error("Error al cambiar estado ping up " + jetson.getMacEqu());
                                        }
                                    });
                                    closeConnection();
                                } else {
                                    log.info("Error al enviar requermiento s ");
                                }
                                tmpList.clear(); // Vacío, guarda el siguiente lote
                            }
                        }
                    }
                } else {
                    log.info("No es regional");
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                if (sessionCtEquipos != null) {
                    sessionCtEquipos.close();
                    sessionCtEquipos = null;
                }
                if (sessionSaadmin != null) {
                    sessionSaadmin.close();
                    sessionSaadmin = null;
                }
                if (sessionEyes != null) {
                    sessionEyes.close();
                    sessionEyes = null;
                }
            }
            try {
                Thread.sleep(prop.getLong("jetsonSend.server.timeSleep")); //definir una propiedad
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        finished = true;
    }

    @Override
    public boolean shutdown(long timeToWait
    ) {
        isEnd = true;
        long startTime = Calendar.getInstance().getTimeInMillis();
        log.info("Deteniendo OperatorDownloadProcess...");
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
        log.info("Finalizó el Proceso de Descargas de Operadores.");
        return true;
    }

    private boolean fileSender(File fileToSend, String tienda, String filePath) {
        String filename = fileToSend.getName();
        if (filename != null) {

            log.info("Archivo a enviar: " + filename + " a " + filePath + " a la tienda " + tienda);
            //obtenemos las subcarpetas si existen
            //String subdirs = fileToSend.getAbsolutePath().substring(inFolder.getAbsolutePath().length() + 1, fileToSend.getAbsolutePath().length() - filename.length());

            StringBuffer data = getFrameHeader();
            data.append(ArmsServerConstants.Communication.FRAME_SEP).append(filePath).append(filename)
                    .append(ArmsServerConstants.Communication.FRAME_SEP).append(fileToSend.length());
            List list = Arrays.asList(p.split(data.toString()));

            Frame frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
                    ArmsServerConstants.Communication.FRAME_SEP);
            if (frame.loadData()) {
                boolean send = sendFileHeader(frame, tienda) && sendFileBytes(fileToSend);

                if (send) {
                    //fileToSend.delete();
                    log.info("Archivo enviado correctamente.");
                    closeClient();
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

    protected StringBuffer getFrameHeader() {
        StringBuffer data = new StringBuffer();
        data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
                .append(ArmsServerConstants.Process.FILE_RECEIVER_OPERATION)
                //.append("19")
                .append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
                .append(ArmsServerConstants.Communication.FRAME_SEP).append(storeNumber)
                .append(ArmsServerConstants.Communication.FRAME_SEP).append(ArmsServerConstants.Communication.TEMP_CONN)
                .append(ArmsServerConstants.Communication.FRAME_SEP)
                .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
        return data;
    }

    protected boolean sendFileBytes(File fileToSend) {

        int qtyBytesLength = prop.getInt("clientSocket.quantityBytesLength");

        try {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSend))) {
                long totalBytesToRead = fileToSend.length();
                byte[] byteArray = new byte[(int) (totalBytesToRead < 8192 ? totalBytesToRead : 8192)];

                while (bis.read(byteArray) != -1 && totalBytesToRead > 0) {
                    socketClient.writeByteArraySocket(byteArray);
                    totalBytesToRead = totalBytesToRead - 8192;
                    if (totalBytesToRead < 8192 && totalBytesToRead > 0) {
                        byteArray = new byte[(int) totalBytesToRead];
                    }
                }
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
                    Frame frameRpta = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
                            ArmsServerConstants.Communication.FRAME_SEP);
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

    protected boolean sendFileHeader(Frame frame, String ip) {
        String str;
        List list;
        Frame frameRpta;
        int qtyBytesLength = prop.getInt("clientSocket.quantityBytesLength");
        try {

            if (socketClient == null || !socketClient.isConnected()) {
                connectServer(ip);
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
                        frameRpta = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
                                ArmsServerConstants.Communication.FRAME_SEP);
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

    private boolean connectServer(String ip) {
        if (socketClient == null || !socketClient.isConnected()) {
            log.info("Store IP: " + ip + ", port: " + port);
            socketClient = new ConnSocketClient();
            socketClient.setIpServer(ip);
            socketClient.setPortServer(port);
            //socketClient.setPortServer(properties.getInt("serverSocket.port"));
            socketClient.setRetries(2);
            socketClient.setTimeOutConnection(prop.getInt("serverSocket.timeOutConnection"));
            //socketClient.setTimeOutConnection(300000);
            socketClient.setTimeOutSleep(prop.getInt("serverSocket.timeOutSleep"));
            socketClient.setQuantityBytesLength(prop.getInt("serverSocket.quantityBytesLength"));
        }
        return socketClient.connectSocketUsingRetries();
    }

    protected void closeClient() {
        if (socketClient != null) {
            socketClient.closeConnection();
        }
    }

    private StringBuffer createHeadreDta(String req) {
        StringBuffer data = new StringBuffer();
        data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
                .append(ArmsServerConstants.Process.OPERATOR_JETSON_PENDING)
                .append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
                .append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
                .append(ArmsServerConstants.Communication.FRAME_SEP)
                .append(ArmsServerConstants.Communication.TEMP_CONN)
                .append(ArmsServerConstants.Communication.FRAME_SEP)
                .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
                .append(ArmsServerConstants.Communication.FRAME_SEP)
                .append(req);
        return data;
    }

    protected boolean sendToLocal(StringBuffer data, String ip) {
        List list = Arrays.asList(p.split(data.toString()));
        Frame frame = new Frame(list,
                ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
                ArmsServerConstants.Communication.FRAME_SEP);
        if (frame.loadData()) {
            boolean send = sendFrameToLocal(frame, ip);
            if (send) {
                log.info("Archivo enviado correctamente.");
                return true;
            } else {
                log.error("Error al enviar al server.");
            }
        }
        return false;
    }

    protected void closeConnection() {
        if (socketClient != null) {
            socketClient.closeConnection();
            socketClient = null;
        }
    }

    protected boolean sendFrameToLocal(Frame frame, String tienda) {
        String str;
        List list;
        Frame frameRpta;
        int qtyBytesLength = prop.getInt("clientSocket.quantityBytesLength");
        try {
            if (socketClient == null || !socketClient.isConnected()) {
                connectClient(tienda);
            }
            String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength);
            log.info("Trama a enviar: " + trama);
            //log.info("Socket ip:" + socketClient.getIpServer() +", port:" + socketClient.getPortServer());
            if (socketClient.writeDataSocket(trama)) {
                int numberOfBytes = 0;
                int timeOutCycles = 0;
                while (numberOfBytes == 0) {
                    numberOfBytes = socketClient.readLengthDataSocket();
                    if (timeOutCycles == 5) {
                        // cada 5 timeouts escribimos una trama vacía para
                        // asegurarnos que el socket esté activo
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
                        frameRpta = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
                                ArmsServerConstants.Communication.FRAME_SEP);
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

    protected boolean connectClient(String ip) {
        if (socketClient == null || !socketClient.isConnected()) {
            log.info("Store IP: " + ip + ", port: " + port);
            socketClient = new ConnSocketClient();
            socketClient.setIpServer(ip);
            socketClient.setPortServer(port);
            //socketClient.setPortServer(properties.getInt("serverSocket.port"));
            socketClient.setRetries(2);
            socketClient.setTimeOutConnection(prop.getInt("serverSocket.timeOutConnection"));
            //socketClient.setTimeOutConnection(300000);
            socketClient.setTimeOutSleep(prop.getInt("serverSocket.timeOutSleep"));
            socketClient.setQuantityBytesLength(prop.getInt("serverSocket.quantityBytesLength"));
        }
        return socketClient.connectSocketUsingRetries();
    }
}
