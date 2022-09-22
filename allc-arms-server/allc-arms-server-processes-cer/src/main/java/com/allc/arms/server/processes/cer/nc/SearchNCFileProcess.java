/**
 * 
 */
package com.allc.arms.server.processes.cer.nc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
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
import org.hibernate.Session;

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
public class SearchNCFileProcess extends AbstractProcess {
	private static Logger log = Logger.getLogger(SearchNCFileProcess.class);

	private File syncWithCentral;
	private int sleepTime;
	private Iterator filesToProcess = null;
	public boolean isEnd = false;
	protected String storeCode;
	protected boolean finished = false;
	protected ConnSocketClient socketClient = null;
	protected ConnSocketClient socketClientB = null;
	private Session session = null;
	protected StoreDAO storeDAO = new StoreDAO();
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	protected List pathActiveStore = new ArrayList();
	private int storeCodeHasta;
	private File outFolderConci;
	private File inFolder;
	private File outFolder;
	protected DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
	protected DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	protected int retries;
	private File bkpFolderFactElect;
	private static Pattern pFrame = Pattern.compile(ArmsServerConstants.Communication.REGEX);

	protected void inicializar() {
		isEnd = false;
		try {
			storeCode = properties.getObject("eyes.store.code");
			retries = properties.getInt("conciliacion.retries");
			while (storeCode.length() < 3)
				storeCode = "0" + storeCode;

			sleepTime = properties.getInt("searchEbil.sleeptime");

			storeCodeHasta = properties.getObject("searchEbil.in.folder").length() + 1;
			syncWithCentral = new File(properties.getObject("searchEbil.out.folder.path") + File.separator
					+ properties.getObject("searchEbil.sync.folder.path"));
			syncWithCentral.mkdirs();

			outFolderConci = new File(properties.getObject("conciliacion.out.folder.facelect"));
			outFolderConci.mkdirs();

			bkpFolderFactElect = new File(properties.getObject("conciliacion.bkp.folder.facelect"));
			bkpFolderFactElect.mkdirs();

			inFolder = new File(properties.getObject("searchNc.in.folder"));
			inFolder.mkdirs();
			outFolder = new File(properties.getObject("searchEbil.out.folder.path"));
			outFolder.mkdirs();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void run() {
		log.info("Iniciando SearchNCFileProcess...");
		iniciarSesion("Saadmin");
		inicializar();
		boolean tramaEnviada = false;
		String sequence = null;
		while (!isEnd) {
			String filename = null;
			boolean processOK = false;
			try {
				File ncFile = getNextNCFile();
				log.info("Directorio de sync with central: " + syncWithCentral.getAbsolutePath());
				if (ncFile != null) {
					// copiar archivo a carpeta de sync con central
					if (Integer.valueOf(storeCode) > 0) {
						String absolutePath = ncFile.getAbsolutePath();
						String filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
						FilesHelper.copyFile(filePath, syncWithCentral.getAbsolutePath(), ncFile.getName(),
								ncFile.getName());
					}
					filename = ncFile.getName().toUpperCase();
					UtilityFile
							.createWriteDataFile(getEyesFileName(),
									"SRCH_NC_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
											+ storeCode + "|STR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
													.format(new Date())
											+ "|Archivo a Procesar: " + filename + ".\n",
									true);
					log.info("Archivo a procesar: " + filename);
					BufferedReader br = new BufferedReader(new FileReader(ncFile));
					String[] parts = filename.split("\\.");
					String tiendaCode = parts[1];
					String tiendaFile = parts[1];
					while(tiendaFile.length()<3)
						tiendaFile = "0" + tiendaFile;
					String linea = br.readLine().replace("Ñ", "N").replace("ñ", "n");
					if (linea != null) {
						String name = linea.trim();
						log.info("Línea leida: " + name);
						String folder = null;

						// obtener del ncFile el codigo de la tienda
						String path = ncFile.getAbsolutePath();
						String pathFinal = path.substring(0, path.lastIndexOf(File.separator));
						//String tiendaCode = linea.substring(14, 18);

						String secondLine = br.readLine().replace("Ñ", "N").replace("ñ", "n");
						String pos = secondLine.substring(13, 16);
						String tramaSyscard = null;
						if (storeCode.equals("000")) {

							if (!storeDAO.hayServidorLocal(getSession(), Integer.valueOf(tiendaCode))) {

								sequence = obtenerSec(tiendaCode);
								if (sequence != null)
									tramaSyscard = actualizarTrama(secondLine.trim(), sequence);
								if (tramaSyscard != null) {
									log.info("Se envia la trama a syscard para nota de credito");
									tramaEnviada = enviarTramaToSyscard(tramaSyscard);
								}

								if (sequence != null) {
									String fecha = linea.substring(25, 29) + "-" + linea.substring(23, 25) + "-"
											+ linea.substring(21, 23);
									String[] partsSecondLine = tramaSyscard.split("\\|");
									/* Creacion archivo fe por tienda */
									log.info("Creacion archivo fe por tienda.");
									String feFileName = "fe" + tiendaFile + linea.substring(23, 25)
											+ linea.substring(21, 23) + ".txt";
									File feArchivo = new File(outFolderConci, feFileName);
									FileWriter writerFe = new FileWriter(feArchivo, true);
									BufferedWriter bwr = new BufferedWriter(writerFe);
									String hora = linea.substring(29, 31) + ":" + linea.substring(31, 33) + ":"
											+ linea.substring(33, 35);
									String numDoc = partsSecondLine[6];
									String tipoDoc = "04";
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
									bwr2.write(tramaSyscard);
									bwr2.newLine();
									if (bwr2 != null) {
										bwr2.close();
									}
									writerDo.close();
								}

							}

						} else {

							sequence = obtenerSec(tiendaCode);
							if (sequence != null)
								tramaSyscard = actualizarTrama(secondLine.trim(), sequence);
							if (tramaSyscard != null) {
								log.info("Se envia la trama a syscard para nota de credito");
								tramaEnviada = enviarTramaToSyscard(tramaSyscard);
							}

							if(sequence != null) {
								String fecha = linea.substring(25, 29) + "-" + linea.substring(23, 25) + "-"
										+ linea.substring(21, 23);
								String[] partsSecondLine = tramaSyscard.split("\\|");
								/* Creacion archivo fe por tienda */
								log.info("Creacion archivo fe por tienda.");
								String feFileName = "fe" + tiendaFile + linea.substring(23, 25)
										+ linea.substring(21, 23) + ".txt";
								File feArchivo = new File(outFolderConci, feFileName);
								BufferedWriter bwr = new BufferedWriter(new FileWriter(feArchivo, true));
								String hora = linea.substring(29, 31) + ":" + linea.substring(31, 33) + ":"
										+ linea.substring(33, 35);
								String numDoc = partsSecondLine[6];
								String tipoDoc = "04";
								bwr.write(fecha + "," + hora + "," + numDoc + "," + tipoDoc);
								bwr.newLine();
								if (bwr != null) {
									bwr.close();
								}
								/* Creacion archivo do por tienda */
								log.info("Creacion archivo do por tienda.");
								String doFileName = "do" + tiendaFile + linea.substring(23, 25)
										+ linea.substring(21, 23) + ".txt";
								File doArchivo = new File(outFolderConci, doFileName);
								BufferedWriter bwr2 = new BufferedWriter(new FileWriter(doArchivo, true));
								bwr2.write(tramaSyscard);
								bwr2.newLine();
								if (bwr2 != null) {
									bwr2.close();
								} 
							}

						}
						if (sequence != null) {
							if (name.startsWith("NCR"))
								folder = outFolder.getAbsolutePath() + File.separator + tiendaCode + File.separator + pos + File.separator + "NCR";
							br.close();
							File folderFile = new File(folder);
							folderFile.mkdirs();
							File fileToSave = new File(folderFile.getAbsolutePath(), linea);
							BufferedWriter bwrAux = new BufferedWriter(new FileWriter(fileToSave, true));
							bwrAux.write(tramaSyscard);
							bwrAux.newLine();
							if (bwrAux != null) {
								bwrAux.close();
							}
							ncFile.delete();
							log.info("Archivo procesado.");
						} else {
							log.info("OutFolder: " + outFolder.getAbsolutePath() + File.separator + tiendaCode + File.separator + pos + File.separator + "NCR");
							log.info("Archivo no pudo ser procesado.");
						}

					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					UtilityFile
							.createWriteDataFile(getEyesFileName(),
									"SRCH_NC_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
											+ storeCode + "|ERR|"
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
	
	protected Session getSession(){
		iniciarSesion("Saadmin");
		return session;
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


	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	private File getNextNCFile() {

		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd)
					return null;
				File[] files = inFolder.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isFile() && !pathname.getName().toUpperCase().startsWith("TMP");
					}
				});
				if (files.length == 0) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						log.error(e.getMessage(), e);
					}
				} else {
//					Arrays.sort(files, new Comparator() {
//						public int compare(Object obj1, Object obj2) {
//							String name1 = ((File) obj1).getName().toUpperCase();
//							int sequence1 = 0;
//							String name2 = ((File) obj2).getName().toUpperCase();
//							int sequence2 = 0;
//							sequence1 = Integer.parseInt(name1.substring(4, 8));
//							sequence2 = Integer.parseInt(name2.substring(4, 8));
//							if (sequence1 == sequence2) {
//								return 0;
//							}
//							if (sequence1 < sequence2) {
//								return -1;
//							}
//							return 1;
//						}
//					});
					this.filesToProcess = Arrays.asList(files).iterator();
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

	public boolean enviarTramaToSyscard(String trama) {

		StringBuffer mensaje = new StringBuffer("");
		mensaje.append(trama);
		String mjeLength = StringUtils.leftPad(Integer.toHexString(mensaje.length()), 4, "0");

		String msjEnviarHex = strToHexa(trama);
		StringBuffer mensajeEnviar = new StringBuffer("");
		mensajeEnviar.append(msjEnviarHex);
		mensajeEnviar.insert(0, mjeLength);

		log.info("Mensaje a Enviar a Syscard: " + mensajeEnviar.toString());

		ConnSocketClient socketClient = new ConnSocketClient();
		socketClient.setIpServer(properties.getObject("activateGiftcard.server.ip"));
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
					return true;
				} else {
					UtilityFile
							.createWriteDataFile(getEyesFileName(),
									"SRCH_EBIL_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + storeCode + "|WAR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(
													new Date())
											+ "|Syscard no respondio, pero la trama se enviò.\n",
									true);
					return true;
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
		return false;
	}

	public String strToHexa(String str) {
		String result = "";
		try {
			byte[] s = str.getBytes("ISO-8859-1");
			for (int i = 0; i < s.length; i++) {
				result = result + Integer.toString((s[i] << 0) & 0x000000ff, 16);
			}
		} catch (Exception e){
			log.error(e.getMessage(), e);
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

	public String obtenerSec(String tienda) {

		String secuencia = null;
		try {
			Store store = storeDAO.getStoreByCode(getSession(), Integer.valueOf(tienda));

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
					.append(ArmsServerConstants.Communication.FRAME_SEP).append("01");

			List list = Arrays.asList(pFrame.split(data.toString()));
			Frame frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
					ArmsServerConstants.Communication.FRAME_SEP);
			if (frame.loadData()) {
				secuencia = sendConsultaToAgent(store.getIp(), frame);
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

		tramaToSyscard = trama.substring(0, 17) + StringUtils.leftPad(secuencia, 6, "0") + trama.substring(23);

		return tramaToSyscard;
	}

	protected String sendConsultaToAgent(String ip, Frame frame) {
		String str;
		List list;
		Frame frameRpta;
		String secuencia = null;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
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
						frameRpta.loadData();
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
		if (socketClientB == null || !socketClientB.isConnected()) {
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
