package com.allc.arms.agent.processes.el;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.keyed.KeyedFileBean;
import com.allc.arms.utils.keyed.KeyedFileMethods;
import com.allc.arms.utils.keyed.Util4690;
import com.allc.arms.utils.tsl.TSLUtility;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.ControllerFiles;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.KeyedFile;
import com.ibm.OS4690.POSFile;
import com.ibm.OS4690.RandomAccessFile4690;

public class ELReaderProcess extends AbstractProcess {
	protected Logger log = Logger.getLogger(ELReaderProcess.class);
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
	protected String businessDateDay = null;
	protected String businessDateDayOld = null;
	protected ConnSocketClient socketClient;
	private KeyedFileBean keyedFileBean = new KeyedFileBean();
	public static boolean storeClosePassed;
	protected int sequenceNumber = 0;
	
	protected boolean init() {
		boolean result = false;
		try {
			storeClosePassed = false;
			ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
			storeNumber = (new Integer(controllerStatusData.getStoreNumber())).toString();
			while(storeNumber.length() < 3)
				storeNumber = "0" + storeNumber;
			Files.creaEscribeDataArchivo4690(getEyesFileName(), "EL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Iniciando proceso de lectura de ExceptionLog.\n", true);
			elSeekFileName = properties.getObject("elReader.file.seek");
			elFileName = properties.getObject("elReader.file.name");
			timeSleep = Long.parseLong(properties.getObject("elReader.timeSleep").toString());
			String eamtermsPathName = (String) properties.getObject("TSL.path.eamterms");
			String eamtermsFileName = (String) properties.getObject("TSL.file.eamterms");
			keyedFileBean.setPathAndFileName(eamtermsPathName + File4690.separatorChar + eamtermsFileName);
			keyedFileBean.setMode("rw");
			keyedFileBean.setAccess(KeyedFile.SHARED_READ_WRITE_ACCESS);
			keyedFileBean.setFileType(KeyedFile.MIRRORED_FILE);
			keyedFileBean.setDistributionMethod(KeyedFile.COMPOUND_FILE);
			keyedFileBean.setKeyLength(3);
			keyedFileBean.setRecordSize(9);
			readCloseCtrlFlag();
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
					readTSLReg();
					Thread.sleep(timeSleep);
				}
				closeClient();
				Files.creaEscribeDataArchivo4690(getEyesFileName(), "EL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Finalizó el proceso de lectura de ExceptionLog.\n", true);
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(), "EL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al iniciar el proceso de lectura de ExceptionLog.\n", true);
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(), "EL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error en el proceso de lectura de ExceptionLog.\n", true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
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

	protected void readTSLReg() {
		long filePointer;
		String line = "";
		String data;
		StringBuffer mess = new StringBuffer("");
		Frame frame;
		List list;
		String value = "";
		long tmp = 0;
		boolean sent = true;
		try {
			/** Obtain the last position since where must send the information **/
			filePointer = getOffset(elSeekFileName, elFileName);

			if (filePointer >= 0) {
				posFileSeekWriter = new POSFile(elSeekFileName, "rw", POSFile.SHARED_READ_WRITE_ACCESS);
				/** open the fileSeek to Read **/
				randELFileRead = new RandomAccessFile4690(elFileName, "r");
				
				boolean errorReadEL = false;
				int logguerCount = 10;
				/** Read file line by line **/
				while (!endTSLProcess && !errorReadEL && !storeClosePassed) {
					try {
						line = Files.readLineByBytesPositionOfFile4690(randELFileRead, filePointer);
					} catch (Exception e) {
						//agregamos esto para que cuando hagan un cierre "forzado" no quede en un bucle infinito
						line = null;
						errorReadEL = true;
						log.error(e.getMessage(), e);
						Files.creaEscribeDataArchivo4690(getEyesFileName(), "EL_READ_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al leer el archivo: "+elFileName+". Se volverá a crear el Puntero.\n", true);
						Files.deleteFile4690(elSeekFileName);
					}
					if(logguerCount == 10) {
						log.info("Puntero" + (filePointer) + "LINE: " + line);
					} else if(logguerCount == 0) {
						logguerCount = 10;
					}
					if(businessDateDay == null || businessDateDay.equals("00000000"))
						setBusinessDateDay();
					if (((businessDateDay!= null && !businessDateDay.equals("00000000")) || (businessDateDayOld!= null && !businessDateDayOld.equals("00000000"))) && null != line && !line.trim().equals("")) {
						line = line.substring(1, line.length() - 1);
						/** Obtain the data to manipulate **/
						data = ident(line);
						if (data != null) {
							sequenceNumber++;
							mess = new StringBuffer("");
							mess.append("S").append(ArmsAgentConstants.Communication.FRAME_SEP)
									.append(ArmsAgentConstants.Process.EL_PROCESS).append(ArmsAgentConstants.Communication.FRAME_SEP)
									.append("000").append(ArmsAgentConstants.Communication.FRAME_SEP).append(storeNumber)
									.append(ArmsAgentConstants.Communication.FRAME_SEP).append(ArmsAgentConstants.Communication.PERM_CONN)
									.append(ArmsAgentConstants.Communication.FRAME_SEP)
									.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
									.append(ArmsAgentConstants.Communication.FRAME_SEP);

							mess.append(data.toString()).append(ArmsAgentConstants.Communication.FRAME_SEP).append(businessDateDay.equals("00000000") ? businessDateDayOld : businessDateDay)
							.append(ArmsAgentConstants.Communication.FRAME_SEP)
							.append(sequenceNumber);
							value = mess.toString();

							list = Arrays.asList(p.split(value));

							frame = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
									ArmsAgentConstants.Communication.FRAME_SEP);
							if (frame.loadData()) {
								sent = sendTSLRecord(frame);
								/** if successful then update the seek file **/
								if (sent) {
									filePointer = randELFileRead.getFilePointer();
									tmp = filePointer;
									String valorPosicion = Util.rpad(String.valueOf(tmp), ArmsAgentConstants.Communication.SPACE, 20)
											+ ArmsAgentConstants.Communication.CRLF;
									posFileSeekWriter.write(valorPosicion.getBytes(), 0, POSFile.FROM_START_OF_FILE, POSFile.FLUSH,
											valorPosicion.length());
								} else {
									Thread.sleep(timeSleep);
								}
							}
						} else {
							filePointer = randELFileRead.getFilePointer();
							tmp = filePointer;
							String valorPosicion = Util.rpad(String.valueOf(tmp), ArmsAgentConstants.Communication.SPACE, 20)
									+ ArmsAgentConstants.Communication.CRLF;
							posFileSeekWriter.write(valorPosicion.getBytes(), 0, POSFile.FROM_START_OF_FILE, POSFile.FLUSH,
									valorPosicion.length());
						}
					} else if((null == line || line.trim().equals("")) && filePointer != randELFileRead.getFilePointer()){
						//si el puntero cambio es porque shay dos 0D0A seguidos y tenemos que avanzar el puntero para que continue leyendo
						log.info("Punteros diferentes: filePointer: "+filePointer+" filePointer: "+randELFileRead.getFilePointer());
						filePointer = randELFileRead.getFilePointer();
						tmp = filePointer;
						String valorPosicion = Util.rpad(String.valueOf(tmp), ArmsAgentConstants.Communication.SPACE, 20)
								+ ArmsAgentConstants.Communication.CRLF;
						posFileSeekWriter.write(valorPosicion.getBytes(), 0, POSFile.FROM_START_OF_FILE, POSFile.FLUSH,
								valorPosicion.length());
					} else {
						logguerCount--;
						if(!errorReadEL)
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
	
	public int readCloseCtrlFlag() {
		try {
			KeyedFileMethods.openFile(keyedFileBean);
			int recordSize = keyedFileBean.getKeyedFile().getRecordSize();
			byte[] key = Util4690.pack("9999");
			byte[] record = new byte[recordSize];
			System.arraycopy(key, 0, record, 0, key.length);
			keyedFileBean.getKeyedFile().read(record, 1);
			String registro = new String(record);
			log.info("Leido: " + registro.substring(2));
			SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmm");
			Date fecha = formatter.parse(ControllerFiles.unpack(registro.substring(13, 18).getBytes()));
			log.info("Datetime:"+formatter.format(fecha));
			int closeFlg = registro.substring(12, 13).getBytes() [0];
			log.info("CloseFlg:"+closeFlg);
			return closeFlg;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			KeyedFileMethods.closeFile(keyedFileBean);
		}
		return 0;
	}

	private void setBusinessDateDay() {
		try {
			businessDateDay = readFechaContable();
			if("00000000".equals(businessDateDay)){
				businessDateDayOld = readFechaContableOld();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private String getEyesFileName(){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
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
	private long getOffset(String dataFileSeekName, String dataFileName) {
		long filePointer = 0;
		try {
			//si hubo un cierre borramos el puntero
			if(storeClosePassed){
				sequenceNumber = 0;
				businessDateDay = null;
				readCloseCtrlFlag();
				Files.deleteFile4690(elSeekFileName);
				Thread.sleep(30000);
				if(0 == readCloseCtrlFlag())
					storeClosePassed = false;
				else
					return -1;
			}
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
//			if (filePointer > 0) {
//				randELFileRead = new RandomAccessFile4690(elFileName, "r");
//				lengthDataFile = randELFileRead.length();
//				randELFileRead.close();
//				/**
//				 * si la posicion del archivo es mayor al tama�o del archivo que contiene la data, entonces hubo un cierre y se trata de un
//				 * archivo nuevo. Se registrara la info desde el inicio
//				 **/
//				if (filePointer > lengthDataFile) {
//					filePointer = 0;
//					setBusinessDateDay();
//					log.info("getOffset: Se detecto cierre. se enviara la informacion desde el inicio. " + dataFileSeekName + ": "
//							+ filePointer + " vs " + dataFileName + ": " + lengthDataFile);
//				}
//			}

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
	private long getOffsetFile(String fileName) {
		long filePointer;
		String data;
		try {
			data = randSeekRead.readLine();
			randSeekRead.seek(0);

			if (null == data)
				filePointer = 0;
			else
				try {
					filePointer = Long.parseLong(data.replaceAll(" ", ""));
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

	private String obtiene(List entrada, int pos, int opc) {
		String salida = "";
		byte[] arregloB;
		try {
			if (opc == 1) {
				salida = entrada.get(pos).toString();
				if (salida.length() > 0) {
					arregloB = salida.getBytes();
					salida = TSLUtility.unpack(arregloB);
				} else
					salida = "";
			} else {
				salida = entrada.get(pos).toString();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return salida;
	}

	private String parser(List reg, int pp) {
		String salida = "";

		try {
			switch (pp) {
				case 20: {
					log.info("case 20");
					salida = TSLUtility.campos(reg, "0111110000000000");
					break;
				}
				case 21: {
					log.info("case 21");
					salida = TSLUtility.campos(reg, "0111110000000000");
					break;
				}
				case 23: {
					log.info("case 23");
					salida = TSLUtility.campos(reg, "0111110000000000");
					break;
				}
				case 24: {
					log.info("case 24");
					salida = TSLUtility.campos(reg, "011110000000000");
					break;
				}
				default:
					return null;
			}
		} catch (Exception e) {
			log.error("parser: " + e);
		}

		return salida;
	}

	/**
	 * 
	 * @param dato
	 * @return
	 */
	private String ident(String dato) {
		String sal1 = "";
		String salida = "";
		try {
			/** Se carga la data en una lista **/
			List list = Arrays.asList(R.split(dato));
			sal1 = list.size() > 1 ? obtiene(list, 3, 1) : "";
			log.info("SAL1: " + sal1);
			try {
				sal1 = sal1.replace('F', '0');
				int pp = (new Integer(sal1)).intValue();
				salida = parser(list, pp);
				log.info("SALIDA: " + salida);
			} catch (Exception e) {
				log.error(e.getMessage() + " dato: " + dato, e);
			}

		} catch (Exception e) {
			log.error("ident: " + e);
		}
		return salida;
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
	
	public String readFechaContableOld() {		
		try {
			RandomAccessFile4690 raf = new RandomAccessFile4690("C:/ADX_UDT1/LXACCTDT.OLD", "r");
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

}
