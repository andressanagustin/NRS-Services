package com.allc.arms.server.processes.operator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.operator.AuthorizesOPC;
import com.allc.arms.server.persistence.operator.IndicatOPC;
import com.allc.arms.server.persistence.operator.LevelAuthorizes;
import com.allc.arms.server.persistence.operator.Operator;
import com.allc.arms.server.persistence.operator.OperatorDAO;
import com.allc.arms.server.persistence.operator.OperatorStore;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.server.processes.utils.AbstractProcessPrincipal;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;


/**
 * proceso encargado de leer archivos generado por el agente y cargar en la base de datos de la suit
 * carga:
 * 		Operadores (operac_ec.op_operador)
 * 		Usuarios (SAADMIN.us_usuarios)
 * 		OperadorTienda (operac_ec.op_opetnd)
 * 		UsuariosTienda (SAADMIN.us_usutnd)
 * 		IndicarPorOperadores (operac_ec.OP_OPERAMDA)
 * 		NivelesAutorizacionPorOperadores (operac_ec.OP_OPERANVA)
 * 
 * @author Andres Sanagustin
 *
 */
public class OperatorLoaderProcess extends AbstractProcessPrincipal {
	protected static Logger log = Logger.getLogger(OperatorLoaderProcess.class.getName());
	protected PropFile prop = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	public boolean isEnd = false;
	protected Session session = null;
	private Session sessionSaadmin = null;
	protected boolean finished = false;
	public final Integer STATUS_INICIAL = new Integer(0);
	public final Integer STATUS_BUSCAR = new Integer(1);
	public final Integer STATUS_EN_PROCESO = new Integer(2); //CUANDO ESPERA EL ARCHIVO PARA PROCESAR
	public final Integer STATUS_ACTUALIZADO = new Integer(3);
	public final Integer STATUS_PROCESANDO = new Integer(4); //CUANDO ENCUENTRA EL ARCHIVO Y EMPIEZA A PROCESAR
	public final Integer STATUS_ERROR = new Integer(9);
	protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	protected ConnSocketClient socketClient = null;
	protected String descriptorProceso = "OP_LOAD_P";
	DateFormat datosFec = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	OperatorDAO opDao;
	StoreDAO storeDao;
	
	/** Definimos si se cargan modelos de los archivos recibidos o de la base de datos del modelo **/
	//private boolean cargaModelos = false; // true/1 = carga de archivos ; false/0 = carga de la base de datos.
	private final boolean cargaModelos = prop.getInt("iniLoadOperator.cargaModelos") != 0;
	private int status = 5;
	private int subscribe = 1;
	private String pathFile;
	private int download = 1;
        
    private final String[] character = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "Ñ", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
	
	protected void iniciarSesion() {
		while (session == null && !isEnd) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory("Opera").openSession();
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

	private void iniciarSaadminSesion() {
		while (sessionSaadmin == null && !isEnd) {
			try {
				sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionSaadmin == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}
	
	public void run() {		
		//meter en while cada 5min parametro.		
		
        while (!isEnd && isPrincipal()) {
                if(isPrincipal())
                    {
			log.info("Operator Load Process iniciada.");
			try {
				/** open a session **/
				iniciarSesion();
				iniciarSaadminSesion();
				opDao = new OperatorDAO();
				storeDao = new StoreDAO();
				String storeNumber;
				log.info("Buscando Tiendas para inicializar operadores...");
				boolean connectionError = false;
				boolean guardarCambios = false;
				
				List<String> listTiendasLoad = new ArrayList<>();
				List<String> listTiendasProces = new ArrayList<>();
				//validar por estado de tienda primero y salir si no hay para actualizar o para recibir.
				List<Store> storeLoadIniOperadores = storeDao.getStoreLoadOperator(sessionSaadmin,STATUS_BUSCAR);
				log.info("Tiendas para mandar trama y inicializar.");
				if (storeLoadIniOperadores != null && !storeLoadIniOperadores.isEmpty()) {
					log.info("hay Tiendas: " + storeLoadIniOperadores.size());
					//buscar en tiendas si hay que actualizar
					//validar en tabla tienda si hay que buscar archivo.
					for(Store item: storeLoadIniOperadores)
					{
						Store storeToSend = item;
						//String ipTienda = item.getIp();
						storeNumber = storeToSend.getKey().toString();
						while(storeNumber.length() < 3)
							storeNumber = "0" + storeNumber;
						 
						StringBuffer data = new StringBuffer();
						data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
								.append(ArmsServerConstants.Process.LOAD_INIT_OPERATOR_OPERATION)
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(storeNumber)
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
								.append(ArmsServerConstants.Communication.FRAME_SEP)
								.append(ArmsServerConstants.Communication.TEMP_CONN)
								.append(ArmsServerConstants.Communication.FRAME_SEP)
								.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
						
						//Store storeToSend = storeDAO.getStoreByCode(sessionSaadmin, Integer.valueOf(operadorTienda.getCodTienda()));
						log.info("Ip: " + storeToSend.getIp());
						connectionError = !sendToLocal(data, storeToSend);
						closeConnection();
						log.info("Respuesta Socket: " + connectionError);
						
						if(!connectionError) {
							//si retorna true
							listTiendasLoad.add(storeNumber);
							log.info("Entro a guardar."+STATUS_EN_PROCESO);
							item.setEstIniLoadOpe(STATUS_EN_PROCESO); 
							guardarCambios = true;
						}
						
					}
					
				}
				
				List<Store> storeProcesarArchivos = storeDao.getStoreLoadOperator(sessionSaadmin,STATUS_EN_PROCESO);
				log.info("Tiendas para procesar archivos.");
				if (storeProcesarArchivos != null && !storeProcesarArchivos.isEmpty()) {
					log.info("hay Archivos: " + storeProcesarArchivos.size());
					for(Store itemProceso: storeProcesarArchivos)
					{
						storeNumber = itemProceso.getKey().toString();;
						while(storeNumber.length() < 3)
							storeNumber = "0" + storeNumber;
						log.info("TIENDA " + storeNumber);
						pathFile = prop.getObject("SUITE_ROOT") + File.separator + "allc_dat" + File.separator + "out" + File.separator + storeNumber+ File.separator+"iniOpe";
						File carpeta = new File(pathFile);						
						String[] listFile = carpeta.list();
						if (listFile == null || listFile.length == 0) {
							log.info("No hay Archivos para procesar dentro de la carpeta " + pathFile);
						    //si no hay archivos y paso mas de 5 min cambiar estado
							Date actual = new Date();
							Date fechaBase = itemProceso.getFecEstIniLoadOpe();
							if(fechaBase!= null)
							{
								long diff = actual.getTime() - fechaBase.getTime();
								long min = diff / (1000*60);
								if(min >= 5)
								{
									itemProceso.setEstIniLoadOpe(STATUS_INICIAL);
									guardarCambios = true;
									log.info("Reiniciamos estado de inicializar la tienda.");
								}
							}							
							//logear warning							
							UtilityFile.createWriteDataFile(getEyesFileName(), descriptorProceso+"|"+prop.getHostName()+"|3|"+prop.getHostAddress()+"|"+storeNumber+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se encontro el archivo de la tienda: "+storeNumber+", en la ruta: "+pathFile+".\n", true);
						}
						else {
							File file1 = new File(pathFile+File.separator+storeNumber+"-1-OperaLoadData");
							File file2 = new File(pathFile+File.separator+storeNumber+"-2-OperaLoadData");
							if(file1.exists() && file2.exists()) {
								log.info("estan ambos archivos en el server, INICIA proceso.");
								itemProceso.setEstIniLoadOpe(STATUS_PROCESANDO);
								storeDao.updateStore(sessionSaadmin, itemProceso);
								ProcessFileOperator processFileOperator = new ProcessFileOperator(itemProceso.getKey(), pathFile);
								processFileOperator.start();
								guardarCambios = true;
							}else {
								log.info("No termino de subir ambos archivos, esperamos.");
							}
						}
					}
				}
				if (guardarCambios) {
					if (storeLoadIniOperadores != null && !storeLoadIniOperadores.isEmpty())
						storeDao.updateStoreEstado(sessionSaadmin,storeLoadIniOperadores);
					log.info("Se solicito archivos de las siguientes tiendas: " + listTiendasLoad.toString());
					log.info("Se pocesaron archivos de las siguientes tiendas: " + listTiendasProces.toString());
					UtilityFile.createWriteDataFile(getEyesFileName(), descriptorProceso+"|"+prop.getHostName()+"|3|"+prop.getHostAddress()+"|000|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Se pidieron archivos de las siguientes tiendas: "+listTiendasLoad.toString()+", se procesaron archivos de las siguientes tiendas: "+listTiendasProces.toString()+".\n", true);
				}			
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					UtilityFile.createWriteDataFile(getEyesFileName(), "descriptorProceso|"+prop.getHostName()+"|3|"+prop.getHostAddress()+"|000|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al correr el proceso "+descriptorProceso+".\n", true);
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}	
			} finally {
				if (session != null && sessionSaadmin != null) {
					session.close();
					session = null;
					sessionSaadmin.close();
					sessionSaadmin=null;
				}
			}
                    }
			try {
				Thread.sleep(prop.getLong("iniLoadOperator.timesleep"));
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
			
		}
		finished = true;
	}
	
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		//closeConnection();
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo OperatorDownloadProcess...");
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
		log.info("Finalizó el Proceso de Descargas de Operadores.");
		return true;
	}
	
	private String getEyesFileName() {
		return prop.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	protected boolean sendToLocal(StringBuffer data, Store store){
		List list = Arrays.asList(p.split(data.toString()));
		Frame frame = new Frame(list,
				ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
				ArmsServerConstants.Communication.FRAME_SEP);
		if (frame.loadData()) {
			boolean send = sendFrameToLocal(frame, store);
			if (send) {
				log.info("El pedido de generacion de archivo se genero satisfactoriamente.");
				return true;
			} else {
				log.error("Error al enviar al server.");
			}
		}
		return false;
	}
	
	protected boolean sendFrameToLocal(Frame frame, Store tienda) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = prop.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected())
				connectClient(prop, tienda);
			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
			log.info("Trama a enviar: " + trama);
			//log.info("Socket ip:" + socketClient.getIpServer() +", port:" + socketClient.getPortServer());
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
	
	protected boolean connectClient(PropFile properties, Store tienda) {
		if (socketClient == null || !socketClient.isConnected()) {
			log.info("Store IP: " + tienda.getIp()+", port: " + properties.getInt("clientSocket.port"));
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(tienda.getIp());
			socketClient.setPortServer(properties.getInt("clientSocket.port"));
			socketClient.setRetries(2);
			socketClient.setTimeOutConnection(properties.getInt("serverSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("serverSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("serverSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}
	
	protected void closeConnection() {
		if (socketClient != null)
		{
			socketClient.closeConnection();
			socketClient = null;
		}
	}
}
