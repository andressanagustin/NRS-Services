/**
 *
 */
package com.allc.arms.server.processes.operator;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.operator.AuthorizesOPC;
import com.allc.arms.server.persistence.operator.Indicat;
import com.allc.arms.server.persistence.operator.IndicatOPC;
import com.allc.arms.server.persistence.operator.LevelAuthorizes;
import com.allc.arms.server.persistence.operator.Operator;
import com.allc.arms.server.persistence.operator.OperatorDAO;
import com.allc.arms.server.persistence.operator.OperatorStore;
import com.allc.arms.server.persistence.operator.OperatorWrapper;
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

/**
 * Proceso encargado de detectar un ABM de operador en la BD e informar a los
 * agentes correspondientes.
 *
 * @author GUSTAVOK
 * @modified AndresS
 */
public class OperatorUpdateProcess extends AbstractProcessPrincipal {

    protected static Logger log = Logger.getLogger(OperatorUpdateProcess.class);
    protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
    public boolean isEnd = false;
    public final Integer STATUS_INICIAL = new Integer(0);
    public final Integer STATUS_PROCESAR = new Integer(1);
    public final Integer STATUS_EN_PROCESO = new Integer(2);
    public final Integer STATUS_PROCESADO = new Integer(3);
    public final Integer STATUS_PROCESAR_SIN_PASSW = new Integer(4);
    public final Integer STATUS_PROCESAR_SOLO_REGIONAL = new Integer(5);
    public final Integer ERROR_CONEXION_TIENDA_ACTUAL = new Integer(100);
    public final Integer ERROR_CONEXION_TIENDA_ANTERIOR = new Integer(101);
    public final Integer SUBSCRIBE_BLOQUEADO = new Integer(0);
    public final Integer SUBSCRIBE_ACTIVO = new Integer(1);
    public final Integer SUBSCRIBE_ELIMINADO = new Integer(2);
    public final Integer DOWNLOAD_NOT = new Integer(0);
    public final Integer DOWNLOAD_START = new Integer(1);
    public final Integer DOWNLOAD_FINISH = new Integer(2);
    protected Session session = null;
    private Session sessionSaadmin = null;
    protected ConnSocketClient socketClient = null;
    protected boolean finished = false;
    protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);

    final static XStream xstream = new XStream() {
        protected MapperWrapper wrapMapper(final MapperWrapper next) {
            return new HibernateMapper(next);
        }
    };

    protected void iniciarSesion() {
        while (session == null && !isEnd) {
            try {
                session = HibernateSessionFactoryContainer.getSessionFactory("Opera").openSession();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            if (session == null) {
                try {
                    log.error("OCURRIÓ UN ERROR AL CREAR LA CONEXIÓN A LA BD, SE REINTENTARÁ EN 3 seg.");
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
            } catch (Exception e) {
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
        } catch (Exception e) {
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

    protected boolean connectClient(PropFile properties, Store tienda) {
        if (socketClient == null || !socketClient.isConnected()) {
            log.info("Store IP: " + tienda.getIp() + ", port: " + properties.getInt("serverSocket.port"));
            socketClient = new ConnSocketClient();
            socketClient.setIpServer(tienda.getIp());
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

    public void run() {
        //List list;
        Frame frame;
        xstream.registerConverter(new HibernateProxyConverter());
        xstream.registerConverter(new HibernatePersistentCollectionConverter(xstream.getMapper()));
        xstream.registerConverter(new HibernatePersistentMapConverter(xstream.getMapper()));
        xstream.registerConverter(new HibernatePersistentSortedMapConverter(xstream.getMapper()));
        xstream.registerConverter(new HibernatePersistentSortedSetConverter(xstream.getMapper()));
        xstream.setMode(XStream.NO_REFERENCES);
        while (!isEnd) {
            if (isPrincipal()) {
                log.info("Buscando actualizacion de operadores...");
                String store = properties.getObject("eyes.store.code");
                /**
                 * open a session *
                 */
                iniciarSesion();
                iniciarSaadminSesion();
                OperatorDAO opDao = new OperatorDAO();
                StoreDAO storeDAO = new StoreDAO();
                try {
                    // agregar que vuelva a procesar los que estan en proceso.
                    List operators = opDao.getOperatorsByStatus(session, STATUS_PROCESAR);
                    operators.addAll(opDao.getOperatorsByStatus(session, STATUS_PROCESAR_SIN_PASSW));
                    operators.addAll(opDao.getOperatorsByStatus(session, STATUS_EN_PROCESO));
                    operators.addAll(opDao.getOperatorsByStatus(session, STATUS_PROCESAR_SOLO_REGIONAL));
                    if (operators != null && !operators.isEmpty()) {
                        log.info("Se procesaran " + operators.size() + " operadores.");
                        Iterator itOperators = operators.iterator();
                        while (itOperators.hasNext()) {
                            Operator operador = null;
                            operador = (Operator) itOperators.next();
                            log.debug(operador.toString());
                            String statusIni = operador.getStatus().toString();
                            try {
                                UtilityFile.createWriteDataFile(getEyesFileName(), "OPER_UPD_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store + "|STR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Iniciando procesamiento del Operador: " + operador.getIdentityDocument() + ".\n", true);                         
    //                            operador.setStatus(STATUS_EN_PROCESO);
    //                            opDao.updateOperator(session, operador);
                                // seteamos si solo envia a regional y no al controladro
                                int sendControlador = Integer.valueOf(statusIni).equals(STATUS_PROCESAR_SOLO_REGIONAL) ? 0 : 1;
                                boolean sendToTienda = false; // servidor local
                                boolean sendToRegionalServ = false; // servidor regional

                                boolean errorOperaTienda = false;
                                boolean downloadedAll = true; //SI ALGUNO NO DESCARGA POR DOWNLOAD=0
                                boolean downloadedStore = false; //TIENE PARA PROCESAR EN AL MENOS UNA TIENDA

                                // nuevo tiendas => tabla intermedia 
                                Iterator itOpeTnd = operador.getTiendas().iterator();

                                // controlar por tienda, por problemas de conexion.	
                                while (itOpeTnd.hasNext()) {
                                    boolean connectionError = false;
                                    OperatorStore operadorTienda = null;
                                    operadorTienda = (OperatorStore) itOpeTnd.next();
                                    String statusIniTienda = operadorTienda.getStatus().toString();
                                    log.debug("Tienda " + operadorTienda.getCodTienda() + " ESTADO: " + operadorTienda.getStatus() + " DOWNLOAD: " + operadorTienda.getDownload() + " OPERADOR: " + operador.getOperadorId() + " TIPO MODELO: " + operadorTienda.getTipoModelo());

                                    //String subscribeTienda = operadorTienda.getSubscribe().toString();
                                    try {	   
                                            if (operadorTienda.getStatus().intValue() == STATUS_PROCESAR
                                                    || operadorTienda.getStatus().intValue() == STATUS_PROCESAR_SIN_PASSW
                                                    || operadorTienda.getStatus().intValue() == STATUS_PROCESAR_SOLO_REGIONAL
                                                    || operadorTienda.getStatus().intValue() == STATUS_EN_PROCESO) {
                                                    if (operadorTienda.getDownload() == null || operadorTienda.getDownload().intValue() != DOWNLOAD_START) { //NO MANDA A ACTUALIZAR
                                                            downloadedAll = false;
                                                                                    continue;
                                                                            }
                                                    log.info("Tienda " + operadorTienda.getCodTienda() + " ESTADO: " + operadorTienda.getStatus() + " DOWNLOAD: " + operadorTienda.getDownload() + " OPERADOR: " + operador.getOperadorId() + " TIPO MODELO: " + operadorTienda.getTipoModelo());
                                                    downloadedStore = true;
                                                    operador.setStatus(STATUS_EN_PROCESO);
                                                opDao.updateOperator(session, operador);
                                                    Store storeToSend = storeDAO.getStoreById(sessionSaadmin, Integer.valueOf(operadorTienda.getCodTienda()));

                                                // iniciamos variables de las tiendas
                                                operador.setCodTienda(Integer.valueOf(storeToSend.getKey().toString()));
                                                operador.setIpTienda(operadorTienda.getIpTienda());
                                                operador.setTipoModelo(operadorTienda.getTipoModelo());
                                                operador.setStatusTienda(operadorTienda.getStatus());
                                                operador.setSubscribeTienda(operadorTienda.getSubscribe());
                                                /*if (operador.getIdModOpera() == null || operador.getIdModOpera() <= 0) {
                                            operador.setAuthorizations(new ArrayList<>());
                                                }
                                                if (operador.getIdModOperaSO() == null || operador.getIdModOperaSO() <= 0) {
                                            operador.setLevelAuthorizations(new ArrayList<>());
                                                } */                                   

                                                operadorTienda.setStatus(STATUS_EN_PROCESO);
                                                opDao.updateOperatorStore(session, operadorTienda);

                                                // hay servidor local mandamos para agregar en el servidor local
                                                if (storeDAO.hayServidorLocal(sessionSaadmin, Integer.valueOf(storeToSend.getKey().toString()))) {
                                                    log.info("HAY SERVIDOR LOCAL " + storeToSend.getIp() + ", MANDA A TIENDA: " + operadorTienda.getCodTienda());
                                                    // VER --> AGREGAR ESTO connectClientIsUp
                                                    XStream xstreamAux = new XStream();
                                                    xstreamAux.alias("Operator", Operator.class);
                                                    xstreamAux.alias("AuthorizesOPC", AuthorizesOPC.class);
                                                    xstreamAux.alias("LevelAuthorizes", LevelAuthorizes.class);
                                                    xstreamAux.omitField(Operator.class, "tiendas");
                                                    xstreamAux.alias("IndicatOPC", IndicatOPC.class);
                                                    xstreamAux.alias("Indicat", Indicat.class);
                                                    xstreamAux.aliasField("OperadorId", Operator.class, "operadorId");
                                                    xstreamAux.aliasField("OptionsLevel", Operator.class, "optionsLevel");
                                                    xstreamAux.aliasField("Name", Operator.class, "name");
                                                    xstreamAux.aliasField("OperatorBirthDate", Operator.class, "operatorBirthDate");
                                                    xstreamAux.aliasField("Status", Operator.class, "status");
                                                    xstreamAux.aliasField("IndSegMej", Operator.class, "indSegMejorada");
                                                    xstreamAux.aliasField("Subscribe", Operator.class, "subscribe");
                                                    xstreamAux.aliasField("IdentityDocument", Operator.class, "identityDocument");
                                                    xstreamAux.aliasField("CodTienda", Operator.class, "CodTienda");
                                                    xstreamAux.aliasField("TipoModelo", Operator.class, "tipoModelo");
                                                    xstreamAux.aliasField("IpTienda", Operator.class, "IpTienda");
                                                    xstreamAux.aliasField("StatusTienda", Operator.class, "statusTienda");
                                                    xstreamAux.aliasField("SubscribeTienda", Operator.class, "subscribeTienda");
                                                    xstreamAux.aliasField("CodNegocio", Operator.class, "codNegocio");
                                                    xstreamAux.aliasField("Grupo", Operator.class, "grupo");
                                                    xstreamAux.aliasField("Uusuario", Operator.class, "usuario");
                                                    xstreamAux.aliasField("NivelAut", Operator.class, "nivelAut");
                                                    xstreamAux.aliasField("NivelAutSO", Operator.class, "nivelAutSO");
                                                    xstreamAux.aliasField("IdModOpera", Operator.class, "idModOpera");
                                                    xstreamAux.aliasField("IdModOperaSO", Operator.class, "idModOperaSO");
                                                    xstreamAux.aliasField("IndicatOPC", AuthorizesOPC.class, "indicatOPC");
                                                    xstreamAux.aliasField("Indicat", IndicatOPC.class, "indicat");
                                                    xstreamAux.addImplicitCollection(Operator.class, "levelAuthorizations", "LevelAuthorizes", LevelAuthorizes.class);
                                                    xstreamAux.addImplicitCollection(Operator.class, "authorizations", "AuthorizesOPC", AuthorizesOPC.class);
                                                    //seteamos el estado inicial para enviarlo
                                                    operador.setStatus(Integer.valueOf(statusIni));

                                                    String opXML = xstreamAux.toXML(operador);
                                                    //volvemos al estado real
                                                    operador.setStatus(STATUS_EN_PROCESO);

                                                    StringBuffer data = new StringBuffer();
                                                    data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
                                                            .append(ArmsServerConstants.Process.OPERATOR_DEALER)
                                                            .append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
                                                            .append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
                                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                                            .append(ArmsServerConstants.Communication.TEMP_CONN)
                                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                                            .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
                                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                                            .append(opXML);
                                                    //Store storeToSend = storeDAO.getStoreByCode(sessionSaadmin, Integer.valueOf(operadorTienda.getCodTienda()));
                                                    connectionError = !sendToLocal(data, storeToSend);
                                                    closeConnection();
                                                    sendToTienda = true;
                                                }

                                                // hay servidor regional mandamos para agregar en el servidor regional
                                                String ipServerHijo = hayServidorRegional(Integer.valueOf(storeToSend.getKey()));
                                                if (ipServerHijo != null) {
                                                    //VER --> AGREGAR ESTO connectClientIsUp
                                                    log.info("HAY SERVIDOR REGIONAL " + ipServerHijo + ", MANDA A TIENDA: " + operadorTienda.getCodTienda());

                                                    XStream xstreamAux = new XStream();
                                                    xstreamAux.alias("Operator", Operator.class);
                                                    xstreamAux.alias("AuthorizesOPC", AuthorizesOPC.class);
                                                    xstreamAux.alias("LevelAuthorizes", LevelAuthorizes.class);
                                                    xstreamAux.omitField(Operator.class, "tiendas");
                                                    xstreamAux.alias("IndicatOPC", IndicatOPC.class);
                                                    xstreamAux.alias("Indicat", Indicat.class);
                                                    xstreamAux.aliasField("OperadorId", Operator.class, "operadorId");
                                                    xstreamAux.aliasField("OptionsLevel", Operator.class, "optionsLevel");
                                                    xstreamAux.aliasField("Name", Operator.class, "name");
                                                    xstreamAux.aliasField("OperatorBirthDate", Operator.class, "operatorBirthDate");
                                                    xstreamAux.aliasField("Status", Operator.class, "status");
                                                    xstreamAux.aliasField("IndSegMej", Operator.class, "indSegMejorada");
                                                    xstreamAux.aliasField("Subscribe", Operator.class, "subscribe");
                                                    xstreamAux.aliasField("IdentityDocument", Operator.class, "identityDocument");
                                                    xstreamAux.aliasField("CodTienda", Operator.class, "CodTienda");
                                                    xstreamAux.aliasField("TipoModelo", Operator.class, "tipoModelo");
                                                    xstreamAux.aliasField("IpTienda", Operator.class, "IpTienda");
                                                    xstreamAux.aliasField("StatusTienda", Operator.class, "statusTienda");
                                                    xstreamAux.aliasField("SubscribeTienda", Operator.class, "subscribeTienda");
                                                    xstreamAux.aliasField("CodNegocio", Operator.class, "codNegocio");
                                                    xstreamAux.aliasField("Grupo", Operator.class, "grupo");
                                                    xstreamAux.aliasField("Uusuario", Operator.class, "usuario");
                                                    xstreamAux.aliasField("NivelAut", Operator.class, "nivelAut");
                                                    xstreamAux.aliasField("NivelAutSO", Operator.class, "nivelAutSO");
                                                    xstreamAux.aliasField("IdModOpera", Operator.class, "idModOpera");
                                                    xstreamAux.aliasField("IdModOperaSO", Operator.class, "idModOperaSO");
                                                    xstreamAux.aliasField("IndicatOPC", AuthorizesOPC.class, "indicatOPC");
                                                    xstreamAux.aliasField("Indicat", IndicatOPC.class, "indicat");
                                                    xstreamAux.addImplicitCollection(Operator.class, "levelAuthorizations", "LevelAuthorizes", LevelAuthorizes.class);
                                                    xstreamAux.addImplicitCollection(Operator.class, "authorizations", "AuthorizesOPC", AuthorizesOPC.class);

                                                    //seteamos el estado inicial para enviarlo
                                                    operador.setStatus(Integer.valueOf(statusIni));
                                                    String opXML = xstreamAux.toXML(operador);
                                                    //volvemos al estado real
                                                    operador.setStatus(STATUS_EN_PROCESO);

                                                    StringBuffer data = new StringBuffer();
                                                    data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
                                                            .append(ArmsServerConstants.Process.OPERATOR_DEALER)
                                                            .append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
                                                            .append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
                                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                                            .append(ArmsServerConstants.Communication.TEMP_CONN)
                                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                                            .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
                                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                                            .append(opXML)
                                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                                            .append(sendControlador);
                                                    //Store storeToSend = storeDAO.getStoreByCode(sessionSaadmin, Integer.valueOf(operadorTienda.getCodTienda()));
                                                    storeToSend.setIp(ipServerHijo);

                                                    log.info("Ip: " + storeToSend.getIp());
                                                    connectionError = !sendToLocal(data, storeToSend);
                                                    log.info("Respuesta Socket: " + connectionError);
                                                    closeConnection();
                                                    sendToRegionalServ = true;
                                                } else if (sendControlador == 0){
                                                    log.info("Esta en estado solo regional pero no encuentra regional.");
                                                    sendToRegionalServ = true; // LO DEJA COMO ENVIADO PARA QUE CAMBIE ES ESTADO
                                                }

                                                // sin servidor local --> tocar aca.
                                                OperatorWrapper operatorWrapper = new OperatorWrapper();
                                                if (!sendToTienda && !sendToRegionalServ && !connectionError && process(operador, operadorTienda, operatorWrapper)) {

                                                    // bloquear 
                                                    if (statusIniTienda.equals(STATUS_PROCESAR.toString()) && operadorTienda.getSubscribe().intValue() == SUBSCRIBE_BLOQUEADO) {
                                                        Random rnd = new Random();
                                                        Long randLong = new Long(rnd.nextLong());
                                                        randLong = randLong < 0 ? randLong * -1 : randLong;
                                                        String pass = (randLong).toString();
                                                        operatorWrapper.setPassword(pass.length() > 8 ? pass.substring(0, 8) : pass);
                                                        log.info("Password aleatoria: " + operatorWrapper.getPassword());
                                                    }

                                                    //log.info("Password aleatoria paso");
                                                    // seteo el estado inicial para que en el Agente se sepa si hay que modificar la password
                                                    operatorWrapper.setStatus(statusIniTienda);
                                                    xstream.alias("OperatorWrapper", OperatorWrapper.class);
                                                    String xml = xstream.toXML(operatorWrapper);
                                                    log.debug("xml: " + xml);

                                                    String today = ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date());
                                                    log.info("MANDA A CONTROLADOR " + operatorWrapper.getIpStore());
                                                    if (connectClientIsUp(operatorWrapper.getIpStore())) {
                                                            if (socketClient == null || !socketClient.isConnected()) {
                                                                connectionError = !connectClient(operatorWrapper.getIpStore());
                                                            }	
                                                    } else {
                                                            connectionError = true;
                                                    }

                                                    if (!sendToTienda && !connectionError) {
                                                            StringBuilder message = new StringBuilder();
                                                            List list = null;
                                                        message.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
                                                                .append(ArmsServerConstants.Process.OPERATOR_DEALER)
                                                                .append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
                                                                .append(ArmsServerConstants.Communication.FRAME_SEP).append(operadorTienda.getCodTienda())
                                                                .append(ArmsServerConstants.Communication.FRAME_SEP).append(ArmsServerConstants.Communication.TEMP_CONN)
                                                                .append(ArmsServerConstants.Communication.FRAME_SEP).append(today)
                                                                .append(ArmsServerConstants.Communication.FRAME_SEP).append(xml);
                                                        list = Arrays.asList(p.split(message.toString()));

                                                        frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
                                                                ArmsServerConstants.Communication.FRAME_SEP);

                                                        if (frame.loadData()) {
                                                            connectionError = !sendOperatorUpdate(operatorWrapper.getIpStore(), frame);
                                                            closeConnection();
                                                        }
                                                    }
                                                } else {
                                                    UtilityFile.createWriteDataFile(getEyesFileName(), "OPER_UPD_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store + "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error al procesar el Operador: " + operador.getIdentityDocument() + ".\n", true);
                                                }

                                                if (!connectionError) {
                                                    operadorTienda.setStatus(STATUS_PROCESADO);
                                                    operadorTienda.setDownload(DOWNLOAD_NOT);
                                                    opDao.updateOperatorStore(session, operadorTienda);
                                                    log.info("Operador tienda: " + operadorTienda.getOperadorId() + ", actualizado.");
                                                    UtilityFile.createWriteDataFile(getEyesFileName(), "OPER_UPD_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store + "|END|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Operador: " + operador.getIdentityDocument() + " procesado.\n", true);
                                                } else {
                                                    errorOperaTienda = true;
                                                    //operadorTienda.setStatus(Integer.valueOf(statusIniTienda));
                                                    //este volver a status inicial. statusIniTienda
                                                    operadorTienda.setStatus(Integer.parseInt(statusIniTienda));
                                                    //operadorTienda.setStatus(STATUS_PROCESAR);
                                                    opDao.updateOperatorStore(session, operadorTienda);
                                                    log.info("Error de conexión con la tienda, se vuelve el opetnd al estado: " + statusIniTienda);
                                                    UtilityFile.createWriteDataFile(getEyesFileName(), "OPER_UPD_PT|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store + "|WAR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|No se pudo informar el Operador: " + operador.getIdentityDocument() + ".\n", true);
                                                }
                                            } // fin STATUS TIENDA
                                    }catch (Exception e) {
                                        log.error(e.getMessage(), e);
                                        try {
                                            UtilityFile.createWriteDataFile(getEyesFileName(), "OPER_UPD_PT|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store + "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|No se pudo informar el Operador: " + operador.getIdentityDocument() + ".\n", true);
                                        } catch (Exception e1) {
                                            log.error(e1.getMessage(), e1);
                                        }
                                        errorOperaTienda = true;
                                        operadorTienda.setStatus(Integer.parseInt(statusIniTienda));
                                        opDao.updateOperatorStore(session, operadorTienda);
                                        log.error("Error de conexión con la tienda, se vuelve el opetnd al estado: " + statusIniTienda);

                                    }
                                } // fin while tienda

                                //if (!connectionError && !errorOperaTienda) {
                                boolean moreDownload = opDao.getOperatorStoreToDownload(session, operador.getOperadorId(), 1);
                                    if (!downloadedAll) { 
    //                            	 operador.setStatus(Integer.parseInt(statusIni));
    //                            	 opDao.updateOperator(session, operador);
                                     if (downloadedStore) {
                                             operador.setStatus(Integer.parseInt(statusIni));
                                             //VER --> SI NO DESCARGA TODAS PORQUE HAY ALGUNA TIENDA EN DOWNLOAD=0 PERO DESCARGA AL MENOS 1, 
                                             // CONSULTAR SI HAY TIENDA CON DOWNLOAD=1 Y SI HAY DEJAR OPERADOR CON DOWNLOAD=1
                                             if (!moreDownload) {
                                                     operador.setDownload(DOWNLOAD_NOT);
                                             }
                                             opDao.updateOperator(session, operador);
                                             log.info("No descarga para todas las tiendas, se vuelve el operador al estado: " + statusIni);	
                                     }
                                     UtilityFile.createWriteDataFile(getEyesFileName(), "OPER_UPD_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store + "|WAR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|No se pudo informar el Operador: " + operador.getIdentityDocument() + " por no confirmar los cambios.\n", true);
                                } else if (!errorOperaTienda) {
                                            //VER --> SI DESCARGO TODAS CONSULTAR SI HAY TIENDA CON DOWNLOAD=1 Y SI HAY DEJAR OPERADOR CON DOWNLOAD=1
                                            // Y QUE SIGA CON LO DE ABAJO
                                            if (!moreDownload) {
                                                             operador.setDownload(DOWNLOAD_NOT);
                                            }
                                            // ANTES DE ACTUALIZAR EL OPERADOR VUELVE A CONSULTAR LAS TIENDAS PARA VER QUE NO TENGA OTRA TIENA A PROCESAR
                                            boolean moreProcesses = opDao.getOperatorStoreToProcesses(session, operador.getOperadorId());
                                            if (moreProcesses) {
                                                    operador.setStatus(Integer.parseInt(statusIni));
                                            opDao.updateOperator(session, operador);
                                            log.info("Operador: " + operador.getIdentityDocument() + " tiene mas tiendas a procesar, vuelve al estado " + statusIni);	
                                                                            }
                                            else {
                                                    operador.setStatus(STATUS_PROCESADO);
                                            opDao.updateOperator(session, operador);
                                            log.info("Operador: " + operador.getIdentityDocument() + ", actualizado.");	
                                            }
                                        UtilityFile.createWriteDataFile(getEyesFileName(), "OPER_UPD_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store + "|END|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Operador: " + operador.getIdentityDocument() + " procesado.\n", true);
                                } else {
                                    //operador.setStatus(Integer.valueOf(statusIni)); ver esto!
                                    operador.setStatus(Integer.parseInt(statusIni));
                                    //operador.setStatus(STATUS_PROCESAR);
                                    opDao.updateOperator(session, operador);
                                    log.info("Error de conexión, se vuelve el operador al estado: " + statusIni);
                                    UtilityFile.createWriteDataFile(getEyesFileName(), "OPER_UPD_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store + "|WAR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|No se pudo informar el Operador: " + operador.getIdentityDocument() + ".\n", true);
                                }
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                try {
                                    UtilityFile.createWriteDataFile(getEyesFileName(), "OPER_UPD_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store + "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error al procesar el Operador: " + operador.getIdentityDocument() + ".\n", true);
                                } catch (Exception e1) {
                                    log.error(e1.getMessage(), e1);
                                }
                                operador.setStatus(Integer.parseInt(statusIni));
                                opDao.updateOperator(session, operador);
                                log.error("Error de conexión, se vuelve el operador al estado: " + statusIni);
                            }
                        }
                    } else {
                        log.info("No hay operadores a procesar.");
                    }
                    session.close();
                    session = null;
                    sessionSaadmin.close();
                    sessionSaadmin = null;

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    try {
                        session.close();
                    } catch (Exception ex) {
                        log.error(e.getMessage(), e);
                    }
                    session = null;
                }
                try {
                    Thread.sleep(properties.getLong("operator.timesleep"));
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        } // finish while
        finished = true;
    }

    private String getEyesFileName() {
        return properties.getObject("eyes.ups.file.name") + "_" + ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
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
            if (socketClient == null || !socketClient.isConnected()) {
                connectClient(properties, tienda);
            }
            String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
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
                        String mje = Util.addLengthStartOfString("", qtyBytesLength).toString();
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

    protected boolean sendOperatorUpdate(String ip, Frame frame) {
        String str;
        List list;
        Frame frameRpta;
        int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
        try {
            if (socketClient == null || !socketClient.isConnected()) {
                connectClient(ip);
            }
            String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
            log.info("Trama a enviar: " + trama);
            if (socketClient.writeDataSocket(trama)) {
                int numberOfBytes = 0;
                int timeOutCycles = 0;
                while (numberOfBytes == 0) {
                    numberOfBytes = socketClient.readLengthDataSocket();
                    if (timeOutCycles == 5) {
                        // cada 5 timeouts escribimos una trama vacía para asegurarnos que el socket esté activo
                        String mje = Util.addLengthStartOfString("", qtyBytesLength).toString();
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
        log.info("Deteniendo OperatorDiscoverProcess...");
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

    public static Boolean process(Operator operator, OperatorStore tienda, OperatorWrapper operatorWrapper) {
        Boolean result = Boolean.FALSE;
        try {
        	
            operatorWrapper.setIpStore(tienda.getIpTienda());
            operatorWrapper.setTipoModelo(tienda.getTipoModelo());
            operatorWrapper.setName(operator.getNameAce());
            //log.info("operatorWrapper.setName: " + operatorWrapper.getName());
            operatorWrapper.setOperatorId(operator.getOperadorId().toString());
            operatorWrapper.setOperatorBirthDate(operator.getOperatorBirthDate());
            operatorWrapper.setStatus(tienda.getStatus().toString()); //tienda
            operatorWrapper.setIndSegMejorada(operator.getIndSegMejorada() == null ? 0 : operator.getIndSegMejorada());
            operatorWrapper.setSubscribe(tienda.getSubscribe().toString()); //tienda
            // si es modelo de Sistema Operativo cambiamos el identity por el nombre, que es el id
        	operatorWrapper.setIdentityDocument(operator.getIdentityDocument());	
            
            List<StringBuffer> list = new ArrayList<StringBuffer>();
            /**
             * Indicat0 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 16,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat1 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 16,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat2 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat3 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat4 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat5 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat6 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat7 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat8 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat9 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat10 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat11 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat12 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat13 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat14 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat15 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat16 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat17 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat18 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));
            /**
             * Indicat19 *
             */
            list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
                    ArmsServerConstants.Communication.CERO)));

            /**
             * set the indicats *
             */
            //List<AuthorizesOPC> listIndicat = operator.getAuthorizations();
            //log.info("Lista de indicat: " + listIndicat.toString());
            for (int i = 0; i < operator.getAuthorizations().size(); i++) {
                /**
                 * by default the "bits" are off, then only set on the IndActivo
                 * = 1 *
                 */
                if (((AuthorizesOPC) operator.getAuthorizations().get(i)).getValue().toString().equals("1")) {
                    int pos = ((AuthorizesOPC) operator.getAuthorizations().get(i)).getIndicatOPC().getBitPos();
                    int numIndicat = ((AuthorizesOPC) operator.getAuthorizations().get(i)).getIndicatOPC().getIndicat()
                            .getIndicat();
                    StringBuffer indicat = list.get(numIndicat);
                    setIndicat(indicat, pos, "1");
                    list.set(numIndicat, indicat);
                }
            }

            operatorWrapper.setIndicats(list);

            StringBuffer levelAut = new StringBuffer();
            int size = operator.getLevelAuthorizations().size();
            Collections.sort(operator.getLevelAuthorizations(), new Comparator() {
                public int compare(Object o1, Object o2) {
                    return Integer.valueOf(((LevelAuthorizes) o1).getIdNvautoriza()).compareTo(Integer.valueOf(((LevelAuthorizes) o2).getIdNvautoriza()));
                }
            });
            for (int i = 0; i < size; i++) {
                levelAut.append(((LevelAuthorizes) operator.getLevelAuthorizations().get(i)).getValue());
            }
            if (operator.getGrupo() != null && !operator.getGrupo().toString().isEmpty()) {
                String grupo = operator.getGrupo().toString();
                while (grupo.length() < 3) {
                    grupo = "0" + grupo;
                }
                levelAut.append(grupo);
            }
            if (operator.getUsuario() != null && !operator.getUsuario().toString().isEmpty()) {
                String usuario = operator.getUsuario().toString();
                while (usuario.length() < 3) {
                    usuario = "0" + usuario;
                }
                levelAut.append(usuario);
            }
            operatorWrapper.setLevelAuthorizations(levelAut.toString());
            if (operator.getNivelAut() != null) {
                operatorWrapper.setNivelAut(operator.getNivelAut().toString());
            }
            if (operator.getNivelAutSO() != null) {
                operatorWrapper.setNivelAutSO(operator.getNivelAutSO().toString());
            }
            result = Boolean.TRUE;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    protected static void setIndicat(StringBuffer str, int pos, String value) {
        try {
            str.replace(pos, pos + 1, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

}
