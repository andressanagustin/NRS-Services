/**
 * 
 */
package com.allc.arms.server.processes.params;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.params.Param;
import com.allc.arms.server.params.ParamValue;
import com.allc.arms.server.params.ParamsDAO;
import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * @author gustavo
 *
 */
public class LoadParamsProcess extends AbstractProcess {
	protected Logger log = Logger.getLogger(LoadParamsProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	private static Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	public boolean isEnd = false;
	private Transaction tx;
	protected Session session = null;
	protected boolean finished = false;
	protected ConnSocketClient socketClient;
	StoreDAO storeDAO = new StoreDAO();

	public void run() {
		log.info("Iniciando LoadParamsProcess...");
		boolean error = false;
		ParamsDAO paramsDAO = new ParamsDAO();
		String store = properties.getObject("eyes.store.code") == null ? "0" : properties.getObject("eyes.store.code"); // server local
		BigInteger idBsnGp = properties.getObject("eyes.store.code.group") == null ? BigInteger.ZERO 
				: new BigInteger(properties.getObject("eyes.store.code.group")); // server regional
		List params = null;
		try {
			//DUERME EL PROCESO POR SI SE RECARGA EL CENTRAL AL MISMO TIEMPO
			if (properties.getObject("load.param.sleep") != null) {
				Thread.sleep(properties.getInt("load.param.sleep"));
			} else {
				Thread.sleep(10000);
			}
			iniciarSesion();
			//si es regional
			if (idBsnGp.compareTo(BigInteger.ZERO) > 0 && Integer.valueOf(store) == 0) {
				log.info("carga de parametros a nivel regional!");
				for (int i = 1; i <= 4; i++) {
					if (i == 3) { //AGENTE: los carga directamente desde el agente al central
						continue;
					}
					StringBuffer data = new StringBuffer();
					data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
							.append(ArmsServerConstants.Process.LOAD_PARAMS_PROCESS)
							.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
							.append(ArmsServerConstants.Communication.FRAME_SEP).append("0")
							//.append(ArmsServerConstants.Communication.FRAME_SEP).append(idBsnGp)
							.append(ArmsServerConstants.Communication.FRAME_SEP)
							.append(ArmsServerConstants.Communication.TEMP_CONN)
							.append(ArmsServerConstants.Communication.FRAME_SEP)
							.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
							.append(ArmsServerConstants.Communication.FRAME_SEP).append(i)
							//para id de grupo
							.append(ArmsServerConstants.Communication.FRAME_SEP).append(idBsnGp) 
							.append(ArmsServerConstants.Communication.FRAME_SEP);
							

					List list = Arrays.asList(p.split(data.toString()));
					Frame frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
							ArmsServerConstants.Communication.FRAME_SEP);
					if (frame.loadData()) {
						log.info("frame cargado");
						log.info("size header del frame -->"+ frame.getHeader().size());
						log.info("size body del frame -->"+ frame.getBody().size());
						log.info("contenido body:");
						for (int y=0; y<frame.getBody().size(); y++) {
							log.info(frame.getBody().get(y));
						}
						
						log.info("enviar parametro a tienda 0");
						Frame respuesta = sendParams(frame, 0);
						closeClient();
						log.info("parametro enviado!, conexion socket tienda 0 cerrada");
						if (respuesta != null) {
							log.info("cantidad de paramtros recibidos --> "+respuesta.getBody().size());
							String param = null;
							Iterator it = respuesta.getBody().iterator();
							initTx();
							log.info("leyendo parametros...");
							while (it.hasNext()) {
								boolean existParam = true;
								param = (String) it.next();
								log.info(param);
								String paramArray[] = param.split("=");
								log.info("leyendo paramValueAux: idBsnGp --> "+idBsnGp+" , clave --> "+paramArray[0]);
								ParamValue paramValueAux = paramsDAO.getParValSpecifiByClaveGroup(session, idBsnGp, i, paramArray[0]);
								if (paramValueAux == null) {
									//Si no existe parametro regional busca la cabecera para crearlo
									Param paramAux = paramsDAO.getParam(session, i, paramArray[0]);
									if (paramAux != null) {
										paramValueAux = new ParamValue();
										paramValueAux.setNivelGrupo(idBsnGp);
										paramValueAux.setParam(paramAux);
									} else {
										log.info("NO EXISTE PARAM CARGADO, NO CREA PARAMETRO");
										existParam = false;
									}

								}
								if (existParam) {
									if(paramArray.length > 1){
										if (paramValueAux.getNivelGrupo() == null || paramValueAux.getNivelGrupo().compareTo(idBsnGp)!=0) {
											Param paramAux = paramsDAO.getParam(session, i, paramArray[0]);
											paramValueAux = new ParamValue();
											paramValueAux.setNivelGrupo(idBsnGp);
											paramValueAux.setParam(paramAux);
										}
										paramValueAux.setEstado(1); //LO DEJA ACTIVO
										paramValueAux.setValor(paramArray[1]);
										session.saveOrUpdate(paramValueAux);
										if(params == null)
											params = new ArrayList();
										params.add(paramValueAux);
									}
								}
							}
							tx.commit();
						} else {
							error = true;
							UtilityFile
									.createWriteDataFile(getEyesFileName(),
											"LOAD_PARAMS_P|" + properties.getHostName() + "|1|"
													+ properties.getHostAddress() + "|" + store
													+ "|WAR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
															.format(new Date())
													+ "|No se pudo cargar los parametros.\n",
											true);
							log.error("Error durante la carga de los parametros.");
						}
					}
				}
			}
			// si es local invocar operacion sin ambito para que retorne todo
			// los param para la tienda
			else if (Integer.valueOf(store) > 0 && idBsnGp.compareTo(BigInteger.ZERO) == 0) {
				log.info("carga de parametros a nivel de tienda!");
				for (int i = 1; i <= 4; i++) {
					if (i == 3) { //AGENTE: los carga directamente desde el agente al central
						continue;
					}
					StringBuffer data = new StringBuffer();
					data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
							.append(ArmsServerConstants.Process.LOAD_PARAMS_PROCESS)
							.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
							.append(ArmsServerConstants.Communication.FRAME_SEP).append(store)
							.append(ArmsServerConstants.Communication.FRAME_SEP)
							.append(ArmsServerConstants.Communication.TEMP_CONN)
							.append(ArmsServerConstants.Communication.FRAME_SEP)
							.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
							.append(ArmsServerConstants.Communication.FRAME_SEP).append(i)
							.append(ArmsServerConstants.Communication.FRAME_SEP);

					List list = Arrays.asList(p.split(data.toString()));
					Frame frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
							ArmsServerConstants.Communication.FRAME_SEP);
					if (frame.loadData()) {
						Frame respuesta = sendParams(frame, 0);
						closeClient();
						if (respuesta != null) {
							String param = null;
							Iterator it = respuesta.getBody().iterator();
							initTx();
							while (it.hasNext()) {
								boolean existParam = true;
								param = (String) it.next();
								String paramArray[] = param.split("=");
								ParamValue paramValueAux = paramsDAO.getParValSpecifiByClave(session, Integer.valueOf(store), i, paramArray[0]);
								if (paramValueAux == null) {
									//Si no existe parametro regional busca la cabecera para crearlo
									Param paramAux = paramsDAO.getParam(session, i, paramArray[0]);
									if (paramAux != null) {
										paramValueAux = new ParamValue();
										paramValueAux.setTienda(Integer.valueOf(store));
										paramValueAux.setParam(paramAux);
									} else {
										log.info("NO EXISTE PARAM CARGADO, NO CREA PARAMETRO");
										existParam = false;
									}

								}
								if (existParam) {
									if(paramArray.length > 1){
										if(paramValueAux.getTienda() == null || paramValueAux.getTienda().compareTo(Integer.valueOf(store))!=0){
											Param paramAux = paramsDAO.getParam(session, i, paramArray[0]);
											paramValueAux = new ParamValue();
											paramValueAux.setTienda(Integer.valueOf(store));
											paramValueAux.setParam(paramAux);
										}
										paramValueAux.setEstado(1); //LO DEJA ACTIVO
										paramValueAux.setValor(paramArray[1]);
										session.saveOrUpdate(paramValueAux);
										if(params == null)
											params = new ArrayList();
										params.add(paramValueAux);
									}
								}
							}
							tx.commit();
						} else {
							error = true;
							UtilityFile
									.createWriteDataFile(getEyesFileName(),
											"LOAD_PARAMS_P|" + properties.getHostName() + "|1|"
													+ properties.getHostAddress() + "|" + store
													+ "|WAR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
															.format(new Date())
													+ "|No se pudo cargar los parametros.\n",
											true);
							log.error("Error durante la carga de los parametros.");
						}
					}
				}
			} else
			{
				log.info("carga de parametros a nivel central!");
				//jjg, carga de parametros generales
				params = paramsDAO.getParamsListStoreGroup(session, -1, -1, ArmsServerConstants.AmbitoParams.ARMS_SERVER_PARAMS);
			}
			String idBsnGpString = properties.getObject("eyes.store.code.group"); //LO DEJO EN STRING PARA QUE TOME LOS 0 ADELANTE
			if (!error) {
				properties.clear(); //LIMPIO LOS PARAMETROS
				Iterator itParams = params.iterator();
				while (itParams.hasNext()) {
					ParamValue paramValue = (ParamValue) itParams.next();
					if(paramValue.getValor() != null)
						properties.setObject(paramValue.getParam().getClave(), paramValue.getValor());
				}
				//SE DEJAN LOS COD. DE TIENDA Y COD. REGIONAL QUE HABIA EN EL PROPERTIES
				if (idBsnGpString != null) {
					properties.setObject("eyes.store.code.group", idBsnGpString);
				}
				
				properties.setObject("eyes.store.code", store);
				properties.storeToFile(ArmsServerConstants.PROP_FILE_NAME);
				UtilityFile.createWriteDataFile(getEyesFileName(),
						"LOAD_PARAMS_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store
								+ "|STR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Cargando par�metros de tienda: " + store + " y Ambito: "
								+ ArmsServerConstants.AmbitoParams.ARMS_SERVER_PARAMS + ".\n",
						true);

				properties.setObject("server.params.loaded", "true");
				log.info("Los parámetros han sido cargados en forma exitosa");
				UtilityFile.createWriteDataFile(getEyesFileName(),
						"LOAD_PARAMS_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store
								+ "|END|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Parametros cargados correctamente.\n",
						true);
			} else {
				properties.setObject("server.params.loaded", "true"); //DEJO ESTO PARA QUE SE EJECUTE IGUAL CON EL PROPERTIES VIEJO
				log.info("NO SE CARGAN LOS PARAMETROS YA QUE DIO ERROR, DEJA LOS QUE YA ESTAN");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				tx.rollback();
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(),
						"LOAD_PARAMS_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store
								+ "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al cargar los par�metros.\n",
						true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		} finally {
			session.close();
			session = null;
		}

		finished = true;
	}

	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	public void initTx() {
		while (tx == null || !tx.isActive()) {
			try {
				tx = session.beginTransaction();
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
	}
	
	
	private void iniciarSesion() {
		while (session == null && !isEnd) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (session == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTA EN 3 seg.");
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	protected Frame sendParams(Frame frame, Integer tienda) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("serverSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected())
				connectClient(tienda);
			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
			log.info("Trama a enviar: " + trama);
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
							return null;
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
						frameRpta.loadData();
						return frameRpta;
					}
				}
			} else {
				socketClient.setConnected(false);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			socketClient.setConnected(false);
		}
		return null;
	}

	protected boolean connectClient(Integer tienda) {

		String storeIP = storeDAO.getStoreByCode(session, tienda).getIp();
		int retries = 5;
		if (properties.getObject("load.param.retries") != null) {
			retries = properties.getInt("load.param.retries");
		}

		if (socketClient == null) {
			socketClient = new ConnSocketClient();
			log.info("IP: "+storeIP);
			socketClient.setIpServer(storeIP);
			socketClient.setPortServer(properties.getInt("serverSocket.port"));
			socketClient.setRetries(retries);
			socketClient.setTimeOutConnection(properties.getInt("serverSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("serverSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("serverSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}

	protected void closeClient() {
		if (socketClient != null && socketClient.isConnected())
			socketClient.closeConnection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.allc.core.process.AbstractProcess#shutdown(long)
	 */
	@Override
	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
