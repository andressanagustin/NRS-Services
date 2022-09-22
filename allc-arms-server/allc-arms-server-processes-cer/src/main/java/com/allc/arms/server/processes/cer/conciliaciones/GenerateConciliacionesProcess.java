package com.allc.arms.server.processes.cer.conciliaciones;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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
import com.allc.arms.server.persistence.conciliacion.Conciliacion;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.server.processes.cer.itemUpdate.EmailSender;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.Files;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;

public class GenerateConciliacionesProcess extends AbstractProcess {

	protected static Logger logger = Logger.getLogger(GenerateConciliacionesProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	public boolean isEnd = false;
	private File outFolderDatafast;
	private File outFolderMedianet;
	private File outFolderClaro;
	private File outFolderBwise;
	private File bkpFolderDatafast;
	private File bkpFolderMedianet;
	private File bkpFolderClaro;
	private File bkpFolderBwise;
	private int sleepTime;
	protected ConnSocketClient socketClient = null;
	protected boolean finished = false;
	protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	// Map hashPinpadsDatafast = null;
	// Map hashPinpadsMedianet = null;
	List pinpads = null;
	StoreDAO storeDAO = new StoreDAO();
	ParamsDAO paramsDAO = new ParamsDAO();
	private int retries;
	File datafastFile;
	File medianetFile;
	File claroFile;
	File bwiseFile;
	protected SimpleDateFormat formatterClaro = new SimpleDateFormat("yyyyMMdd");
	protected SimpleDateFormat formatterJulianDate = new SimpleDateFormat("DDD");
	protected SimpleDateFormat formatterLastID = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private final static long ONCE_PER_WEEK_BWISE = 1000 * 60 * 60 * 24 * 7;
	private final static long ONCE_PER_HOUR_CLARO = 1000 * 60 * 60;
	Timer timerBwise = new Timer();
	Timer timerDataMediaClaro = new Timer();
	// Timer timerClaro = new Timer();
	Timer timerSendFileDataMed = new Timer();
	private EmailSender emailSender = new EmailSender();
	private int horaDataMedClaro;
	private int minDataMedClaro;
	private int horaBwise;
	private int minBwise;
	private int diaBwise;
	private int horaEnvioDataMed;
	private int minEnvioDataMed;
	private String ipDatafast = null;
	private String ipMedianet = null;
	private String ipClaro = null;
	private String ipBwise = null;
	private String usrDatafast = null;
	private String usrMedianet = null;
	private String usrClaro = null;
	private String usrBwise = null;
	private String pswDatafast = null;
	private String pswMedianet = null;
	private String pswClaro = null;
	private String pswBwise = null;
	private String pathDatafast = null;
	private String pathMedianet = null;
	private String pathClaro = null;
	private String pathBwise = null;
	private File inFolderDestinoLocal;
	File recapFile;
	String storeCentralIP = null;
	String storeLocalIP = null;

	protected void inicializar() {
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

	public void run() {
		logger.info("Iniciando GenerateConciliacionesProcess...");
		inicializar();
		final String storeCode = StringUtils.leftPad(properties.getObject("eyes.store.code"), 3, "0");

		try {

			Date horaDatafastMedianetClaro = new Date(System.currentTimeMillis());

			Calendar c = Calendar.getInstance();
			c.setTime(horaDatafastMedianetClaro);
			// Si la hora es posterior a las 10am se programa para el dia
			// siguiente
			if ((c.get(Calendar.HOUR_OF_DAY) > horaDataMedClaro) || (c.get(Calendar.HOUR_OF_DAY) == horaDataMedClaro && c.get(Calendar.MINUTE) == minDataMedClaro)) {
				c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + 1);
			}

			c.set(Calendar.HOUR_OF_DAY, horaDataMedClaro);
			c.set(Calendar.MINUTE, minDataMedClaro);
			c.set(Calendar.SECOND, 0);
			// c.set(Calendar.HOUR_OF_DAY, 8);
			// c.set(Calendar.MINUTE, 40);
			// c.set(Calendar.SECOND, 0);

			horaDatafastMedianetClaro = c.getTime();
			long tiempoRepeticion = 86400000;
			TimerTask timerTaskDataMediaClaro = new TimerTask() {
				public void run() {

					Session sessionSaadmin = null;
					Session sessionArts = null;
					Session sessionEyes = null;
					sessionSaadmin = iniciarSesionSaadmin();
					sessionArts = iniciarSesionArts();
					sessionEyes = iniciarSesionEyes();
					
					storeCentralIP = storeDAO.getStoreByCode(sessionSaadmin, 0).getIp();
					
					ParamValue paravalue = paramsDAO.getParValSpecifiByClave(sessionSaadmin,
							Integer.valueOf(properties.getObject("eyes.store.code")),
							ArmsServerConstants.AmbitoParams.ARMS_AGENT_PARAMS, "clientSocket.ip");

					storeLocalIP = paravalue.getValor();
					
					try {
						List activeStores = null;
						
						if(storeCode.equals("000")){
							activeStores = storeDAO.getAllActiveStore(sessionSaadmin);
						}else{
							activeStores = new ArrayList();
							Store local = storeDAO.getStoreByCode(sessionSaadmin, Integer.valueOf(storeCode));
							activeStores.add(local);
						}
						
						List<String> archivoCreadosDataFast = new ArrayList();
						List<String> archivoCreadosMedianet = new ArrayList();
						Map tamArchivoCreadosDataFast = new HashMap();
						Map tamArchivoCreadosMedianet = new HashMap();
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
									String loteDatafast = datosTienda.get(0);
									String loteMedianet = datosTienda.get(1);
									String midDatafast = datosTienda.get(2);
									String midMedianet = datosTienda.get(3);
									
									Date now = new Date();
								    String strDate = formatterClaro.format(now);
									recapFile = new File("C:/ALLC/WWW/EYES/CNL/RECAP_FILE_TO_LOCAL_" + codTiendaAux + "_" + strDate);

									Integer lastIDCierrre = getLastIDCierre(sessionArts, Integer.valueOf(codTiendaAux));
									Date fechaLastCnlDataFast = getLastDateConciliacion(sessionSaadmin, "DATAFAST",
											Integer.valueOf(codTiendaAux));
									Integer lastIdCierreAntesCnlDataFast = getLastIDCierreAntesLastConciliacion(
											sessionArts, fechaLastCnlDataFast, Integer.valueOf(codTiendaAux));
									boolean creadoData = crearArchivoDataFast(sessionArts, codTiendaAux, lastIDCierrre,
											lastIdCierreAntesCnlDataFast, loteDatafast, midDatafast);

									if (creadoData)
										updateDateConclByProvStore(sessionSaadmin, "DATAFAST", new Date(),
												Integer.valueOf(codTiendaAux));


									Date fechaLastCnlMedianet = getLastDateConciliacion(sessionSaadmin, "MEDIANET",
											Integer.valueOf(codTiendaAux));
									// lastIDCierrre = 8052;
									Integer lastIdCierreAntesCnlMedianet = getLastIDCierreAntesLastConciliacion(
											sessionArts, fechaLastCnlMedianet, Integer.valueOf(codTiendaAux));
									boolean creadoMedia = crearArchivoMedianet(sessionArts, sessionSaadmin,
											codTiendaAux, lastIDCierrre, lastIdCierreAntesCnlMedianet, loteMedianet,
											midMedianet);

									if (creadoMedia)
										updateDateConclByProvStore(sessionSaadmin, "MEDIANET", new Date(),
												Integer.valueOf(codTiendaAux));
	
									if(!storeCode.equals("000")){
										boolean recapSendToTienda = enviarArchivoACentral(recapFile, tiendaAux);
										if(recapSendToTienda){
											activeOperationRecapInCentral(recapFile, tiendaAux);
										}
									}
									
									Date fechaLastCnlClaro = getLastDateConciliacion(sessionSaadmin, "CLARO",
											Integer.valueOf(codTiendaAux));
									Integer lastIdCierreAntesCnlClaro = getLastIDCierreAntesLastConciliacion(
											sessionArts, fechaLastCnlClaro, Integer.valueOf(codTiendaAux));
									// lastIDCierrre = 5598;
									boolean creadoClaro = crearArchivoClaro(sessionArts, codTiendaAux, lastIDCierrre,
											lastIdCierreAntesCnlClaro);
									if (creadoClaro && claroFile != null) {
										updateDateConclByProvStore(sessionSaadmin, "CLARO", new Date(),
												Integer.valueOf(codTiendaAux));

										boolean enviado = sendFilesForConciliacion(sessionSaadmin, "CLARO",
												claroFile.getName(), outFolderClaro);
										if (enviado) {
											String absolutePathClaroOri = outFolderClaro.getAbsolutePath();
											String absolutePathClaroDes = bkpFolderClaro.getAbsolutePath();
											
											File out = new File(bkpFolderClaro, claroFile.getName());
											claroFile.renameTo(out);
											UtilityFile.createWriteDataFile(getEyesFileName(),
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
											UtilityFile.createWriteDataFile(getEyesFileName(),
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

								} catch (Exception e) {
									logger.error(e.getMessage(), e);
								}
							}
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					} finally {
						if (sessionArts != null && sessionSaadmin != null && sessionEyes != null) {
							sessionArts.close();
							sessionSaadmin.close();
							sessionEyes.close();
						}
					}
				}
			};

			timerDataMediaClaro.schedule(timerTaskDataMediaClaro, horaDatafastMedianetClaro, tiempoRepeticion);

			// Calendar date = Calendar.getInstance();
			// date.set(Calendar.DAY_OF_WEEK, diaBwise);
			// date.set(Calendar.HOUR_OF_DAY, horaBwise);
			// date.set(Calendar.MINUTE, minBwise);
			// date.set(Calendar.SECOND, 0);
			// date.set(Calendar.HOUR_OF_DAY, 8);
			// date.set(Calendar.MINUTE, 20);
			// date.set(Calendar.SECOND, 0);

			Date horaBwiseTimer = new Date(System.currentTimeMillis());

			Calendar cBwise = Calendar.getInstance();
			c.setTime(horaBwiseTimer);

			if ((cBwise.get(Calendar.DAY_OF_WEEK) == diaBwise && ((cBwise.get(Calendar.HOUR_OF_DAY) == horaBwise && cBwise.get(Calendar.MINUTE) > minBwise) || (cBwise.get(Calendar.HOUR_OF_DAY) > horaBwise))) || (cBwise.get(Calendar.DAY_OF_WEEK) > diaBwise)) {
				cBwise.set(Calendar.WEEK_OF_YEAR, cBwise.get(Calendar.WEEK_OF_YEAR) + 1);
			}
			cBwise.set(Calendar.DAY_OF_WEEK, diaBwise);
			cBwise.set(Calendar.HOUR_OF_DAY, horaBwise);
			cBwise.set(Calendar.MINUTE, minBwise);
			cBwise.set(Calendar.SECOND, 0);
			// c.set(Calendar.HOUR_OF_DAY, 8);
			// c.set(Calendar.MINUTE, 40);
			// c.set(Calendar.SECOND, 0);

			horaBwiseTimer = cBwise.getTime();
			TimerTask timerTaskBwise = new TimerTask() {
				public void run() {

					Session sessionSaadmin = null;
					Session sessionArts = null;

					sessionSaadmin = iniciarSesionSaadmin();
					sessionArts = iniciarSesionArts();

					try {
						List activeStores = storeDAO.getAllActiveStores(sessionSaadmin);

						if (activeStores != null && !activeStores.isEmpty()) {
							Iterator itTienda = activeStores.iterator();
							while (itTienda.hasNext()) {
								Store tiendaAux = (Store) itTienda.next();
								String codTiendaAux = tiendaAux.getKey().toString();
								while (codTiendaAux.length() < 3)
									codTiendaAux = "0" + codTiendaAux;

								Integer lastIDCierrre = getLastIDCierre(sessionArts, Integer.valueOf(codTiendaAux));
								Date fechaLastCnlBwise = getLastDateConciliacion(sessionSaadmin, "BWISE",
										Integer.valueOf(codTiendaAux));
								Integer lastIdCierreAntesCnlBwise = getLastIDCierreAntesLastConciliacion(sessionArts,
										fechaLastCnlBwise, Integer.valueOf(codTiendaAux));
								boolean creadoBwise = crearArchivoBwise(sessionArts, codTiendaAux, lastIDCierrre,
										lastIdCierreAntesCnlBwise);

								if (creadoBwise && bwiseFile != null) {
									boolean enviado = sendFilesForConciliacion(sessionSaadmin, "BWISE",
											bwiseFile.getName(), outFolderBwise);
									if (enviado) {
										String absolutePathBwiseOri = outFolderBwise.getAbsolutePath();
										String absolutePathBwiseDes = bkpFolderBwise.getAbsolutePath();
										File out = new File(bkpFolderBwise, bwiseFile.getName());
										bwiseFile.renameTo(out);
										UtilityFile.createWriteDataFile(getEyesFileName(),
												"GEN_CCL_P|" + properties.getHostName() + "|3|"
														+ properties.getHostAddress() + "|" + codTiendaAux + "|STR|"
														+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
																.format(new Date())
														+ "|Archivo de Bwise " + bwiseFile.getName() + "enviado con exito"
														+ ".\n",
												true);
										logger.info("Archivo " + bwiseFile.getName() + " enviado con exito.");
									} else{
										UtilityFile.createWriteDataFile(getEyesFileName(),
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
								updateDateConclByProvStore(sessionSaadmin, "BWISE", new Date(),
										Integer.valueOf(codTiendaAux));
							}

						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					} finally {
						if (sessionArts != null && sessionSaadmin != null) {
							sessionArts.close();
							sessionSaadmin.close();
						}
					}

				}
			};
			timerBwise.schedule(timerTaskBwise, horaBwiseTimer, ONCE_PER_WEEK_BWISE);

			
			Date horaSendFilesDataMed = new Date(System.currentTimeMillis());
			Calendar dateSendFilesDataMed = Calendar.getInstance();
			if ((dateSendFilesDataMed.get(Calendar.HOUR_OF_DAY) == horaEnvioDataMed && dateSendFilesDataMed.get(Calendar.MINUTE) > minEnvioDataMed) || (dateSendFilesDataMed.get(Calendar.HOUR_OF_DAY) > horaEnvioDataMed)) {
				dateSendFilesDataMed.set(Calendar.DAY_OF_YEAR, dateSendFilesDataMed.get(Calendar.DAY_OF_YEAR) + 1);
			}
			dateSendFilesDataMed.set(Calendar.HOUR_OF_DAY, horaEnvioDataMed);
			dateSendFilesDataMed.set(Calendar.MINUTE, minEnvioDataMed);
			dateSendFilesDataMed.set(Calendar.SECOND, 0);
			horaSendFilesDataMed = dateSendFilesDataMed.getTime();
			TimerTask timerTaskSendFilesDatMed = new TimerTask() {
				public void run() {

					Session sessionSaadmin = null;
					Session sessionArts = null;

					try {

						sessionSaadmin = iniciarSesionSaadmin();
						sessionArts = iniciarSesionArts();

						File[] filesDatfast = outFolderDatafast.listFiles(new FileFilter() {
							public boolean accept(File pathname) {
								return pathname.isFile();
							}
						});

						if (filesDatfast != null && filesDatfast.length > 0) {
							String absolutePathDataOri = outFolderDatafast.getAbsolutePath();
							String absolutePathDataDes = bkpFolderDatafast.getAbsolutePath();
							for (int i = 0; i < filesDatfast.length; i++) {
								File fileDataSend = new File(filesDatfast[i].getAbsolutePath());
								boolean enviadoData = sendFilesForConciliacion(sessionSaadmin, "DATAFAST",
										fileDataSend.getName(), outFolderDatafast);
								if (enviadoData) {
									File out = new File(bkpFolderDatafast, fileDataSend.getName());
									fileDataSend.renameTo(out);
									logger.info("Archivo Datafast " + fileDataSend.getName() + " enviado con exito.");
									UtilityFile.createWriteDataFile(getEyesFileName(),
											"GEN_CCL_P|" + properties.getHostName() + "|3|"
													+ properties.getHostAddress() + "|" + 000 + "|STR|"
													+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
															.format(new Date())
													+ "|Archivo Datafast " + fileDataSend.getName() + " enviado con exito."
													+ ".\n",
											true);
								} else{
									UtilityFile.createWriteDataFile(getEyesFileName(),
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
							String absolutePathMedOri = outFolderMedianet.getAbsolutePath();
							String absolutePathMedDes = bkpFolderMedianet.getAbsolutePath();
							for (int i = 0; i < filesMedianet.length; i++) {
								File fileMedSend = new File(filesMedianet[i].getAbsolutePath());
								boolean enviadoMed = sendFilesForConciliacion(sessionSaadmin, "MEDIANET",
										fileMedSend.getName(), outFolderMedianet);
								if (enviadoMed) {
									File out = new File(bkpFolderMedianet, fileMedSend.getName());
									fileMedSend.renameTo(out);
									logger.info("Archivo Medianet " + fileMedSend.getName() + " enviado con exito.");
									UtilityFile.createWriteDataFile(getEyesFileName(),
											"GEN_CCL_P|" + properties.getHostName() + "|3|"
													+ properties.getHostAddress() + "|" + 000 + "|STR|"
													+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
															.format(new Date())
													+ "|Archivo Medianet " + fileMedSend.getName() + " enviado con exito."
													+ ".\n",
											true);

								} else {
									UtilityFile.createWriteDataFile(getEyesFileName(),
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

					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					} finally {
						if (sessionArts != null && sessionSaadmin != null) {
							sessionArts.close();
							sessionSaadmin.close();
						}
					}

				}

			};
			timerSendFileDataMed.schedule(timerTaskSendFilesDatMed, horaSendFilesDataMed, tiempoRepeticion);

		} catch (Exception e) {
			UtilityFile.createWriteDataFile(getEyesFileName(),
					"GEN_CCL_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode
							+ "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Error al generar conciliaciones.\n",
					true);
			logger.error(e.getMessage(), e);
		}
		while (!isEnd) {

			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}

			finished = true;
		}
	}

	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
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

	public List getAllPinPadByStore(Session sessionSaadmin, Integer store) {

		try {
			Query query = sessionSaadmin
					.createQuery("from com.allc.arms.server.persistence.pinpad.PinPad where codTienda = " + store);
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return rows;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		logger.info("Deteniendo GenerateConciliacionesProcess...");
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
		timerDataMediaClaro.cancel();
		timerBwise.cancel();
		timerSendFileDataMed.cancel();
		logger.info("Finalizó el Proceso de para Generacion de Conciliaciones.");
		return true;
	}

	public boolean crearArchivoDataFast(Session sessionArts, String tienda, Integer lastIDCierrre,
			Integer lastIdCierreAntesCnl, String loteDatafast, String midDatafast) {
		logger.info("Proceso de conciliación de Datafast para tienda: " + tienda);

		String lastCardID = "";
		String lastpPlzDif = "";
		String cardID = "";
		String plzDif = "";
		String tipoCred = "";
		String lastTipoCredito = "";
		String banco = "";
		String lastBanco = "";


		if (lastIDCierrre > 0 && lastIdCierreAntesCnl >= 0) {
			try {
				logger.info(
						"DATA DATAFAST TIENDA: " + tienda + " ENTRE : " + lastIdCierreAntesCnl + " y " + lastIDCierrre);
				List<Object[]> dataDataFast = getDataFastData(sessionArts, lastIDCierrre, lastIdCierreAntesCnl,
						Integer.valueOf(tienda));
				logger.info("CANTIDAD DE DATA DATAFAST ES: " + dataDataFast.size());
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
			Integer lastIDCierrre, Integer lastIdCierreAntesCnl, String loteMedianet, String midMedianet) {

		logger.info("Proceso de conciliación para Medianet para la tienda: " + tienda);
		
//		Date now = new Date();
//	    String strDate = formatterClaro.format(now);
//		File recapFile = new File("C:/ALLC/WWW/EYES/CNL/RECAP_FILE_TO_LOCAL_" + tienda + "_" + strDate);

		if (lastIDCierrre > 0 && lastIdCierreAntesCnl >= 0) {
			try {

				List<String> dataStore = getDataStoreMedianet(sessionSaadmin, Integer.valueOf(tienda));
				// List<String> dataMediaNetHeader =
				// getMedianetDataHeader(sessionArts, lastIDCierrre,
				// lastIdCierreAntesCnl, Integer.valueOf(tienda));
				logger.info(
						"DATA MEDIANET TIENDA: " + tienda + " ENTRE : " + lastIdCierreAntesCnl + " y " + lastIDCierrre);
				List<Object[]> dataMediaNet = getMedianetData(sessionArts, lastIDCierrre, lastIdCierreAntesCnl,
						Integer.valueOf(tienda));
				logger.info("CANTIDAD DE DATA MEDIANET ES: " + dataMediaNet.size());
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
					// if (pinpads.get(0) != null)
					// tid = ((PinPad) pinpads.get(0)).getTidMedianet();
					// else
					// tid = "";
					String lote = StringUtils.leftPad(loteMedianet, 7, "0");
					// String idCaja = StringUtils.leftPad(tid, 8, " ");
					// String cantTrx =
					// StringUtils.leftPad(dataMediaNetHeader.get(0), 6, "0");
					// int montoTotalAux =
					// Double.valueOf(dataMediaNetHeader.get(1)).intValue();
					// String montoTotal =
					// StringUtils.leftPad(String.valueOf(montoTotalAux), 13,
					// "0");
					String nomTienda = StringUtils.rightPad(dataStore.get(0), 25, " ");
					String ciudadTienda = StringUtils.rightPad(dataStore.get(1), 12, " ");
					// String reg_header = "C" + comercio + fechaAux + lote +
					// idCaja + cantTrx + montoTotal + nomTienda
					// + ciudadTienda + StringUtils.leftPad("", 114, " ");
					// bwr.write(reg_header);
					// bwr.newLine();

					BufferedWriter bwrRecapFile = new BufferedWriter(new FileWriter(recapFile, true));

					for (int i = 0; i < dataMediaNet.size(); i++) {
						Object[] row = dataMediaNet.get(i);

						tid = row[14].toString();

						if (!tid.equals(lastTid)) {
							List<String> dataMediaNetHeader = getMedianetDataHeader(sessionArts, lastIDCierrre,
									lastIdCierreAntesCnl, Integer.valueOf(tienda), tid);
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
					// String totalReg =
					// StringUtils.leftPad(String.valueOf(dataMediaNet.size() +
					// 2), 6, "0");
					// String reg_total = "TN" + fechaAux + lote + "000001" +
					// totalReg + StringUtils.leftPad("", 178, " ");
					// bwr.write(reg_total);
					// bwr.newLine();
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

	public boolean crearArchivoClaro(Session sessionArts, String tienda, Integer lastIDCierrre,
			Integer lastIdCierreAntesCnl) {

		logger.info("Proceso de conciliación para Claro para la tienda: " + tienda);

		while (tienda.length() < 4)
			tienda = "0" + tienda;

		if (lastIDCierrre > 0 && lastIdCierreAntesCnl >= 0) {
			try {
				logger.info(
						"DATA CLARO TIENDA: " + tienda + " ENTRE : " + lastIdCierreAntesCnl + " y " + lastIDCierrre);
				List<Object[]> dataDetalle = getClaroData(sessionArts, lastIDCierrre, lastIdCierreAntesCnl,
						Integer.valueOf(tienda));
				logger.info("CANTIDAD DE DATA CLARO ES: " + dataDetalle.size());
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

	public boolean crearArchivoBwise(Session sessionArts, String tienda, Integer lastIDCierrre,
			Integer lastIdCierreAntesCnl) {
		logger.info("Proceso de conciliación para Bwise para la tienda: " + tienda);

		if (lastIDCierrre > 0 && lastIdCierreAntesCnl >= 0) {
			logger.info("ID para buscar fecha de ultimo cierre: " + lastIDCierrre);
			Date fechaActual = getFechaLastCierre(sessionArts, lastIDCierrre);
			logger.info("fechaActual: " + fechaActual.toString());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String fechaAsString = sdf.format(fechaActual);
			while (tienda.length() < 4)
				tienda = "0" + tienda;
			String bwiseFileName = "ta" + tienda.substring(1, tienda.length()) + fechaAsString.substring(2, 4)
					+ fechaAsString.substring(4, 6) + fechaAsString.substring(6, 8) + ".txt";

			try {
				logger.info(
						"DATA BWISE TIENDA: " + tienda + " ENTRE : " + lastIdCierreAntesCnl + " y " + lastIDCierrre);
				List<Object[]> dataDetalle = getBwiseData(sessionArts, lastIDCierrre, lastIdCierreAntesCnl,
						Integer.valueOf(tienda));
				logger.info("CANTIDAD DE DATA BWISE ES: " + dataDetalle.size());
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

	private List<Object[]> getDataFastData(Session sessionArts, Integer lastCierreID, Integer lastCierrreAntesCnlID,
			Integer store) {
		try {

			logger.info(
					"SELECT CO_TND_PINPAD.TID, CO_TND_PINPAD.FECHA_TRX, CO_TND_PINPAD.HORA_TRX, CO_TND_PINPAD.SEQ_TRX, CO_TND_PINPAD.NUM_AUTO, PA_BIN_CPID.CD_CARDPID, CO_TND_PINPAD.MONTO + CO_TND_PINPAD.INTERES as MONTO, "
							+ "CO_TND_PINPAD.INTERES, CO_TND_PINPAD.COD_DIF, CO_TND_PINPAD.PLZ_DIF, CO_TND_PINPAD.IVA_TRX, CO_TND_PINPAD.MONTO_B_NO_GRAB_IVA, CO_TND_PINPAD.MONTO_B_GRAB_IVA, CO_TND_PINPAD.ID_TRN, CO_TND_PINPAD.SQ_NBR, "
							+ "PA_BIN_CPID.ORD_RECAP AS ORD_RECAP, (CASE WHEN CO_TND_PINPAD.COD_DIF <> '00' THEN PA_BIN_CPID.ID_CTAB_DIF ELSE PA_BIN_CPID.ID_CTAB END) AS ID_CTAB FROM CO_TND_PINPAD, TR_TRN, TR_LTM_RTL_TRN, PA_STR_RTL, TR_LTM_TND LEFT JOIN PA_BIN ON SUBSTRING (CONVERT(VARCHAR,CONVERT(INT, SUBSTRING(TR_LTM_TND.ID_ACNT_TND,1,8))), 1, 6) = PA_BIN.CD_BIN "
							+ "LEFT JOIN PA_BIN_RANGO ON CONVERT(INT, SUBSTRING (CONVERT(VARCHAR,CONVERT(INT, SUBSTRING(TR_LTM_TND.ID_ACNT_TND,1,8))), 1, 6)) >= PA_BIN_RANGO.INICIO AND CONVERT(INT, SUBSTRING (CONVERT(VARCHAR,CONVERT(INT, SUBSTRING(TR_LTM_TND.ID_ACNT_TND,1,8))), 1, 6)) <= PA_BIN_RANGO.FIN "
							+ "AND (PA_BIN.CD_BIN IS NULL OR (PA_BIN.CD_BIN < PA_BIN_RANGO.INICIO AND PA_BIN.CD_BIN > PA_BIN_RANGO.FIN)) LEFT JOIN PA_BIN_CPID ON PA_BIN.CD_CARDPID=PA_BIN_CPID.CD_CARDPID  OR PA_BIN_CPID.CD_CARDPID = PA_BIN_RANGO.CD_CARDPID WHERE  PA_STR_RTL.ID_BSN_UN = TR_TRN.ID_BSN_UN "
							+ "and TR_LTM_RTL_TRN.ID_TRN = TR_TRN.ID_TRN and TR_LTM_RTL_TRN.AI_LN_ITM = TR_LTM_TND.AI_LN_ITM "
							+ "and TR_TRN.ID_TRN = CO_TND_PINPAD.ID_TRN and CO_TND_PINPAD.ID_TRN = TR_LTM_TND.ID_TRN and CO_TND_PINPAD.SQ_NBR = TR_LTM_TND.AI_LN_ITM and CO_TND_PINPAD.COD_ADQ = 1 and CO_TND_PINPAD.FL_RV = 0 and TR_TRN.FL_CNCL = 0 "
							+ " and TR_TRN.FL_VD = 0 and TR_LTM_RTL_TRN.FL_VD_LN_ITM = 0 and TR_TRN.TY_TRN = 1 and PA_STR_RTL.CD_STR_RT = "
							+ store + " and CO_TND_PINPAD.ID_TRN > " + lastCierrreAntesCnlID
							+ " and CO_TND_PINPAD.ID_TRN < " + lastCierreID
							+ "Order By ID_CTAB, ORD_RECAP, PA_BIN_CPID.CD_CARDPID, CO_TND_PINPAD.COD_DIF, CO_TND_PINPAD.PLZ_DIF ASC");
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT CO_TND_PINPAD.TID, CO_TND_PINPAD.FECHA_TRX, CO_TND_PINPAD.HORA_TRX, CO_TND_PINPAD.SEQ_TRX, CO_TND_PINPAD.NUM_AUTO, PA_BIN_CPID.CD_CARDPID, CO_TND_PINPAD.MONTO + CO_TND_PINPAD.INTERES as MONTO, "
							+ "CO_TND_PINPAD.INTERES, CO_TND_PINPAD.COD_DIF, CO_TND_PINPAD.PLZ_DIF, CO_TND_PINPAD.IVA_TRX, CO_TND_PINPAD.MONTO_B_NO_GRAB_IVA, CO_TND_PINPAD.MONTO_B_GRAB_IVA, CO_TND_PINPAD.ID_TRN, CO_TND_PINPAD.SQ_NBR, "
							+ "PA_BIN_CPID.ORD_RECAP AS ORD_RECAP, (CASE WHEN CO_TND_PINPAD.COD_DIF <> '00' THEN PA_BIN_CPID.ID_CTAB_DIF ELSE PA_BIN_CPID.ID_CTAB END) AS ID_CTAB FROM CO_TND_PINPAD, TR_TRN, TR_LTM_RTL_TRN, PA_STR_RTL, TR_LTM_TND LEFT JOIN PA_BIN ON SUBSTRING (CONVERT(VARCHAR,CONVERT(INT, SUBSTRING(TR_LTM_TND.ID_ACNT_TND,1,8))), 1, 6) = PA_BIN.CD_BIN "
							+ "LEFT JOIN PA_BIN_RANGO ON CONVERT(INT, SUBSTRING (CONVERT(VARCHAR,CONVERT(INT, SUBSTRING(TR_LTM_TND.ID_ACNT_TND,1,8))), 1, 6)) >= PA_BIN_RANGO.INICIO AND CONVERT(INT, SUBSTRING (CONVERT(VARCHAR,CONVERT(INT, SUBSTRING(TR_LTM_TND.ID_ACNT_TND,1,8))), 1, 6)) <= PA_BIN_RANGO.FIN "
							+ "AND (PA_BIN.CD_BIN IS NULL OR (PA_BIN.CD_BIN < PA_BIN_RANGO.INICIO AND PA_BIN.CD_BIN > PA_BIN_RANGO.FIN)) LEFT JOIN PA_BIN_CPID ON PA_BIN.CD_CARDPID=PA_BIN_CPID.CD_CARDPID  OR PA_BIN_CPID.CD_CARDPID = PA_BIN_RANGO.CD_CARDPID WHERE  PA_STR_RTL.ID_BSN_UN = TR_TRN.ID_BSN_UN "
							+ "and TR_LTM_RTL_TRN.ID_TRN = TR_TRN.ID_TRN and TR_LTM_RTL_TRN.AI_LN_ITM = TR_LTM_TND.AI_LN_ITM "
							+ "and TR_TRN.ID_TRN = CO_TND_PINPAD.ID_TRN and CO_TND_PINPAD.ID_TRN = TR_LTM_TND.ID_TRN and CO_TND_PINPAD.SQ_NBR = TR_LTM_TND.AI_LN_ITM and CO_TND_PINPAD.COD_ADQ = 1 and CO_TND_PINPAD.FL_RV = 0 and TR_TRN.FL_CNCL = 0 "
							+ " and TR_TRN.FL_VD = 0 and TR_LTM_RTL_TRN.FL_VD_LN_ITM = 0 and TR_TRN.TY_TRN = 1 and PA_STR_RTL.CD_STR_RT = "
							+ store + " and CO_TND_PINPAD.ID_TRN > " + lastCierrreAntesCnlID
							+ " and CO_TND_PINPAD.ID_TRN < " + lastCierreID
							+ "Order By ID_CTAB, ORD_RECAP, PA_BIN_CPID.CD_CARDPID, CO_TND_PINPAD.COD_DIF, CO_TND_PINPAD.PLZ_DIF ASC");

			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getClaroData(Session sessionArts, Integer lastCierreID, Integer lastCierrreAntesCnlID,
			Integer store) {
		try {
			// SQLQuery query = sessionArts.createSQLQuery(
			// "SELECT R.NUM_AUTO, T.TS_TRN_END, R.MONTO, S.MO_TX, R.HORA_TRX,
			// W.CD_WS, O.CD_OPR, R.REF, PR.FL_PIN, R.NUM FROM TR_TRN T,
			// CO_REC_ELEC R, AS_WS W, PA_OPR O, TR_LTM_SLS_RTN S, PA_REC_OPER
			// RO, PA_REC_ELEC PR, AS_ITM I, PA_STR_RTL PA WHERE PA.ID_BSN_UN =
			// T.ID_BSN_UN AND T.ID_TRN = R.ID_TRN AND T.ID_WS=W.ID_WS AND
			// T.ID_OPR=O.ID_OPR AND T.ID_TRN = S.ID_TRN AND R.CD_ITM = I.CD_ITM
			// AND R.SQ_NBR = S.AI_LN_ITM AND I.ID_ITM = S.ID_ITM AND I.CD_ITM =
			// PR.CD_ITM AND PR.ID_OPE = RO.ID_OPE AND RO.DES_OPE = 'CLARO' and
			// PA.CD_STR_RT = 065 and T.ID_TRN > 4508 and T.ID_TRN <= 4529 Order
			// By T.TS_TRN_END ASC");
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT R.NUM_AUTO, T.TS_TRN_END, R.MONTO, S.MO_TX, R.HORA_TRX, W.CD_WS, O.CD_OPR, R.REF, PR.FL_PIN, R.NUM FROM TR_TRN T, CO_REC_ELEC R, AS_WS W, PA_OPR O, TR_LTM_SLS_RTN S, PA_REC_OPER RO, PA_REC_ELEC PR, AS_ITM I, PA_STR_RTL PA WHERE PA.ID_BSN_UN = T.ID_BSN_UN AND T.ID_TRN = R.ID_TRN AND T.ID_WS=W.ID_WS AND T.ID_OPR=O.ID_OPR AND T.ID_TRN = S.ID_TRN AND R.CD_ITM = I.CD_ITM AND R.SQ_NBR = S.AI_LN_ITM  AND I.ID_ITM = S.ID_ITM AND I.CD_ITM = PR.CD_ITM AND PR.ID_OPE = RO.ID_OPE AND RO.DES_OPE = 'CLARO' and PA.CD_STR_RT = "
							+ store + " and T.ID_TRN > " + lastCierrreAntesCnlID + " and T.ID_TRN <= " + lastCierreID
							+ " and T.FL_CNCL = 0 and T.FL_VD = 0 Order By T.TS_TRN_END ASC");
			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private List<Object[]> getBwiseData(Session sessionArts, Integer lastCierreID, Integer lastCierrreAntesCnlID,
			Integer store) {
		try {

			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT ST.DE_STR_RT, T.TS_TRN_END, R.HORA_TRX, R.REF, R.NUM, (LS.MO_PRC_REG + LS.MO_TX) as MONTO, R.NUM_AUTO, M.DES_METNV, M.NM_METNV FROM TR_TRN T, CO_REC_ELEC R, PA_REC_OPER RO, PA_REC_ELEC PR, PA_STR_RTL ST, PA_REC_METNV M , PA_STR_RTL PA, AS_ITM IT, TR_LTM_SLS_RTN LS WHERE PA.ID_BSN_UN = T.ID_BSN_UN AND T.ID_TRN = R.ID_TRN AND R.CD_ITM = PR.CD_ITM AND PR.CD_ITM = IT.CD_ITM AND IT.ID_ITM = LS.ID_ITM AND R.ID_TRN = LS.ID_TRN AND R.SQ_NBR = LS.AI_LN_ITM AND PR.ID_OPE = RO.ID_OPE AND T.ID_BSN_UN = ST.ID_BSN_UN AND PR.MET_ENVIO = M.ID_METNV AND RO.CD_ADQ = 5 and PA.CD_STR_RT = "
							+ store + " and T.ID_TRN > " + lastCierrreAntesCnlID + " and T.ID_TRN <= " + lastCierreID
							+ " and T.FL_CNCL = 0 and T.FL_VD = 0 Order By T.TS_TRN_END ASC");
			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private List<String> getMedianetDataHeader(Session sessionArts, Integer lastCierreID, Integer lastCierrreAntesCnlID,
			Integer store, String tid) {
		try {

			// SQLQuery query = sessionArts.createSQLQuery(
			// "SELECT case when COUNT (CO_TND_PINPAD.ID_TRN) is null then 0
			// else COUNT (CO_TND_PINPAD.ID_TRN) end as cant,case when
			// SUM(CO_TND_PINPAD.MONTO) is null then 0 else
			// SUM(CO_TND_PINPAD.MONTO) end as total FROM CO_TND_PINPAD,
			// TR_LTM_TND, PA_BIN, TR_TRN, TR_LTM_RTL_TRN, PA_STR_RTL WHERE
			// PA_STR_RTL.ID_BSN_UN = TR_TRN.ID_BSN_UN and TR_LTM_RTL_TRN.ID_TRN
			// = TR_TRN.ID_TRN and TR_LTM_RTL_TRN.AI_LN_ITM =
			// TR_LTM_TND.AI_LN_ITM and TR_TRN.ID_TRN = CO_TND_PINPAD.ID_TRN and
			// CO_TND_PINPAD.ID_TRN = TR_LTM_TND.ID_TRN and CO_TND_PINPAD.SQ_NBR
			// = TR_LTM_TND.AI_LN_ITM and SUBSTRING (TR_LTM_TND.ID_ACNT_TND, 1,
			// 6) = PA_BIN.CD_BIN and PA_BIN.HOST = 'MED' and TR_TRN.FL_CNCL = 0
			// and TR_TRN.FL_VD = 0 and TR_LTM_RTL_TRN.FL_VD_LN_ITM = 0 and
			// CO_TND_PINPAD.FL_RV = 0 and TR_TRN.TY_TRN = 1 and
			// PA_STR_RTL.CD_STR_RT = 065 and CO_TND_PINPAD.ID_TRN > 4587 and
			// CO_TND_PINPAD.ID_TRN < 4618");
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT case when COUNT (CO_TND_PINPAD.ID_TRN) is null then 0 else COUNT (CO_TND_PINPAD.ID_TRN) end as cant,case when SUM(CO_TND_PINPAD.MONTO) is null then 0 else SUM(CO_TND_PINPAD.MONTO) end as total FROM CO_TND_PINPAD, TR_LTM_TND, PA_BIN, TR_TRN, TR_LTM_RTL_TRN, PA_STR_RTL WHERE PA_STR_RTL.ID_BSN_UN = TR_TRN.ID_BSN_UN and TR_LTM_RTL_TRN.ID_TRN = TR_TRN.ID_TRN and TR_LTM_RTL_TRN.AI_LN_ITM = TR_LTM_TND.AI_LN_ITM and TR_TRN.ID_TRN = CO_TND_PINPAD.ID_TRN and CO_TND_PINPAD.ID_TRN = TR_LTM_TND.ID_TRN and CO_TND_PINPAD.SQ_NBR = TR_LTM_TND.AI_LN_ITM and SUBSTRING (TR_LTM_TND.ID_ACNT_TND, 1, 6) = PA_BIN.CD_BIN and PA_BIN.HOST = 'MED' and TR_TRN.FL_CNCL = 0 and TR_TRN.FL_VD = 0 and TR_LTM_RTL_TRN.FL_VD_LN_ITM = 0 and CO_TND_PINPAD.FL_RV = 0 and TR_TRN.TY_TRN = 1 and PA_STR_RTL.CD_STR_RT = "
							+ store + " and CO_TND_PINPAD.ID_TRN > " + lastCierrreAntesCnlID
							+ " and CO_TND_PINPAD.ID_TRN <= " + lastCierreID + "and CO_TND_PINPAD.TID = '" + tid + "'");
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

	private List<Object[]> getMedianetData(Session sessionArts, Integer lastCierreID, Integer lastCierrreAntesCnlID,
			Integer store) {
		try {
			// SQLQuery query = sessionArts.createSQLQuery(
			// "SELECT TR_LTM_TND.ID_ACNT_TND, CO_TND_PINPAD.FECHA_TRX,
			// CO_TND_PINPAD.HORA_TRX, CO_TND_PINPAD.SEQ_TRX,
			// CO_TND_PINPAD.NUM_AUTO, CO_TND_PINPAD.MONTO,
			// CO_TND_PINPAD.COD_DIF, CO_TND_PINPAD.PLZ_DIF,
			// CO_TND_PINPAD.IVA_TRX, CO_TND_PINPAD.MSM_GRACIA,
			// CO_TND_PINPAD.MONTO_B_NO_GRAB_IVA, CO_TND_PINPAD.MONTO_B_GRAB_IVA
			// FROM CO_TND_PINPAD, TR_LTM_TND, PA_BIN, TR_TRN, TR_LTM_RTL_TRN,
			// PA_STR_RTL WHERE PA_STR_RTL.ID_BSN_UN = TR_TRN.ID_BSN_UN and
			// TR_LTM_RTL_TRN.ID_TRN = TR_TRN.ID_TRN and
			// TR_LTM_RTL_TRN.AI_LN_ITM = TR_LTM_TND.AI_LN_ITM and TR_TRN.ID_TRN
			// = CO_TND_PINPAD.ID_TRN and CO_TND_PINPAD.ID_TRN =
			// TR_LTM_TND.ID_TRN and CO_TND_PINPAD.SQ_NBR = TR_LTM_TND.AI_LN_ITM
			// and SUBSTRING (TR_LTM_TND.ID_ACNT_TND, 1, 6) = PA_BIN.CD_BIN and
			// PA_BIN.HOST = 'MED' and TR_TRN.FL_CNCL = 0 and TR_TRN.FL_VD = 0
			// and TR_LTM_RTL_TRN.FL_VD_LN_ITM = 0 and TR_TRN.TY_TRN = 1 and
			// CO_TND_PINPAD.COD_ADQ = 2 and CO_TND_PINPAD.FL_RV = 0 and
			// PA_STR_RTL.CD_STR_RT = 065 and CO_TND_PINPAD.ID_TRN > 4587 and
			// CO_TND_PINPAD.ID_TRN < 4618 Order By CO_TND_PINPAD.FECHA_TRX
			// ASC");
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT TR_LTM_TND.ID_ACNT_TND, CO_TND_PINPAD.FECHA_TRX, CO_TND_PINPAD.HORA_TRX, CO_TND_PINPAD.SEQ_TRX, CO_TND_PINPAD.NUM_AUTO, CO_TND_PINPAD.MONTO, CO_TND_PINPAD.COD_DIF, CO_TND_PINPAD.PLZ_DIF, CO_TND_PINPAD.IVA_TRX, CO_TND_PINPAD.MSM_GRACIA, CO_TND_PINPAD.MONTO_B_NO_GRAB_IVA, CO_TND_PINPAD.MONTO_B_GRAB_IVA, CO_TND_PINPAD.ID_TRN, CO_TND_PINPAD.SQ_NBR, CO_TND_PINPAD.TID FROM CO_TND_PINPAD, TR_LTM_TND, PA_BIN, TR_TRN, TR_LTM_RTL_TRN, PA_STR_RTL WHERE PA_STR_RTL.ID_BSN_UN = TR_TRN.ID_BSN_UN and TR_LTM_RTL_TRN.ID_TRN = TR_TRN.ID_TRN and TR_LTM_RTL_TRN.AI_LN_ITM = TR_LTM_TND.AI_LN_ITM and TR_TRN.ID_TRN = CO_TND_PINPAD.ID_TRN and CO_TND_PINPAD.ID_TRN = TR_LTM_TND.ID_TRN and CO_TND_PINPAD.SQ_NBR = TR_LTM_TND.AI_LN_ITM and SUBSTRING (TR_LTM_TND.ID_ACNT_TND, 1, 6) = PA_BIN.CD_BIN and PA_BIN.HOST = 'MED' and TR_TRN.FL_CNCL = 0 and TR_TRN.FL_VD = 0 and TR_LTM_RTL_TRN.FL_VD_LN_ITM = 0 and TR_TRN.TY_TRN = 1 and CO_TND_PINPAD.COD_ADQ = 2 and CO_TND_PINPAD.FL_RV = 0 and PA_STR_RTL.CD_STR_RT = "
							+ store + " and CO_TND_PINPAD.ID_TRN > " + lastCierrreAntesCnlID
							+ " and CO_TND_PINPAD.ID_TRN < " + lastCierreID
							+ " Order By CO_TND_PINPAD.TID, CO_TND_PINPAD.FECHA_TRX ASC");

			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
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

	public Integer getLastIdCnfCnl(Session sessionSaadmin) {
		try {

			SQLQuery query = sessionSaadmin.createSQLQuery("select max (ID_CN_CNF) from CFG_CN_CNF");
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return (Integer) rows.get(0);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
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

	public Conciliacion getConciliacionByProveedor(Session sessionSaadmin, String proveedor) {
		Query query = sessionSaadmin
				.createQuery("from com.allc.arms.server.persistence.conciliacion.Conciliacion where proveedorName = '"
						+ proveedor + "' ");
		Iterator iterator = query.iterate();
		while (iterator.hasNext())
			return (Conciliacion) iterator.next();
		return null;
	}

	public Conciliacion getConciliacionByProveedorWithDate(Session sessionSaadmin, String proveedor,
			Date dateLastCierre) {
		Query query = sessionSaadmin
				.createQuery("from com.allc.arms.server.persistence.conciliacion.Conciliacion where proveedorName = '"
						+ proveedor + "' and posClosed = 1 and lastConciliacionDate < '" + dateLastCierre + "'");
		Iterator iterator = query.iterate();
		while (iterator.hasNext())
			return (Conciliacion) iterator.next();
		return null;
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

	public boolean updateDateConclByProvStore(Session sessionSaadmin, String proveedor1, String proveedor2, Date fecha,
			Integer tienda) {
		Transaction tx = null;
		try {
			tx = sessionSaadmin.beginTransaction();
			Query query = sessionSaadmin.createSQLQuery("UPDATE CFG_CN_CNF SET LST_CN_DATE = :valor1 WHERE SPR_NM = '"
					+ proveedor1 + "' and DES_CLAVE = " + tienda);
			query.setParameter("valor1", fecha);
			query.executeUpdate();
			Query query2 = sessionSaadmin.createSQLQuery("UPDATE CFG_CN_CNF SET LST_CN_DATE = :valor1 WHERE SPR_NM = '"
					+ proveedor2 + "' and DES_CLAVE = " + tienda);
			query2.setParameter("valor1", fecha);
			query2.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
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

	public boolean enviarArchivoACentral(File fileToSend, Store store) {

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
	
	public void activeOperationRecapInCentral(File fileToProcess, Store tienda){
		
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
				UtilityFile.createWriteDataFile(getEyesFileName(), "GEN_CCL_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+tienda.getKey().toString()+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Trama para procesar archivos de recap eviada con exito.\n", true);
				logger.info("Activación de Operacion RegisterRecap enviado correctamente a ArmsServerCentral.");
				
			} else {
				UtilityFile.createWriteDataFile(getEyesFileName(), "GEN_CCL_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+tienda.getKey().toString()+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Trama para procesar archivos de recap no enviada.\n", true);
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
}
