package com.allc.arms.agent.operations.cer.syscard;

import java.util.Date;

import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class WriteTramaSyscardOperation extends AbstractOperation{
	
	private static Logger logger = Logger.getLogger(WriteTramaSyscardOperation.class);
	//private Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		logger.info("Write Trama Syscard Operation iniciada...");

		try {
			Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "WR_TR_SYS|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Guardando la Trama Syscard para la tienda: "+frame.getBody().get(0)+".\n", true);
			
			String tramaSyscard = (String) frame.getBody().get(0);
			
			Files.creaEscribeDataArchivo4690(getSyscardFileName(properties), tramaSyscard + "\n", true);
			
				
			StringBuffer sb = new StringBuffer();
			sb.append("0");
			
			frame.setBodyStr(sb);
			
			responder(socket, frame, properties);
			
					
		} catch (Exception e) {
			try {
				logger.error("Error al guardar la trama de syscard.");
				StringBuffer sb = new StringBuffer();
				sb.append("1");
				frame.setBodyStr(sb);
				responder(socket, frame, properties);
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "WR_TR_SYS|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al guardar la trama Syscard para la tienda: "+frame.getBody().get(0)+".\n", true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	public boolean process(ConnPipeServer arg0, Frame arg1, PropFile arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
	
	private String getSyscardFileName(PropFile properties){
		return properties.getObject("syscard.file.name")+".DAT";
	}

	protected void responder(ConnSocketServer socket, Frame frame, PropFile properties) {
		String data = Util.addLengthStartOfString(frame.getString().toString(), properties.getInt("serverSocket.quantityBytesLength")).toString();
		if (socket.writeDataSocket(data))
			logger.info("Respuesta a enviar: " + frame.toString());
	}
}
