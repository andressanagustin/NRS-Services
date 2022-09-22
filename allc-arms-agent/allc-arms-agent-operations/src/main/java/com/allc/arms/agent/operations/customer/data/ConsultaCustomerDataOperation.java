/**
 * 
 */
package com.allc.arms.agent.operations.customer.data;

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
public class ConsultaCustomerDataOperation extends AbstractOperation {
	private Logger logger = Logger.getLogger(ConsultaCustomerDataOperation.class);
	private Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);

	/**
	 * 
	 */
	public ConsultaCustomerDataOperation() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.allc.core.operation.AbstractOperation#shutdown(long)
	 */
	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.allc.core.operation.AbstractOperation#process(com.allc.comm.socket.ConnSocketServer, com.allc.comm.frame.Frame,
	 * com.allc.properties.PropFile)
	 */
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		logger.info("Consulta Customer Data Operation iniciada...");
		String str;
		try {
			Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_CUDA_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Consultando Datos del cliente: "+frame.getBody().get(0)+".\n", true);
			ConnSocketClient socketClient = new ConnSocketClient();
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(properties.getObject("clientSocket.ip"));
			socketClient.setPortServer(properties.getInt("clientSocket.port"));
			socketClient.setRetries(properties.getInt("clientSocket.retries"));
			socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
			String trama = Util.addLengthStartOfString(frame.getString().toString(), properties.getInt("serverSocket.quantityBytesLength"));
			logger.info("Trama a enviar: " + trama);
			boolean clienteEncontrado = false;
			if (socketClient.connectSocketUsingRetries() && socketClient.writeDataSocket(trama)) {
				Frame frameRta = leerRespuesta(socketClient);
				frameRta.loadData();
				if (frameRta.getStatusTrama() == 0) {
					clienteEncontrado = true;
				}
				socketClient.closeConnection();
				str = Util.addLengthStartOfString(frameRta.getString().toString(), properties.getInt("serverSocket.quantityBytesLength"));
				logger.info("Respuesta a enviar: " + str);
				socket.writeDataSocket(str);
				if(clienteEncontrado)
					Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_CUDA_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Datos del Cliente: "+frame.getBody().get(0)+" encontrado.\n", true);
				else
					Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_CUDA_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Datos del Cliente: "+frame.getBody().get(0)+" no encontrado.\n", true);
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_CUDA_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No hay conexión con el server para consultar Datos del Cliente: "+frame.getBody().get(0)+".\n", true);
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_CUDA_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al buscar Datos del Cliente: "+frame.getBody().get(0)+".\n", true);
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
		int numberOfBytes = 0;
		while(numberOfBytes == 0)
			numberOfBytes = socketClient.readLengthDataSocket();
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
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.allc.core.operation.AbstractOperation#process(com.allc.comm.pipe.ConnPipeServer, com.allc.comm.frame.Frame,
	 * com.allc.properties.PropFile)
	 */
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {

		return false;
	}

}
