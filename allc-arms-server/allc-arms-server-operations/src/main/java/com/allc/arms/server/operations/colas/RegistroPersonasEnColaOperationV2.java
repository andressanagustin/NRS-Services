package com.allc.arms.server.operations.colas;

import java.io.DataInputStream;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.type.StandardBasicTypes;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.store.RetailStoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.entities.RetailStore;
import com.allc.entities.Workstation;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class RegistroPersonasEnColaOperationV2 extends AbstractOperation {

    private static Logger log;
    protected ConnSocketClient socketClient;
    protected static Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
    protected String personasCola, numeroTrx, fecha, storeNumber, terminalNumber, cdOpr = null;
    protected int storeCode, terminalCode;
    RetailStoreDAO retailStoreDAO = new RetailStoreDAO();
    private Session session = null;
    protected PropFile prop = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
    private AlertaClientesColaSuperadoOperation alertaSupervisor;

    private void initialize() {
        log = Logger.getLogger(RegistroPersonasEnColaOperationV2.class);
        log.info("Inicia operacion Registro Personas en Cola V2");
        iniciarSesion("Arts");
        alertaSupervisor = new AlertaClientesColaSuperadoOperation(session);
    }

    @Override
    public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
        frame.loadData();
        initialize();
        StringBuilder msg = new StringBuilder();
        msg.append(frame.getHeaderStr()).append(frame.getSeparator());

        //valida si es regional, enviar registro a la central tambien
        Integer idBsnGp = properties.getObject("eyes.store.code.group") == null ? 0
                : Integer.parseInt(properties.getObject("eyes.store.code.group")); // server regional

        if (idBsnGp > 0) {
            try {
                sendFileHeader(frame);
                log.info("Si pudo enviar frame registroPersonasEnCola a la central");
            } catch (Exception e) {
                log.info("No pudo enviar frame registroPersonasEnCola a la central... " + e);
            }
        }

        fecha = (String) frame.getHeader().get(5);
        log.info("fecha          : " + fecha);
        storeNumber = (String) frame.getBody().get(0);
        log.info("storeNumber    : " + storeNumber);
        terminalNumber = "0" + (String) frame.getBody().get(1);
        log.info("terminalNumber : " + terminalNumber);
        numeroTrx = (String) frame.getBody().get(2);
        log.info("numeroTrx      : " + numeroTrx);
        personasCola = (String) frame.getBody().get(3);
        log.info("personasCola   : " + personasCola);
        
        if(personasCola != null && !personasCola.isEmpty()){

            String[] body = frame.getBodyStr().toString().split("\\|");

            if (body.length > 4) {
                cdOpr = (String) frame.getBody().get(4);
            }
            log.info("idOpr          : " + cdOpr);
            alertaSupervisor.setProp(obtenerPropiedadesAlertasSupervisorBD("3"));
            iniciarSesion("Arts");
            RetailStore retailStore = retailStoreDAO.getRetailStoreByCode(session, Integer.parseInt(storeNumber));
            storeCode = retailStore.getRetailStoreID();
            log.info("storeCode: " + storeCode);
            Workstation workstation = new Workstation();
            workstation = retailStoreDAO.getRetailTerminalByStoreCodeTerminalCode(session, terminalNumber, storeCode);
            log.info("is null?" + (workstation == null));
            terminalCode = workstation.getWorkstationID();
            log.info("terminalCode   : " + terminalCode);
            boolean grabo = false;
            try {
                grabo = insertaQueue(String.valueOf(storeCode), String.valueOf(terminalCode), numeroTrx, fecha, personasCola, cdOpr);
            } catch (SQLException e) {
                log.error("error al insertaQueue " + e);
                e.printStackTrace();
            }

            // respuesta para el CC
            if (grabo) {
                msg.append("0"); // si salió bien
                log.info("grabo en bdd");
                String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
                log.info("Trama a responder " + msg);
                socket.writeDataSocket(tmp);
                try {
                    alertaSupervisor.lanzarAlertaColaExcedida(storeCode, terminalCode, Integer.valueOf(numeroTrx), Integer.valueOf(personasCola), cdOpr);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            } else {
                msg.append("1"); // no salió bien
                log.info("No grabo en bdd");
                log.info("Trama a rsponder " + msg);
                String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
                socket.writeDataSocket(tmp);
            }
        }else{
                msg.append("0"); // si salió bien
                log.info("No guarda en bdd, personas en cola vacio");
                String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
                log.info("Trama a rsponder " + msg);
                socket.writeDataSocket(tmp);
        }
        
        session.close();
        session = null;
        return false;
    }

    protected void iniciarSesion(String name) {
        while (session == null) {
            try {
                session = HibernateSessionFactoryContainer.getSessionFactory(name).openSession();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            if (session == null) {
                try {
                    log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    public boolean insertaQueue(String codigoTienda, String codigoTerminal, String numeroTrx, String fecha, String numeroCola, String cdOpr)
            throws SQLException {

        Transaction tx = null;
        try {
            log.debug("voy a grabar a BDD");
            tx = session.beginTransaction();
            if (cdOpr != null) {
                Query query = session.createSQLQuery(
                        "INSERT INTO arts_ec.co_queue_dt (id_bsn_un, id_ws, ai_trn, date_queue, number_queue, cd_opr) VALUES (:valor1, :valor2, :valor3, now(), :valor5, :valor6)");
                query.setParameter("valor1", Integer.valueOf(codigoTienda));
                query.setParameter("valor2", Integer.valueOf(codigoTerminal));
                query.setParameter("valor3", Integer.valueOf(numeroTrx));
                //query.setParameter("valor4", fecha);
                query.setParameter("valor5", Integer.valueOf(numeroCola));
                query.setParameter("valor6", Integer.valueOf(cdOpr));
                query.executeUpdate();
            } else {
                Query query = session.createSQLQuery(
                        "INSERT INTO arts_ec.co_queue_dt (id_bsn_un, id_ws, ai_trn, date_queue, number_queue) VALUES (:valor1, :valor2, :valor3, now(), :valor5)");
                query.setParameter("valor1", Integer.valueOf(codigoTienda));
                query.setParameter("valor2", Integer.valueOf(codigoTerminal));
                query.setParameter("valor3", Integer.valueOf(numeroTrx));
                //query.setParameter("valor4", fecha);
                query.setParameter("valor5", Integer.valueOf(numeroCola));
                query.executeUpdate();
            }
            tx.commit();
            log.debug("grabo en BDD con exito");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    @Override
    public boolean process(ConnPipeServer arg0, Frame arg1, PropFile arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean shutdown(long arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean validarEjecucionProcesoAlerta(String codigoAlerta) {

        try {

            SQLQuery query = session.createSQLQuery(SqlPostgres.VALIDA_EJECUCION_PROCESO_ALERTA_SUPERVISOR);
            query.setString(0, codigoAlerta);
            Integer result = 0;

            result = ((BigInteger) query.list().get(0)).intValue();
            return result == 1;

        } catch (Exception ex) {
            log.error("Error al verificar validacion ejecucion proceso alerta supervisor", ex);
            return false;
        }

    }

    private Map<String, String> obtenerPropiedadesAlertasSupervisorBD(String codigoCatalgo) {
        Map<String, String> parametros = new HashMap<String, String>();

        try {
            if (!conexionDbLink()) {
                return null;
            }
            // faltan parametros de tienda y caja
            SQLQuery query = session.createSQLQuery(SqlPostgres.OBTENER_PARAMETROS_ALERTAS_SUPERVISOR_EN_BD_LOCAL);
            query.setParameter(0, Integer.parseInt(this.storeNumber), StandardBasicTypes.INTEGER);
            query.setParameter(1, Integer.parseInt(this.terminalNumber), StandardBasicTypes.INTEGER);
            query.setString(2, codigoCatalgo);
            //query.setString(0, codigoCatalgo);
            List<Object[]> rows = query.list();

            if (rows.size() == 0) {
                log.error("No se pudo cargar los parametros para la alerta " + codigoCatalgo);
                return null;
            }

            for (Object[] row : rows) {
                parametros.put((String) row[0], (String) row[1]);
            }

            return parametros;

        } catch (Exception ex) {
            log.error("Error al obtener parametros de configuracion de la alerta en BD", ex);
            return null;
        }

    }

    public boolean conexionDbLink() {
        if (prop.getObject("eyes.store.code.group") == null || Integer.parseInt(prop.getObject("eyes.store.code.group")) == 0) {
            return true;
        }

        log.debug("conexion a la central");
        try {

            SQLQuery query = session.createSQLQuery(SqlPostgres.CHECK_DBLINK);
            List<Object[]> rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                log.debug("ya existe una conexion a la central");
                return true;
            }

            query = session.createSQLQuery(SqlPostgres.CONEXION_DB_LINK
                    .replace("**HOST**", prop.getObject("central.db.host"))
                    .replace("**DBNAME**", prop.getObject("central.db.name"))
                    .replace("**USUARIO**", prop.getObject("central.db.usuario"))
                    .replace("**PASSWORD**", prop.getObject("central.db.password"))
            );

            rows = query.list();

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("No se pudo establecer conexion a BD central!", ex);
            return false;
            // TODO: handle exception
        }
        log.debug("conexion establecida");
        return true;

    }

    protected boolean connectCentral() {
        if (socketClient == null) {
            socketClient = new ConnSocketClient();
            SQLQuery query = session.createSQLQuery(SqlPostgres.OBTENER_IP_SUITE_CENTRAL);
            String hostCentral = (String) query.list().get(0);

            socketClient.setIpServer(hostCentral);
            socketClient.setPortServer(8000);
            socketClient.setRetries(2);
            socketClient.setTimeOutConnection(5000);
            socketClient.setTimeOutSleep(300);
            socketClient.setQuantityBytesLength(5);
        }
        return socketClient.connectSocketUsingRetries();
    }

    protected boolean sendFileHeader(Frame frame) {
        String str;
        List list;
        Frame frameRpta;
        int qtyBytesLength = prop.getInt("clientSocket.quantityBytesLength");
        try {
            if (socketClient == null || !socketClient.isConnected()) {
                log.debug("socket 1");
            }
            connectCentral();
            log.debug("socket 2");
            String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
            log.info("Trama a enviar: " + trama);
            if (socketClient.writeDataSocket(trama)) {
                int numberOfBytes = 0;
                int timeOutCycles = 0;
                while (numberOfBytes == 0) {
                    numberOfBytes = socketClient.readLengthDataSocket();
                    if (timeOutCycles == 5) {
                        // cada 5 timeouts escribimos una trama vacía para asegurarnos que el socket
                        // esté activo
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
                        frameRpta.loadData();
                        String resp = (String) frameRpta.getBody().get(0);
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
        } finally {
            socketClient.closeConnection();
        }
        return false;
    }

}
