/**
 * 
 */
package com.allc.arms.server.processes.cer.giftcard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.devolucion.DevolucionDAO;
import com.allc.arms.server.persistence.devolucion.Giftcard;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;

/**
 * @author gustavo
 *
 */
public class ActivateGiftcardProcess extends AbstractProcess {
	private static Logger log = Logger.getLogger(ActivateGiftcardProcess.class);
	private File inFolder;
	private File outFolder;
	private int sleepTime;
	private Iterator filesToProcess = null;
	protected boolean finished = false;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	public boolean isEnd = false;
	private SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
	private Session session = null;
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
	public final int RESERVED_FOR_PRIVATE_USE = 124;

	protected void inicializar() {
		isEnd = false;
		try {
			inFolder = new File(properties.getObject("activateGiftcard.in.folder.path"));
			outFolder = new File(properties.getObject("activateGiftcard.out.folder.path"));
			sleepTime = properties.getInt("activateGiftcard.sleeptime");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void run() {
		log.info("Iniciando ActivateGiftcardProcess...");
		inicializar();
		String filename = null;
		String storeCode = properties.getObject("eyes.store.code");
		while (!isEnd) {
			try {
				File giftFile = getNextGiftFile();

				if (giftFile != null) {
					filename = giftFile.getName().toUpperCase();
					String[] parts = filename.split("\\.");
					Integer store = new Integer(parts[1]);
					log.info("Archivo a procesar: " + filename + " STORE: " + store);
					UtilityFile.createWriteDataFile(getEyesFileName(), "ACT_GFC_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo a Procesar: "+filename+".\n", true);
					if (activarGift(giftFile)) {
						giftFile.renameTo(new File(outFolder, giftFile.getName()));
						log.info("Archivo movido a la carpeta Out.");
						UtilityFile.createWriteDataFile(getEyesFileName(), "ACT_GFC_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo: "+filename+" procesado correctamente.\n", true);
					} else {
						UtilityFile.createWriteDataFile(getEyesFileName(), "ACT_GFC_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el Archivo: "+filename+".\n", true);
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							log.error(e.getMessage(), e);
						}
					}
				}
			} catch (Exception e) {
				UtilityFile.createWriteDataFile(getEyesFileName(), "ACT_GFC_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el Archivo: "+filename+".\n", true);
				log.error(e.getMessage(), e);
			}
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
		finished = true;
	}
	
	private String getEyesFileName(){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	private void iniciarSesion() {
		while (session == null && !isEnd) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory("Devs").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (session == null)
				try {
					log.error("OCURRIÓ UN ERROR AL CREAR LA CONEXIÓN A LA BD, SE REINTENTARÁ EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	private void cerrarSesion() {
		try {
			session.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		session = null;
	}

	private boolean activarGift(File giftFile) {
		boolean retorno = true;
		iniciarSesion();
		DevolucionDAO devolucionDAO = new DevolucionDAO();
		StringBuffer mje = new StringBuffer("");
		String header = "0000000000";
		String type = "0200";
		StringBuffer bitmap = new StringBuffer(StringUtils.leftPad("", 128, ArmsServerConstants.Communication.CERO));
		bitmap.setCharAt(BITMAP_2_ACTIVE - 1, '1');
		bitmap.setCharAt(PRIMARY_ACCOUNT_NUMBER - 1, '1');
		bitmap.setCharAt(PROCESSING_CODE - 1, '1');
		bitmap.setCharAt(TRANSACTION_AMOUNT - 1, '1');
		bitmap.setCharAt(SYSTEM_TRACE_AUDIT_NUMBER - 1, '1');
		bitmap.setCharAt(LOCAL_TRANSACTION_TIME - 1, '1');
		bitmap.setCharAt(LOCAL_TRANSACTION_DATE - 1, '1');
		bitmap.setCharAt(EXPIRATION_DATE - 1, '1');
		bitmap.setCharAt(POS_ENTRY_MODE - 1, '1');
		bitmap.setCharAt(NETWORK_INTERNATIONAL_ID - 1, '1');
		bitmap.setCharAt(POS_CONDITION_CODE - 1, '1');
		bitmap.setCharAt(TRACK_2_DATA - 1, '1');
		bitmap.setCharAt(TERMINAL_ID - 1, '1');
		bitmap.setCharAt(MERCHANT_ID - 1, '1');
		bitmap.setCharAt(TRACK_1 - 1, '1');
		bitmap.setCharAt(NUMERO_LOTE_ACTIVO - 1, '1');
		bitmap.setCharAt(ADICIONAL_DATA_3 - 1, '1');
		bitmap.setCharAt(RESERVED_FOR_NATIONAL_USE - 1, '1');
		bitmap.setCharAt(RESERVED_FOR_PRIVATE_USE - 1, '1');
		try {
			FileReader fr = new FileReader(giftFile);
			BufferedReader br = new BufferedReader(fr);
			int largoConvertir = 0;
			String linea = br.readLine();
			if (linea != null) {
				log.debug("Línea leida: " + linea);
				String lineParts[] = linea.split("\\|");
				String primaryAcctNbr = generarNvariable(lineParts[1].substring(0, 16), 16, 2);//
				String processingCode = "914000";//
				String transAmount = generarNfijo(lineParts[2].substring(0, 12), 12);//
				String systemTANbr = generarNfijo(lineParts[2].substring(12, 18), 6);//
				String fecha = sdf.format(new Date());
				String localTrxTime = generarNfijo(fecha.substring(4, 10), 6);//
				String localTrxDate = generarNfijo(fecha.substring(0, 4), 4);//
				String expirationDate = generarNfijo(lineParts[2].substring(18, 22), 4);//
				String posEntryMode = "0220";//
				String networkIntID = "0001";//
				String posCondCode = "00";//
				String track2 = generarZvariable(lineParts[1], 37, 2) + "0";//
				String terminalID = generarAns(lineParts[2].substring(22, 30).trim(), 8);
				String merchantID = generarAns(lineParts[2].substring(33, 45) + "   ", 15);
				String track1 = generarAnsvariable(lineParts[0], 76, 2);
				String nroLoteActivo = generarAnsvariable("000001", 6, 4);
				String addData = generarAnsvariable("01", 2, 4);
				String resNatUse = generarAnsvariable("00010001", 8, 4);
				String resPrivUse = generarAnsvariable("P0000000027027-00000000000          000000000000000000", 54, 4);
				mje.append(header);
				mje.append(type);
				mje.append(translateBitmapToHexa(bitmap.toString()));
				mje.append(primaryAcctNbr);
				mje.append(processingCode);
				mje.append(transAmount);
				mje.append(systemTANbr);
				mje.append(localTrxTime);
				mje.append(localTrxDate);
				mje.append(expirationDate);
				mje.append(posEntryMode);
				mje.append(networkIntID);
				mje.append(posCondCode);
				mje.append(track2);
				largoConvertir = mje.length();
				mje.append(terminalID);
				mje.append(merchantID);
				mje.append(track1);
				mje.append(nroLoteActivo);
				mje.append(addData);
				mje.append(resNatUse);
				mje.append(resPrivUse);
			}
			br.close();
			fr.close();
			ConnSocketClient socketClient = new ConnSocketClient();
			socketClient.setIpServer(properties.getObject("activateGiftcard.server.ip"));
			socketClient.setPortServer(properties.getInt("activateGiftcard.server.port"));
			socketClient.setRetries(properties.getInt("activateGiftcard.server.retries"));
			socketClient.setTimeOutConnection(properties.getInt("activateGiftcard.server.timeOut"));
			socketClient.setTimeOutSleep(properties.getInt("activateGiftcard.server.timeSleep"));
			socketClient.setQuantityBytesLength(2);

			String mjeLength = StringUtils.leftPad(Integer.toHexString(mje.length() / 2), 4, ArmsServerConstants.Communication.CERO);
			mje.insert(0, mjeLength);
			largoConvertir += 4;
			log.info("Mensaje a Enviar: " + mje.toString().toUpperCase());
			File file = new File("C:/ALLC/ArmsServer/GF2.DAT");
    		if(!file.exists())
    			file.createNewFile();
    		FileOutputStream fos = new FileOutputStream(file, true);
    		byte [] a = strNumToByteAry(mje.toString());
    		fos.write(a, 0, a.length);
//    		a = translateToHexa(mje.toString().substring(largoConvertir)).getBytes();
//    		fos.write(a, 0, a.length);
    		fos.close();
			String data = "";
			int cant;
			try {
				if (socketClient.connectSocketUsingRetries()) {
					log.info("Socket conectado");
					if (socketClient.writeByteArraySocket(strNumToByteAry(mje.toString()))) {
						log.info("Mensaje enviado");
						if (!socketClient.timeOutSocket()) {
							log.info("Leyendo respuesta");
							cant = socketClient.leeLongitudDataHexaSocket();
							log.info("Cantidad a leer(dec):" + cant);
							data = socketClient.readDataSocket(cant);
							log.info("Respuesta recibida:" + data);
						}
					}
					log.info("Cerrando conexión");
					socketClient.closeConnection();
					;
				} else {
					log.info("Socket NO conectado");
				}
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}

			String filename = giftFile.getName().toUpperCase();
			String[] parts = filename.split("\\.");
			Giftcard giftcard = new Giftcard();
			giftcard.setId(new Integer(parts[0].substring(3)));
			if (data != null & !data.trim().isEmpty()) {
				boolean afirmativo = respuestaAfirmativa(translateResponse(data));
				if (afirmativo)
					giftcard.setEstado(1);
				else
					giftcard.setEstado(2);
			} else
				giftcard.setEstado(3);
			devolucionDAO.insertGiftcard(session, giftcard);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			retorno = false;
		}
		cerrarSesion();
		return retorno;
	}

	private static String translateToHexa(String cadena) {
		String hexa = "";
		for (int i = 0; i < cadena.length(); i = i + 2) {
			int decimal = Integer.parseInt(cadena.substring(i,  i+2),16);
			String hexStr = Integer.toString(decimal, 16);
			hexa = hexa + (hexStr.length() < 2 ? "0" + hexStr : hexStr);
		}
		return hexa;
	}
	
	private String translateResponse(String cadena) {
		byte[] bytes = cadena.getBytes();
		StringBuffer retorno = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String hexa = Integer.toString((bytes[i] << 0) & 0x000000ff, 16);
			retorno.append(hexa.length() < 2 ? "0" + hexa : hexa);
		}
//		log.info("Respuesta: " + retorno.toString());
		return retorno.toString();
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
		
	public String strToHexa(String str) {
		String result = null;
		byte[] s = str.getBytes();
		for(int i=0; i< s.length;i++){
			int decimal = (int) s[i];
			result = result + Integer.toString(decimal, 16);
		}
		return result;
	}
	
	

	private String translateBitmapToHexa(String bitmap) {
		String hexa = "";
		for (int i = 0; i < bitmap.length(); i = i + 8) {
			int decimal = Integer.parseInt(bitmap.substring(i, i + 8), 2);
			String hexStr = Integer.toString(decimal, 16);
			hexa = hexa + (hexStr.length() < 2 ? "0" + hexStr : hexStr);
		}
		return hexa;
	}

	private StringBuffer translateHexaToBitmap(String hexa) {
		StringBuffer bitmap = new StringBuffer();
		for (int i = 0; i < hexa.length(); i = i + 2) {
			int decimal = Integer.parseInt(hexa.substring(i, i + 2), 16);
			String bitStr = Integer.toString(decimal, 2);
			bitmap.append(StringUtils.leftPad(bitStr, 8, ArmsServerConstants.Communication.CERO));
		}
		return bitmap;
	}

	/** Genera un campo del tipo N ..X */
	private String generarNvariable(String cadena, int max, int sizeLength) {
		if (cadena != null) {
			if (cadena.length() > max) {
				cadena = cadena.substring(0, max);
			}
			String size = Integer.valueOf(cadena.length()).toString();
			for (int i = size.length(); i < sizeLength; i++)
				size = "0" + size;
			cadena = size + cadena;
		}
		return cadena;
	}

	/** Genera un campo del tipo N X */
	private String generarNfijo(String cadena, int max) {
		if (cadena != null) {
			if (cadena.length() > max) {
				return cadena.substring(0, max);
			}
			for (int i = cadena.length(); i < max; i++)
				cadena = "0" + cadena;
		}
		return cadena;
	}

	/** Genera un campo del tipo Z ..X */
	private String generarZvariable(String cadena, int max, int sizeLength) {
		if (cadena != null) {
			if (cadena.length() > max) {
				cadena = cadena.substring(0, max);
			}
			String size = Integer.valueOf(cadena.length()).toString();
			for (int i = size.length(); i < sizeLength; i++)
				size = "0" + size;
			cadena = size + cadena;
		}
		return cadena;
	}

	/** Genera un campo del tipo Ans X */
	private String generarAns(String cadena, int max) {
		if (cadena != null) {
			if (cadena.length() > max) {
				cadena = cadena.substring(0, max);
			}
			for (int i = cadena.length(); i < max; i++)
				cadena = "0" + cadena;
			cadena = DatatypeConverter.printHexBinary(cadena.getBytes());
		}
		return cadena;
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

	private boolean respuestaAfirmativa(String data) {
		StringBuffer bitmap = translateHexaToBitmap(data.substring(14, 30));
		int pos = 30;
		if (bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') {
			pos += 16;
			bitmap.append(translateHexaToBitmap(data.substring(30, pos)));
		}
		if (bitmap.charAt(PRIMARY_ACCOUNT_NUMBER - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2));
			pos = pos + 2 + cantAleer;
		}
		if (bitmap.charAt(PROCESSING_CODE - 1) == '1') {
			pos = pos + 6;
		}
		if (bitmap.charAt(TRANSACTION_AMOUNT - 1) == '1') {
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
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2));
			pos = pos + 2 + (cantAleer % 2 == 0 ? cantAleer : cantAleer + 1);
		}
		if (bitmap.charAt(RETRIEVAL_REFERENCE_NUMBER - 1) == '1') {
			pos = pos + 24;
		}
		if (bitmap.charAt(AUTORIZATION_NUMBER - 1) == '1') {
			pos = pos + 12;
		}
		if (bitmap.charAt(RESPONSE_CODE - 1) == '1') {
			String responseCode = translateHexaToUtf8(data.substring(pos, pos + 2)) + translateHexaToUtf8(data.substring(pos + 2, pos + 4));
			log.info("Response Code: " + responseCode);
			if (responseCode != null && (responseCode.equals("00") || responseCode.equals("08") || responseCode.equals("11")))
				return true;
		}

		return false;
	}

	/** Genera un campo del tipo Ans ..X */
	private String generarAnsvariable(String cadena, int max, int sizeLength) {
		if (cadena != null) {
			if (cadena.length() > max) {
				cadena = cadena.substring(0, max);
			}
			cadena = DatatypeConverter.printHexBinary(cadena.getBytes());
			String size = Integer.valueOf(cadena.length()/2).toString();
			for (int i = size.length(); i < sizeLength; i++)
				size = "0" + size;
			cadena = size + cadena;
		}
		return cadena;
	}

	private File getNextGiftFile() {
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd)
					return null;
				File[] files = inFolder.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isFile() && (pathname.getName().toUpperCase().startsWith("GFC"));
					}
				});
				if (files.length == 0) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						log.error(e.getMessage(), e);
					}
				} else {
					Arrays.sort(files, new Comparator() {
						public int compare(Object obj1, Object obj2) {
							String name1 = ((File) obj1).getName().toUpperCase();
							int sequence1 = Integer.parseInt(name1.substring(4, 8));
							String name2 = ((File) obj2).getName().toUpperCase();
							int sequence2 = Integer.parseInt(name2.substring(4, 8));
							if (sequence1 == sequence2) {
								return 0;
							}
							if (sequence1 < sequence2) {
								return -1;
							}
							return 1;
						}
					});
					this.filesToProcess = Arrays.asList(files).iterator();
				}
			} while (((this.filesToProcess == null) || !filesToProcess.hasNext()));
		}
		return (File) this.filesToProcess.next();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.allc.core.process.AbstractProcess#shutdown(long)
	 */
	@Override
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo ActivateGiftcardProcess...");
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
		log.info("Finalizó el Proceso de Búsqueda de Archivos Giftcard.");
		return true;
	}

}
