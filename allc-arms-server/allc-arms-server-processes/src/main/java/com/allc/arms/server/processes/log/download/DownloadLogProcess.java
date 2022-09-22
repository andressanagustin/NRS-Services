/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.processes.log.download;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.equipo.Equipo;
import com.allc.arms.server.persistence.equipo.EquipoDAO;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.server.processes.utils.AbstractProcessPrincipal;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

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
public class DownloadLogProcess extends AbstractProcessPrincipal {

    private final static Logger LOGGER = Logger.getLogger(DownloadLogProcess.class.getName());

    private final PropFile prop = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);

    private Session sessionSaadmin = null;
    private Session sessionEyesmin = null;

    private boolean finished = false;
    public boolean isEnd = false;

    private StoreDAO storeDAO;
    private EquipoDAO equipoDAO;

    private ConnSocketClient socketClient = null;
    private final Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);

    private final static Integer PROCESAR = 1;
    private final static Integer PROCESANDO = 2;
    private final static Integer PROCESADO = 3;

    private void iniciarSaadminSesion() {
        while (sessionSaadmin == null && !isEnd) {
            try {
                sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
            } catch (HibernateException e) {
                LOGGER.error(e.getMessage(), e);
            }
            if (sessionSaadmin == null) {
                try {
                    LOGGER.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        finished = true;
    }

    private void iniciarEyesSesion() {
        while (sessionEyesmin == null && !isEnd) {
            try {
                sessionEyesmin = HibernateSessionFactoryContainer.getSessionFactory("Eyes").openSession();
            } catch (HibernateException e) {
                LOGGER.error(e.getMessage(), e);
            }
            if (sessionEyesmin == null) {
                try {
                    LOGGER.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        finished = true;
    }

    @Override
    public void run() {

        while (!isEnd) {
            if (isPrincipal()) {
                LOGGER.info(" ---------------------------- PROCESO de descarga de Archivos log -------------------------------");
                iniciarSaadminSesion();
                iniciarEyesSesion();
                storeDAO = new StoreDAO();
                equipoDAO = new EquipoDAO();
                List<Store> stores = storeDAO.getStoreDownloadLog(sessionSaadmin, PROCESAR);
                stores.forEach((store) -> {
                    try {
                        LOGGER.info("Buscando equipos para enviar " + store.getKey());
                        Equipo equipo = equipoDAO.findOnlineByIdStore(sessionEyesmin, store.getKey(), store.getDesClaveDownload());
                        if (equipo != null) {
                            LOGGER.info("Busca Equipo asociado a la tienda " + store.getKey());
                            StringBuffer data = new StringBuffer();
                            data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
                                    .append(ArmsServerConstants.Process.DOWNLOAD_LOG_OPERATION)
                                    .append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
                                    .append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
                                    .append(ArmsServerConstants.Communication.FRAME_SEP)
                                    .append(ArmsServerConstants.Communication.TEMP_CONN)
                                    .append(ArmsServerConstants.Communication.FRAME_SEP)
                                    .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
                                    .append(ArmsServerConstants.Communication.FRAME_SEP).append("");
                            LOGGER.info("Encuentra equipo y envia " + equipo.getIp());
                            store.setDownloadLog(PROCESANDO);
                            storeDAO.updateStore(sessionSaadmin, store);
                            if (sendToLocal(data, store, equipo.getIp())) {
                                closeConnection();
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                });
                try {
                    if (sessionEyesmin != null) {
                        sessionEyesmin.close();
                        sessionEyesmin = null;
                    }
                    if (sessionSaadmin != null) {
                        sessionSaadmin.close();
                        sessionSaadmin = null;
                    }

                } catch (HibernateException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } else {
                LOGGER.info(" NO PROCESO de descarga de Archivos log -------------------------------");

            }
            try {
                Thread.sleep(prop.getInt("tsl.contol.process.timer"));
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private boolean sendToLocal(StringBuffer data, Store store, String ip) {
        List list = Arrays.asList(p.split(data.toString()));
        Frame frame = new Frame(list,
                ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
                ArmsServerConstants.Communication.FRAME_SEP);
        if (frame.loadData()) {
            boolean send = sendFrameToLocal(frame, store, ip);
            if (send) {
                LOGGER.info("Archivo enviado correctamente.");
                return true;
            } else {
                LOGGER.error("Error al enviar al server.");
            }
        }
        return false;
    }

    boolean sendFrameToLocal(Frame frame, Store tienda, String ip) {
        String str;
        List list;
        Frame frameRpta;
        int qtyBytesLength = prop.getInt("clientSocket.quantityBytesLength");
        try {
            boolean connectClient = false;
            if (socketClient == null || !socketClient.isConnected()) {
                connectClient = connectClient(ip);
            }
            if (connectClient) {
                String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength);
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
                            frameRpta = new Frame(list, list.size() - 1,
                                    ArmsServerConstants.Communication.FRAME_SEP);
                            if (frameRpta.getStatusTrama() == 0) {
                                tienda.setDownloadLog(PROCESADO);
                                tienda.setDesClaveDownload(null);
                                storeDAO.updateStore(sessionSaadmin, tienda);
                                return true;
                            }
                        }
                    }
                } else {
                    socketClient.setConnected(false);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            socketClient.setConnected(false);
        }
        return false;
    }

    protected boolean connectClient(String ip) {
        LOGGER.info("Enviado a cliente " + ip + " " + prop.getInt("clientSocket.port"));
        if (socketClient == null || !socketClient.isConnected()) {
            socketClient = new ConnSocketClient();
            socketClient.setIpServer(ip);
            socketClient.setPortServer(prop.getInt("clientSocket.port"));
            socketClient.setRetries(prop.getInt("clientSocket.retries"));
            socketClient.setTimeOutConnection(prop.getInt("clientSocket.timeOutConnection"));
            socketClient.setTimeOutSleep(prop.getInt("clientSocket.timeOutSleep"));
            socketClient.setQuantityBytesLength(prop.getInt("clientSocket.quantityBytesLength"));
        }
        return socketClient.connectSocketUsingRetries();
    }

    protected void closeConnection() {
        if (socketClient != null) {
            socketClient.closeConnection();
            socketClient = null;
        }
    }

    @Override
    public boolean shutdown(long timeToWait) {
        isEnd = true;
        //closeConnection();
        long startTime = Calendar.getInstance().getTimeInMillis();
        LOGGER.info("Deteniendo Interfaces de carga de maestro de items ...");
        while (!finished) {
            try {
                long dif = Calendar.getInstance().getTimeInMillis() - startTime;
                if (dif >= timeToWait) {
                    return false;
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.info("Finalizó el Proceso de Interfaces de carga de maestro de items.");
        return true;
    }

}
