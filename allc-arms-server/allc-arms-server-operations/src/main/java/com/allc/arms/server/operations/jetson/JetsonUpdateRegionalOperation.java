package com.allc.arms.server.operations.jetson;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.equipo.Equipo;
import com.allc.arms.server.persistence.equipo.EquipoDAO;
import com.allc.arms.server.persistence.equipo.Jetson;
import com.allc.arms.server.persistence.equipo.JetsonEntrenamiento;
import com.allc.arms.server.persistence.equipo.JetsonVersionAprendizaje;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.thoughtworks.xstream.XStream;
import java.util.List;
import org.hibernate.HibernateException;

public class JetsonUpdateRegionalOperation extends AbstractOperation {

    protected static Logger log = Logger.getLogger(JetsonUpdateRegionalOperation.class);

    private Session sessionCtEquipos = null;
    private Session sessionEyes = null;
    EquipoDAO equipoDAO;

    protected String descriptorOperation = "JT_REG_O";

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
                    sleep(3000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private void iniciarEyesSesion() {
        while (sessionEyes== null) {
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
    public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
        log.info("Inicio: " + descriptorOperation);
        String msg = "1";
        // outFolderServer = "C:/Users/tyrone.lopez/Downloads/ArmsServer/ArmsServer/jetson/entrenamiento";
        try {
            UtilityFile.createWriteDataFile(getEyesFileName(properties),
                    descriptorOperation + "|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
                    + frame.getHeader().get(3) + "|STR|"
                    + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
                    + "|Iniciando recepción de estado jetson.\n",
                    true);
            iniciarCtEquiposSesion();
            equipoDAO = new EquipoDAO();
            if (frame.getBody().size() >= 3 && frame.getBody().get(2) != null && !frame.getBody().get(2).toString().isEmpty()) {
                XStream xstreamAux2 = new XStream();
                xstreamAux2.alias("JetsonEntrenamiento", JetsonEntrenamiento.class);
                JetsonEntrenamiento entrenamiento = (JetsonEntrenamiento) xstreamAux2.fromXML(frame.getBody().get(3).toString());
                equipoDAO.saveJetsonEntrenamiento(sessionCtEquipos, entrenamiento);
                XStream xstreamAux1 = new XStream();
                xstreamAux1.alias("JetsonVersionAprendizaje", JetsonVersionAprendizaje.class);
                JetsonVersionAprendizaje jva = (JetsonVersionAprendizaje) xstreamAux1.fromXML(frame.getBody().get(2).toString());
                equipoDAO.saveJetsonAprendizajes(sessionCtEquipos, jva);

            }
            String jetsonXml = (String) frame.getBody().get(0);
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
            List<Jetson> jetsons = (List<Jetson>) xstreamAux.fromXML(jetsonXml);
            for (Jetson jetson : jetsons) {
                Jetson jetsonXmac = equipoDAO.getJetsonXmac(sessionCtEquipos, jetson.getMacEqu());
                if (jetsonXmac == null) {
                    jetsonXmac = new Jetson();
                    iniciarEyesSesion();
                    Equipo equipo = equipoDAO.findOnlineJetsonByIdStore(sessionEyes, Integer.parseInt(frame.getBody().get(1).toString()));
                    jetsonXmac.setIdEquipo(equipo.getIdEquipo());
                }
                jetsonXmac.setFecNew(jetson.getFecNew());
                jetsonXmac.setDesEqu(jetson.getDesEqu());
                jetsonXmac.setMacEqu(jetson.getMacEqu());
                jetsonXmac.setIpEqu(jetson.getIpEqu());
                jetsonXmac.setFecNew(jetson.getFecNew());
                jetsonXmac.setFecUpdate(new Date());
                jetsonXmac.setEstadoActivo(jetson.getEstadoActivo());
                jetsonXmac.setCodigoVap(jetson.getCodigoVap());
                jetsonXmac.setHsExtErrores(jetson.getHsExtErrores());
                jetsonXmac.setEmtEstadoDesde(jetson.getEmtEstadoDesde());
                jetsonXmac.setEmtEstadoHasta(jetson.getEmtEstadoHasta());
                jetsonXmac.setUserFtp(jetson.getUserFtp());
                jetsonXmac.setPassFtp(jetson.getPassFtp());
                jetsonXmac.setStatus(jetson.getStatus());
                jetsonXmac.setStatusReiniciar(jetson.getStatusReiniciar());
                jetsonXmac.setStatusApagar(jetson.getStatusApagar());
                jetsonXmac.setStatusUpload(jetson.getStatusUpload());
                jetsonXmac.setFecUpload(jetson.getFecUpload());
                jetsonXmac.setInfoEqipo(jetson.getInfoEqipo());
                jetsonXmac.setPingStatus(jetson.getPingStatus());
                equipoDAO.updateJetson(sessionCtEquipos, jetsonXmac);
            }

            msg = "0";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            try {
                UtilityFile.createWriteDataFile(getEyesFileName(properties),
                        descriptorOperation + "|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
                        + frame.getHeader().get(3) + "|ERR|"
                        + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
                        + "|Error al recibir el estado jetson.\n",
                        true);
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
            }
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
        //respondemos trama de confirmacion
        String sb = frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + msg;
        log.info("Trama para responder --- " + sb);
        socket.writeDataSocket(Util.addLengthStartOfString(sb, properties.getInt("serverSocket.quantityBytesLength")));

        return false;
    }

    private String getEyesFileName(PropFile properties) {
        return properties.getObject("eyes.ups.file.name") + "_" + ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
    }

    @Override
    public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
        // TODO Auto-generated method stub
        return false;
    }

}
