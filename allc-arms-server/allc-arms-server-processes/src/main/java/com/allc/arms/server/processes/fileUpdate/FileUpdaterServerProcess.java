package com.allc.arms.server.processes.fileUpdate;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.params.ParamValue;
import com.allc.arms.server.params.ParamsDAO;
import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.Files;
import com.allc.files.helper.FilesHelper;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FlexosException;
import com.ibm.OS4690.RandomAccessFile4690;


/**
 * Proceso encargado de actualizar los archivos que provienen del Controlador. Es decir, que actualiza archivos en el Servidor
 * 
 * @author gustavo
 *
 */
public class FileUpdaterServerProcess extends AbstractProcess {
	protected Logger log = Logger.getLogger(FileUpdaterServerProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	protected long timeSleep;
	private Iterator filesToProcess = null;
	protected boolean isEnd = false;
	protected boolean finished = false;
	protected Store centralStore;
	protected String storeCode;
	protected HashMap activeStoreIp = new HashMap();
	protected List pathActiveStore = new ArrayList();
	protected ConnSocketClient socketClient;
	private Session session = null;
	protected StoreDAO storeDAO = new StoreDAO();
	private static Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	
	protected boolean init() {
		boolean result = false;
		try {
			iniciarSesion("Saadmin");
			storeCode = properties.getObject("eyes.store.code");
			while (storeCode.length() < 3)
				storeCode = "0" + storeCode;
			centralStore = storeDAO.getStoreByCode(session, 0);
//			inFolder = new File("C:/ALLC/WWW/ALLC_DAT/OUT/"+store+"/FTS");
//			inFolder.mkdir();
			timeSleep = 3000;//Long.parseLong(properties.getObject("fileUpdaterDown.timeSleep").toString());
			ParamsDAO paramsDAO = new ParamsDAO();
			ParamValue paravalue = null;
			paravalue = paramsDAO.getParValSpecifiByClave(session,
					Integer.valueOf(storeCode),
					ArmsServerConstants.AmbitoParams.ARMS_AGENT_PARAMS, "clientSocket.ip");
			if (Integer.valueOf(storeCode) == 0) {
				List tiendasActivas = storeDAO.getAllActiveStores(session);
				if (tiendasActivas != null && !tiendasActivas.isEmpty()) {

					Iterator itStores = tiendasActivas.iterator();

					while (itStores.hasNext()) {
						Store tienda = (Store) itStores.next();
						String codTienda = tienda.getKey().toString();
						while (codTienda.length() < 3)
							codTienda = "0" + codTienda;
						activeStoreIp.put(codTienda, tienda.getIp());
						
						paravalue = paramsDAO.getParamByClave(session, Integer.valueOf(codTienda).toString(),
								ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "SYNC_OUT");
						String dirTemp = properties.getObject("SUITE_ROOT") + paravalue.getValor() + codTienda
								+ File.separator + "FTS";
						log.info("dirTemp:"+dirTemp);
						(new File(dirTemp)).mkdirs();
						pathActiveStore.add(dirTemp);
					}

				} else {
					log.info("No existen tiendas activas disponibles.");
				}
			} else {
				String storeIP = storeDAO.getStoreByCode(session, Integer.valueOf(storeCode)).getIp();
				activeStoreIp.put(storeCode, storeIP);
				paravalue = paramsDAO.getParamByClave(session, Integer.valueOf(storeCode).toString(),
						ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "SYNC_OUT");
				String dirTemp = properties.getObject("SUITE_ROOT") + File.separator + paravalue.getValor()
						+ File.separator + storeCode + File.separator + "FTS";
				(new File(dirTemp)).mkdirs();
				pathActiveStore.add(dirTemp);
			}
			result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}
	
	protected void iniciarSesion(String name) {
		while (session == null || !session.isOpen()) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory(name).openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (session == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	public void run() {
		log.info("Iniciando File Updater Server Process...");
		init();
		while (!isEnd) {
			File updateFile = null;
			try {
				iniciarSesion("Saadmin");
				File[] list = getFilesList();
				int size = list != null ? list.length : 0;
				for(int i = 0; i < size; i++){
					updateFile = list[i];
					UtilityFile.createWriteDataFile(getEyesFileName(),
							"FILE_UPD_D_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeCode
									+ "|STR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
											.format(new Date())	+ "|Iniciando procesamiento de: "+ updateFile.getName()+".\n", true);
					log.info("Archivo a procesar: " + updateFile.getName());
					log.info("Ruta del Archivo a procesar: " + updateFile.getAbsolutePath());
					String inFolderPath = updateFile.getAbsolutePath().substring(0, (int)updateFile.getAbsolutePath().length() - updateFile.getName().length());
					log.info("Ruta del Archivo a procesar: " + inFolderPath);
					String cmdFileName = inFolderPath + File.separator + updateFile.getName().split("\\.")[0]+".CMD";
					log.info("Archivo de comandos: " + cmdFileName);
					String storeCodeToSend = inFolderPath.substring(inFolderPath.length()-8, inFolderPath.length()-5);
					log.info("Tienda del archivo: " + storeCodeToSend);
					if(!(new File(cmdFileName)).exists()){
						log.info("Archivo de comandos: " + cmdFileName + "no existe.");
						UtilityFile.createWriteDataFile(getEyesFileName(),
								"FILE_UPD_D_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeCode
										+ "|WAR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
												.format(new Date())	+ "|No se pudo actualizar el archivo: "+ updateFile.getName()+".\n", true);
					} else {
						String originDir = readDirPath(cmdFileName);
						if(originDir == null){
							log.info("Archivo de comandos: " + cmdFileName + "no contiene ruta.");
							UtilityFile.createWriteDataFile(getEyesFileName(),
									"FILE_UPD_D_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeCode
											+ "|WAR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
													.format(new Date())	+ "|No se pudo actualizar el archivo: "+ updateFile.getName()+".\n", true);
						} else {
							File originFile = new File(originDir+File.separator+updateFile.getName());
							boolean needUpdate = !comparator(originFile, updateFile);
							if(needUpdate){
								try {
									try  {
									originFile.delete();
									log.info("Ruta a copiar: "+ originFile.getAbsolutePath());
									FilesHelper.copyFile(inFolderPath, originDir, updateFile.getName(), originFile.getName());
									if(existCommands(cmdFileName)){
										int c = 2;
										String cmd = readCommand(cmdFileName, c);
										while (cmd != null && !cmd.trim().isEmpty()) {
											
											short priority = 5;
											try {
												Runtime.getRuntime().exec(cmd);
											} catch (FlexosException fe) {
												log.error(fe.getMessage(), fe);
											}
											log.info("Comando ejecutado: "+ cmd);
											c++;
											cmd = readCommand(cmdFileName, c);
										}
									}
									} catch (Exception e){
										log.error(e.getMessage(), e);
									}
									
									File fileCMD = new File(cmdFileName);
									UtilityFile.createWriteDataFile(getEyesFileName(),
											"FILE_UPD_D_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeCode
													+ "|END|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
															.format(new Date())	+ "|Finalizó el procesamiento del archivo: "+ updateFile.getName()+".\n", true);
									StringBuffer data = new StringBuffer();
									data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
											.append(ArmsServerConstants.Process.FILE_UPDATER_SERVER_OPERATION)
											.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
											.append(ArmsServerConstants.Communication.FRAME_SEP).append(storeCode)
											.append(ArmsServerConstants.Communication.FRAME_SEP).append(ArmsServerConstants.Communication.TEMP_CONN)
											.append(ArmsServerConstants.Communication.FRAME_SEP)
											.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
											.append(ArmsServerConstants.Communication.FRAME_SEP).append(updateFile.getName())
											.append(ArmsServerConstants.Communication.FRAME_SEP).append(fileCMD.getName());
									
									List list2 = Arrays.asList(p.split(data.toString()));
		
									Frame frame = new Frame(list2, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
											ArmsServerConstants.Communication.FRAME_SEP);
									if (frame.loadData()) {
										boolean send = sendFiles(frame, Integer.valueOf(storeCodeToSend));
										closeClient();
										if (send) {
											updateFile.delete();
											fileCMD.delete();
											log.info("Archivo procesado correctamente.");
										} else {
											log.error("Error al informar al server.");
										}
									}
									
								} catch (Exception e){
									log.error(e.getMessage(), e);
									UtilityFile.createWriteDataFile(getEyesFileName(),
											"FILE_UPD_D_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeCode
													+ "|WAR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
															.format(new Date())	+ "|No se pudo actualizar el archivo: "+ updateFile.getName()+".\n", true);
								}
							}
						}
					}
				}
				try {
					Thread.sleep(timeSleep);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					UtilityFile.createWriteDataFile(getEyesFileName(),
							"FILE_UPD_D_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeCode
									+ "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
											.format(new Date())	+ "|Error al procesar el archivo: "+ updateFile.getName()+".\n", true);
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
			} finally {
				if(session != null){
					try {
						session.close();
					} catch (Exception e){
						log.error(e.getMessage(), e);
					}
				}
				session = null;
			}
		}
		finished = true;
	}
	
	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
	
	private File[] getFilesList() {
//		log.info("Buscando archivos para actualizar...");
		Iterator itInFolders = pathActiveStore.iterator();
		File inFolder = null;
		File[] files = null;
		List filesStore = new ArrayList();
		int count = 0;
		while (itInFolders.hasNext()) {
			inFolder = new File((String) itInFolders.next());
			File[] tempFiles = inFolder.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isFile() && !pathname.getName().toUpperCase().endsWith(".CMD") && 
							!pathname.getName().toUpperCase().endsWith(".cmd") && !pathname.getName().toUpperCase().startsWith("TMP");
				}
			});
			if (tempFiles != null) {
				filesStore.add(tempFiles);
				count = count + tempFiles.length;
			}
		}
		if (count > 0) {
			files = new File[count];
			Iterator itFiles = filesStore.iterator();
			int countAux = 0;
			while (itFiles.hasNext()) {
				File[] temp = (File[]) itFiles.next();
				System.arraycopy(temp, 0, files, countAux, temp.length);
				countAux = countAux + temp.length;
			}
			this.filesToProcess = Arrays.asList(files).iterator();
		}
//		
//		File[] files = inFolder.listFiles(new FileFilter() {
//			public boolean accept(File pathname) {
//				return pathname.isFile() && !pathname.getName().toUpperCase().endsWith(".CMD") && !pathname.getName().toUpperCase().endsWith(".cmd") && !pathname.getName().toUpperCase().startsWith("TMP");
//			}
//		});
		if (files != null && files.length != 0) {
			Arrays.sort(files, new Comparator() {
				public int compare(Object f1, Object f2) {
                    return (-1) * ((File) f1).getName().compareTo(((File) f2).getName());
                }
			});
		}
		return files;
	}
	
	private boolean comparator(File originFile, File updateFile){
		if(originFile.length() != updateFile.length() || originFile.lastModified() != updateFile.lastModified())
			return false;
		return true;
	}
	
	private String readDirPath(String cdmFile){
		String linea = null;
		try {
			linea = Files.readSpecifictLineOfFile(cdmFile, 1);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return linea;
	}
	
	private boolean existCommands(String cdmFile){
		String linea = null;
		try {
			//nos interesa saber si hay un comando en la segunda linea
			linea = Files.readSpecifictLineOfFile(cdmFile, 2);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if(linea != null)
			return true;
		return false;
	}
	
	private String readCommand(String cdmFile, long pos){
		String linea = null;
		try {
			linea = Files.readSpecifictLineOfFile(cdmFile, pos);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return linea;
	}
	
	protected boolean connectClient(String ip) {
		if (socketClient == null) {
			socketClient = new ConnSocketClient();
			log.info("IP:"+ip+" Port:"+properties.getInt("clientSocket.port"));
			socketClient.setIpServer(ip);
			socketClient.setPortServer(properties.getInt("clientSocket.port"));
			socketClient.setRetries(properties.getInt("clientSocket.retries"));
			socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}
	
	protected boolean sendFiles(Frame frame, Integer storeToSend) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			Store storeTemp = storeDAO.getStoreByCode(session, storeToSend);
			if (socketClient == null || !socketClient.isConnected())
				connectClient(storeTemp.getIp());
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
	
	protected void closeClient() {
		if (socketClient != null)
			socketClient.closeConnection();
	}
	
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo FileUpdaterServerProcess...");
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
