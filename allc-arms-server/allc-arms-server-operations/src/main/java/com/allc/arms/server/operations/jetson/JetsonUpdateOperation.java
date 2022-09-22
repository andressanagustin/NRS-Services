package com.allc.arms.server.operations.jetson;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.equipo.Equipo;
import com.allc.arms.server.persistence.equipo.EquipoDAO;
import com.allc.arms.server.persistence.equipo.Jetson;
import com.allc.arms.server.persistence.equipo.JetsonUploadFile;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.thoughtworks.xstream.XStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;

public class JetsonUpdateOperation extends AbstractOperation {

    private static Logger log = Logger.getLogger(JetsonUpdateOperation.class);

    public final Integer STATUS_INICIAL = 0;
    public final Integer STATUS_BUSCAR = 1;
    public final Integer STATUS_EN_PROCESO = 2;
    public final Integer STATUS_ACTUALIZADO = 3;
    public final Integer STATUS_BUSCAR_MASIVO = 4;
    private Session sessionCtEquipos = null;
    private Session sessionEyes = null;

    private EquipoDAO equipoDAO;

    private String descriptorOperation = "JT_UPD_O";

    private String storeCodeLocal;
    private String storeCodeRegional;
    private String ipCentral;
    private int port;

    String storeNumber;

    private PropFile properties;
    private ConnSocketClient socketClient = null;
    private final Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);

    @Override
    public boolean shutdown(long timeToWait) {
        // TODO Auto-generated method stub
        return false;
    }

    private void iniciarCtEquiposSesion() {
        while (sessionCtEquipos == null) {
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

    private void iniciarSesionEyes() {
        while (sessionEyes == null) {
            try {
                sessionEyes = HibernateSessionFactoryContainer.getSessionFactory("Eyes").openSession();
            } catch (HibernateException e) {
                log.error(e.getMessage(), e);
            }
            if (sessionEyes == null) {
                try {
                    log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
        log.info("Inicio: " + descriptorOperation);
        this.properties = properties;
        String msg = "1";
        try {

            UtilityFile.createWriteDataFile(getEyesFileName(properties),
                    descriptorOperation + "|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
                    + frame.getHeader().get(3) + "|STR|"
                    + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
                    + "|Iniciando recepción de estado jetson.\n",
                    true);
            iniciarCtEquiposSesion();
            iniciarSesionEyes();
            equipoDAO = new EquipoDAO();
            String requerimiento = ((String) frame.getBody().get(0)).toLowerCase();
            Jetson jetsonRecive;
            String mac;
            log.info("Requerimiento -- " + requerimiento);
            switch (requerimiento) {
                case "d": {
                    String macJetson = (String) frame.getBody().get(1);
                    String ipJetson = (String) frame.getBody().get(2);
                    log.info("macJetson: " + macJetson + "ipJetson: " + ipJetson);
                    if (macJetson != null && ipJetson != null) {
                        jetsonRecive = equipoDAO.getJetsonXmac(sessionCtEquipos, macJetson);
                        if (jetsonRecive != null && (jetsonRecive.getStatus().equals(STATUS_EN_PROCESO) || jetsonRecive.getStatus().equals(STATUS_BUSCAR_MASIVO))) {
                            jetsonRecive.setStatus(STATUS_ACTUALIZADO);
                            jetsonRecive.setFecUpload(new Date());
                            equipoDAO.updateJetson(sessionCtEquipos, jetsonRecive);
                        }
                    }
                    msg = "0";
                    UtilityFile.createWriteDataFile(getEyesFileName(properties), descriptorOperation + "|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + frame.getHeader().get(3) + "|END|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Finalizo proceso correctamente", true);
                    break;
                }
                case "u":
                    String option = (String) frame.getBody().get(1);
                    log.info("Opcion --- " + option);
//                    if (option.equals("1")) {
//                        Integer idEquipo = Integer.parseInt(frame.getBody().get(2).toString());
//                        JetsonUploadFile entrenamiento = equipoDAO.insertJetsonUploadFile(sessionCtEquipos, idEquipo);
//                        if (entrenamiento != null) {
//                            msg = "u-1".concat(ArmsServerConstants.Communication.FRAME_SEP)
//                                    .concat(entrenamiento.getIdFileEquipo().toString())
//                                    .concat(ArmsServerConstants.Communication.FRAME_SEP)
//                                    .concat("0");
//                        }
//                    } else 
                    if (option.equals("2")) {
                        log.info("Inicia proceso 2 de operacion");
                        //Files.zippearDirectorio(outFolderServer + File.separator + frame.getBody().get(2).toString(), outFolderServer + File.separator + frame.getBody().get(2).toString());
                        String macJetson = (String) frame.getBody().get(2);
                        Jetson jet = equipoDAO.getJetsonXmac(sessionCtEquipos, macJetson);
                        jet.setStatusUploadUp(1);
                        jet.setFecUpload(new Date());
                        jet.setFecUpdate(new Date());
                        jet.setFileUploadUp(frame.getBody().get(3).toString());
                        equipoDAO.updateJetson(sessionCtEquipos, jet);
                        log.info("Inicia actualiza jetson");
                        equipoDAO.insertJetsonUploadFile(sessionCtEquipos, jet.getCodigoEqu(), frame.getBody().get(3).toString());
                        msg = "0";
                    }
                    break;
                case "o": {
                    String macJetson = (String) frame.getBody().get(1);
                    Jetson jetsonXmac = equipoDAO.getJetsonXmac(sessionCtEquipos, macJetson);
                    XStream xstreamAux = new XStream();
                    xstreamAux.alias("Jetson", Jetson.class);
                    String opXML = xstreamAux.toXML(jetsonXmac);
                    String result = jetsonXmac == null ? "0" : "1";
                    msg = "o-1".concat(ArmsServerConstants.Communication.FRAME_SEP)
                            .concat(result)
                            .concat(ArmsServerConstants.Communication.FRAME_SEP)
                            .concat((String) frame.getBody().get(2))
                            .concat(ArmsServerConstants.Communication.FRAME_SEP)
                            .concat(opXML)
                            .concat(ArmsServerConstants.Communication.FRAME_SEP)
                            .concat("0");
                    break;
                }
                case "n":
                    String nombre = (String) frame.getBody().get(1);
                    mac = (String) frame.getBody().get(2);
                    String ip = (String) frame.getBody().get(3);
                    Integer store = Integer.parseInt(frame.getBody().get(4).toString());
                    jetsonRecive = equipoDAO.getJetsonXmac(sessionCtEquipos, mac);
                    if (jetsonRecive == null) {
                        Equipo equipo = equipoDAO.findOnlineJetsonByIdStore(sessionEyes, store);
                        jetsonRecive = new Jetson();
                        jetsonRecive.setDesEqu(nombre);
                        jetsonRecive.setMacEqu(mac);
                        jetsonRecive.setIpEqu(ip);
                        jetsonRecive.setStatus(0);
                        jetsonRecive.setEstadoActivo(1);
                        jetsonRecive.setFecNew(new Date());
                        jetsonRecive.setFecUpdate(new Date());
                        jetsonRecive.setIdEquipo(equipo.getIdEquipo());
                        jetsonRecive.setUserFtp("nuo");
                        jetsonRecive.setPassFtp("nuo123");
                        jetsonRecive.setIdLocal(store);
                        jetsonRecive.setStatusRegistrarUp(STATUS_BUSCAR);
                        equipoDAO.updateJetson(sessionCtEquipos, jetsonRecive);
                        msg = "0";
                    }
                    break;
                case "s":
                    mac = (String) frame.getBody().get(1);
                    String info = (String) frame.getBody().get(2);
                    jetsonRecive = equipoDAO.getJetsonXmac(sessionCtEquipos, mac);
                    if (jetsonRecive != null) {
                        jetsonRecive.setInfoEqipo(info);
                        jetsonRecive.setPingStatusUp(STATUS_BUSCAR);
                        equipoDAO.updateJetson(sessionCtEquipos, jetsonRecive);
                        msg = "0";
                    }
                    break;
                default:
                    log.info("Operacion no sopertada");
                    break;
            }
            String sb = frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + msg;
            log.info("Trama para responder --- " + sb);
            socket.writeDataSocket(Util.addLengthStartOfString(sb, properties.getInt("serverSocket.quantityBytesLength")));
//            storeCodeLocal = properties.getObject("eyes.store.code");
//                // si es dintinto de 000 es un servidor regional
//                storeCodeRegional = properties.getObject("eyes.store.code.group");
//                if (Integer.parseInt(storeCodeLocal) == 0 && Integer.parseInt(storeCodeRegional) != 0) {
//                    ipCentral = properties.getObject("clientSocket.ipCentral");
//                    port = properties.getInt("serverSocket.port");
//                    log.info("Es servidor regional");
//                    if (!sendFrameToLocal(frame, ipCentral)) {
//                        log.error("No se pudo enviar al central");
//                    }
//                    closeConnection();
//                }
            try {
                sleep(500);
            } catch (InterruptedException e) {
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            try {
                String sb = frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + "1";
                log.info("Trama para responder --- " + sb);
                socket.writeDataSocket(Util.addLengthStartOfString(sb, properties.getInt("serverSocket.quantityBytesLength")));
                UtilityFile.createWriteDataFile(getEyesFileName(properties),
                        descriptorOperation + "|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
                        + frame.getHeader().get(3) + "|ERR|"
                        + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
                        + "|Error al recibir el estado jetson.\n",
                        true);
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
            }
            return false;
        } finally {
            if (sessionCtEquipos != null || sessionEyes != null) {
                sessionCtEquipos.close();
                sessionCtEquipos = null;
                sessionEyes.close();
                sessionEyes = null;
            }
        }

        //respondemos trama de confirmacion
        return true;
    }

    private String getEyesFileName(PropFile properties) {
        return properties.getObject("eyes.ups.file.name") + "_" + ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
    }

    @Override
    public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean sendFrameToLocal(Frame frame, String ip) {
        if (frame.loadData()) {
            String str;
            List list;
            Frame frameRpta;
            int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
            try {
                if (socketClient == null || !socketClient.isConnected()) {
                    connectClient(ip);
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
        }
        return false;
    }

    private boolean connectClient(String ip) {
        if (socketClient == null || !socketClient.isConnected()) {
            log.info("Store IP: " + ip + ", port: " + port);
            socketClient = new ConnSocketClient();
            socketClient.setIpServer(ip);
            socketClient.setPortServer(port);
            //socketClient.setPortServer(properties.getInt("serverSocket.port"));
            socketClient.setRetries(2);
            socketClient.setTimeOutConnection(properties.getInt("serverSocket.timeOutConnection"));
            //socketClient.setTimeOutConnection(300000);
            socketClient.setTimeOutSleep(properties.getInt("serverSocket.timeOutSleep"));
            socketClient.setQuantityBytesLength(properties.getInt("serverSocket.quantityBytesLength"));
        }
        return socketClient.connectSocketUsingRetries();
    }

    private void closeConnection() {
        if (socketClient != null) {
            socketClient.closeConnection();
            socketClient = null;
        }
    }

}
