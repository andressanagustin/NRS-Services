package com.allc.arms.agent.processes.cer.syscard;

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
import com.ibm.OS4690.POSFile;
import com.ibm.OS4690.RandomAccessFile4690;

public class SendTramaFromPosProcess extends AbstractProcess {
	protected Logger log = Logger.getLogger(SendTramaFromPosProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	protected RandomAccessFile4690 randSeekRead = null;
	protected RandomAccessFile4690 randFileRead = null;
	protected POSFile posFileSeekWriter = null;
	protected String tramasSeekFileName;
	protected long timeSleep;
	protected String valorEnCero;
	protected Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
	protected boolean endProcess = false;
	protected boolean finished = false;
	protected String storeNumber = "";
	protected String tramasFileName = null;
	protected String actualDateDay;
	protected ConnSocketClient socketClient;

	protected boolean init() {
		boolean result = false;
		try {
			ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
			storeNumber = (new Integer(controllerStatusData.getStoreNumber())).toString();
			while (storeNumber.length() < 3)
				storeNumber = "0" + storeNumber;
			Files.creaEscribeDataArchivo4690(getEyesFileName(),
					"SYS_READ_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber
							+ "|STR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando proceso de lectura del archivos de tramas syscard desde POS.\n",
					true);
			valorEnCero = Util.rpad(ArmsAgentConstants.Communication.CERO, ArmsAgentConstants.Communication.SPACE, 20)
					+ ArmsAgentConstants.Communication.CRLF;
			tramasSeekFileName = "F:/allc_pgm/ArmsAgent/tramaFromPOSseek.dat";
			timeSleep = Long.parseLong(properties.getObject("syscardReader.timeSleep").toString());
			actualDateDay = getFechaActual();
			result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	public void run() {
		try {
			if (init()) {
				while (!endProcess) {
					if (readTramas()) {
						Thread.sleep(timeSleep);
					}
				}
				Files.creaEscribeDataArchivo4690(getEyesFileName(),
						"SYS_READ_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|END|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Finaliza el proceso de envío del archivo de tramas syscard.\n",
						true);
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(),
						"SYS_READ_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|ERR|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al iniciar el proceso de envío del archivo de tramas syscard.\n",
						true);
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(),
						"SYS_READ_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|ERR|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error en el proceso de envío de tramas de syscard.\n",
						true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
			log.error(e.getMessage(), e);
		}
		finished = true;
	}

	public boolean shutdown(long timeToWait) {
		endProcess = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo SendTramaFromPosProcess...");
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

	protected boolean readTramas() {
		boolean isStoreClosed = false;
		long tmp = 0;
		String valorPosicion;
		long punteroFile = 0;
		boolean sent = true;
		String linea = "";

		try {
			tramasFileName = "tramaFromPOS"+actualDateDay+".dat";
						
			if (tramasFileName == null || tramasFileName.equals(""))
				return false;
			if (!Files.fileExists4690(tramasSeekFileName)) {
				Files.creaEscribeDataArchivo4690(tramasSeekFileName, valorEnCero, false);
				Files.creaEscribeDataArchivo4690(tramasSeekFileName,
						tramasFileName + ArmsAgentConstants.Communication.CRLF, true);
				randSeekRead = new RandomAccessFile4690(tramasSeekFileName, "r");
			} else {
				randSeekRead = new RandomAccessFile4690(tramasSeekFileName, "r");
				tramasFileName = obtieneNombreSeek();
				actualDateDay = tramasFileName.substring(12, 16);
			}

			
			punteroFile = obtieneOffsetSeek(tramasSeekFileName);
			log.info("puntero:" + punteroFile);
			if (punteroFile >= 0) {
				posFileSeekWriter = new POSFile(tramasSeekFileName, "rw", POSFile.SHARED_READ_WRITE_ACCESS);
				//utilizamos este contador para que si no avanza el puntero loguee cada 10 ciclos
				int logguerCount = 10;
				while (!endProcess) {
					if(logguerCount == 10) {
						log.info("Leyendo puntosFile:" + (punteroFile + 1));
					} else if(logguerCount == 0) {
						logguerCount = 11;
					}
					if (!Files.fileExists4690(tramasFileName))
						Files.creaEscribeDataArchivo4690(tramasFileName, "", true);
					if(randFileRead == null)
						randFileRead = new RandomAccessFile4690(tramasFileName, "r");
					linea = Files.readLineByBytesPositionOfFile4690(randFileRead, punteroFile);
					if (null != linea && !linea.trim().equals("")) {
						StringBuffer data = new StringBuffer();
						data.append(ArmsAgentConstants.Communication.SOCKET_CHANNEL).append(ArmsAgentConstants.Communication.FRAME_SEP).append(ArmsAgentConstants.Process.SEND_TRAMA_POS_OPERATION)
						.append(ArmsAgentConstants.Communication.FRAME_SEP).append("000").append(ArmsAgentConstants.Communication.FRAME_SEP)
						.append(storeNumber).append(ArmsAgentConstants.Communication.FRAME_SEP)
						.append(ArmsAgentConstants.Communication.TEMP_CONN).append(ArmsAgentConstants.Communication.FRAME_SEP)
						.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss")).append(ArmsAgentConstants.Communication.FRAME_SEP).append(linea);
						List list = Arrays.asList(p.split(data.toString()));
						Frame frame = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
								ArmsAgentConstants.Communication.FRAME_SEP);
						if (frame.loadData()) {
							sent = sendTrama(frame);
						}

						if (sent) {
							punteroFile = randFileRead.getFilePointer();
							tmp = punteroFile;
							valorPosicion = Util.rpad(String.valueOf(tmp), ArmsAgentConstants.Communication.SPACE, 20)
									+ ArmsAgentConstants.Communication.CRLF;
							posFileSeekWriter.write(valorPosicion.getBytes(), 0, POSFile.FROM_START_OF_FILE,
									POSFile.FLUSH, valorPosicion.length());
						}
					} else {
						if (!actualDateDay.equals(getFechaActual())) {
							Iterator itFechas = obtenerFechasIntermedias(actualDateDay, getFechaActual()).iterator();
							
							if(randFileRead != null){
								randFileRead.close();
								randFileRead = null;
							}
							while (itFechas.hasNext()) {
								actualDateDay = (String) itFechas.next();
								tramasFileName = "tramaFromPOS"+actualDateDay+".dat";
								File4690 file = new File4690(tramasFileName);
								if(file.exists()){
									punteroFile = 0;
									Files.creaEscribeDataArchivo4690(tramasSeekFileName, valorEnCero, false);
									Files.creaEscribeDataArchivo4690(tramasSeekFileName,
											tramasFileName + ArmsAgentConstants.Communication.CRLF, true);
									break;
								}
							}
							Files.creaEscribeDataArchivo4690(tramasSeekFileName, valorEnCero, false);
							Files.creaEscribeDataArchivo4690(tramasSeekFileName,
									tramasFileName + ArmsAgentConstants.Communication.CRLF, true);
							
							
						} else{
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
	
	private String getFechaActual() {
		return Util.convertDateToString(new Date(), "MMdd");
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
					if(dia1 == dia2 && mes1 == mes2)
						break;
				}
			}
			if(dia1 == dia2 && mes1 == mes2)
				break;

			dia1 = 0;
			mes1++;
			if(mes1 > mesMax)
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

	protected boolean sendTrama(Frame frame) {
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
		} finally {
			closeClient();
		}
		return false;
	}
	
	protected String obtieneNombreSeek() {
		String data = null;
		try {
			randSeekRead.readLine();
			//la linea que contiene el nombre es la segunda
			data = randSeekRead.readLine();
			randSeekRead.seek(0);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return data;
	}

}
