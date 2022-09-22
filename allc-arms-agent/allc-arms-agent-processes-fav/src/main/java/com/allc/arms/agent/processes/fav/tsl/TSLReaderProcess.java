package com.allc.arms.agent.processes.fav.tsl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.allc.arms.agent.processes.el.ELReaderProcess;
import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.tsl.TSLUtility;
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
import com.ibm.OS4690.POSFile;
import com.ibm.OS4690.RandomAccessFile4690;

public class TSLReaderProcess extends AbstractProcess {
	protected Logger log = Logger.getLogger(TSLReaderProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	protected RandomAccessFile4690 randSeekRead = null;
	protected RandomAccessFile4690 randFileRead = null;
	protected POSFile posFileSeekWriter = null;
	protected String tslSeekFileName;
	protected String elFileName = null;
	protected long timeSleep;
	protected String valorEnCero;
	protected Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
	protected boolean endTSLProcess = false;
	protected boolean finished = false;
	protected String storeNumber = "";
	protected String tslFileName = null;
	protected String businessDateDay;
	private static boolean storeClosedPassed = false;
	protected ConnSocketClient socketClient;

	protected boolean init() {
		boolean result = false;
		try {
			ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
			storeNumber = (new Integer(controllerStatusData.getStoreNumber())).toString();
			while(storeNumber.length() < 3)
				storeNumber = "0" + storeNumber;
			Files.creaEscribeDataArchivo4690(getEyesFileName(), "TSL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Iniciando proceso de lectura de TSL.\n", true);
			valorEnCero = Util.rpad(ArmsAgentConstants.Communication.CERO, ArmsAgentConstants.Communication.SPACE, 20)
					+ ArmsAgentConstants.Communication.CRLF;
			tslSeekFileName = properties.getObject("tslReader.file.seek");
			elFileName = properties.getObject("elReader.file.name");
			timeSleep = Long.parseLong(properties.getObject("tslReader.timeSleep").toString());
			result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	public void run() {
		try {
			if (init()) {
				while (!endTSLProcess) {
					if (readTSLReg()) {
						Thread.sleep(timeSleep);
					}
				}
				closeClient();
				Files.creaEscribeDataArchivo4690(getEyesFileName(), "TSL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Finalizó el proceso de lectura de TSL.\n", true);
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(), "TSL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al iniciar el proceso de lectura de TSL.\n", true);
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(), "TSL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error en el proceso de lectura de TSL.\n", true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
			log.error(e.getMessage(), e);
		}
		finished = true;
	}
	
	private String getEyesFileName(){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	public boolean shutdown(long timeToWait) {
		endTSLProcess = true;
		closeClient();
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo TSLReader...");
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

	protected boolean readTSLReg() {
		boolean isStoreClosed = false;
		long tmp = 0;
		String valorPosicion;
		long punteroFile = 0;
		boolean sent = true;
		boolean sentPinpad = true;
		StringBuffer data = new StringBuffer();
		StringBuffer dataPinpad = new StringBuffer();
		StringBuffer concGenerate = new StringBuffer();
		StringBuffer replaceFile = new StringBuffer();
		String reg = "";
		String linea = "";
		String decod = "";
		String value = "";
		String valuePinpad="";
		List list;
		List listPinpad;
		Frame frame;
		Frame framePinpad;
		Frame frameConci;
		Frame frameReplace;
		String valueConciliacion = ""; 
		String valueReplace = ""; 
		List listConci;
		List listReplace;
		try {
			String newTslFileName = getEamtranFileName();
			if (newTslFileName == null)
				return false;
			boolean changeFiles = false;
			if (Files.fileExists(tslSeekFileName) && storeClosedPassed
					&& !newTslFileName.equalsIgnoreCase(obtieneNombreSeek())) {
				changeFiles = true;
				log.info("Los archivos TSL tienen diferentes nombre, se modifica el tslSeekFile: " + tslSeekFileName);
			}
			storeClosedPassed = false;
			if (!Files.fileExists4690(tslSeekFileName) || changeFiles) {
				Files.creaEscribeDataArchivo4690(tslSeekFileName, valorEnCero, false);
				Files.creaEscribeDataArchivo4690(tslSeekFileName, newTslFileName + ArmsAgentConstants.Communication.CRLF, true);
				tslFileName = newTslFileName;
				businessDateDay = null;
			} else {
				tslFileName = obtieneNombreSeek();
			}
			randSeekRead = new RandomAccessFile4690(tslSeekFileName, "r");

			data.append(ArmsAgentConstants.Communication.SOCKET_CHANNEL).append(ArmsAgentConstants.Communication.FRAME_SEP).append(ArmsAgentConstants.Process.TSL_PROCESS)
					.append(ArmsAgentConstants.Communication.FRAME_SEP).append("000").append(ArmsAgentConstants.Communication.FRAME_SEP)
					.append(storeNumber).append(ArmsAgentConstants.Communication.FRAME_SEP)
					.append(ArmsAgentConstants.Communication.PERM_CONN).append(ArmsAgentConstants.Communication.FRAME_SEP);

			punteroFile = obtieneOffsetSeek(tslSeekFileName);
			log.info("puntero:" + punteroFile);
			if (punteroFile >= 0) {
				posFileSeekWriter = new POSFile(tslSeekFileName, "rw", POSFile.SHARED_READ_WRITE_ACCESS);
				randFileRead = new RandomAccessFile4690(tslFileName, "r");
				boolean errorReadTSL = false;
				//utilizamos este contador para que si no avanza el puntero loguee cada 10 ciclos
				int logguerCount = 10;
				while (!endTSLProcess && !isStoreClosed && !errorReadTSL) {
					if(logguerCount == 10) {
						log.info("Leyendo tsl:" + (punteroFile));
						log.info("Fecha Contable: "+ businessDateDay);
					} else if(logguerCount == 0) {
						logguerCount = 10;
					}
					try {
						linea = Files.readLineByBytesPositionOfFile4690(randFileRead, punteroFile);
						log.info("Linea: " + linea);
					} catch (Exception e) {
						//agregamos esto para que cuando hagan un cierre "forzado" no quede en un bucle infinito
						linea = null;
						errorReadTSL = true;
						log.error(e.getMessage(), e);
						Files.creaEscribeDataArchivo4690(getEyesFileName(), "TSL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al leer el archivo: "+tslFileName+". Se volverá a crear el Puntero.\n", true);
						Files.deleteFile4690(tslSeekFileName);
					}
					if(businessDateDay == null || businessDateDay.equals("00000000")){
						businessDateDay = readFechaContable();
					}
					if (businessDateDay != null && !businessDateDay.equals("00000000") && null != linea && !linea.trim().equals("")) {
						log.info(linea);
						reg = linea.substring(1, linea.length()) + "," + "\"";
						decod = ident(reg);
						if (decod.length() > 0) {
							log.info("decod: " + decod);
							value = data.toString() + DateFormatUtils.format(new Date(), "yyyyMMddHHmmss")
									+ ArmsAgentConstants.Communication.FRAME_SEP + decod + ArmsAgentConstants.Communication.FRAME_SEP
									+ businessDateDay;
							list = Arrays.asList(p.split(value));
							String[] decodArray = decod.split("\\|");
							log.debug("TYPE: " + decodArray[0].toString());
							/** StoreClosing **/
							if (decodArray[0].toString().equals(ArmsAgentConstants.Tsl.STORE_CLOSING)) {
								isStoreClosed = true;
								storeClosedPassed = true;
								log.info("CIERRE ENCONTRADO");
								procesarArchivos(DateFormatUtils.format(new Date(), "yyMMdd"));
								log.info("ARCHIVOS PROCESADOS");
							}

							frame = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
									ArmsAgentConstants.Communication.FRAME_SEP);
							if (frame.loadData()) {
								sent = sendTSLRecord(frame);
								if (!sent) {
									isStoreClosed = false;
									storeClosedPassed = false;
									Thread.sleep(30000);
								}
							}
							
							if(isStoreClosed){
								
								try {
									ELReaderProcess.storeClosePassed = true;
									dataPinpad = new StringBuffer();
									dataPinpad.append(ArmsAgentConstants.Communication.SOCKET_CHANNEL).append(ArmsAgentConstants.Communication.FRAME_SEP).append(ArmsAgentConstants.Process.PINPAD_CLOSE_OPERATION)
									.append(ArmsAgentConstants.Communication.FRAME_SEP).append("000").append(ArmsAgentConstants.Communication.FRAME_SEP)
									.append(storeNumber).append(ArmsAgentConstants.Communication.FRAME_SEP)
									.append(ArmsAgentConstants.Communication.TEMP_CONN).append(ArmsAgentConstants.Communication.FRAME_SEP);
									
									valuePinpad = dataPinpad.toString() + DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
									
									listPinpad = Arrays.asList(p.split(valuePinpad));
									
									framePinpad = new Frame(listPinpad, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
											ArmsAgentConstants.Communication.FRAME_SEP);
									framePinpad.loadData();
									String tramaPP = Util.addLengthStartOfString(framePinpad.getString().toString(), properties.getInt("serverSocket.quantityBytesLength"))
											.toString();
									boolean tramaPinpadEnviada = false;
									if (socketClient.connectSocketUsingRetries() && socketClient.writeDataSocket(tramaPP)) {
										Frame frameRta = leerRespuesta(socketClient);
										frameRta.loadData();
										if (frameRta.getStatusTrama() == 0) {
											tramaPinpadEnviada = true;
										}
										socketClient.closeConnection();
										if(tramaPinpadEnviada)
											Files.creaEscribeDataArchivo4690(getEyesFileName(), "TSL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Trama para cierre en pinpad enviada con exito.\n", true);
										else
											Files.creaEscribeDataArchivo4690(getEyesFileName(), "TSL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Trama para cierre en pinpad no enviada.\n", true);
									} else
										Files.creaEscribeDataArchivo4690(getEyesFileName(), "TSL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No hay conexión con el server para enviar trama de cierre para pinpads.\n", true);
									
									updateArchivoFechaContable();
									
								} catch (Exception e) {
									log.error(e.getMessage(), e);
								}
								
								try {
									
									concGenerate = new StringBuffer();
									concGenerate.append(ArmsAgentConstants.Communication.SOCKET_CHANNEL).append(ArmsAgentConstants.Communication.FRAME_SEP).append(ArmsAgentConstants.Process.GEN_SYS_CNL_OPERATION)
									.append(ArmsAgentConstants.Communication.FRAME_SEP).append("000").append(ArmsAgentConstants.Communication.FRAME_SEP)
									.append(storeNumber).append(ArmsAgentConstants.Communication.FRAME_SEP)
									.append(ArmsAgentConstants.Communication.TEMP_CONN)
									.append(ArmsAgentConstants.Communication.FRAME_SEP)
									.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
									.append(ArmsAgentConstants.Communication.FRAME_SEP).append(storeNumber)
									.append(ArmsAgentConstants.Communication.FRAME_SEP).append(businessDateDay);
									
									valueConciliacion = concGenerate.toString();
									
									listConci = Arrays.asList(p.split(valueConciliacion));
									
									frameConci = new Frame(listConci, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
											ArmsAgentConstants.Communication.FRAME_SEP);
									frameConci.loadData();
									String tramaC = Util.addLengthStartOfString(frameConci.getString().toString(), properties.getInt("serverSocket.quantityBytesLength"))
											.toString();
									boolean tramaGenerateConcEnviada = false;
									if (socketClient.connectSocketUsingRetries() && socketClient.writeDataSocket(tramaC)) {
										Frame frameRta = leerRespuesta(socketClient);
										frameRta.loadData();
										if (frameRta.getStatusTrama() == 0) {
											tramaGenerateConcEnviada = true;
										}
										socketClient.closeConnection();
										if(tramaGenerateConcEnviada)
											Files.creaEscribeDataArchivo4690(getEyesFileName(), "TSL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Trama para generar archivos de conciliaciones eviada con exito.\n", true);
										else
											Files.creaEscribeDataArchivo4690(getEyesFileName(), "TSL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Trama para generar archivos de conciliaciones no enviada.\n", true);
									} else
										Files.creaEscribeDataArchivo4690(getEyesFileName(), "TSL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No hay conexión con el server para enviar trama para generar archivos de conciliaciones.\n", true);
									
									
									
								} catch (Exception e) {
									log.error(e.getMessage(), e);
								}

								try {
									Files.deleteFile4690("F:/allc_pgm/ArmsAgent/syscardRecMsg.dat");
									Files.deleteFile4690("C:/ADX_UDT1/LXSEEKSU.DAT");
									FilesHelper.copyFile4690("f:/allc_pgm/ArmsAgent/Respaldo", "C:/ADX_UDT1" , "LXSEEKSU.DAT", "LXSEEKSU.DAT");
									Files.creaEscribeDataArchivo4690(getEyesFileName(), "TSL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo LXSEEKSU.DAT reemplazado con exito.\n", true);
								} catch (Exception e) {
									log.error(e.getMessage(), e);
									Files.creaEscribeDataArchivo4690(getEyesFileName(), "TSL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo LXSEEKSU.DAT no pudo ser reemplazado.\n", true);
								}
							} else {
								ELReaderProcess.storeClosePassed = false;
							}
							
						}
						if (!isStoreClosed && sent) {
							punteroFile = randFileRead.getFilePointer();
							tmp = punteroFile;
							valorPosicion = Util.rpad(String.valueOf(tmp), ArmsAgentConstants.Communication.SPACE, 20)
									+ ArmsAgentConstants.Communication.CRLF;
							posFileSeekWriter.write(valorPosicion.getBytes(), 0, POSFile.FROM_START_OF_FILE, POSFile.FLUSH,
									valorPosicion.length());
						}
					} else if((null == linea || linea.trim().equals("")) && punteroFile < randFileRead.getFilePointer()){
						//si el puntero cambio es porque shay dos 0D0A seguidos y tenemos que avanzar el puntero para que continue leyendo
						log.info("Punteros diferentes: punteroFile: "+punteroFile+" filePointer: "+randFileRead.getFilePointer());
						punteroFile = randFileRead.getFilePointer();
						tmp = punteroFile;
						valorPosicion = Util.rpad(String.valueOf(tmp), ArmsAgentConstants.Communication.SPACE, 20)
								+ ArmsAgentConstants.Communication.CRLF;
						posFileSeekWriter.write(valorPosicion.getBytes(), 0, POSFile.FROM_START_OF_FILE, POSFile.FLUSH,
								valorPosicion.length());
					} else {
						logguerCount--;
						if(!errorReadTSL)
							Thread.sleep(timeSleep);
					}
				}
				try {
					posFileSeekWriter.closeFull();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				try {
					randFileRead.close();
					randSeekRead.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}

				if (isStoreClosed) {
					Files.creaEscribeDataArchivo4690(getEyesFileName(), "TSL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|PRC|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Cierre encontrado.\n", true);
					if (Files.deleteFile4690(tslSeekFileName))
						log.info("store closing was detected, then seekFile " + tslSeekFileName + " was deleted");
					else
						log.info("store closing was detected, but seekFile " + tslSeekFileName + " wasn't deleted");
				}
			} else {
				try {
					randFileRead.close();
					randSeekRead.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return isStoreClosed;
	}
	
	protected void procesarArchivos(String fechaContable){
		File4690 tsl = new File4690(tslFileName);
		String storeCode = storeNumber;
		while(storeCode.length() < 3)
			storeCode = "0" + storeCode;
		String aniomesdia = fechaContable.substring(0, 2) + fechaContable.substring(2, 4) + fechaContable.substring(4, 6);
		tsl.renameTo(new File4690("c:/respaldo/LG"+aniomesdia+"."+storeCode));
		Files.zippear4690("c:/respaldo/", "LG"+aniomesdia+"."+storeCode, "LZ"+aniomesdia+"."+storeCode);
		String mesDia = Files.formatMonth(fechaContable.substring(2, 4)) + fechaContable.substring(4, 6);
		Files.zippearJournals4690("c:/respaldo/", properties.getObject("TSL.journals.directory").toString(), mesDia, "J"+aniomesdia+".ZIP");
		File4690 el = new File4690(elFileName);
		el.renameTo(new File4690("c:/respaldo/EX"+aniomesdia+"."+storeCode));
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
		if (socketClient != null && socketClient.isConnected())
			socketClient.closeConnection();
	}

	protected boolean sendTSLRecord(Frame frame) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected())
				connectClient();
			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
			log.info("Trama a enviar: " + trama);
			if (socketClient != null && socketClient.isConnected() && socketClient.writeDataSocket(trama)) {
				int numberOfBytes = 0;
				int timeOutCycles = 0;
				while (numberOfBytes == 0) {
					numberOfBytes = socketClient.readLengthDataSocket();
					if (timeOutCycles == 5) {
						// cada 5 timeouts escribimos una trama vacía para asegurarnos que el socket esté activo
						String mje = Util.addLengthStartOfString("", qtyBytesLength).toString();
						if(!socketClient.writeDataSocket(mje)){
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

	protected void comprimirJournals(String fechaContable, String fechaCierre) {
		File4690 journalsDir = new File4690(properties.getObject("TSL.journals.directory").toString());
		String zipDir = properties.getObject("TSL.zip.directory").toString();
		log.info("TSL.journals.directory: " + properties.getObject("TSL.journals.directory").toString());
		log.info("TSL.zip.directory: " + properties.getObject("TSL.zip.directory").toString());
		generarTSRPOSFile(journalsDir.getPath(), fechaContable);
		Files.zippear4690(fechaContable, fechaCierre, journalsDir, zipDir, generarZIPName(fechaContable));
	}

	protected String generarZIPName(String fecha) {
		String ano = fecha.substring(0, 2);
		String mes = fecha.substring(2, 4);
		String dia = fecha.substring(4, 6);
		return storeNumber + dia + mes + ano;
	}

	protected void generarTSRPOSFile(String journalsDir, String fechaContable) {
		Map posPosition = new HashMap();
		StringBuffer flags = new StringBuffer();
		try {
			String mesDia = Files.formatMonth(fechaContable.substring(2, 4)) + fechaContable.substring(4, 6);
			File4690 folder = new File4690(journalsDir);
			File4690[] listOfDir = folder.listFiles();
			for (int i = 0; i < listOfDir.length; i++) {
				String journalName = listOfDir[i].getName();
				if (journalName.toUpperCase().startsWith("J" + mesDia) && !journalName.toUpperCase().endsWith(".C")) {
					Integer pos = new Integer(journalName.substring(journalName.length() - 3));
					posPosition.put(pos, "1");
				}
			}
			for (int i = 0; i < 999; i++) {
				if (posPosition.get(new Integer(i + 1)) != null)
					flags.append("1");
				else
					flags.append("0");
			}
			Files.creaEscribeDataArchivo4690(journalsDir + File.separator + "TSRPOS", flags.toString(), false);
			log.info("Archivo TSRPOS generado con �xito.");
		} catch (Exception e) {
			log.error("Error al generar el archivo TSRPOS.", e);
		}
	}

	/**
	 * Obtiene la posicion del archivo store, desde la que se debe de enviar la informacion.
	 * 
	 * @param nombreFileSeek
	 *            Nombre del archivo seek
	 * @return La posicion de envio
	 * @throws IOException
	 *             Si el archivo seek no contiene un numero.
	 */
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
	
	/**
	 * Obtiene el nombre del archivo store, desde la que se debe de enviar la informacion.
	 * 
	 * @return El nombre del archivo
	 * @throws IOException
	 *             Si el archivo seek no contiene un numero.
	 */
	protected String obtieneNombreSeek() {
		String data = null;
		try {
			randSeekRead = new RandomAccessFile4690(tslSeekFileName, "r");
			randSeekRead.readLine();
			//la linea que contiene el nombre es la segunda
			data = randSeekRead.readLine();
			randSeekRead.seek(0);
			randSeekRead.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return data;
	}

	protected String getEamtranFileName() {
		String eamtranFileName = null;

		try {
			String eamtranFilePrefix = "EAMTRAN";
			String eamtermsPathName = (String) properties.getObject("TSL.path.eamterms");
			String eamtermsFileName = (String) properties.getObject("TSL.file.eamterms");
			File4690 archivo = new File4690(eamtermsPathName + File4690.separatorChar + eamtermsFileName);
			log.info("Nombre Archivo: " + archivo.getAbsolutePath());
			String dataFile = TSLUtility.leerArchivo4690(eamtermsPathName + File4690.separatorChar + eamtermsFileName);
			if (null != dataFile) {
				log.info("Leído: " + dataFile);
				String secondPart = dataFile.split(eamtranFilePrefix)[1];
				String eamtranLetter = secondPart.substring(0, 1);
				eamtranFileName = eamtermsPathName + File4690.separatorChar + eamtranFilePrefix + eamtranLetter + ".DAT";
				log.info(eamtranFileName);
				businessDateDay = readFechaContable();
			} else
				log.error("cannot find the path for the file " + eamtermsPathName + ":" + eamtermsFileName);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return eamtranFileName;
	}

	protected String ident(String dato) throws Exception {
		byte[] arreglop, arregloj;
		arreglop = dato.substring(0, 1).getBytes("ISO-8859-1");
		String sal1 = "", salp = "";
		String salida = "";
		String cadena = "";
		int tipo = 0;
		int tcadena = 0;
		List list, lista, listb;
		String REGEX = "\",\"";
		String CAMP = "\":\"";
		String SCAMP = ":";
		Pattern p = Pattern.compile(REGEX);
		Pattern q = Pattern.compile(CAMP);
		Pattern r = Pattern.compile(SCAMP);
		try {
			tipo = Integer.parseInt(TSLUtility.unpack(arreglop));
			log.info("Tipo: " + tipo);
			if (tipo == 0) {

				list = Arrays.asList(p.split(dato));
				for (int x = 0; x <= list.size() - 1; x++) {
					cadena = list.get(x).toString();

					arregloj = cadena.substring(0, 1).getBytes("ISO-8859-1");
					tcadena = Integer.parseInt(TSLUtility.unpack(arregloj));

					lista = Arrays.asList(r.split(cadena));
					if (x == list.size() - 1)
						salp = TSLUtility.parseatlog(lista, tcadena);
					else
						salp = TSLUtility.parseatlog(lista, tcadena) + ",";

					salida = salida + salp;

				}
			} else if (tipo == 20) {
				lista = Arrays.asList(q.split(dato.substring(2)));
				sal1 = TSLUtility.obtiene(lista, 3, 1);
				salida = "20" + "|" + TSLUtility.parseaexecpt(lista, sal1);
				// TODO esto se hace hasta que agreguemos el procesamiento de los excepton log en el server (sin header00)
				salida = "";
			} else if (tipo == 21) {
				listb = Arrays.asList(r.split(dato.substring(2)));

				salida = "21" + "|" + TSLUtility.obtiene(listb, 0, 1) + "|" + TSLUtility.obtiene(listb, 1, 1) + "|"
						+ TSLUtility.obtiene(listb, 2, 1) + "|" + TSLUtility.obtiene(listb, 3, 1) + "|";
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			salida = "";
		}
		return salida;
	}
	
	private String getSyscardFileName(PropFile properties){
		return properties.getObject("syscard.file.name")+".DAT";
	}

	protected Frame leerRespuesta(ConnSocketClient socketClient) {
		int numberOfBytes = socketClient.readLengthDataSocket();
		if (numberOfBytes > 0) {
			String str = socketClient.readDataSocket(numberOfBytes);
			if (StringUtils.isNotBlank(str)) {
				List list = Arrays.asList(p.split(str));
				Frame frameRpta = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
						ArmsAgentConstants.Communication.FRAME_SEP);
				log.info("Respuesta recibida: " + frameRpta.toString());
				return frameRpta;
			}
		}
		if( numberOfBytes == 0)
			numberOfBytes = socketClient.readLengthDataSocket();
		
		log.info("No se recibio respuesta.");
		return null;
	}
	
	public String readFechaContable() {		
		try {
			RandomAccessFile4690 raf = new RandomAccessFile4690("C:/ADX_UDT1/LXACCTDT.DAT", "r");
			raf.seek(0);
			String fecha = raf.readLine();
			raf.close();
			if(Integer.valueOf(fecha).compareTo(Integer.valueOf(20170101)) < 0)
				return "00000000";
			return fecha.trim();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return "00000000";
	}
	
	public void updateArchivoFechaContable(){
		log.info("Actualizacion Archivo Fecha Contable.");
		try {
			String oldFecha = readFechaContable() + ArmsAgentConstants.Communication.CRLF;
			Files.creaEscribeDataArchivo4690("C:/ADX_UDT1/LXACCTDT.OLD", oldFecha, false);
			log.info("Fecha anterior actualizada en archivo contable old: "+ oldFecha);
			
			String newFecha = Util.rpad(ArmsAgentConstants.Communication.CERO, ArmsAgentConstants.Communication.CERO, 8)
					+ ArmsAgentConstants.Communication.CRLF;
			Files.creaEscribeDataArchivo4690("C:/ADX_UDT1/LXACCTDT.DAT", newFecha, false);
			log.info("Fecha actualizada en archivo contable: "+ newFecha);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}		
	}

}
