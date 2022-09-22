package com.allc.arms.server.operations.cer.recap;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class UpdateLotePinpadCentralOperation extends AbstractOperation{
	
	protected static Logger log = Logger.getLogger(UpdateLotePinpadCentralOperation.class);
	protected Session session;
	private Transaction tx;

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		
		log.info("Iniciando UpdateLotePinpadCentralOperation operation...");
		
		UtilityFile.createWriteDataFile(getEyesFileName(properties), "UDP_LOTE_CTRL_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Actualizando Recap en tienda.\n", true);
		
		String loteData = (String) frame.getBody().get(0);
		String loteMed = (String) frame.getBody().get(1);
		String tienda = (String) frame.getBody().get(2);;
		StringBuilder msg = new StringBuilder();
		
		try {
			
			iniciarSesion("Eyes");
			
			updateNumLoteTienda(Integer.valueOf(tienda), loteData, loteMed);
			
			msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("0");
			String tmp = Util.addLengthStartOfString(msg.toString(),
					properties.getInt("serverSocket.quantityBytesLength"));
			log.info("Respuesta a enviar:" + tmp);
			if (socket.writeDataSocket(tmp)) {
				log.info("Respuesta Positiva enviada con exito al ArmsServerLocal.");
			} else
				log.info("Respuesta Positiva no pudo ser enviada con exito al ArmsServerLocal.");
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("1");
			String tmp = Util.addLengthStartOfString(msg.toString(),
					properties.getInt("serverSocket.quantityBytesLength"));
			log.info("Respuesta a enviar:" + tmp);
			if (socket.writeDataSocket(tmp)) {
				log.info("Respuesta Negativa enviada con exito al ArmsServerLocal.");
			} else
				log.info("Respuesta Negativa no pudo ser enviada con exito al ArmsServerLocal.");
		}
		
		return false;
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
	
	private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
	
	protected void iniciarSesion(String name) {
		while (session == null) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory(name).openSession();
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
	
	public boolean updateNumLoteTienda(Integer tienda, String loteData, String loteMedia) {
		tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session
					.createSQLQuery("UPDATE CFG_LOTE_PINPAD SET NUM_LOT_R_DATA = :valor1 WHERE DES_CLAVE = " + tienda);
			query.setParameter("valor1", loteData);
			query.executeUpdate();
			Query query2 = session
					.createSQLQuery("UPDATE CFG_LOTE_PINPAD SET NUM_LOT_R_MED = :valor1 WHERE DES_CLAVE = " + tienda);
			query2.setParameter("valor1", loteMedia);
			query2.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}

}
