package com.allc.arms.agent.processes.cer.codopera;

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
import com.allc.files.helper.FilesHelper;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileFilter4690;
import com.ibm.OS4690.FlexosException;

public class UpdateCodeOperaProcess extends AbstractProcess{
	
	private static Logger log = Logger.getLogger(UpdateCodeOperaProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	private boolean isEnd = false;
	protected boolean finished = false;
	private Iterator filesToProcess = null;
	private static Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
	private File4690 inFolder;
	private int sleepTime;
	private String store;
	protected ConnSocketClient socketClient;
	
	protected void init() {
		try {
			inFolder = new File4690(properties.getObject("updateCodOpera.in.folder.path"));
			inFolder.mkdirs();
			(new File4690(properties.getObject("updateCodOpera.out.folder.path"))).mkdirs();
			sleepTime = properties.getInt("updateCodOpera.sleepTime");

			ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
			store = (new Integer(controllerStatusData.getStoreNumber())).toString();
			while(store.length() < 3)
				store = "0" + store;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private String getEyesFileName(){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
	
	public void run() {
		log.info("Iniciando Update CodOpera Process...");
		init();
		String codOperaName = null;
		while (!isEnd) {
			try {
				File4690 codOperaFile = getNextCodOperaFile();
				if (codOperaFile != null) {
					codOperaName = codOperaFile.getName();
					Files.creaEscribeDataArchivo4690(getEyesFileName(), "UPD_CODOPERA_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Iniciando procesamiento de: "+codOperaName+".\n", true);
					log.info("Archivo a procesar: " + codOperaFile.getName());
					FilesHelper.copyFile4690(properties.getObject("updateCodOpera.in.folder.path"),
							properties.getObject("updateCodOpera.out.folder.path"), codOperaFile.getName(), codOperaFile.getName());
					log.info("Archivo copiado a: " + properties.getObject("updateCodOpera.out.folder.path"));
					//String folder = properties.getObject("updateCodOpera.in.folder.path") + "/" + codOperaFile.getName();
					//log.info("FOLDER: " + folder);
					short priority = 5;
					try {
						ControllerApplicationServices.startBackgroundApplication(properties.getObject("updateCodOpera.background"),
								codOperaFile.getName().getBytes(), "Iniciando actualizaci???n CodOpera", priority);
						log.info("Background codOpera iniciada.");
					} catch (FlexosException fe) {
						log.error(fe.getMessage(), fe);
					}
					Thread.sleep(1000);
					StringBuffer data = new StringBuffer();
					data.append("S").append(ArmsAgentConstants.Communication.FRAME_SEP)
							.append(ArmsAgentConstants.Process.UPDATE_COD_OPERA)
							.append(ArmsAgentConstants.Communication.FRAME_SEP).append("000")
							.append(ArmsAgentConstants.Communication.FRAME_SEP).append(store)
							.append(ArmsAgentConstants.Communication.FRAME_SEP).append(ArmsAgentConstants.Communication.TEMP_CONN)
							.append(ArmsAgentConstants.Communication.FRAME_SEP)
							.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
							.append(ArmsAgentConstants.Communication.FRAME_SEP).append(codOperaFile.getName());
					List list = Arrays.asList(p.split(data.toString()));

					Frame frame = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
							ArmsAgentConstants.Communication.FRAME_SEP);
					if (frame.loadData()) {
						boolean send = sendCodOperaFiles(frame);
						closeClient();
						if (send) {
							try{
								File4690 fileToDel = new File4690(properties.getObject("updateCodOpera.in.folder.path"),codOperaFile.getName());
								fileToDel.delete();
								log.info("Archivo borrado.");
							} catch (Exception e) {
								log.error(e.getMessage(), e);
							}
							Files.creaEscribeDataArchivo4690(getEyesFileName(), "UPD_CODOPERA_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Finaliza el procesamiento del archivo: "+codOperaName+".\n", true);
							log.info("Archivo procesado correctamente.");
						} else {
							Files.creaEscribeDataArchivo4690(getEyesFileName(), "UPD_CODOPERA_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo informar al servidor sobre: "+codOperaName+".\n", true);
							log.error("Error al informar al server.");
						}
					}
				}
			} catch (Exception e) {
				try {
					Files.creaEscribeDataArchivo4690(getEyesFileName(), "UPD_CODOPERA_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el archivo: "+codOperaName+".\n", true);
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
				log.error(e.getMessage(), e);
			}
		}
		finished = true;
	}
	
	protected boolean connectClient() {
		if (socketClient == null) {
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(properties.getObject("clientSocket.ip"));
			socketClient.setPortServer(properties.getInt("clientSocket.port"));
			socketClient.setRetries(properties.getInt("clientSocket.retries"));
			socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}

	protected void closeClient() {
		if (socketClient != null)
			socketClient.closeConnection();
	}
	
	protected boolean sendCodOperaFiles(Frame frame) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected())
				connectClient();
			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
			log.info("Trama a enviar: " + trama);
			if (socketClient.writeDataSocket(trama)) {
				int numberOfBytes = 0;
				int timeOutCycles = 0;
				while (numberOfBytes == 0) {
					numberOfBytes = socketClient.readLengthDataSocket();
					if (timeOutCycles == 5) {
						// cada 5 timeouts escribimos una trama vac??a para
						// asegurarnos que el socket est?? activo
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
						frameRpta.loadData();
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

	private File4690 getNextCodOperaFile() {
		log.info("Buscando archivos para actualizacion CodOpera");
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				File4690[] files = inFolder.listFiles(new FileFilter4690() {
					public boolean accept(File4690 pathname) {
						log.debug("Pathname: " + pathname.getName().toUpperCase());
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

	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo UpdateMotoProcess...");
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
		log.info("Finaliz?? el Proceso de Actualizaci??n de Maestro de Moto.");
		return true;
	}

}
