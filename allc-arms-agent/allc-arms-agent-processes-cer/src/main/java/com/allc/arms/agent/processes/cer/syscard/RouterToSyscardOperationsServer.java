/**
 * 
 */
package com.allc.arms.agent.processes.cer.syscard;

import java.net.SocketException;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.allc.AllcUtils;
import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.properties.PropFile;

/**
 * @author gustavo
 *
 */
public class RouterToSyscardOperationsServer implements Runnable {
	static Logger log = Logger.getLogger(RouterToSyscardOperationsServer.class);
	private ConnSocketServer socketServer;
	private PropFile properties = null;
	public static boolean isEnd = false;
	public final int BITMAP_2_ACTIVE = 1;
	public final int PRIMARY_ACCOUNT_NUMBER = 2;
	public final int PROCESSING_CODE = 3;
	public final int TRANSACTION_AMOUNT = 4;
	public final int SYSTEM_TRACE_AUDIT_NUMBER = 11;
	public final int LOCAL_TRANSACTION_TIME = 12;
	public final int LOCAL_TRANSACTION_DATE = 13;
	public final int EXPIRATION_DATE = 14;
	public final int POS_ENTRY_MODE = 22;
	public final int NETWORK_INTERNATIONAL_ID = 24;
	public final int POS_CONDITION_CODE = 25;
	public final int TRACK_2_DATA = 35;
	public final int RETRIEVAL_REFERENCE_NUMBER = 37;
	public final int AUTORIZATION_NUMBER = 38;
	public final int RESPONSE_CODE = 39;
	public final int TERMINAL_ID = 41;
	public final int MERCHANT_ID = 42;
	public final int TRACK_1 = 45;
	public final int NUMERO_LOTE_ACTIVO = 61;
	public final int ADICIONAL_DATA = 105;
	public final int ADICIONAL_DATA_2 = 112;
	public final int ADICIONAL_DATA_3 = 114;
	public final int RESERVED_FOR_NATIONAL_USE = 116;
	public final int ADICIONAL_DATA_4 = 120;
	public final int RESERVED_FOR_PRIVATE_USE = 124;
	
	public RouterToSyscardOperationsServer(ConnSocketServer socketServer, PropFile properties) {
		this.socketServer = socketServer;
		try {
			socketServer.getClient().setSoLinger(true, 0);
		} catch (SocketException e) {
			log.error(e.getMessage(), e);
		}
		this.properties = properties;
	}

	public void run() {
		int numBytesToRead;
		String data = "";

		try {
			log.info("Conectado!");
			numBytesToRead = readCountBytesToRead();
//			log.info("Received cant: " + numBytesToRead);
			if (numBytesToRead > AllcUtils.Communication.NUMBER_CERO) {

				data = readData(numBytesToRead);
				if (data != null && !data.equals("")) {
//					log.info("Received data: " + data);
					
					ConnSocketClient socketClient = new ConnSocketClient();
					socketClient.setIpServer(properties.getObject("sendTramaSyscard.server.ip"));
					socketClient.setPortServer(properties.getInt("sendTramaSyscard.server.port"));
					socketClient.setRetries(properties.getInt("sendTramaSyscard.server.retries"));
					socketClient.setTimeOutConnection(properties.getInt("sendTramaSyscard.server.timeOut"));
					socketClient.setTimeOutSleep(properties.getInt("sendTramaSyscard.server.timeSleep"));
					socketClient.setQuantityBytesLength(2);
					if (socketClient.connectSocketUsingRetries()) {
//						log.info("Socket conectado");
						StringBuffer mje = new StringBuffer(data);
						String mjeLength = StringUtils.leftPad(Integer.toHexString(mje.length()), 4, ArmsAgentConstants.Communication.CERO);
//						mje.insert(0, mjeLength);
						byte [] a = new byte[data.length()+2];
						System.arraycopy(strNumToByteAry(mjeLength), 0, a, 0, 2);
						System.arraycopy(data.getBytes(), 0, a, 2, data.getBytes().length);
//						log.info("longitud: "+a.length +" cadena: "+ translateRequest(new String(a)));
						if (socketClient.writeByteArraySocket(a)) {
//							log.info("Mensaje enviado");
							if (!socketClient.timeOutSocket()) {
//								log.info("Leyendo respuesta");
								int cant = socketClient.leeLongitudDataHexaSocket();
//								log.info("Cantidad a leer(dec):" + cant);
								data = socketClient.readDataSocket(cant);
//								log.info("Respuesta recibida:" + data);
								
//								String response = translateResponse(data);
//								respuestaAfirmativa(response);
								mje = new StringBuffer(data);
								mjeLength = StringUtils.leftPad(Integer.toHexString(cant), 4, ArmsAgentConstants.Communication.CERO);
								a = new byte[data.length()+2];
								System.arraycopy(strNumToByteAry(mjeLength), 0, a, 0, 2);
								System.arraycopy(data.getBytes("ISO-8859-1"), 0, a, 2, data.getBytes().length);
//								log.info("longitud: "+a.length +" cadena: "+ translateResponse(new String(a)));
								socketServer.writeByteArraySocket(a);
//									log.info("Respuesta a enviar:" + mje.toString());
							}
						}
						log.info("Cerrando conexión");
						socketClient.closeConnection();
					} else {
						log.info("Socket NO conectado");
					}
				}
			}
		} catch (Exception x) {
			log.error(x.getMessage(), x);
		}
		finally {
			try {
				Thread.sleep(50000);
				closeConn();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	private String translateResponse(String cadena) throws Exception {
		byte[] bytes = cadena.getBytes("ISO-8859-1");
		StringBuffer retorno = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String hexa = Integer.toString((bytes[i] << 0) & 0x000000ff, 16);
			retorno.append(hexa.length() < 2 ? "0" + hexa : hexa);
		}
		log.info("Respuesta: " + retorno.toString());
		return retorno.toString();
	}
	

	private StringBuffer translateHexaToBitmap(String hexa) {
		StringBuffer bitmap = new StringBuffer();
		for (int i = 0; i < hexa.length(); i = i + 2) {
			int decimal = Integer.parseInt(hexa.substring(i, i + 2), 16);
			String bitStr = Integer.toString(decimal, 2);
			bitmap.append(StringUtils.leftPad(bitStr, 8, ArmsAgentConstants.Communication.CERO));
		}
		return bitmap;
	}

	private boolean respuestaAfirmativa(String data) {
		StringBuffer bitmap = translateHexaToBitmap(data.substring(14, 30));
		int pos = 30;
		if (bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') {
			pos += 16;
			bitmap.append(translateHexaToBitmap(data.substring(30, pos)));
		}
		if (bitmap.charAt(PRIMARY_ACCOUNT_NUMBER - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2)).intValue();
			pos = pos + 2 + cantAleer;
		}
		if (bitmap.charAt(PROCESSING_CODE - 1) == '1') {
			pos = pos + 6;
		}
		if (bitmap.charAt(TRANSACTION_AMOUNT - 1) == '1') {
			pos = pos + 12;
		}
		if (bitmap.charAt(SYSTEM_TRACE_AUDIT_NUMBER - 1) == '1') {
			String sysTraceAudNbr = data.substring(pos, pos + 6);
			pos = pos + 6;
		}
		if (bitmap.charAt(LOCAL_TRANSACTION_TIME - 1) == '1') {
			String localTrxTime = data.substring(pos, pos + 6);
			pos = pos + 6;
		}
		if (bitmap.charAt(LOCAL_TRANSACTION_DATE - 1) == '1') {
			pos = pos + 4;
		}
		if (bitmap.charAt(EXPIRATION_DATE - 1) == '1') {
			pos = pos + 4;
		}
		if (bitmap.charAt(POS_ENTRY_MODE - 1) == '1') {
			pos = pos + 4;
		}
		if (bitmap.charAt(NETWORK_INTERNATIONAL_ID - 1) == '1') {
			pos = pos + 4;
		}
		if (bitmap.charAt(POS_CONDITION_CODE - 1) == '1') {
			pos = pos + 2;
		}
		if (bitmap.charAt(TRACK_2_DATA - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2)).intValue();
			pos = pos + 2 + (cantAleer % 2 == 0 ? cantAleer : cantAleer + 1);
		}
		if (bitmap.charAt(RETRIEVAL_REFERENCE_NUMBER - 1) == '1') {
			pos = pos + 24;
		}
		if (bitmap.charAt(AUTORIZATION_NUMBER - 1) == '1') {
			pos = pos + 12;
		}
		if (bitmap.charAt(RESPONSE_CODE - 1) == '1') {
			String responseCode = translateHexaToUtf8(data.substring(pos, pos + 2)) + translateHexaToUtf8(data.substring(pos + 2, pos + 4));
			log.info("Response Code: " + responseCode);
			if (responseCode != null && (responseCode.equals("00") || responseCode.equals("08") || responseCode.equals("11")))
				return true;
		}

		return false;
	}
	

	private String translateHexaToUtf8(String hexa) {
		try {
			byte[] decodedHex = DatatypeConverter.parseHexBinary(hexa);
			return new String(decodedHex, "UTF-8");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private String translateRequest(String cadena) throws Exception {
		byte[] bytes = cadena.getBytes();
		StringBuffer retorno = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String hexa = Integer.toString((bytes[i] << 0) & 0x000000ff, 16);
			retorno.append(hexa.length() < 2 ? "0" + hexa : hexa);
		}
		log.info("Request: " + retorno.toString());
		return retorno.toString();
	}
	
	public byte[] strNumToByteAry(String str) {

		int len = str.length();
		if (len % 2 == 1) {
			str += "0";
			len++;
		}

		byte[] result = new byte[len / 2];
		for (int i = 0, j = 0; i < len; i += 2, j++) {
			result[j] = (byte) Integer.parseInt(str.substring(i, i + 2), 16);
		}
		return result;
	}


	private void closeConn() {
		if (socketServer != null) {
			log.info("Cerrando socket");
			socketServer.closeConnectionServer();
		} 
	}

	private int readCountBytesToRead() {
		if (socketServer != null) {
			return socketServer.leeLongitudDataHexaSocket();
		} 
		return 0;
	}

	private String readData(int countBytesToRead) {
		if (socketServer != null) {
			return socketServer.readDataSocket(countBytesToRead);
		} 
		return "";
	}
}
