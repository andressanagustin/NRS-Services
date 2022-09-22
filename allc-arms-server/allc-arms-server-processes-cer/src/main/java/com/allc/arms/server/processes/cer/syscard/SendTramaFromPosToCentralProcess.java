package com.allc.arms.server.processes.cer.syscard;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

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
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class SendTramaFromPosToCentralProcess extends AbstractProcess{
	
	protected Logger log = Logger.getLogger(SendTramaFromPosToCentralProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	protected RandomAccessFile randSeekRead = null;
	protected RandomAccessFile randFileRead = null;
	protected File posFileSeekWriter = null;
	protected String tramasPosSeekFileName;
	protected long timeSleep;
	protected String valorEnCero;
	protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	protected boolean endProcess = false;
	protected boolean finished = false;
	protected String storeNumber = "";
	protected String tramasPosFileName = null;
	protected String actualDateDay;
	protected ConnSocketClient socketClient;
	protected StoreDAO storeDAO = new StoreDAO();
	private Session sessionSaAdmin = null;
	String ipCentral = null;
	
	protected boolean init() {
		boolean result = false;
		try {
			storeNumber = properties.getObject("eyes.store.code");
			while (storeNumber.length() < 3)
				storeNumber = "0" + storeNumber;
			Files.creaEscribeDataArchivo(getEyesFileName(),
					"SND_TRAMA_TO_CTRL_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber
							+ "|STR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando proceso de lectura del archivo de tramas del POS en ArmsServer Local.\n",
					true);
			valorEnCero = Util.rpad(ArmsServerConstants.Communication.CERO, ArmsServerConstants.Communication.SPACE, 20)
					+ ArmsServerConstants.Communication.CRLF;
			tramasPosSeekFileName = "tramasFromPosSeek.dat";
			timeSleep = 15000;
			actualDateDay = getFechaActual();
			result = true;
			iniciarSaAdminSesion();
			Store central = storeDAO.getStoreByCode(sessionSaAdmin, 0);
			ipCentral = central.getIp();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}
	
	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
	
	private void iniciarSaAdminSesion() {
		while (sessionSaAdmin == null) {
			try {
				sessionSaAdmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionSaAdmin == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}
	
	public void run() {
		try {
			if (init()) {
				while (!endProcess) {
					if (readTramas()) {
						Thread.sleep(timeSleep);
					}
				}
				Files.creaEscribeDataArchivo(getEyesFileName(),
						"SND_TRAMA_TO_CTRL_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|END|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Finaliza el proceso de envío del archivo de tramas del POS a Central.\n",
						true);
			} else
				Files.creaEscribeDataArchivo(getEyesFileName(),
						"SND_TRAMA_TO_CTRL_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al iniciar el proceso de envío del archivo de tramas del POS a Central.\n",
						true);
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo(getEyesFileName(),
						"SND_TRAMA_TO_CTRL_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error en el proceso de envío de tramas del POS a Central.\n",
						true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
			log.error(e.getMessage(), e);
		} finally {
			sessionSaAdmin.close();
			sessionSaAdmin = null;
		}
		finished = true;
	}
	
	protected boolean readTramas() {
		boolean isStoreClosed = false;
		long tmp = 0;
		String valorPosicion;
		long punteroFile = 0;
		String linea = "";
		boolean sent = true;

		try {
			tramasPosFileName = "tramasFromPos"+ actualDateDay + ".dat";
			if (tramasPosFileName == null || tramasPosFileName.equals(""))
				return false;
			if (!Files.fileExists(tramasPosSeekFileName)) {
				Files.creaEscribeDataArchivo(tramasPosSeekFileName, valorEnCero, false);
				Files.creaEscribeDataArchivo(tramasPosSeekFileName,
						tramasPosFileName + ArmsServerConstants.Communication.CRLF, true);
			} else {
				tramasPosFileName = Files.readSpecifictLineOfFile(tramasPosSeekFileName, 2);
				if(tramasPosFileName == null){
					Files.deleteFile(tramasPosSeekFileName);
					return false;
				}
				actualDateDay = tramasPosFileName.substring(13, 21);
			}

			randSeekRead = new RandomAccessFile(tramasPosSeekFileName, "r");
			punteroFile = obtieneOffsetSeek(tramasPosSeekFileName);
			log.info("puntero:" + punteroFile);
			File file = new File(tramasPosFileName);
			if (!Files.fileExists(tramasPosFileName))
				Files.creaEscribeDataArchivo(tramasPosFileName, "", true);
			if (punteroFile >= 0) {
				randFileRead = new RandomAccessFile(tramasPosFileName, "r");
				randFileRead.seek(punteroFile);
				// utilizamos este contador para que si no avanza el puntero
				// loguee cada 10 ciclos
				int logguerCount = 10;
				while (!endProcess) {
					if (logguerCount == 10) {
						log.info("Leyendo tramasFromPos File:" + (punteroFile + 1));
					} else if (logguerCount == 0) {
						logguerCount = 11;
					}
					if (!Files.fileExists(tramasPosFileName))
						Files.creaEscribeDataArchivo(tramasPosFileName, "", true);
					
					linea = Files.readLineByBytesPositionOfFile(randFileRead, punteroFile);
					
					if (null != linea && !linea.trim().equals("")) {
						
						StringBuffer data = new StringBuffer();
						data.append(ArmsServerConstants.Communication.SOCKET_CHANNEL).append(ArmsServerConstants.Communication.FRAME_SEP).append(ArmsServerConstants.Process.SEND_PUNTOS_OPERATION)
						.append(ArmsServerConstants.Communication.FRAME_SEP).append("000").append(ArmsServerConstants.Communication.FRAME_SEP)
						.append(storeNumber).append(ArmsServerConstants.Communication.FRAME_SEP)
						.append(ArmsServerConstants.Communication.TEMP_CONN).append(ArmsServerConstants.Communication.FRAME_SEP)
						.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss")).append(ArmsServerConstants.Communication.FRAME_SEP).append(linea).append(ArmsServerConstants.Communication.FRAME_SEP).append(actualDateDay);
						List list = Arrays.asList(p.split(data.toString()));
						Frame frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
								ArmsServerConstants.Communication.FRAME_SEP);
						if (frame.loadData()) {
							sent = sendTrama(frame);
						}
						if (sent) {
							punteroFile = randFileRead.getFilePointer();
							valorPosicion = Util.rpad(String.valueOf(punteroFile),
									ArmsServerConstants.Communication.SPACE, 20)
									+ ArmsServerConstants.Communication.CRLF;
							Files.creaEscribeDataArchivo(tramasPosSeekFileName, valorPosicion, false);
							Files.creaEscribeDataArchivo(tramasPosSeekFileName,
									tramasPosFileName + ArmsServerConstants.Communication.CRLF, true);
						}
					} else {
						if (!actualDateDay.equals(getFechaActual())) {
							Iterator itFechas = obtenerFechasIntermedias(actualDateDay, getFechaActual()).iterator();
							while (itFechas.hasNext()) {
								actualDateDay = (String) itFechas.next();
								tramasPosFileName = "tramasFromPos" + actualDateDay + ".dat";
								if(actualDateDay.equals(getFechaActual()))
									Files.creaEscribeDataArchivo(tramasPosFileName, "", true);
								file = new File(tramasPosFileName);
								if (file.exists()) {
									Files.creaEscribeDataArchivo(tramasPosSeekFileName, valorEnCero, false);
									Files.creaEscribeDataArchivo(tramasPosSeekFileName,
											tramasPosFileName + ArmsServerConstants.Communication.CRLF, true);
									randFileRead = new RandomAccessFile(tramasPosFileName, "r");
									randFileRead.seek(0L);
									punteroFile = 0L;
									break;
								}
							}
						} else {
							logguerCount--;
							try {
								Thread.sleep(timeSleep);
							} catch (InterruptedException e) {
								log.error(e.getMessage(), e);
							}
						}
					}
				}
				try {
					randSeekRead.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				try {
					randFileRead.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			} else {
				try {
					randSeekRead.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				try {
					Thread.sleep(timeSleep);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return isStoreClosed;
	}

	private String getFechaActual() {
		return Util.convertDateToString(new Date(), "YYYYMMdd");
	}

	private List obtenerFechasIntermedias(String fechaIni, String fechaFin) {
		int mes1 = (new Integer(fechaIni.substring(0, 2))).intValue();
		int mes2 = (new Integer(fechaFin.substring(0, 2))).intValue();
		int dia1 = (new Integer(fechaIni.substring(2, 4))).intValue();
		int dia2 = (new Integer(fechaFin.substring(2, 4)).intValue());
		List fechas = new ArrayList();
		int diaMax = 31;
		int mesMax = 12;
		while (mes1 <= mesMax && mes1 <= mes2) {
			while (dia1 <= diaMax) {
				if (dia1 == diaMax) {
					dia1++;
				} else {
					dia1++;
					String dia = (new Integer(dia1).toString());
					String mes = (new Integer(mes1).toString());
					fechas.add((mes.length() < 2 ? "0" + mes : mes) + (dia.length() < 2 ? "0" + dia : dia));
					if (dia1 == dia2 && mes1 == mes2)
						break;
				}
			}
			if (dia1 == dia2 && mes1 == mes2)
				break;

			dia1 = 0;
			mes1++;
			if (mes1 > mesMax)
				mes1 = 1;
		}
		fechas.add(fechaFin);
		return fechas;
	}

	protected long obtieneOffsetSeek(String nombreFileSeek) {
		long punteroFile;
		String data;
		try {
			data = randSeekRead.readLine();
			randSeekRead.seek(0);
			if (null == data)
				punteroFile = 0;
			else
				punteroFile = Long.parseLong(data.replaceAll(" ", ""));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			punteroFile = -1;
		}
		return punteroFile;
	}

	protected boolean connectClient(String ip) {
		if (socketClient == null) {
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(ip);
			socketClient.setPortServer(properties.getInt("serverSocket.port"));
			socketClient.setRetries(3);
			socketClient.setTimeOutConnection(properties.getInt("serverSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("serverSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("serverSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}

	protected void closeClient() {
		if (socketClient != null && socketClient.isConnected())
			socketClient.closeConnection();
	}

	protected boolean sendTrama(Frame frame) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected()) {

				connectClient(ipCentral);
			}
			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
			log.info("Trama a enviar: " + trama);
			if (socketClient != null && socketClient.isConnected() && socketClient.writeDataSocket(trama)){
				UtilityFile.createWriteDataFile(getEyesFileName(),
						"SND_TRAMA_TO_CTRL_P" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|END|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Envio de trama realizado con exito.\n",
						true);
				return true;
			}else
				UtilityFile.createWriteDataFile(getEyesFileName(),
						"SND_TRAMA_TO_CTRL_P" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|WAR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|No se pudo enviar la trama.\n",
						true);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			closeClient();
		}
		return false;
	}


	public boolean shutdown(long timeToWait) {
		endProcess = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo SendTramaFromPosToCentralProcess...");
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
