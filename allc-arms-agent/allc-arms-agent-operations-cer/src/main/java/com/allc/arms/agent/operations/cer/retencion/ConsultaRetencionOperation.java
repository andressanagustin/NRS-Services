/**
 * 
 */
package com.allc.arms.agent.operations.cer.retencion;

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

/**
 * @author gustavo
 *
 */
public class ConsultaRetencionOperation extends AbstractOperation {
	static Logger log = Logger.getLogger(ConsultaRetencionOperation.class);
	static Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		log.info("Consulta Retención Operation iniciada...");

		try {
			Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_RET_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Consultando Factura: "+frame.getBody().get(0)+".\n", true);
			ConnSocketClient socketClient = new ConnSocketClient();
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(properties.getObject("clientSocket.ip"));
			socketClient.setPortServer(properties.getInt("clientSocket.port"));
			socketClient.setRetries(properties.getInt("clientSocket.retries"));
			socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
			String trama = Util.addLengthStartOfString(frame.getString().toString(), properties.getInt("serverSocket.quantityBytesLength")).toString();
			log.info("Trama a enviar: " + trama);
			boolean factEncontrada = false;
			if (socketClient.connectSocketUsingRetries() && socketClient.writeDataSocket(trama)) {
				Frame frameRta = leerRespuesta(socketClient);
				frameRta.loadData();
				if (frameRta.getBody().get(0).equals("1")) {
					factEncontrada = true;
				}
				responder(socket, frameRta, properties);
				socketClient.closeConnection();
				if(factEncontrada)
					Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_RET_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Factura: "+frame.getBody().get(0)+" encontrada.\n", true);
				else
					Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_RET_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Factura: "+frame.getBody().get(0)+" no permite retención.\n", true);
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_RET_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No hay conexión con el server para consultar la Factura: "+frame.getBody().get(0)+".\n", true);
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_RET_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al buscar la Factura: "+frame.getBody().get(0)+".\n", true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
			log.error(e.getMessage(), e);
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
				log.info("Respuesta recibida: " + frameRpta.toString());
				return frameRpta;
			}
		}
		if( numberOfBytes == 0)
			numberOfBytes = socketClient.readLengthDataSocket();
		
		log.info("No se recibio respuesta.");
		return null;
	}

	protected void responder(ConnSocketServer socket, Frame frame, PropFile properties) {
		if (socket.writeDataSocket(Util.addLengthStartOfString(frame.getString().toString(), properties.getInt("serverSocket.quantityBytesLength")).toString()))
			log.info("Respuesta a enviar: " + frame.toString());
	}

	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

}
