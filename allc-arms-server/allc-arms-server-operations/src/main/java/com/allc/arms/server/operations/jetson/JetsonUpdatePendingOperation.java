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
import com.allc.arms.server.persistence.equipo.JetsonUploadFile;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

/**
 *
 * @author Tyrone Lopez
 */
public class JetsonUpdatePendingOperation extends AbstractOperation {

    private static Logger log = Logger.getLogger(JetsonUpdatePendingOperation.class);

    private Session sessionCtEquipos = null;
    private Session sessionEyes = null;
    protected String storeCodeLocal;
    protected String storeCodeRegional;
    protected String ipCentral;
    private EquipoDAO equipoDAO;

    protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
    protected ConnSocketClient socketClient = null;
    protected PropFile prop = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);

    protected String descriptorOperation = "JT_UPD_PND_O";
    private final String outFolderServer = "/var/www/html/nrs/ALCEYES/jetson/entrenamiento";

    String storeNumber;

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
    public boolean shutdown(long timeToWait) {
        return false;
    }

    @Override
    public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
        log.info("Inicio operacion: " + descriptorOperation);
        try {
            iniciarCtEquiposSesion();
            iniciarSesionEyes();
            equipoDAO = new EquipoDAO();
            String requerimiento = frame.getBody().get(0).toString().toUpperCase();
            XStream xstreamAux = new XStream();
            xstreamAux.alias("JetsonList", List.class);
            xstreamAux.alias("Jetson", Jetson.class);
            String xml = frame.getBody().get(1).toString();
            List<Jetson> jetsons = (List<Jetson>) xstreamAux.fromXML(xml);
            log.info("Requerimiento recibido " + requerimiento);
            switch (requerimiento) {
                case "S":
                    jetsons.forEach((jetson) -> {

                        try {
                            log.info("Buscando jetson MAC " + jetson.getMacEqu() + " " + jetson.getInfoEqipo());
                            Jetson jetsonToUpdate = equipoDAO.getJetsonXmac(sessionCtEquipos, jetson.getMacEqu());
                            jetsonToUpdate.setInfoEqipo(jetson.getInfoEqipo());
                            equipoDAO.updateJetson(sessionCtEquipos, jetsonToUpdate);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    });
                    break;
                case "N":
                    jetsons.forEach((jetson) -> {
                        try {
                            Jetson createJetson = equipoDAO.getJetsonXmac(sessionCtEquipos, jetson.getMacEqu());
                            if (createJetson == null) {
                                Equipo e = equipoDAO.findOnlineJetsonByIdStore(sessionEyes, jetson.getIdLocal());
                                createJetson = jetson;
                                createJetson.setCodigoEqu(null);
                                createJetson.setIdEquipo(e.getIdEquipo());
                                createJetson.setStatus(null);
                                createJetson.setStatusApagar(null);
                                createJetson.setStatusRegistrarUp(null);
                                createJetson.setStatusReiniciar(null);
                                createJetson.setStatusUpload(null);
                                createJetson.setStatusUploadUp(null);
                                equipoDAO.updateJetson(sessionCtEquipos, createJetson);
                            }
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    });
                    break;
                case "U":
                    jetsons.forEach((jetson) -> {
                        log.info("Trabajando con el jetson " + jetson);
                        try {

                            Jetson jetsonLocal = equipoDAO.getJetsonXmac(sessionCtEquipos, jetson.getMacEqu());
                            if (jetsonLocal == null) {
                                jetsonLocal = new Jetson();
                            }
                            jetsonLocal.setFecUpload(new Date());
                            jetsonLocal.setFecUpdate(new Date());
                            jetsonLocal.setStatusUpload(null);
                            equipoDAO.updateJetson(sessionCtEquipos, jetsonLocal);
                            JetsonUploadFile entrenamiento = equipoDAO.insertJetsonUploadFile(sessionCtEquipos, jetsonLocal.getCodigoEqu(), jetson.getFileUploadUp());
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    });
                    break;
                default:
                    log.info("operacion no soportada");
                    break;
            }
            String sb = frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + "0";
            log.info("Trama para responder --- " + sb);
            socket.writeDataSocket(Util.addLengthStartOfString(sb, properties.getInt("serverSocket.quantityBytesLength")));
        } catch (Exception e) {
            String sb = frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + "1";
            log.info("Trama para responder --- " + sb);
            socket.writeDataSocket(Util.addLengthStartOfString(sb, properties.getInt("serverSocket.quantityBytesLength")));
        } finally {
            if (sessionCtEquipos != null) {
                sessionCtEquipos.close();
                sessionCtEquipos = null;
            }
            if (sessionEyes != null) {
                sessionEyes.close();
                sessionEyes = null;
            }
        }
        return false;
    }

    @Override
    public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
        return false;
    }

}
