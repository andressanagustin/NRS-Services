package com.allc.arms.server.processes.app.principal;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.server.Server;
import com.allc.arms.server.persistence.server.ServerDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.mail.EmailSender;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
//import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.process.AbstractProcess;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 *
 * @author Andres S.
 * com.allc.arms.server.processes.app.principal.PrincipalProcess
 */
public class PrincipalProcess extends AbstractProcess {

    private final static Logger LOGGER = Logger.getLogger(PrincipalProcess.class.getName());
    private final PropFile prop = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
    private boolean finished = false;
    public boolean isEnd = false;
    public boolean isSecundario = false;
    private Session sessionSaadmin = null;
    private Session sessionSaadminCentral = null;
    private ServerDAO serverDAO;
    protected ConnSocketClient socketServer = null;
    protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);

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

    private void iniciarSaadminCentralSesion() {
        while (sessionSaadminCentral == null && !isEnd) {
            try {
                sessionSaadminCentral = HibernateSessionFactoryContainer.getSessionFactory("SaadminCentral").openSession();
            } catch (HibernateException e) {
                LOGGER.error(e.getMessage(), e);
            }
            if (sessionSaadminCentral == null) {
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
        LOGGER.info("Iniciando Vallidacion de server principal...");
        // valida si es server secundario, en las tablas nuevas,
        iniciarSaadminSesion();
        serverDAO = new ServerDAO();

        try {
            BigInteger idBsnGp = prop.getObject("eyes.store.code.group") == null ? BigInteger.ZERO
                    : new BigInteger(prop.getObject("eyes.store.code.group")); // server regional
            int intentos = 0;
            Server serverSecundario = serverDAO.getServer(sessionSaadmin, idBsnGp);
            isSecundario = !serverSecundario.isPrimario_app();
            int nodo = serverSecundario.getIdNodo();
            // si lo es hace ping contra una operacion del server primario a ver si esta arriba!
            while (!isEnd && isSecundario) {
                iniciarSaadminSesion();
                LOGGER.info("Es un server secundario. Intento " + intentos);
                List listToSend;
                StringBuilder message = new StringBuilder();
                Frame frameToAgent;
                boolean acivoPrincipal = false;
                intentos++;

                // buscar ip del server principal del mismo nodo
                Server serverPrincipal = serverDAO.getServerxNodoPrincipal(sessionSaadmin, nodo);
                if (serverPrincipal != null) {
                    String ip = serverPrincipal.getIp();
                    if (connectClient(ip)) {
                        LOGGER.info("CONECTA");
                        String today = ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date());
                        message.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
                                .append(ArmsServerConstants.Process.APP_PRINCIPAL)
                                .append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
                                .append(ArmsServerConstants.Communication.FRAME_SEP).append("")
                                .append(ArmsServerConstants.Communication.FRAME_SEP).append("")
                                .append(ArmsServerConstants.Communication.FRAME_SEP).append(today)
                                .append(ArmsServerConstants.Communication.FRAME_SEP).append("");
                        LOGGER.info("Trama a enviar: " + message);
                        listToSend = Arrays.asList(p.split(message.toString()));
                        frameToAgent = new Frame(listToSend, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
                                ArmsServerConstants.Communication.FRAME_SEP);
                        if (frameToAgent.loadData()) {
                            if (sendTramaToAgent(ip, frameToAgent)) {
                                intentos = 0;
                                acivoPrincipal = true;
                            }
                        } else {
                            LOGGER.info("Trama mal armada. No envia");
                        }
                        //Esta arriba el server principal
                        if (acivoPrincipal) {
                            LOGGER.info("Server principal activo. Respondio Operacion");
                        } else {
                            LOGGER.info("Server principal no responde. No respondio Operacion");
                        }

                        closeConnection();
                    } else {

                        //acivoPrincipal = false;
                        //SI NO CONECTA LO DEJA OFFLINE
                        LOGGER.info("Server principal no responde. No conecto.");
                    }

                    if (!acivoPrincipal && intentos >= 3) {
                        intentos = 0;
                        String messageEmail = "Cambiamos al server: " + serverSecundario.getIp() + " Como principal y al " + serverPrincipal.getIp() + " Como secundario.";
                        LOGGER.info(messageEmail);
                        //activar el secundario como principal de aplicacion y dejar el primario como secundario de aplicacion.
                        serverPrincipal.setPrimario_app(false);
                        serverDAO.updateServer(sessionSaadmin, serverPrincipal);
                        serverSecundario.setPrimario_app(true);
                        serverDAO.updateServer(sessionSaadmin, serverSecundario);
                        iniciarSaadminCentralSesion();
                        Server prin = serverDAO.getServerxIp(sessionSaadminCentral, serverPrincipal.getIp());
                        prin.setPrimario_app(false);
                        serverDAO.updateServer(sessionSaadminCentral, prin);

                        Server sec = serverDAO.getServerxIp(sessionSaadminCentral, serverSecundario.getIp());
                        sec.setPrimario_app(true);
                        serverDAO.updateServer(sessionSaadminCentral, sec);
                        try {
                            if (sessionSaadminCentral != null) {
                                sessionSaadminCentral.close();
                                sessionSaadminCentral = null;
                            }
                        } catch (HibernateException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                        EmailSender.sendMail(
                                prop.getObject("smtp.user"),
                                prop.getObject("smtp.password"),
                                prop.getObject("smtp.server"),
                                prop.getObject("smtp.user"),
                                prop.getObject("administrator.alert.mail.to"),
                                "Servidor no responde.",
                                messageEmail);
                    }
                }
                serverSecundario = serverDAO.getServer(sessionSaadmin, idBsnGp);
                isSecundario = !serverSecundario.isPrimario_app();
                try {
                    if (sessionSaadmin != null) {
                        sessionSaadmin.close();
                        sessionSaadmin = null;
                    }
                    Thread.sleep(prop.getInt("tsl.contol.process.timer"));
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }

            }
            LOGGER.info("Se detiene el proceso");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            //closeConnection();
        } finally {
            try {
                if (sessionSaadmin != null) {
                    sessionSaadmin.close();
                    sessionSaadmin = null;
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    protected boolean connectClient(String ip) {
        if (socketServer == null || !socketServer.isConnected()) {
            socketServer = new ConnSocketClient();
            socketServer.setIpServer(ip);
            socketServer.setPortServer(prop.getInt("serverSocket.port"));
            socketServer.setRetries(2);
            socketServer.setTimeOutConnection(prop.getInt("serverSocket.timeOutConnection"));
            socketServer.setTimeOutSleep(prop.getInt("serverSocket.timeOutSleep"));
            socketServer.setQuantityBytesLength(prop.getInt("serverSocket.quantityBytesLength"));
        }
        LOGGER.info("Intenta Conectar con agente IP: " + socketServer.getIpServer() + ", puerto: " + socketServer.getPortServer());
        return socketServer.connectSocket(); //Pruebo conectar una sola vez
    }

    protected void closeConnection() {
        if (socketServer != null) {
            socketServer.closeConnection();
        }
    }

    protected boolean sendTramaToAgent(String ip, Frame frame) {
        String str;
        List list;
        Frame frameRpta;
        int qtyBytesLength = prop.getInt("clientSocket.quantityBytesLength");
        try {
            connectClient(ip);
            String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
            LOGGER.info("Trama a enviar: " + trama);
            if (socketServer.writeDataSocket(trama)) {
                int numberOfBytes = 0;
                int timeOutCycles = 0;
                while (numberOfBytes == 0) {
                    numberOfBytes = socketServer.readLengthDataSocket();
                    if (timeOutCycles == 5) {
                        // cada 5 timeouts escribimos una trama vacía para asegurarnos que el socket esté activo
                        String mje = Util.addLengthStartOfString("", qtyBytesLength).toString();
                        if (!socketServer.writeDataSocket(mje)) {
                            socketServer.setConnected(false);
                            return false;
                        }
                        timeOutCycles = 0;
                    }
                    timeOutCycles++;
                }
                if (numberOfBytes > 0) {
                    str = socketServer.readDataSocket(numberOfBytes);
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
                LOGGER.info("NO ENVIA LA TRAMA");
                socketServer.setConnected(false);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            socketServer.setConnected(false);
        }
        return false;
    }

    @Override
    public boolean shutdown(long timeToWait) {
        isEnd = true;
        //closeConnection();
        long startTime = Calendar.getInstance().getTimeInMillis();
        LOGGER.info("Deteniendo proceso de validacion de aplicacion principal ...");
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
        LOGGER.info("Finalizó el Proceso de validacion de aplicacion principal.");
        return true;
    }
}
