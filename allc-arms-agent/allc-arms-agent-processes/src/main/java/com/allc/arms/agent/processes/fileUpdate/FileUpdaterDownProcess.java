package com.allc.arms.agent.processes.fileUpdate;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
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
import com.ibm.OS4690.RandomAccessFile4690;


/**
 * Proceso encargado de actualizar los archivos que provienen del Servidor. Es decir, que actualiza desde el disco F hacia el C.
 * 
 * @author gustavo
 *
 */
public class FileUpdaterDownProcess extends AbstractProcess {
	protected Logger log = Logger.getLogger(FileUpdaterDownProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	protected long timeSleep;
	protected File4690 inFolder;
	protected boolean isEnd = false;
	protected boolean finished = false;
	private String store;
	protected ConnSocketClient socketClient;
	private static Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
	
	protected boolean init() {
		boolean result = false;
		try {
			ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
			store = (new Integer(controllerStatusData.getStoreNumber())).toString();
			inFolder = new File4690(properties.getObject("fileUpdaterDown.in.folder.path"));
			inFolder.mkdir();
			timeSleep = Long.parseLong(properties.getObject("fileUpdaterDown.timeSleep").toString());
			result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}


	public void run() {
		log.info("Iniciando File Updater Down Process...");
		init();
		while (!isEnd) {
			File4690 updateFile = null;
			try {
				File4690[] list = getFilesList();
				int size = list.length;
				for(int i = 0; i < size; i++){
					updateFile = list[i];
					Files.creaEscribeDataArchivo4690(getEyesFileName(), "FILE_UPD_D_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Iniciando procesamiento de: "+ updateFile.getName()+".\n", true);
					log.info("Archivo a procesar: " + updateFile.getName());
					String cmdFileName = inFolder.getAbsolutePath()+File.separator +updateFile.getName().split("\\.")[0]+".CMD";
					log.info("Archivo de comandos: " + cmdFileName);
					if(!(new File4690(cmdFileName)).exists()){
						log.info("Archivo de comandos: " + cmdFileName + "no existe.");
						Files.creaEscribeDataArchivo4690(getEyesFileName(), "FILE_UPD_D_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo actualizar el archivo: "+ updateFile.getName()+".\n", true);
					} else {
						String originDir = readDirPath(cmdFileName);
						if(originDir == null){
							log.info("Archivo de comandos: " + cmdFileName + "no contiene ruta.");
							Files.creaEscribeDataArchivo4690(getEyesFileName(), "FILE_UPD_D_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo actualizar el archivo: "+ updateFile.getName()+".\n", true);
						} else {
							File4690 originFile = new File4690(originDir+File.separator+updateFile.getName());
							boolean needUpdate = true; //!comparator(originFile, updateFile);
							if(needUpdate){
								try {
									//log.info("Params: "+inFolder.getAbsolutePath()+", "+originDir+", "+updateFile.getName()+", "+originFile.getName());
									originFile.delete();
									FilesHelper.copyFile4690(inFolder.getAbsolutePath(), originDir, updateFile.getName(), originFile.getName());
									//log.info("Archivo: " +originDir+File.separator +originFile.getName() + " actualizado");
									if(existCommands(cmdFileName)){
										//log.info("Existe comando");
										int c = 2;
										String cmd = readCommand(cmdFileName, c);
										while (cmd != null && !cmd.trim().isEmpty()) {
											
											short priority = 5;
											try {
												log.info("BG: " + properties.getObject("fileUpdaterDown.background"));
												ControllerApplicationServices.startBackgroundApplication(properties.getObject("fileUpdaterDown.background"),
														cmd.getBytes(), "Iniciando ejecuci�n de comando", priority);
												log.info("Background iniciada.");
											} catch (FlexosException fe) {
												log.error(fe.getMessage(), fe);
											}
											//Runtime.getRuntime().exec(cmd);
											log.info("Comando ejecutado: "+ cmd);
											c++;
											cmd = readCommand(cmdFileName, c);
										}
									}
		
									File4690 fileCMD = new File4690(cmdFileName);
									Files.creaEscribeDataArchivo4690(getEyesFileName(), "FILE_UPD_D_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Finalizó el procesamiento del archivo: "+ updateFile.getName()+".\n", true);
									StringBuffer data = new StringBuffer();
									data.append("S").append(ArmsAgentConstants.Communication.FRAME_SEP)
											.append(ArmsAgentConstants.Process.FILE_UPDATER_DOWN_PROCESS)
											.append(ArmsAgentConstants.Communication.FRAME_SEP).append("000")
											.append(ArmsAgentConstants.Communication.FRAME_SEP).append(store)
											.append(ArmsAgentConstants.Communication.FRAME_SEP).append(ArmsAgentConstants.Communication.TEMP_CONN)
											.append(ArmsAgentConstants.Communication.FRAME_SEP)
											.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
											.append(ArmsAgentConstants.Communication.FRAME_SEP).append(updateFile.getName())
											.append(ArmsAgentConstants.Communication.FRAME_SEP).append(fileCMD.getName());
									
									List list2 = Arrays.asList(p.split(data.toString()));
		
									Frame frame = new Frame(list2, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
											ArmsAgentConstants.Communication.FRAME_SEP);
									if (frame.loadData()) {
										boolean send = sendFiles(frame);
										closeClient();
										if (send) {
											updateFile.delete();
											fileCMD.delete();
											log.info("Archivo procesado correctamente.");
										} else {
											log.error("Error al informar al server.");
										}
									}
									//if(fileCMD.exists())
										//fileCMD.delete();
								} catch (Exception e){
									log.error(e.getMessage(), e);
									Files.creaEscribeDataArchivo4690(getEyesFileName(), "FILE_UPD_D_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo actualizar el archivo: "+ updateFile.getName()+".\n", true);
								}
							}
						}
					}
					//updateFile.delete();
				}
				try {
					Thread.sleep(timeSleep);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			} catch (Exception e) {
				try {
					Files.creaEscribeDataArchivo4690(getEyesFileName(), "FILE_UPD_D_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el archivo: "+ (updateFile != null ? updateFile.getName() : "") + ".\n", true);
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
	
	private File4690[] getFilesList() {
//		log.info("Buscando archivos para actualizar...");
		File4690[] files = inFolder.listFiles(new FileFilter4690() {
			public boolean accept(File4690 pathname) {
				return pathname.isFile() && !pathname.getName().toUpperCase().endsWith(".CMD") && !pathname.getName().toUpperCase().endsWith(".cmd") && !pathname.getName().toUpperCase().startsWith("TMP");
			}
		});
		if (files.length != 0) {
			Arrays.sort(files, new Comparator() {
				public int compare(Object f1, Object f2) {
                    return (-1) * ((File4690) f1).getName().compareTo(((File4690) f2).getName());
                }
			});
		}
		return files;
	}
	
	private boolean comparator(File4690 originFile, File4690 updateFile){
//		log.info("OriginL: "+originFile.length());
//		log.info("UpdateL: "+updateFile.length());
//		log.info("OriginLM: "+originFile.lastModified());
//		log.info("UpdateLM: "+updateFile.lastModified());
		if(originFile.length() != updateFile.length() || originFile.lastModified() != updateFile.lastModified())
			return false;
		return true;
	}
	
	private String readDirPath(String cdmFile){
		String linea = null;
		try {
			RandomAccessFile4690 randFileRead = new RandomAccessFile4690(cdmFile, "r");
			randFileRead.seek(0);
			linea = randFileRead.readLine();
			randFileRead.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return linea;
	}
	
	private boolean existCommands(String cdmFile){
		String linea = null;
		try {
			RandomAccessFile4690 randFileRead = new RandomAccessFile4690(cdmFile, "r");
			randFileRead.seek(0);
			randFileRead.readLine();
			//nos interesa saber si hay un comando en la segunda linea
			linea = randFileRead.readLine();
			randFileRead.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		if(linea != null)
			return true;
		return false;
	}
	
	private String readCommand(String cdmFile, long pos){
		String linea = null;
		try {
			RandomAccessFile4690 randFileRead = new RandomAccessFile4690(cdmFile, "r");
			randFileRead.seek(0);
			for(int i = 0; i < pos; i++)
				linea = randFileRead.readLine();
			randFileRead.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return linea;
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
	
	protected boolean sendFiles(Frame frame) {
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
	
	protected void closeClient() {
		if (socketClient != null)
			socketClient.closeConnection();
	}
	
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo FileUpdaterDownProcess...");
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
		return true;
	}


}
