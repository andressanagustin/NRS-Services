package com.allc.arms.server.operations.cer.moto;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.item.ItemDAO;
import com.allc.arms.server.persistence.moto.Moto;
import com.allc.arms.server.persistence.moto.MotoDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.entities.Item;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class ConsultaMotoOperation extends AbstractOperation {

	private Logger logger = Logger.getLogger(ConsultaMotoOperation.class);
	MotoDAO motoDAO = new MotoDAO();
	ItemDAO itemDAO = new ItemDAO();
	private Session session = null;

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		logger.info("Iniciando Consulta Moto...");

		try {
			String numSerie = (String) frame.getBody().get(0);
			int tiendaConsulta = (new Integer(((String) frame.getHeader().get(3)))).intValue();
			UtilityFile.createWriteDataFile(getEyesFileName(properties),
					"CONS_MOTO_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Consultando Moto: " + frame.getBody().get(0) + ".\n",
					true);

			if (numSerie != null) {
				String message = null;
				iniciarSesion("Arts");
				boolean motoEncontrada = true;

				if (motoDAO.motoVendida(session, numSerie)) {
					StringBuffer sb = new StringBuffer();
					sb.append("2").append(ArmsServerConstants.Communication.FRAME_SEP).append(numSerie);
					message = sb.toString();
					logger.info("Moto vendida. Serial: " + numSerie);
				} else {
					Moto moto = motoDAO.getMotoByNumSerie(session, numSerie);

					if (moto != null) {
						int tienda = Integer.valueOf(moto.getCentro());
						if (tienda == tiendaConsulta) {

							logger.info("Moto: " + numSerie + " encontrada en la tienda: " + tiendaConsulta + ".");
							String material = moto.getMaterial();
							Item item = itemDAO.getItem(session, material);
							if (item != null) {
								StringBuffer sb = new StringBuffer();
								sb.append("0").append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getSerie()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(String.valueOf(item.getItemCode()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getChasis()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getAnoFabricacion()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getClase()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getColor()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getCilindraje()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getCapAsiento()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getNumCPN()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getTieneNumCPN()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getSubcategoria()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getTipoCombustible()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getTipoCarroceria()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getNumCKD()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getMarca()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getModelo()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getPaisOrigen()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getTonelaje()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getMandante()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getMaterial()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getMotor()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getNumSRI()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getFechaCPN()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getCentro()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getStatus()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getEjes()))
										.append(ArmsServerConstants.Communication.FRAME_SEP)
										.append(Util.validaNotNull(moto.getRuedas()));
								message = sb.toString();
							}
						} else {
							logger.info("Moto: " + numSerie + " encontrada en una tienda diferente a la original. Tienda: " + moto.getCentro());
							StringBuffer sb = new StringBuffer();
							sb.append("3").append(ArmsServerConstants.Communication.FRAME_SEP).append(numSerie)
									.append(ArmsServerConstants.Communication.FRAME_SEP)
									.append(moto.getCentro());
							message = sb.toString();

						}
					}
				}
				if (message == null) {
					logger.info("Moto: " + numSerie + " no encontrada");
					StringBuffer sb = new StringBuffer();
					motoEncontrada = false;
					sb.append("1").append(ArmsServerConstants.Communication.FRAME_SEP).append(numSerie);
					message = sb.toString();
				}
				StringBuilder sb = new StringBuilder(
						frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + message);
				if (socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(),
						properties.getInt("serverSocket.quantityBytesLength")))) {
					logger.info("Respuesta enviada.");
					if (motoEncontrada)
						UtilityFile.createWriteDataFile(getEyesFileName(properties),
								"CONS_MOTO_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
										+ frame.getHeader().get(3) + "|END|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|Moto: " + frame.getBody().get(0) + " encontrada.\n",
								true);
					else
						UtilityFile.createWriteDataFile(getEyesFileName(properties),
								"CONS_MOTO_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
										+ frame.getHeader().get(3) + "|END|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|Moto: " + frame.getBody().get(0) + " no encontrada.\n",
								true);
				} else
					UtilityFile.createWriteDataFile(getEyesFileName(properties),
							"CONS_MOTO_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
									+ frame.getHeader().get(3) + "|WAR|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|No se pudo enviar la respuesta.\n",
							true);
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"CONS_MOTO_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|WAR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Requerimiento inválido.\n",
						true);
			session.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"CONS_MOTO_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al buscar el Moto: " + frame.getBody().get(0) + ".\n",
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

}
