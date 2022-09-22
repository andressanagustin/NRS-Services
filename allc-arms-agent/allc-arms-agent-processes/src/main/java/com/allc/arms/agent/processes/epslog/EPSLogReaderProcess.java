package com.allc.arms.agent.processes.epslog;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.tsl.TSLUtility;
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

public class EPSLogReaderProcess extends AbstractProcess {
	protected Logger log = Logger.getLogger(EPSLogReaderProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	protected RandomAccessFile4690 randSeekRead = null;
	protected RandomAccessFile4690 randELFileRead = null;
	protected POSFile posFileSeekWriter = null;
	protected String elSeekFileName;
	protected long timeSleep;
	protected Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
	protected Pattern R = Pattern.compile(ArmsAgentConstants.Communication.REGEX2);
	protected boolean endTSLProcess = false;
	protected boolean finished = false;
	protected String storeNumber = "";
	protected String elFileName = null;
	protected String businessDateDay;
	protected ConnSocketClient socketClient;

	protected boolean init() {
		boolean result = false;
		try {
			ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
			storeNumber = controllerStatusData.getStoreNumber();
			elSeekFileName = properties.getObject("epsLogReader.file.seek");
			elFileName = properties.getObject("epsLogReader.file.name");
			timeSleep = Long.parseLong(properties.getObject("epsLogReader.timeSleep").toString());
			setBusinessDateDay();
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
					readEPSLogReg();
					Thread.sleep(timeSleep);
				}
				closeClient();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		finished = true;
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

	protected void readEPSLogReg() {
		int filePointer;
		String data = "";
		StringBuffer mess = new StringBuffer("");
		Frame frame;
		List list;
		String value = "";
		boolean sent = true;
		try {
			/** Obtain the last position since where must send the information **/
			filePointer = getOffset(elSeekFileName, elFileName);
			log.info("Puntero1 " + (filePointer));
			if (filePointer >= 0) {
				
				posFileSeekWriter = new POSFile(elSeekFileName, "rw", POSFile.SHARED_READ_WRITE_ACCESS);
				/** open the fileSeek to Read **/
				randELFileRead = new RandomAccessFile4690(elFileName, "r");
				//utilizamos este contador para que si no avanza el puntero loguee cada 10 ciclos
				int logguerCount = 10;
				/** Read file line by line **/
				while (null != data && !endTSLProcess) {
					if(logguerCount == 10) {
						log.info("Puntero2 " + (filePointer));
					} else if(logguerCount == 0) {
						logguerCount = 10;
					}
					
					data = TSLUtility.leerArchivo4690(elFileName, 512, filePointer);
					if (null != data && !data.trim().equals("")) {
						mess = new StringBuffer("");
						mess.append("S").append(ArmsAgentConstants.Communication.FRAME_SEP)
								.append(ArmsAgentConstants.Process.EPS_LOG_PROCESS).append(ArmsAgentConstants.Communication.FRAME_SEP)
								.append("000").append(ArmsAgentConstants.Communication.FRAME_SEP).append(storeNumber)
								.append(ArmsAgentConstants.Communication.FRAME_SEP).append(ArmsAgentConstants.Communication.PERM_CONN)
								.append(ArmsAgentConstants.Communication.FRAME_SEP)
								.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
								.append(ArmsAgentConstants.Communication.FRAME_SEP);

						mess.append(data.toString()).append(ArmsAgentConstants.Communication.FRAME_SEP).append(businessDateDay);
						value = mess.toString();

						list = Arrays.asList(p.split(value));

						frame = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
								ArmsAgentConstants.Communication.FRAME_SEP);
						if (frame.loadData()) {
							sent = sendEPSLogRecord(frame);
							/** if successful then update the seek file **/
							if (sent) {
								int tmp = filePointer+512;
								log.info("tmp nuevo: "+tmp);
								String valorPosicion = Util.rpad(String.valueOf(tmp), ArmsAgentConstants.Communication.SPACE, 20)
										+ ArmsAgentConstants.Communication.CRLF;
								posFileSeekWriter.write(valorPosicion.getBytes(), 0, POSFile.FROM_START_OF_FILE, POSFile.FLUSH,
										valorPosicion.length());
								filePointer = tmp;
							} else {
								Thread.sleep(timeSleep);
							}
						}
					} else {
						logguerCount--;
						Thread.sleep(timeSleep);
					}
				}

				/** seek writer **/
				try {
					posFileSeekWriter.closeFull();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				/** seek reader **/
				try {
					randSeekRead.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				/** EL file reader **/
				try {
					randELFileRead.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void setBusinessDateDay() {
		try {
			String eamtranFilePrefix = "EAMTRAN";
			String eamtermsPathName = (String) properties.getObject("TSL.path.eamterms");
			String eamtermsFileName = (String) properties.getObject("TSL.file.eamterms");
			File4690 archivo = new File4690(eamtermsPathName + File4690.separatorChar + eamtermsFileName);
			log.info("Nombre Archivo: " + archivo.getAbsolutePath());
			String dataFile = TSLUtility.leerArchivo4690(eamtermsPathName + File4690.separatorChar + eamtermsFileName);
			if (null != dataFile) {
				String secondPart = dataFile.split(eamtranFilePrefix)[1];
				businessDateDay = TSLUtility.unpack(secondPart.substring(4, 9).getBytes());
			} else
				log.error("cannot find the path for the file " + eamtermsPathName + ":" + eamtermsFileName);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
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

	protected boolean sendEPSLogRecord(Frame frame) {
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
				while (numberOfBytes == 0) {
					numberOfBytes = socketClient.readLengthDataSocket();
				}
				if (numberOfBytes > 0) {
					str = socketClient.readDataSocket(numberOfBytes);
					if (StringUtils.isNotBlank(str)) {
						list = Arrays.asList(p.split(str));
						frameRpta = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
								ArmsAgentConstants.Communication.FRAME_SEP);
						log.info("Respuesta recibida: " + frameRpta.toString());
						log.info("Status: "+ frameRpta.getStatusTrama());
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
		}
		return false;
	}

	/**
	 * Obtiene la posicion del archivo dataFileName desde la que se debe obtener la informacion para registrarla como ExceptionLog
	 * 
	 * @param dataFileSeekName
	 *            Nombre del archivo que indica la ultima posicion en el archivo dataFileName hasta donde se obtuvo la informacion para
	 *            registrarla como ExceptionLog
	 * @param dataFileName
	 *            Nombre del archivo que contiene la data que se registra como Exceptionlog
	 * @return La posicion en el archivo dataFileName desde la que se debe de tomar la informacion para registrarla como ExceptionLog
	 */
	private int getOffset(String dataFileSeekName, String dataFileName) {
		int filePointer = 0, lengthDataFile = 0;
		try {

			if (!Files.fileExists4690(elSeekFileName)) {
				if (null != elFileName) {
					Files.creaEscribeDataArchivo4690(elSeekFileName,
							Util.rpad(ArmsAgentConstants.Communication.CERO, ArmsAgentConstants.Communication.SPACE, 20)
									+ ArmsAgentConstants.Communication.CRLF, false);
					Files.creaEscribeDataArchivo4690(elSeekFileName, elFileName + ArmsAgentConstants.Communication.CRLF, true);
				}
			}

			randSeekRead = new RandomAccessFile4690(elSeekFileName, "r");
			filePointer = getOffsetFile(dataFileSeekName);
			log.info("File Pointer: "+filePointer);
			if (filePointer > 0) {
				randELFileRead = new RandomAccessFile4690(elFileName, "r");
				lengthDataFile = (int) randELFileRead.length();
				randELFileRead.close();
				/**
				 * si la posicion del archivo es mayor al tama�o del archivo que contiene la data, entonces hubo un cierre y se trata de un
				 * archivo nuevo. Se registrara la info desde el inicio
				 **/
				if (filePointer > lengthDataFile) {
					filePointer = 0;
					setBusinessDateDay();
					log.info("getOffset: Se detecto cierre. se enviara la informacion desde el inicio. " + dataFileSeekName + ": "
							+ filePointer + " vs " + dataFileName + ": " + lengthDataFile);
				}
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return filePointer;
	}

	/**
	 * Obtain the file offset
	 * 
	 * @param fileName
	 *            File that contains the offset
	 * @return Position
	 */
	private int getOffsetFile(String fileName) {
		int filePointer;
		String data;
		try {
			data = randSeekRead.readLine();
			randSeekRead.seek(0);

			if (null == data)
				filePointer = 0;
			else
				try {
					filePointer = Integer.parseInt(data.replaceAll(" ", ""));
				} catch (Exception e) {
					log.error("getOffsetFile: the file " + fileName + " not contain a number as a pointer. ", e);
					filePointer = -1;
				}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			filePointer = -1;
		}
		return filePointer;
	}

}
