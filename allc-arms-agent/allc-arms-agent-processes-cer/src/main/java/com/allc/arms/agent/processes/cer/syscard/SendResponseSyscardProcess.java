package com.allc.arms.agent.processes.cer.syscard;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
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
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.POSFile;
import com.ibm.OS4690.RandomAccessFile4690;

public class SendResponseSyscardProcess extends AbstractProcess {
	protected Logger log = Logger.getLogger(SendResponseSyscardProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	protected RandomAccessFile4690 randSeekRead = null;
	protected RandomAccessFile4690 randFileRead = null;
	protected POSFile posFileSeekWriter = null;
	protected String syscardSeekFileName;
	protected long timeSleep;
	protected String valorEnCero;
	protected Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
	protected boolean endSyscardProcess = false;
	protected boolean finished = false;
	protected String storeNumber = "";
	protected String syscardFileName = null;
	protected String businessDateDay;
	private static boolean storeClosedPassed = false;
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
							+ "|Iniciando proceso de lectura del archivos de tramas syscard.\n",
					true);
			valorEnCero = Util.rpad(ArmsAgentConstants.Communication.CERO, ArmsAgentConstants.Communication.SPACE, 20)
					+ ArmsAgentConstants.Communication.CRLF;
			syscardSeekFileName = "F:/allc_pgm/ArmsAgent/syscardResponseseek.dat";
			timeSleep = Long.parseLong(properties.getObject("syscardReader.timeSleep").toString());

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
				while (!endSyscardProcess) {
					if (readSyscardTrama()) {
						Thread.sleep(timeSleep);
					}
				}
				Files.creaEscribeDataArchivo4690(getEyesFileName(),
						"SYS_READ_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|END|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Finaliza el proceso de lectura del archivo de tramas syscard.\n",
						true);
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(),
						"SYS_READ_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|ERR|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al iniciar el proceso de lectura del archivo de tramas syscard.\n",
						true);
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(),
						"SYS_READ_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|ERR|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error en el proceso de lectura de tramas de syscard.\n",
						true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
			log.error(e.getMessage(), e);
		}
		finished = true;
	}

	public boolean shutdown(long timeToWait) {
		endSyscardProcess = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo SyscardFileReader...");
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

	protected boolean readSyscardTrama() {
		boolean isStoreClosed = false;
		long tmp = 0;
		String valorPosicion;
		long punteroFile = 0;
		boolean sent = true;
		String linea = "";

		try {
			String newSafesPsFileName = "responseSyscard.dat";
			if (newSafesPsFileName == null || newSafesPsFileName.equals(""))
				return false;

			storeClosedPassed = false;
			if (!Files.fileExists4690(syscardSeekFileName)) {
				Files.creaEscribeDataArchivo4690(syscardSeekFileName, valorEnCero, false);
				Files.creaEscribeDataArchivo4690(syscardSeekFileName,
						newSafesPsFileName + ArmsAgentConstants.Communication.CRLF, true);

			}

			syscardFileName = newSafesPsFileName;

			randSeekRead = new RandomAccessFile4690(syscardSeekFileName, "r");
			punteroFile = obtieneOffsetSeek(syscardSeekFileName);
			log.info("puntero:" + punteroFile);
			if (punteroFile >= 0) {
				posFileSeekWriter = new POSFile(syscardSeekFileName, "rw", POSFile.SHARED_READ_WRITE_ACCESS);
				if (!Files.fileExists4690(syscardFileName))
					Files.creaEscribeDataArchivo4690(syscardFileName, "", true);
				randFileRead = new RandomAccessFile4690(syscardFileName, "r");
				//utilizamos este contador para que si no avanza el puntero loguee cada 10 ciclos
				int logguerCount = 10;
				while (!endSyscardProcess) {
					if(logguerCount == 10) {
						log.info("Leyendo syscardFile:" + (punteroFile + 1));
					} else if(logguerCount == 0) {
						logguerCount = 10;
					}
					if (!Files.fileExists4690(syscardFileName))
						Files.creaEscribeDataArchivo4690(syscardFileName, "", true);
					linea = Files.readLineByBytesPositionOfFile4690(randFileRead, punteroFile);
					
					if (null != linea && !linea.trim().equals("")) {

						/*
						 * se verifica si la linea leida dentro del archivo
						 * corresponde a un cierre de tienda
						 */
						if (linea.equals("CIERRE")) {
							log.info("Se leyo un cierre: " + linea);
							storeClosedPassed = true;
						} else{
							StringBuffer data = new StringBuffer();
							data.append(ArmsAgentConstants.Communication.SOCKET_CHANNEL).append(ArmsAgentConstants.Communication.FRAME_SEP).append(ArmsAgentConstants.Process.RECEIVE_RESP_SYS_OPERATION)
							.append(ArmsAgentConstants.Communication.FRAME_SEP).append("000").append(ArmsAgentConstants.Communication.FRAME_SEP)
							.append(storeNumber).append(ArmsAgentConstants.Communication.FRAME_SEP)
							.append(ArmsAgentConstants.Communication.TEMP_CONN).append(ArmsAgentConstants.Communication.FRAME_SEP)
							.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss")).append(ArmsAgentConstants.Communication.FRAME_SEP).append(linea);
							List list = Arrays.asList(p.split(data.toString()));
							Frame frame = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
									ArmsAgentConstants.Communication.FRAME_SEP);
							if (frame.loadData()) {
								sent = sendResponseSysc(frame);
							}
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
						/*
						 * si hubo un cierre se invoca el metodo de
						 * renombramiento de archivos
						 */
						if (storeClosedPassed) {
							renombrarFileSyscard();
							storeClosedPassed = false;
							valorPosicion = Util.rpad(String.valueOf(0), ArmsAgentConstants.Communication.SPACE, 20)
									+ ArmsAgentConstants.Communication.CRLF;
							posFileSeekWriter.write(valorPosicion.getBytes(), 0, POSFile.FROM_START_OF_FILE,
									POSFile.FLUSH, valorPosicion.length());
							punteroFile = obtieneOffsetSeek(syscardSeekFileName);
						} else {
							logguerCount--;
						}

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

	protected boolean sendResponseSysc(Frame frame) {
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


	public void renombrarFileSyscard() throws IOException {

		log.info("Inicia metodo de renombramiento");

		String[] parts = syscardFileName.split("\\.");
		String file1 = parts[0].concat(".1");
		String file2 = parts[0].concat(".2");
		String file3 = parts[0].concat(".3");

		if (Files.fileExists4690(file3)) {
			File4690 file46901 = new File4690(file1);
			File4690 file46902 = new File4690(file2);
			File4690 file46903 = new File4690(file3);
			File4690 file4690A = new File4690(syscardFileName);
			Files.deleteFile4690(file3);
			file46902.renameTo(file46903);
			file46901.renameTo(file46902);
			file4690A.renameTo(file46901);

		} else if (Files.fileExists4690(file2)) {

			File4690 file46901 = new File4690(file1);
			File4690 file46902 = new File4690(file2);
			File4690 file46903 = new File4690(file3);
			File4690 file4690A = new File4690(syscardFileName);
			file46902.renameTo(file46903);
			file46901.renameTo(file46902);
			file4690A.renameTo(file46901);

		} else if (Files.fileExists4690(file1)) {

			File4690 file46901 = new File4690(file1);
			File4690 file46902 = new File4690(file2);
			File4690 file4690A = new File4690(syscardFileName);
			file46901.renameTo(file46902);
			file4690A.renameTo(file46901);

		} else {

			File4690 file46901 = new File4690(file1);
			File4690 file4690A = new File4690(syscardFileName);
			file4690A.renameTo(file46901);

		}
	}
}
