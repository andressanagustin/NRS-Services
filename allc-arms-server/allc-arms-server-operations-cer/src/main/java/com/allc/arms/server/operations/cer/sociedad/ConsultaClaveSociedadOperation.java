package com.allc.arms.server.operations.cer.sociedad;

import java.util.Date;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.sociedad.Sociedad;
import com.allc.arms.server.persistence.sociedad.SociedadDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class ConsultaClaveSociedadOperation extends AbstractOperation{
	
	private Logger logger = Logger.getLogger(ConsultaClaveSociedadOperation.class);
	private SociedadDAO sociedadDAO = new SociedadDAO();
	private Session session = null;
	
	
	
	
	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		
		logger.info("Iniciando Consulta de Clave para Sociedad...");

		try {
			String sociedad = (String) frame.getBody().get(0);

			UtilityFile.createWriteDataFile(getEyesFileName(properties),
					"CONS_SOC_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Consultando Clave de Contabilidad para la sociedad: " + frame.getBody().get(0) + ".\n",
					true);

			if (sociedad != null) {
				String message = null;
				iniciarSesion("Arts");
				boolean claveEncontrada = true;

				Sociedad sociedadEmpleado = sociedadDAO.getClaveBySociedad(session, sociedad);
				
				if (sociedadEmpleado != null) {
					
					StringBuffer sb = new StringBuffer();
					sb.append("0").append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(sociedad)
					.append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(sociedadEmpleado.getClaveContabilidad());
					message = sb.toString();
				}

				if (message == null) {
					StringBuffer sb = new StringBuffer();
					claveEncontrada = false;
					sb.append("1").append(ArmsServerConstants.Communication.FRAME_SEP).append(sociedad);
					message = sb.toString();
				}
				StringBuilder sb = new StringBuilder(
						frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + message);
				if (socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(),
						properties.getInt("serverSocket.quantityBytesLength")))) {
					if (claveEncontrada)
						UtilityFile.createWriteDataFile(getEyesFileName(properties),
								"CONS_SOC_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
										+ frame.getHeader().get(3) + "|END|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|Clave para la sociedad: " + frame.getBody().get(0) + " encontrada.\n",
								true);
					else
						UtilityFile
								.createWriteDataFile(getEyesFileName(properties),
										"CONS_SOC_O|" + properties.getHostName() + "|3|"
												+ properties.getHostAddress() + "|" + frame.getHeader().get(3) + "|END|"
												+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
														.format(new Date())
												+ "|Clave para la sociedad: " + frame.getBody().get(0)
												+ " no encontrada.\n",
										true);
				} else
					UtilityFile.createWriteDataFile(getEyesFileName(properties),
							"CONS_SOC_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
									+ frame.getHeader().get(3) + "|WAR|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|No se pudo enviar la respuesta.\n",
							true);
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"CONS_SOC_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|WAR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Requerimiento inválido.\n",
						true);
			session.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"CONS_SOC_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al buscar la clave para la sociedad: " + frame.getBody().get(0) + ".\n",
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
