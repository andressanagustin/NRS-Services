/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.agent.operations.colas;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import org.apache.log4j.Logger;

/**
 *
 * @author Tyrone Lopez
 */
public class EliminarFileColasOperation extends AbstractOperation {

    private final Logger log = Logger.getLogger(EliminarFileColasOperation.class);

    private String alertListFileName;

    @Override
    public boolean shutdown(long timeToWait) {
        return false;
    }

    @Override
    public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
        log.info("Inicia operacion para eliminar archivos");
        int j = 0;
        boolean borrar = false;
        alertListFileName = properties.getObject("alertaPersonaCola.file.listAlert");
        while (j < 3 && !borrar) {
            if (Files.deleteFile4690(alertListFileName)) {
                borrar = true;
                log.info("Alert was detected, then AlertList " + alertListFileName + " was deleted.");
            } else {
                borrar = false;
                log.info("Alert was detected, but seekFile wasn't deleted");
            }
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
            j++;
        }
        String sb = frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP;
        if (borrar) {
            sb = sb.concat("0");
        } else {
            sb = sb.concat("1");
        }
        String trama = Util.addLengthStartOfString(sb, properties.getInt("serverSocket.quantityBytesLength"));
        log.info("Respuesta a enviar " + trama);
        if (socket.writeDataSocket(trama)) {
            log.info("Trama enviada");
        } else {
            log.fatal("trama no enviada");
        }
        return false;
    }

    @Override
    public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
        return false;
    }

}
