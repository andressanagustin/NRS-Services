/**
 * 
 */
package com.allc.arms.server.operations.cer.ilimitada;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.devolucion.DevolucionDAO;
import com.allc.arms.server.persistence.devolucion.Ilimitada;
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
public class ReversarIlimitadaOperation extends AbstractOperation {
	private Logger log = Logger.getLogger(ReversarIlimitadaOperation.class);
	private int sleepTime;
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
			String monto = (String) frame.getBody().get(0);
			String refNum = (String) frame.getBody().get(1);
			String terminalId = (String) frame.getBody().get(2);
			String merchantId = (String) frame.getBody().get(3);
			String numAuto = (String) frame.getBody().get(4);
			String ilimData = (String) frame.getBody().get(5);
			Integer idDev = Integer.valueOf((String) frame.getBody().get(6));

			
			UtilityFile.createWriteDataFile(getEyesFileName(properties), "REV_ILI_O|"+properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
					+ frame.getHeader().get(3) + "|STR|"
					+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Reversando Ilimitada.\n", true);
			if (reversarIlimitada(monto, refNum, terminalId, merchantId, numAuto, ilimData, idDev)) {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "REV_ILI_O|"+properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
						+ frame.getHeader().get(3) + "|STR|"
						+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Ilimitada reversada correctamente.\n", true);
				message.append("0");
			} else {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "REV_ILI_O|"+properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
						+ frame.getHeader().get(3) + "|STR|"
						+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al reversar la ilimitada.\n", true);
				message.append("1");
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			}
			message.append(frame.getHeaderStr()).append(frame.getSeparator()).append("0");
			String tmp = Util.addLengthStartOfString(message.toString(),
					properties.getInt("serverSocket.quantityBytesLength"));
			socket.writeDataSocket(tmp);
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"REV_ILI_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al reversar la Ilimitada.\n",
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
	private boolean reversarIlimitada(String monto, String refNum, String terminalId, String merchantId, String numAuto, String ilimData, Integer idDev) {
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
		bitmap.setCharAt(TERMINAL_ID - 1, '1');
		bitmap.setCharAt(MERCHANT_ID - 1, '1');
		bitmap.setCharAt(NUMERO_LOTE_ACTIVO - 1, '1');
		bitmap.setCharAt(ADICIONAL_DATA_3 - 1, '1');
		bitmap.setCharAt(RESERVED_FOR_NATIONAL_USE - 1, '1');
		bitmap.setCharAt(RESERVED_FOR_PRIVATE_USE - 1, '1');
		try {
			StringBuffer msg = new StringBuffer("MT="+type);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("BM1="+bitmap.substring(0, 64));
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("BM2="+bitmap.substring(64,128));
			String primaryAcctNbr = generarNvariable(ilimData.substring(45, 55), 15, 2);//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PAC="+ilimData.substring(45, 55));
			String processingCode = "244000";//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PC="+processingCode);
			String transAmount = generarNfijo("0", 12);//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AM="+transAmount);
			String systemTANbr = generarNfijo(refNum, 6);//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("STAN="+refNum);
			String fecha = sdf.format(new Date());
			String localTrxTime = generarNfijo(fecha.substring(4, 10), 6);//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("LTT="+fecha.substring(4, 10));
			String localTrxDate = generarNfijo(fecha.substring(0, 4), 4);//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("LTD="+fecha.substring(0, 4));
			String expirationDate = generarNfijo("9912", 4);//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("ED="+"9912");
			String posEntryMode = "0120";//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PEM="+posEntryMode);
			String networkIntID = "0001";//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("NII="+networkIntID);
			String posCondCode = "00";//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PCC="+posCondCode);
			String mid = merchantId.trim();
			String terminalID = generarAns(mid.substring(mid.length()-5)+terminalId.trim(), 8);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("TI="+mid.substring(mid.length()-5)+terminalId.trim());
			String merchantID = generarAns(merchantId.trim() + "   ", 15);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("MI="+merchantId.trim() + "   ");
			String nroLoteActivo = generarAnsvariable("000001", 6, 4);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("NLA="+"000001");
			String addData = generarAnsvariable("01", 2, 4);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AD3="+"01");
			String resNatUse = generarAnsvariable("00010001", 8, 4);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("RFNU="+"00010001");
			String resPrivUse = generarAnsvariable("C00304                              ", 36, 4);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("RFPU="+"C00304                              ");
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
			mje.append(terminalID);
			mje.append(merchantID);
			mje.append(nroLoteActivo);
			mje.append(addData);
			mje.append(resNatUse);
			mje.append(resPrivUse);
			
			writeTramaReq(msg.toString());
										
				
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

			if (data != null & !data.trim().isEmpty()) {
				String response = translateResponse(data);
				writeTramaResp(response);
				retorno = respuestaAfirmativa(response);

				Ilimitada ilimitada = new Ilimitada();
				ilimitada.setId(idDev);
				ilimitada.setResponseCodeConsulta(new String(responseCode));
				if(retorno){
					retorno = sendTramaReverso(monto, refNum, terminalId, merchantId, numAuto, ilimData);
					ilimitada.setResponseCodeReverso(new String(responseCode));
				}
				devolucionDAO.insertIlimitada(session, ilimitada);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			retorno = false;
		}
		cerrarSesion();
		return retorno;
	}
	
	private boolean sendTramaReverso(String monto, String refNum, String terminalId, String merchantId, String numAuto, String ilimData){
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
		bitmap.setCharAt(AUTORIZATION_NUMBER - 1, '1');
		bitmap.setCharAt(POS_CONDITION_CODE - 1, '1');
		bitmap.setCharAt(TERMINAL_ID - 1, '1');
		bitmap.setCharAt(MERCHANT_ID - 1, '1');
		bitmap.setCharAt(NUMERO_LOTE_ACTIVO - 1, '1');
		bitmap.setCharAt(ADICIONAL_DATA_3 - 1, '1');
		bitmap.setCharAt(RESERVED_FOR_NATIONAL_USE - 1, '1');
		bitmap.setCharAt(ADICIONAL_DATA_4 - 1, '1');
		bitmap.setCharAt(RESERVED_FOR_PRIVATE_USE - 1, '1');
		try {
			StringBuffer msg = new StringBuffer("MT="+type);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("BM1="+bitmap.substring(0, 64));
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("BM2="+bitmap.substring(64,128));
			String primaryAcctNbr = generarNvariable(ilimData.substring(45, 55), 15, 2);//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PAC="+ilimData.substring(45, 55));
			String processingCode = "254000";//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PC="+processingCode);
			String transAmount = generarNfijo(monto, 12);//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AM="+transAmount);
			String systemTANbr = generarNfijo(refNum, 6);//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("STAN="+refNum);
			String fecha = sdf.format(new Date());
			String localTrxTime = generarNfijo(fecha.substring(4, 10), 6);//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("LTT="+fecha.substring(4, 10));
			String localTrxDate = generarNfijo(fecha.substring(0, 4), 4);//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("LTD="+fecha.substring(0, 4));
			String expirationDate = generarNfijo("9912", 4);//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("ED="+"9912");
			String posEntryMode = "0120";//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PEM="+posEntryMode);
			String networkIntID = "0001";//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("NII="+networkIntID);
			String posCondCode = "00";//
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PCC="+posCondCode);
			while(numAuto.length() < 6)
				numAuto = "0"+numAuto;
			String autorizationNum = generarAns(numAuto, 6);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AN="+numAuto);
			String mid = merchantId.trim();
			String terminalID = generarAns(mid.substring(mid.length()-5)+terminalId.trim(), 8);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("TI="+mid.substring(mid.length()-5)+terminalId.trim());
			String merchantID = generarAns(merchantId.trim() + "   ", 15);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("MI="+merchantId.trim() + "   ");
			String nroLoteActivo = generarAnsvariable("000001", 6, 4);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("NLA="+"000001");
			String addData = generarAnsvariable("01", 2, 4);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AD3="+"01");
			String resNatUse = generarAnsvariable("00010001", 8, 4);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("RFNU="+"00010001");
			String relleno = " ";
			while(relleno.length() < 428)
				relleno += " ";
			String addData4 = generarAnsvariable(relleno + ilimData, 627, 4);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AD4="+relleno + ilimData);
			String resPrivUse = generarAnsvariable("P0000000304304-00000000708          000000000000000000", 54, 4);
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("RFPU="+"P0000000304304-00000000708          000000000000000000");
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
			mje.append(autorizationNum);
			mje.append(terminalID);
			mje.append(merchantID);
			mje.append(nroLoteActivo);
			mje.append(addData);
			mje.append(resNatUse);
			mje.append(addData4);
			mje.append(resPrivUse);
			writeTramaReq(msg.toString());
										
				
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

			if (data != null & !data.trim().isEmpty()) {
				String response = translateResponse(data);
				writeTramaResp(response);
				return respuestaAfirmativa(response);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
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
			String sysTraceAudNbr = data.substring(pos, pos + 6);
			pos = pos + 6;
		}
		if (bitmap.charAt(LOCAL_TRANSACTION_TIME - 1) == '1') {
			String localTrxTime = data.substring(pos, pos + 6);
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
