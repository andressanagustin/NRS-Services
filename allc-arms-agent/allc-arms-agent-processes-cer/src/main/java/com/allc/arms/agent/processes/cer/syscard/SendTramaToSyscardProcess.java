package com.allc.arms.agent.processes.cer.syscard;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
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

public class SendTramaToSyscardProcess extends AbstractProcess {
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

	protected Logger log = Logger.getLogger(SendTramaToSyscardProcess.class);
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
			syscardSeekFileName = properties.getObject("syscardReader.file.seek");
			timeSleep = Long.parseLong(properties.getObject("syscardReader.timeSleep").toString());

			result = true;
			businessDateDay = readFechaContable();
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
		// closeClient();
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
			String newSafesPsFileName = properties.getObject("syscard.file.name") + ".DAT";
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
			File4690 file = new File4690(syscardFileName);
			if(!file.exists())
				file.createNewFile();
			randFileRead = new RandomAccessFile4690(syscardFileName, "r");
			punteroFile = obtieneOffsetSeek(syscardSeekFileName);
			log.info("puntero:" + punteroFile);
			if (punteroFile >= 0) {
				posFileSeekWriter = new POSFile(syscardSeekFileName, "rw", POSFile.SHARED_READ_WRITE_ACCESS);
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
					
					if(randFileRead == null || randFileRead.getChannel4690() == null || !randFileRead.getChannel4690().isOpen())
						randFileRead = new RandomAccessFile4690(syscardFileName, "r");
					
					linea = Files.readLineByBytesPositionOfFile4690(randFileRead, punteroFile);
					if (null != linea && !linea.trim().equals("")) {
						
						if(businessDateDay == null || businessDateDay.equals("00000000")){
							businessDateDay = readFechaContable();
						}

						/*
						 * se verifica si la linea leida dentro del archivo
						 * corresponde a un cierre de tienda
						 */
						if (linea.equals("CIERRE")) {
							log.info("Se leyo un cierre: " + linea);
							storeClosedPassed = true;
							businessDateDay = "00000000";
						} else
							sent = sendTramaSyscard(linea);

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
							//necesitamos cerrar este archivo para poder renombrarlo
							randFileRead.close();
							renombrarFileSyscard();
							storeClosedPassed = false;
							punteroFile = 0;
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
					if(randFileRead != null)
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

	protected boolean sendTramaSyscard(String linea) {
		boolean retorno = true;

		ConnSocketClient socketClient = new ConnSocketClient();
		socketClient.setIpServer(properties.getObject("sendTramaSyscard.server.ip"));
		socketClient.setPortServer(properties.getInt("sendTramaSyscard.server.port"));
		socketClient.setRetries(properties.getInt("sendTramaSyscard.server.retries"));
		socketClient.setTimeOutConnection(properties.getInt("sendTramaSyscard.server.timeOut"));
		socketClient.setTimeOutSleep(properties.getInt("sendTramaSyscard.server.timeSleep"));
		socketClient.setQuantityBytesLength(2);
		log.info("Mensaje a Enviar: " + linea.toString().toUpperCase());
		try {
			String request = translateRequest(linea);
			writeTramaReq(request);
			String consData = getConsData(request.substring(4));
			String data = "";
			int cant;
		
			if (socketClient.connectSocketUsingRetries()) {
				log.info("Socket conectado");
				if (socketClient.writeByteArraySocket(linea.getBytes("ISO-8859-1"))) {
					log.info("Mensaje enviado");
					if (!socketClient.timeOutSocket()) {
						log.info("Leyendo respuesta");
						cant = socketClient.leeLongitudDataHexaSocket();
						log.info("Cantidad a leer(dec):" + cant);
						data = socketClient.readDataSocket(cant);
						log.info("Respuesta recibida:" + data);
						String response = translateResponse(data);
						writeTramaResp(response);
						Files.creaEscribeDataArchivo4690("responseSyscard.dat", generarLineaARegistrar(response) + consData + businessDateDay + "\n", true);
					} else {
						Files.creaEscribeDataArchivo4690("responseSyscard.dat", generarLineaARegistrar(translateRequest(linea))+ consData + businessDateDay + "\n", true);
					}
				}
				log.info("Cerrando conexión");
				socketClient.closeConnection();
			} else {
				log.info("Socket NO conectado");
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			retorno = false;
		}
		/*
		 * se verifica que la respuesta recibida sea distinta de null para
		 * confirmar el env�o exitoso de la trama
		 */
		return retorno;
	}
	
	private String translateResponse(String cadena) throws Exception {
		byte[] bytes = cadena.getBytes("ISO-8859-1");
		StringBuffer retorno = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String hexa = Integer.toString((bytes[i] << 0) & 0x000000ff, 16);
			retorno.append(hexa.length() < 2 ? "0" + hexa : hexa);
		}
		log.info("Respuesta: " + retorno.toString());
		return retorno.toString();
	}
	
	private String translateRequest(String cadena) throws Exception {
		byte[] bytes = cadena.getBytes("ISO-8859-1");
		StringBuffer retorno = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String hexa = Integer.toString((bytes[i] << 0) & 0x000000ff, 16);
			retorno.append(hexa.length() < 2 ? "0" + hexa : hexa);
		}
		log.info("Request: " + retorno.toString());
		return retorno.toString();
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
	
	protected boolean writeTramaReq(String linea) {
		boolean retorno = true;
		String msgData = createMsg(linea.substring(4));
		try {
			Files.creaEscribeDataArchivo("tramaFromPOS"+getFechaActual()+".dat", msgData + "\n", true);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			retorno = false;
		}
		return retorno;
	}
	
	protected boolean writeTramaResp(String linea) {
		boolean retorno = true;
		String msgData = createMsg(linea);
		try {
			Files.creaEscribeDataArchivo("tramaFromPOS"+getFechaActual()+".dat", msgData + "\n", true);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			retorno = false;
		}
		return retorno;
	}
	
	private String translateHexaStringToUtf8(String data){
		String retorno = "";
		for(int i = 0; i < data.length()-1; i=i+2){
			retorno = retorno + translateHexaToUtf8(data.substring(i, i + 2));
		}
		return retorno;
	}
	private String createMsg(String data) {
		StringBuffer bitmap = translateHexaToBitmap(data.substring(14, 30));
		StringBuffer msg = new StringBuffer("MT="+data.substring(10, 14));
//		log.info("Bitmap1:"+bitmap + " hexa: "+data.substring(14, 30));
		int pos = 30;
		msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("BM1="+bitmap);
		if (bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') {
			pos += 16;
			StringBuffer bitmap2 = translateHexaToBitmap(data.substring(30, pos));
//			log.info("Bitmap2:"+bitmap2 + " hexa: "+data.substring(30,  pos));
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
		}
		if (bitmap.charAt(AUTORIZATION_NUMBER - 1) == '1') {
			String autNbr = translateHexaToUtf8(data.substring(pos, pos + 2)) + translateHexaToUtf8(data.substring(pos + 2, pos + 4)) + translateHexaToUtf8(data.substring(pos + 4, pos + 6)) + translateHexaToUtf8(data.substring(pos + 6, pos + 8)) + translateHexaToUtf8(data.substring(pos + 8, pos + 10)) + translateHexaToUtf8(data.substring(pos + 10, pos + 12));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("AN="+autNbr);
			pos = pos + 12;
		}
		if (bitmap.charAt(RESPONSE_CODE - 1) == '1') {
			String responseCode = translateHexaToUtf8(data.substring(pos, pos + 2)) + translateHexaToUtf8(data.substring(pos + 2, pos + 4));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("RC="+responseCode);
			pos = pos + 4;
		}
		if (bitmap.charAt(TERMINAL_ID - 1) == '1') {
			String terminal = data.substring(pos, pos + 16);
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("TI="+translateHexaStringToUtf8(terminal));
			pos = pos + 16;
		}
		if (bitmap.charAt(MERCHANT_ID - 1) == '1') {
			String merchant = data.substring(pos, pos + 30);
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("MI="+translateHexaStringToUtf8(merchant));
			pos = pos + 30;
		}
		if (bitmap.charAt(TRACK_1 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2)).intValue();
			String track1 = data.substring(pos+2, pos+2 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("T1D="+translateHexaStringToUtf8(track1));
			pos = pos + 2 + (cantAleer * 2);
		}
		if (bitmap.charAt(NUMERO_LOTE_ACTIVO - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String lote = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("NLA="+translateHexaStringToUtf8(lote));
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("AD="+translateHexaStringToUtf8(addData));
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA_2 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("AD2="+translateHexaStringToUtf8(addData));
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA_3 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("AD3="+translateHexaStringToUtf8(addData));
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(RESERVED_FOR_NATIONAL_USE - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("RFNU="+translateHexaStringToUtf8(addData));
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA_4 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("AD4="+translateHexaStringToUtf8(addData));
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA_5 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("AD5="+translateHexaStringToUtf8(addData));
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(RESERVED_FOR_PRIVATE_USE - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("RFPU="+translateHexaStringToUtf8(addData));
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(RESERVED - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsAgentConstants.Communication.FRAME_SEP).append("RV="+translateHexaStringToUtf8(addData));
			pos = pos + 4 + (cantAleer * 2);
		}

		return msg.toString();
	}
	
	private String getFechaActual() {
		return Util.convertDateToString(new Date(), "MMdd");
	}
	
	private String getConsData(String data) {
		StringBuffer bitmap = translateHexaToBitmap(data.substring(14, 30));
		String lineaAregistrar = "";
		int pos = 30;
		if (bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') {
			pos += 16;
			bitmap.append(translateHexaToBitmap(data.substring(30, pos)));
		}
		if (bitmap.charAt(PRIMARY_ACCOUNT_NUMBER - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2)).intValue();
			if(cantAleer % 2 == 1)
				cantAleer++;
			pos = pos + 2 + cantAleer;
		}
		if (bitmap.charAt(PROCESSING_CODE - 1) == '1') {
			pos = pos + 6;
		}
		if (bitmap.charAt(TRANSACTION_AMOUNT - 1) == '1') {
			String amount = data.substring(pos, pos + 12);
			lineaAregistrar = amount;
			pos = pos + 12;
		}
		if (bitmap.charAt(SYSTEM_TRACE_AUDIT_NUMBER - 1) == '1') {
			pos = pos + 6;
		}
		if (bitmap.charAt(LOCAL_TRANSACTION_TIME - 1) == '1') {
			pos = pos + 6;
		}
		if (bitmap.charAt(LOCAL_TRANSACTION_DATE - 1) == '1') {
			pos = pos + 4;
		}
		if (bitmap.charAt(EXPIRATION_DATE - 1) == '1') {
			pos = pos + 4;
		}
		if (bitmap.charAt(POS_ENTRY_MODE - 1) == '1') {
			pos = pos + 4;
		}
		if (bitmap.charAt(NETWORK_INTERNATIONAL_ID - 1) == '1') {
			pos = pos + 4;
		}
		if (bitmap.charAt(POS_CONDITION_CODE - 1) == '1') {
			pos = pos + 2;
		}
		if (bitmap.charAt(TRACK_2_DATA - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2)).intValue();
			pos = pos + 2 + (cantAleer % 2 == 0 ? cantAleer : cantAleer + 1);
		}
		if (bitmap.charAt(RETRIEVAL_REFERENCE_NUMBER - 1) == '1') {
			String reference = data.substring(pos, pos + 24);
			pos = pos + 24;
//			log.info("reference: "+reference);
		}
		if (bitmap.charAt(AUTORIZATION_NUMBER - 1) == '1') {
			String autNbr = translateHexaToUtf8(data.substring(pos, pos + 2)) + translateHexaToUtf8(data.substring(pos + 2, pos + 4)) + translateHexaToUtf8(data.substring(pos + 4, pos + 6)) + translateHexaToUtf8(data.substring(pos + 6, pos + 8)) + translateHexaToUtf8(data.substring(pos + 8, pos + 10)) + translateHexaToUtf8(data.substring(pos + 10, pos + 12));
			lineaAregistrar = lineaAregistrar + autNbr;
			pos = pos + 12;
		}
		if (bitmap.charAt(RESPONSE_CODE - 1) == '1') {
			String responseCode = translateHexaToUtf8(data.substring(pos, pos + 2)) + translateHexaToUtf8(data.substring(pos + 2, pos + 4));
			log.info("Response Code: " + responseCode);
			pos = pos + 4;
		}
		if (bitmap.charAt(TERMINAL_ID - 1) == '1') {
			String terminal = data.substring(pos, pos + 16);
//			log.info("terminal: "+terminal);
			pos = pos + 16;
		}
		if (bitmap.charAt(MERCHANT_ID - 1) == '1') {
			String merchant = data.substring(pos, pos + 30);
//			log.info("merchant: "+merchant);
			pos = pos + 30;
		}
		if (bitmap.charAt(TRACK_1 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2)).intValue();
			String track1 = data.substring(pos+2, pos+2 +((cantAleer * 2)));
//			log.info("track1: "+track1);
			pos = pos + 2 + (cantAleer * 2);
		}
		if (bitmap.charAt(NUMERO_LOTE_ACTIVO - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String lote = data.substring(pos+4, pos+4 +((cantAleer * 2)));
//			log.info("lote: "+lote);
			pos = pos + 4 + (cantAleer * 2);
		}
		if (bitmap.charAt(ADICIONAL_DATA - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
//			log.info("addData1: "+addData);
			pos = pos + 4 + (cantAleer * 2);
		}
		if (bitmap.charAt(ADICIONAL_DATA_2 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
//			log.info("addData2: "+addData);
			pos = pos + 4 + (cantAleer * 2);
		}
		if (bitmap.charAt(ADICIONAL_DATA_3 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
//			log.info("addData3: "+addData);
			pos = pos + 4 + (cantAleer * 2);
		}
		if (bitmap.charAt(RESERVED_FOR_NATIONAL_USE - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
//			log.info("res nat: "+addData);
			pos = pos + 4 + (cantAleer * 2);
		}
		if (bitmap.charAt(ADICIONAL_DATA_4 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
//			log.info("addData4: "+addData);
			pos = pos + 4 + (cantAleer * 2);
		}
		if (bitmap.charAt(ADICIONAL_DATA_5 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
//			log.info("addData5: "+addData);
			pos = pos + 4 + (cantAleer * 2);
		}
		if (bitmap.charAt(RESERVED_FOR_PRIVATE_USE - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
//			log.info("res pri: "+addData);
			String facturaHexa = data.substring(pos + 26, pos + 56);
			String factura = "";
			for(int i = 0; i < 29; i=i+2){
				factura = factura + translateHexaToUtf8(facturaHexa.substring(i, i + 2));
			}
			String montoHexa = data.substring(pos + 76, pos + 94);
			String monto = "";
			for(int i = 0; i < 17; i=i+2){
				monto = monto + translateHexaToUtf8(montoHexa.substring(i, i + 2));
			}
			lineaAregistrar = lineaAregistrar + factura + monto;
			pos = pos + 4 + (cantAleer * 2);
//			log.info("Factura:"+factura);
//			log.info("Monto:"+monto);
		}
		if (bitmap.charAt(RESERVED - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
//			log.info("res: "+addData);
			pos = pos + 4 + (cantAleer * 2);
		}

		return lineaAregistrar;
	}
	
	private String generarLineaARegistrar(String data) {
		StringBuffer bitmap = translateHexaToBitmap(data.substring(14, 30));
		String lineaAregistrar = "";
		int pos = 30;
		if (bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') {
			pos += 16;
			bitmap.append(translateHexaToBitmap(data.substring(30, pos)));
		}
		if (bitmap.charAt(PRIMARY_ACCOUNT_NUMBER - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2)).intValue();
			if(cantAleer % 2 == 1)
				cantAleer++;
			String primaryAccNbr = data.substring(pos + 2, pos + 2 + cantAleer);
			lineaAregistrar = primaryAccNbr;
			pos = pos + 2 + cantAleer;
		}
		if (bitmap.charAt(PROCESSING_CODE - 1) == '1') {
			pos = pos + 6;
		}
		if (bitmap.charAt(TRANSACTION_AMOUNT - 1) == '1') {
			pos = pos + 12;
		}
		if (bitmap.charAt(SYSTEM_TRACE_AUDIT_NUMBER - 1) == '1') {
			String sysTraceAudNbr = data.substring(pos, pos + 6);
			lineaAregistrar = lineaAregistrar + sysTraceAudNbr;
			pos = pos + 6;
		}
		if (bitmap.charAt(LOCAL_TRANSACTION_TIME - 1) == '1') {
			pos = pos + 6;
		}
		if (bitmap.charAt(LOCAL_TRANSACTION_DATE - 1) == '1') {
			pos = pos + 4;
		}
		if (bitmap.charAt(EXPIRATION_DATE - 1) == '1') {
			pos = pos + 4;
		}
		if (bitmap.charAt(POS_ENTRY_MODE - 1) == '1') {
			pos = pos + 4;
		}
		if (bitmap.charAt(NETWORK_INTERNATIONAL_ID - 1) == '1') {
			pos = pos + 4;
		}
		if (bitmap.charAt(POS_CONDITION_CODE - 1) == '1') {
			pos = pos + 2;
		}
		if (bitmap.charAt(TRACK_2_DATA - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2)).intValue();
			pos = pos + 2 + (cantAleer % 2 == 0 ? cantAleer : cantAleer + 1);
		}
		if (bitmap.charAt(RETRIEVAL_REFERENCE_NUMBER - 1) == '1') {
			pos = pos + 24;
		}
		if (bitmap.charAt(AUTORIZATION_NUMBER - 1) == '1') {
			String autNbr = translateHexaToUtf8(data.substring(pos, pos + 2)) + translateHexaToUtf8(data.substring(pos + 2, pos + 4)) + translateHexaToUtf8(data.substring(pos + 4, pos + 6)) + translateHexaToUtf8(data.substring(pos + 6, pos + 8)) + translateHexaToUtf8(data.substring(pos + 8, pos + 10)) + translateHexaToUtf8(data.substring(pos + 10, pos + 12));
			lineaAregistrar = lineaAregistrar + autNbr;
			pos = pos + 12;
		} else {
			lineaAregistrar = lineaAregistrar + "      ";
			
		}
		if (bitmap.charAt(RESPONSE_CODE - 1) == '1') {
			String responseCode = translateHexaToUtf8(data.substring(pos, pos + 2)) + translateHexaToUtf8(data.substring(pos + 2, pos + 4));
			lineaAregistrar = lineaAregistrar + responseCode;
			log.info("Response Code: " + responseCode);
			pos = pos + 4;
		} else {
			lineaAregistrar = lineaAregistrar + "99";
		}
		if (bitmap.charAt(TERMINAL_ID - 1) == '1') {
			pos = pos + 16;
		}
		if (bitmap.charAt(MERCHANT_ID - 1) == '1') {
			pos = pos + 30;
		}
		if (bitmap.charAt(TRACK_1 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2)).intValue();
			pos = pos + 2 + (cantAleer * 2);
		}
		if (bitmap.charAt(NUMERO_LOTE_ACTIVO - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA_2 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA_3 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(RESERVED_FOR_NATIONAL_USE - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA_4 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA_5 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(RESERVED_FOR_PRIVATE_USE - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String facturaHexa = data.substring(pos + 22, pos + 52);
			String factura = "";
			for(int i = 0; i < 29; i=i+2){
				factura = factura + translateHexaToUtf8(facturaHexa.substring(i, i + 2));
			}
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(RESERVED - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			pos = pos + 4 + (cantAleer * 2);
		}
		
		return lineaAregistrar;
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
}
