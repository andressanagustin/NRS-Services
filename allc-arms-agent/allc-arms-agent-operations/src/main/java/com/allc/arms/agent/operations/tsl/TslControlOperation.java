/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.agent.operations.tsl;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.tsl.TSLUtility;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.RandomAccessFile4690;
import java.io.IOException;
import java.util.Date;
import org.apache.log4j.Logger;

/**
 *
 * @author Tyrone Lopez
 */
public class TslControlOperation extends AbstractOperation {

    protected static Logger log = Logger.getLogger(TslControlOperation.class);

    protected RandomAccessFile4690 randSeekRead = null;

    protected RandomAccessFile4690 randSeqRead = null;

    protected String tslSeekFileName;

    protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);

    @Override
    public boolean shutdown(long l) {
        return false;
    }

    @Override
    public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
        // log.info("conectando - socet clietn");
        addEyesInfo("Iniciando operacion TSLCONTROL", "WAR");
        tslSeekFileName = properties.getObject("tslReader.file.seek");
        long punteroFile = obtieneOffsetSeek();
        String seekFileName = obtieneNombreSeek();
        String fileName = getEamtranFileName(properties);
        long fileBit = getEamtranSequence(fileName);
        String message = punteroFile + ArmsAgentConstants.Communication.FRAME_SEP + seekFileName + ArmsAgentConstants.Communication.FRAME_SEP + fileName + ArmsAgentConstants.Communication.FRAME_SEP + fileBit;
        String sb = frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + message + ArmsAgentConstants.Communication.FRAME_SEP + "0";
        String trama = Util.addLengthStartOfString(sb, properties.getInt("serverSocket.quantityBytesLength"));

        if (socket.writeDataSocket(trama)) {
            // log.info("Respuesta a enviar: " + trama);
        } else {
            log.fatal("trama no enviada");
        }
        addEyesInfo("Finalizando operacion TSLCONTROL", "WAR");
        return true;
    }

    protected long obtieneOffsetSeek() {
        long punteroFile = 0l;
        try {
            randSeekRead = new RandomAccessFile4690(tslSeekFileName, "r");
            String data;
            try {
                data = randSeekRead.readLine();
                randSeekRead.seek(0);
                if (null == data) {
                    punteroFile = 0;
                } else {
                    punteroFile = Long.parseLong(data.replaceAll(" ", ""));
                }
                randSeekRead.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                punteroFile = -1;
                randSeekRead.close();
                addEyesInfo("Error al leer archivo " + tslSeekFileName, "ERR");
            } catch (NumberFormatException e) {
                log.error(e.getMessage(), e);
                punteroFile = -1;
                randSeekRead.close();
            }
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        return punteroFile;
    }

    protected String obtieneNombreSeek() {
        String data = null;
        try {
            randSeekRead = new RandomAccessFile4690(tslSeekFileName, "r");
            randSeekRead.readLine();
            //la linea que contiene el nombre es la segunda
            data = randSeekRead.readLine();
            randSeekRead.seek(0);
            randSeekRead.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addEyesInfo("Error al leer archivo " + tslSeekFileName, "ERR");
            try {
                randSeekRead.close();
            } catch (Exception ex) {
            }
        }
        return data;
    }

    protected String getEamtranFileName(PropFile properties) {
        String eamtranFileName = null;

        try {
            String eamtranFilePrefix = "EAMTRAN";
            String eamtermsPathName = (String) properties.getObject("TSL.path.eamterms");
            String eamtermsFileName = (String) properties.getObject("TSL.file.eamterms");

            // log.info("Nombre Archivo: " + archivo.getAbsolutePath());
            String dataFile = TSLUtility.leerArchivo4690(eamtermsPathName + File4690.separatorChar + eamtermsFileName);
            if (null != dataFile) {
                //    log.info("Leído: " + dataFile);
                String secondPart = dataFile.split(eamtranFilePrefix)[1];
                String eamtranLetter = secondPart.substring(0, 1);
                eamtranFileName = eamtermsPathName + File4690.separatorChar + eamtranFilePrefix + eamtranLetter + ".DAT";
                //      log.info(eamtranFileName);
            } else {
                log.error("cannot find the path for the file " + eamtermsPathName + ":" + eamtermsFileName);
                addEyesInfo("Error no se encuentra el archivo EAMTRAN ", "ERR");
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return eamtranFileName;
    }

    private long getEamtranSequence(String syscardSeqFileName) {
        long seq = 0;

        try {
            if (Files.fileExists4690(syscardSeqFileName)) {
                RandomAccessFile4690 randomAccessFile4690 = new RandomAccessFile4690(syscardSeqFileName, "r");
                seq = randomAccessFile4690.length();
                randomAccessFile4690.close();
            }
            //  log.info("Sequence: " + seq);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return seq;
    }

    @Override
    public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean addEyesInfo(String result, String type) {
        String eyesFileName = properties.getObject("eyes.ups.file.name") + "_" + ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
        return Files.creaEscribeDataArchivo4690(eyesFileName, "TSL_CTRL_O|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + properties.getObject("store.code") + "|" + type + "|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Jetson Nano." + result + "\n", true);
    }

}
