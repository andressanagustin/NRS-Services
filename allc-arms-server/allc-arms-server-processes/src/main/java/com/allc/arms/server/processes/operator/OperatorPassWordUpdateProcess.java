package com.allc.arms.server.processes.operator;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.server.persistence.supervisor.Supervisor;
import com.allc.arms.server.persistence.supervisor.SupervisorDAO;
import com.allc.arms.server.processes.utils.AbstractProcessPrincipal;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class OperatorPassWordUpdateProcess extends AbstractProcessPrincipal {

	protected static Logger log = Logger.getLogger(OperatorPassWordUpdateProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	public boolean isEnd = false;
	public final Integer PROCESAR = new Integer(1);
	public final Integer FALLO = new Integer(2);
	public final Integer ACTIVO = new Integer(4);
	protected Session session = null;
	private Session sessionSaadmin = null;
	protected ConnSocketClient socketClient = null;
	protected boolean finished = false;
	protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	
	protected void iniciarSesion() {
		while (session == null && !isEnd) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory("Opera").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (session == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	protected boolean connectClient(PropFile properties, Store tienda) {
		if (socketClient == null || !socketClient.isConnected()) {
			log.info("Store IP: " + tienda.getIp());
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
		return socketClient.connectSocketUsingRetries();
	}

	protected void closeConnection() {
		if (socketClient != null)
			socketClient.closeConnection();
	}

	public void run() {
		List list;
		Frame frame;
			
        while (!isEnd) {
			log.info("Buscando actualizacion de clave de operadoresSupervisor...");
                        if(isPrincipal()){
                            String store = properties.getObject("eyes.store.code");
                            /** open a session **/
                            iniciarSesion();
                            iniciarSaadminSesion();
                            SupervisorDAO supDao = new SupervisorDAO();
                            StoreDAO storeDAO = new StoreDAO();
                            try {
                                    List operatorPasswordChange = supDao.getSupOperatorsByStatus(session, PROCESAR);

                                    if (operatorPasswordChange != null && !operatorPasswordChange.isEmpty()) {
                                            log.info("Se actualizaran " + operatorPasswordChange.size() + " passwords.");
                                            Iterator itOperators = operatorPasswordChange.iterator();
                                            while (itOperators.hasNext()) {
                                                    Supervisor supervisor = null;
                                                    try {
                                                            supervisor = (Supervisor) itOperators.next();
                                                            UtilityFile.createWriteDataFile(getEyesFileName(),
                                                                            "OPER_SUP_UPD_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
                                                                                            + "|" + store + "|STR|"
                                                                                            + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
                                                                                                            .format(new Date())
                                                                                            + "|Iniciando procesamiento del Supervisor: "
                                                                                            + supervisor.getIdOperadorSupervisor() + ".\n",
                                                                            true);
                                                            log.debug(supervisor.toString());
                                                            StringBuilder message = new StringBuilder();

                                                            Store tienda = storeDAO.getStoreByCode(sessionSaadmin, Integer.valueOf(supervisor.getOperador().getCodTienda()));
                                                            if(tienda.getLocalServer() > 0)
                                                                    connectClient(properties, tienda);
                                                            else
                                                                    connectClient(supervisor.getOperador().getIpTienda());

                                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

                                                            // log.info("La nueva password del supervisor es: "
                                                            // + codSupervisorNew);

                                                            String today = ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date());
                                                            message.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
                                                                            .append(ArmsServerConstants.Process.OPER_SUP_UPDATE)
                                                                            .append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
                                                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                                                            .append(store)
                                                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                                                            .append(ArmsServerConstants.Communication.TEMP_CONN)
                                                                            .append(ArmsServerConstants.Communication.FRAME_SEP).append(today)
                                                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                                                            .append(supervisor.getOperador().getIdentityDocument())
                                                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                                                            .append(supervisor.getClave())
                                                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                                                            .append(supervisor.getClave128())
                                                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                                                            .append(sdf.format(supervisor.getFechaInicio()))
                                                                            .append(ArmsServerConstants.Communication.FRAME_SEP)
                                                                            .append(supervisor.getIdRegistro());
                                                            list = Arrays.asList(p.split(message.toString()));

                                                            frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
                                                                            ArmsServerConstants.Communication.FRAME_SEP);

                                                            boolean connectionError = false;

                                                            if (frame.loadData()) {

                                                                    connectionError = !sendOperatorSupervisorUpdate(supervisor.getOperador().getIpTienda(),
                                                                                    frame);
                                                            }

                                                            if (!connectionError) {
                                                                    supervisor.setEstado(ACTIVO);
                                                                    supDao.updateOperatorSupervisor(session, supervisor);
                                                                    log.info("Clave del OperadorSupervisor: "
                                                                                    + supervisor.getOperador().getIdentityDocument() + ", actualizada.");
                                                                    UtilityFile.createWriteDataFile(getEyesFileName(),
                                                                                    "OPER_SUP_UPD_P|" + properties.getHostName() + "|3|"
                                                                                                    + properties.getHostAddress() + "|" + store + "|END|"
                                                                                                    + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
                                                                                                                    .format(new Date())
                                                                                                    + "|Clave del OperadorSupervisor: "
                                                                                                    + supervisor.getOperador().getIdentityDocument() + " actualizada.\n",
                                                                                    true);
                                                            } else {
                                                                    supervisor.setEstado(FALLO);
                                                                    supDao.updateOperatorSupervisor(session, supervisor);
                                                                    log.error("Error de conexi�n, se coloca al operadorSupervisor en el estado: "
                                                                                    + FALLO);
                                                                    UtilityFile.createWriteDataFile(getEyesFileName(),
                                                                                    "OPER_SUP_UPD_P|" + properties.getHostName() + "|3|"
                                                                                                    + properties.getHostAddress() + "|" + store + "|WAR|"
                                                                                                    + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
                                                                                                                    .format(new Date())
                                                                                                    + "|No se pudo modificar la clave del OperadorSupervisor: "
                                                                                                    + supervisor.getOperador().getIdentityDocument() + ".\n",
                                                                                    true);

                                                            }
                                                    } catch (Exception e) {
                                                            log.error(e.getMessage(), e);
                                                            try {
                                                                    supervisor.setEstado(FALLO);
                                                                    supDao.updateOperatorSupervisor(session, supervisor);
                                                                    UtilityFile.createWriteDataFile(getEyesFileName(),
                                                                                    "OPER_SUP_UPD_P|" + properties.getHostName() + "|3|"
                                                                                                    + properties.getHostAddress() + "|" + store + "|ERR|"
                                                                                                    + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
                                                                                                                    .format(new Date())
                                                                                                    + "|Error al actualizar la clave del OperadorSupervisor: "
                                                                                                    + supervisor.getOperador().getIdentityDocument() + ".\n",
                                                                                    true);
                                                            } catch (Exception e1) {
                                                                    log.error(e1.getMessage(), e1);
                                                            } finally {
                                                                    closeConnection();
                                                            }
                                                    }
                                            }
                                    } else {
                                            log.info("No hay operadoresSupervisor para actualizar.");
                                    }
                                    session.close();
                                    session = null;

                            } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    try {
                                            session.close();
                                    } catch (Exception ex) {
                                            log.error(e.getMessage(), e);
                                    }
                                    session = null;
                            }
                        }
			try {
				Thread.sleep(properties.getLong("operatorSupervisor.timesleep"));
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
		finished = true;
	}
	
	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	protected boolean sendOperatorSupervisorUpdate(String ip, Frame frame) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			
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

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		closeConnection();
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo OperatorPasswordUpdateProcess...");
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
		log.info("Finalizó el Proceso de Actualizacion de OperadoresSupervisores.");
		return true;
	}

}
