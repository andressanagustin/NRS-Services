package com.allc.arms.server.operations.cer.epslog;

import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.epsLog.EPSLogReg;
import com.allc.arms.server.persistence.epsLog.EPSLogRegDAO;
import com.allc.arms.utils.tsl.TSLConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class EPSLogReaderOperation extends AbstractOperation {

	private static Logger log = Logger.getLogger(EPSLogReaderOperation.class);
	private EPSLogRegDAO epsLogRegDAO = new EPSLogRegDAO();
	private Session session = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		log.info("Iniciando EPSLogReaderOperation..");
		try {
			
			StringBuilder msg = new StringBuilder();
			iniciarSesion("Arts");
			
			String line = (String) frame.getBody().get(0);
			String fecha = line.substring(16, 33);
			int tienda = (new Integer(((String) frame.getHeader().get(3)))).intValue();
			String diaContable = (String) frame.getBody().get(1);
			
			EPSLogReg epsLogReg = new EPSLogReg();
			epsLogReg.setStoreNumber(tienda);
			epsLogReg.setDateTime(sdf.parse(fecha));
			epsLogReg.setBusinessDate(TSLConstants.Formatters.TLOG_SUFIX_DATE_TIME_FORMATTER_ACE.parse(diaContable));
			epsLogReg.setHeader(line.substring(0, 33));
			epsLogReg.setData(line.substring(33, 512));
			
			boolean insertOK = epsLogRegDAO.insertaEPSLogReg(session, epsLogReg);
			
			if (insertOK) {
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
