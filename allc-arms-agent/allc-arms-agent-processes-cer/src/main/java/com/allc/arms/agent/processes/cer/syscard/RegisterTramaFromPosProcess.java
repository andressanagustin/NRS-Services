package com.allc.arms.agent.processes.cer.syscard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.keyed.KeyedFileBean;
import com.allc.arms.utils.keyed.KeyedFileMethods;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.Files;
import com.allc.files.helper.FilesHelper;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileFilter4690;
import com.ibm.OS4690.FlexosException;
import com.ibm.OS4690.KeyedFile;
import com.ibm.OS4690.RandomAccessFile4690;

public class RegisterTramaFromPosProcess extends AbstractProcess {
	public final int BITMAP_2_ACTIVE = 1;
	public final int PRIMARY_ACCOUNT_NUMBER = 2;
	public final int PROCESSING_CODE = 3;
	public final int TRANSACTION_AMOUNT = 4;
	public final int SYSTEM_TRACE_AUDIT_NUMBER = 11;
	public final int LOCAL_TRANSACTION_TIME = 12;
	public final int LOCAL_TRANSACTION_DATE = 13;
	public final int EXPIRATION_DATE = 14;
	public final int POS_ENTRY_MODE = 22;
	public final int NETWORK_INTERNATIONAL_ID = 24;
	public final int POS_CONDITION_CODE = 25;
	public final int TRACK_2_DATA = 35;
	public final int RETRIEVAL_REFERENCE_NUMBER = 37;
	public final int AUTORIZATION_NUMBER = 38;
	public final int RESPONSE_CODE = 39;
	public final int TERMINAL_ID = 41;
	public final int MERCHANT_ID = 42;
	public final int TRACK_1 = 45;
	public final int NUMERO_LOTE_ACTIVO = 61;
	public final int ADICIONAL_DATA = 105;
	public final int ADICIONAL_DATA_2 = 112;
	public final int ADICIONAL_DATA_3 = 114;
	public final int RESERVED_FOR_NATIONAL_USE = 116;
	public final int ADICIONAL_DATA_4 = 120;
	public final int ADICIONAL_DATA_5 = 122;
	public final int RESERVED_FOR_PRIVATE_USE = 124;
	public final int RESERVED = 125;

	protected Logger log = Logger.getLogger(RegisterTramaFromPosProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	protected long timeSleep;
	protected String valorEnCero;
	protected Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
	protected boolean endSyscardProcess = false;
	protected boolean finished = false;
	protected String storeNumber = "";
	protected String syscardFileName = "";
	protected String actualDateDay;
	private Iterator filesToProcess = null;
	private File4690 inFolder;
	private KeyedFileBean keyedFileBean = new KeyedFileBean();
	Map hashProcessFiles = null;

	protected boolean init() {
		boolean result = false;
		try {
			ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
			storeNumber = (new Integer(controllerStatusData.getStoreNumber())).toString();
			while (storeNumber.length() < 3)
				storeNumber = "0" + storeNumber;
			Files.creaEscribeDataArchivo4690(getEyesFileName(),
					"REG_TR_FRM_POS_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + storeNumber
							+ "|STR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando proceso de lectura del archivos de tramas enviadas desde el POS.\n",
					true);
			valorEnCero = Util.rpad(ArmsAgentConstants.Communication.CERO, ArmsAgentConstants.Communication.SPACE, 20)
					+ ArmsAgentConstants.Communication.CRLF;
			timeSleep = properties.getLong("syscardReader.timeSleep");
			inFolder = new File4690("c:/ADX_IDT4");
			inFolder.mkdir();
			
			keyedFileBean.setPathAndFileName("C:/ADX_UDT1/SCLOG.DAT");
			keyedFileBean.setMode("rw");
			keyedFileBean.setAccess(KeyedFile.SHARED_READ_WRITE_ACCESS);
			keyedFileBean.setFileType(KeyedFile.MIRRORED_FILE);
			keyedFileBean.setDistributionMethod(KeyedFile.COMPOUND_FILE);
			keyedFileBean.setKeyLength(3);
			keyedFileBean.setRecordSize(9);
			String fechaReg = readSeek("000");
			if(fechaReg != null && fechaReg.length() >= 4)
				actualDateDay = fechaReg.substring(fechaReg.length()-4);
			else {
				actualDateDay = getFechaActual();
				writeSeek("000", Util.lpad(actualDateDay, ArmsAgentConstants.Communication.CERO, 6));
			}
			hashProcessFiles = new HashMap();
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
						"REG_TR_FRM_POS_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|END|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Finaliza el proceso de lectura del archivo de tramas enviadas desde el POS.\n",
						true);
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(),
						"REG_TR_FRM_POS_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|ERR|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al iniciar el proceso de lectura del archivo de tramas enviadas desde el POS.\n",
						true);
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(),
						"REG_TR_FRM_POS_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ storeNumber + "|ERR|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error en el proceso de lectura de tramas de enviadas desde el POS.\n",
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
		log.info("Deteniendo RegisterTramaFromPosProcess...");
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
		String valorPosicion;
		long punteroFile = 0;
		boolean sent = true;
		String linea = "";
		RandomAccessFile4690 randFileRead = null;
		
		try {
			int logguerCount = 10;
			while(!endSyscardProcess && syscardFileName != null) {
				syscardFileName = getNextSCLogFile().getName();
				//log.info("Archivo a procesar: "+syscardFileName);
				String terminal = syscardFileName.substring(syscardFileName.length()-3);
				punteroFile = Long.valueOf(readSeek(terminal)).longValue();
				//log.info("puntero:" + punteroFile);
				if(logguerCount == 10) {
					log.info("Archivo a procesar: "+syscardFileName);
					log.info("puntero:" + punteroFile);
				} else if(logguerCount == 0) {
					logguerCount = 11;
				}
				if(punteroFile == 0){
					valorPosicion = Util.lpad(String.valueOf(punteroFile), ArmsAgentConstants.Communication.CERO, 6);
					writeSeek(terminal, valorPosicion);
				}
				linea = "";

				if(!hashProcessFiles.containsKey(terminal)){
					randFileRead = new RandomAccessFile4690(inFolder+"/"+syscardFileName, "r");
					hashProcessFiles.put(terminal, randFileRead);
				}else{
					randFileRead = (RandomAccessFile4690) hashProcessFiles.get(terminal);
				}
					
				if (punteroFile >= 0) {
					while (!endSyscardProcess && linea != null) {
						linea = Files.readLineByBytesPositionOfFile4690(randFileRead, punteroFile);
//						log.info("Linea leida: "+ linea);
						if (null != linea) {
							if (linea.length() > 18 && (linea.substring(14, 18).equals("0200") || linea.substring(14, 18).equals("0210") || linea.substring(14, 18).equals("0400") || linea.substring(14, 18).equals("0410") || linea.substring(14, 18).equals("0800") || linea.substring(14, 18).equals("0810"))) {
								sent = writeTrama(linea);
							}
							if (sent) {
								punteroFile = randFileRead.getFilePointer();
								valorPosicion = Util.lpad(String.valueOf(punteroFile), ArmsAgentConstants.Communication.CERO, 6);
								writeSeek(terminal, valorPosicion);
							}
						} else
							break;
					}
					
				}					
				
				if(!filesToProcess.hasNext()){
					if (!actualDateDay.equals(getFechaActual())) {
						Iterator itFechas = obtenerFechasIntermedias(actualDateDay, getFechaActual()).iterator();
						while (itFechas.hasNext()) {
							actualDateDay = (String) itFechas.next();
							File4690[] files = inFolder.listFiles(new FileFilter4690() {
								public boolean accept(File4690 pathname) {
									return pathname.isFile() && pathname.getName().toUpperCase().startsWith("SCLG"+actualDateDay);
								}
							});
							if(files != null && files.length > 0) {
								Files.deleteFile4690("C:/ADX_UDT1/SCLOG.DAT");
								
								FilesHelper.copyFile4690("f:/allc_pgm/ArmsAgent/Respaldo", "C:/ADX_UDT1" , "SCLOG.DAT", "SCLOG.DAT");
								
								valorPosicion = Util.lpad(actualDateDay, ArmsAgentConstants.Communication.CERO, 6);
								writeSeek("000", valorPosicion);
								break;
							}
						}

						Iterator it = hashProcessFiles.values().iterator();

					    while (it.hasNext()) {
					    	randFileRead = (RandomAccessFile4690)it.next();
					        randFileRead.close();
					   }
					    hashProcessFiles = new HashMap();
					   
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
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return true;
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
	
	private File4690 getNextSCLogFile() {
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				File4690[] files = inFolder.listFiles(new FileFilter4690() {
					public boolean accept(File4690 pathname) {
						return pathname.isFile() && pathname.getName().toUpperCase().startsWith("SCLG"+actualDateDay);
					}
				});
				if (files.length == 0) {
					if (!actualDateDay.equals(getFechaActual())) {
						Iterator itFechas = obtenerFechasIntermedias(actualDateDay, getFechaActual()).iterator();
						while (itFechas.hasNext()) {
							actualDateDay = (String) itFechas.next();
							File4690[] filesTemp = inFolder.listFiles(new FileFilter4690() {
								public boolean accept(File4690 pathname) {
									return pathname.isFile() && pathname.getName().toUpperCase().startsWith("SCLG"+actualDateDay);
								}
							});
							if(filesTemp != null && filesTemp.length > 0) {
								break;
							}
						}
					} else {
						try {
							Thread.sleep(timeSleep);
						} catch (InterruptedException e) {
							log.error(e.getMessage(), e);
						}
					}
				} else {
					this.filesToProcess = Arrays.asList(files).iterator();
				}
			} while (((this.filesToProcess == null) || !filesToProcess.hasNext()));
		}
		return (File4690) this.filesToProcess.next();
	}

	public boolean writeSeek(String key, String pointerData) {
		boolean result = false;
		int numberBytesWritten = 0;
		try {
			KeyedFileMethods.openFile(keyedFileBean);
			byte[] pointerRecord = new byte[keyedFileBean.getRecordSize()];
			System.arraycopy(key.getBytes(), 0, pointerRecord, 0, key.length());
			System.arraycopy(pointerData.getBytes(), 0, pointerRecord, 3, pointerData.length());
			numberBytesWritten = keyedFileBean.getKeyedFile().write(pointerRecord, KeyedFile.NO_UNLOCK,
					KeyedFile.NO_HOLD);

			if (numberBytesWritten == keyedFileBean.getKeyedFile().getRecordSize())
				result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			KeyedFileMethods.closeFile(keyedFileBean);
		}
		return result;
	}

	public String readSeek(String key) {
		try {
			KeyedFileMethods.openFile(keyedFileBean);
			int recordSize = keyedFileBean.getKeyedFile().getRecordSize();
			byte[] pointerRecord = new byte[recordSize];
			System.arraycopy(key.getBytes(), 0, pointerRecord, 0, key.length());
			keyedFileBean.getKeyedFile().read(pointerRecord, 1);
			String record = new String(pointerRecord);
//			log.info("Leido: " + record.substring(record.length()-6));
			return record.substring(record.length()-6);
		} catch (FlexosException fe) {
			if(fe.getReturnCode() == -2131556664)
				log.info("Clave: "+key+" no encontrada.");
			else
				log.error(fe.getMessage(), fe);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			KeyedFileMethods.closeFile(keyedFileBean);
		}
		return "0";
	}

	protected boolean writeTrama(String linea) {
		boolean retorno = true;
		String msgData = createMsg(linea.substring(4));
		try {
			Files.creaEscribeDataArchivo4690("tramaFromPOS"+actualDateDay+".dat", msgData + "\n", true);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			retorno = false;
		}
		return retorno;
	}
	
	private StringBuffer translateHexaToBitmap(String hexa) {
		StringBuffer bitmap = new StringBuffer();
		for (int i = 0; i < hexa.length(); i = i + 2) {
			int decimal = Integer.parseInt(hexa.substring(i, i + 2), 16);
			String bitStr = Integer.toString(decimal, 2);
			while(bitStr.length() < 8)
				bitStr = ArmsAgentConstants.Communication.CERO + bitStr;
			bitmap.append(bitStr);
		}
		return bitmap;
	}
	private String createMsg(String data) {
		StringBuffer bitmap = translateHexaToBitmap(data.substring(14, 30));
		StringBuffer msg = new StringBuffer("MT="+data.substring(10, 14));
		int pos = 30;
		msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("BM1="+bitmap);
		if (bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') {
			pos += 16;
			StringBuffer bitmap2 = translateHexaToBitmap(data.substring(30, pos));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("BM2="+bitmap2);
			bitmap.append(bitmap2);
		}
		
		if (bitmap.charAt(PRIMARY_ACCOUNT_NUMBER - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2)).intValue();
			if(cantAleer % 2 == 1)
				cantAleer++;
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("PAC="+data.substring(pos+2, pos+2+cantAleer));
			pos = pos + 2 + cantAleer;
		}
		if (bitmap.charAt(PROCESSING_CODE - 1) == '1') {
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("PC="+data.substring(pos, pos+6));
			pos = pos + 6;
		}
		if (bitmap.charAt(TRANSACTION_AMOUNT - 1) == '1') {
			String amount = data.substring(pos, pos + 12);
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("AM="+amount);
			pos = pos + 12;
		}
		if (bitmap.charAt(SYSTEM_TRACE_AUDIT_NUMBER - 1) == '1') {
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("STAN="+data.substring(pos, pos+6));
			pos = pos + 6;
		}
		if (bitmap.charAt(LOCAL_TRANSACTION_TIME - 1) == '1') {
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("LTT="+data.substring(pos, pos+6));
			pos = pos + 6;
		}
		if (bitmap.charAt(LOCAL_TRANSACTION_DATE - 1) == '1') {
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("LTD="+data.substring(pos, pos+4));
			pos = pos + 4;
		}
		if (bitmap.charAt(EXPIRATION_DATE - 1) == '1') {
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("ED="+data.substring(pos, pos+4));
			pos = pos + 4;
		}
		if (bitmap.charAt(POS_ENTRY_MODE - 1) == '1') {
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("PEM="+data.substring(pos, pos+4));
			pos = pos + 4;
		}
		if (bitmap.charAt(NETWORK_INTERNATIONAL_ID - 1) == '1') {
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("NII="+data.substring(pos, pos+4));
			pos = pos + 4;
		}
		if (bitmap.charAt(POS_CONDITION_CODE - 1) == '1') {
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("PCC="+data.substring(pos, pos+2));
			pos = pos + 2;
		}
		if (bitmap.charAt(TRACK_2_DATA - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2)).intValue();
			if(cantAleer % 2 == 1)
				cantAleer++;
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("T2D="+data.substring(pos+2, pos+2+cantAleer));
			pos = pos + 2 + (cantAleer % 2 == 0 ? cantAleer : cantAleer + 1);
		}
		if (bitmap.charAt(RETRIEVAL_REFERENCE_NUMBER - 1) == '1') {
			String reference = data.substring(pos, pos + 24);
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("RRN="+translateHexaStringToUtf8(data.substring(pos, pos+24)));
			pos = pos + 24;
			log.info("reference: "+reference);
		}
		if (bitmap.charAt(AUTORIZATION_NUMBER - 1) == '1') {
			String autNbr = translateHexaToUtf8(data.substring(pos, pos + 2)) + translateHexaToUtf8(data.substring(pos + 2, pos + 4)) + translateHexaToUtf8(data.substring(pos + 4, pos + 6)) + translateHexaToUtf8(data.substring(pos + 6, pos + 8)) + translateHexaToUtf8(data.substring(pos + 8, pos + 10)) + translateHexaToUtf8(data.substring(pos + 10, pos + 12));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("AN="+autNbr);
			pos = pos + 12;
		}
		if (bitmap.charAt(RESPONSE_CODE - 1) == '1') {
			String responseCode = translateHexaToUtf8(data.substring(pos, pos + 2)) + translateHexaToUtf8(data.substring(pos + 2, pos + 4));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("RC="+responseCode);
			log.info("Response Code: " + responseCode);
			pos = pos + 4;
		}
		if (bitmap.charAt(TERMINAL_ID - 1) == '1') {
			String terminal = data.substring(pos, pos + 16);
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("TI="+translateHexaStringToUtf8(terminal));
			log.info("terminal: "+terminal);
			pos = pos + 16;
		}
		if (bitmap.charAt(MERCHANT_ID - 1) == '1') {
			String merchant = data.substring(pos, pos + 30);
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("MI="+translateHexaStringToUtf8(merchant));
			log.info("merchant: "+merchant);
			pos = pos + 30;
		}
		if (bitmap.charAt(TRACK_1 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2)).intValue();
			String track1 = data.substring(pos+2, pos+2 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("T1D="+translateHexaStringToUtf8(track1));
			log.info("track1: "+track1);
			pos = pos + 2 + (cantAleer * 2);
		}
		if (bitmap.charAt(NUMERO_LOTE_ACTIVO - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			if(data.length() >= (pos+4 +(cantAleer * 2))){
				String lote = data.substring(pos+4, pos+4 +((cantAleer * 2)));
				msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("NLA="+translateHexaStringToUtf8(lote));
				log.info("lote: "+lote);
				pos = pos + 4 + (cantAleer * 2);
			} else {
				return msg.toString();
			}
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("AD="+translateHexaStringToUtf8(addData));
			log.info("addData1: "+addData);
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA_2 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("AD2="+translateHexaStringToUtf8(addData));
			log.info("addData2: "+addData);
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA_3 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("AD3="+translateHexaStringToUtf8(addData));
			log.info("addData3: "+addData);
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(RESERVED_FOR_NATIONAL_USE - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("RFNU="+translateHexaStringToUtf8(addData));
			log.info("res nat: "+addData);
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA_4 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("AD4="+translateHexaStringToUtf8(addData));
			log.info("addData4: "+addData);
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA_5 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("AD5="+translateHexaStringToUtf8(addData));
			log.info("addData5: "+addData);
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(RESERVED_FOR_PRIVATE_USE - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("RFPU="+translateHexaStringToUtf8(addData));
			log.info("res pri: "+addData);
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(RESERVED - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("RV="+translateHexaStringToUtf8(addData));
			log.info("res: "+addData);
			pos = pos + 4 + (cantAleer * 2);
		}

		return msg.toString();
	}
	
	private String translateHexaStringToUtf8(String data){
		String retorno = "";
		for(int i = 0; i < data.length()-1; i=i+2){
			retorno = retorno + translateHexaToUtf8(data.substring(i, i + 2));
		}
		return retorno;
	}
	
	private String translateHexaToUtf8(String hexa) {
		try {
			byte[] decodedHex = DatatypeConverter.parseHexBinary(hexa);
			return new String(decodedHex, "UTF-8");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}


}
