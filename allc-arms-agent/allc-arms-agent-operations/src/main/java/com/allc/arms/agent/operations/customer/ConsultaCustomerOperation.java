/**
 * 
 */
package com.allc.arms.agent.operations.customer;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.customer.Activity;
import com.allc.arms.utils.customer.Customer;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.thoughtworks.xstream.XStream;

/**
 * @author gustavo
 *
 */
public class ConsultaCustomerOperation extends AbstractOperation {
	private Logger logger = Logger.getLogger(ConsultaCustomerOperation.class);
	private Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);

	/**
	 * 
	 */
	public ConsultaCustomerOperation() {
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
		logger.info("Consulta Customer Operation iniciada...");
		String str;
		String codeAnsw;
		CustomerEnrollmentKeyed customerEnrollmentKeyed = new CustomerEnrollmentKeyed();
		CustomerActivityKeyed customerActivityKeyed = new CustomerActivityKeyed();
		try {
			Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_CUST_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Consultando cliente: "+frame.getBody().get(0)+".\n", true);
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
					codeAnsw = "0";
					String customerXML = (String) frameRta.getBody().get(1);
					XStream xstream = new XStream();
					xstream.alias("Customer", Customer.class);
					xstream.alias("Activity", Activity.class);
					Customer customer = (Customer) xstream.fromXML(customerXML);

					if (customerEnrollmentKeyed.process(customer).equals(Boolean.TRUE)) {
						logger.info("record added customer " + customer.getCustomerId());
						if (customerActivityKeyed.process(customer).equals(Boolean.TRUE))
							logger.info("added activity " + customer.getCustomerId());
					}
					clienteEncontrado = true;
				} else {
					codeAnsw = "1";
				}
				socketClient.closeConnection();
				str = Util.addLengthStartOfString(frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + codeAnsw,
						properties.getInt("serverSocket.quantityBytesLength"));
				logger.info("Respuesta a enviar: " + str);
				socket.writeDataSocket(str);
				if(clienteEncontrado)
					Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_CUST_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Cliente: "+frame.getBody().get(0)+" encontrado.\n", true);
				else
					Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_CUST_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Cliente: "+frame.getBody().get(0)+" no encontrado.\n", true);
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_CUST_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No hay conexión con el server para consultar el Cliente: "+frame.getBody().get(0)+".\n", true);
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_CUST_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al buscar el Cliente: "+frame.getBody().get(0)+".\n", true);
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
		logger.info("Consulta Customer Operation iniciada...");
		String codeAnsw;
		CustomerEnrollmentKeyed customerEnrollmentKeyed = new CustomerEnrollmentKeyed();
		CustomerActivityKeyed customerActivityKeyed = new CustomerActivityKeyed();
		try {
			customerActivityKeyed.init(properties);
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
				if (frameRta.getStatusTrama() == 0) {
					codeAnsw = "0";
					String customerXML = (String) frameRta.getBody().get(1);
					XStream xstream = new XStream();
					xstream.alias("Customer", Customer.class);
					xstream.alias("Activity", Activity.class);
					Customer customer = (Customer) xstream.fromXML(customerXML);

					if (customerEnrollmentKeyed.process(customer).equals(Boolean.TRUE)) {
						logger.info("record added customer " + customer.getCustomerId());
						if (customerActivityKeyed.process(customer).equals(Boolean.TRUE))
							logger.info("added activity " + customer.getCustomerId());
					}
				} else {
					codeAnsw = "-1";
				}
				socketClient.closeConnection();
				frame.getList().set(Frame.COMMUNICATION_CHANNEL, ArmsAgentConstants.Communication.PIPE_CHANNEL);
				frame.loadData();
				StringBuffer sb = new StringBuffer(frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + codeAnsw);
				String responseChannel = (String) frame.getHeader().get(Frame.RESPONSE_CHANNEL);
				String pipeResponse = responseChannel.substring(responseChannel.length() - 1);
				String pos = null;
				if (Util.isNumeric(responseChannel.substring(0, 3))) {
					pos = responseChannel.substring(0, 3); // desde el POS 001B = 001
				} else {
					pos = responseChannel.substring(1, 3); // desde el Controlador 0CCB = CC
				}
				String data = Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength"));
				logger.info("Respuesta a enviar: " + data);
				pipe.sendData(pipeResponse, pos, data);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

}
