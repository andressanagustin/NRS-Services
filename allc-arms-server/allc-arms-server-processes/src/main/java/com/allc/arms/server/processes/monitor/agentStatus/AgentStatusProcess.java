package com.allc.arms.server.processes.monitor.agentStatus;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.equipo.EquipoDAO;
import com.allc.arms.server.persistence.monitor.Monitor;
import com.allc.arms.server.persistence.monitor.MonitorDAO;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.server.processes.utils.AbstractProcessPrincipal;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * Proceso encargado de mandar una trama al agente para ver si esta online o no
 * 
 *
 */
public class AgentStatusProcess extends AbstractProcessPrincipal{
	
	private Logger logger = Logger.getLogger(AgentStatusProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	private Session sessionEyes = null;
	private Session sessionSaadmin = null;
	protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	protected ConnSocketClient socketClient = null;
	public boolean isEnd = false;
	
	public void run() {
		List listToSend;
		StringBuilder message;
		Frame frameToAgent;
		logger.info("Iniciando AgentStatus...");
		
		while (!isEnd) {
            if(isPrincipal()){
				iniciarSesion("Eyes", "Saadmin");
				logger.info("Obtiene las tiendas");
				StoreDAO storeDAO = new StoreDAO(); //Obtengo tiendas
				boolean connectionError = false;
				List stores = storeDAO.getAllActiveStores(sessionSaadmin);
				if (stores != null && !stores.isEmpty()) {
					Iterator itStore = stores.iterator();
					while (itStore.hasNext()) {
						try {
							Store store = (Store) itStore.next();
							String ip = store.getIp();
							int agentStatus = 0;
							logger.info("Tienda " + store.getStoreId() + " IP " + store.getIp());
							if(ip != null && connectClientIsUp(ip)){
								logger.info("CONECTA");
								String today = ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date());
								message = new StringBuilder();
								message.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(ArmsServerConstants.Process.STATUS_AGENT)
										.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
										.append(ArmsServerConstants.Communication.FRAME_SEP).append("")
										.append(ArmsServerConstants.Communication.FRAME_SEP).append("")
										.append(ArmsServerConstants.Communication.FRAME_SEP).append(today)
										.append(ArmsServerConstants.Communication.FRAME_SEP).append("");
								logger.info("Trama a enviar: " + message);
								listToSend = Arrays.asList(p.split(message.toString()));
								frameToAgent = new Frame(listToSend, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
														ArmsServerConstants.Communication.FRAME_SEP);
								if (frameToAgent.loadData()) {
									Frame respuesta = sendTramaToAgent(ip, frameToAgent); 
									if (respuesta != null && respuesta.getStatusTrama() == 0) {
										agentStatus = 1;
										if (respuesta.getBody().size() > 0) {
											if (!updateDateController(store.getKey(), ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.parse((String) respuesta.getBody().get(1)))){
												logger.info("NO SE PUDO ACTUALIZAR LA FECHA");
											}	
										} else {
											logger.info("NO RECIBE HORA DEL CONTROLADOR");
										}
									}
									else {
										logger.info("NO RECIBE RESPUESTA O EL ESTADO ES 0");
									}
								}
								else {
									logger.info("Trama mal armada. No envia");
								}
								//Actualiza tienda
								logger.info("Actualiza equipos de tienda " +  store.getKey() + " - Status new " + agentStatus);
								EquipoDAO equipoDAO = new EquipoDAO();
								equipoDAO.updateIndOnlineByIdStoreAndIp(sessionEyes, store.getKey(), store.getIp(), agentStatus);
								closeConnection();
							}
							else {
								//SI NO CONECTA LO DEJA OFFLINE
								logger.info("No conecta, Actualiza equipos de tienda " +  store.getKey() + " - Status new " + agentStatus);
								EquipoDAO equipoDAO = new EquipoDAO();
								equipoDAO.updateIndOnlineByIdStoreAndIp(sessionEyes, store.getKey(), store.getIp(), agentStatus);
							}	
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
							closeConnection();
						}
					}
				}
				else logger.info("No se encontraron tiendas");
				finalizaSesion();
            }
            try {
                Thread.sleep(properties.getLong("agentStatus.timesleep"));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                closeConnection();
            }
        }
	 }
	
	/**
	 * Método que prueba si la conexión esta levantada
	 * 
	 * @return true si se conecta, false si no se conecta.
	 */
    protected boolean connectClientIsUp(String ip) {
    	final int TIMEOUT = 10000;
    	try {
    		SocketAddress sockaddr = new InetSocketAddress(ip.trim(), properties.getInt("clientSocket.port"));
    		Socket socket = new Socket();
    		socket.connect(sockaddr, TIMEOUT);
    		socket.close();
        	return true;
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
        	return false;
		}
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
			logger.info("Intenta Conectar con agente IP: " + socketClient.getIpServer() +", puerto: " + socketClient.getPortServer());
			return socketClient.connectSocket(); //Pruebo conectar una sola vez
	}
		
	protected void closeConnection() {
		if (socketClient != null)
			socketClient.closeConnection();
	}
		
	protected Frame sendTramaToAgent(String ip, Frame frame) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
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
						logger.info("Respuesta recibida: " + frameRpta.toString());
						frameRpta.loadData();
						return frameRpta;
					}
				}
			} else {
				logger.info("NO ENVIA LA TRAMA");
				socketClient.setConnected(false);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			socketClient.setConnected(false);
		}
		return null;
	}
		
	protected void iniciarSesion(String name1, String name2) {
		while (sessionEyes == null) {
			try {
				sessionEyes = HibernateSessionFactoryContainer.getSessionFactory(name1).openSession();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (sessionEyes == null)
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
	
	protected void finalizaSesion() {
		if (sessionEyes != null) {
            sessionEyes.close();
            sessionEyes = null;
        }
		if (sessionSaadmin != null) {
            sessionSaadmin.close();
            sessionSaadmin = null;
        }
	}
	
	protected boolean updateDateController(Integer desClave, Date fecha) {
		try {
			MonitorDAO monitorDAO = new MonitorDAO();
			Monitor monitor = monitorDAO.getMonitorById(sessionEyes, desClave, "CC");
	    	if (monitor == null) {
	    		logger.info("NO ENCUENTRA MONITOR");
	    		return false;
	    	}
	    	monitor.setFechaControlador(fecha);
	    	return monitorDAO.insertMonitor(sessionEyes, monitor);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}


	@Override
	public boolean shutdown(long arg0) {
		isEnd = true;
		finalizaSesion();
		closeConnection();
		logger.info("Finaliza el Proceso AgentStatus.");
		return true;
	}

}
