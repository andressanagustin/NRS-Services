package com.allc.arms.agent.operations.cer.sociedad;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class ConsultaClaveSociedadOperation extends AbstractOperation {
	
	private static Logger logger = Logger.getLogger(ConsultaClaveSociedadOperation.class);
	private Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
	
	
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		
		logger.info("Consulta de Clave para Sociedad Operation iniciada...");

		try {
			Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_SOC_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Consultando Clave para la sociedad: "+frame.getBody().get(0)+".\n", true);
			ConnSocketClient socketClient = new ConnSocketClient();
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(properties.getObject("clientSocket.ip"));
			socketClient.setPortServer(properties.getInt("clientSocket.port"));
			socketClient.setRetries(properties.getInt("clientSocket.retries"));
			socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
			String trama = Util.addLengthStartOfString(frame.getString().toString(), properties.getInt("serverSocket.quantityBytesLength"))
					.toString();
			logger.info("Trama a enviar: " + trama);
			boolean sociedadEncontrada = false;
			if (socketClient.connectSocketUsingRetries() && socketClient.writeDataSocket(trama)) {
				Frame frameRta = leerRespuesta(socketClient);
				frameRta.loadData();
				if (frameRta.getStatusTrama() == 0) {
					sociedadEncontrada = true;
				}
				responder(socket, frameRta, properties);
				socketClient.closeConnection();
				if(sociedadEncontrada)
					Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_SOC_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Clave de contabilidad para la sociedad: "+frame.getBody().get(0)+" encontrada.\n", true);
				else
					Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_SOC_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Clave de contabilidad para la sociedad: "+frame.getBody().get(0)+" no encontrada.\n", true);
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_SOC_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No hay conexión con el server para consultar la Clave de contabilidad para la sociedad: "+frame.getBody().get(0)+".\n", true);
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_SOC_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al buscar la Clave de contabilidad para la sociedad: "+frame.getBody().get(0)+".\n", true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
			logger.error(e.getMessage(), e);
		}

		return false;
	}
	
	private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	protected Frame leerRespuesta(ConnSocketClient socketClient) {
		int numberOfBytes = socketClient.readLengthDataSocket();
		if (numberOfBytes > 0) {
			String str = socketClient.readDataSocket(numberOfBytes);
			if (StringUtils.isNotBlank(str)) {
				List list = Arrays.asList(p.split(str));
				Frame frameRpta = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
						ArmsAgentConstants.Communication.FRAME_SEP);
				logger.info("Respuesta recibida: " + frameRpta.toString());
				return frameRpta;
			}
		}
		if( numberOfBytes == 0)
			numberOfBytes = socketClient.readLengthDataSocket();
		
		logger.info("No se recibio respuesta.");
		return null;
	}

	protected void responder(ConnSocketServer socket, Frame frame, PropFile properties) {
		String data = Util.addLengthStartOfString(frame.getString().toString(), properties.getInt("serverSocket.quantityBytesLength")).toString();
		if (socket.writeDataSocket(data))
			logger.info("Respuesta a enviar: " + frame.toString());
	}

	public boolean process(ConnPipeServer arg0, Frame arg1, PropFile arg2) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
