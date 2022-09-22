package com.allc.arms.server.operations.utils;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class ReSendTramatoCentralOperation extends AbstractOperation {

	protected static Logger log;
	protected String descriptorOperation = "RE_SEND_O";
	protected ConnSocketClient socketServerCentral = null;
	protected StoreDAO storeDAO = new StoreDAO();
	private Session sessionSaAdmin = null;

	protected void initialize() {
		log = Logger.getLogger(ReSendTramatoCentralOperation.class);
	}

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {

		String ip = null;
		StringBuilder msg = new StringBuilder();
		initialize();

		log.info("Iniciando ReSendTramatoCentralOperation...");

		try {

			UtilityFile.createWriteDataFile(getEyesFileName(properties),
					descriptorOperation + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando reenvio de trama.\n",
					true);
			iniciarSaAdminSesion();
			Store central = storeDAO.getStoreByCode(sessionSaAdmin, 0);

			msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("0");
			String tmp = Util.addLengthStartOfString(msg.toString(),
					properties.getInt("serverSocket.quantityBytesLength"));
			log.info("Respuesta a enviar:" + tmp);
			if (socket.writeDataSocket(tmp)) {
				log.info("Respuesta Positiva enviada con exito al agente.");
			} else
				log.info("Respuesta Positiva no pudo ser enviada con exito al agente.");

			ip = central.getIp();
			if (ip != null) {
				connectServer(properties, ip);
				if (frame.loadData()) {
					String trama = Util.addLengthStartOfString(frame.getString().toString(),
							properties.getInt("serverSocket.quantityBytesLength")).toString();
					log.info("Trama a enviar: " + trama);
					if (socketServerCentral.writeDataSocket(trama))
						UtilityFile.createWriteDataFile(getEyesFileName(properties),
								descriptorOperation + properties.getHostName() + "|3|" + properties.getHostAddress()
										+ "|" + frame.getHeader().get(3) + "|END|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|Reenvio de trama realizado con exito.\n",
								true);

					else
						UtilityFile.createWriteDataFile(getEyesFileName(properties),
								descriptorOperation + properties.getHostName() + "|3|" + properties.getHostAddress()
										+ "|" + frame.getHeader().get(3) + "|WAR|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|No se pudo reenviar la trama.\n",
								true);

				}
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						descriptorOperation + "|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al intentar reenviar la trama.\n",
						true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("1");
				String tmp = Util.addLengthStartOfString(msg.toString(),
						properties.getInt("serverSocket.quantityBytesLength"));
				log.info("Respuesta a enviar:" + tmp);
				if (socket.writeDataSocket(tmp)) {
					log.info("Respuesta Negativa enviada con exito al agente.");
				} else
					log.info("Respuesta Negativa no pudo ser enviada con exito al agente.");
			}
		} finally {
			sessionSaAdmin.close();

		}
		return false;
	}

	protected String getEyesFileName(PropFile properties) {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	protected boolean connectServer(PropFile properties, String ip) {
		if (socketServerCentral == null || !socketServerCentral.isConnected()) {
			socketServerCentral = new ConnSocketClient();
			socketServerCentral.setIpServer(ip);
			socketServerCentral.setPortServer(properties.getInt("serverSocket.port"));
			socketServerCentral.setRetries(3);
			socketServerCentral.setTimeOutConnection(properties.getInt("serverSocket.timeOutConnection"));
			socketServerCentral.setTimeOutSleep(properties.getInt("serverSocket.timeOutSleep"));
			socketServerCentral.setQuantityBytesLength(properties.getInt("serverSocket.quantityBytesLength"));
		}
		return socketServerCentral.connectSocketUsingRetries();
	}

	private void iniciarSaAdminSesion() {
		while (sessionSaAdmin == null) {
			try {
				sessionSaAdmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionSaAdmin == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
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

}
