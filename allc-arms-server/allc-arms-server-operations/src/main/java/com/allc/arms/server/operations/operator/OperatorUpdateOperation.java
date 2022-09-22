package com.allc.arms.server.operations.operator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.allc.arms.server.operations.utils.AbstractOperationPrincipal;
import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.operator.AuthorizesOPC;
import com.allc.arms.server.persistence.operator.Indicat;
import com.allc.arms.server.persistence.operator.IndicatOPC;
import com.allc.arms.server.persistence.operator.LevelAuthorizes;
import com.allc.arms.server.persistence.operator.Operator;
import com.allc.arms.server.persistence.operator.OperatorDAO;
import com.allc.arms.server.persistence.operator.OperatorStore;
import com.allc.arms.server.persistence.operator.OperatorWrapper;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.thoughtworks.xstream.XStream;

/**
 * Operación encargada de recibir la actualización del operador desde ArmsServer Central, registrarla en ArmsServer Local y enviarla a ArmsAgent.
 * 
 * @author gustavo
 * @modified AndresS
 * 
 */
public class OperatorUpdateOperation extends AbstractOperationPrincipal{
	
	private Logger logger = Logger.getLogger(OperatorUpdateOperation.class);
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	protected String storeCode;
	protected String storeCodeLocal;
	protected String storeCodeRegional;
	private Session sessionOpera = null;
	private Session sessionSaadmin = null;
	public final Integer STATUS_INICIAL = new Integer(0);
	public final Integer STATUS_PROCESAR = new Integer(1);
	public final Integer STATUS_EN_PROCESO = new Integer(2);
	public final Integer STATUS_PROCESADO = new Integer(3);
	public final Integer STATUS_PROCESAR_SIN_PASSW = new Integer(4);
	public final Integer STATUS_PROCESAR_SOLO_REGIONAL = new Integer(5);
	public final Integer ERROR_CONEXION_TIENDA_ACTUAL = new Integer(100);
	public final Integer ERROR_CONEXION_TIENDA_ANTERIOR = new Integer(101);
	public final Integer SUBSCRIBE_BLOQUEADO = new Integer(0);
	public final Integer SUBSCRIBE_ACTIVO = new Integer(1);
	public final Integer SUBSCRIBE_ELIMINADO = new Integer(2);
	public final Integer DOWNLOAD_START = new Integer(1);
	protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	protected boolean isEnd = false;
	protected ConnSocketClient socketClient = null;
	private static SimpleDateFormat formateador = new SimpleDateFormat("yyyyMMddHHmmss", new Locale("ES_ES"));
	// definimos esta variable para ver si descargamos al controlador los cambios de operadores(true) o no(false)
	private boolean sendControlador = true;
	
	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		List list;
		Frame frameToAgent;
		// si es distinto de 000 es un servidor local
		storeCodeLocal = properties.getObject("eyes.store.code");
		// si es dintinto de 000 es un servidor regional
		storeCodeRegional = properties.getObject("eyes.store.code.group");
		iniciarSesion("Opera", "Saadmin");
		StringBuilder message = new StringBuilder();
		String msg = "1";
		OperatorDAO operatorDAO = new OperatorDAO();
		StoreDAO storeDAO = new StoreDAO();
		boolean connectionError = false;
		logger.info("Iniciando OperatorUpdateOperation...");
		String operatorID = null;
		boolean sendToTienda = false; // servidor local
		boolean sendToRegionalServ = false; // servidor regional
		
		try {					
			
			String OperatorXML = (String) frame.getBody().get(0);
			sendControlador = Integer.parseInt((String)frame.getBody().get(1)) == 0 ? false : true;
			XStream xstream = new XStream();
			xstream.alias("Operator", Operator.class);
			xstream.alias("AuthorizesOPC", AuthorizesOPC.class);
			xstream.alias("LevelAuthorizes", LevelAuthorizes.class);
			xstream.omitField(Operator.class, "tiendas");
			xstream.alias("IndicatOPC", IndicatOPC.class);
			xstream.alias("Indicat", Indicat.class);
			xstream.aliasField("OperadorId", Operator.class, "operadorId");
			xstream.aliasField("OptionsLevel", Operator.class, "optionsLevel");
			xstream.aliasField("Name", Operator.class, "name");
			xstream.aliasField("OperatorBirthDate", Operator.class, "operatorBirthDate");
			xstream.aliasField("Status", Operator.class, "status");
			xstream.aliasField("IndSegMej", Operator.class, "indSegMejorada");
			xstream.aliasField("Subscribe", Operator.class, "subscribe");
			xstream.aliasField("IdentityDocument", Operator.class, "identityDocument");
			xstream.aliasField("CodTienda", Operator.class, "CodTienda");
			xstream.aliasField("TipoModelo", Operator.class, "tipoModelo");
			xstream.aliasField("IpTienda", Operator.class, "IpTienda");
			xstream.aliasField("StatusTienda", Operator.class, "statusTienda");
			xstream.aliasField("SubscribeTienda", Operator.class, "subscribeTienda");
			xstream.aliasField("CodNegocio", Operator.class, "codNegocio");
			xstream.aliasField("Grupo", Operator.class, "grupo");
			xstream.aliasField("Uusuario", Operator.class, "usuario");
			xstream.aliasField("NivelAut", Operator.class, "nivelAut");
			xstream.aliasField("NivelAutSO", Operator.class, "nivelAutSO");
            xstream.aliasField("IdModOpera", Operator.class, "idModOpera");
			xstream.aliasField("IdModOperaSO", Operator.class, "idModOperaSO");
			xstream.aliasField("IndicatOPC", AuthorizesOPC.class, "indicatOPC");
			xstream.aliasField("Indicat", IndicatOPC.class, "indicat");
			xstream.addImplicitCollection(Operator.class, "levelAuthorizations", "LevelAuthorizes", LevelAuthorizes.class);
			xstream.addImplicitCollection(Operator.class, "authorizations","AuthorizesOPC", AuthorizesOPC.class );
			
			Operator operator = (Operator) xstream.fromXML(OperatorXML);
			operatorID = operator.getIdentityDocument().toString();
			UtilityFile.createWriteDataFile(getEyesFileName(properties), "OPER_UPD_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Actualizando operador: "+operatorID+".\n", true);
			Operator originOp = operatorDAO.getOperatorsByIdentityDocument(sessionOpera, operator.getIdentityDocument().toString());
			// si no lo encuentra? originOp == null
			originOp = updateOperator(originOp, operator);
			/*if(operator.getIdModOpera() == null || operator.getIdModOpera() <= 0)
            {
                operator.setAuthorizations(new ArrayList<>());
            }
            if(operator.getIdModOperaSO()== null || operator.getIdModOperaSO()<= 0)
            {
                operator.setLevelAuthorizations(new ArrayList<>());
            }*/
                                    
			Store storeToSend = storeDAO.getStoreByCode(sessionSaadmin, originOp.getCodTienda());
			int codStore = storeToSend.getStoreId();
			String keyStore = storeToSend.getKey().toString();
			
			int itemTndOpe = updateOperatorANDTienda(operatorDAO,originOp,codStore);
			
			// agregar manejo de varias tiendas NOO RECIBE DE A UNA TIENDA
			String statusIni = originOp.getStatus().toString();
			String statusIniTienda = originOp.getStatusTienda().toString();
			logger.info("statusIniTienda:"+statusIniTienda+"statusIni:"+statusIni);
			
			
			originOp.setStatus(STATUS_EN_PROCESO);
			originOp.setStatusTienda(STATUS_EN_PROCESO);
			originOp.getTiendas().get(itemTndOpe).setStatus(STATUS_EN_PROCESO);
			
			operatorDAO.updateOperator(sessionOpera, originOp);
			//operatorDAO.updateOperatorStore(sessionOpera, originOpTnd);
			
			// Es un servidor LOCAL
			if(Integer.parseInt(storeCodeLocal) != 0 && Integer.parseInt(storeCodeRegional) == 0 )
			{
				storeCode = storeCodeLocal;
				logger.info("ESTAMOS EN UN SERVIDOR LOCAL, codStore: " + storeCode);
			}
			
			
			// Es un servidor REGIONAL
			if(Integer.parseInt(storeCodeLocal) == 0 && Integer.parseInt(storeCodeRegional) != 0 )
			{
				storeCode = storeCodeRegional;
				logger.info("ESTAMOS EN UN SERVIDOR REGIONAL, codStore: " + storeCode);
				//Store storeToSend = storeDAO.getStoreById(sessionSaadmin, Integer.valueOf(operator.getCodTienda()));
				
				// hay servidor local mandamos para agregar en el servidor local
				if(storeDAO.hayServidorLocal(sessionSaadmin, Integer.valueOf(keyStore))){
					logger.info("HAY SERVIDOR LOCAL, MANDA A TIENDA: " + operator.getCodTienda());
					XStream xstreamAux = new XStream();
					xstreamAux.alias("Operator", Operator.class);
					xstreamAux.alias("AuthorizesOPC", AuthorizesOPC.class);
					xstreamAux.alias("LevelAuthorizes", LevelAuthorizes.class);
					xstreamAux.omitField(Operator.class, "tiendas");
					xstreamAux.alias("IndicatOPC", IndicatOPC.class);
					xstreamAux.alias("Indicat", Indicat.class);
					xstreamAux.aliasField("OperadorId", Operator.class, "operadorId");
					xstreamAux.aliasField("OptionsLevel", Operator.class, "optionsLevel");
					xstreamAux.aliasField("Name", Operator.class, "name");
					xstreamAux.aliasField("OperatorBirthDate", Operator.class, "operatorBirthDate");
					xstreamAux.aliasField("Status", Operator.class, "status");
					xstreamAux.aliasField("IndSegMej", Operator.class, "indSegMejorada");
					xstreamAux.aliasField("Subscribe", Operator.class, "subscribe");
					xstreamAux.aliasField("IdentityDocument", Operator.class, "identityDocument");
					xstreamAux.aliasField("CodTienda", Operator.class, "CodTienda");
					xstreamAux.aliasField("TipoModelo", Operator.class, "tipoModelo");
					xstreamAux.aliasField("IpTienda", Operator.class, "IpTienda");
					xstreamAux.aliasField("StatusTienda", Operator.class, "statusTienda");
					xstreamAux.aliasField("SubscribeTienda", Operator.class, "subscribeTienda");
					xstreamAux.aliasField("CodNegocio", Operator.class, "codNegocio");
					xstreamAux.aliasField("Grupo", Operator.class, "grupo");
					xstreamAux.aliasField("Uusuario", Operator.class, "usuario");
					xstreamAux.aliasField("NivelAut", Operator.class, "nivelAut");
                    xstreamAux.aliasField("NivelAutSO", Operator.class, "nivelAutSO");
					xstreamAux.aliasField("IdModOpera", Operator.class, "idModOpera");
					xstreamAux.aliasField("IdModOperaSO", Operator.class, "idModOperaSO");
					xstreamAux.aliasField("Indicat", IndicatOPC.class, "indicat");
					xstreamAux.aliasField("IndicatOPC", AuthorizesOPC.class, "indicatOPC");
					xstreamAux.addImplicitCollection(Operator.class, "levelAuthorizations", "LevelAuthorization", LevelAuthorizes.class);
					xstreamAux.addImplicitCollection(Operator.class, "authorizations","Authorization", AuthorizesOPC.class );
					//seteamos el estado inicial para enviarlo
					operator.setStatus(Integer.valueOf(statusIni));
					String opXML = xstreamAux.toXML(operator);
					//volvemos al estado real
					operator.setStatus(STATUS_EN_PROCESO);
					
					StringBuffer data = new StringBuffer();
					data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
							.append(ArmsServerConstants.Process.OPERATOR_DEALER)
							.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
							.append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
							.append(ArmsServerConstants.Communication.FRAME_SEP)
							.append(ArmsServerConstants.Communication.TEMP_CONN)
							.append(ArmsServerConstants.Communication.FRAME_SEP)
							.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
							.append(ArmsServerConstants.Communication.FRAME_SEP)
							.append(opXML);
					//Store storeToSend = storeDAO.getStoreByCode(sessionSaadmin, Integer.valueOf(operator.getCodTienda()));
					connectionError = !sendToLocal(data, storeToSend);
					closeConnection();
					sendToTienda = true;
				} 
				
				
				// hay servidor regional mandamos para agregar en el servidor regional
				String ipServerHijo = hayServidorRegional(Integer.valueOf(keyStore));
				logger.info("ip Server Hijo: " + ipServerHijo);
				if(ipServerHijo != null){
					logger.info("HAY SERVIDOR REGIONAL, MANDA A TIENDA: " + operator.getCodTienda());
					XStream xstreamAux = new XStream();
					xstreamAux.alias("Operator", Operator.class);
					xstreamAux.alias("AuthorizesOPC", AuthorizesOPC.class);
					xstreamAux.alias("LevelAuthorizes", LevelAuthorizes.class);
					xstreamAux.omitField(Operator.class, "tiendas");
					xstreamAux.alias("IndicatOPC", IndicatOPC.class);
					xstreamAux.alias("Indicat", Indicat.class);
					xstreamAux.aliasField("OperadorId", Operator.class, "operadorId");
					xstreamAux.aliasField("OptionsLevel", Operator.class, "optionsLevel");
					xstreamAux.aliasField("Name", Operator.class, "name");
					xstreamAux.aliasField("OperatorBirthDate", Operator.class, "operatorBirthDate");
					xstreamAux.aliasField("Status", Operator.class, "status");
					xstreamAux.aliasField("IndSegMej", Operator.class, "indSegMejorada");
					xstreamAux.aliasField("Subscribe", Operator.class, "subscribe");
					xstreamAux.aliasField("IdentityDocument", Operator.class, "identityDocument");
					xstreamAux.aliasField("CodTienda", Operator.class, "CodTienda");
					xstreamAux.aliasField("TipoModelo", Operator.class, "tipoModelo");
					xstreamAux.aliasField("IpTienda", Operator.class, "IpTienda");
					xstreamAux.aliasField("StatusTienda", Operator.class, "statusTienda");
					xstreamAux.aliasField("SubscribeTienda", Operator.class, "subscribeTienda");
					xstreamAux.aliasField("CodNegocio", Operator.class, "codNegocio");
					xstreamAux.aliasField("Grupo", Operator.class, "grupo");
					xstreamAux.aliasField("Uusuario", Operator.class, "usuario");
					xstreamAux.aliasField("NivelAut", Operator.class, "nivelAut");
                    xstreamAux.aliasField("NivelAutSO", Operator.class, "nivelAutSO");
					xstreamAux.aliasField("IdModOpera", Operator.class, "idModOpera");
					xstreamAux.aliasField("IdModOperaSO", Operator.class, "idModOperaSO");
					xstreamAux.aliasField("Indicat", IndicatOPC.class, "indicat");
					xstreamAux.aliasField("IndicatOPC", AuthorizesOPC.class, "indicatOPC");
					xstreamAux.addImplicitCollection(Operator.class, "levelAuthorizations", "LevelAuthorization", LevelAuthorizes.class);
					xstreamAux.addImplicitCollection(Operator.class, "authorizations","Authorization", AuthorizesOPC.class );
					//seteamos el estado inicial para enviarlo
					operator.setStatus(Integer.valueOf(statusIni));
					String opXML = xstreamAux.toXML(operator);
					//volvemos al estado real
					operator.setStatus(STATUS_EN_PROCESO);
					
					StringBuffer data = new StringBuffer();
					data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
							.append(ArmsServerConstants.Process.OPERATOR_DEALER)
							.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
							.append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
							.append(ArmsServerConstants.Communication.FRAME_SEP)
							.append(ArmsServerConstants.Communication.TEMP_CONN)
							.append(ArmsServerConstants.Communication.FRAME_SEP)
							.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
							.append(ArmsServerConstants.Communication.FRAME_SEP)
							.append(opXML);
					//Store storeToSend = storeDAO.getStoreByCode(sessionSaadmin, Integer.valueOf(operator.getCodTienda()));
					// preguntar por este ip si esta bien
					logger.info("Ip: " + storeToSend.getIp());
					connectionError = !sendToLocal(data, storeToSend);
					closeConnection();
					sendToRegionalServ = true;
				} 			
			}
			
			
			
			OperatorWrapper operatorWrapper = new OperatorWrapper();
			if (!sendToTienda && !sendToRegionalServ && !connectionError && process(originOp, operatorWrapper)) {		
				logger.info("Mandamos al controlador, ip:" + operatorWrapper.getIpStore() + " - sendControlador: " + sendControlador);
				if(sendControlador){
					// bloquear 
					//if (statusIni.equals(STATUS_PROCESAR) && originOp.getSubscribe().intValue() == 0) {
					if (statusIniTienda.equals(STATUS_PROCESAR.toString()) && originOp.getSubscribeTienda().intValue() == SUBSCRIBE_BLOQUEADO) {
						Random rnd = new Random();
						Long randLong = new Long(rnd.nextLong());
						randLong = randLong < 0 ? randLong * -1 : randLong;
						String pass = (randLong).toString();
						operatorWrapper.setPassword(pass.length() > 8 ? pass.substring(0, 8) : pass);
						logger.debug("Password aleatoria: " + operatorWrapper.getPassword());
					}
					// seteo el estado inicial para que en el Agente se sepa si hay que modificar la password
					operatorWrapper.setStatus(statusIniTienda);
					xstream.alias("OperatorWrapper", OperatorWrapper.class);
					String xml = xstream.toXML(operatorWrapper);
					
					String today = ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date());
					
					// si este servidor local corresponde a la tienda actual, informamos a nuestro agente
					//if(Integer.valueOf(storeCodeLocal).compareTo(Integer.valueOf(originOp.getCodTienda())) == 0) {
					if (socketClient == null || !socketClient.isConnected())
						connectionError = !connectClient(operatorWrapper.getIpStore());
					
					//logger.info("ConnectionError: " + connectionError  + ", sndToTienda: " + sendToTienda + ", ip:" + socketClient.getIpServer() + ", port:" + socketClient.getPortServer());
					if(!sendToTienda && !connectionError){		
						//logger.info("Entro a mandar el msj.");
						message.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
								.append(ArmsServerConstants.Process.OPERATOR_DEALER)
								.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(originOp.getCodTienda())
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(ArmsServerConstants.Communication.TEMP_CONN)
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(today)
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(xml);
						list = Arrays.asList(p.split(message.toString()));
		
						frameToAgent = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
								ArmsServerConstants.Communication.FRAME_SEP);
						
						if (frameToAgent.loadData()) {
							connectionError = !sendOperatorUpdate(operatorWrapper.getIpStore(), frameToAgent);
							closeConnection();
						}
					}
				}
				
				
				if (!connectionError) {
					//originOpTnd.setStatus(STATUS_PROCESADO);
					//operatorDAO.updateOperatorStore(sessionOpera, originOpTnd);
					originOp.getTiendas().get(itemTndOpe).setStatus(STATUS_PROCESADO);
					originOp.getTiendas().get(itemTndOpe).setDownload(0);
					logger.info("Operador tienda: " + originOp.getTiendas().get(itemTndOpe).getOperadorId()  + ", actualizado.");
					
					originOp.setStatus(STATUS_PROCESADO);
					operatorDAO.updateOperator(sessionOpera, originOp);
					logger.info("Operador: " + originOp.getIdentityDocument() + ", actualizado.");
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "OPER_UPD_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Operador: "+operatorID+" procesado.\n", true);
					msg = "0";
				} else {
					//originOpTnd.setStatus(STATUS_PROCESAR);
					originOp.getTiendas().get(itemTndOpe).setStatus(STATUS_PROCESAR);
					originOp.getTiendas().get(itemTndOpe).setDownload(DOWNLOAD_START);
					originOp.setStatus(STATUS_PROCESAR);
					originOp.setDownload(DOWNLOAD_START);
					//operatorDAO.updateOperatorStore(sessionOpera, originOpTnd);
					operatorDAO.updateOperator(sessionOpera, originOp);
					logger.error("Error de conexi�n, se vuelve el operador al estado: " + STATUS_PROCESAR);
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "OPER_UPD_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo informar el Operador: "+operatorID+".\n", true);
				}
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "OPER_UPD_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el Operador: "+operatorID+".\n", true);
			//Manda ok de respuesta por mas que no lo envía al controlador ya que actualizó en este server
			msg = "0";
			StringBuffer sb = new StringBuffer(frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + msg);
			socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")));
			Thread.sleep(3000);
		} catch (Exception e) {
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "OPER_UPD_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al actualizar el operador: "+operatorID+".\n", true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
			logger.error(e.getMessage(), e);
		}

		return false;
	}
	


	protected boolean sendOperatorUpdate(String ip, Frame frame) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected())
				connectClient(ip);
			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
			logger.info("Trama a enviar: " + trama);
			if (socketClient.writeDataSocket(trama)) {
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
		
	protected boolean connectClient(String ip) {
		if (socketClient == null || !socketClient.isConnected()) {
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(ip);
			socketClient.setPortServer(properties.getInt("clientSocket.port"));
			socketClient.setRetries(properties.getInt("clientSocket.retries"));
			socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
		}
		//logger.info("IP: " + socketClient.getIpServer() +", puerto: " + socketClient.getPortServer());
		return socketClient.connectSocketUsingRetries();
	}
	
	protected boolean connectClient(PropFile properties, Store tienda) {
		if (socketClient == null || !socketClient.isConnected()) {
			logger.info("Store IP: " + tienda.getIp());
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(tienda.getIp());
			socketClient.setPortServer(properties.getInt("serverSocket.port"));
			socketClient.setRetries(2);
			socketClient.setTimeOutConnection(properties.getInt("serverSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("serverSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("serverSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}
	
	protected void closeConnection() {
		if (socketClient != null)
			socketClient.closeConnection();
	}


	protected OperatorStore updateOperatorTienda(OperatorStore originOpTnd, Operator originOp,Integer codStore) {
		if(originOpTnd == null)
			originOpTnd = new OperatorStore();
		
		originOpTnd.setCodTienda(codStore);
		originOpTnd.setIpTienda(originOp.getIpTienda());
		originOpTnd.setOperadorId(originOp.getOperadorId());
		originOpTnd.setStatus(originOp.getStatusTienda());
		originOpTnd.setSubscribe(originOp.getSubscribeTienda());
		originOpTnd.setFecha(new Date());
		return originOpTnd;
	}
	
	protected Integer updateOperatorANDTienda(OperatorDAO operatorDAO, Operator originOp,Integer codStore) throws Exception {
		logger.info("originOp.getOperadorId():" + originOp.getOperadorId() +"originOp.getTipoModelo()"+originOp.getTipoModelo());
		OperatorStore originOpTnd = null;
		
		if(originOp.getOperadorId() != null)
			originOpTnd = operatorDAO.getOperatorStoreByCodStore(sessionOpera,originOp.getOperadorId(),codStore,originOp.getTipoModelo());
		
		if(originOpTnd == null)
			originOpTnd = new OperatorStore();
		
		originOpTnd.setCodTienda(codStore);
		originOpTnd.setTipoModelo(originOp.getTipoModelo());
		originOpTnd.setIpTienda(originOp.getIpTienda());
		originOpTnd.setStatus(originOp.getStatusTienda());
		originOpTnd.setOperadorId(originOp.getOperadorId());
		originOpTnd.setSubscribe(originOp.getSubscribeTienda());
		originOpTnd.setFecha(new Date());
		//return originOpTnd;
		
		if(originOpTnd.getOperadorId() == null)
		{
			Operator opNew = operatorDAO.getOperatorsByIdentityDocument(sessionOpera, originOp.getIdentityDocument().toString());
			logger.info("El operador buscado, opNew:" + opNew);
			if(opNew != null)
			{
				originOpTnd.setOperadorId(opNew.getOperadorId());
				logger.info("NUEVA RELACION, idOperador: " + originOpTnd.getOperadorId());
			}
		}
		
		List<OperatorStore> listTndOpe = originOp.getTiendas();
		Integer itemTndOpe = null;
		int i = 0;
		for(OperatorStore item: listTndOpe)
		{
			if(item.getCodTienda().equals(originOpTnd.getCodTienda()) && item.getOperadorId().equals(originOpTnd.getOperadorId())) {
				
				item.setCodTienda(originOpTnd.getCodTienda());
				item.setIpTienda(originOpTnd.getIpTienda());
				item.setOperadorId(originOpTnd.getOperadorId());
				item.setStatus(originOpTnd.getStatus());
				item.setSubscribe(originOpTnd.getSubscribe());
				item.setFecha(originOpTnd.getFecha());
				itemTndOpe = i;
				//item.setStatus(STATUS_EN_PROCESO);
			}
			i++;
		}
		
		if(itemTndOpe == null) {
			int j = 0;
			itemTndOpe = 0;
			// no esta la tienda
			listTndOpe.add(originOpTnd);
			originOp.setTiendas(listTndOpe);
			itemTndOpe = listTndOpe.indexOf(originOpTnd);			
		}
		
		return itemTndOpe;		
	}
	
	protected Operator updateOperator(Operator originOp, Operator operator) {
		
		if(originOp == null)
			originOp = new Operator();
		//else 
			//originOp.setOperadorId(operator.getOperadorId());
		
		originOp.setOptionsLevel(operator.getOptionsLevel());
		originOp.setName(operator.getName());
		originOp.setOperatorBirthDate(operator.getOperatorBirthDate());
		originOp.setStatus(operator.getStatus());
		originOp.setSubscribe(operator.getSubscribe());
		originOp.setIndSegMejorada(operator.getIndSegMejorada());
		originOp.setCodTienda(operator.getCodTienda());
		originOp.setIpTienda(operator.getIpTienda());
		originOp.setStatusTienda(operator.getStatusTienda());
		originOp.setSubscribeTienda(operator.getSubscribeTienda());
		originOp.setCodNegocio(operator.getCodNegocio());
		originOp.setCodNegocioAnt(operator.getCodNegocioAnt());
		originOp.setGrupo(operator.getGrupo());
		originOp.setUsuario(operator.getUsuario());
		originOp.setNivelAut(operator.getNivelAut());
		originOp.setNivelAutSO(operator.getNivelAutSO());
		originOp.setIdModOpera(operator.getIdModOpera());
		originOp.setIdModOperaSO(operator.getIdModOperaSO());
		originOp.setTipoModelo(operator.getTipoModelo());
		originOp.setIdentityDocument(operator.getIdentityDocument());
		originOp.setNameAce(operator.getNameAce());
		originOp.setApellidoM(operator.getApellidoM());
		originOp.setApellidoP(operator.getApellidoP());
		originOp.setIdReg(operator.getIdReg());
		originOp.setIniciales(operator.getIniciales());
		
		/************************************************************ INDICATS INDICATSOPC **********************************************************************/
		
		Collections.sort(operator.getAuthorizations(), new Comparator() {
			public int compare(Object o1, Object o2) {
				return Integer.valueOf(((AuthorizesOPC)o1).getIndicatOPC().getIdIndicatOPC()).compareTo(Integer.valueOf(((AuthorizesOPC)o2).getIndicatOPC().getIdIndicatOPC()));
			}
		});
		if(originOp.getAuthorizations() == null || originOp.getAuthorizations().isEmpty())
			originOp.setAuthorizations(new ArrayList());
		else {
			Collections.sort(originOp.getAuthorizations(), new Comparator() {
				public int compare(Object o1, Object o2) {
					return Integer.valueOf(((AuthorizesOPC)o1).getIndicatOPC().getIdIndicatOPC()).compareTo(Integer.valueOf(((AuthorizesOPC)o2).getIndicatOPC().getIdIndicatOPC()));
				}
			});
		}
		
		int size = operator.getAuthorizations().size();
		for(int i = 0; i < size; i++){
			if(originOp.getAuthorizations().size() <= i){
				originOp.getAuthorizations().add(operator.getAuthorizations().get(i));
			} else {
				originOp.getAuthorizations().get(i).setValue(operator.getAuthorizations().get(i).getValue());
			}
		}
		
		/*********************************************************** NIVELES DE AUTORIZACION *******************************************************************/
		
		Collections.sort(operator.getLevelAuthorizations(), new Comparator() {
			public int compare(Object o1, Object o2) {
				return Integer.valueOf(((LevelAuthorizes)o1).getIdNvautoriza()).compareTo(Integer.valueOf(((LevelAuthorizes)o2).getIdNvautoriza()));
			}
		});
		if(originOp.getLevelAuthorizations() == null || originOp.getLevelAuthorizations().isEmpty())
			originOp.setLevelAuthorizations(new ArrayList());
		else {
			Collections.sort(originOp.getLevelAuthorizations(), new Comparator() {
				public int compare(Object o1, Object o2) {
					return Integer.valueOf(((LevelAuthorizes)o1).getIdNvautoriza()).compareTo(Integer.valueOf(((LevelAuthorizes)o2).getIdNvautoriza()));
				}
			});
		}
		
		size = operator.getLevelAuthorizations().size();
		for(int i = 0; i < size; i++){
			if(originOp.getLevelAuthorizations().size() <= i){
				originOp.getLevelAuthorizations().add(operator.getLevelAuthorizations().get(i));
			} else {
				originOp.getLevelAuthorizations().get(i).setValue(operator.getLevelAuthorizations().get(i).getValue());
			}
		}
		
		
		return originOp;
	}
	
	protected void iniciarSesion(String name1, String name2) {
		while (sessionOpera == null) {
			try {
				sessionOpera = HibernateSessionFactoryContainer.getSessionFactory(name1).openSession();
				
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (sessionOpera == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}
		while (sessionSaadmin == null) {
			try {
				sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory(name2).openSession();
				
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

	private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
	

	protected Boolean process(Operator operator, OperatorWrapper operatorWrapper) {
		Boolean result = Boolean.FALSE;
		try {
			operatorWrapper.setIpStore(operator.getIpTienda());
			operatorWrapper.setTipoModelo(operator.getTipoModelo());
			operatorWrapper.setName(operator.getNameAce());
			operatorWrapper.setOperatorId(operator.getOperadorId().toString());
			operatorWrapper.setOperatorBirthDate(operator.getOperatorBirthDate());
			//operatorWrapper.setStatus(operator.getStatus().toString());
			operatorWrapper.setStatus(operator.getStatusTienda().toString()); // Tomamos el de las tiendas
			//operatorWrapper.setSubscribe(operator.getSubscribe().toString());
			operatorWrapper.setSubscribe(operator.getSubscribeTienda().toString()); //Tomamos el de las tiendas
			operatorWrapper.setIndSegMejorada(operator.getIndSegMejorada() == null ? 0 : operator.getIndSegMejorada());
			operatorWrapper.setIdentityDocument(operator.getIdentityDocument());
			
			List<StringBuffer> list = new ArrayList<StringBuffer>();
			/** Indicat0 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 16,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat1 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 16,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat2 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat3 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat4 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat5 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat6 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat7 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat8 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat9 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat10 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat11 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat12 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat13 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat14 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat15 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat16 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat17 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat18 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			/** Indicat19 **/
			list.add(new StringBuffer(StringUtils.leftPad(ArmsServerConstants.Communication.EMPTY_STR, 8,
					ArmsServerConstants.Communication.CERO)));
			
			
			/** set the indicats **/
			for (int i = 0; i < operator.getAuthorizations().size(); i++) {
				/** by default the "bits" are off, then only set on the IndActivo = 1 **/
				if (((AuthorizesOPC) operator.getAuthorizations().get(i)).getValue().toString().equals("1")) {
					int pos = ((AuthorizesOPC) operator.getAuthorizations().get(i)).getIndicatOPC().getBitPos();
					int numIndicat = ((AuthorizesOPC) operator.getAuthorizations().get(i)).getIndicatOPC().getIndicat()
							.getIndicat();
					StringBuffer indicat = list.get(numIndicat);
					setIndicat(indicat, pos, "1");
					list.set(numIndicat, indicat);
				}
			}

			operatorWrapper.setIndicats(list);
			
			StringBuffer levelAut = new StringBuffer();
			int size = operator.getLevelAuthorizations().size();
			Collections.sort(operator.getLevelAuthorizations(), new Comparator() {
				public int compare(Object o1, Object o2) {
					return Integer.valueOf(((LevelAuthorizes)o1).getIdNvautoriza()).compareTo(Integer.valueOf(((LevelAuthorizes)o2).getIdNvautoriza()));
				}
			});
			for(int i = 0; i < size; i++){
				levelAut.append(((LevelAuthorizes)operator.getLevelAuthorizations().get(i)).getValue());
			}
			if(operator.getGrupo()!=null&&!operator.getGrupo().toString().isEmpty()){
				String grupo = operator.getGrupo().toString();
				while(grupo.length()<3)
					grupo="0"+grupo;
				levelAut.append(grupo);
			}
			if(operator.getUsuario()!=null&& !operator.getUsuario().toString().isEmpty()){
				String usuario = operator.getUsuario().toString();
				while(usuario.length()<3)
					usuario="0"+usuario;
				levelAut.append(usuario);
			}			
			operatorWrapper.setLevelAuthorizations(levelAut.toString());
            if (operator.getNivelAut() != null) {
                operatorWrapper.setNivelAut(operator.getNivelAut().toString());
            }
            if (operator.getNivelAutSO() != null) {
                operatorWrapper.setNivelAutSO(operator.getNivelAutSO().toString());
            }
			result = Boolean.TRUE;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	protected void setIndicat(StringBuffer str, int pos, String value) {
		try {
			str.replace(pos, pos + 1, value);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}
	
	protected boolean sendToLocal(StringBuffer data, Store store){
		List list = Arrays.asList(p.split(data.toString()));
		Frame frame = new Frame(list,
				ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
				ArmsServerConstants.Communication.FRAME_SEP);
		if (frame.loadData()) {
			boolean send = sendFrameToLocal(frame, store);
			if (send) {
				logger.info("Archivo enviado correctamente.");
				return true;
			} else {
				logger.error("Error al enviar al server.");
			}
		}
		return false;
	}
	protected boolean sendFrameToLocal(Frame frame, Store tienda) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected())
				connectClient(properties, tienda);
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
}
