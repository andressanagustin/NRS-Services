/**
 * 
 */
package com.allc.arms.server.operations.cer.cedpadruc;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.cedpadruc.CedPadRucDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.entities.CedRuc;
import com.allc.entities.Extranjero;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * @author gustavo
 *
 */
public class ConsultaCedPadRucOperation extends AbstractOperation {
	private Logger logger = Logger.getLogger(ConsultaCedPadRucOperation.class);
	private CedPadRucDAO dao = new CedPadRucDAO();
	private Session session = null;

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		logger.info("Iniciando Consulta CedPadRuc...");

		try {
			String tipo = (String) frame.getBody().get(0);
			String clave = (String) frame.getBody().get(1);
			UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CPR_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Consultando Cédula, Padrón o RUC: "+frame.getBody().get(1)+".\n", true);

			if (tipo != null && clave != null) {
				String message = null;
				iniciarSesion("Arts");
				boolean cprEncontrado = true;
				if ("1".equals(tipo) || "2".equals(tipo)) {
					CedRuc cedRuc = dao.getCedRucByCode(session, clave);
					if (cedRuc != null) {
						StringBuffer sb = new StringBuffer();
						sb.append("0").append(ArmsServerConstants.Communication.FRAME_SEP).append(cedRuc.getCodigo())
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getNombre()))
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getDireccion()))
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getTelefono()))
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getGenero()));
						message = sb.toString();
					}
				} else if ("4".equals(tipo)) {
					Extranjero cedRuc = dao.getExtranjeroById(session, clave);
					if (cedRuc != null) {
						StringBuffer sb = new StringBuffer();
						sb.append("0").append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getCodigo()))
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getNombre()))
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getDireccion()))
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getTelefono()));
						message = sb.toString();
					}
				} else if ("3".equals(tipo)) {
					CedRuc cedRuc = dao.getCedRucByCode(session, clave);
					if (cedRuc != null) {
						StringBuffer sb = new StringBuffer();
						sb.append("0").append(ArmsServerConstants.Communication.FRAME_SEP).append(cedRuc.getCodigo())
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getNombre()))
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getRegistroElec().getMesa()))
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getTelefono()))
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getRegistroElec().getProvincia()))
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getRegistroElec().getCanton()))
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getRegistroElec().getCircunscripcion()))
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getRegistroElec().getParroquia()))
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getRegistroElec().getZona()))
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getRegistroElec().getRecinto()))
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getRegistroElec().getJunta()))
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getRegistroElec().getFuncion()))
								.append(ArmsServerConstants.Communication.FRAME_SEP).append(Util.validaNotNull(cedRuc.getRegistroElec().getGenero()));
						message = sb.toString();
					}
				}
				if (message == null){
					cprEncontrado = false;
					message = "1";
				}
				StringBuilder sb = new StringBuilder(frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + message);
				if(socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")))){
					if(cprEncontrado)
						UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CPR_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Cédula, Padrón o RUC: "+frame.getBody().get(1)+" encontrado.\n", true);
					else
						UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CPR_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Cédula, Padrón o RUC: "+frame.getBody().get(1)+" no encontrado.\n", true);
				} else
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CPR_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CPR_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Requerimiento inválido.\n", true);
			session.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CPR_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al buscar el Cédula, Padrón o RUC: "+frame.getBody().get(1)+".\n", true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
		return false;
	}
	
	private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
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
	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

}
