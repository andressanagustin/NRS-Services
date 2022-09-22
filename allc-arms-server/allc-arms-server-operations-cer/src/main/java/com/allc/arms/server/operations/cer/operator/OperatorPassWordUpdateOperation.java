package com.allc.arms.server.operations.cer.operator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.operator.Operator;
import com.allc.arms.server.persistence.operator.OperatorDAO;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.server.persistence.supervisor.Supervisor;
import com.allc.arms.server.persistence.supervisor.SupervisorDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class OperatorPassWordUpdateOperation extends AbstractOperation{
	
	private Logger logger = Logger.getLogger(OperatorPassWordUpdateOperation.class);
	OperatorDAO operatorDAO;
	StoreDAO storeDAO;
	SupervisorDAO supDao;
	private Session session = null;
	protected boolean finished = false;
	protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	public final Integer PROCESAR = new Integer(1);
	public final Integer FALLO = new Integer(2);
	public final Integer ACTIVO = new Integer(4);
	protected Session sessionSaAdmin = null;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	protected ConnSocketClient socketClient = null;
	
	public void initialize(PropFile properties) {
		try {
			storeDAO = new StoreDAO();
			operatorDAO = new OperatorDAO();
			supDao = new SupervisorDAO();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		
		logger.info("Iniciando Operator Update Password Operation...");
		StringBuilder message = new StringBuilder();
		List list;
		boolean connectionError = false;
		String store = properties.getObject("eyes.store.code");
		
		Transaction tx = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

		initialize(properties);
		iniciarSesion();
		iniciarSesionSaAdmin();
		
		try {
			String identityDocument = (String) frame.getBody().get(0);
			String clave = (String) frame.getBody().get(1);
			String clave128 = (String) frame.getBody().get(2);
			String fechaInicio = (String) frame.getBody().get(3);
			String registro = (String) frame.getBody().get(4);
			
			Date fechaInicioDate = sdf.parse(fechaInicio);
			
			Operator ope = operatorDAO.getOperatorsByIdentityDocument(session, identityDocument);
			
			list = supDao.getSupOperatorsByOperador(session, ope.getOperadorId().toString());
			
			tx = session.beginTransaction();
			
			if(list != null && !list.isEmpty()){
				Iterator itcodSup = list.iterator();
				while(itcodSup.hasNext()){
					Supervisor supervi = (Supervisor) itcodSup.next();
					supervi.setEstado(5);
					session.saveOrUpdate(supervi);
					
				}
			}
			
			Supervisor newSup = new Supervisor();
			newSup.setClave(clave);
			newSup.setClave128(clave128);
			newSup.setEstado(PROCESAR);
			newSup.setFechaInicio(fechaInicioDate);
			newSup.setIdRegistro(Integer.valueOf(registro));
			newSup.setOperador(ope);
			session.saveOrUpdate(newSup);
			tx.commit();
			
			
			String today = ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
					.format(new Date());
			message.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(ArmsServerConstants.Process.OPER_SUP_UPDATE)
					.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
					.append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(store)
					.append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(ArmsServerConstants.Communication.TEMP_CONN)
					.append(ArmsServerConstants.Communication.FRAME_SEP).append(today)
					.append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(identityDocument)
					.append(ArmsServerConstants.Communication.FRAME_SEP).append(clave);
			list = Arrays.asList(p.split(message.toString()));

			frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
					ArmsServerConstants.Communication.FRAME_SEP);
			
			Store tiendaAux = storeDAO.getStoreByCode(sessionSaAdmin,Integer.valueOf(store));
			
			if (frame.loadData()) {
				connectionError = !sendOperatorSupervisorUpdate(
						tiendaAux.getIp(), frame);
				if (!connectionError)
					closeConnection();
			}
			
			if (!connectionError) {
				newSup.setEstado(ACTIVO);
				supDao.updateOperatorSupervisor(session, newSup);
				logger.info("Clave del OperadorSupervisor: "
						+ newSup.getOperador().getIdentityDocument() + ", actualizada en tienda "+ store +".");
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"OPER_SUP_UPD_O|" + properties.getHostName() + "|3|"
								+ properties.getHostAddress() + "|" + store + "|END|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
										.format(new Date())
								+ "|Clave del OperadorSupervisor: "
								+ newSup.getOperador().getIdentityDocument() + " actualizada en tienda " + store + ".\n",
						true);
				StringBuilder sb5 = new StringBuilder();
				String msgRta= null;
				sb5.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(ArmsServerConstants.Process.OPER_SUP_UPDATE)
				.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
				.append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(store)
				.append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(ArmsServerConstants.Communication.TEMP_CONN)
				.append(ArmsServerConstants.Communication.FRAME_SEP).append(today)
				.append(ArmsServerConstants.Communication.FRAME_SEP).append("0");
				msgRta = sb5.toString();
				StringBuilder sb2 = new StringBuilder(msgRta);
				if (socket.writeDataSocket(Util.addLengthStartOfString(sb2.toString(),
						properties.getInt("serverSocket.quantityBytesLength")))) {
					logger.info("Respuesta enviada: " + sb2.toString());
				} else
					UtilityFile.createWriteDataFile(getEyesFileName(properties),
							"OPER_SUP_UPD_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
									+ frame.getHeader().get(3) + "|WAR|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|No se pudo informar a central.\n",
							true);
			} else {
				newSup.setEstado(FALLO);
				supDao.updateOperatorSupervisor(session, newSup);
				logger.error("Error de conexi�n, se coloca al operadorSupervisor en el estado: " + FALLO);
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"OPER_SUP_UPD_O|" + properties.getHostName() + "|3|"
								+ properties.getHostAddress() + "|" + store + "|WAR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
										.format(new Date())
								+ "|No se pudo modificar la clave del OperadorSupervisor: "
								+ newSup.getOperador().getIdentityDocument() + "para la tienda " + store + ".\n",
						true);
				StringBuilder sb4 = new StringBuilder();
				String msgRta= null;
				sb4.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(ArmsServerConstants.Process.OPER_SUP_UPDATE)
				.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
				.append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(store)
				.append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(ArmsServerConstants.Communication.TEMP_CONN)
				.append(ArmsServerConstants.Communication.FRAME_SEP).append(today)
				.append(ArmsServerConstants.Communication.FRAME_SEP).append("1");
				msgRta = sb4.toString();
				StringBuilder sb2 = new StringBuilder(msgRta);
				if (socket.writeDataSocket(Util.addLengthStartOfString(sb2.toString(),
						properties.getInt("serverSocket.quantityBytesLength")))) {
					logger.info("Respuesta de fallo enviada.");
				} else
					UtilityFile.createWriteDataFile(getEyesFileName(properties),
							"OPER_SUP_UPD_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
									+ frame.getHeader().get(3) + "|WAR|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|Fallo durante el proceso de actualizar supervisor en tienda.\n",
							true);

			}
			
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			tx.rollback();
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "OPER_SUP_UPD_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al actualizar el supervisor.\n", true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
		finished = true;
		return false;
	}
	
	private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
	
	protected boolean sendOperatorSupervisorUpdate(String ip, Frame frame) {
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

	
	protected boolean connectClient(String ip) {
		if (socketClient == null) {
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
	protected void iniciarSesion() {
		while (session == null) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory("Opera").openSession();
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

	protected void iniciarSesionSaAdmin() {
		while (sessionSaAdmin == null) {
			try {
				sessionSaAdmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (sessionSaAdmin == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}
	}

}
