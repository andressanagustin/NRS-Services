package com.allc.arms.server.processes.cer.pinpad;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.pinpad.PinPad;
import com.allc.arms.server.persistence.pinpad.PinPadDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.lectora.cnx.EnvioSeg;

public class PinPadConfigurationProcess extends AbstractProcess {

	protected static Logger logger = Logger.getLogger(PinPadConfigurationProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	public boolean isEnd = false;
	protected Session session = null;
	protected ConnSocketClient socketClient = null;
	protected boolean finished = false;
	protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	public final Integer ACTUALIZAR = new Integer(1);
	public final Integer NOACTUALIZAR = new Integer(0);

	public void run() {
		List list;
		Frame frame;
		boolean enviado = false;
		while (!isEnd) {
			logger.info("Buscando pinpads para actualizar configuración...");
			String store = properties.getObject("eyes.store.code");
			/** open a session **/
			iniciarSesion("Eyes");
			PinPadDAO pinpadDao = new PinPadDAO();
			try {
				List pinpadToConfigure = pinpadDao.getPinpadByStatus(session, ACTUALIZAR);

				if (pinpadToConfigure != null && !pinpadToConfigure.isEmpty()) {
					logger.info("Se actualizaran " + pinpadToConfigure.size() + " pinpads.");
					Iterator itPinpads = pinpadToConfigure.iterator();
					while (itPinpads.hasNext() && !isEnd) {
						PinPad pinpad = null;
						try {
							pinpad = (PinPad) itPinpads.next();
							UtilityFile.createWriteDataFile(getEyesFileName(),
									"UP_PP_CONF_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + store + "|STR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(
													new Date())
											+ "|Iniciando proceso de configuración del pinpad: " + pinpad.getIdPinpad()
											+ ".\n",
									true);

							enviado = sendPCToPinpad(pinpad, store);

							if (enviado)
								logger.info("Se actualizo el pinpad: " + pinpad.getIdPinpad() + " de la tienda: "
										+ pinpad.getCodTienda() + ".");
							else
								logger.info("No se pudo actualizar el pinpad: " + pinpad.getIdPinpad()
										+ " de la tienda: " + pinpad.getCodTienda() + ".");
						} catch (Exception e) {
							logger.error(e.getMessage(), e);

						}
					}
				} else {
					logger.info("No hay pinpads para configurar.");
				}
				session.close();
				session = null;

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				try {
					session.close();
				} catch (Exception ex) {
					logger.error(e.getMessage(), e);
				}
				session = null;
			}
			try {
				Thread.sleep(properties.getLong("update.config.pinpad.timesleep"));
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
		finished = true;
	}

	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	@Override
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		logger.info("Deteniendo PinPadConfigurationProcess...");
		while (!finished)
			try {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= timeToWait) {
					return false;
				}
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		logger.info("Finaliza el Proceso de Actualización de configuración de Pinpads.");
		return true;
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

	public boolean sendPCToPinpad(PinPad pinpad, String store) throws Exception {

		boolean confiEnviada = false;

		logger.info("Inicia envio trama de configuracion para el pinpad: " + pinpad.getIdPinpad());

		String msjConClave = null;
		StringBuffer mensaje = new StringBuffer("");
		mensaje.append("CP");
		mensaje.append(StringUtils.rightPad(pinpad.getDireccionIP(), 15, ""));
		mensaje.append(StringUtils.rightPad(pinpad.getMascara(), 15, ""));
		mensaje.append(StringUtils.rightPad(pinpad.getGateway(), 15, ""));
		mensaje.append(StringUtils.rightPad(pinpad.getDirIPHostPRedA(), 15, ""));
		mensaje.append(StringUtils.rightPad(pinpad.getPuertoTcpHostPRedA(), 6, ""));
		mensaje.append(StringUtils.rightPad(pinpad.getDirIPHostARedA(), 15, ""));
		mensaje.append(StringUtils.rightPad(pinpad.getPuertoTcpHostARedA(), 6, ""));
		mensaje.append(StringUtils.rightPad(pinpad.getDirIPHostPRedB(), 15, ""));
		mensaje.append(StringUtils.rightPad(pinpad.getPuertoTcpHostPRedB(), 6, ""));
		mensaje.append(StringUtils.rightPad(pinpad.getDirIPHostARedB(), 15, ""));
		mensaje.append(StringUtils.rightPad(pinpad.getPuertoTcpHostARedB(), 6, ""));
		mensaje.append(StringUtils.leftPad(pinpad.getPuertoEscucha(), 6, "0"));

		msjConClave = EnvioSeg.Genera_Componente(mensaje.toString(), 1);
		StringBuffer mensajeAEnviar = new StringBuffer(msjConClave);

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
				cant = socketClient.leeLongitudDataHexaSocket();
				if (cant > 0) {
					logger.info("Cantidad a leer:" + cant);
					data = socketClient.readDataSocket(cant);
					logger.info("Respuesta recibida:" + data);
					socketClient.closeConnection();
				} else {
					UtilityFile.createWriteDataFile(getEyesFileName(),
							"UP_PP_CONF_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
									+ store + "|WAR|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|Proceso de configuración de pinpad no respondio, pero la trama se enviò.\n",
							true);

					pinpad.setActualizar(NOACTUALIZAR);
					session.saveOrUpdate(pinpad);
					confiEnviada = true;
				}
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(),
						"UP_PP_CONF_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store
								+ "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|No hay conexión con el pinpad: " + pinpad.getIdPinpad() + " de la tienda: " + store
								+ " para realizar la configuración.\n",
						true);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

		if (data != null && !data.isEmpty()) {

			if (data.substring(2, 4).equals("00")) {
				logger.info("Respuesta exitosa desde proceso de configuración del pinpad.");
				UtilityFile.createWriteDataFile(getEyesFileName(),
						"UP_PP_CONF_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store
								+ "|PRC|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Proceso de configuracion de pinpad respondió con exito.\n",
						true);
				pinpad.setActualizar(NOACTUALIZAR);
				session.saveOrUpdate(pinpad);
				confiEnviada = true;
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(),
						"UP_PP_CONF_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store
								+ "|WAR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Proceso de configuración de pinpad respondió error. Codigo de error: "
								+ data.substring(2, 4) + ". Mensaje de error: " + data.substring(6, 21) + ".\n",
						true);

		}

		return confiEnviada;
	}

}
