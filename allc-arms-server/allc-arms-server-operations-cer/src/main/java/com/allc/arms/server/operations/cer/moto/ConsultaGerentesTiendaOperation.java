package com.allc.arms.server.operations.cer.moto;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.operator.Operator;
import com.allc.arms.server.persistence.operator.OperatorDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class ConsultaGerentesTiendaOperation extends AbstractOperation {

	private Logger logger = Logger.getLogger(ConsultaGerentesTiendaOperation.class);
	OperatorDAO operatorDAO = new OperatorDAO();
	private Session session = null;

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {

		logger.info("Iniciando Consulta Gerentes de Tienda...");

		try {
			String tienda = (String) frame.getBody().get(0);

			UtilityFile.createWriteDataFile(getEyesFileName(properties),
					"CONS_GERENTE_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Consultando Gerentes para tienda: " + frame.getBody().get(0) + ".\n",
					true);

			if (tienda != null) {
				String message = null;
				iniciarSesion("Opera");
				boolean gerentesEncontrados = true;

				List gerentesPorTienda = operatorDAO.getGerentesByStore(session, tienda);
				if (gerentesPorTienda != null && !gerentesPorTienda.isEmpty()) {
					StringBuffer sb = new StringBuffer();
					sb.append("0").append(ArmsServerConstants.Communication.FRAME_SEP);
					Operator operadorGerente = new Operator();
					Iterator<Operator> it = gerentesPorTienda.iterator();

					while (it.hasNext()) {
						operadorGerente = it.next();
						sb.append(operadorGerente.getIdentityDocument()).append(",").append(operadorGerente.getName()).append(" ").append(operadorGerente.getApellidoP());
						if (it.hasNext()) {
							sb.append(";");
						}
					}

					message = sb.toString();
				}

				if (message == null) {
					StringBuffer sb = new StringBuffer();
					gerentesEncontrados = false;
					sb.append("1").append(ArmsServerConstants.Communication.FRAME_SEP).append(tienda);
					message = sb.toString();
				}
				StringBuilder sb = new StringBuilder(
						frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + message);
				if (socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(),
						properties.getInt("serverSocket.quantityBytesLength")))) {
					if (gerentesEncontrados)
						UtilityFile.createWriteDataFile(getEyesFileName(properties),
								"CONS_GERENTE_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
										+ frame.getHeader().get(3) + "|END|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|Gerentes para tienda: " + frame.getBody().get(0) + " encontrados.\n",
								true);
					else
						UtilityFile
								.createWriteDataFile(getEyesFileName(properties),
										"CONS_GERENTE_O|" + properties.getHostName() + "|3|"
												+ properties.getHostAddress() + "|" + frame.getHeader().get(3) + "|END|"
												+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
														.format(new Date())
												+ "|Gerentes para tienda: " + frame.getBody().get(0)
												+ " no encontrados.\n",
										true);
				} else
					UtilityFile.createWriteDataFile(getEyesFileName(properties),
							"CONS_GERENTE_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
									+ frame.getHeader().get(3) + "|WAR|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|No se pudo enviar la respuesta.\n",
							true);
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"CONS_GERENTE_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|WAR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Requerimiento inválido.\n",
						true);
			session.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"CONS_GERENTE_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al buscar el Cédula, Padrón o RUC: " + frame.getBody().get(1) + ".\n",
						true);
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
					logger.error("OCURRIÓ UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
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

}
