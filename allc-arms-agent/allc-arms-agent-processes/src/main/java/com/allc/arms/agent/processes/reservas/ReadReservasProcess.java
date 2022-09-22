/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.agent.processes.reservas;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.stockpile.ItemBean;
import com.allc.arms.utils.tsl.TSLUtility;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;
import com.ibm.OS4690.FileInputStream4690;
import com.ibm.OS4690.FlexosException;
import com.ibm.OS4690.RandomAccessFile4690;
import com.thoughtworks.xstream.XStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Tyrone Lopez
 */
public class ReadReservasProcess extends AbstractProcess {

    protected Logger log = Logger.getLogger(ReadReservasProcess.class);

    private ConnSocketClient socketClient = null;

    private final Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);

    protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);

    protected boolean finished = false, end = false;
    protected String storeNumber = "";

    // private ItemKeyed itemKeyed = new ItemKeyed();
    protected RandomAccessFile4690 maeItemsRead = null;
    private final String maeItemsFileName = "C:/ADX_IDT1/MAEITEMS.DAT";
    private final String macontraFileName = "C:/ADX_IDT1/MACONTRA.DAT";

    protected boolean init() {
        boolean result = false;
        try {
            ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
            storeNumber = (new Integer(controllerStatusData.getStoreNumber())).toString();
            while (storeNumber.length() < 3) {
                storeNumber = "0" + storeNumber;
            }
            //      itemKeyed.init(properties);

            result = true;
        } catch (FlexosException e) {
            log.error(e.getMessage(), e);
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
        }

        return result;
    }

    @Override
    public void run() {

        while (!end) {
            try {
                if (init()) {
                    log.info("Iniciando proceso ReadReservas");
                    addEyesInfo("Iniciando proceso de recoleccion de reservas", "STR");

                    if (Files.fileExists4690(maeItemsFileName)) {
                        BufferedReader br = null;
                        BufferedReader bufferedReaderContratos = null;
                        List<ItemBean> lista = new ArrayList<ItemBean>();
                        try {
                            br = new BufferedReader(new InputStreamReader(new FileInputStream4690(maeItemsFileName)));
                            bufferedReaderContratos = new BufferedReader(new InputStreamReader(new FileInputStream4690(macontraFileName)));
                            List<String> contratosActivos = new ArrayList<String>();
                            char[] con = new char[512];
                            int offsetContra = 0;
                            while (bufferedReaderContratos.read(con, offsetContra, 512) > 0) {
                                String contratoString = new String(con);
                                char[] contrato = new char[63];
                                Reader contratoInputString = new StringReader(contratoString.substring(4));
                                BufferedReader readerContrato = new BufferedReader(contratoInputString);
                                while (readerContrato.read(contrato, offsetContra, 63) > 0) {
                                    String regContrato = new String(contrato);
                                    byte[] llaveContratoB = regContrato.substring(0, 5).getBytes();
                                    String contratLlave = TSLUtility.unpack(llaveContratoB);
                                    if (!contratLlave.equals("0000000000")) {
                                        //byte[] estadoContratoB = regContrato.substring(5, 6).getBytes();
                                        if (regContrato.substring(5, 6).equals("1") || regContrato.substring(5, 6).equals("4") || regContrato.substring(5, 6).equals("7")) {
                                            contratosActivos.add(TSLUtility.unpack(llaveContratoB));
                                        }
                                    }
                                }

                            }

                            char[] reg = new char[512];
                            int offset = 0;
                            while (br.read(reg, offset, 512) > 0) {
                                String regStr = new String(reg);
                                Reader inputString = new StringReader(regStr.substring(4));
                                BufferedReader reader = new BufferedReader(inputString);
                                char[] registro = new char[39];
                                int offset1 = 0;
                                while (reader.read(registro, offset1, 39) > 0) {
                                    String registrStr = new String(registro);
                                    String code = registrStr.substring(7, 13);
                                    if (!code.isEmpty()) {
                                        byte[] arreglop;
                                        arreglop = code.getBytes();
                                        String unpack = TSLUtility.unpack(arreglop);
                                        if (!unpack.contains("000000000000") && !unpack.contains("-")) {
                                            byte[] cantidad, fecha, llave;
                                            llave = registrStr.substring(0, 7).getBytes();
                                            String unpackLlave = TSLUtility.unpack(llave);
                                            boolean ex = false;
                                            for (ItemBean item : lista) {
                                                if (item.getCod_Item().equals(unpack) && item.getContrato().equals(unpackLlave.substring(0, 10))) {
                                                    ex = true;
                                                    break;
                                                }

                                            }
                                            if (!ex) {
                                                ItemBean ib = new ItemBean();
                                                ib.setCod_Item(unpack);
                                                cantidad = registrStr.substring(14, 17).getBytes();
                                                fecha = registrStr.substring(22, 26).getBytes();
                                                String unpackCantidad = TSLUtility.unpack(cantidad);
                                                String unpackFecha = TSLUtility.unpack(fecha);
                                                ib.setFecha(unpackFecha);
                                                ib.setLlaveItem(unpackLlave);
                                                ib.setContrato(unpackLlave.substring(0, 10));
                                                try {
                                                    ib.setCantidad(Integer.parseInt(unpackCantidad));
                                                } catch (NumberFormatException e) {
                                                    log.info("Error item" + unpack);
                                                    log.error(e.getMessage(), e);
                                                    addEyesInfo("Error al procesar la recoleccion de reservas " + unpack, "ERR");
                                                }
                                                if (contratosActivos.contains(unpackLlave.substring(0, 10))) {
                                                    lista.add(ib);
                                                }
                                            }
                                        }
                                    }
                                }
                                reader.close();
                            }
                            List<ItemBean> listaFinal = new ArrayList<ItemBean>();
                            for (ItemBean itemBean : lista) {
                                ItemBean x = null;
                                for (ItemBean o : listaFinal) {
                                    if (o.getCod_Item().equals(itemBean.getCod_Item())) {
                                        x = o;
                                        break;
                                    }
                                }
                                if (x == null) {
                                    listaFinal.add(itemBean);
                                } else {
                                    Integer cant = x.getCantidad() + itemBean.getCantidad();
                                    x.setCantidad(cant);
                                }
                            }

                            XStream xstreamAux = new XStream();
                            xstreamAux.alias("ListItems", List.class);
                            xstreamAux.alias("Item", ItemBean.class);
                            String xml = xstreamAux.toXML(listaFinal);
                            StringBuilder sb = getHeaderFrame();
                            sb.append(ArmsAgentConstants.Communication.FRAME_SEP)
                                    .append(storeNumber)
                                    .append(ArmsAgentConstants.Communication.FRAME_SEP)
                                    .append(xml);
                            sendOperation(sb.toString());
                            Thread.sleep(3000);

                        } catch (IOException e1) {
                            log.error(e1.getMessage(), e1);
                            addEyesInfo("Error al procesar la recoleccion de reservas", "ERR");
                        } finally {
                            try {
                                if (br != null) {
                                    br.close();
                                }
                                if (bufferedReaderContratos != null) {
                                    bufferedReaderContratos.close();
                                }
                                log.info("Termina proceso ReadReservas " + lista.size());
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                addEyesInfo("Error al procesar la recoleccion de reservas", "ERR");
            }
            addEyesInfo("Termina proceso de recoleccion de reservas", "END");
            try {
                Thread.sleep(Long.parseLong(properties.getObject("reservas.timeSleep")));
                log.info("Detenieno proceso ReadReservas por: " + 300000);
            } catch (InterruptedException e) {
            }
        }
        finished = true;
    }

    private StringBuilder getHeaderFrame() {
        StringBuilder data = new StringBuilder();
        data.append("S").append(ArmsAgentConstants.Communication.FRAME_SEP)
                .append(ArmsAgentConstants.Process.UPDATE_RESERVAS_OERATION)
                .append(ArmsAgentConstants.Communication.FRAME_SEP).append("000")
                .append(ArmsAgentConstants.Communication.FRAME_SEP).append(0)
                .append(ArmsAgentConstants.Communication.FRAME_SEP)
                .append(ArmsAgentConstants.Communication.TEMP_CONN)
                .append(ArmsAgentConstants.Communication.FRAME_SEP)
                .append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
        return data;
    }

    private boolean sendOperation(String data) {
        log.info(data);
        List list = Arrays.asList(p.split(data));
        Frame frame = new Frame(list,
                ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
                ArmsAgentConstants.Communication.FRAME_SEP);
        if (frame.loadData()) {
            boolean send = sendFrameToLocal(frame);
            if (send) {
                closeConnection();
                return true;
            } else {
                log.error("Error al enviar al server.");
            }
        }
        return false;
    }

    protected boolean sendFrameToLocal(Frame frame) {
        log.info("iniciando envio de trama");
        String str;
        List list;
        Frame frameRpta;
        int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
        try {
            boolean connectClient = false;
            if (socketClient == null || !socketClient.isConnected()) {
                log.info("Conectando cliente");
                connectClient = connectClient();
            }
            if (connectClient) {
                log.info("Cliente conectado");
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
                                    ArmsAgentConstants.Communication.FRAME_SEP);
                            if (frameRpta.getStatusTrama() == 0) {
                                return true;
                            }
                        }
                    }
                } else {
                    socketClient.setConnected(false);
                }
            } else {
                log.error("Cliente no conectado");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            socketClient.setConnected(false);
        }
        return false;
    }

    protected boolean connectClient() {
        log.info("IP a conectar" + properties.getObject("clientSocket.ip"));
        if (socketClient == null) {
            socketClient = new ConnSocketClient();
            socketClient.setIpServer(properties.getObject("clientSocket.ip"));
            socketClient.setPortServer(properties.getInt("clientSocket.port"));
            socketClient.setRetries(properties.getInt("clientSocket.retries"));
            socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
            socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
            socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
        }
        return socketClient.connectSocketUsingRetries();
    }

    private void closeConnection() {
        if (socketClient != null) {
            socketClient.closeConnection();
            socketClient = null;
        }
    }

    @Override
    public boolean shutdown(long timeToWait
    ) {
        long startTime = Calendar.getInstance().getTimeInMillis();
        log.info("Deteniendo ReadReservasProcess...");
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
        return true;
    }

    public boolean addEyesInfo(String result, String type) {
        String eyesFileName = properties.getObject("eyes.ups.file.name") + "_" + ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
        return Files.creaEscribeDataArchivo4690(eyesFileName, "RESERVAS_JUG_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + properties.getObject("store.code") + "|" + type + "|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|" + result + "\n", true);
    }

}
