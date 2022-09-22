package com.allc.arms.server.operations.app.principal;

import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * 80:com.allc.arms.server.operations.app.principal.PrincipalOperation
*/
public class PrincipalOperation extends AbstractOperation{
	private static Logger logger = Logger.getLogger(PrincipalOperation.class);

	@Override
	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		logger.info("PrincipalOperation iniciada...");
		try {
			StringBuilder sb = new StringBuilder(frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + "0");
			String trama = Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength"));
			logger.info("Respuesta a enviar: " + trama);
			if(socket.writeDataSocket(trama)){
				logger.info("Trama enviada al server");
			} else {
				logger.info("NO se envia la trama al server");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

}