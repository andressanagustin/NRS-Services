/**
 * 
 */
package com.allc.arms.agent.operations.cer.devolucion;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.allc.arms.agent.operations.cer.cedpadruc.ConsultaCedPadRucOperation;
import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * @author gustavo
 *
 */
public class ConsultaDevolucionOperation extends AbstractOperation {
	private Logger logger = Logger.getLogger(ConsultaCedPadRucOperation.class);
	private Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);

	/* (non-Javadoc)
	 * @see com.allc.core.operation.AbstractOperation#shutdown(long)
	 */
	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.allc.core.operation.AbstractOperation#process(com.allc.comm.socket.ConnSocketServer, com.allc.comm.frame.Frame, com.allc.properties.PropFile)
	 */
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		logger.info("Consulta Devolución Operation iniciada...");
		try {
			Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_DEV_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Consultando devolución: "+frame.getBody().get(0)+".\n", true);
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
			boolean devEncontrada = false;
			if (socketClient.connectSocketUsingRetries() && socketClient.writeDataSocket(trama)) {
				Frame frameRta = leerRespuesta(socketClient);
				frameRta.loadData();
				if (frameRta.getStatusTrama() == 0) {
					devEncontrada = true;
				}
				responder(socket, frameRta, properties);
				socketClient.closeConnection();
				if(devEncontrada)
					Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_DEV_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Devolución: "+frame.getBody().get(0)+" encontrada.\n", true);
				else
					Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_DEV_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Devolución: "+frame.getBody().get(0)+" no encontrada.\n", true);
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_DEV_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No hay conexión con el server para consultar la Devolución: "+frame.getBody().get(0)+".\n", true);
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_DEV_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al buscar la Devolución: "+frame.getBody().get(0)+".\n", true);
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

	/* (non-Javadoc)
	 * @see com.allc.core.operation.AbstractOperation#process(com.allc.comm.pipe.ConnPipeServer, com.allc.comm.frame.Frame, com.allc.properties.PropFile)
	 */
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		logger.info("Consulta Devolución Operation iniciada...");
		try {
			ConnSocketClient socketClient = new ConnSocketClient();
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(properties.getObject("clientSocket.ip"));
			socketClient.setPortServer(properties.getInt("clientSocket.port"));
			socketClient.setRetries(properties.getInt("clientSocket.retries"));
			socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
			frame.getList().set(Frame.COMMUNICATION_CHANNEL, ArmsAgentConstants.Communication.SOCKET_CHANNEL);
			frame.loadData();
			String trama = Util.addLengthStartOfString(frame.getString().toString(), properties.getInt("serverSocket.quantityBytesLength"))
					.toString();
			logger.info("Trama a enviar: " + trama);
			if (socketClient.connectSocketUsingRetries() && socketClient.writeDataSocket(trama)) {
				Frame frameRta = leerRespuesta(socketClient);
				frameRta.getList().set(Frame.COMMUNICATION_CHANNEL, ArmsAgentConstants.Communication.PIPE_CHANNEL);
				frameRta.loadData();
				responder(pipe, frameRta, properties);
				socketClient.closeConnection();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	protected void responder(ConnSocketServer socket, Frame frame, PropFile properties) {
		logger.info("Respuesta a enviar: " + frame.toString());	
		socket.writeDataSocket(Util.addLengthStartOfString(frame.getString().toString(),
				properties.getInt("serverSocket.quantityBytesLength")).toString());
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

	protected void responder(ConnPipeServer pipe, Frame frame, PropFile properties) {
		String responseChannel = (String) frame.getHeader().get(Frame.RESPONSE_CHANNEL);
		String pipeResponse = responseChannel.substring(responseChannel.length() - 1);
		String pos = null;
		if (Util.isNumeric(responseChannel.substring(0, 3))) {
			pos = responseChannel.substring(0, 3); // desde el POS 001B = 001
		} else {
			pos = responseChannel.substring(1, 3); // desde el Controlador 0CCB = CC
		}
		String data = Util.addLengthStartOfString(frame.getString().toString(), properties.getInt("serverSocket.quantityBytesLength"));
		logger.info("Respuesta a enviar: " + data);
		pipe.sendData(pipeResponse, pos, data);
	}


}
