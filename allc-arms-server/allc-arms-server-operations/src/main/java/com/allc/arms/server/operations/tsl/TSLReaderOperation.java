package com.allc.arms.server.operations.tsl;

import com.allc.arms.server.operations.tsl.utils.Item;
import com.allc.arms.server.operations.tsl.utils.JsonUtil;
import java.math.BigInteger;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.store.RetailStoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.arms.utils.tsl.TSLRecordXMLGenerator;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.entities.RetailStore;
import com.allc.entities.RetailTransactionLineItem;
import com.allc.entities.SaleReturnLineItem;
import com.allc.entities.Transaction;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.Query;

public class TSLReaderOperation extends AbstractOperation {

    private static Logger log = Logger.getLogger(TSLReaderOperation.class);
    protected static String tslDefaultLocalRepositoryToStore;
    RetailStoreDAO retailStoreDAO = new RetailStoreDAO();
    private Session session = null;
    private String storeCode = null;
    private String syncPath = null;
    private BigInteger idBsnGp = null;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");

    public boolean shutdown(long timeToWait) {
        // TODO Auto-generated method stub
        if (session != null) {
            session.close();
            session = null;
        }
        return false;
    }

    public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {

        //agregar codigo grupo
        storeCode = properties.getObject("eyes.store.code") == null ? "0" : properties.getObject("eyes.store.code");
        syncPath = properties.getObject("searchEbil.sync.folder.path");
        idBsnGp = properties.getObject("eyes.store.code.group") == null ? BigInteger.ZERO
                : new BigInteger(properties.getObject("eyes.store.code.group"));
        try {
            UtilityFile.createWriteDataFile(getEyesFileName(properties), "TSL_READ_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + frame.getHeader().get(3) + "|STR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Iniciando procesamiento de TSL.\n", true);

            tslDefaultLocalRepositoryToStore = properties.getObject("TSL.defaultLocalRepositoryToStore");

            if (StringUtils.isNotBlank(tslDefaultLocalRepositoryToStore)) {
                UtilityFile.createDir(tslDefaultLocalRepositoryToStore);
            }
            log.info("TSL.defaultLocalRepositoryToStore: " + tslDefaultLocalRepositoryToStore);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        try {
            StringBuilder msg = new StringBuilder();
            /**
             * Instantiate from TSLRecordXMLGenerator *
             */
            TSLRecordXMLGenerator tslRecordXMLGenerator = new TSLRecordXMLGenerator();
            /**
             * pass the TSLRecord and the path to save it as a xml file *
             */
            iniciarSesion("Arts");
            int tienda = new Integer(((String) frame.getHeader().get(3)));
            RetailStore retailStore = retailStoreDAO.getRetailStoreByCode(session, tienda);
            //crar nuevo metodo generate que reciba codigo de grupo...
            // el metodo solo debe distinguir si es central o no
            Transaction trx = tslRecordXMLGenerator.generate(frame,
                    tslDefaultLocalRepositoryToStore,
                    true,
                    retailStore,
                    (idBsnGp.compareTo(BigInteger.ZERO) == 0 && Integer.valueOf(storeCode) == 0), //esCentral? 
                    syncPath);

            if (trx != null) {
                msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("0");
                String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
                socket.writeDataSocket(tmp);
                log.info("----- trx no null " + properties.getObject("google.storage.file.path"));
                try {
                    if (trx.getRetailTransaction() != null && trx.getRetailTransaction().getFacturaElec() != null && trx.getRetailTransaction().getFacturaElec().getNumeroFac() != null && !trx.getRetailTransaction().getFacturaElec().getNumeroFac().isEmpty()) {
                        String fecha = trx.getEndDateTimeString().replaceAll("(?i)T", "").replace("-", "").replace(":", "");
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonUtil ju = new JsonUtil();
                        ju.setStoreId(Integer.parseInt(trx.getRetailStoreCode()));
                        ju.setTimestamp(fecha);
                        List<Item> items = new LinkedList<>();
                        //&& !trx.getCancelFlag().booleanValue()
                        if (trx.getRetailTransaction() != null && trx.getRetailTransaction().getLineItems() != null && !trx.getRetailTransaction().getLineItems().isEmpty() && !trx.getCancelFlag()) {
                            ju.setFacturaId(trx.getRetailTransaction().getFacturaElec() != null ? trx.getRetailTransaction().getFacturaElec().getNumeroFac() : "");
                            for (Object lineItem : trx.getRetailTransaction().getLineItems()) {
                                if (lineItem != null && lineItem instanceof RetailTransactionLineItem) {
                                    RetailTransactionLineItem rtli = ((RetailTransactionLineItem) lineItem);
                                    SaleReturnLineItem itm = rtli.getSaleLI();
                                    if (itm != null) {
                                        Object[] row = getPostBarCode(session, itm.getItemCode().toString());
                                        Integer itemType = row != null && row.length > 0 && row[0] != null ? (int) row[0] : 1;
                                        if (!itemType.equals(7)) {
                                            short fl = 0;
                                            if (itm.getUnits() > 0) {
                                                fl = 1;
                                            }
                                            int quantity = fl == 1 ? itm.getUnits().intValue() : itm.getQuantity().intValue();
                                            if (rtli.getVoidFlag()) {
                                                quantity = quantity * (-1);
                                            }
                                            //String posBarCode = getPostBarCode(session, itm.getItemCode().toString());
                                            Item i = new Item(itm.getItemCode(), quantity, fl == 1 ? "V" : "Q");
                                            items.add(i);
                                        }
                                    }
                                }
                            }
                            if (items.size() > 0) {
                                ju.setItems(items);
                                String fileName = properties.getObject("google.storage.file.path") + File.separator + trx.getRetailStoreCode() + "-"
                                        + trx.getWorkstationCode() + "-" + trx.getSequenceNumber() + "-" + fecha + ".json";
                                log.info("creando archivo json " + fileName);
                                objectMapper.writeValue(new File(fileName), ju);
                            }
                        }
                    }

                } catch (Exception e) {
                    //manejar error
                    log.error(e.getMessage(), e);
                }

            } else {
                log.info("----- trx null");
                msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("-1");
                String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
                socket.writeDataSocket(tmp);
            }
            if (session != null) {
                session.close();
                session = null;
            }

//			COMENTADO TICKETSERVER
//			if (trx.getControlTransaction() != null && trx.getControlTransaction().getBusinessEOD() != null) {
//				log.info("Starting Ticketserver...(IP: " + socket.getClient().getInetAddress().getHostAddress() + ")");
//				UtilityFile.createWriteDataFile(getEyesFileName(properties), "TSL_READ_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|PRC|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Cierre encontrado, procesando Ticketserver.\n", true);
//				TicketserverApp tcktSrvr = new TicketserverApp();
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//				String fecha = sdf.format(trx.getBusinessDayDate()).toString();
//				String zipName = trx.getRetailStoreCode() + fecha.substring(6, 8) + fecha.substring(4, 6) + fecha.substring(2, 4) + ".zip";
//
//				tcktSrvr.process(socket.getClient().getInetAddress().getHostAddress(), zipName);
//			}
            UtilityFile.createWriteDataFile(getEyesFileName(properties), "TSL_READ_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + frame.getHeader().get(3) + "|END|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Línea del TSL procesada.\n", true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            try {
                UtilityFile.createWriteDataFile(getEyesFileName(properties), "TSL_READ_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + frame.getHeader().get(3) + "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error al procesar la línea del TSL.\n", true);
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
            }
        }
        return false;
    }

    private String getEyesFileName(PropFile properties) {
        return properties.getObject("eyes.ups.file.name") + "_" + ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
    }

    public static String formatMonth(String month) {
        try {
            new Integer(month);
        } catch (NumberFormatException e) {
        }
        if ("10".equalsIgnoreCase(month)) {
            return "A";
        } else if ("11".equalsIgnoreCase(month)) {
            return "B";
        } else if ("12".equalsIgnoreCase(month)) {
            return "C";
        }
        return month;
    }

    @Override
    public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
        // TODO Auto-generated method stub
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

    private Object[] getPostBarCode(Session session, String code) {
        Query q = session.createSQLQuery("select it.ID_TY_ITM ,i.id_itm_ps from arts_ec.id_ps i \n"
                + "right join arts_ec.as_itm it on i.id_itm = it.id_itm\n"
                + "where cd_itm = '" + code + "' limit 1");

        return (Object[]) q.uniqueResult();
    }
}
