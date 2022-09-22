package com.allc.arms.server.operations.cer.pinpad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.pinpad.PinPad;
import com.allc.arms.server.persistence.pinpad.PinPadDAO;
import com.allc.arms.server.persistence.store.StoreDAO;
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
import com.lectora.cnx.EnvioSeg;

public class SendClosedToPinPadOperation extends AbstractOperation{
	
	private Logger logger = Logger.getLogger(SendClosedToPinPadOperation.class);
	private Session session = null;
	private PinPadDAO pinpadDAO = new PinPadDAO();
	private Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	protected ConnSocketClient socketClient = null;
	StoreDAO storeDAO = new StoreDAO();
	
	String storeCentralIP = null;

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		
		logger.info("Iniciando SendClosedToPinPadOperation operation...");
		
		Session sessionSaadmin = null;
		sessionSaadmin = iniciarSesionSaadmin();
		storeCentralIP = storeDAO.getStoreByCode(sessionSaadmin, 0).getIp();
		
		try {
			
			Integer store = Integer.valueOf((String)frame.getHeader().get(Frame.POS_SOURCE));
		
			
			
			UtilityFile.createWriteDataFile(getEyesFileName(properties),
					"SEND_CLS_PP_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando busqueda pinpads para tienda: " + store + ".\n",
					true);
			
			iniciarSesion("Eyes");
			List<String> infoPinpadTienda = getInfoPinpadByTienda(store);
			String message = null;
			StringBuffer sb = new StringBuffer();
			sb.append("0");
			message = sb.toString();
			StringBuilder sb1 = new StringBuilder(frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + message);
			
			if (socket.writeDataSocket(Util.addLengthStartOfString(sb1.toString(),
					properties.getInt("serverSocket.quantityBytesLength")))) 
				
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"SEND_CLS_PP_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|PRC|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Enviando respuesta de cierre para Pinpad.\n",
						true);
			 else
					UtilityFile.createWriteDataFile(getEyesFileName(properties),
							"SEND_CLS_PP_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
									+ frame.getHeader().get(3) + "|WAR|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|No se pudo enviar la respuesta.\n",
							true);
			
			List pinpads = pinpadDAO.getPinpadByStore(session, store);
			
			if(pinpads != null && !pinpads.isEmpty()) {
				Iterator itPinpads = pinpads.iterator();
				while(itPinpads.hasNext()){
					
					logger.info("Inicia envio trama para cada pinpad de la tienda.");
					
					PinPad pinpad = (PinPad) itPinpads.next();
					String msjConClave = null;
					StringBuffer mensaje = new StringBuffer("");
					mensaje.append("PC");
					mensaje.append(StringUtils.leftPad(infoPinpadTienda.get(0), 6, "0"));
					mensaje.append(StringUtils.leftPad(infoPinpadTienda.get(2), 6, "0"));
					mensaje.append(StringUtils.leftPad(infoPinpadTienda.get(1), 6, "0"));
					mensaje.append(StringUtils.leftPad(infoPinpadTienda.get(3), 6, "0"));
					mensaje.append(StringUtils.rightPad(infoPinpadTienda.get(4), 15, ""));
					mensaje.append(StringUtils.rightPad(pinpad.getTidDatafast(), 8, ""));
					mensaje.append(StringUtils.rightPad(infoPinpadTienda.get(5), 15, ""));
					mensaje.append(StringUtils.rightPad(pinpad.getTidMedianet(), 8, ""));
					mensaje.append(StringUtils.rightPad(pinpad.getCidCaja(), 15, ""));
					
					msjConClave = EnvioSeg.Genera_Componente(mensaje.toString(), 1);
					StringBuffer mensajeAEnviar = new StringBuffer (msjConClave);
					
					String mjeLength = StringUtils.leftPad(Integer.toHexString(msjConClave.length()), 4, "0");
					mensajeAEnviar.insert(0, mjeLength);
					
					
					
					logger.info("Mensaje a Enviar: " + mensajeAEnviar.toString());
					
					ConnSocketClient socketClient = new ConnSocketClient();
					socketClient.setIpServer(pinpad.getDireccionIP());
					socketClient.setPortServer(Integer.valueOf(pinpad.getPuertoEscucha()));
					socketClient.setRetries(3);
					socketClient.setTimeOutConnection(20000);
					socketClient.setTimeOutSleep(600);
					socketClient.setQuantityBytesLength(2);
					String data = null;
					
					int cant;
					try {

						if (socketClient.connectSocketUsingRetries() && socketClient.writeDataSocket(mensajeAEnviar.toString())) {
							
							logger.info("Leyendo respuesta del pinpad.");
							cant = Integer.parseInt(socketClient.readDataSocket(4), 16);
							if(cant > 0){
								logger.info("Cantidad a leer:" + cant);
								data = socketClient.readDataSocket(cant);
								logger.info("Respuesta recibida:" + data);
								socketClient.closeConnection();
							}
							else{
								UtilityFile.createWriteDataFile(getEyesFileName(properties),
										"SEND_CLS_PP_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
												+ frame.getHeader().get(3) + "|WAR|"
												+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
												+ "|Proceso de control de pinpad no respondio, pero la trama se enviò.\n",
										true);
								Date fechaActual = new Date();
								pinpad.setFechaUltimoCierre(fechaActual);
								session.saveOrUpdate(pinpad);
							}
						} else
							UtilityFile.createWriteDataFile(getEyesFileName(properties),
									"SEND_CLS_PP_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
											+ frame.getHeader().get(3) + "|ERR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
											+ "|No hay conexión con el pinpad: "+ pinpad.getIdPinpad()+" de la tienda: "+store+" para realizar el cierre.\n", true);
									
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}

					if(data != null && !data.isEmpty()){
						
						if(data.substring(2, 4).equals("00")){
							logger.info("Respuesta exitosa desde proceso de control del pinpad.");
							UtilityFile.createWriteDataFile(getEyesFileName(properties),
									"SEND_CLS_PP_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
											+ frame.getHeader().get(3) + "|PRC|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
											+ "|Proceso de control de pinpad respondió con exito.\n",
									true);
							Date fechaActual = new Date();
							pinpad.setFechaUltimoCierre(fechaActual);
							session.saveOrUpdate(pinpad);
						}else
							UtilityFile.createWriteDataFile(getEyesFileName(properties),
									"SEND_CLS_PP_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
											+ frame.getHeader().get(3) + "|WAR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
											+ "|Proceso de control de pinpad respondió error. Codigo de error: " + data.substring(2, 4) + ". Mensaje de error: " + data.substring(6, 21) +".\n",
									true);
						
					}
				}
				
				Integer loteDatafast = Integer.valueOf(infoPinpadTienda.get(0));
				loteDatafast = loteDatafast + 1;
				String loteData = StringUtils.leftPad(String.valueOf(loteDatafast), 6, "0");
				Integer loteMedianet = Integer.valueOf(infoPinpadTienda.get(1));
				loteMedianet = loteMedianet + 1;
				String loteMedia = StringUtils.leftPad(String.valueOf(loteMedianet), 6, "0");
				
				updateNumLoteTienda(store, loteData, loteMedia);
				
				
				updateNumLoteInCentral(store, loteData, loteMedia);
							
			}  else
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "SEND_CLS_PP_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No existen pinpads para la tienda: "+ store +".\n", true);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "SEND_CLS_PP_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error durante el proceso de envío de cierre a pinpads.\n", true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
		return false;
	}
	
	private String getEyesFileName(PropFile properties) {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	protected void iniciarSesion(String name) {
		while (session == null) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory(name).openSession();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (session == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}
	}
	
	public boolean updateNumLoteTienda(Integer tienda, String loteData, String loteMedia) {
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session
					.createSQLQuery("UPDATE CFG_LOTE_PINPAD SET NUM_LOT_R_DATA = :valor1 WHERE DES_CLAVE = " + tienda);
			query.setParameter("valor1", loteData);
			query.executeUpdate();
			Query query2 = session
					.createSQLQuery("UPDATE CFG_LOTE_PINPAD SET NUM_LOT_R_MED = :valor1 WHERE DES_CLAVE = " + tienda);
			query2.setParameter("valor1", loteMedia);
			query2.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}
	
	private List<String> getInfoPinpadByTienda(Integer tienda) {
		try {

			SQLQuery query = session.createSQLQuery(
					"Select NUM_LOT_R_DATA, NUM_LOT_R_MED, SEQ_TRX_DATA, SEQ_TRX_MED, MID_DATA, MID_MED From CFG_LOTE_PINPAD Where DES_CLAVE = "+ tienda);
			List<Object[]> rows = query.list();
			for (Object[] row : rows) {
				List<String> retorno = new ArrayList<String>();
				retorno.add(row[0] != null ? row[0].toString() : null);
				retorno.add(row[1] != null ? row[1].toString() : null);
				retorno.add(row[2] != null ? row[2].toString() : null);
				retorno.add(row[3] != null ? row[3].toString() : null);
				retorno.add(row[4] != null ? row[4].toString() : null);
				retorno.add(row[5] != null ? row[5].toString() : null);
				return retorno;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public void updateNumLoteInCentral(Integer tienda, String loteDatafast, String loteMedianet){
		
		StringBuffer loteGenerate = new StringBuffer();
		String valueLote = ""; 
		List listLote;
		Frame frameLote;
		
		loteGenerate.append(ArmsServerConstants.Communication.SOCKET_CHANNEL)
		.append(ArmsServerConstants.Communication.FRAME_SEP)
		.append(ArmsServerConstants.Process.UPDATE_LOTE_CENTRAL_OP)
		.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
		.append(ArmsServerConstants.Communication.FRAME_SEP)
		.append(tienda).append(ArmsServerConstants.Communication.FRAME_SEP)
		.append(ArmsServerConstants.Communication.TEMP_CONN)
		.append(ArmsServerConstants.Communication.FRAME_SEP)
		.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
		.append(ArmsServerConstants.Communication.FRAME_SEP).append(loteDatafast)
		.append(ArmsServerConstants.Communication.FRAME_SEP).append(loteMedianet)
		.append(ArmsServerConstants.Communication.FRAME_SEP).append(tienda)
		;
		
		valueLote = loteGenerate.toString();
		
		listLote = Arrays.asList(p.split(valueLote));
		
		frameLote = new Frame(listLote, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
				ArmsServerConstants.Communication.FRAME_SEP);
		
		if (frameLote.loadData()) {
			boolean send = sendFrame(frameLote, properties, storeCentralIP);
			closeClient();
			if (send) {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "SEND_CLS_PP_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+tienda+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Trama para actualizar lotes en central eviada con exito.\n", true);
				logger.info("Trama para actualizar lotes en central eviada con exito.\n");
				
			} else {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "SEND_CLS_PP_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+tienda+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Trama para actualizar lotes en central no enviada.\n", true);
				logger.error("Trama para actualizar lotes en central no enviada.\n");
			}
		}
		
//		frameLote.loadData();
//		String tramaC = Util.addLengthStartOfString(frameLote.getString().toString(), properties.getInt("serverSocket.quantityBytesLength"))
//				.toString();
//		boolean tramaGenerateRecapEnviada = false;
//		if (socketClient.connectSocketUsingRetries() && socketClient.writeDataSocket(tramaC)) {
//			Frame frameRta = leerRespuesta(socketClient);
//			frameRta.loadData();
//			if (frameRta.getStatusTrama() == 0) {
//				tramaGenerateRecapEnviada = true;
//			}
//			socketClient.closeConnection();
//			if(tramaGenerateRecapEnviada)
//				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "SEND_CLS_PP_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+tienda+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Trama para actualizar lotes en central eviada con exito.\n", true);
//			else
//				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "SEND_CLS_PP_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+tienda+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Trama para actualizar lotes en central no enviada.\n", true);
//		} else
//			Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "SEND_CLS_PP_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+tienda+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No hay conexión con central para enviar trama de actualizacion de lotes.\n", true);
//		
			
	}
	
	protected Frame leerRespuesta(ConnSocketClient socketClient) {
		int numberOfBytes = socketClient.readLengthDataSocket();
		if (numberOfBytes > 0) {
			String str = socketClient.readDataSocket(numberOfBytes);
			if (StringUtils.isNotBlank(str)) {
				List list = Arrays.asList(p.split(str));
				Frame frameRpta = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
						ArmsServerConstants.Communication.FRAME_SEP);
				logger.info("Respuesta recibida: " + frameRpta.toString());
				return frameRpta;
			}
		}
		if( numberOfBytes == 0)
			numberOfBytes = socketClient.readLengthDataSocket();
		
		logger.info("No se recibio respuesta.");
		return null;
	}
	
	protected boolean connectClient(PropFile properties, String ipStore) {

		if (socketClient == null || !socketClient.isConnected()) {
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(ipStore);
			socketClient.setPortServer(properties.getInt("serverSocket.port"));
			socketClient.setRetries(2);
			socketClient.setTimeOutConnection(properties.getInt("serverSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("serverSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("serverSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}

	protected void closeClient() {
		if (socketClient != null)
			socketClient.closeConnection();
	}
	
	protected Session iniciarSesionSaadmin() {

		Session sessionSaadmin = null;

		while (sessionSaadmin == null) {
			try {
				sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (sessionSaadmin == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}

		return sessionSaadmin;
	}
	
	protected boolean sendFrame(Frame frame, PropFile properties, String ipTienda) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected())
				connectClient(properties, ipTienda);
			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
			logger.info("Trama a enviar: " + trama);
			if (socketClient.writeDataSocket(trama)) {
				int numberOfBytes = 0;
				int timeOutCycles = 0;
				while (numberOfBytes == 0) {
					numberOfBytes = socketClient.readLengthDataSocket();
					if (timeOutCycles == 5) {
						// cada 5 timeouts escribimos una trama vacía para
						// asegurarnos que el socket esté activo
						String mje = Util.addLengthStartOfString("", qtyBytesLength).toString();
						if (!socketClient.writeDataSocket(mje)) {
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
						frameRpta = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
								ArmsServerConstants.Communication.FRAME_SEP);
						logger.info("Respuesta recibida: " + frameRpta.toString());
						if (frameRpta.getStatusTrama() == 0) {
							return true;
						}
					}
				}
			} else {
				socketClient.setConnected(false);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			socketClient.setConnected(false);
		}
		return false;
	}

	@Override
	public boolean process(ConnPipeServer arg0, Frame arg1, PropFile arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
