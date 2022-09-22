/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.processes.store;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.Socket;

import java.util.regex.Pattern;
import java.util.Iterator;
import java.util.Calendar;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import java.io.IOException;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.equipo.Equipo;
import com.allc.arms.server.persistence.equipo.EquipoDAO;
import com.allc.arms.server.persistence.store.BusinessStore;
import com.allc.arms.server.persistence.store.RetailStoreDAO;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreUtil;
import com.allc.arms.server.processes.utils.AbstractProcessPrincipal;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.allc.comm.frame.Frame;
import com.allc.entities.RetailStore;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import com.thoughtworks.xstream.hibernate.mapper.HibernateMapper;
import com.thoughtworks.xstream.hibernate.converter.HibernateProxyConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentMapConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentSortedMapConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentSortedSetConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentCollectionConverter;

/**
 *
 * @author ruben.gomez
 */
public class StoreUpdateProcess extends AbstractProcessPrincipal {

    protected static Logger log = Logger.getLogger(StoreUpdateProcess.class);
    protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
    protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
    protected ConnSocketClient socketClient = null;
    public final Integer STATUS_PROCESAR = 1;
    public final Integer STATUS_EN_PROCESO = 2;
    public final Integer STATUS_PROCESADO = 3;
    protected Session sessionSaadmin = null;
    protected Session sessionArts = null;
    protected Session sessionEyes = null;
    protected boolean finished = false;
    public boolean isEnd = false;

    final static XStream xstream = new XStream() {
        protected MapperWrapper wrapMapper(final MapperWrapper next) {
            return new HibernateMapper(next);
        }
    };

    public void run() {

        xstream.registerConverter(new HibernateProxyConverter());
        xstream.registerConverter(new HibernatePersistentCollectionConverter(xstream.getMapper()));
        xstream.registerConverter(new HibernatePersistentMapConverter(xstream.getMapper()));
        xstream.registerConverter(new HibernatePersistentSortedMapConverter(xstream.getMapper()));
        xstream.registerConverter(new HibernatePersistentSortedSetConverter(xstream.getMapper()));
        xstream.setMode(XStream.NO_REFERENCES);

        while (!isEnd) {
            
            if (isPrincipal()) {
                log.info("Buscando actualizacion de Tiendas...");
                String storeCode = properties.getObject("eyes.store.code");
                
                iniciarSesionArts();
                iniciarSesionEyes();
                iniciarSesionSaadmin();
                
                StoreDAO storeDAO = new StoreDAO();
                EquipoDAO equipoDAO = new EquipoDAO();
                RetailStoreDAO retailStoreDAO = new RetailStoreDAO();

                try {
                    List stores = storeDAO.getStoresByStatus(sessionSaadmin, STATUS_PROCESAR); 

                    if (stores != null && !stores.isEmpty()) {
                        log.info("Se procesaran " + stores.size() + " tiendas.");
                        Iterator itStores = stores.iterator();

                        while (itStores.hasNext()) {
                            Store store = null;
                            store = (Store) itStores.next();
                            
                            UtilityFile.createWriteDataFile(getEyesFileName(), "STORE_UPD_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode + "|STR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Iniciando procesamiento de la tienda: " + store.getKey() + ".\n", true);
                            log.info("Tienda " + store.getKey() + " ESTADO DESCARGA: " + STATUS_PROCESAR);

                            try {
                                boolean connectionError = false;
                                StoreUtil storeUtil = new StoreUtil();
                                
                                store.setStatusDownload(STATUS_EN_PROCESO); 
                                storeDAO.updateStore(sessionSaadmin, store); 
                                
                                List<Equipo> equipos = equipoDAO.getAllEquiposByIdStore(sessionEyes, store.getKey());
                                RetailStore retailStore = retailStoreDAO.getRetailStoreByCode(sessionArts, store.getKey());
                                List<BusinessStore> businessStores = storeDAO.getBusinessStores(sessionSaadmin, store.getStoreId());

                                storeUtil.setStore(store);
                                storeUtil.setEquipos(equipos);
                                storeUtil.setRetailStore(retailStore);
                                storeUtil.setBusinessStores(businessStores);

                                log.info("store: " + store.toString());
                                log.info("equipos: " + equipos.toString());
                                log.info("businessStores: " + businessStores);
                                log.info("retailStore: " + retailStore.toString());
                                
                                equipos.forEach((t) -> {log.info("equipos: " + t);});
                                businessStores.forEach((t) -> { log.info("businessStore: " + t);});
                                
                                XStream xstreamAux = new XStream();
                                xstreamAux.alias("StoreUtil", StoreUtil.class);
                                String storeXML = xstreamAux.toXML(storeUtil);

                                StringBuffer data = new StringBuffer();
                                data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
                                        .append(ArmsServerConstants.Process.STORE_DEALER)
                                        .append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
                                        .append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
                                        .append(ArmsServerConstants.Communication.FRAME_SEP)
                                        .append(ArmsServerConstants.Communication.TEMP_CONN)
                                        .append(ArmsServerConstants.Communication.FRAME_SEP)
                                        .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
                                        .append(ArmsServerConstants.Communication.FRAME_SEP)
                                        .append(storeXML);

                                String ipServersHijo = hayServidorRegional(store.getKey());

                                if (ipServersHijo != null && !ipServersHijo.isEmpty()) {
                                        log.info("HAY SERVIDOR REGIONAL " + ipServersHijo + ", MANDA A TIENDA: " + store.getKey());
                                        connectionError = !sendToLocal(data, ipServersHijo);
                                        log.info("Respuesta Socket: " + connectionError);
                                        closeConnection();
                                } else {
                                    log.info("LA TIENDA: " + store.getKey()+ " , NO TIENE UN SERVIDOR REGIONAL CONFIGURADO");
                                }

                                if (!connectionError) {
                                    store.setStatusDownload(STATUS_PROCESADO);
                                    storeDAO.updateStore(sessionSaadmin, store);
                                    log.info("Tienda: " + store.getKey() + ", Actualizada.");
                                    UtilityFile.createWriteDataFile(getEyesFileName(), "STORE_UPD_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode + "|END|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Tienda: " + store.getKey() + " procesado.\n", true);
                                } else {
                                    store.setStatusDownload(STATUS_PROCESAR);
                                    storeDAO.updateStore(sessionSaadmin, store);
                                    log.info("Error de conexión con la Tienda " + store.getKey() + ", se vuelve al estado inicial: " + STATUS_PROCESAR);
                                    UtilityFile.createWriteDataFile(getEyesFileName(), "STORE_UPD_PT|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode + "|WAR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|No se pudo informar a la Tienda: " + store.getKey() + ".\n", true);
                                }
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                try {
                                    UtilityFile.createWriteDataFile(getEyesFileName(), "STORE_UPD_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode + "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error al procesar la Tienda: " + store.getKey() + ".\n", true);
                                } catch (Exception e1) {
                                    log.error(e1.getMessage(), e1);
                                }
                                store.setStatusDownload(STATUS_PROCESAR);
                                storeDAO.updateStore(sessionSaadmin, store);
                                log.error("Error de conexión, se devuelve la Tienda " + store.getKey() + " al estado inicial: " + STATUS_PROCESAR);
                            }
                        }
                    } else {
                        log.info("No hay Tiendas a procesar.");
                    }
                    sessionArts.close();
                    sessionEyes.close();
                    sessionSaadmin.close();
                    sessionArts = null;
                    sessionEyes = null;
                    sessionSaadmin = null;

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    try {
                        sessionArts.close();
                        sessionEyes.close();
                        sessionSaadmin.close();
                    } catch (HibernateException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                    sessionArts = null;
                    sessionEyes = null;
                    sessionSaadmin = null;
                }
            }
            try {
                Thread.sleep(properties.getLong("storeUpdate.timesleep"));
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

        } // finish while
        finished = true;
    }

    private String getEyesFileName() {
        return properties.getObject("eyes.ups.file.name") + "_" + ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
    }

    protected boolean sendToLocal(StringBuffer data, String ipServersHijo) {
        List list = Arrays.asList(p.split(data.toString()));
        Frame frame = new Frame(list,
                ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
                ArmsServerConstants.Communication.FRAME_SEP);
        if (frame.loadData()) {
            boolean send = sendFrameToLocal(frame, ipServersHijo);
            if (send) {
                log.info("Archivo enviado correctamente.");
                return true;
            } else {
                log.error("Error al enviar al server.");
            }
        }
        return false;
    }

    protected boolean sendFrameToLocal(Frame frame, String ipServersHijo) {
        String str;
        List list;
        Frame frameRpta;
        int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
        try {
            if (socketClient == null || !socketClient.isConnected()) {
                connectClient(properties, ipServersHijo);
            }
            String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength);
            log.info("Trama a enviar: " + trama);
            log.info("Socket ip:" + socketClient.getIpServer() + ", port:" + socketClient.getPortServer());
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

    public boolean shutdown(long timeToWait) {
        isEnd = true;
        closeConnection();
        long startTime = Calendar.getInstance().getTimeInMillis();
        log.info("Deteniendo StoreProcess...");
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
        log.info("Finalizó el Proceso de Tiendas.");
        return true;
    }

    protected void iniciarSesionArts() {
        while (sessionArts == null && !isEnd) {
            try {
                sessionArts = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
            } catch (HibernateException e) {
                log.error(e.getMessage(), e);
            }
            if (sessionArts == null) {
                try {
                    log.error("OCURRIÓ UN ERROR AL CREAR LA CONEXIÓN A LA BD, SE REINTENTARÁ EN 3 seg.");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    protected void iniciarSesionEyes() {
        while (sessionEyes == null && !isEnd) {
            try {
                sessionEyes = HibernateSessionFactoryContainer.getSessionFactory("Eyes").openSession();
            } catch (HibernateException e) {
                log.error(e.getMessage(), e);
            }
            if (sessionEyes == null) {
                try {
                    log.error("OCURRIÓ UN ERROR AL CREAR LA CONEXIÓN A LA BD, SE REINTENTARÁ EN 3 seg.");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private void iniciarSesionSaadmin() {
        while (sessionSaadmin == null && !isEnd) {
            try {
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

    /**
     * Método que prueba si la conexión esta levantada
     *
     * @param ip
     * @return true si se conecta, false si no se conecta.
     */
    protected boolean connectClientIsUp(String ip) {
        final int TIMEOUT = 10000;
        try {
            SocketAddress sockaddr = new InetSocketAddress(ip.trim(), properties.getInt("clientSocket.port"));
            Socket socket = new Socket();
            socket.connect(sockaddr, TIMEOUT);
            socket.close();
            return true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
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

    protected boolean connectClient(PropFile properties, String ipServersHijo) {
        if (socketClient == null || !socketClient.isConnected()) {
            log.info("Store IP: " + ipServersHijo + ", port: " + properties.getInt("serverSocket.port"));
            socketClient = new ConnSocketClient();
            socketClient.setIpServer(ipServersHijo);
            socketClient.setPortServer(properties.getInt("serverSocket.port"));
            socketClient.setRetries(2);
            socketClient.setTimeOutConnection(properties.getInt("serverSocket.timeOutConnection"));
            socketClient.setTimeOutSleep(properties.getInt("serverSocket.timeOutSleep"));
            socketClient.setQuantityBytesLength(properties.getInt("serverSocket.quantityBytesLength"));
        }
        return socketClient.connectSocketUsingRetries();
    }

    protected void closeConnection() {
        if (socketClient != null) {
            socketClient.closeConnection();
            socketClient = null;
        }
    }

}
