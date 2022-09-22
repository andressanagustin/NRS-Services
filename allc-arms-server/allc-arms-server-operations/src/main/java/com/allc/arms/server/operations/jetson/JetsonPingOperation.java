/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.operations.jetson;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.equipo.Equipo;
import com.allc.arms.server.persistence.equipo.EquipoDAO;
import com.allc.arms.server.persistence.equipo.Jetson;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.apache.log4j.Logger;

/**
 *
 * @author Tyrone Lopez
 */
public class JetsonPingOperation extends AbstractOperation {

    private final static Logger LOGGER = Logger.getLogger(JetsonPingOperation.class);

    private final String descriptorOperation = "JT_PNG_O";

    private Session sessionCtEquipos = null;

    private Session sessionEyes = null;

    private EquipoDAO equipoDAO;

    private String storeCodeLocal;
    private String storeCodeRegional;
    private String ipCentral;
    private int port;

    private PropFile properties;
    private ConnSocketClient socketClient = null;
    private final Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);

    private void iniciarCtEquiposSesion() {
        while (sessionCtEquipos == null) {
            try {
                sessionCtEquipos = HibernateSessionFactoryContainer.getSessionFactory("EquiposJ").openSession();
            } catch (HibernateException e) {
                LOGGER.error(e.getMessage(), e);
            }
            if (sessionCtEquipos == null) {
                try {
                    LOGGER.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    sleep(3000);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    private void iniciarSesionEyes() {
        while (sessionEyes == null) {
            try {
                sessionEyes = HibernateSessionFactoryContainer.getSessionFactory("Eyes").openSession();
            } catch (HibernateException e) {
                LOGGER.error(e.getMessage(), e);
            }
            if (sessionEyes == null) {
                try {
                    LOGGER.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public boolean shutdown(long timeToWait) {
        return false;
    }

    @Override
    public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
        this.properties = properties;
        LOGGER.info("Inicia operacion " + descriptorOperation);
        iniciarCtEquiposSesion();
        iniciarSesionEyes();
        equipoDAO = new EquipoDAO();
        String requermiento = (String) frame.getBody().get(0);
        System.out.println("requermiento " + requermiento);
        if (requermiento.equals("1")) {
            try {
                String store = (String) frame.getBody().get(1);
                Equipo equipo = equipoDAO.findOnlineJetsonByIdStore(sessionEyes, Integer.parseInt(store));
                StringBuilder sb = new StringBuilder();
                sb.append(frame.getHeaderStr()).append(ArmsServerConstants.Communication.FRAME_SEP);
                if (equipo != null) {
                    List<Jetson> jetsonXIdEquipo = equipoDAO.getJetsonXIdEquipo(sessionCtEquipos, equipo.getIdEquipo());
                    if (jetsonXIdEquipo != null) {
                        jetsonXIdEquipo.forEach((jetson) -> {
                            sb.append(jetson.getMacEqu()).append("/").append(jetson.getIpEqu()).append("#");
                        });
                        jetsonXIdEquipo.forEach((jetson) -> {
                            try {
                                jetson.setInfoEqipo(null);
                                equipoDAO.updateJetson(sessionCtEquipos, jetson);
                            } catch (Exception e) {
                                LOGGER.error("Error al actualizar el jetson " + jetson.getMacEqu());
                            }
                        });
                    }
                } else {
                    LOGGER.error("No existe el equipo");
                }
                sb.append(ArmsServerConstants.Communication.FRAME_SEP).append("0");
                LOGGER.info("Trama para responder --- " + sb.toString());
                socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")));
            } catch (NumberFormatException ex) {
                LOGGER.error(ex.getMessage(), ex);
                StringBuilder sb = new StringBuilder();
                sb.append(frame.getHeaderStr()).append(ArmsServerConstants.Communication.FRAME_SEP).append("1");
                socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")));
            }
        } else if (requermiento.equals("2") && frame.getBody().size() > 1) {
            if (!((String) frame.getBody().get(1)).isEmpty()) {
                String[] jetsons = ((String) frame.getBody().get(1)).split("#");
                for (String jetson : jetsons) {
                    String[] info = jetson.split("/");
                    if (info != null) {
                        String mac = info[0];
                        String ip = info[1];
                        boolean pingTojetson = Boolean.valueOf(info[2]);
                        LOGGER.info(mac + " " + ip + " " + pingTojetson);
                        Jetson jetsonToUpdate = equipoDAO.getJetsonXmac(sessionCtEquipos, mac);
                        if (jetsonToUpdate != null) {
                            jetsonToUpdate.setPingStatus(pingTojetson ? 3 : 0);
                            if (!pingTojetson) {
                                jetsonToUpdate.setInfoEqipo(null);
                                jetsonToUpdate.setPingStatusUp(1);
                            }
                            try {
                                equipoDAO.updateJetson(sessionCtEquipos, jetsonToUpdate);
                            } catch (Exception ex) {
                                LOGGER.error(ex.getMessage(), ex);
                            }
                        }
                    }
                }
                // si es distinto de 000 es un servidor local
                storeCodeLocal = properties.getObject("eyes.store.code");
                // si es dintinto de 000 es un servidor regional
                storeCodeRegional = properties.getObject("eyes.store.code.group");
                if ((storeCodeLocal != null && Integer.parseInt(storeCodeLocal) == 0) && (storeCodeRegional != null && Integer.parseInt(storeCodeRegional) != 0)) {
                    ipCentral = properties.getObject("clientSocket.ipCentral");
                    port = properties.getInt("serverSocket.port");
                    LOGGER.info("Es servidor regional");
                    if (!sendFrameToLocal(frame, ipCentral)) {
                        LOGGER.error("No se pudo enviar al central");
                    }
                    closeConnection();
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append(frame.getHeaderStr()).append(ArmsServerConstants.Communication.FRAME_SEP).append("0");
            LOGGER.info("Trama para responder --- " + sb.toString());
            socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")));
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(frame.getHeaderStr()).append(ArmsServerConstants.Communication.FRAME_SEP).append("1");
            socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")));
        }
        try {
            sleep(500);
        } catch (InterruptedException e) {
        }
        return true;
    }

    @Override
    public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
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
                LOGGER.info("Trama a enviar: " + trama);
                //LOGGER.info("Socket ip:" + socketClient.getIpServer() +", port:" + socketClient.getPortServer());
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
                            LOGGER.info("Respuesta recibida: " + frameRpta.toString());
                            if (frameRpta.getStatusTrama() == 0) {
                                return true;
                            }
                        }
                    }
                } else {
                    socketClient.setConnected(false);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                socketClient.setConnected(false);
            }
        }
        return false;
    }

    private boolean connectClient(String ip) {
        if (socketClient == null || !socketClient.isConnected()) {
            LOGGER.info("Store IP: " + ip + ", port: " + port);
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
