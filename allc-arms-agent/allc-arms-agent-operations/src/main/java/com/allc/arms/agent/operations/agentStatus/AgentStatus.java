package com.allc.arms.agent.operations.agentStatus;

import java.util.Date;

import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.ControllerDateTime;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class AgentStatus extends AbstractOperation{
	private static Logger logger = Logger.getLogger(AgentStatus.class);

	@Override
	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		logger.info("AgentStatus iniciada...");
		try {
			String message = "0";
			String dateController = ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(ControllerDateTime.getDateTimeController());
			StringBuilder sb = new StringBuilder(frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + message + ArmsAgentConstants.Communication.FRAME_SEP + dateController);
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
