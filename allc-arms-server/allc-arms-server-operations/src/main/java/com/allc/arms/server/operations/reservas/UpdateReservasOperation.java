/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.operations.reservas;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.thoughtworks.xstream.XStream;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Tyrone Lopez
 */
public class UpdateReservasOperation extends AbstractOperation {

    private final static Logger LOGGER = Logger.getLogger(UpdateReservasOperation.class);

    private final String descriptorOperation = "UPDATE_RESERVAS";

    private Session sessionArtsEc = null;

    protected String storeCodeLocal;

    protected String storeCodeRegional;

    private ConnSocketClient socketClient = null;

    private final Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);

    protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);

    private void iniciarArtsEcSesion() {
        while (sessionArtsEc == null) {
            try {
                sessionArtsEc = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            if (sessionArtsEc == null) {
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
        LOGGER.info("Iniciando operacion" + descriptorOperation);
        iniciarArtsEcSesion();
        XStream xstreamAux = new XStream();
        xstreamAux.alias("ListItems", List.class);
        xstreamAux.alias("Item", ItemBean.class);
        storeCodeLocal = properties.getObject("eyes.store.code");
        // si es dintinto de 000 es un servidor regional
        storeCodeRegional = properties.getObject("eyes.store.code.group");

        boolean sendToCentralServ = false; // servidor regional

        // Es un servidor REGIONAL
        if (Integer.parseInt(storeCodeLocal) == 0 && Integer.parseInt(storeCodeRegional) != 0) {
            if (frame.loadData()) {
                boolean send = sendFrameToLocal(frame);
                if (send) {
                    closeConnection();
                    return true;
                } else {
                    LOGGER.error("Error al enviar al server.");
                }
            }
            sendToCentralServ = true;
        }
        if (!sendToCentralServ) {
            String storeNumber = frame.getBody().get(0).toString();
            List<Integer> rsIDBSNUN = consulta_id_bsn_un_x_codigo(Long.parseLong(storeNumber));
            List<ItemBean> listado = (List<ItemBean>) xstreamAux.fromXML(frame.getBody().get(1).toString());
            rsIDBSNUN.stream().map((row) -> row.longValue()).forEachOrdered((id_bsn_un) -> {
                listado.forEach((itemBean) -> {
                    BigInteger itm = consulta_as_itm_x_codigo(Long.parseLong(itemBean.getCod_Item()));
                    if (itm != null) {
                        long id_itm = itm.longValue();
                        actualiza_as_itm_str_api(itemBean.getCantidad().floatValue(), id_itm, id_bsn_un);
                    }
                });
            });
        }
        sessionArtsEc.close();
        String sb = frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + "0";
        socket.writeDataSocket(Util.addLengthStartOfString(sb, properties.getInt("serverSocket.quantityBytesLength")));
        return false;
    }

    @Override
    public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
        return true;
    }

    protected boolean sendFrameToLocal(Frame frame) {
        LOGGER.info("iniciando envio de trama");
        String str;
        List list;
        Frame frameRpta;
        int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
        try {
            boolean connectClient = false;
            if (socketClient == null || !socketClient.isConnected()) {
                LOGGER.info("Conectando cliente");
                connectClient = connectClient();
            }
            if (connectClient) {
                LOGGER.info("Cliente conectado");
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
                                return true;
                            }
                        }
                    }
                } else {
                    socketClient.setConnected(false);
                }
            } else {
                LOGGER.error("Cliente no conectado");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            socketClient.setConnected(false);
        }
        return false;
    }

    protected boolean connectClient() {
        LOGGER.info("IP a conectar" + properties.getObject("clientSocket.ipCentral"));
        if (socketClient == null) {
            socketClient = new ConnSocketClient();
            socketClient.setIpServer(properties.getObject("clientSocket.ipCentral"));
            socketClient.setPortServer(properties.getInt("serverSocket.port"));
            socketClient.setRetries(properties.getInt("clientSocket.retries"));
            socketClient.setTimeOutConnection(properties.getInt("serverSocket.timeOutConnection"));
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

    private boolean actualiza_as_itm_str_api(Float stock, long ID_ITM, long id_bsn_un) {
        Transaction tx = null;
        try {
            tx = this.sessionArtsEc.beginTransaction();
            Query query = this.sessionArtsEc.createSQLQuery("UPDATE AS_ITM_STR SET stock_reservado =:valor1, stock_reservado_fecha = :valor4 WHERE ID_ITM = :valor2 AND ID_BSN_UN = :valor3 ");
            query.setParameter("valor1", stock);
            query.setParameter("valor2", ID_ITM);
            query.setParameter("valor3", id_bsn_un);
            query.setTimestamp("valor4", new Timestamp(System.currentTimeMillis()));
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            tx.rollback();
            return false;
        }
        return true;
    }

    private BigInteger consulta_as_itm_x_codigo(long cdItm) {
        try {
            Query query = this.sessionArtsEc.createSQLQuery("SELECT ID_ITM FROM AS_ITM WHERE CD_ITM = " + cdItm);
            List<BigInteger> rows = query.list();  //Rows AS_ITM_LOAD_FILE
            return (rows != null && !rows.isEmpty()) ? rows.get(0) : null;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    private List<Integer> consulta_id_bsn_un_x_codigo(long cd_str_rt) {
        try {
            //id_bsn_un, de_str_rt, cd_str_rt, iva_tax, inc_prc, imp_1, imp_2, imp_3, imp_4, imp_5, imp_6, imp_7, imp_8, ce_cobe, no_afil_fl, id_ctab, dist_dir, fl_stock_load 
            Query query = this.sessionArtsEc.createSQLQuery("select id_bsn_un FROM PA_STR_RTL where cd_str_rt = " + cd_str_rt);
            List<Integer> rows = query.list();  //Rows AS_ITM_LOAD_FILE
            return (rows != null && !rows.isEmpty()) ? rows : new ArrayList<>();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }
}
