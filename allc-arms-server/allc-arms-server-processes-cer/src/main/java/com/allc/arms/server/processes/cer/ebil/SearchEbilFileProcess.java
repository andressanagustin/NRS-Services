/**
 * 
 */
package com.allc.arms.server.processes.cer.ebil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
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
import com.allc.files.helper.FilesHelper;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * @author gustavo
 *
 */
public class SearchEbilFileProcess extends AbstractProcess {
	private static Logger log = Logger.getLogger(SearchEbilFileProcess.class);

	private File syncWithCentral;
	private int sleepTime;
	private Iterator filesToProcess = null;
	public boolean isEnd = false;
	protected String storeCode;
	protected boolean finished = false;
	protected ConnSocketClient socketClient = null;
	protected ConnSocketClient socketClientB = null;
	private Session session = null;
	private Session sessionArts = null;
	protected StoreDAO storeDAO = new StoreDAO();
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	protected HashMap activeStoreIp = new HashMap();
	protected HashMap outFolders = new HashMap();
	protected List pathActiveStore = new ArrayList();
	private int storeCodeHasta;
	private File outFolderConci;
	protected DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
	protected DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	protected int retries;
	private String ipFactElect = null;
	private String usrFactElect = null;
	private String pswFactElect = null;
	private String pathFactElect = null;
	private File bkpFolderFactElect;
	private static Pattern pFrame = Pattern.compile(ArmsServerConstants.Communication.REGEX);

	protected void inicializar() {
		isEnd = false;
		try {
			storeCode = properties.getObject("eyes.store.code");
			retries = properties.getInt("conciliacion.retries");
			while (storeCode.length() < 3)
				storeCode = "0" + storeCode;
			ParamsDAO paramsDAO = new ParamsDAO();
			ParamValue paravalue = null;

			sleepTime = properties.getInt("searchEbil.sleeptime");

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
						outFolders.put(codTienda,
								properties.getObject("searchEbil.out.folder.path") + File.separator + codTienda);

						paravalue = paramsDAO.getParamByClave(session, Integer.valueOf(codTienda).toString(),
								ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "SYNC_OUT");
						String dirTemp = properties.getObject("SUITE_ROOT") + paravalue.getValor() + codTienda
								+ File.separator + properties.getObject("searchEbil.in.folder");
						(new File(dirTemp)).mkdirs();
						pathActiveStore.add(dirTemp);

					}
				} else {
					log.info("No existen tiendas activas disponibles.");
				}
			} else {
				String storeIP = storeDAO.getStoreByCode(session, Integer.valueOf(storeCode)).getIp();
				activeStoreIp.put(storeCode, storeIP);
				outFolders.put(storeCode,
						properties.getObject("searchEbil.out.folder.path") + File.separator + storeCode);
				paravalue = paramsDAO.getParamByClave(session, Integer.valueOf(storeCode).toString(),
						ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "SYNC_OUT");
				pathActiveStore.add(properties.getObject("SUITE_ROOT") + File.separator + paravalue.getValor()
						+ File.separator + storeCode + File.separator + properties.getObject("searchEbil.in.folder"));
			}
			storeCodeHasta = properties.getObject("searchEbil.in.folder").length() + 1;
			syncWithCentral = new File(properties.getObject("searchEbil.out.folder.path") + File.separator
					+ properties.getObject("searchEbil.sync.folder.path"));
			syncWithCentral.mkdirs();

			outFolderConci = new File(properties.getObject("conciliacion.out.folder.facelect"));
			outFolderConci.mkdirs();

			bkpFolderFactElect = new File(properties.getObject("conciliacion.bkp.folder.facelect"));
			bkpFolderFactElect.mkdirs();

			ipFactElect = properties.getObject("sendFilesFactElect.server.serverFTP");
			usrFactElect = properties.getObject("sendFilesFactElect.username");
			pswFactElect = properties.getObject("sendFilesFactElect.password");
			pathFactElect = properties.getObject("sendFilesFactElect.ftp.folder.path");

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void run() {
		log.info("Iniciando SearchEbilFileProcess...");
		iniciarSesion("Saadmin");
		iniciarSesionArts();
		inicializar();
		while (!isEnd) {
			String filename = null;
			boolean processOK = false;
			try {
				File ebilFile = getNextEbilFile();

				if (ebilFile != null) {
					// copiar archivo a carpeta de sync con central
					if (Integer.valueOf(storeCode) > 0) {
						String absolutePath = ebilFile.getAbsolutePath();
						String filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
						FilesHelper.copyFile(filePath, syncWithCentral.getAbsolutePath(), ebilFile.getName(),
								ebilFile.getName());
					}
					filename = ebilFile.getName().toUpperCase();
					UtilityFile
							.createWriteDataFile(getEyesFileName(),
									"SRCH_EBIL_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + storeCode + "|STR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
													.format(new Date())
											+ "|Archivo a Procesar: " + filename + ".\n",
									true);
					log.info("Archivo a procesar: " + filename);
					BufferedReader br = new BufferedReader(new FileReader(ebilFile));
					String pos = null;
//					log.info("POS: " + pos);
					String linea = br.readLine();
					if (linea != null) {
						String name = linea.trim();
						log.info("Línea leida: " + name);
						String folder = null;

						// obtener del ebilFile el codigo de la tienda
						String path = ebilFile.getAbsolutePath();
						String pathFinal = path.substring(0, path.lastIndexOf(File.separator));
						String tiendaCode = pathFinal.substring(pathFinal.length() - (storeCodeHasta + 3),
								pathFinal.length() - storeCodeHasta);
						String tiendaFile = tiendaCode;
						while(tiendaFile.length()<3)
							tiendaFile = "0" + tiendaFile;
						String secondLine = br.readLine();
						pos = secondLine.substring(13, 16);
						log.info("POS: " + pos);
						if (storeCode.equals("000")) {
							// if (!storeDAO.hayServidorLocal(session,
							// Integer.valueOf(tiendaCode))) {
							if (!storeDAO.hayServidorLocal(session, Integer.valueOf(tiendaCode))
									&& name.startsWith("NCR")) {

								String sequence = null;
								String tramaSyscard = null;
								sequence = obtenerSec(tiendaCode);

								if (sequence != null)
									tramaSyscard = actualizarTrama(secondLine.trim(), sequence);
								if (tramaSyscard != null) {
									log.info("Se envia la trama a syscard para nota de credito");
									enviarTramaToSyscard(tramaSyscard);
								}
							}

							String fecha = linea.substring(25, 29) + "-" + linea.substring(23, 25) + "-"
									+ linea.substring(21, 23);
							Date date = inputFormat.parse(fecha);
							String dateInvoice = outputFormat.format(date);
							String[] partsSecondLine = secondLine.split("\\|");
							
								
							
						} else if (name.startsWith("NCR")) {

							String sequence = null;
							String tramaSyscard = null;
							sequence = obtenerSec(tiendaCode);

							if (sequence != null)
								tramaSyscard = actualizarTrama(secondLine.trim(), sequence);
							if (tramaSyscard != null) {
								log.info("Se envia la trama a syscard para nota de credito");
								enviarTramaToSyscard(tramaSyscard);
							}
						}

						File outFolder = new File((String) outFolders.get(tiendaCode));
						if (name.startsWith("FCT"))
							folder = outFolder.getAbsolutePath() + File.separator + pos + File.separator + "FCT";
						else if (name.startsWith("NCR"))
							folder = outFolder.getAbsolutePath() + File.separator + pos + File.separator + "NCR";
						else{
							folder = outFolder.getAbsolutePath() + File.separator + pos + File.separator + "RCT";
							name.replaceFirst("RCT", "FCT");
						}
						br.close();
						FilesHelper.copyFileAndRemoveSpecificLine(pathFinal, folder, ebilFile.getName(), name, 0);
						log.info("Archivo procesado.");
						if (storeDAO.hayServidorLocal(session, Integer.valueOf(tiendaCode))
								|| enviarTrama(filename, tiendaCode)) {
							/* Creacion archivo fe por tienda */
							log.info("Creacion archivo fe por tienda.");

							String feFileName = "fe" + tiendaFile + linea.substring(23, 25)
									+ linea.substring(21, 23) + ".txt";
							File feArchivo = new File(outFolderConci, feFileName);
							FileWriter writerFe = new FileWriter(feArchivo, true);
							BufferedWriter bwr = new BufferedWriter(writerFe);
							
							String fecha = linea.substring(25, 29) + "-" + linea.substring(23, 25) + "-"
									+ linea.substring(21, 23);
							
							String[] partsSecondLine = secondLine.split("\\|");

							String hora = linea.substring(29, 31) + ":" + linea.substring(31, 33) + ":"
									+ linea.substring(33, 35);
							String numDoc = partsSecondLine[6];
							String tipoDoc = "01";

							bwr.write(fecha + "," + hora + "," + numDoc + "," + tipoDoc + ",");
							bwr.newLine();
							if (bwr != null) {
								bwr.close();
							}
							writerFe.close();
							/* Creacion archivo do por tienda */
							log.info("Creacion archivo do por tienda.");
							String doFileName = "do" + tiendaFile + linea.substring(23, 25)
									+ linea.substring(21, 23) + ".txt";

							File doArchivo = new File(outFolderConci, doFileName);
							FileWriter writerDo = new FileWriter(doArchivo, true);
							BufferedWriter bwr2 = new BufferedWriter(writerDo);
							bwr2.write(secondLine);
							bwr2.newLine();
							if (bwr2 != null) {
								bwr2.close();
							}
							writerDo.close();
							ebilFile.delete();
							processOK = true;
							UtilityFile.createWriteDataFile(getEyesFileName(),
									"SRCH_EBIL_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + storeCode + "|END|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
													.format(new Date())
											+ "|Archivo: " + filename + " procesado.\n",
									true);
							log.info("Archivo: " + filename + " procesado.");
						} else {
							File newFile = null;
							newFile = new File(folder + File.separator + name);
							newFile.delete();
							UtilityFile.createWriteDataFile(getEyesFileName(),
									"SRCH_EBIL_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + storeCode + "|WAR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
													.format(new Date())
											+ "|Error al solicitar borrado de Archivo: " + filename + ".\n",
									true);
							log.info("Error al solicitar el borrado de Archivo: " + filename + ".");
						}
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					UtilityFile
							.createWriteDataFile(getEyesFileName(),
									"SRCH_EBIL_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + storeCode + "|ERR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(
													new Date())
											+ "|Error al procesar el archivo: " + filename + ".\n",
									true);
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
			}
			try {
				if (!processOK)
					Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
		finished = true;
	}

	private boolean enviarTrama(String filename, String tienda) {
		try {
			String tiendaIP = (String) activeStoreIp.get(tienda);
			connectClient(tiendaIP);
			String today = ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date());
			StringBuffer message = new StringBuffer();
			message.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(ArmsServerConstants.Process.UPDATE_EBIL_FILE_PROCESS)
					.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
					.append(ArmsServerConstants.Communication.FRAME_SEP).append(storeCode)
					.append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(ArmsServerConstants.Communication.TEMP_CONN)
					.append(ArmsServerConstants.Communication.FRAME_SEP).append(today)
					.append(ArmsServerConstants.Communication.FRAME_SEP).append(filename);
			List list = Arrays.asList(p.split(message.toString()));
			Frame frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
					ArmsServerConstants.Communication.FRAME_SEP);
			if (frame.loadData()) {
				String trama = Util.addLengthStartOfString(frame.getString().toString(),
						properties.getInt("serverSocket.quantityBytesLength")).toString();
				log.info("IP:" + tiendaIP);
				log.info("Port:" + socketClient.getPortServer());
				log.info("Trama a enviar: " + trama);
				if (socketClient.writeDataSocket(trama)) {
					Frame frameRta = leerRespuesta();
					if(frameRta != null){
						frameRta.loadData();
						if (frameRta.getStatusTrama() == 0) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			closeConnection();
		}
		return false;
	}

	protected Frame leerRespuesta() {
		int numberOfBytes = socketClient.readLengthDataSocket();
		if (numberOfBytes > 0) {
			String str = socketClient.readDataSocket(numberOfBytes);
			if (StringUtils.isNotBlank(str)) {
				List list = Arrays.asList(p.split(str));
				Frame frameRpta = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
						ArmsServerConstants.Communication.FRAME_SEP);
				log.info("Respuesta recibida: " + frameRpta.toString());
				return frameRpta;
			}
		}
		if (numberOfBytes == 0)
			numberOfBytes = socketClient.readLengthDataSocket();

		log.info("No se recibio respuesta.");
		return null;
	}

	protected boolean connectClient(String ip) {
		if (socketClient != null && !socketClient.getIpServer().equals(ip)) {
			if (socketClient.isConnected())
				socketClient.closeConnection();
			socketClient = null;
		}
		if (socketClient == null || !socketClient.isConnected()) {
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(ip);
			socketClient.setPortServer(properties.getInt("clientSocket.port"));
			socketClient.setRetries(properties.getInt("clientSocket.retries"));
			socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}

	protected void closeConnection() {
		if (socketClient != null)
			socketClient.closeConnection();
	}

	protected void iniciarSesion(String name) {
		while (session == null) {
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

	protected void iniciarSesionArts() {
		while (sessionArts == null) {
			try {
				sessionArts = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionArts == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	private File getNextEbilFile() {

		File inFolder = null;

		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd)
					return null;
				Iterator itInFolders = pathActiveStore.iterator();
				File[] files = null;
				List filesStore = new ArrayList();
				int count = 0;
				while (itInFolders.hasNext()) {
					inFolder = new File((String) itInFolders.next());
					File[] tempFiles = inFolder.listFiles(new FileFilter() {
						public boolean accept(File pathname) {
							return pathname.isFile() && !pathname.getName().toUpperCase().startsWith("TMP") && (pathname.getName().indexOf(".") == pathname.getName().lastIndexOf("."));
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

				} else {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						log.error(e.getMessage(), e);
					}
				}

			} while (((this.filesToProcess == null) || !filesToProcess.hasNext()));
		}
		return (File) this.filesToProcess.next();
	}

	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo SearchEbilFileProcess...");
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
		log.info("Finalizó el Proceso de Búsqueda de Archivos de Facturas.");
		return true;
	}

	public Double getMontoFactPorFecha(String fechaContable, Integer store) {
		try {

			log.info("Fecha para la consulta FACT: " + fechaContable);
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT case when sum(case when TR_LTM_TND.FL_IS_CHNG=0 then TR_LTM_TND.MO_ITM_LN_TND else -TR_LTM_TND.MO_ITM_LN_TND end) is null then 0 else sum(case when TR_LTM_TND.FL_IS_CHNG=0 then TR_LTM_TND.MO_ITM_LN_TND else -TR_LTM_TND.MO_ITM_LN_TND end) end as total  FROM TR_INVC, TR_LTM_TND, TR_TRN, PA_STR_RTL, TR_LTM_RTL_TRN WHERE TR_INVC.ID_TRN = TR_LTM_TND.ID_TRN and TR_LTM_TND.ID_TRN = TR_TRN.ID_TRN and TR_LTM_RTL_TRN.ID_TRN = TR_LTM_TND.ID_TRN and TR_LTM_RTL_TRN.AI_LN_ITM = TR_LTM_TND.AI_LN_ITM and TR_TRN.ID_BSN_UN=PA_STR_RTL.ID_BSN_UN and TR_LTM_RTL_TRN.FL_VD_LN_ITM = 0 and PA_STR_RTL.CD_STR_RT =  "
							+ store + " and CAST(TR_TRN.DC_DY_BSN AS DATE) = CAST(convert(datetime,'" + fechaContable
							+ "',121) AS DATE)");
			List rows = query.list();
			log.info("rows: " + rows.get(0));
			if (rows != null && !rows.isEmpty())
				return Double.valueOf(rows.get(0).toString());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public Double getMontoNcPorFecha(String fechaContable, Integer store) {
		try {

			log.info("Fecha para la consulta NC: " + fechaContable);
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT case when sum(case when TR_LTM_TND.FL_IS_CHNG = 1 then TR_LTM_TND.MO_ITM_LN_TND end) is null then 0 else sum(case when TR_LTM_TND.FL_IS_CHNG = 1 then TR_LTM_TND.MO_ITM_LN_TND end) end as total FROM TR_RTN, TR_LTM_TND, TR_TRN, PA_STR_RTL,  TR_LTM_RTL_TRN WHERE TR_RTN.ID_TRN = TR_LTM_TND.ID_TRN and TR_LTM_TND.ID_TRN = TR_TRN.ID_TRN and TR_LTM_RTL_TRN.ID_TRN = TR_LTM_TND.ID_TRN and TR_LTM_RTL_TRN.AI_LN_ITM = TR_LTM_TND.AI_LN_ITM and TR_TRN.ID_BSN_UN=PA_STR_RTL.ID_BSN_UN and TR_LTM_RTL_TRN.FL_VD_LN_ITM = 0 and PA_STR_RTL.CD_STR_RT =  "
							+ store + " and CAST(TR_TRN.DC_DY_BSN AS DATE) = CAST(convert(datetime,'" + fechaContable
							+ "',121) AS DATE)");
			List rows = query.list();
			log.info("rows: " + rows.get(0));
			if (rows != null && !rows.isEmpty())
				return Double.valueOf(rows.get(0).toString());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public void enviarTramaToSyscard(String trama) {

		StringBuffer mensaje = new StringBuffer("");
		mensaje.append(trama);
		String mjeLength = StringUtils.leftPad(Integer.toHexString(mensaje.length()), 4, "0");

		String msjEnviarHex = strToHexa(trama);
		StringBuffer mensajeEnviar = new StringBuffer("");
		mensajeEnviar.append(msjEnviarHex);
		mensajeEnviar.insert(0, mjeLength);

		log.info("Mensaje a Enviar a Syscard: " + mensajeEnviar.toString());

		ConnSocketClient socketClient = new ConnSocketClient();
		socketClient.setIpServer("172.29.3.10");
		socketClient.setPortServer(4009);
		socketClient.setRetries(3);
		socketClient.setTimeOutConnection(20000);
		socketClient.setTimeOutSleep(600);
		socketClient.setQuantityBytesLength(2);
		String data = null;

		int cant;
		try {

			if (socketClient.connectSocketUsingRetries()
					&& socketClient.writeByteArraySocket(strNumToByteAry(mensajeEnviar.toString()))) {

				log.info("Leyendo respuesta de Syscard.");
				if (!socketClient.timeOutSocket()) {
					cant = socketClient.leeLongitudDataHexaSocket();
					log.info("Cantidad a leer(dec):" + cant);
					data = socketClient.readDataSocket(cant);
					log.info("Respuesta recibida:" + data);
				} else {
					UtilityFile
							.createWriteDataFile(getEyesFileName(),
									"SRCH_EBIL_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + storeCode + "|WAR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(
													new Date())
											+ "|Syscard no respondio, pero la trama se enviò.\n",
									true);

				}
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(),
						"SRCH_EBIL_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ storeCode + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|No hay conexión con Syscard.",
						true);

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}

	}

	public String strToHexa(String str) {
		String result = "";
		byte[] s = str.getBytes();
		for (int i = 0; i < s.length; i++) {
			int decimal = (int) s[i];
			result = result + Integer.toString(decimal, 16);
		}
		return result;
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

	public boolean sendFilesForConciliacion(String nombreFileToSend, File outFolder) {

		FTPClient ftpClient = null;
		int reintentos = 0;
		log.info("Inicio de transferencia de archivo: " + nombreFileToSend);

		while (reintentos < retries) {
			log.info("Archivo a transferir: " + nombreFileToSend);
			try {
				if (ipFactElect != null && usrFactElect != null && pswFactElect != null && pathFactElect != null) {
					ftpClient = new FTPClient();
					ftpClient.connect(ipFactElect);
					ftpClient.login(usrFactElect, pswFactElect);
					ftpClient.enterLocalPassiveMode();
					ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
					File firstLocalFile = new File(outFolder, nombreFileToSend);
					log.info("File local: " + firstLocalFile.getAbsolutePath());
					String firstRemoteFile = pathFactElect + nombreFileToSend;
					log.info("File remoto: " + firstRemoteFile);
					InputStream inputStream = new FileInputStream(firstLocalFile);
					boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
					inputStream.close();
					try {
						if (ftpClient != null && ftpClient.isConnected()) {
							ftpClient.logout();
							ftpClient.disconnect();
						}
					} catch (IOException ex) {
						log.error(ex.getMessage(), ex);
					}
					if (done) {
						log.info("Archivo transferido con exito.");
						return true;
					} else
						reintentos++;
				} else
					reintentos = retries;
			} catch (Exception e) {
				log.error("Se produjo un error durante el intento de transferencia.");
				log.error(e.getMessage(), e);
				reintentos++;

			}
		}
		return false;
	}

	public String obtenerSec(String tienda) {

		String secuencia = null;
		try {
			String tiendaIP = (String) activeStoreIp.get(tienda);
			secuencia = null;
			StringBuffer data = new StringBuffer();
			data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(ArmsServerConstants.Process.CONSULTA_SEQ_SYSCARD)
					.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
					.append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
					.append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(ArmsServerConstants.Communication.TEMP_CONN)
					.append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
					.append(ArmsServerConstants.Communication.FRAME_SEP).append("1");

			List list = Arrays.asList(pFrame.split(data.toString()));
			Frame frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
					ArmsServerConstants.Communication.FRAME_SEP);
			if (frame.loadData()) {
				secuencia = sendConsultaToAgent(tiendaIP, frame);
				closeConnectionControler();
				if (secuencia != null) {
					log.info("Secuencia obtenida en forma correcta.");
					return secuencia;
				} else {
					log.info("La secuencian no pudo ser obtenida en forma correcta.");
				}
			}
		} catch (Exception e) {
			log.error("Se produjo un error durante el intento de obtener el numero de secuencia.");
			log.error(e.getMessage(), e);
		}

		return secuencia;
	}

	public String actualizarTrama(String trama, String secuencia) {

		String tramaToSyscard = null;

		tramaToSyscard = trama.substring(0, 17) + StringUtils.leftPad(secuencia, 6, "0") + trama.substring(17);

		return tramaToSyscard;
	}

	protected String sendConsultaToAgent(String ip, Frame frame) {
		String str;
		List list;
		Frame frameRpta;
		String secuencia = null;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClientB == null || !socketClientB.isConnected())
				connectControler(ip);
			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
			log.info("Trama a enviar: " + trama);
			if (socketClientB.writeDataSocket(trama)) {
				int numberOfBytes = 0;
				int timeOutCycles = 0;
				while (numberOfBytes == 0) {
					numberOfBytes = socketClientB.readLengthDataSocket();
					if (timeOutCycles == 5) {
						// cada 5 timeouts escribimos una trama vacía para
						// asegurarnos que el socket esté activo
						String mje = Util.addLengthStartOfString("", qtyBytesLength).toString();
						if (!socketClientB.writeDataSocket(mje)) {
							socketClientB.setConnected(false);
							return secuencia;
						}
						timeOutCycles = 0;
					}
					timeOutCycles++;
				}
				if (numberOfBytes > 0) {
					str = socketClientB.readDataSocket(numberOfBytes);
					if (StringUtils.isNotBlank(str)) {
						list = Arrays.asList(p.split(str));
						frameRpta = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
								ArmsServerConstants.Communication.FRAME_SEP);
						log.info("Respuesta recibida: " + frameRpta.toString());
						secuencia = (String) frameRpta.getBody().get(0);
						return secuencia;

					}
				}
			} else {
				socketClientB.setConnected(false);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			socketClientB.setConnected(false);
		}
		return secuencia;
	}

	protected boolean connectControler(String ip) {
		if (socketClientB == null) {
			socketClientB = new ConnSocketClient();
			socketClientB.setIpServer(ip);
			socketClientB.setPortServer(properties.getInt("clientSocket.port"));
			socketClientB.setRetries(properties.getInt("clientSocket.retries"));
			socketClientB.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClientB.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClientB.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
		}
		return socketClientB.connectSocketUsingRetries();
	}

	protected void closeConnectionControler() {
		if (socketClientB != null)
			socketClientB.closeConnection();
	}

}
