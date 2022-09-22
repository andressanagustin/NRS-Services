/**
 * 
 */
package com.allc.arms.server.operations.cer.giftcard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
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
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.Files;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * @author gustavo
 *
 */
public class ActivateGiftcardOperation extends AbstractOperation {
	private Logger log = Logger.getLogger(ActivateGiftcardOperation.class);
	private File inFolder;
	private File outFolder;
	private int sleepTime;
	private Iterator filesToProcess = null;
	protected boolean finished = false;
	private String responseCode;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.allc.core.operation.AbstractOperation#shutdown(long)
	 */
	@Override
	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.allc.core.operation.AbstractOperation#process(com.allc.comm.socket.
	 * ConnSocketServer, com.allc.comm.frame.Frame,
	 * com.allc.properties.PropFile)
	 */
	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		try {
			StringBuffer message = new StringBuffer();
			inicializar();
			String filename = (String) frame.getBody().get(0);
			File giftFile = new File(inFolder, filename);

			if (filename != null && giftFile.exists()) {
				filename = giftFile.getName().toUpperCase();
				String[] parts = filename.split("\\.");
				Integer store = new Integer(parts[1]);
				log.info("Archivo a procesar: " + filename + " STORE: " + store);
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "ACT_GFC_O|"+properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
						+ frame.getHeader().get(3) + "|STR|"
						+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo a Procesar: "+filename+".\n", true);
				if (activarGift(giftFile)) {
					giftFile.renameTo(new File(outFolder, giftFile.getName()));
					log.info("Archivo movido a la carpeta Out.");
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "ACT_GFC_O|"+properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo: "+filename+" procesado correctamente.\n", true);
				} else {
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "ACT_GFC_O|"+properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el Archivo: "+filename+".\n", true);
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						log.error(e.getMessage(), e);
					}
				}
				message.append("0");
			} else 
				message.append("1");
			
//			String sb = Util.addLengthStartOfString(
//					frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + message.toString(),
//					properties.getInt("serverSocket.quantityBytesLength"));
//			log.info("Respuesta a enviar: " + sb);
//			socket.writeDataSocket(sb);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"ACT_GFC_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al activar la Giftcard: " + frame.getBody().get(0) + ".\n",
						true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
		return false;
	}

	private String getEyesFileName(PropFile properties) {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
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
		String linea = null;
		try {
			String filename = giftFile.getName().toUpperCase();
			String[] parts = filename.split("\\.");
			FileReader fr = new FileReader(giftFile);
			BufferedReader br = new BufferedReader(fr);
			linea = br.readLine();
			String cardNumber = "";
			if (linea != null) {
				StringBuffer msg = new StringBuffer("MT="+type);
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("BM1="+bitmap.substring(0, 64));
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("BM2="+bitmap.substring(64,128));
				log.debug("Línea leida: " + linea);
				String lineParts[] = linea.split("\\|");
				cardNumber = lineParts[1].substring(0, 16);
				Giftcard existGiftcard = devolucionDAO.getGiftcardByCardNumber(session, cardNumber);
				if(existGiftcard != null) {
					log.info("Giftcard preexistente en BD, Estado: "+ existGiftcard.getEstado());
					if(existGiftcard.getEstado().intValue() == 0 && existGiftcard.getResponseCode() != null){
						log.info("Giftcard con Response Code: "+ existGiftcard.getResponseCode());
					}
					if(existGiftcard.getEstado().intValue() == 0 && (existGiftcard.getResponseCode() != null && (existGiftcard.getResponseCode().equals("00") || existGiftcard.getResponseCode().equals("08") || existGiftcard.getResponseCode().equals("11")))){
						log.info("Giftcard no se enviará a Syscard porque ya se realizó envío existoso previo");
						existGiftcard.setEstado(1);
						devolucionDAO.insertGiftcard(session, existGiftcard);
						cerrarSesion();
						return false;
					}
					if(existGiftcard.getEstado() == 1 || existGiftcard.getEstado() == 4){
						log.info("Giftcard no se enviará a Syscard porque ya se realizó envío existoso previo");
						cerrarSesion();
						return false;
					}
				} else {
					log.info("Giftcard inexistente en BD, Validando si existe ID registrado.");
					Giftcard giftcard = devolucionDAO.getGiftcard(session, new Integer(parts[0].substring(3)));
					if(giftcard == null){
						log.info("Giftcard inexistente en BD, Se registrará solo ID.");
						giftcard = new Giftcard();
						giftcard.setId(new Integer(parts[0].substring(3)));
						devolucionDAO.insertGiftcard(session, giftcard);
					} else {
						if(giftcard.getCardNumber() != null && !giftcard.getCardNumber().isEmpty()){
							log.info("Giftcard no se enviará a Syscard porque ya existe ID (Devolución iniciada).");
							cerrarSesion();
							return false;
						} else {
							log.info("Giftcard en proceso..");							
						}
					}
				}
					
				String primaryAcctNbr = generarNvariable(lineParts[1].substring(0, 16), 16, 2);//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PAC="+lineParts[1].substring(0, 16));
				String processingCode = "914000";//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PC="+processingCode);
				String transAmount = generarNfijo(lineParts[2].substring(0, 12), 12);//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AM="+lineParts[2].substring(0, 12));
				String systemTANbr = generarNfijo(lineParts[2].substring(12, 18), 6);//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("STAN="+lineParts[2].substring(12, 18));
				String fecha = sdf.format(new Date());
				String localTrxTime = generarNfijo(fecha.substring(4, 10), 6);//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("LTT="+fecha.substring(4, 10));
				String localTrxDate = generarNfijo(fecha.substring(0, 4), 4);//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("LTD="+fecha.substring(0, 4));
				String expirationDate = generarNfijo(lineParts[2].substring(18, 22), 4);//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("ED="+lineParts[2].substring(18, 22));
				String posEntryMode = "0220";//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PEM="+posEntryMode);
				String networkIntID = "0001";//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("NII="+networkIntID);
				String posCondCode = "00";//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PCC="+posCondCode);
				String track2 = generarZvariable(lineParts[1], 37, 2) + "0";//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("T2D="+track2.substring(2));
				String terminalID = generarAns(lineParts[2].substring(22, 30).trim(), 8);
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("TI="+lineParts[2].substring(22, 30).trim());
				String merchantID = generarAns(lineParts[2].substring(33, 45) + "   ", 15);
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("MI="+lineParts[2].substring(33, 45) + "   ");
				String track1 = generarAnsvariable(lineParts[0], 76, 2);
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("T1D="+lineParts[0]);
				String nroLoteActivo = generarAnsvariable("000001", 6, 4);
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("NLA="+"000001");
				String addData = generarAnsvariable("01", 2, 4);
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AD3="+"01");
				String resNatUse = generarAnsvariable("00010001", 8, 4);
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("RFNU="+"00010001");
				String resPrivUse = generarAnsvariable("P0000000027027-00000000000          000000000000000000", 54, 4);
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("RFPU="+"P0000000027027-00000000000          000000000000000000");
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
				mje.append(terminalID);
				mje.append(merchantID);
				mje.append(track1);
				mje.append(nroLoteActivo);
				mje.append(addData);
				mje.append(resNatUse);
				mje.append(resPrivUse);
				
				writeTramaReq(msg.toString());
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
			log.info("Mensaje a Enviar: " + mje.toString().toUpperCase());
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
				} else {
					log.info("Socket NO conectado");
				}
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}

			
			Giftcard giftcard = devolucionDAO.getGiftcard(session, new Integer(parts[0].substring(3)));
			if(giftcard == null){
				giftcard = new Giftcard();
				giftcard.setId(new Integer(parts[0].substring(3)));
			}
			boolean afirmativo = false;
			if (data != null & !data.trim().isEmpty()) {
				String response = translateResponse(data);
				writeTramaResp(response);
				afirmativo = respuestaAfirmativa(response, giftcard);
				if (afirmativo)
					giftcard.setEstado(1);
				else
					giftcard.setEstado(2);
				giftcard.setResponseCode(responseCode);
			} else
				giftcard.setEstado(3);
			//4=procesada, 5=reversada, 6=reversa rechazada
			devolucionDAO.insertGiftcard(session, giftcard);
			cerrarSesion();
			if(afirmativo || giftcard.getEstado().intValue() == 3){
				log.info("Esperando 30 segundos...");
				Thread.sleep(properties.getInt("activateGiftcard.server.reversaTimeOut"));
				iniciarSesion();
				log.info("Revisando estado de Giftcard");
				giftcard = devolucionDAO.getGiftcard(session, new Integer(parts[0].substring(3)));
				log.info("Estado leido:"+giftcard.getEstado().intValue());
				if(giftcard.getEstado().intValue() == 1 || giftcard.getEstado().intValue() == 3){
					log.info("Giftcard no procesada, se procederá a reversar.");
					if(reversar(linea)){
						giftcard.setEstado(5);
						log.info("Reversa correcta");
					} else {
						giftcard.setEstado(6);	
						log.info("Error al reversar");
					}
					devolucionDAO.insertGiftcard(session, giftcard);
					log.info("Estado de Giftcard actualizado.");
				} else {
					log.info("Giftcard procesada correctamente, no es necesario reversar.");
				}
				cerrarSesion();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			retorno = false;
		}
		
		return retorno;
	}
	
	protected boolean reversar(String linea){
		StringBuffer mje = new StringBuffer("");
		String header = "0000000000";
		String type = "0400";
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
			if (linea != null) {
				StringBuffer msg = new StringBuffer("MT="+type);
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("BM1="+bitmap.substring(0, 64));
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("BM2="+bitmap.substring(64,128));
				log.debug("Línea leida: " + linea);
				String lineParts[] = linea.split("\\|");
				String primaryAcctNbr = generarNvariable(lineParts[1].substring(0, 16), 16, 2);//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PAC="+lineParts[1].substring(0, 16));
				String processingCode = "914000";//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PC="+processingCode);
				String transAmount = generarNfijo(lineParts[2].substring(0, 12), 12);//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AM="+lineParts[2].substring(0, 12));
				String systemTANbr = generarNfijo(lineParts[2].substring(12, 18), 6);//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("STAN="+lineParts[2].substring(12, 18));
				String fecha = sdf.format(new Date());
				String localTrxTime = generarNfijo(fecha.substring(4, 10), 6);//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("LTT="+fecha.substring(4, 10));
				String localTrxDate = generarNfijo(fecha.substring(0, 4), 4);//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("LTD="+fecha.substring(0, 4));
				String expirationDate = generarNfijo(lineParts[2].substring(18, 22), 4);//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("ED="+lineParts[2].substring(18, 22));
				String posEntryMode = "0220";//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PEM="+posEntryMode);
				String networkIntID = "0001";//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("NII="+networkIntID);
				String posCondCode = "00";//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PCC="+posCondCode);
				String track2 = generarZvariable(lineParts[1], 37, 2) + "0";//
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("T2D="+track2.substring(2));
				String terminalID = generarAns(lineParts[2].substring(22, 30).trim(), 8);
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("TI="+lineParts[2].substring(22, 30).trim());
				String merchantID = generarAns(lineParts[2].substring(33, 45) + "   ", 15);
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("MI="+lineParts[2].substring(33, 45) + "   ");
				String track1 = generarAnsvariable(lineParts[0], 76, 2);
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("T1D="+lineParts[0]);
				String nroLoteActivo = generarAnsvariable("000001", 6, 4);
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("NLA="+"000001");
				String addData = generarAnsvariable("01", 2, 4);
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AD3="+"01");
				String resNatUse = generarAnsvariable("00010001", 8, 4);
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("RFNU="+"00010001");
				String resPrivUse = generarAnsvariable("P0000000027027-00000000000          000000000000000000", 54, 4);
				msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("RFPU="+"P0000000027027-00000000000          000000000000000000");
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
				mje.append(terminalID);
				mje.append(merchantID);
				mje.append(track1);
				mje.append(nroLoteActivo);
				mje.append(addData);
				mje.append(resNatUse);
				mje.append(resPrivUse);
				
				writeTramaReq(msg.toString());
			}
			ConnSocketClient socketClient = new ConnSocketClient();
			socketClient.setIpServer(properties.getObject("activateGiftcard.server.ip"));
			socketClient.setPortServer(properties.getInt("activateGiftcard.server.port"));
			socketClient.setRetries(properties.getInt("activateGiftcard.server.retries"));
			socketClient.setTimeOutConnection(properties.getInt("activateGiftcard.server.timeOut"));
			socketClient.setTimeOutSleep(properties.getInt("activateGiftcard.server.timeSleep"));
			socketClient.setQuantityBytesLength(2);

			String mjeLength = StringUtils.leftPad(Integer.toHexString(mje.length() / 2), 4, ArmsServerConstants.Communication.CERO);
			mje.insert(0, mjeLength);
			log.info("Mensaje a Enviar: " + mje.toString().toUpperCase());
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
				} else {
					log.info("Socket NO conectado");
				}
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}

			Giftcard giftcard = new Giftcard();
			if (data != null & !data.trim().isEmpty()) {
				String response = translateResponse(data);
				writeTramaResp(response);
				boolean afirmativo = respuestaAfirmativa(response, giftcard);
				if (afirmativo)
					return true;
				else
					return false;
			} else
				return false;
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}
	

	protected boolean writeTramaReq(String linea) {
		boolean retorno = true;
		try {
			Files.creaEscribeDataArchivo("tramaFromServer"+getFechaActual()+".dat", linea + "\n", true);
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
			Files.creaEscribeDataArchivo("tramaFromServer"+getFechaActual()+".dat", msgData + "\n", true);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			retorno = false;
		}
		return retorno;
	}
	
	private String getFechaActual() {
		return Util.convertDateToString(new Date(), "MMdd");
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
		int pos = 30;
		msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("BM1="+bitmap);
		if (bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') {
			pos += 16;
			StringBuffer bitmap2 = translateHexaToBitmap(data.substring(30, pos));
			log.info("Bitmap2:"+bitmap2 + " hexa: "+data.substring(30, pos));
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("BM2="+bitmap2);
			bitmap.append(bitmap2);
		}
		
		if (bitmap.charAt(PRIMARY_ACCOUNT_NUMBER - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2)).intValue();
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PAC="+data.substring(pos+2, pos+2+cantAleer));
			pos = pos + 2 + cantAleer;
		}
		if (bitmap.charAt(PROCESSING_CODE - 1) == '1') {
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PC="+data.substring(pos, pos+6));
			pos = pos + 6;
		}
		if (bitmap.charAt(TRANSACTION_AMOUNT - 1) == '1') {
			String amount = data.substring(pos, pos + 12);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AM="+amount);
			pos = pos + 12;
		}
		if (bitmap.charAt(SYSTEM_TRACE_AUDIT_NUMBER - 1) == '1') {
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("STAN="+data.substring(pos, pos+6));
			pos = pos + 6;
		}
		if (bitmap.charAt(LOCAL_TRANSACTION_TIME - 1) == '1') {
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("LTT="+data.substring(pos, pos+6));
			pos = pos + 6;
		}
		if (bitmap.charAt(LOCAL_TRANSACTION_DATE - 1) == '1') {
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("LTD="+data.substring(pos, pos+4));
			pos = pos + 4;
		}
		if (bitmap.charAt(EXPIRATION_DATE - 1) == '1') {
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("ED="+data.substring(pos, pos+4));
			pos = pos + 4;
		}
		if (bitmap.charAt(POS_ENTRY_MODE - 1) == '1') {
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PEM="+data.substring(pos, pos+4));
			pos = pos + 4;
		}
		if (bitmap.charAt(NETWORK_INTERNATIONAL_ID - 1) == '1') {
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("NII="+data.substring(pos, pos+4));
			pos = pos + 4;
		}
		if (bitmap.charAt(POS_CONDITION_CODE - 1) == '1') {
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PCC="+data.substring(pos, pos+2));
			pos = pos + 2;
		}
		if (bitmap.charAt(TRACK_2_DATA - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2)).intValue();
			if(cantAleer % 2 == 1)
				cantAleer++;
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("T2D="+data.substring(pos+2, pos+2+cantAleer));
			pos = pos + 2 + (cantAleer % 2 == 0 ? cantAleer : cantAleer + 1);
		}
		if (bitmap.charAt(RETRIEVAL_REFERENCE_NUMBER - 1) == '1') {
			String reference = data.substring(pos, pos + 24);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("RRN="+translateHexaStringToUtf8(data.substring(pos, pos+24)));
			pos = pos + 24;
		}
		if (bitmap.charAt(AUTORIZATION_NUMBER - 1) == '1') {
			String autNbr = translateHexaToUtf8(data.substring(pos, pos + 2)) + translateHexaToUtf8(data.substring(pos + 2, pos + 4)) + translateHexaToUtf8(data.substring(pos + 4, pos + 6)) + translateHexaToUtf8(data.substring(pos + 6, pos + 8)) + translateHexaToUtf8(data.substring(pos + 8, pos + 10)) + translateHexaToUtf8(data.substring(pos + 10, pos + 12));
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AN="+autNbr);
			pos = pos + 12;
		}
		if (bitmap.charAt(RESPONSE_CODE - 1) == '1') {
			String responseCode = translateHexaToUtf8(data.substring(pos, pos + 2)) + translateHexaToUtf8(data.substring(pos + 2, pos + 4));
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("RC="+responseCode);
			pos = pos + 4;
		}
		if (bitmap.charAt(TERMINAL_ID - 1) == '1') {
			String terminal = data.substring(pos, pos + 16);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("TI="+translateHexaStringToUtf8(terminal));
			pos = pos + 16;
		}
		if (bitmap.charAt(MERCHANT_ID - 1) == '1') {
			String merchant = data.substring(pos, pos + 30);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("MI="+translateHexaStringToUtf8(merchant));
			pos = pos + 30;
		}
		if (bitmap.charAt(TRACK_1 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2)).intValue();
			String track1 = data.substring(pos+2, pos+2 +((cantAleer * 2)));
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("T1D="+translateHexaStringToUtf8(track1));
			pos = pos + 2 + (cantAleer * 2);
		}
		if (bitmap.charAt(NUMERO_LOTE_ACTIVO - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String lote = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("NLA="+translateHexaStringToUtf8(lote));
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AD="+translateHexaStringToUtf8(addData));
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA_2 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AD2="+translateHexaStringToUtf8(addData));
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA_3 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AD3="+translateHexaStringToUtf8(addData));
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(RESERVED_FOR_NATIONAL_USE - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("RFNU="+translateHexaStringToUtf8(addData));
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(ADICIONAL_DATA_4 - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AD4="+translateHexaStringToUtf8(addData));
			pos = pos + 4 + (cantAleer * 2);
		}
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(RESERVED_FOR_PRIVATE_USE - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("RFPU="+translateHexaStringToUtf8(addData));
		}

		return msg.toString();
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

	private boolean respuestaAfirmativa(String data, Giftcard giftcard) {
		StringBuffer bitmap = translateHexaToBitmap(data.substring(14, 30));
		int pos = 30;
		if (bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') {
			pos += 16;
			bitmap.append(translateHexaToBitmap(data.substring(30, pos)));
		}
		if (bitmap.charAt(PRIMARY_ACCOUNT_NUMBER - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2));
			giftcard.setCardNumber(data.substring(pos+2, pos+2+cantAleer));
			pos = pos + 2 + cantAleer;
		}
		if (bitmap.charAt(PROCESSING_CODE - 1) == '1') {
			pos = pos + 6;
		}
		if (bitmap.charAt(TRANSACTION_AMOUNT - 1) == '1') {
			giftcard.setAmount(Integer.valueOf(data.substring(pos, pos + 12)));
			pos = pos + 12;
		}
		if (bitmap.charAt(SYSTEM_TRACE_AUDIT_NUMBER - 1) == '1') {
			String sysTraceAudNbr = data.substring(pos, pos + 6);
			giftcard.setReferenceNumber(sysTraceAudNbr);
			pos = pos + 6;
		}
		if (bitmap.charAt(LOCAL_TRANSACTION_TIME - 1) == '1') {
			String localTrxTime = data.substring(pos, pos + 6);
			giftcard.setTime(localTrxTime);
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
			String autNbr = translateHexaToUtf8(data.substring(pos, pos + 2)) + translateHexaToUtf8(data.substring(pos + 2, pos + 4)) + translateHexaToUtf8(data.substring(pos + 4, pos + 6)) + translateHexaToUtf8(data.substring(pos + 6, pos + 8)) + translateHexaToUtf8(data.substring(pos + 8, pos + 10)) + translateHexaToUtf8(data.substring(pos + 10, pos + 12));
			giftcard.setAuthorizationNumber(autNbr);
			pos = pos + 12;
		}
		if (bitmap.charAt(RESPONSE_CODE - 1) == '1') {
			responseCode = translateHexaToUtf8(data.substring(pos, pos + 2)) + translateHexaToUtf8(data.substring(pos + 2, pos + 4));
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



	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.allc.core.operation.AbstractOperation#process(com.allc.comm.pipe.
	 * ConnPipeServer, com.allc.comm.frame.Frame, com.allc.properties.PropFile)
	 */
	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}


}
