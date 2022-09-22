/**
 * 
 */
package com.allc.arms.server.operations.cer.devolucion;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.devolucion.DevFactura;
import com.allc.arms.server.persistence.devolucion.Devolucion;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * @author gustavo
 *
 */
public class UpdateDevolucionOperation extends AbstractOperation {
	protected static Logger log = Logger.getLogger(UpdateDevolucionOperation.class);
	protected Session sesion;
	private Transaction tx;
	protected boolean finished = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.allc.core.operation.AbstractOperation#shutdown(long)
	 */
	@Override
	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.allc.core.operation.AbstractOperation#process(com.allc.comm.socket.ConnSocketServer, com.allc.comm.frame.Frame,
	 * com.allc.properties.PropFile)
	 */
	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		log.info("Iniciando Update Devolucion Operation...");
		try {
			UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_DEV_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Actualizando Devolución: "+frame.getBody().get(0)+".\n", true);
			Long idDevs = Long.valueOf((String) frame.getBody().get(0));
			String message = null;
			boolean devActualizada = false;
			if (updateDev(idDevs)){
				devActualizada = true;
				message = "0";
			} else
				message = "1";
			StringBuilder sb = new StringBuilder(frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + message);
			log.info("Respuesta a enviar: " + sb.toString());
			if(socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")))){
				if(devActualizada)
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_DEV_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Devolución: "+frame.getBody().get(0)+" actualizada.\n", true);
				else
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_DEV_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Devolución: "+frame.getBody().get(0)+" no actualizada.\n", true);
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_DEV_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_DEV_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al buscar la Devolución: "+frame.getBody().get(0)+".\n", true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
		return false;
	}
	
	private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	private boolean updateDev(Long idDevs) {
		try {
			openSession();
			Devolucion dev = (Devolucion) sesion.get(Devolucion.class, idDevs);
			if (dev != null) {
				dev.setIdEstado(6);
				sesion.save(dev);
				DevFactura fact = (DevFactura) sesion.get(DevFactura.class, idDevs);
				if (fact != null) {
					fact.setEstado(3);
					sesion.save(fact);
				}
				tx.commit();
			}
		} catch (Exception e) {
			try {
				tx.rollback();
			} catch (Exception ex) {
				log.error(e.getMessage(), e);
			}
			log.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	public void openSession() {
		while (sesion == null) {
			try {
				sesion = HibernateSessionFactoryContainer.getSessionFactory("Devs").openSession();
				tx = sesion.beginTransaction();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				sesion = null;
				tx = null;
			}
			if (sesion == null || tx == null)
				try {
					log.error("OCURRIÓ UN ERROR AL CREAR LA SESIÓN A LA BD, SE REINTENTARÁ EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.allc.core.operation.AbstractOperation#process(com.allc.comm.pipe.ConnPipeServer, com.allc.comm.frame.Frame,
	 * com.allc.properties.PropFile)
	 */
	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

}
