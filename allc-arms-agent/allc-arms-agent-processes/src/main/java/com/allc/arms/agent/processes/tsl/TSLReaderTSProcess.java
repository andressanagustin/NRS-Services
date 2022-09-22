package com.allc.arms.agent.processes.tsl;

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

public class TSLReaderTSProcess extends AbstractProcess {
	protected Logger log = Logger.getLogger(TSLReaderTSProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	protected RandomAccessFile4690 randSeekRead = null;
	protected POSFile posFileSeekWriter = null;
	protected String tslSeekFileName;
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
			storeNumber = controllerStatusData.getStoreNumber();
			valorEnCero = Util.rpad(ArmsAgentConstants.Communication.CERO, ArmsAgentConstants.Communication.SPACE, 20)
					+ ArmsAgentConstants.Communication.CRLF;
			tslSeekFileName = properties.getObject("tslReader.file.seek");
			timeSleep = Long.parseLong(properties.getObject("tslReader.timeSleep").toString());
			connectClient();
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

	protected boolean readTSLReg() {
		boolean isStoreClosed = false;
		long tmp = 0;
		String valorPosicion;
		long punteroFile = 0;
		boolean sent = true;
		StringBuffer data = new StringBuffer();
		String reg = "";
		String linea = "";
		String decod = "";
		String value = "";
		List list;
		Frame frame;
		try {
			String newTslFileName = getEamtranFileName();
			if (newTslFileName == null)
				return false;
			boolean changeFiles = false;
			if (Files.fileExists(tslSeekFileName) && storeClosedPassed
					&& !newTslFileName.equalsIgnoreCase(Files.readSpecifictLineOfFile4690(tslSeekFileName, 2))) {
				changeFiles = true;
				log.info("Los archivos TSL tienen diferentes nombre, se modifica el tslSeekFile: " + tslSeekFileName);
			}
			storeClosedPassed = false;
			if (!Files.fileExists4690(tslSeekFileName) || changeFiles) {
				Files.creaEscribeDataArchivo4690(tslSeekFileName, valorEnCero, false);
				Files.creaEscribeDataArchivo4690(tslSeekFileName, newTslFileName + ArmsAgentConstants.Communication.CRLF, true);
				tslFileName = newTslFileName;
			} else {
				tslFileName = Files.readSpecifictLineOfFile4690(tslSeekFileName, 2);
			}

			data.append("S").append(ArmsAgentConstants.Communication.FRAME_SEP).append(ArmsAgentConstants.Process.TSL_PROCESS)
					.append(ArmsAgentConstants.Communication.FRAME_SEP).append("000").append(ArmsAgentConstants.Communication.FRAME_SEP)
					.append(storeNumber).append(ArmsAgentConstants.Communication.FRAME_SEP)
					.append(ArmsAgentConstants.Communication.PERM_CONN).append(ArmsAgentConstants.Communication.FRAME_SEP);

			randSeekRead = new RandomAccessFile4690(tslSeekFileName, "r");
			punteroFile = obtieneOffsetSeek(tslSeekFileName);
			log.info("puntero:" + punteroFile);
			if (punteroFile >= 0) {
				posFileSeekWriter = new POSFile(tslSeekFileName, "rw", POSFile.SHARED_READ_WRITE_ACCESS);

				while (!endTSLProcess && !isStoreClosed) {
					log.info("Leyendo tsl:" + (punteroFile + 1));
					linea = Files.readSpecifictLineOfFile4690(tslFileName, punteroFile + 1);
					if (null != linea && !linea.equals("")) {
						log.info(linea);
						reg = linea.substring(1, linea.length()) + "," + "\"";
						decod = ident(reg);
						if (decod.length() > 0) {
							log.info("decod: " + decod);
							String[] decodArray = decod.split("\\|");
							log.debug("TYPE: " + decodArray[0].toString());
							/** StoreClosing **/
							if (decodArray[0].toString().equals(ArmsAgentConstants.Tsl.STORE_CLOSING)) {
								isStoreClosed = true;
								storeClosedPassed = true;
								log.info("CIERRE ENCONTRADO");
								comprimirJournals(businessDateDay, DateFormatUtils.format(new Date(), "yyyyMMdd"));
								log.info("JOURNALS COMPRIMIDOS");
								value = data.toString() + DateFormatUtils.format(new Date(), "yyyyMMddHHmmss")
										+ ArmsAgentConstants.Communication.FRAME_SEP + decod + ArmsAgentConstants.Communication.FRAME_SEP
										+ businessDateDay;
								list = Arrays.asList(p.split(value));
								frame = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
										ArmsAgentConstants.Communication.FRAME_SEP);
								if (frame.loadData()) {
									sent = sendTSLRecord(frame);
									if(!sent){
										isStoreClosed = false;
										storeClosedPassed = false;
									}
								}
							}
						}
						if (!isStoreClosed && sent) {
							tmp = punteroFile++;
							valorPosicion = Util.rpad(String.valueOf(tmp), ArmsAgentConstants.Communication.SPACE, 20)
									+ ArmsAgentConstants.Communication.CRLF;
							posFileSeekWriter.write(valorPosicion.getBytes(), 0, POSFile.FROM_START_OF_FILE, POSFile.FLUSH,
									valorPosicion.length());
						}
					} else {
						Thread.sleep(timeSleep);
					}
				}
				try {
					posFileSeekWriter.closeFull();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				try {
					randSeekRead.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}

				if (isStoreClosed) {
					if (Files.deleteFile4690(tslSeekFileName))
						log.info("store closing was detected, then seekFile " + tslSeekFileName + " was deleted");
					else
						log.info("store closing was detected, but seekFile " + tslSeekFileName + " wasn't deleted");
				}
			} else {
				try {
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
		if (socketClient != null)
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
			if (socketClient.writeDataSocket(trama)) {
				int numberOfBytes = 0;
				int timeOutCycles = 0;
				while (numberOfBytes == 0){
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
				if (journalName.toUpperCase().startsWith("J" + mesDia)) {
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
				businessDateDay = TSLUtility.unpack(secondPart.substring(4, 9).getBytes());
			} else
				log.error("cannot find the path for the file " + eamtermsPathName + ":" + eamtermsFileName);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return eamtranFileName;
	}

	protected String ident(String dato) {
		byte[] arreglop, arregloj;
		arreglop = dato.substring(0, 1).getBytes();
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

					arregloj = cadena.substring(0, 1).getBytes();
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

}
