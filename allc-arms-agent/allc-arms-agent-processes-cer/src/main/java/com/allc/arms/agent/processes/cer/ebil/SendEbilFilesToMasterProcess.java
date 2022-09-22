package com.allc.arms.agent.processes.cer.ebil;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileFilter4690;
import com.ibm.OS4690.FileInputStream4690;

public class SendEbilFilesToMasterProcess extends AbstractProcess {
	
	protected Logger log = Logger.getLogger(SendEbilFilesToMasterProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	private Iterator filesToProcess = null;
	private File4690 inFolder;
	private int sleepTime;
	private String store;
	private static Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
	private boolean isEnd = false;
	protected boolean finished = false;
	protected ConnSocketClient socketClient;
	protected String descriptorProcess = "EBIL_SEND_P";
	private String idMaster;
	
	protected void init() {
		try {
			inFolder = new File4690(properties.getObject("updateEbil.in.folder"));
			inFolder.mkdir();
			
			sleepTime = properties.getInt("updateItem.sleepTime");

			ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
			idMaster = controllerStatusData.getMasterControllerId();
			store = (new Integer(controllerStatusData.getStoreNumber())).toString();
			while(store.length() < 3)
				store = "0" + store;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void run() {
		log.info("Iniciando SendEbilFilesToMasterProcess...");
		init();
		while (!isEnd) {
			try {
				File4690 ebilFile = getNextEbilFile();
				log.info("Archivo Ebil a enviar: " + ebilFile.getName());
				
				StringBuffer data = getFrameHeader(store);
				data.append(ArmsAgentConstants.Communication.FRAME_SEP).append(inFolder + File.separator + ebilFile.getName()).append(ArmsAgentConstants.Communication.FRAME_SEP).append(ebilFile.length());
				List list = Arrays.asList(p.split(data.toString()));

				Frame frameToSend = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
						ArmsAgentConstants.Communication.FRAME_SEP);
				
				if (frameToSend.loadData()) {
					boolean send = sendFileHeader(frameToSend) && sendFileBytes(ebilFile);
					closeClient(socketClient);
					if (send) {
						log.info("Archivo enviado correctamente.");
						Files.creaEscribeDataArchivo4690(getEyesFileName(), descriptorProcess+"|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store + "|PRC|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Archivo enviado: " + ebilFile.getName() + ".\n", true);
						ebilFile.delete();
					} else {
						log.error("Error al enviar el archivo.");
						Files.creaEscribeDataArchivo4690(getEyesFileName(), descriptorProcess+"|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store + "|ERR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error al enviar el archivo: " + ebilFile.getName() + ".\n", true);
					}
				} else{
					log.error("Error al enviar el archivo.");
					Files.creaEscribeDataArchivo4690(getEyesFileName(), descriptorProcess+"|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store + "|ERR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Error al enviar el archivo: " + ebilFile.getName() + ".\n", true);
				}
				
			} catch (Exception e) {
				try {
					Files.creaEscribeDataArchivo4690(getEyesFileName(),descriptorProcess+"|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el archivo.\n", true);
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
				log.error(e.getMessage(), e);
			}
		}
		finished = true;
	}
	
	private String getEyesFileName(){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
	
	protected boolean connectClient(String ip, int port) {
		
		if (socketClient == null || !socketClient.isConnected()) {
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(ip);
			socketClient.setPortServer(port);
			socketClient.setRetries(properties.getInt("clientSocket.retries"));
			socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}
	
	protected void closeClient(ConnSocketClient socketClient) {
		if (socketClient != null)
			socketClient.closeConnection();
	}
	
	private File4690 getNextEbilFile() {
		log.info("Buscando archivos de ebil para enviar al maestro: " + store);
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				File4690[] files = inFolder.listFiles(new FileFilter4690() {
					public boolean accept(File4690 pathname) {
						return pathname.isFile();
					}
				});
				if (files.length == 0) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						log.error(e.getMessage(), e);
					}
				} else {
					Arrays.sort(files, new Comparator() {
						public int compare(Object obj1, Object obj2) {
							String name1 = ((File4690) obj1).getName().toUpperCase();
							int sequence1 = 0;
							String name2 = ((File4690) obj2).getName().toUpperCase();
							int sequence2 = 0;
							sequence1 = Integer.parseInt(name1.substring(0, 8));
							sequence2 = Integer.parseInt(name2.substring(0, 8));

							if (sequence1 == sequence2) {
								return 0;
							}
							if (sequence1 < sequence2) {
								return -1;
							}
							return 1;
						}
					});
					this.filesToProcess = Arrays.asList(files).iterator();
				}
			} while (((this.filesToProcess == null) || !filesToProcess.hasNext()));
		}
		return (File4690) this.filesToProcess.next();
	}
	
	protected StringBuffer getFrameHeader(String storeCode) {
		StringBuffer data = new StringBuffer();
		data.append("S").append(ArmsAgentConstants.Communication.FRAME_SEP)
				.append(ArmsAgentConstants.Process.EBIL_MASTER_RECEIVER_OPERATION)
				.append(ArmsAgentConstants.Communication.FRAME_SEP)
				.append("000")
				.append(ArmsAgentConstants.Communication.FRAME_SEP)
				.append(storeCode)
				.append(ArmsAgentConstants.Communication.FRAME_SEP)
				.append(ArmsAgentConstants.Communication.TEMP_CONN)
				.append(ArmsAgentConstants.Communication.FRAME_SEP)
				.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
		return data;
	}
	
	protected boolean sendFileHeader(Frame frame) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected())
				connectClient(idMaster, 2040);
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
						frameRpta = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
								ArmsAgentConstants.Communication.FRAME_SEP);
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
	
	protected boolean sendFileBytes(File4690 fileToSend) {
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream4690(fileToSend));
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
			while (numberOfBytes == 0)
				numberOfBytes = socketClient.readLengthDataSocket();
			if (numberOfBytes > 0) {
				String str = socketClient.readDataSocket(numberOfBytes);
				if (StringUtils.isNotBlank(str)) {
					List list = Arrays.asList(p.split(str));
					Frame frameRpta = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER, ArmsAgentConstants.Communication.FRAME_SEP);
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

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo SendEbilFilesToMasterProcess...");
		while (!finished)
			try {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= timeToWait) {
					return false;
				}
				Thread.sleep(500);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		log.info("Finalizó el Proceso de Envio de Ebil al Maestro.");
		return true;
	}

}
