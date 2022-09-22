package com.allc.arms.server.operations.cer.conciliaciones;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.params.ParamValue;
import com.allc.arms.server.params.ParamsDAO;
import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;

public class GenerateConciliacionesOperation extends AbstractOperation {

	protected static Logger logger = Logger.getLogger(GenerateConciliacionesOperation.class);
	public boolean isEnd = false;
	protected File outFolderDatafast;
	protected File outFolderMedianet;
	protected File outFolderClaro;
	protected File outFolderBwise;
	protected File bkpFolderDatafast;
	protected File bkpFolderMedianet;
	protected File bkpFolderClaro;
	protected File bkpFolderBwise;
	protected int sleepTime;
	protected ConnSocketClient socketClient = null;
	protected boolean finished = false;
	protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	protected List pinpads = null;
	protected StoreDAO storeDAO = new StoreDAO();
	protected ParamsDAO paramsDAO = new ParamsDAO();
	protected int retries;
	protected File datafastFile;
	protected File medianetFile;
	protected File claroFile;
	protected File bwiseFile;
	protected SimpleDateFormat formatterClaro = new SimpleDateFormat("yyyyMMdd");
	protected SimpleDateFormat formatterJulianDate = new SimpleDateFormat("DDD");
	protected SimpleDateFormat formatterLastID = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	protected int horaDataMedClaro;
	protected int minDataMedClaro;
	protected int horaBwise;
	protected int minBwise;
	protected int diaBwise;
	protected int horaEnvioDataMed;
	protected int minEnvioDataMed;
	protected String ipDatafast = null;
	protected String ipMedianet = null;
	protected String ipClaro = null;
	protected String ipBwise = null;
	protected String usrDatafast = null;
	protected String usrMedianet = null;
	protected String usrClaro = null;
	protected String usrBwise = null;
	protected String pswDatafast = null;
	protected String pswMedianet = null;
	protected String pswClaro = null;
	protected String pswBwise = null;
	protected String pathDatafast = null;
	protected String pathMedianet = null;
	protected String pathClaro = null;
	protected String pathBwise = null;
	protected File inFolderDestinoLocal;
	protected File recapFile;
	protected String storeCentralIP = null;
	protected String storeLocalIP = null;

	public void initialize(PropFile properties) {
		try {
			outFolderDatafast = new File(properties.getObject("conciliacion.out.folder.datafast"));
			outFolderDatafast.mkdirs();
			outFolderMedianet = new File(properties.getObject("conciliacion.out.folder.medianet"));
			outFolderMedianet.mkdirs();
			outFolderClaro = new File(properties.getObject("conciliacion.out.folder.claro"));
			outFolderClaro.mkdirs();
			outFolderBwise = new File(properties.getObject("conciliacion.out.folder.bwise"));
			outFolderBwise.mkdirs();
			bkpFolderDatafast = new File(properties.getObject("conciliacion.bkp.folder.datafast"));
			bkpFolderDatafast.mkdirs();
			bkpFolderMedianet = new File(properties.getObject("conciliacion.bkp.folder.medianet"));
			bkpFolderMedianet.mkdirs();
			bkpFolderClaro = new File(properties.getObject("conciliacion.bkp.folder.claro"));
			bkpFolderClaro.mkdirs();
			bkpFolderBwise = new File(properties.getObject("conciliacion.bkp.folder.bwise"));
			bkpFolderBwise.mkdirs();
			sleepTime = properties.getInt("conciliacion.sleeptime");
			retries = properties.getInt("conciliacion.retries");
			horaDataMedClaro = properties.getInt("conciliacion.horaDataMedClaro");
			minDataMedClaro = properties.getInt("conciliacion.minDataMedClaro");
			horaBwise = properties.getInt("conciliacion.horaBwise");
			minBwise = properties.getInt("conciliacion.minBwise");
			diaBwise = properties.getInt("conciliacion.diaBwise");
			horaEnvioDataMed = properties.getInt("conciliacion.horaEnvioArchivo.dataMed");
			minEnvioDataMed = properties.getInt("conciliacion.minEnvioArchivo.dataMed");
			ipDatafast = properties.getObject("sendFilesDatafast.server.serverFTP");
			ipMedianet = properties.getObject("sendFilesMedianet.server.serverFTP");
			ipClaro = properties.getObject("sendFilesClaro.server.serverFTP");
			ipBwise = properties.getObject("sendFilesBwise.server.serverFTP");
			usrDatafast = properties.getObject("sendFilesDatafast.username");
			usrMedianet = properties.getObject("sendFilesMedianet.username");
			usrClaro = properties.getObject("sendFilesClaro.username");
			usrBwise = properties.getObject("sendFilesBwise.username");
			pswDatafast = properties.getObject("sendFilesDatafast.password");
			pswMedianet = properties.getObject("sendFilesMedianet.password");
			pswClaro = properties.getObject("sendFilesClaro.password");
			pswBwise = properties.getObject("sendFilesBwise.password");
			pathDatafast = properties.getObject("sendFilesDatafast.ftp.folder.path");
			pathMedianet = properties.getObject("sendFilesMedianet.ftp.folder.path");
			pathClaro = properties.getObject("sendFilesClaro.ftp.folder.path");
			pathBwise = properties.getObject("sendFilesBwise.ftp.folder.path");

			inFolderDestinoLocal = new File("C:/ALLC/WWW/EYES/CNL");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		logger.info("Iniciando GenerateConciliacionesOperation...");
		initialize(properties);
		String storeCode = null;
		try {
			storeCode = StringUtils.leftPad((String) frame.getBody().get(0), 3, "0");
			String fechaCont = (String) frame.getBody().get(1);
			String iniciales = (String) frame.getBody().get(2);
			String nroLoteDat = (String) frame.getBody().get(3);
			String nroLoteMed = (String) frame.getBody().get(4);
			Integer sendFlag = Integer.valueOf((String) frame.getBody().get(5));
			Integer updateFlag = Integer.valueOf((String) frame.getBody().get(6));
			
			UtilityFile.createWriteDataFile(getEyesFileName(properties),
					"GEN_CCL_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando Conciliación "+iniciales+", Tienda:" + storeCode + ", LoteData:"+nroLoteDat+ ", LoteMed:"+nroLoteMed+".\n",
					true);
			StringBuilder msg = new StringBuilder();
			String retorno = "0";
			if(iniciales.contains("T")){
				Session sessionSaadmin = iniciarSesionSaadmin();
				Session sessionArts = iniciarSesionArts();
				Session sessionEyes = iniciarSesionEyes();
				
				storeCentralIP = storeDAO.getStoreByCode(sessionSaadmin, 0).getIp();
				
				ParamValue paravalue = paramsDAO.getParValSpecifiByClave(sessionSaadmin,
						Integer.valueOf(properties.getObject("eyes.store.code")),
						ArmsServerConstants.AmbitoParams.ARMS_AGENT_PARAMS, "clientSocket.ip");

				storeLocalIP = paravalue.getValor();
				
				try {
					List activeStores = new ArrayList();
					Store local = storeDAO.getStoreByCode(sessionSaadmin, Integer.valueOf(storeCode));
					activeStores.add(local);
					
					if (activeStores != null && !activeStores.isEmpty()) {
						Iterator itTienda = activeStores.iterator();
						while (itTienda.hasNext()) {
							try {
								Store tiendaAux = (Store) itTienda.next();
								String codTiendaAux = tiendaAux.getKey().toString();
								while (codTiendaAux.length() < 3)
									codTiendaAux = "0" + codTiendaAux;
								List<String> datosTienda = getDatosByTienda(sessionEyes,
										Integer.valueOf(codTiendaAux));
								String loteDatafast = nroLoteDat;
								while(loteDatafast.length() < 6)
									loteDatafast = "0"+loteDatafast;
								String loteMedianet = nroLoteMed;
								while(loteMedianet.length() < 6)
									loteMedianet = "0"+loteMedianet;
								String midDatafast = datosTienda.get(2);
								String midMedianet = datosTienda.get(3);
								
								Date now = new Date();
							    String strDate = formatterClaro.format(now);
							    
							    File fileExist = new File("C:/ALLC/WWW/EYES/CNL/RECAP_FILE_TO_LOCAL_" + codTiendaAux + "_" + strDate);
								int cont = 0;
								while(fileExist.exists()){
									logger.info("El archivo: "+fileExist.getName()+ (cont > 0 ? cont : "") +" existe en el destino y se renombrara");
									cont++;
									File tempFile = new File("C:/ALLC/WWW/EYES/CNL/RECAP_FILE_TO_LOCAL_" + codTiendaAux + "_" + strDate+"_"+cont);
									if(tempFile.exists())
										fileExist = new File("C:/ALLC/WWW/EYES/CNL/RECAP_FILE_TO_LOCAL_" + codTiendaAux + "_" + strDate+"_"+cont);
									else
										fileExist.renameTo(new File("C:/ALLC/WWW/EYES/CNL/RECAP_FILE_TO_LOCAL_" + codTiendaAux + "_" + strDate+"_"+cont));
								}
								recapFile = new File("C:/ALLC/WWW/EYES/CNL/RECAP_FILE_TO_LOCAL_" + codTiendaAux + "_" + strDate);
								
								boolean creadoData = crearArchivoDataFast(sessionArts, codTiendaAux, fechaCont, loteDatafast, midDatafast);

								if (creadoData && updateFlag.compareTo(1)==0)
									updateDateConclByProvStore(sessionSaadmin, "DATAFAST", new Date(),
											Integer.valueOf(codTiendaAux));

								boolean creadoMedia = crearArchivoMedianet(sessionArts, sessionSaadmin,
										codTiendaAux, fechaCont, loteMedianet,
										midMedianet);

								if (creadoMedia && updateFlag.compareTo(1)==0)
									updateDateConclByProvStore(sessionSaadmin, "MEDIANET", new Date(),
											Integer.valueOf(codTiendaAux));

								if(!storeCode.equals("000")){
									boolean recapSendToTienda = enviarArchivoACentral(recapFile, tiendaAux, properties);
									if(recapSendToTienda){
										activeOperationRecapInCentral(recapFile, tiendaAux, properties);
									}
								}
								
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
								retorno = "1";
							}
						}
					}
				
					if(sendFlag.compareTo(1)==0){
						File[] filesDatfast = outFolderDatafast.listFiles(new FileFilter() {
							public boolean accept(File pathname) {
								return pathname.isFile();
							}
						});
	
						if (filesDatfast != null && filesDatfast.length > 0) {
							for (int i = 0; i < filesDatfast.length; i++) {
								File fileDataSend = new File(filesDatfast[i].getAbsolutePath());
								boolean enviadoData = sendFilesForConciliacion(sessionSaadmin, "DATAFAST",
										fileDataSend.getName(), outFolderDatafast);
								if (enviadoData) {
									File out = new File(bkpFolderDatafast, fileDataSend.getName());
									fileDataSend.renameTo(out);
									logger.info("Archivo Datafast " + fileDataSend.getName() + " enviado con exito.");
									UtilityFile.createWriteDataFile(getEyesFileName(properties),
											"GEN_CCL_P|" + properties.getHostName() + "|3|"
													+ properties.getHostAddress() + "|" + 000 + "|STR|"
													+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
															.format(new Date())
													+ "|Archivo Datafast " + fileDataSend.getName() + " enviado con exito."
													+ ".\n",
											true);
								} else{
									UtilityFile.createWriteDataFile(getEyesFileName(properties),
											"GEN_CCL_P|" + properties.getHostName() + "|3|"
													+ properties.getHostAddress() + "|" + 000 + "|WAR|"
													+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
															.format(new Date())
													+ "|Archivo de Datafast " + bwiseFile.getName() 
													+ "no pudo ser enviado con exito." + ".\n",true);
									logger.info("Archivo Datafast " + fileDataSend.getName()
											+ " no pudo ser enviado con exito.");
								}
							}
						}
						File[] filesMedianet = outFolderMedianet.listFiles(new FileFilter() {
							public boolean accept(File pathname) {
								return pathname.isFile();
							}
						});
	
						if (filesMedianet != null && filesMedianet.length > 0) {
							for (int i = 0; i < filesMedianet.length; i++) {
								File fileMedSend = new File(filesMedianet[i].getAbsolutePath());
								boolean enviadoMed = sendFilesForConciliacion(sessionSaadmin, "MEDIANET",
										fileMedSend.getName(), outFolderMedianet);
								if (enviadoMed) {
									File out = new File(bkpFolderMedianet, fileMedSend.getName());
									fileMedSend.renameTo(out);
									logger.info("Archivo Medianet " + fileMedSend.getName() + " enviado con exito.");
									UtilityFile.createWriteDataFile(getEyesFileName(properties),
											"GEN_CCL_P|" + properties.getHostName() + "|3|"
													+ properties.getHostAddress() + "|" + 000 + "|STR|"
													+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
															.format(new Date())
													+ "|Archivo Medianet " + fileMedSend.getName() + " enviado con exito."
													+ ".\n",
											true);
	
								} else {
									UtilityFile.createWriteDataFile(getEyesFileName(properties),
											"GEN_CCL_P|" + properties.getHostName() + "|3|"
													+ properties.getHostAddress() + "|" + 000 + "|WAR|"
													+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
															.format(new Date())
													+ "|Archivo de Medianet " + bwiseFile.getName() 
													+ "no pudo ser enviado con exito." + ".\n",true);
									logger.info("Archivo Medianet " + fileMedSend.getName()
											+ " no pudo ser enviado con exito.");
								}
							}
						}
					}

				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					retorno = "1";
				} finally {
					if (sessionArts != null && sessionSaadmin != null && sessionEyes != null) {
						sessionArts.close();
						sessionSaadmin.close();
						sessionEyes.close();
					}
				}
			
			
			} 
			if(iniciales.contains("C")) {
				Session sessionSaadmin = iniciarSesionSaadmin();
				Session sessionArts = iniciarSesionArts();
				
				storeCentralIP = storeDAO.getStoreByCode(sessionSaadmin, 0).getIp();
				
				ParamValue paravalue = paramsDAO.getParValSpecifiByClave(sessionSaadmin,
						Integer.valueOf(properties.getObject("eyes.store.code")),
						ArmsServerConstants.AmbitoParams.ARMS_AGENT_PARAMS, "clientSocket.ip");

				storeLocalIP = paravalue.getValor();
				
				try {
					List activeStores = new ArrayList();
					Store local = storeDAO.getStoreByCode(sessionSaadmin, Integer.valueOf(storeCode));
					activeStores.add(local);
					if (activeStores != null && !activeStores.isEmpty()) {
						Iterator itTienda = activeStores.iterator();
						while (itTienda.hasNext()) {
							try {
								Store tiendaAux = (Store) itTienda.next();
								String codTiendaAux = tiendaAux.getKey().toString();
								while (codTiendaAux.length() < 3)
									codTiendaAux = "0" + codTiendaAux;
								
							    boolean creadoClaro = crearArchivoClaro(sessionArts, codTiendaAux, fechaCont);
								if (creadoClaro && claroFile != null) {
									if(updateFlag.compareTo(1)==0)
										updateDateConclByProvStore(sessionSaadmin, "CLARO", new Date(),
												Integer.valueOf(codTiendaAux));
									if(sendFlag.compareTo(1)==0){
										boolean enviado = sendFilesForConciliacion(sessionSaadmin, "CLARO",
												claroFile.getName(), outFolderClaro);
										if (enviado) {
											File out = new File(bkpFolderClaro, claroFile.getName());
											claroFile.renameTo(out);
											UtilityFile.createWriteDataFile(getEyesFileName(properties),
													"GEN_CCL_P|" + properties.getHostName() + "|3|"
															+ properties.getHostAddress() + "|" + codTiendaAux + "|STR|"
															+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
																	.format(new Date())
															+ "|Archivo de Claro" + claroFile.getName()
															+ "enviado con exito" + ".\n",
													true);
											logger.info(
													"Archivo de Claro " + claroFile.getName() + " enviado con exito.");
										} else{
											UtilityFile.createWriteDataFile(getEyesFileName(properties),
													"GEN_CCL_P|" + properties.getHostName() + "|3|"
															+ properties.getHostAddress() + "|" + codTiendaAux + "|WAR|"
															+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
																	.format(new Date())
															+ "|Archivo de Claro" + claroFile.getName()
															+ "no pudo ser enviado con exito." + ".\n",true);
											logger.info("Archivo de Claro " + claroFile.getName()
													+ " no pudo ser enviado con exito.");
										}
									}
								}

							} catch (Exception e) {
								logger.error(e.getMessage(), e);
								retorno = "1";
							}
						}
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					retorno = "1";
				} finally {
					if (sessionArts != null && sessionSaadmin != null) {
						sessionArts.close();
						sessionSaadmin.close();
					}
				}
			} 
			if(iniciales.contains("B")){
			
				Session sessionSaadmin = iniciarSesionSaadmin();
				Session sessionArts = iniciarSesionArts();

				try {
					List activeStores = storeDAO.getAllActiveStores(sessionSaadmin);

					if (activeStores != null && !activeStores.isEmpty()) {
						Iterator itTienda = activeStores.iterator();
						while (itTienda.hasNext()) {
							Store tiendaAux = (Store) itTienda.next();
							String codTiendaAux = tiendaAux.getKey().toString();
							while (codTiendaAux.length() < 3)
								codTiendaAux = "0" + codTiendaAux;

							boolean creadoBwise = crearArchivoBwise(sessionArts, codTiendaAux, fechaCont);

							if (creadoBwise && bwiseFile != null) {
								if(updateFlag.compareTo(1)==0)
									updateDateConclByProvStore(sessionSaadmin, "BWISE", new Date(),
											Integer.valueOf(codTiendaAux));
								if(sendFlag.compareTo(1)==0){
									boolean enviado = sendFilesForConciliacion(sessionSaadmin, "BWISE",
											bwiseFile.getName(), outFolderBwise);
									if (enviado) {
										File out = new File(bkpFolderBwise, bwiseFile.getName());
										bwiseFile.renameTo(out);
										UtilityFile.createWriteDataFile(getEyesFileName(properties),
												"GEN_CCL_P|" + properties.getHostName() + "|3|"
														+ properties.getHostAddress() + "|" + codTiendaAux + "|STR|"
														+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
																.format(new Date())
														+ "|Archivo de Bwise " + bwiseFile.getName() + "enviado con exito"
														+ ".\n",
												true);
										logger.info("Archivo " + bwiseFile.getName() + " enviado con exito.");
									} else{
										UtilityFile.createWriteDataFile(getEyesFileName(properties),
												"GEN_CCL_P|" + properties.getHostName() + "|3|"
														+ properties.getHostAddress() + "|" + codTiendaAux + "|WAR|"
														+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
																.format(new Date())
														+ "|Archivo de Bwise" + bwiseFile.getName() 
														+ "no pudo ser enviado con exito." + ".\n",true);
										logger.info(
												"Archivo " + bwiseFile.getName() + " no pudo ser enviado con exito.");
									}
								}
							}
							
						}

					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					retorno = "1";
				} finally {
					if (sessionArts != null && sessionSaadmin != null) {
						sessionArts.close();
						sessionSaadmin.close();
					}
				}

			
			}
			msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append(retorno);
			String tmp = Util.addLengthStartOfString(msg.toString(),
					properties.getInt("serverSocket.quantityBytesLength"));
			socket.writeDataSocket(tmp);
			
		} catch (Exception e) {
			UtilityFile.createWriteDataFile(getEyesFileName(properties),
					"GEN_CCL_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode
							+ "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Error al generar conciliaciones.\n",
					true);
			logger.error(e.getMessage(), e);
		}
		finished = true;
		return false;
	}

	private String getEyesFileName(PropFile properties) {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
	
	public boolean sendFilesForConciliacion(Session sessionSaadmin, String proveedor, String nombreFileToSend,
			File outFolder) {

		com.jcraft.jsch.Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;
		int reintentos = 0;
		String ip = null;
		String usr = null;
		String psw = null;
		String path = null;
		int SFTPPORT = 22;

		if (proveedor.equals("DATAFAST")) {
			ip = ipDatafast;
			usr = usrDatafast;
			psw = pswDatafast;
			path = pathDatafast;
		} else if (proveedor.equals("MEDIANET")) {
			ip = ipMedianet;
			usr = usrMedianet;
			psw = pswMedianet;
			path = pathMedianet;
		} else if (proveedor.equals("CLARO")) {
			ip = ipClaro;
			usr = usrClaro;
			psw = pswClaro;
			path = pathClaro;
		} else if (proveedor.equals("BWISE")) {
			ip = ipBwise;
			usr = usrBwise;
			psw = pswBwise;
			path = pathBwise;
		}
		logger.info("Inicio de transferencia de archivo: " + nombreFileToSend);

		while (reintentos < retries) {
			logger.info("Archivo a transferir: " + nombreFileToSend);
			try {
				if (ip != null && usr != null && psw != null && path != null) {

					JSch jsch = new JSch();
					session = jsch.getSession(usr, ip, SFTPPORT);
					session.setPassword(psw);
					java.util.Properties config = new java.util.Properties();
					config.put("StrictHostKeyChecking", "no");
					session.setConfig(config);
					session.connect();
					channel = session.openChannel("sftp");
					channel.connect();
					channelSftp = (ChannelSftp) channel;
					channelSftp.cd(path);
					File f = new File(outFolder, nombreFileToSend);
					FileInputStream inputStream = new FileInputStream(f);
					channelSftp.put(inputStream, f.getName());
					logger.info("Archivo " + nombreFileToSend + " transferido con exito.");

					try {
						inputStream.close();
						channelSftp.exit();
						channel.disconnect();
						session.disconnect();
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
					return true;

				} else
					reintentos = retries;
			} catch (Exception e) {
				logger.error("Se produjo un error durante el intento de transferencia.");
				logger.error(e.getMessage(), e);
				reintentos++;

			}
		}
		return false;
	}
	
	public void activeOperationRecapInCentral(File fileToProcess, Store tienda, PropFile properties){
		
		StringBuffer recapGenerate = new StringBuffer();
		String valueRecap = ""; 
		List listRecap;
		Frame frameRecap;
		
		recapGenerate.append(ArmsServerConstants.Communication.SOCKET_CHANNEL)
		.append(ArmsServerConstants.Communication.FRAME_SEP)
		.append(ArmsServerConstants.Process.REGISTER_RECAP_OP)
		.append(ArmsServerConstants.Communication.FRAME_SEP).append(tienda.getKey().toString())
		.append(ArmsServerConstants.Communication.FRAME_SEP)
		.append(0).append(ArmsServerConstants.Communication.FRAME_SEP)
		.append(ArmsServerConstants.Communication.TEMP_CONN)
		.append(ArmsServerConstants.Communication.FRAME_SEP)
		.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
		.append(ArmsServerConstants.Communication.FRAME_SEP).append(fileToProcess.getAbsolutePath());
		valueRecap = recapGenerate.toString();
		listRecap = Arrays.asList(p.split(valueRecap));
		frameRecap = new Frame(listRecap, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
				ArmsServerConstants.Communication.FRAME_SEP);
		
		if (frameRecap.loadData()) {
			boolean send = sendFrame(frameRecap, properties, storeCentralIP);
			closeClient();
			if (send) {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "GEN_CCL_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+tienda.getKey().toString()+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Trama para procesar archivos de recap eviada con exito.\n", true);
				logger.info("Activación de Operacion RegisterRecap enviado correctamente a ArmsServerCentral.");
				
			} else {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "GEN_CCL_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+tienda.getKey().toString()+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Trama para procesar archivos de recap no enviada.\n", true);
				logger.error("Error al enviar la activación de la operación RegisterRecap a ArmsServerCentral.");
			}
		}
			
	}
	
	protected Frame leerRespuesta(ConnSocketClient socketClient) {
		int numberOfBytes = socketClient.readLengthDataSocket();
		if (numberOfBytes > 0) {
			String str = socketClient.readDataSocket(numberOfBytes);
			if (StringUtils.isNotBlank(str)) {
				List list = Arrays.asList(p.split(str));
				Frame frameRpta = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
						ArmsServerConstants.Communication.FRAME_SEP);
				logger.info("Respuesta recibida: " + frameRpta.toString());
				return frameRpta;
			}
		}
		if( numberOfBytes == 0)
			numberOfBytes = socketClient.readLengthDataSocket();
		
		logger.info("No se recibio respuesta.");
		return null;
	}
	
	public boolean enviarArchivoACentral(File fileToSend, Store store, PropFile properties) {
		Store tienda = store;
		String storeIP = storeCentralIP;
		StringBuffer data = new StringBuffer();
		data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(ArmsServerConstants.Process.FILE_SENDER_OPERATION)
				.append(ArmsServerConstants.Communication.FRAME_SEP).append(tienda.getKey().toString())
				.append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
				.append(ArmsServerConstants.Communication.FRAME_SEP).append(ArmsServerConstants.Communication.TEMP_CONN)
				.append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
				.append(ArmsServerConstants.Communication.FRAME_SEP).append(fileToSend.getAbsolutePath())
				.append(ArmsServerConstants.Communication.FRAME_SEP).append(inFolderDestinoLocal.getAbsolutePath())
				.append(ArmsServerConstants.Communication.FRAME_SEP).append(storeIP)
				.append(ArmsServerConstants.Communication.FRAME_SEP).append(properties.getObject("serverSocket.port"));

		List list = Arrays.asList(p.split(data.toString()));
		Frame frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
				ArmsServerConstants.Communication.FRAME_SEP);
		if (frame.loadData()) {
			logger.info("IP-LOCAL: " + storeLocalIP);
			boolean send = sendFrame(frame, properties, storeLocalIP);
			closeClient();
			if (send) {
				logger.info("Archivo " + fileToSend.getName() + " enviado correctamente a la tienda: "
						+ tienda.getKey().toString() + ".");
				return true;
			} else {
				logger.error("Error al enviar el archivo " + fileToSend.getName() + " a la tienda: "
						+ tienda.getKey().toString() + ".");
			}
		}

		return false;
	}

	protected boolean sendFrame(Frame frame, PropFile properties, String tiendaIp) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected())
				connectClient(properties, tiendaIp);
			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
			logger.info("Trama a enviar: " + trama);
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
						logger.info("Respuesta recibida: " + frameRpta.toString());
						if (frameRpta.getStatusTrama() == 0) {
							return true;
						}
					}
				}
			} else {
				socketClient.setConnected(false);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			socketClient.setConnected(false);
		}
		return false;
	}

	protected boolean connectClient(PropFile properties, String storeIP) {
		if (socketClient == null || !socketClient.isConnected()) {
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(storeIP);
			socketClient.setPortServer(properties.getInt("serverSocket.port"));
			socketClient.setRetries(2);
			socketClient.setTimeOutConnection(properties.getInt("serverSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("serverSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("serverSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}

	protected void closeClient() {
		if (socketClient != null)
			socketClient.closeConnection();
	}
	
	public boolean updateDateConclByProvStore(Session sessionSaadmin, String proveedor, Date fecha, Integer tienda) {
		Transaction tx = null;
		try {
			tx = sessionSaadmin.beginTransaction();
			Query query = sessionSaadmin.createSQLQuery("UPDATE CFG_CN_CNF SET LST_CN_DATE = :valor1 WHERE SPR_NM = '"
					+ proveedor + "' and DES_CLAVE = " + tienda);
			query.setParameter("valor1", fecha);
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}
	
	public boolean crearArchivoDataFast(Session sessionArts, String tienda, String fechaContable,
			String loteDatafast, String midDatafast) {
		logger.info("Proceso de conciliación de Datafast para tienda: " + tienda);

		String lastCardID = "";
		String lastpPlzDif = "";
		String cardID = "";
		String plzDif = "";
		String tipoCred = "";
		String lastTipoCredito = "";
		String banco = "";
		String lastBanco = "";


		if (fechaContable != null) {
			try {
				List<Object[]> dataDataFast = getDataFastData(sessionArts, fechaContable,
						Integer.valueOf(tienda));
				if (dataDataFast != null && !dataDataFast.isEmpty()) {
					logger.info("Inicia creacion archivo conciliacion DataFast.");
					Object[] rowAux = dataDataFast.get(0);
					String fechaToFileName = rowAux[1].toString().substring(4, 6) + rowAux[1].toString().substring(6, 8)
							+ rowAux[1].toString().substring(0, 4);
					String fechaAsString = rowAux[1].toString().substring(2, 8);
					String datafastFileName = fechaToFileName + "." + midDatafast.substring(midDatafast.length() - 3);

					File datafastFileDel = new File(outFolderDatafast, datafastFileName);
					if (datafastFileDel.exists())
						datafastFileDel.delete();

					datafastFile = new File(outFolderDatafast, datafastFileName);
					BufferedWriter bwr = new BufferedWriter(new FileWriter(datafastFile, true));
					String reg_header = "1" + midDatafast + fechaAsString + StringUtils.leftPad("", 183, " ");
					bwr.write(reg_header);
					bwr.newLine();
					int seq = 0;
					String seqLote = null;

					BufferedWriter bwrRecapFile = new BufferedWriter(new FileWriter(recapFile, true));

					for (int i = 0; i < dataDataFast.size(); i++) {
						Object[] row = dataDataFast.get(i);

						String tid = StringUtils.leftPad(row[0].toString(), 8, "0");
						String fecha = row[1].toString().substring(2, 8);
						String hora = row[2].toString();
						String vale = StringUtils.leftPad(row[3].toString(), 6, "0");
						String auto = StringUtils.leftPad(row[4].toString(), 6, "0");
						cardID = row[5].toString();
						plzDif = row[9].toString();
						tipoCred = row[8].toString().trim();
						banco = row[16].toString().trim();
						if (!lastCardID.equals(cardID) || !lastpPlzDif.equals(plzDif)
								|| !lastTipoCredito.equals(tipoCred) || !lastBanco.equals(banco)) {

							if (seq < 99)
								seq++;
							else
								seq = 1;

						}

						if (seq < 10)
							seqLote = "0" + String.valueOf(seq);
						else
							seqLote = String.valueOf(seq);

						// String loteByTid = (String)
						// hashPinpadsDatafast.get(tid);
						String lote = loteDatafast.substring(loteDatafast.length() - 4) + seqLote;
						int montoAux = Double.valueOf(row[6].toString()).intValue();
						String monto = StringUtils.leftPad(String.valueOf(montoAux), 13, "0");
						String indTrx = "1";
						int interesAux = Double.valueOf(row[7].toString()).intValue();
						String interes = StringUtils.leftPad(String.valueOf(interesAux), 13, "0");
						String tipoCredito = StringUtils.leftPad(row[8].toString().trim(), 2, "0");
						String cuotas = StringUtils.leftPad(row[9].toString().trim(), 2, "0");
						int ivaAux = Double.valueOf(row[10].toString()).intValue();
						String iva = StringUtils.leftPad(String.valueOf(ivaAux), 13, "0");
						String servicio = StringUtils.leftPad("", 13, "0");
						String propina = StringUtils.leftPad("", 13, "0");

						String montoFijo = StringUtils.leftPad("", 13, "0");
						String ice = StringUtils.leftPad("", 13, "0");
						String otherTax = StringUtils.leftPad("", 13, "0");
						String cashOver = StringUtils.leftPad("", 13, "0");
						int montoTarifaAAux = Double.valueOf(row[11].toString()).intValue();
						String montoTarifaA = StringUtils.leftPad(String.valueOf(montoTarifaAAux), 13, "0");
						int montoTarifaBAux = Double.valueOf(row[12].toString()).intValue();
						String montoTarifaB = StringUtils.leftPad(String.valueOf(montoTarifaBAux), 13, "0");
						String filler = StringUtils.leftPad("", 13, " ");
						bwr.write("2" + tid + fecha + hora + vale + auto + lote + monto + indTrx + tipoCredito + cuotas
								+ iva + servicio + propina + interes + montoFijo + ice + otherTax + cashOver
								+ montoTarifaA + montoTarifaB + filler);
						lastCardID = cardID;
						lastpPlzDif = plzDif;
						lastTipoCredito = tipoCred;
						lastBanco = banco;
						bwr.newLine();

						bwrRecapFile.write(row[1].toString() + "|" + hora + "|" + vale + "|" + auto + "|" + lote);
						bwrRecapFile.newLine();

						updatePagoPinpadLote(sessionArts, Integer.valueOf(row[13].toString()),
								Integer.valueOf(row[14].toString()), Integer.valueOf(lote));
					}
					// se deben considerar el pie + detalles
					String totalReg = StringUtils.leftPad(String.valueOf(dataDataFast.size() + 1), 6, "0");
					String reg_control = "9" + fechaAsString + totalReg + StringUtils.leftPad("", 187, " ");
					bwr.write(reg_control);
					bwr.newLine();
					if (bwr != null) {
						bwr.close();
					}
					if (bwrRecapFile != null) {
						bwrRecapFile.close();
					}
				}

				return true;

			} catch (Exception e) {
				logger.info("Error durante el proceso de creacion del archivo Datafast para la tienda: " + tienda);
				logger.error(e.getMessage(), e);
			}

		}

		return false;
	}

	public boolean crearArchivoMedianet(Session sessionArts, Session sessionSaadmin, String tienda,
			String fechaContable, String loteMedianet, String midMedianet) {
		logger.info("Proceso de conciliación para Medianet para la tienda: " + tienda);
		
		if (fechaContable != null) {
			try {
				List<String> dataStore = getDataStoreMedianet(sessionSaadmin, Integer.valueOf(tienda));
				List<Object[]> dataMediaNet = getMedianetData(sessionArts, fechaContable,
						Integer.valueOf(tienda));
				if (dataMediaNet != null && !dataMediaNet.isEmpty()) {
					logger.info("Inicia creacion archivo conciliacion Medianet.");

					Object[] rowAux = dataMediaNet.get(0);
					String fechaAux = rowAux[1].toString();
					String medianetFileName = "T05DROS0" + tienda + "_" + fechaAux + ".txt";

					File medianetFileDel = new File(outFolderMedianet, medianetFileName);
					if (medianetFileDel.exists())
						medianetFileDel.delete();

					medianetFile = new File(outFolderMedianet, medianetFileName);
					BufferedWriter bwr = new BufferedWriter(new FileWriter(medianetFile, true));

					String comercio = StringUtils.leftPad(midMedianet, 12, "0");
					String tid = "";
					String lastTid = "";
					String nextTid = "";
					int countTrxTid = 0;
					String lote = StringUtils.leftPad(loteMedianet, 7, "0");
					String nomTienda = StringUtils.rightPad(dataStore.get(0), 25, " ");
					String ciudadTienda = StringUtils.rightPad(dataStore.get(1), 12, " ");

					BufferedWriter bwrRecapFile = new BufferedWriter(new FileWriter(recapFile, true));

					for (int i = 0; i < dataMediaNet.size(); i++) {
						Object[] row = dataMediaNet.get(i);

						tid = row[14].toString();

						if (!tid.equals(lastTid)) {
							List<String> dataMediaNetHeader = getMedianetDataHeader(sessionArts, fechaContable, Integer.valueOf(tienda), tid);
							String cantTrx = StringUtils.leftPad(dataMediaNetHeader.get(0), 6, "0");
							countTrxTid = Integer.valueOf(dataMediaNetHeader.get(0));
							int montoTotalAux = Double.valueOf(dataMediaNetHeader.get(1)).intValue();
							String montoTotal = StringUtils.leftPad(String.valueOf(montoTotalAux), 13, "0");
							String idCaja = StringUtils.leftPad(tid, 8, " ");
							String reg_header = "C" + comercio + fechaAux + lote + idCaja + cantTrx + montoTotal
									+ nomTienda + ciudadTienda + StringUtils.leftPad("", 114, " ");
							bwr.write(reg_header);
							bwr.newLine();
						}
						String numAux = row[0].toString();
						String numTarjeta = StringUtils
								.rightPad(numAux.substring(0, 6) + "XXXXXX" + numAux.substring(12, 16), 19, " ");
						String codTrx = "003000";
						String fecha = row[1].toString();
						String hora = row[2].toString();
						String vale = StringUtils.leftPad(row[3].toString(), 6, "0");
						String auto = StringUtils.leftPad(row[4].toString(), 6, "0");
						int montoAux = Double.valueOf(row[5].toString()).intValue();
						String monto = StringUtils.leftPad(String.valueOf(montoAux), 13, "0");
						String indTrx = "1";
						String interes = StringUtils.leftPad("", 13, "0");

						String tipoCredito = null;
						if (row[6].toString().trim().equals("00"))
							tipoCredito = "PE";
						else if (row[6].toString().trim().equals("01") || row[6].toString().trim().equals("02"))
							tipoCredito = "CF";
						else if (row[6].toString().trim().equals("03"))
							tipoCredito = "PI";
						else if (row[6].toString().trim().equals("04") || row[6].toString().trim().equals("05"))
							tipoCredito = "PR";
						else if (row[6].toString().trim().equals("06"))
							tipoCredito = "PS";
						else if (row[6].toString().trim().equals("07"))
							tipoCredito = "S1";
						else
							tipoCredito = "  ";

						String cuotas = StringUtils.leftPad(row[7].toString().trim(), 2, "0");
						String lectura = "090";
						String tipoMoneda = "840";
						int ivaAux = Double.valueOf(row[8].toString()).intValue();
						String iva = StringUtils.leftPad(String.valueOf(ivaAux), 13, "0");
						String servicio = StringUtils.leftPad("", 13, "0");
						String propina = StringUtils.leftPad("", 13, "0");
						String montoFijo = StringUtils.leftPad("", 13, "0");
						String tipoPromo = "00";
						String mesesGracia = StringUtils.leftPad(row[9].toString().trim(), 2, "0");
						String codEmpresa = "0000";
						String statusTrx = "O";
						String codRta = "00";
						String codDispositivo = "0";
						String adqTarjeta = StringUtils.rightPad("CREDIMATIC01", 12, " ");
						String adqServicio = StringUtils.leftPad("", 12, " ");
						int montoTarifaAAux = Double.valueOf(row[10].toString()).intValue();
						String montoTarifaA = StringUtils.leftPad(String.valueOf(montoTarifaAAux), 13, "0");
						int montoTarifaBAux = Double.valueOf(row[11].toString()).intValue();
						String montoTarifaB = StringUtils.leftPad(String.valueOf(montoTarifaBAux), 13, "0");
						String filler = StringUtils.leftPad("", 3, " ");
						bwr.write("D" + numTarjeta + codTrx + fecha + hora + vale + auto + monto + indTrx + tipoCredito
								+ cuotas + lectura + tipoMoneda + iva + servicio + propina + interes + montoFijo
								+ tipoPromo + mesesGracia + codEmpresa + statusTrx + codRta + codDispositivo
								+ adqTarjeta + adqServicio + montoTarifaB + montoTarifaA + filler);
						bwr.newLine();

						updatePagoPinpadLote(sessionArts, Integer.valueOf(row[12].toString()),
								Integer.valueOf(row[13].toString()), Integer.valueOf(lote));

						if (dataMediaNet.size() - 1 > i && dataMediaNet.get(i + 1) != null) {
							Object[] rowNext = dataMediaNet.get(i + 1);
							nextTid = rowNext[14].toString();

							if (!tid.equals(nextTid)) {
								String totalReg = StringUtils.leftPad(String.valueOf(countTrxTid + 2), 6, "0");
								String reg_total = "TN" + fechaAux + lote + "000001" + totalReg
										+ StringUtils.leftPad("", 178, " ");
								bwr.write(reg_total);
								bwr.newLine();
								countTrxTid = 0;
							}
						} else {
							String totalReg = StringUtils.leftPad(String.valueOf(countTrxTid + 2), 6, "0");
							String reg_total = "TN" + fechaAux + lote + "000001" + totalReg
									+ StringUtils.leftPad("", 178, " ");
							bwr.write(reg_total);
							bwr.newLine();
							countTrxTid = 0;
						}

						bwrRecapFile.write(fecha + "|" + hora + "|" + vale + "|" + auto + "|" + lote);
						bwrRecapFile.newLine();

						lastTid = tid;
					}
					if (bwr != null) {
						bwr.close();
					}
					if (bwrRecapFile != null) {
						bwrRecapFile.close();
					}
				}

				return true;

			} catch (Exception e) {
				logger.info("Error durante el proceso de creacion del archivo Medianet para la tienda: " + tienda);
				logger.error(e.getMessage(), e);
			}
		}

		return false;
	}

	public boolean crearArchivoClaro(Session sessionArts, String tienda, String fechaContable) {

		logger.info("Proceso de conciliación para Claro para la tienda: " + tienda);

		while (tienda.length() < 4)
			tienda = "0" + tienda;

		if (fechaContable != null) {
			try {
				List<Object[]> dataDetalle = getClaroData(sessionArts, fechaContable,
						Integer.valueOf(tienda));
				if (dataDetalle != null && !dataDetalle.isEmpty()) {
					logger.info("Inicia creacion archivo conciliacion Claro.");
					Object[] rowAux = dataDetalle.get(0);
					String fechaAux = formatterClaro.format((Date) rowAux[1]);
					String claroFileName = "12_" + fechaAux.substring(4, 6) + fechaAux.substring(6, 8) + "."
							+ tienda.substring(1, tienda.length());

					File claroFileDel = new File(outFolderClaro, claroFileName);
					if (claroFileDel.exists())
						claroFileDel.delete();

					claroFile = new File(outFolderClaro, claroFileName);
					BufferedWriter bwr = new BufferedWriter(new FileWriter(claroFile, true));

					// String fecha = formatterClaro.format(fechaAux);
					String diaJuliano = formatterJulianDate.format((Date) rowAux[1]);
					String numRec = "00" + tienda.substring(1, tienda.length())
							+ diaJuliano.substring(diaJuliano.length() - 3) + "00";
					String reg_header = "1" + fechaAux + numRec + StringUtils.leftPad("", 15, "0")
							+ tienda.substring(1, tienda.length()) + "82" + StringUtils.leftPad("", 89, "0");
					bwr.write(reg_header);
					bwr.newLine();
					Double totalVenta = new Double(0);
					Double totalIva = new Double(0);
					for (int i = 0; i < dataDetalle.size(); i++) {
						Object[] row = dataDetalle.get(i);
						String numAut = StringUtils.leftPad(row[0].toString(), 10, "0");
						String fechaTrx = formatterClaro.format((Date) row[1]);
						String tipoCred = "01";
						String cuotas = "00";
						String valFijo = "0000000000000826632";
						Double valorTotalAux = Double.valueOf(row[2].toString()) + Double.valueOf(row[3].toString());
						String valorTotal = StringUtils.leftPad(String.valueOf(valorTotalAux.intValue()), 15, "0");
						totalVenta = totalVenta + valorTotalAux;
						String iva = row[3].toString().trim();
						totalIva = totalIva + Double.valueOf(iva);
						iva = StringUtils.leftPad(row[3].toString(), 15, "0");
						String hora = row[4].toString();
						String terminal = StringUtils.leftPad(row[5].toString(), 8, "0");
						String cajero = StringUtils.leftPad(row[6].toString(), 6, "0");
						String fijo = "01";
						String seqTrx = StringUtils.leftPad(row[7].toString(), 6, "0");
						String tipCta = "00";
						String tipTrx = row[8].toString().equals("1") ? "1" : "2";

						String telAux = row[9].toString().trim();
						String telefono = null;
						if (telAux.length() <= 10)
							telefono = StringUtils.leftPad(telAux, 10, "0");
						else
							telefono = StringUtils.leftPad(telAux.substring(0, 10), 10, "0");

						String filler = "00000";

						bwr.write("2" + numAut + fechaTrx + tipoCred + cuotas + valFijo + numAut + valorTotal + iva
								+ hora + terminal + cajero + fijo + seqTrx + tipCta + tipTrx + telefono + filler);
						bwr.newLine();

					}
					String cantTrx = StringUtils.leftPad(String.valueOf(dataDetalle.size()), 6, "0");
					int totalVentaDiaAux = totalVenta.intValue();
					String totalVentaDia = StringUtils.leftPad(String.valueOf(totalVentaDiaAux), 16, "0");
					int totalIvaDiaAux = totalIva.intValue();
					String totalIvaDia = StringUtils.leftPad(String.valueOf(totalIvaDiaAux), 16, "0");
					String reg_total = "3" + cantTrx + totalVentaDia + totalIvaDia + cantTrx + totalVentaDia
							+ StringUtils.leftPad("", 67, "0");
					bwr.write(reg_total);
					bwr.newLine();
					if (bwr != null) {
						bwr.close();
					}
				}

				return true;

			} catch (Exception e) {
				logger.info("Error durante el proceso de creacion del archivo Claro para la tienda: " + tienda);
				logger.error(e.getMessage(), e);
			}
		}

		return false;
	}

	public boolean crearArchivoBwise(Session sessionArts, String tienda, String fechaContable) {
		logger.info("Proceso de conciliación para Bwise para la tienda: " + tienda);

		if (fechaContable != null) {
			while (tienda.length() < 4)
				tienda = "0" + tienda;
			String bwiseFileName = "ta" + tienda.substring(1, tienda.length()) + fechaContable.substring(2, 4)
					+ fechaContable.substring(5, 7) + fechaContable.substring(8, 10) + ".txt";

			try {
				List<Object[]> dataDetalle = getBwiseData(sessionArts, fechaContable, Integer.valueOf(tienda));
				if (dataDetalle != null && !dataDetalle.isEmpty()) {
					logger.info("Inicia creacion archivo conciliacion BWISE.");

					File bwiseFileDel = new File(outFolderBwise, bwiseFileName);
					if (bwiseFileDel.exists())
						bwiseFileDel.delete();

					bwiseFile = new File(outFolderBwise, bwiseFileName);
					BufferedWriter bwr = new BufferedWriter(new FileWriter(bwiseFile, true));

					for (int i = 0; i < dataDetalle.size(); i++) {
						Object[] row = dataDetalle.get(i);
						String nombreTienda = row[0].toString();
						String fecha = row[1].toString().substring(0, 4) + "/" + row[1].toString().substring(5, 7) + "/"
								+ row[1].toString().substring(8, 10);
						String hora = row[2].toString().substring(0, 2) + ":" + row[2].toString().substring(2, 4) + ":"
								+ row[2].toString().substring(4, 6);
						String referencia = row[3].toString();
						String num = row[4].toString();
						int valorAux = Double.valueOf(row[5].toString()).intValue();
						String valor = null;
						if (valorAux < 100)
							valor = "0" + String.valueOf(valorAux);
						else
							valor = String.valueOf(valorAux);
						String valorFinal = valor.substring(0, valor.length() - 2) + "."
								+ valor.substring(valor.length() - 2, valor.length());
						String numAut = row[6].toString();
						String metodo = row[7].toString();
						String nomMetodo = row[8].toString();

						bwr.write(tienda + "," + nombreTienda + "," + fecha + "," + hora + "," + referencia + "," + num
								+ "," + valorFinal + "," + numAut + "," + metodo + "," + nomMetodo + ",");
						bwr.newLine();

					}
					if (bwr != null) {
						bwr.close();
					}
				}

				return true;

			} catch (Exception e) {
				logger.info("Error durante el proceso de creacion del archivo Bwise para la tienda: " + tienda);
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public boolean updatePagoPinpadLote(Session session, Integer idTrn, Integer seqNum, Integer numLote) {
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createSQLQuery(
					"UPDATE CO_TND_PINPAD SET LOTE = :valor1 WHERE ID_TRN = " + idTrn + " and SQ_NBR = " + seqNum);
			query.setParameter("valor1", numLote);
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}
	
	private List<Object[]> getDataFastData(Session sessionArts, String fechaContable, Integer store) {
		try {
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT CO_TND_PINPAD.TID, CO_TND_PINPAD.FECHA_TRX, CO_TND_PINPAD.HORA_TRX, CO_TND_PINPAD.SEQ_TRX, CO_TND_PINPAD.NUM_AUTO, PA_BIN_CPID.CD_CARDPID, CO_TND_PINPAD.MONTO + CO_TND_PINPAD.INTERES as MONTO, "
							+ "CO_TND_PINPAD.INTERES, CO_TND_PINPAD.COD_DIF, CO_TND_PINPAD.PLZ_DIF, CO_TND_PINPAD.IVA_TRX, CO_TND_PINPAD.MONTO_B_NO_GRAB_IVA, CO_TND_PINPAD.MONTO_B_GRAB_IVA, CO_TND_PINPAD.ID_TRN, CO_TND_PINPAD.SQ_NBR, "
							+ "PA_BIN_CPID.ORD_RECAP AS ORD_RECAP, (CASE WHEN CO_TND_PINPAD.COD_DIF <> '00' THEN PA_BIN_CPID.ID_CTAB_DIF ELSE PA_BIN_CPID.ID_CTAB END) AS ID_CTAB FROM CO_TND_PINPAD, TR_TRN, TR_LTM_RTL_TRN, PA_STR_RTL, TR_LTM_TND LEFT JOIN PA_BIN ON SUBSTRING (CONVERT(VARCHAR,CONVERT(INT, SUBSTRING(TR_LTM_TND.ID_ACNT_TND,1,8))), 1, 6) = PA_BIN.CD_BIN "
							+ "LEFT JOIN PA_BIN_RANGO ON CONVERT(INT, SUBSTRING (CONVERT(VARCHAR,CONVERT(INT, SUBSTRING(TR_LTM_TND.ID_ACNT_TND,1,8))), 1, 6)) >= PA_BIN_RANGO.INICIO AND CONVERT(INT, SUBSTRING (CONVERT(VARCHAR,CONVERT(INT, SUBSTRING(TR_LTM_TND.ID_ACNT_TND,1,8))), 1, 6)) <= PA_BIN_RANGO.FIN "
							+ "AND (PA_BIN.CD_BIN IS NULL OR (PA_BIN.CD_BIN < PA_BIN_RANGO.INICIO AND PA_BIN.CD_BIN > PA_BIN_RANGO.FIN)) LEFT JOIN PA_BIN_CPID ON PA_BIN.CD_CARDPID=PA_BIN_CPID.CD_CARDPID  OR PA_BIN_CPID.CD_CARDPID = PA_BIN_RANGO.CD_CARDPID WHERE  PA_STR_RTL.ID_BSN_UN = TR_TRN.ID_BSN_UN "
							+ "and TR_LTM_RTL_TRN.ID_TRN = TR_TRN.ID_TRN and TR_LTM_RTL_TRN.AI_LN_ITM = TR_LTM_TND.AI_LN_ITM "
							+ "and TR_TRN.ID_TRN = CO_TND_PINPAD.ID_TRN and CO_TND_PINPAD.ID_TRN = TR_LTM_TND.ID_TRN and CO_TND_PINPAD.SQ_NBR = TR_LTM_TND.AI_LN_ITM and CO_TND_PINPAD.COD_ADQ = 1 and CO_TND_PINPAD.FL_RV = 0 and TR_TRN.FL_CNCL = 0 "
							+ " and TR_TRN.FL_VD = 0 and TR_LTM_RTL_TRN.FL_VD_LN_ITM = 0 and TR_TRN.TY_TRN = 1 and PA_STR_RTL.CD_STR_RT = "
							+ store + " and TR_TRN.DC_DY_BSN = '" + fechaContable + "' Order By ID_CTAB, ORD_RECAP, PA_BIN_CPID.CD_CARDPID, CO_TND_PINPAD.COD_DIF, CO_TND_PINPAD.PLZ_DIF ASC");

			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getClaroData(Session sessionArts, String fechaContable,	Integer store) {
		try {
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT R.NUM_AUTO, T.TS_TRN_END, R.MONTO, S.MO_TX, R.HORA_TRX, W.CD_WS, O.CD_OPR, R.REF, PR.FL_PIN, R.NUM FROM TR_TRN T, CO_REC_ELEC R, AS_WS W, PA_OPR O, TR_LTM_SLS_RTN S, PA_REC_OPER RO, PA_REC_ELEC PR, AS_ITM I, PA_STR_RTL PA WHERE PA.ID_BSN_UN = T.ID_BSN_UN AND T.ID_TRN = R.ID_TRN AND T.ID_WS=W.ID_WS AND T.ID_OPR=O.ID_OPR AND T.ID_TRN = S.ID_TRN AND R.CD_ITM = I.CD_ITM AND R.SQ_NBR = S.AI_LN_ITM  AND I.ID_ITM = S.ID_ITM AND I.CD_ITM = PR.CD_ITM AND PR.ID_OPE = RO.ID_OPE AND RO.DES_OPE = 'CLARO' and PA.CD_STR_RT = "
							+ store + " and T.DC_DY_BSN = '" + fechaContable + "' and T.FL_CNCL = 0 and T.FL_VD = 0 Order By T.TS_TRN_END ASC");
			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getBwiseData(Session sessionArts, String fechaContable, Integer store) {
		try {
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT ST.DE_STR_RT, T.TS_TRN_END, R.HORA_TRX, R.REF, R.NUM, (LS.MO_PRC_REG + LS.MO_TX) as MONTO, R.NUM_AUTO, M.DES_METNV, M.NM_METNV FROM TR_TRN T, CO_REC_ELEC R, PA_REC_OPER RO, PA_REC_ELEC PR, PA_STR_RTL ST, PA_REC_METNV M , PA_STR_RTL PA, AS_ITM IT, TR_LTM_SLS_RTN LS WHERE PA.ID_BSN_UN = T.ID_BSN_UN AND T.ID_TRN = R.ID_TRN AND R.CD_ITM = PR.CD_ITM AND PR.CD_ITM = IT.CD_ITM AND IT.ID_ITM = LS.ID_ITM AND R.ID_TRN = LS.ID_TRN AND R.SQ_NBR = LS.AI_LN_ITM AND PR.ID_OPE = RO.ID_OPE AND T.ID_BSN_UN = ST.ID_BSN_UN AND PR.MET_ENVIO = M.ID_METNV AND RO.CD_ADQ = 5 and PA.CD_STR_RT = "
							+ store + " and T.DC_DY_BSN = '" + fechaContable + "' and T.FL_CNCL = 0 and T.FL_VD = 0 Order By T.TS_TRN_END ASC");
			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public Date getFechaLastCierre(Session sessionArts, Integer trx) {
		try {

			SQLQuery query = sessionArts.createSQLQuery("Select DC_DY_BSN From TR_TRN Where ID_TRN  = " + trx);
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return (Date) rows.get(0);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private List<String> getMedianetDataHeader(Session sessionArts, String fechaContable,
			Integer store, String tid) {
		try {
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT case when COUNT (CO_TND_PINPAD.ID_TRN) is null then 0 else COUNT (CO_TND_PINPAD.ID_TRN) end as cant,case when SUM(CO_TND_PINPAD.MONTO) is null then 0 else SUM(CO_TND_PINPAD.MONTO) end as total FROM CO_TND_PINPAD, TR_LTM_TND, PA_BIN, TR_TRN, TR_LTM_RTL_TRN, PA_STR_RTL WHERE PA_STR_RTL.ID_BSN_UN = TR_TRN.ID_BSN_UN and TR_LTM_RTL_TRN.ID_TRN = TR_TRN.ID_TRN and TR_LTM_RTL_TRN.AI_LN_ITM = TR_LTM_TND.AI_LN_ITM and TR_TRN.ID_TRN = CO_TND_PINPAD.ID_TRN and CO_TND_PINPAD.ID_TRN = TR_LTM_TND.ID_TRN and CO_TND_PINPAD.SQ_NBR = TR_LTM_TND.AI_LN_ITM and SUBSTRING (TR_LTM_TND.ID_ACNT_TND, 1, 6) = PA_BIN.CD_BIN and PA_BIN.HOST = 'MED' and TR_TRN.FL_CNCL = 0 and TR_TRN.FL_VD = 0 and TR_LTM_RTL_TRN.FL_VD_LN_ITM = 0 and CO_TND_PINPAD.FL_RV = 0 and TR_TRN.TY_TRN = 1 and PA_STR_RTL.CD_STR_RT = "
							+ store + " and TR_TRN.DC_DY_BSN = '"+fechaContable+"' and CO_TND_PINPAD.TID = '" + tid + "'");
			List<Object[]> rows = query.list();
			for (Object[] row : rows) {
				List<String> retorno = new ArrayList<String>();
				retorno.add(row[0] != null ? row[0].toString() : null);
				retorno.add(row[1] != null ? row[1].toString() : null);
				return retorno;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private List<String> getDataStoreMedianet(Session sessionSaadmin, Integer tienda) {
		try {

			SQLQuery query = sessionSaadmin.createSQLQuery(
					"Select MN_TIENDA.DES_TIENDA, PM_CIUDAD.DES_CIUDAD From MN_TIENDA, PM_CIUDAD Where MN_TIENDA.COD_CIUDAD = PM_CIUDAD.COD_CIUDAD and MN_TIENDA.DES_CLAVE = "
							+ tienda);
			List<Object[]> rows = query.list();
			for (Object[] row : rows) {
				List<String> retorno = new ArrayList<String>();
				retorno.add(row[0] != null ? row[0].toString() : null);
				retorno.add(row[1] != null ? row[1].toString() : null);
				return retorno;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	private List<Object[]> getMedianetData(Session sessionArts, String fechaContable,
			Integer store) {
		try {
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT TR_LTM_TND.ID_ACNT_TND, CO_TND_PINPAD.FECHA_TRX, CO_TND_PINPAD.HORA_TRX, CO_TND_PINPAD.SEQ_TRX, CO_TND_PINPAD.NUM_AUTO, CO_TND_PINPAD.MONTO, CO_TND_PINPAD.COD_DIF, CO_TND_PINPAD.PLZ_DIF, CO_TND_PINPAD.IVA_TRX, CO_TND_PINPAD.MSM_GRACIA, CO_TND_PINPAD.MONTO_B_NO_GRAB_IVA, CO_TND_PINPAD.MONTO_B_GRAB_IVA, CO_TND_PINPAD.ID_TRN, CO_TND_PINPAD.SQ_NBR, CO_TND_PINPAD.TID FROM CO_TND_PINPAD, TR_LTM_TND, PA_BIN, TR_TRN, TR_LTM_RTL_TRN, PA_STR_RTL WHERE PA_STR_RTL.ID_BSN_UN = TR_TRN.ID_BSN_UN and TR_LTM_RTL_TRN.ID_TRN = TR_TRN.ID_TRN and TR_LTM_RTL_TRN.AI_LN_ITM = TR_LTM_TND.AI_LN_ITM and TR_TRN.ID_TRN = CO_TND_PINPAD.ID_TRN and CO_TND_PINPAD.ID_TRN = TR_LTM_TND.ID_TRN and CO_TND_PINPAD.SQ_NBR = TR_LTM_TND.AI_LN_ITM and SUBSTRING (TR_LTM_TND.ID_ACNT_TND, 1, 6) = PA_BIN.CD_BIN and PA_BIN.HOST = 'MED' and TR_TRN.FL_CNCL = 0 and TR_TRN.FL_VD = 0 and TR_LTM_RTL_TRN.FL_VD_LN_ITM = 0 and TR_TRN.TY_TRN = 1 and CO_TND_PINPAD.COD_ADQ = 2 and CO_TND_PINPAD.FL_RV = 0 and PA_STR_RTL.CD_STR_RT = "
							+ store + " and TR_TRN.DC_DY_BSN = '"+fechaContable+"' Order By CO_TND_PINPAD.TID, CO_TND_PINPAD.FECHA_TRX ASC");

			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public Integer getLastIDCierreAntesLastConciliacion(Session sessionArts, Date dateLastConciliacion, Integer store) {
		try {

			String fecha = formatterLastID.format(dateLastConciliacion);
			SQLQuery query = sessionArts.createSQLQuery(
					"Select  case  when MAX (TR_BSN_EOD.ID_TRN) IS NULL then 0 else MAX (TR_BSN_EOD.ID_TRN) end From TR_TRN, TR_BSN_EOD, PA_STR_RTL Where TR_BSN_EOD.ID_TRN=TR_TRN.ID_TRN and TR_TRN.ID_BSN_UN=PA_STR_RTL.ID_BSN_UN and PA_STR_RTL.CD_STR_RT =  "
							+ store + "  and TR_TRN.TS_TRN_BGN <= convert(datetime,'" + fecha + "',121)");
			List rows = query.list();
			logger.info("rows: " + rows.get(0));
			if (rows != null && !rows.isEmpty())
				return (Integer) rows.get(0);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}
	
	public Date getLastDateConciliacion(Session sessionSaadmin, String proveedor, Integer tienda)
			throws ParseException {
		try {

			SQLQuery query = sessionSaadmin.createSQLQuery(
					"Select LST_CN_DATE From CFG_CN_CNF Where SPR_NM  = '" + proveedor + "' and DES_CLAVE = " + tienda);
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return (Date) rows.get(0);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		Date fechaAyer = cal.getTime();

		insertNewCnlCfg(sessionSaadmin, proveedor, tienda, fechaAyer);
		return null;
	}
	
	public boolean insertNewCnlCfg(Session sessionSaadmin, String proveedor, Integer tienda, Date fecha) {
		Transaction tx = null;
		try {
			tx = sessionSaadmin.beginTransaction();
			Query query = sessionSaadmin.createSQLQuery(
					"INSERT INTO CFG_CN_CNF (SPR_NM, DES_CLAVE, LST_CN_DATE) VALUES (:valor1, :valor2, :valor3)");
			query.setParameter("valor1", proveedor);
			query.setParameter("valor2", tienda);
			query.setParameter("valor3", fecha);
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}

	public Integer getLastIDCierre(Session sessionArts, Integer store) {
		try {
			SQLQuery query = sessionArts.createSQLQuery(
					"Select  case  when MAX (TR_BSN_EOD.ID_TRN) IS NULL then 0 else MAX (TR_BSN_EOD.ID_TRN) end  From TR_BSN_EOD, TR_TRN, PA_STR_RTL Where TR_BSN_EOD.ID_TRN=TR_TRN.ID_TRN and TR_TRN.ID_BSN_UN=PA_STR_RTL.ID_BSN_UN and PA_STR_RTL.CD_STR_RT = "
							+ store);
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return (Integer) rows.get(0);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}

	private List<String> getDatosByTienda(Session sessionEyes, Integer tienda) {
		try {

			SQLQuery query = sessionEyes.createSQLQuery(
					"Select NUM_LOT_R_DATA, NUM_LOT_R_MED, MID_DATA, MID_MED From CFG_LOTE_PINPAD Where DES_CLAVE = "
							+ tienda);
			List<Object[]> rows = query.list();
			for (Object[] row : rows) {
				List<String> retorno = new ArrayList<String>();
				retorno.add(row[0] != null ? row[0].toString() : null);
				retorno.add(row[1] != null ? row[1].toString() : null);
				retorno.add(row[2] != null ? row[2].toString() : null);
				retorno.add(row[3] != null ? row[3].toString() : null);
				return retorno;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	protected Session iniciarSesionSaadmin() {

		Session sessionSaadmin = null;

		while (sessionSaadmin == null) {
			try {
				sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (sessionSaadmin == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}

		return sessionSaadmin;
	}

	protected Session iniciarSesionArts() {

		Session sessionArts = null;

		while (sessionArts == null) {
			try {
				sessionArts = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (sessionArts == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}

		return sessionArts;
	}

	protected Session iniciarSesionEyes() {

		Session sessionEyes = null;

		while (sessionEyes == null) {
			try {
				sessionEyes = HibernateSessionFactoryContainer.getSessionFactory("Eyes").openSession();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (sessionEyes == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}

		return sessionEyes;
	}
	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		logger.info("Deteniendo GenerateConciliacionesOperation...");
		while (!finished)
			try {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= timeToWait) {
					return false;
				}
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		logger.info("Finalizo la Operacion de Generacion de archivos de Conciliacion.");
		return true;
	}

}
