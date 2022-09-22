/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.processes.tsl;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.equipo.Equipo;
import com.allc.arms.server.persistence.equipo.EquipoDAO;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.server.persistence.tsl.control.TslProcessControl;
import com.allc.arms.server.persistence.tsl.control.TslProcessControlDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
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
public class TslControlProcess extends AbstractProcess {
    
    protected static Logger log = Logger.getLogger(TslControlProcess.class);
    
    protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
    
    protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
    
    protected ConnSocketClient socketClient = null;
    
    protected boolean finished = false;
    
    public boolean isEnd = false;
    
    private StoreDAO storeDAO;
    
    private EquipoDAO equipoDAO;
    
    private TslProcessControlDAO controlDAO;
    
    private Session sessionSaadmin = null;
    
    private Session sessionEyesmin = null;
    
    private void iniciarSaadminSesion() {
        while (sessionSaadmin == null && !isEnd) {
            try {
                sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
            } catch (HibernateException e) {
                log.error(e.getMessage(), e);
            }
            if (sessionSaadmin == null) {
                try {
                    log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
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
                log.error(e.getMessage(), e);
            }
            if (sessionEyesmin == null) {
                try {
                    log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        finished = true;
    }
    
    protected boolean connectClient(String ip) {
        if (socketClient == null || !socketClient.isConnected()) {
            socketClient = new ConnSocketClient();
            socketClient.setIpServer(ip);
            socketClient.setPortServer(properties.getInt("clientSocket.port"));
            socketClient.setRetries(properties.getInt("clientSocket.retries"));
            socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
            socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
            socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
        }
        return socketClient.connectSocketUsingRetries();
    }
    
    protected void closeConnection() {
        if (socketClient != null) {
            log.info("Cerrando socket");
            socketClient.closeConnection();
            socketClient = null;
        }
    }
    
    @Override
    public void run() {
        log.info("Inicia proceso de TslControlProcess");
        while (!isEnd) {
            log.info("Inicia proceso de Inicia proceso de base de datos");
            iniciarSaadminSesion();
            iniciarEyesSesion();
            storeDAO = new StoreDAO();
            controlDAO = new TslProcessControlDAO();
            equipoDAO = new EquipoDAO();
            List<Store> allActiveStores = storeDAO.getAllActiveStores(sessionSaadmin);
            log.info("Busca tiendas para procesar -- total encontradas " + allActiveStores.size());
            for (Store store : allActiveStores) {
                try {
                    log.info("Busca Equipo asociado a la tienda " + store.getKey());
                    Equipo equipo = equipoDAO.findOnlineByIdStore(sessionEyesmin, store.getKey());
                    if (equipo != null) {
                        StringBuffer data = new StringBuffer();
                        data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
                                .append(ArmsServerConstants.Process.OPERATOR_TSL)
                                .append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
                                .append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
                                .append(ArmsServerConstants.Communication.FRAME_SEP)
                                .append(ArmsServerConstants.Communication.TEMP_CONN)
                                .append(ArmsServerConstants.Communication.FRAME_SEP)
                                .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
                                .append(ArmsServerConstants.Communication.FRAME_SEP).append("");
                        log.info("Encuentra equipo y envia " + store.getIp());
                        if (sendToLocal(data, store)) {
                            closeConnection();
                        }
                    } else {
                        log.info("No ncuentra equipo ");
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            try {
                if (sessionEyesmin != null) {
                    sessionEyesmin.close();
                }
                if (sessionSaadmin != null) {
                    sessionSaadmin.close();
                }
                Thread.sleep(properties.getInt("tsl.contol.process.timer"));
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        finished = true;
    }
    
    protected boolean sendToLocal(StringBuffer data, Store store) {
        List list = Arrays.asList(p.split(data.toString()));
        Frame frame = new Frame(list,
                ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
                ArmsServerConstants.Communication.FRAME_SEP);
        if (frame.loadData()) {
            boolean send = sendFrameToLocal(frame, store);
            if (send) {
                log.info("Archivo enviado correctamente.");
                return true;
            } else {
                log.error("Error al enviar al server.");
            }
        }
        return false;
    }
    
    protected boolean sendFrameToLocal(Frame frame, Store tienda) {
        String str;
        List list;
        Frame frameRpta;
        int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
        try {
            boolean connectClient = false;
            if (socketClient == null || !socketClient.isConnected()) {
                connectClient = connectClient(tienda.getIp());
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
                                //  TslProcessControl tpc = controlDAO.getProcessControlBYIdTienda(sessionSaadmin, tienda.getStoreId());
                                //if (tpc == null) {
                                TslProcessControl tpc = new TslProcessControl();
                                tpc.setIdTienda(tienda.getKey());
                                // }
                                tpc.setTslSeekBit(Integer.parseInt(list.get(6).toString()));
                                tpc.setTslSeekFileName(list.get(7).toString());
                                tpc.setFileName(list.get(8).toString());
                                tpc.setFileBit(Integer.parseInt(list.get(9).toString()));
                                controlDAO.insertControl(sessionSaadmin, tpc);
                                return true;
                            }
                        }
                    }
                } else {
                    socketClient.setConnected(false);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            socketClient.setConnected(false);
        } finally {
            closeConnection();
            
        }
        return false;
    }
    
    @Override
    public boolean shutdown(long timeToWait) {
        isEnd = true;
        closeConnection();
        long startTime = Calendar.getInstance().getTimeInMillis();
        log.info("Deteniendo TslControlProcess...");
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
        log.info("Finalizó el Proceso de Operadores.");
        return true;
    }
    
}
