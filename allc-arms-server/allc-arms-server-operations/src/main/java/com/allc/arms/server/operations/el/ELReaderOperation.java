package com.allc.arms.server.operations.el;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.cierre.CierreDAO;
import com.allc.arms.server.persistence.store.RetailStoreDAO;
import com.allc.arms.utils.tsl.TSLRecordXMLGenerator;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.entities.RetailStore;
import com.allc.entities.Transaction;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class ELReaderOperation extends AbstractOperation {

	private static Logger log = Logger.getLogger(ELReaderOperation.class);
	private static String tslDefaultLocalRepositoryToStore;
	protected RetailStoreDAO retailStoreDAO = new RetailStoreDAO();
	protected CierreDAO cierreDAO = new CierreDAO();
	private Session session = null;
	private String storeCode = null;
	private String syncPath = null;

	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		
		storeCode = properties.getObject("eyes.store.code");
		syncPath = properties.getObject("searchEbil.sync.folder.path");
		
		try {
			log.info("Iniciando ELProcess..");
			tslDefaultLocalRepositoryToStore = properties.getObject("TSL.defaultLocalRepositoryToStore");

			if (StringUtils.isNotBlank(tslDefaultLocalRepositoryToStore))
				UtilityFile.createDir(tslDefaultLocalRepositoryToStore);
			log.info("TSL.defaultLocalRepositoryToStore: " + tslDefaultLocalRepositoryToStore);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		try {
			StringBuilder msg = new StringBuilder();
			/** Instantiate from TSLRecordXMLGenerator **/
			TSLRecordXMLGenerator tslRecordXMLGenerator = new TSLRecordXMLGenerator();
			iniciarSesion("Arts");
			int tienda = (new Integer(((String) frame.getHeader().get(3)))).intValue();
			RetailStore retailStore = retailStoreDAO.getRetailStoreByCode(session, tienda);
			Date oldDate = cierreDAO.getLastCloseDate(session);
			if(oldDate == null)
				oldDate = new Date();
			log.info("Fecha Contable Antigua: "+oldDate.toString());
			/** pass the TSLRecord and the path to save it as a xml file **/
			Transaction trx = tslRecordXMLGenerator.generateByEL(frame, tslDefaultLocalRepositoryToStore, retailStore, oldDate, storeCode, syncPath);
			if (trx != null) {
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("0");
			} else {
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("-1");
			}

			String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
			socket.writeDataSocket(tmp);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return false;
	}

	public static String formatMonth(String month) {
		try {
			new Integer(month);
		} catch (NumberFormatException e) {
		}
		if ("10".equalsIgnoreCase(month)) {
			return "A";
		} else if ("11".equalsIgnoreCase(month)) {
			return "B";
		} else if ("12".equalsIgnoreCase(month)) {
			return "C";
		}
		return month;
	}

	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
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
}
