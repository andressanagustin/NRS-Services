/**
 * 
 */
package com.allc.arms.server.processes.cer.reverso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.devolucion.DevolucionDAO;
import com.allc.arms.server.persistence.devolucion.PagoCer;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.Files;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * @author gustavo
 *
 */
public class ReversarPagoSysProcess extends AbstractProcess {
	private static Logger log = Logger.getLogger(ReversarPagoSysProcess.class);
	private File inFolder;
	private File outFolder;
	private int sleepTime;
	private Iterator filesToProcess = null;
	protected boolean finished = false;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	public boolean isEnd = false;
	private String responseCode;
	private SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
	private Session session = null;
	private Session sessionArts = null;
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
	public final int RESERVED = 125;

	protected void inicializar() {
		isEnd = false;
		try {
			inFolder = new File("C:/ALLC/WWW/EYES/PAGCER/IN");
			inFolder.mkdirs();
			outFolder = new File("C:/ALLC/WWW/EYES/PAGCER/BKP");
			outFolder.mkdirs();
			sleepTime = properties.getInt("activateGiftcard.sleeptime");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void run() {
		log.info("Iniciando ReversarPagoProcess...");
		inicializar();
		String filename = null;
		String storeCode = properties.getObject("eyes.store.code");
		while (!isEnd) {
			try {
				File giftFile = getNextPagoFile();

				if (giftFile != null) {
					filename = giftFile.getName().toUpperCase();
					String[] parts = filename.split("\\.");
					Integer store = new Integer(parts[1]);
					log.info("Archivo a procesar: " + filename + " STORE: " + store);
					UtilityFile.createWriteDataFile(getEyesFileName(), "RVS_PAG_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo a Procesar: "+filename+".\n", true);
					if (reversarPago(giftFile)) {
						giftFile.renameTo(new File(outFolder, giftFile.getName()));
						log.info("Archivo movido a la carpeta Out.");
						UtilityFile.createWriteDataFile(getEyesFileName(), "RVS_PAG_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo: "+filename+" procesado correctamente.\n", true);
					} else {
						UtilityFile.createWriteDataFile(getEyesFileName(), "RVS_PAG_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el Archivo: "+filename+".\n", true);
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							log.error(e.getMessage(), e);
						}
					}
				}
			} catch (Exception e) {
				UtilityFile.createWriteDataFile(getEyesFileName(), "RVS_PAG_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el Archivo: "+filename+".\n", true);
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

	private void iniciarSesionArts() {
		while (sessionArts == null && !isEnd) {
			try {
				sessionArts = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionArts == null)
				try {
					log.error("OCURRIÓ UN ERROR AL CREAR LA CONEXIÓN A LA BD, SE REINTENTARÁ EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	private void cerrarSesionArts() {
		try {
			sessionArts.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		sessionArts = null;
	}

	private boolean reversarPago(File pagoFile) {
		boolean retorno = true;
		responseCode = "";
		int seq = 0;
		iniciarSesion();
		DevolucionDAO devolucionDAO = new DevolucionDAO();
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
//		bitmap.setCharAt(EXPIRATION_DATE - 1, '1');
		bitmap.setCharAt(POS_ENTRY_MODE - 1, '1');
		bitmap.setCharAt(NETWORK_INTERNATIONAL_ID - 1, '1');
		bitmap.setCharAt(POS_CONDITION_CODE - 1, '1');
//		bitmap.setCharAt(TRACK_2_DATA - 1, '1');
		bitmap.setCharAt(AUTORIZATION_NUMBER - 1, '1');
		bitmap.setCharAt(TERMINAL_ID - 1, '1');
		bitmap.setCharAt(MERCHANT_ID - 1, '1');
//		bitmap.setCharAt(TRACK_1 - 1, '1');
		bitmap.setCharAt(NUMERO_LOTE_ACTIVO - 1, '1');
		bitmap.setCharAt(ADICIONAL_DATA_3 - 1, '1');
		bitmap.setCharAt(RESERVED_FOR_NATIONAL_USE - 1, '1');
		bitmap.setCharAt(RESERVED_FOR_PRIVATE_USE - 1, '1');
		
		String nroFactura = null;
		String nroTarj = null;
		Integer monto = null;
//		bitmap.setCharAt(RESERVED - 1, '1');
		try {
			FileReader fr = new FileReader(pagoFile);
			BufferedReader br = new BufferedReader(fr);
			boolean sendLine = false;
			String linea = br.readLine();
			while(linea != null){
				mje = new StringBuffer("");
				seq++;
				if (linea.length() == 88) {
					StringBuffer msg = new StringBuffer("MT="+type);
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("BM1="+bitmap.substring(0, 64));
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("BM2="+bitmap.substring(64,128));
					log.debug("Línea leida: " + linea);
					String primaryAcctNbr = generarNvariable(linea.substring(0, 16), 16, 2);//
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PAC="+linea.substring(0, 16));
					nroTarj = linea.substring(0, 16);
					String processingCode = "284000";//
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PC="+processingCode);
					String transAmount = generarNfijo(linea.substring(16, 28), 12);//
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AM="+linea.substring(16, 28));
					monto = Integer.valueOf(linea.substring(16,  28));
					String systemTANbr = generarNfijo(linea.substring(28, 34), 6);//
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("STAN="+linea.substring(28, 34));
					String fecha = sdf.format(new Date());
					String localTrxTime = generarNfijo(fecha.substring(4, 10), 6);//
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("LTT="+fecha.substring(4, 10));
					String localTrxDate = generarNfijo(fecha.substring(0, 4), 4);//
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("LTD="+fecha.substring(0, 4));
	//				String expirationDate = generarNfijo("0001", 4);//lineParts[2].substring(18, 22), 4);//
					String posEntryMode = "0120";//
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PEM="+posEntryMode);
					String networkIntID = "0001";//
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PEM="+posEntryMode);
					String posCondCode = "00";//
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("PCC="+posCondCode);
	//				String track2 = generarZvariable(lineParts[1], 37, 2) + "0";//
					String autoNumber = generarAns(linea.substring(34, 40), 6);
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AN="+linea.substring(34, 40));
					String terminalID = generarAns(linea.substring(40, 48), 8);
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("TI="+linea.substring(40, 48));
					String merchantID = generarAns(linea.substring(48, 63), 15);
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("MI="+linea.substring(48, 63));
	//				String track1 = generarAnsvariable(lineParts[0], 76, 2);
					String nroLoteActivo = generarAnsvariable("000001", 6, 4);
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("NLA="+"000001");
					//114
					String addData = generarAnsvariable("01", 2, 4);
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("AD3="+"01");
					//116
					String resNatUse = generarAnsvariable("00010001", 8, 4);
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("RFNU="+"00010001");
					//124
					String resPrivUse = generarAnsvariable("P"+linea.substring(63, 88)+"          000000000000000000", 54, 4);//P0000000027081001020161143          000000146000000001", 54, 4);
					msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("RFPU="+"P"+linea.substring(63, 88)+"          000000000000000000");
					nroFactura = linea.substring(73, 88);
					mje.append(header);
					mje.append(type);
					mje.append(translateBitmapToHexa(bitmap.toString()));
					mje.append(primaryAcctNbr);
					mje.append(processingCode);
					mje.append(transAmount);
					mje.append(systemTANbr);
					mje.append(localTrxTime);
					mje.append(localTrxDate);
	//				mje.append(expirationDate);
					mje.append(posEntryMode);
					mje.append(networkIntID);
					mje.append(posCondCode);
	//				mje.append(track2);
					mje.append(autoNumber);
					mje.append(terminalID);
					mje.append(merchantID);
	//				mje.append(track1);
					mje.append(nroLoteActivo);
					mje.append(addData);
					mje.append(resNatUse);
					mje.append(resPrivUse);
					writeTramaReq(msg.toString());
					
					sendLine = true;
				} else {
					log.info("Línea mal formateada");
				}
				String data = "";
				if(sendLine){
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
				}
	
				String filename = pagoFile.getName().toUpperCase();
				String[] parts = filename.split("\\.");
				PagoCer pagoCer = new PagoCer();
				pagoCer.setId(new Integer(parts[0].substring(3)));
				pagoCer.setSequenceNumber(seq);
				if (data != null & !data.trim().isEmpty()) {
					String response = translateResponse(data);
					writeTramaResp(response);
					boolean afirmativo = respuestaAfirmativa(response);
					if (afirmativo)
						pagoCer.setEstado(1);
					else
						pagoCer.setEstado(2);
					pagoCer.setResponseCode(responseCode);
				} else
					pagoCer.setEstado(3);
				devolucionDAO.insertPagoCer(session, pagoCer);
				registerRevPag(nroFactura, nroTarj, monto);
				linea = br.readLine();
			}
			br.close();
			fr.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			retorno = false;
		}
		cerrarSesion();
		return retorno;
	}
	
	protected boolean registerRevPag(String nroFactura, String nroTarj, Integer monto){
		boolean retorno = true;
		Transaction tx = null;
		try {
			log.info("Registrando pago");
			iniciarSesionArts();
			List<String> pagoData = getPagoCERData(nroFactura, nroTarj, monto);
			if(pagoData != null){
				tx = initTx();
				log.info("UPDATE CO_CRD_DT SET STS_CRD = '2' WHERE ID_TRN = '" + pagoData.get(0)+"' AND SQ_NBR = '" + pagoData.get(1) + "'");
				Query query = sessionArts.createSQLQuery("UPDATE CO_CRD_DT SET STS_CRD = '2' WHERE ID_TRN = '" + pagoData.get(0)+"' AND SQ_NBR = '" + pagoData.get(1) + "'");
				query.executeUpdate();
				tx.commit();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				if(tx !=null)
					tx.rollback();
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
			retorno = false;
		} finally {
			cerrarSesionArts();
		}
		log.info("Pago registrado");
		return retorno;
	}
	
	private List<String> getPagoCERData(String nroFactura, String nroTarj, Integer monto) {
		try {
			log.info("SELECT CD.ID_TRN, CD.SQ_NBR FROM TR_INVC I, CO_CRD_DT CD WHERE I.ID_TRN = CD.ID_TRN AND I.INVC_NMB = '"+nroFactura+"' AND CD.CRD_NBR = '"+nroTarj+"' AND CD.AMNT = "+monto+"");
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT CD.ID_TRN, CD.SQ_NBR FROM TR_INVC I, CO_CRD_DT CD WHERE I.ID_TRN = CD.ID_TRN AND I.INVC_NMB = '"+nroFactura+"' AND CD.CRD_NBR = '"+nroTarj+"' AND CD.AMNT = "+monto+"");

			List<Object[]> rows = query.list();
			if (rows != null && !rows.isEmpty())
				for(Object[] row : rows){
		    		List<String> retorno = new ArrayList<String>();
		    		retorno.add(row[0]!= null ? row[0].toString() : null);
		    		retorno.add(row[1]!= null ? row[1].toString() : null);
		    		return retorno;
		    	}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public Transaction initTx() {
		Transaction tx = null;
		while (tx == null || !tx.isActive()) {
			try {
				tx = sessionArts.beginTransaction();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				tx = null;
			}
			if (tx == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA TRANSACCION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
		return tx;
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
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("BM2="+bitmap2);
			bitmap.append(bitmap2);
		}
		
		if (bitmap.charAt(PRIMARY_ACCOUNT_NUMBER - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 2)).intValue();
			if(cantAleer % 2 == 1)
				cantAleer++;
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
		if ((bitmap.charAt(BITMAP_2_ACTIVE - 1) == '1') && bitmap.charAt(RESERVED - 1) == '1') {
			int cantAleer = Integer.valueOf(data.substring(pos, pos + 4)).intValue();
			String addData = data.substring(pos+4, pos+4 +((cantAleer * 2)));
			msg.append(ArmsServerConstants.Communication.FRAME_SEP).append("RV="+translateHexaStringToUtf8(addData));
			pos = pos + 4 + (cantAleer * 2);
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
			if(cantAleer % 2 == 1)
				cantAleer++;
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

	private File getNextPagoFile() {
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd)
					return null;
				File[] files = inFolder.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isFile() && (pathname.getName().toUpperCase().startsWith("PAG"));
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
