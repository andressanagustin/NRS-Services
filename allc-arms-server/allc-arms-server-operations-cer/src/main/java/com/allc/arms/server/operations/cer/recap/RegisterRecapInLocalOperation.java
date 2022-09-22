package com.allc.arms.server.operations.cer.recap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

public class RegisterRecapInLocalOperation extends AbstractOperation {

	protected static Logger log = Logger.getLogger(RegisterRecapInLocalOperation.class);
	protected Session sesion;
	private Transaction tx;
	protected boolean finished = false;
	Session sessionArts = null;
	
	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		
		log.info("Iniciando RegisterRecapInLocalOperation...");
		
		UtilityFile.createWriteDataFile(getEyesFileName(properties), "REG_RECAP_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Actualizando Recap en tienda.\n", true);
		
		String originfileName = (String) frame.getBody().get(0);
		File fileToRegister = new File(originfileName);
		String fecha = "";
		String hora = "";
		String seq = "";
		String auto = "";
		String lote = "";
		StringBuilder msg = new StringBuilder();
		
		log.info("Archivo a Registrar: " + originfileName);
		
		try {
			BufferedReader readerFileRecap = new BufferedReader(new FileReader(fileToRegister));
			iniciarSesionArts();
			String line;
			while ((line = readerFileRecap.readLine()) != null) {
				
				String[] parts = line.split("\\|");
				fecha = parts[0];
				hora = parts[1];
				seq = parts[2];
				auto = parts[3];
				lote = parts[4];
				
				updateRecapInfo(fecha, hora, seq, auto, lote);
				
			}
			
			readerFileRecap.close();
			
			msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("0");
			String tmp = Util.addLengthStartOfString(msg.toString(),
					properties.getInt("serverSocket.quantityBytesLength"));
			log.info("Respuesta a enviar:" + tmp);
			if (socket.writeDataSocket(tmp)) {
				log.info("Respuesta Positiva enviada con exito al ArmsServerCentral.");
			} else
				log.info("Respuesta Positiva no pudo ser enviada con exito al ArmsServerCentral.");
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("1");
			String tmp = Util.addLengthStartOfString(msg.toString(),
					properties.getInt("serverSocket.quantityBytesLength"));
			log.info("Respuesta a enviar:" + tmp);
			if (socket.writeDataSocket(tmp)) {
				log.info("Respuesta Negativa enviada con exito al ArmsServerCentral.");
			} else
				log.info("Respuesta Negativa no pudo ser enviada con exito al ArmsServerCentral.");
		}
		
		return false;
	}

	
	private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
	
	public boolean updateRecapInfo(String fecha, String hora, String seq, String auto, String lote) {
		tx = null;
		try {
			tx = sessionArts.beginTransaction();
			Query query = sessionArts.createSQLQuery("UPDATE CO_TND_PINPAD SET LOTE = :valor1 WHERE FECHA_TRX = '"
					+ fecha + "' and HORA_TRX = '" + hora + "' and SEQ_TRX = '" + seq + "' and NUM_AUTO = '" + auto + "'");
			query.setParameter("valor1", lote);
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}
	
	protected Session iniciarSesionArts() {

		while (sessionArts == null) {
			try {
				sessionArts = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionArts == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}

		return sessionArts;
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
