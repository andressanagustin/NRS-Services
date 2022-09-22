/**
 * 
 */
package com.allc.socket.tester;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.allc.comm.socket.ConnSocketClient;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.JAttachMsgSenderImpl.Sender;

/**
 * @author gustavo
 *
 */
public class SocketClient {

	/**
	 * 
	 */
	public SocketClient() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		Logger log = Logger.getLogger(SocketClient.class);
		try {
			PropFile properties = PropFile.getInstance("configurator.properties");
			int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
			String msg = properties.getObject("clientSocket.msgToSend");
			ConnSocketClient socketClient = new ConnSocketClient();
			socketClient.setIpServer(properties.getObject("clientSocket.ip"));
			socketClient.setPortServer(properties.getInt("clientSocket.port"));
			socketClient.setRetries(properties.getInt("clientSocket.retries"));
			socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
			socketClient.connectSocketUsingRetries();

			//sendPlmsMsg(socketClient, msg, log);
			sendAlliancesMsg(socketClient, msg, qtyBytesLength, log);
			//sendPinpadMsg(socketClient, msg, qtyBytesLength, log);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public static void sendPinpadMsg(ConnSocketClient socketClient, String msg, int qtyBytesLength, Logger log){
		log.info("Trama a enviar: " + msg);
		if (socketClient.writeDataSocket(msg)) {
			int numberOfBytes = 0;
			while (numberOfBytes == 0){
				numberOfBytes = Integer.parseInt(socketClient.readDataSocket(4), 16);
			}
			if (numberOfBytes > 0) {
				log.info("Cant bytes a leer: " + numberOfBytes);
				String str = socketClient.readDataSocket(numberOfBytes);
				log.info("Respuesta recibida: " + str);
			}
		} else {
			socketClient.setConnected(false);
		}
	}
	
	public static void sendAlliancesMsg(ConnSocketClient socketClient, String msg, int qtyBytesLength, Logger log){
		String trama = Util.addLengthStartOfString(msg, qtyBytesLength).toString();
		log.info("Trama a enviar: " + trama);
		if (socketClient.writeDataSocket(trama)) {
			int numberOfBytes = 0;
			while (numberOfBytes == 0)
				numberOfBytes = socketClient.readLengthDataSocket();
			if (numberOfBytes > 0) {
				log.info("Cant bytes a leer: " + numberOfBytes);
				String str = socketClient.readDataSocket(numberOfBytes);
				log.info("Respuesta recibida: " + str);
			}
		} else {
			socketClient.setConnected(false);
		}
	}
	

	public static void sendPlmsMsg(ConnSocketClient socketClient, String msg, Logger log){
		log.info("Trama a enviar: " + msg);
		if (socketClient.writeDataSocket(msg)) {
			int numberOfBytes = 100;
				log.info("Cant bytes a leer: " + numberOfBytes);
				String str = socketClient.readDataSocket(numberOfBytes);
				log.info("Respuesta recibida: " + str);
		} else {
			socketClient.setConnected(false);
		}
	}
}
