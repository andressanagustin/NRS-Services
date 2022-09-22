package com.allc.arms.server.processes.jetson;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.equipo.Equipo;
import com.allc.arms.server.persistence.equipo.EquipoDAO;
import com.allc.arms.server.persistence.equipo.Jetson;
import com.allc.arms.server.persistence.equipo.JetsonEntrenamiento;
import com.allc.arms.server.persistence.equipo.JetsonVersionAprendizaje;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.server.processes.utils.AbstractProcessPrincipal;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentCollectionConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentMapConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentSortedMapConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentSortedSetConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernateProxyConverter;
import com.thoughtworks.xstream.hibernate.mapper.HibernateMapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import java.io.IOException;
import java.util.LinkedList;
import org.hibernate.HibernateException;

/**
 * proceso encargado de monitorear tabla de jetson y enviar al regiona/agente
 * los archivos
 *
 * @author Andres Sanagustin
 *
 */
public class JetsonSendFileProcess extends AbstractProcessPrincipal {

    protected static Logger log = Logger.getLogger(JetsonSendFileProcess.class.getName());
    protected PropFile prop = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
    public boolean isEnd = false;
    private Session sessionCtEquipos = null;
    private Session sessionSaadmin = null;
    private Session sessionEyes = null;
    protected boolean finished = false;
    public final Integer STATUS_INICIAL = 0;
    public final Integer STATUS_BUSCAR = 1;
    public final Integer STATUS_EN_PROCESO = 2;
    public final Integer STATUS_ACTUALIZADO = 3;
    protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
    protected ConnSocketClient socketClient = null;
    protected String descriptorProceso = "JT_SEND_P";
    private Integer port = null;
    protected String storeNumber = "";
    private String outFolder = "";
    private String outFolderAgent = "";
    private final String archivoName = "";
    protected String resultOperation;

    EquipoDAO equipoDAO;
    StoreDAO storeDAO;

    final static XStream xstream = new XStream() {
        @Override
        protected MapperWrapper wrapMapper(final MapperWrapper next) {
            return new HibernateMapper(next);
        }
    };

    private void iniciarCtEquiposSesion() {
        while (sessionCtEquipos == null && !isEnd) {
            try {
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
        xstream.registerConverter(new HibernateProxyConverter());
        xstream.registerConverter(new HibernatePersistentCollectionConverter(xstream.getMapper()));
        xstream.registerConverter(new HibernatePersistentMapConverter(xstream.getMapper()));
        xstream.registerConverter(new HibernatePersistentSortedMapConverter(xstream.getMapper()));
        xstream.registerConverter(new HibernatePersistentSortedSetConverter(xstream.getMapper()));
        xstream.setMode(XStream.NO_REFERENCES);
        outFolder = "/var/www/html/nrs/ALCEYES/jetson/produccion";
        //outFolder = "C:/Users/tyrone.lopez/Downloads/ArmsServer/ArmsServer/jetson";

        //outFolderAgent = "F:/ALLC_DAT/IN/update"; //prop.getObject("fileUpdaterUp.in.folder.path");  //carpeta agente 
        //outFolderAgent = "C:/Users/tyrone.lopez/Downloads/ArmsServer/ArmsServer/jetson"; //prop.getObject("fileUpdaterUp.in.folder.path");  //carpeta agente 
        outFolderAgent = "C:/ArmsAgent/jetson";
        //archivoName = storeNumber+"-1-OperaLoadData";

        while (!isEnd) {
            if (isPrincipal()) {
                log.info("Jetson Sender Process iniciada.");
                try {
                    /**
                     * open a session *
                     */
                    iniciarCtEquiposSesion();
                    iniciarSaadminSesion();
                    iniciarSesionEyes();
                    equipoDAO = new EquipoDAO();
                    storeDAO = new StoreDAO();

                    boolean sendToRegionalServ = false; // servidor regional
                    boolean connectionError = false;
                    //boolean guardarCambios = false;

                    log.info("Buscando JETSON para enviar ping...");
                    List<Jetson> jetsonSenderBuscar = equipoDAO.getJetsonSenderToPing(sessionCtEquipos, STATUS_BUSCAR);
                    for (Jetson jetsonToSend : jetsonSenderBuscar) {
                        Equipo equipo = equipoDAO.findOnlineJetsonByIdEquipo(sessionEyes, jetsonToSend.getIdEquipo());
                        if (equipo != null) {
                            storeNumber = equipo.getIdLocal().toString();
                            Store storeToSend = storeDAO.getStoreByCode(sessionSaadmin, equipo.getIdLocal());
                            if (storeToSend != null) {
                                while (storeNumber.length() < 3) {
                                    storeNumber = "0" + storeNumber;
                                }
                                port = prop.getInt("clientSocket.port");
                                String ipServerHijo = hayServidorRegional(equipo.getIdLocal());
                                log.info("ip Server Hijo: " + ipServerHijo);

                                if (ipServerHijo != null) {
                                    storeToSend.setIp(ipServerHijo);
                                    port = prop.getInt("serverSocket.port");
                                    String opXML = jetsonToString(jetsonToSend);
                                    StringBuffer data = new StringBuffer();
                                    data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(ArmsServerConstants.Process.OPERATOR_JETSON_SENDER)
                                            .append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
                                            .append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(ArmsServerConstants.Communication.TEMP_CONN)
                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(opXML)
                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(storeToSend.getKey());
                                    log.info("XML: " + opXML);

                                    log.info("Ip: " + storeToSend.getIp() + ", port: " + port);
                                    connectionError = !sendToLocal(data, storeToSend.getIp());
                                    log.info("Respuesta Socket: " + connectionError);
                                    closeConnection();
                                    sendToRegionalServ = true;
                                }
                                if (!sendToRegionalServ && !connectionError) {
                                    StringBuffer data = new StringBuffer();
                                    String xml = jetsonToString(jetsonToSend);
                                    data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(ArmsServerConstants.Process.PUBLISH_JETSON_OPERATION)
                                            .append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
                                            .append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(ArmsServerConstants.Communication.TEMP_CONN)
                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append("s")
                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(xml);
                                    //port = prop.getInt("clientSocket.port");
                                    log.info("trama, Ip: " + equipo.getIp() + ", port: " + port);
                                    connectionError = !sendToLocal(data, equipo.getIp());
                                    log.info("Respuesta Socket: " + connectionError);
                                }
                                if (!connectionError) {
                                    //jetsonToSend.setStatus(STATUS_ACTUALIZADO);
                                    jetsonToSend.setPingStatus(4);
                                    if (resultOperation != null && resultOperation.equals("0")) {
                                        jetsonToSend.setPingStatus(3);
                                    }
                                    equipoDAO.updateJetson(sessionCtEquipos, jetsonToSend);
                                    UtilityFile.createWriteDataFile(getEyesFileName(), descriptorProceso + "|" + prop.getHostName() + "|3|" + prop.getHostAddress() + "|000|END|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Se pidieron archivos de las siguientes tiendas: +listTiendasLoad.toString()+, se procesaron archivos de las siguientes tiendas: +listTiendasProces.toString()+.\n", true);
                                }
                            }
                        }
                    }
                    sendToRegionalServ = false; // servidor regional
                    connectionError = false;
                    log.info("Buscando JETSON para enviar reiniciar...");
                    jetsonSenderBuscar = equipoDAO.getJetsonSenderToRestart(sessionCtEquipos, STATUS_BUSCAR);
                    for (Jetson jetsonToSend : jetsonSenderBuscar) {
                        Equipo equipo = equipoDAO.findOnlineJetsonByIdEquipo(sessionEyes, jetsonToSend.getIdEquipo());
                        if (equipo != null) {
                            storeNumber = equipo.getIdLocal().toString();
                            Store storeToSend = storeDAO.getStoreByCode(sessionSaadmin, equipo.getIdLocal());
                            if (storeToSend != null) {
                                while (storeNumber.length() < 3) {
                                    storeNumber = "0" + storeNumber;
                                }
                                port = prop.getInt("clientSocket.port");
                                String ipServerHijo = hayServidorRegional(equipo.getIdLocal());
                                log.info("ip Server Hijo: " + ipServerHijo);

                                if (ipServerHijo != null) {
                                    String opXML = jetsonToString(jetsonToSend);
                                    port = prop.getInt("serverSocket.port");
                                    StringBuffer data = new StringBuffer();
                                    data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(ArmsServerConstants.Process.OPERATOR_JETSON_SENDER)
                                            .append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
                                            .append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(ArmsServerConstants.Communication.TEMP_CONN)
                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(opXML)
                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(storeToSend.getKey());
                                    log.info("XML: " + opXML);
                                    storeToSend.setIp(ipServerHijo);

                                    log.info("Ip: " + storeToSend.getIp() + ", port: " + port);
                                    connectionError = !sendToLocal(data, storeToSend.getIp());
                                    log.info("Respuesta Socket: " + connectionError);
                                    closeConnection();
                                    sendToRegionalServ = true;
                                }
                                if (!sendToRegionalServ && !connectionError) {
                                    port = prop.getInt("clientSocket.port");
                                    StringBuffer data = createData(jetsonToSend, "r");
                                    connectionError = !sendToLocal(data, equipo.getIp());
                                    log.info("Respuesta Socket: " + connectionError);
                                }
                                if (!connectionError) {
                                    //jetsonToSend.setStatus(STATUS_ACTUALIZADO);
                                    jetsonToSend.setStatusReiniciar(STATUS_ACTUALIZADO);
                                    equipoDAO.updateJetson(sessionCtEquipos, jetsonToSend);
                                    //opDao.updateOperatorStore(session, operadorTienda); listTiendasProces
                                    UtilityFile.createWriteDataFile(getEyesFileName(), descriptorProceso + "|" + prop.getHostName() + "|3|" + prop.getHostAddress() + "|000|END|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Se pidieron archivos de las siguientes tiendas: +listTiendasLoad.toString()+, se procesaron archivos de las siguientes tiendas: +listTiendasProces.toString()+.\n", true);
                                }
                            }
                        }

                    }
                    sendToRegionalServ = false; // servidor regional
                    connectionError = false;
                    log.info("Buscando JETSON para enviar apagar...");
                    jetsonSenderBuscar = equipoDAO.getJetsonSenderToApagar(sessionCtEquipos, STATUS_BUSCAR);
                    for (Jetson jetsonToSend : jetsonSenderBuscar) {
                        Equipo equipo = equipoDAO.findOnlineJetsonByIdEquipo(sessionEyes, jetsonToSend.getIdEquipo());
                        if (equipo != null) {
                            Store storeToSend = storeDAO.getStoreByCode(sessionSaadmin, equipo.getIdLocal());
                            if (storeToSend != null) {
                                storeNumber = equipo.getIdLocal().toString();
                                while (storeNumber.length() < 3) {
                                    storeNumber = "0" + storeNumber;
                                }
                                String ipServerHijo = hayServidorRegional(equipo.getIdLocal());
                                log.info("ip Server Hijo: " + ipServerHijo);
                                if (ipServerHijo != null) {
                                    String opXML = jetsonToString(jetsonToSend);
                                    port = prop.getInt("serverSocket.port");
                                    log.info("HAY SERVIDOR REGIONAL, MANDA A TIENDA: " + equipo.getIdLocal());
                                    if (sendFile(ipServerHijo, jetsonToSend, outFolder + File.separator + jetsonToSend.getCodigoVap().toString() + File.separator)) {
                                        log.info("Archivos enviados");
                                    }
                                    //enviar archivos
                                    StringBuffer data = new StringBuffer();
                                    data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(ArmsServerConstants.Process.OPERATOR_JETSON_SENDER)
                                            .append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
                                            .append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(ArmsServerConstants.Communication.TEMP_CONN)
                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(opXML)
                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                            .append(storeToSend.getKey());
                                    log.info("XML: " + opXML);
                                    storeToSend.setIp(ipServerHijo);

                                    log.info("Ip: " + storeToSend.getIp() + ", port: " + port);
                                    connectionError = !sendToLocal(data, storeToSend.getIp());
                                    log.info("Respuesta Socket: " + connectionError);
                                    closeConnection();
                                    sendToRegionalServ = true;

                                }

                                if (!sendToRegionalServ && !connectionError) {
                                    port = prop.getInt("clientSocket.port");
                                    StringBuffer data = createData(jetsonToSend, "q");
                                    connectionError = !sendToLocal(data, equipo.getIp());
                                    log.info("Respuesta Socket: " + connectionError);
                                }
                                if (!connectionError) {
                                    //jetsonToSend.setStatus(STATUS_ACTUALIZADO);
                                    jetsonToSend.setStatusApagar(STATUS_ACTUALIZADO);

                                    equipoDAO.updateJetson(sessionCtEquipos, jetsonToSend);
                                    //opDao.updateOperatorStore(session, operadorTienda); listTiendasProces
                                    UtilityFile.createWriteDataFile(getEyesFileName(), descriptorProceso + "|" + prop.getHostName() + "|3|" + prop.getHostAddress() + "|000|END|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Se pidieron archivos de las siguientes tiendas: +listTiendasLoad.toString()+, se procesaron archivos de las siguientes tiendas: +listTiendasProces.toString()+.\n", true);
                                }
                            }
                        }
                    }
                    sendToRegionalServ = false; // servidor regional
                    connectionError = false;
                    //validar por estado de tienda primero y salir si no hay para actualizar o para recibir.
//                jetsonSenderBuscar = equipoDAO.getJetsonSender(sessionCtEquipos, STATUS_BUSCAR);
//
//                //log.info("Tiendas para mandar trama y inicializar.");
//                if (jetsonSenderBuscar != null && !jetsonSenderBuscar.isEmpty()) {
//                    // validar si hay regional si hay va al regional sino al agente.
//                    log.info("hay " + jetsonSenderBuscar.size() + " jetson para enviar.");
//                    for (Jetson item : jetsonSenderBuscar) {
//                        Jetson jetsonToSend = item;
//                        //String statusIni = jetsonToSend.getStatus().toString();
//                        //jetsonToSend.setStatus(STATUS_EN_PROCESO);
//
//                        log.info("jetson:" + jetsonToSend.toString());
//                        Equipo equipo = equipoDAO.findOnlineJetsonByIdEquipo(sessionEyes, item.getIdEquipo());
//                        if (equipo != null) {
//                            Store storeToSend = storeDAO.getStoreByCode(sessionSaadmin, equipo.getIdLocal());
//                            if (storeToSend != null) {
//                                storeNumber = equipo.getIdLocal().toString();
//                                while (storeNumber.length() < 3) {
//                                    storeNumber = "0" + storeNumber;
//                                }
//
//                                String ipServerHijo = hayServidorRegional(equipo.getIdLocal());
//                                log.info("ip Server Hijo: " + ipServerHijo);
//                                if (ipServerHijo != null) {
//                                    String opXML = jetsonToString(item);
//                                    JetsonVersionAprendizaje aprendizaje = equipoDAO.getAprendizajes(sessionCtEquipos, item.getCodigoVap().longValue());
//                                    String vap = vapToString(aprendizaje);
//                                    String ent = entToString(equipoDAO.getEntrenamiento(sessionCtEquipos, aprendizaje.getCodigoEnt().intValue()));
//                                    port = prop.getInt("serverSocket.port");
//                                    storeToSend.setIp(ipServerHijo);
//                                    log.info("HAY SERVIDOR REGIONAL, MANDA A TIENDA: " + equipo.getIdLocal());
//
//                                    if (sendFile(ipServerHijo, jetsonToSend, outFolder + File.separator + jetsonToSend.getCodigoVap().toString() + File.separator)) {
//                                        log.info("Archivos enviados");
//                                    }
//                                    //enviar archivos
//                                    StringBuffer data = new StringBuffer();
//                                    data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
//                                            .append(ArmsServerConstants.Process.OPERATOR_JETSON_SENDER)
//                                            .append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
//                                            .append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
//                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
//                                            .append(ArmsServerConstants.Communication.TEMP_CONN)
//                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
//                                            .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
//                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
//                                            .append(opXML)
//                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
//                                            .append(storeToSend.getKey())
//                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
//                                            .append(vap)
//                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
//                                            .append(ent);
//                                    log.info("XML: " + opXML);
//
//                                    log.info("Ip: " + storeToSend.getIp() + ", port: " + port);
//                                    connectionError = !sendToLocal(data, storeToSend.getIp());
//                                    log.info("Respuesta Socket: " + connectionError);
//                                    closeConnection();
//                                    sendToRegionalServ = true;
//
//                                }
//
//                                if (!sendToRegionalServ && !connectionError) {
//                                    log.info("MANDA AL AGENTE: " + equipo.getIp());
//                                    // no hace falta mandar el xml con una trama comun creo que basta.
//                                    log.info("Carpeta: " + outFolder + File.separator + jetsonToSend.getCodigoVap().toString());
//                                    File carpeta = new File(outFolder + File.separator + jetsonToSend.getCodigoVap().toString());
//                                    String[] archivos = carpeta.list();
//                                    if (archivos != null) {
//                                        log.info("archivos:" + Arrays.toString(archivos));
//                                        port = prop.getInt("clientSocket.port");
//                                        log.info("Archivos, Ip: " + equipo.getIp() + ", port: " + port);
//
//                                        boolean subioArchivos = sendFile(equipo.getIp(), jetsonToSend, outFolderAgent + File.separator + jetsonToSend.getCodigoVap().toString() + File.separator);
//                                        if (subioArchivos) {
//                                            log.info("Operator Update Operation FINALIZO DE FORMA CORRECTA.");
//                                            log.info("Crea archivo crypto");
//                                            StringBuffer data = new StringBuffer();
//
//                                            String xml = jetsonToString(item);
//                                            data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
//                                                    .append(ArmsServerConstants.Process.PUBLISH_JETSON_OPERATION)
//                                                    .append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
//                                                    .append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
//                                                    .append(ArmsServerConstants.Communication.FRAME_SEP)
//                                                    .append(ArmsServerConstants.Communication.TEMP_CONN)
//                                                    .append(ArmsServerConstants.Communication.FRAME_SEP)
//                                                    .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
//                                                    .append(ArmsServerConstants.Communication.FRAME_SEP)
//                                                    .append("d")
//                                                    .append(ArmsServerConstants.Communication.FRAME_SEP)
//                                                    .append(xml);
//                                            //port = prop.getInt("clientSocket.port");
//                                            log.info("trama, Ip: " + equipo.getIp() + ", port: " + port);
//                                            connectionError = !sendToLocal(data, equipo.getIp());
//                                            log.info("Respuesta Socket: " + connectionError);
//                                            closeConnection();
//                                        } else {
//                                            log.info("NO PUDO SUBIR LOS ARCHIVOS.");
//                                            UtilityFile.createWriteDataFile(getEyesFileName(), descriptorProceso + "|" + prop.getHostName() + "|1|" + prop.getHostAddress() + "|" + storeNumber + "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error en el proceso de carga de Archivos.\n", true);
//                                        }
//                                    }
//                                }
//
//                                if (!connectionError) {
//                                    //jetsonToSend.setStatus(STATUS_ACTUALIZADO);
//                                    jetsonToSend.setStatus(STATUS_EN_PROCESO);
//                                    equipoDAO.updateJetson(sessionCtEquipos, jetsonToSend);
//                                    //opDao.updateOperatorStore(session, operadorTienda); listTiendasProces
//                                    UtilityFile.createWriteDataFile(getEyesFileName(), descriptorProceso + "|" + prop.getHostName() + "|3|" + prop.getHostAddress() + "|000|END|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Se pidieron archivos de las siguientes tiendas: +listTiendasLoad.toString()+, se procesaron archivos de las siguientes tiendas: +listTiendasProces.toString()+.\n", true);
//                                }
//
//                            } else {
//                                log.info("No existe la tienda con codigo: " + equipo.getIdLocal());
//                            }
//                        } else {
//                            log.info("File process No existe el equipo en la tabla fm_equipo, con idEquipo: " + item.getIdEquipo());
//
//                        }
//
//                    }
//
//                }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    try {
                        UtilityFile.createWriteDataFile(getEyesFileName(), "descriptorProceso|" + prop.getHostName() + "|3|" + prop.getHostAddress() + "|000|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error al correr el proceso " + descriptorProceso + ".\n", true);
                    } catch (Exception e1) {
                        log.error(e1.getMessage(), e1);
                    }
                } finally {
                    if (sessionCtEquipos != null || sessionSaadmin != null || sessionEyes != null) {
                        sessionCtEquipos.close();
                        sessionCtEquipos = null;
                        sessionSaadmin.close();
                        sessionSaadmin = null;
                        sessionEyes.close();
                        sessionEyes = null;
                    }
                }
            } else {
                log.info("Jetson Sender Process no iniciada. No es servidor principal");
            }
            try {
                Thread.sleep(prop.getLong("jetsonSend.server.timeSleep")); //definir una propiedad
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

        }
        finished = true;
    }

    private StringBuffer createData(Jetson jetsonToSend, String requerimiento) {
        StringBuffer data = new StringBuffer();
        data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
                .append(ArmsServerConstants.Process.PUBLISH_JETSON_OPERATION)
                .append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
                .append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
                .append(ArmsServerConstants.Communication.FRAME_SEP)
                .append(ArmsServerConstants.Communication.TEMP_CONN)
                .append(ArmsServerConstants.Communication.FRAME_SEP)
                .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
                .append(ArmsServerConstants.Communication.FRAME_SEP)
                .append(requerimiento)
                .append(ArmsServerConstants.Communication.FRAME_SEP)
                .append(jetsonToSend.getIpEqu())
                .append(ArmsServerConstants.Communication.FRAME_SEP)
                .append(jetsonToSend.getMacEqu());

        return data;
    }

    @Override
    public boolean shutdown(long timeToWait) {
        isEnd = true;
        //closeConnection();
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

    private boolean sendFile(String ip, Jetson jetsonToSend, String filePath) {

        // no hace falta mandar el xml con una trama comun creo que basta.
        log.info("Carpeta: " + outFolder + File.separator + jetsonToSend.getCodigoVap().toString());
        log.info("Carpeta destino: " + filePath);
        File carpeta = new File(outFolder + File.separator + jetsonToSend.getCodigoVap().toString());
        String[] archivos = carpeta.list();
        if (archivos != null) {
            log.info("archivos:" + Arrays.toString(archivos));
            log.info("Archivos, Ip: " + ip + ", port: " + port);

            boolean subioArchivos = false;

            for (String name : archivos) {
                File archivo = new File(outFolder + File.separator + jetsonToSend.getCodigoVap().toString() + File.separator + name);
                log.info("Archivo:" + archivo);
                subioArchivos = fileSender(archivo, ip, filePath);
            }
            return subioArchivos;
        } else {
            log.info("No se encontraron los archivos para subir.");
            UtilityFile.createWriteDataFile(getEyesFileName(), descriptorProceso + "|" + prop.getHostName() + "|1|" + prop.getHostAddress() + "|" + storeNumber + "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error en el proceso de carga de operadores.\n", true);
        }
        return true;
    }

    private boolean fileSender(File fileToSend, String tienda, String filePath) {
        String filename = fileToSend.getName();
        if (filename != null) {

            log.info("Archivo a enviar: " + filename);
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
                    UtilityFile.createWriteDataFile(getEyesFileName(),
                            descriptorProceso + "|" + prop.getHostName() + "|3|"
                            + prop.getHostAddress() + "|" + storeNumber + "|PRC|"
                            + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
                            + "|Archivo enviado: " + filename + ".\n",
                            true);
                    closeClient();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                    return true;
                } else {
                    log.error("Error al enviar el archivo.");
                    UtilityFile.createWriteDataFile(getEyesFileName(),
                            descriptorProceso + "|" + prop.getHostName() + "|3|"
                            + prop.getHostAddress() + "|" + storeNumber + "|ERR|"
                            + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
                            + "|Error al enviar el archivo: " + filename + ".\n",
                            true);
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

    protected void closeClient() {
        if (socketClient != null) {
            socketClient.closeConnection();
        }
    }

    protected boolean sendFrameToLocal(Frame frame, String tienda) {
        String str;
        List list;
        Frame frameRpta;
        resultOperation = null;
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
                            if (list.size() > 6) {
                                String get = list.get(6).toString();
                                if (get.equals("rs-s")) {
                                    resultOperation = list.get(7).toString();
                                }
                            }
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

    protected void closeConnection() {
        if (socketClient != null) {
            socketClient.closeConnection();
            socketClient = null;
        }
    }

    private String getEyesFileName() {
        return prop.getObject("eyes.ups.file.name") + "_" + ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
    }

    protected boolean sendFileHeader(Frame frame, String ip) {
        log.info("Enviando archivo  la ip " + ip);
        String str;
        List list;
        Frame frameRpta;
        int qtyBytesLength = prop.getInt("clientSocket.quantityBytesLength");
        try {

            if (socketClient == null || !socketClient.isConnected()) {
                connectClient(ip);
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

    private String jetsonToString(Jetson jetson) {
        XStream xstreamAux = new XStream();
        xstreamAux.alias("JetsonList", List.class);
        xstreamAux.alias("Jetson", Jetson.class);
        xstreamAux.aliasField("CodigoEqu", Jetson.class, "codigoEqu");
        xstreamAux.aliasField("DesEqu", Jetson.class, "desEqu");
        xstreamAux.aliasField("macEqu", Jetson.class, "macEqu");
        xstreamAux.aliasField("ipEqu", Jetson.class, "ipEqu");
        xstreamAux.aliasField("fecNew", Jetson.class, "fecNew");
        xstreamAux.aliasField("fecUpdate", Jetson.class, "fecUpdate");
        xstreamAux.aliasField("estadoActivo", Jetson.class, "estadoActivo");
        xstreamAux.aliasField("hsExtErrores", Jetson.class, "hsExtErrores");
        xstreamAux.aliasField("userFtp", Jetson.class, "userFtp");
        xstreamAux.aliasField("passFtp", Jetson.class, "passFtp");
        xstreamAux.aliasField("idEquipo", Jetson.class, "idEquipo");
        xstreamAux.aliasField("status", Jetson.class, "status");

        xstreamAux.aliasField("codigoVap", Jetson.class, "codigoVap");
        xstreamAux.aliasField("emtEstadoDesde", Jetson.class, "emtEstadoDesde");
        xstreamAux.aliasField("emtEstadoHasta", Jetson.class, "emtEstadoHasta");
        xstreamAux.aliasField("statusReiniciar", Jetson.class, "statusReiniciar");
        xstreamAux.aliasField("statusApagar", Jetson.class, "statusApagar");
        xstreamAux.aliasField("statusUpload", Jetson.class, "statusUpload");
        xstreamAux.aliasField("fecUpload", Jetson.class, "fecUpload");
        xstreamAux.aliasField("infoEqipo", Jetson.class, "infoEqipo");
        xstreamAux.aliasField("pingStatus", Jetson.class, "pingStatus");
        List<Jetson> lst = new LinkedList<>();
        lst.add(jetson);
        return xstreamAux.toXML(lst);
    }

    private String vapToString(JetsonVersionAprendizaje jetson) {
        log.info("Transformando jetson to xml " + jetson.getCodigoVap());
        XStream xstreamAux = new XStream();
        xstreamAux.alias("JetsonVersionAprendizaje", JetsonVersionAprendizaje.class);
        return xstreamAux.toXML(jetson);
    }

    private String entToString(JetsonEntrenamiento jetson) {
        log.info("Transformando jetson to xml " + jetson.getCodigoEnt());
        XStream xstreamAux = new XStream();
        xstreamAux.alias("JetsonEntrenamiento", JetsonEntrenamiento.class);
        return xstreamAux.toXML(jetson);
    }
}
