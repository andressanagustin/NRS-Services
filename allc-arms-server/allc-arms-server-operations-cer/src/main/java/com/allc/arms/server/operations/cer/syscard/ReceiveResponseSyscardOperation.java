package com.allc.arms.server.operations.cer.syscard;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class ReceiveResponseSyscardOperation extends AbstractOperation {
	private Session sesion;
	private Transaction tx;
	public boolean isEnd = false;
	protected boolean finished = false;
	static Logger log = Logger.getLogger(ReceiveResponseSyscardOperation.class);
	protected ConnSocketClient socketClient;
	protected SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		StringBuilder msg = new StringBuilder(frame.getHeaderStr());
		String storeNumber = properties.getObject("eyes.store.code");
		try {
			String data = (String) frame.getBody().get(0);
			log.info("Datos recibidos: " + data);
			UtilityFile.createWriteDataFile(getEyesFileName(properties),
					"RCV_RSP_SYS_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando registro de respuesta Syscard.\n",
					true);
			if (!data.trim().isEmpty()) {
				openSession();
				updateResponseSys(data);
				if(Integer.valueOf(storeNumber)>0)
					saveResponseSysToFile(data);
				msg.append(frame.getSeparator()).append("0");
				String tmp = Util.addLengthStartOfString(msg.toString(),
						properties.getInt("serverSocket.quantityBytesLength"));
				log.info(tmp);
				if (socket.writeDataSocket(tmp)) {
					UtilityFile.createWriteDataFile(getEyesFileName(properties),
							"RCV_RSP_SYS_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
									+ frame.getHeader().get(3) + "|END|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|Respuesta Syscard procesada.\n",
							true);
				} else
					UtilityFile.createWriteDataFile(getEyesFileName(properties),
							"RCV_RSP_SYS_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
									+ frame.getHeader().get(3) + "|WAR|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|No se pudo enviar la respuesta.\n",
							true);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("-1");
				String tmp = Util.addLengthStartOfString(msg.toString(),
						properties.getInt("serverSocket.quantityBytesLength"));
				log.info(tmp);
				socket.writeDataSocket(tmp);
			} catch (Exception ex) {
				log.error(e.getMessage(), e);
			}
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"RCV_RSP_SYS_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al procesar la respuesta.\n",
						true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		} finally {
			sesion.close();
			sesion = null;
			tx = null;
		}
		finished = true;
		return false;
	}

	private String getEyesFileName(PropFile properties) {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	private void updateResponseSys(String data) throws Exception {
		try {
			initTx();
			Date fecha = null;
			String accountNumber = data.substring(0, 16);
			String sequence = data.substring(16, 22);
			String autNum = data.substring(22, 28);
			String respCode = data.substring(28, 30);
			Integer monto1 = Integer.valueOf(data.substring(30, 42));
			String factura = data.substring(42, 57);
			Integer monto2 = Integer.valueOf(data.substring(57, 66));
			
			if(data.length() > 66){
				String fechaCont = data.substring(66, 74);
				fecha = formatter.parse(fechaCont);
			}else
				fecha = new Date();
			
			Query query = sesion.createSQLQuery(
					"INSERT INTO CO_CSM_SYS_DT (INVC_NMB, ACNT_NMB, SEQ_TRX, AUT_NMB, AMNT_1, AMNT_2, RSP_CD, DC_DY_BSN) VALUES (:valor1, :valor2, :valor3, :valor4, :valor5, :valor6, :valor7, :valor8)");
			query.setParameter("valor1", factura);
			query.setParameter("valor2", accountNumber);
			query.setParameter("valor3", sequence);
			query.setParameter("valor4", autNum);
			query.setParameter("valor5", monto1);
			query.setParameter("valor6", monto2);
			query.setParameter("valor7", respCode);
			query.setParameter("valor8", fecha);
			log.info("Query: " + query.getQueryString());
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				tx.rollback();
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
	}

	public void openSession() {
		while (sesion == null) {
			try {
				sesion = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				sesion = null;
			}
			if (sesion == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA SESION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	public void initTx() {
		while (tx == null || !tx.isActive()) {
			try {
				tx = sesion.beginTransaction();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				tx = null;
			}
			if (tx == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA TRANSACCION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	public void saveResponseSysToFile(String data) {
		
		String actualDateDay;
		actualDateDay = getFechaActual();
		String respondeSysFileName = "C:/ALLC/ArmsServer/responseSyscard" +actualDateDay+ ".dat";

		try {
			File responseSysFile = new File(respondeSysFileName);
			if (!responseSysFile.exists())
				responseSysFile.createNewFile();
			BufferedWriter bwr = new BufferedWriter(new FileWriter(responseSysFile, true));
			bwr.write(data);
			bwr.newLine();
			if (bwr != null) {
				bwr.close();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private String getFechaActual() {
		return Util.convertDateToString(new Date(), "YYYYMMdd");
	}
	
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo ReceiveResponseSyscardOperation...");
		while (!finished)
			try {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= timeToWait) {
					return false;
				}
				Thread.sleep(500);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		log.info("Finalizo la Operacion de Recepción de Respuestas de Syscard.");
		return true;
	}

	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

}
