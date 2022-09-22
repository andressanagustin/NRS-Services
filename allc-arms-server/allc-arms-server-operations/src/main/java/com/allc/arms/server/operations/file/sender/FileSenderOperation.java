package com.allc.arms.server.operations.file.sender;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class FileSenderOperation extends AbstractOperation {

	protected static Logger log;
	protected static Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	protected String descriptorOperation = "FILE_SEND_O";

	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}
	
	protected void initialize(){
		log = Logger.getLogger(FileSenderOperation.class);
	}

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		String storeNumber;
		try {
			storeNumber = (String) frame.getHeader().get(3);
			initialize();
			UtilityFile.createWriteDataFile(getEyesFileName(properties), descriptorOperation+"|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeNumber+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Iniciando envío de archivo.\n", true);

			String originfileName = (String) frame.getBody().get(0);
			File fileToSend = new File(originfileName);
			String filename = fileToSend.getName();
			String destPath = (String) frame.getBody().get(1);
			String ip = (String) frame.getBody().get(2);
			int port = (Integer.valueOf((String)frame.getBody().get(3))).intValue();
			String storeCode = properties.getObject("eyes.store.code");
			log.info("Archivo a enviar: " + originfileName);
			
			StringBuffer data = getFrameHeader(storeCode);
			data.append(ArmsServerConstants.Communication.FRAME_SEP).append(destPath + File.separator + filename).append(ArmsServerConstants.Communication.FRAME_SEP).append(fileToSend.length());
			List list = Arrays.asList(p.split(data.toString()));

			Frame frameToSend = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
					ArmsServerConstants.Communication.FRAME_SEP);
			ConnSocketClient socketClient = connectClient(properties, ip, port);
			StringBuilder msg = new StringBuilder();
			if (frameToSend.loadData() && socketClient != null) {
				boolean send = sendFileHeader(frameToSend, properties, socketClient) && sendFileBytes(fileToSend, socketClient, properties);
				closeClient(socketClient);
				if (send) {
					log.info("Archivo enviado correctamente.");
					msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("0");
					UtilityFile.createWriteDataFile(getEyesFileName(properties), descriptorOperation+"|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode + "|PRC|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Archivo enviado: " + filename + ".\n", true);
				} else {
					log.error("Error al enviar el archivo.");
					msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("1");
					UtilityFile.createWriteDataFile(getEyesFileName(properties), descriptorOperation+"|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode + "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error al enviar el archivo: " + filename + ".\n", true);
				}
			} else
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("1");
			
			
			String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
			socket.writeDataSocket(tmp);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				storeNumber = (String) frame.getHeader().get(3);
				if (storeNumber == "") storeNumber="000";
				UtilityFile.createWriteDataFile(getEyesFileName(properties), descriptorOperation+"|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeNumber+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al recibir el archivo.\n", true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
		return false;
	}
	
	protected String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	protected StringBuffer getFrameHeader(String storeCode) {
		StringBuffer data = new StringBuffer();
		data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(ArmsServerConstants.Process.FILE_RECEIVER_OPERATION)
				.append(ArmsServerConstants.Communication.FRAME_SEP)
				.append("000")
				.append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(storeCode)
				.append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(ArmsServerConstants.Communication.TEMP_CONN)
				.append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
		return data;
	}
	
	protected ConnSocketClient connectClient(PropFile properties, String ip, int port) {
		ConnSocketClient socketClient = new  ConnSocketClient();
		socketClient.setIpServer(ip);
		socketClient.setPortServer(port);
		socketClient.setRetries(properties.getInt("clientSocket.retries"));
		socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
		socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
		socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
		if(socketClient.connectSocketUsingRetries())
			return socketClient;
		else
			return null;
	}

	protected void closeClient(ConnSocketClient socketClient) {
		if (socketClient != null)
			socketClient.closeConnection();
	}
	
	protected boolean sendFileHeader(Frame frame, PropFile properties, ConnSocketClient socketClient) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
			log.info("Trama a enviar: " + trama);
			if (socketClient.writeDataSocket(trama)) {
				int numberOfBytes = 0;
				int timeOutCycles = 0;
				while (numberOfBytes == 0) {
					numberOfBytes = socketClient.readLengthDataSocket();
					if (timeOutCycles == 5) {
						// cada 5 timeouts escribimos una trama vacía para
						// asegurarnos que el socket esté activo
						String mje = Util.addLengthStartOfString("", qtyBytesLength).toString();
						if (!socketClient.writeDataSocket(mje)) {
							socketClient.setConnected(false);
							return false;
						}
						timeOutCycles = 0;
					}
					timeOutCycles++;
				}
				if (numberOfBytes > 0) {
					str = socketClient.readDataSocket(numberOfBytes);
					if (StringUtils.isNotBlank(str)) {
						list = Arrays.asList(p.split(str));
						frameRpta = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
								ArmsServerConstants.Communication.FRAME_SEP);
						log.info("Respuesta recibida: " + frameRpta.toString());
						if (frameRpta.getStatusTrama() == 0) {
							return true;
						}
					}
				}
			} else {
				socketClient.setConnected(false);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			socketClient.setConnected(false);
		}
		return false;
	}
	
	protected boolean sendFileBytes(File fileToSend, ConnSocketClient socketClient, PropFile properties) {
		try {
			int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSend));
			long totalBytesToRead = fileToSend.length();
			byte[] byteArray = new byte[(int) (totalBytesToRead < 8192 ? totalBytesToRead : 8192)];
			
			while (bis.read(byteArray) != -1 && totalBytesToRead > 0) {
				socketClient.writeByteArraySocket(byteArray);
				totalBytesToRead = totalBytesToRead - 8192;
				if(totalBytesToRead < 8192 && totalBytesToRead > 0)
					byteArray = new byte[(int) totalBytesToRead];
			}
			bis.close();
			int numberOfBytes = 0;
			int timeOutCycles = 0;
			while (numberOfBytes == 0) {
				numberOfBytes = socketClient.readLengthDataSocket();
				if (timeOutCycles == 5) {
					// cada 5 timeouts escribimos una trama vacía para
					// asegurarnos que el socket esté activo
					String mje = Util.addLengthStartOfString("", qtyBytesLength).toString();
					if (!socketClient.writeDataSocket(mje)) {
						socketClient.setConnected(false);
						return false;
					}
					timeOutCycles = 0;
				}
				timeOutCycles++;
			}
			if (numberOfBytes > 0) {
				String str = socketClient.readDataSocket(numberOfBytes);
				if (StringUtils.isNotBlank(str)) {
					List list = Arrays.asList(p.split(str));
					Frame frameRpta = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER, ArmsServerConstants.Communication.FRAME_SEP);
					log.info("Respuesta recibida: " + frameRpta.toString());
					if (frameRpta.getStatusTrama() == 0) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			socketClient.setConnected(false);
		}
		return false;
	}

	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}
	

}
