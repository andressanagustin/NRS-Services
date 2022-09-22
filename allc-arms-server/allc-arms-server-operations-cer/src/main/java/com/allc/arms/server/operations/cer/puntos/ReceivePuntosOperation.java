package com.allc.arms.server.operations.cer.puntos;

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

public class ReceivePuntosOperation extends AbstractOperation {
	private Session sesion;
	private Transaction tx;
	public boolean isEnd = false;
	protected boolean finished = false;
	protected SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
	static Logger log = Logger.getLogger(ReceivePuntosOperation.class);
	protected ConnSocketClient socketClient;

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		StringBuilder msg = new StringBuilder(frame.getHeaderStr());
		String storeNumber = properties.getObject("eyes.store.code");
		try {
			String data = (String) frame.getBody().get(0);
			String fechaCont = (String) frame.getBody().get(1);
			UtilityFile.createWriteDataFile(getEyesFileName(properties),
					"RCV_PTOS_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando registro de Puntos.\n",
					true);
			if (!data.trim().isEmpty()) {
				openSession();
				log.info("INGRESA OPERACION");
				updatePuntos(data, fechaCont, (String) frame.getHeader().get(3));
				if(Integer.valueOf(storeNumber)>0)
					savePuntosToFile(data, fechaCont);
				msg.append(frame.getSeparator()).append("0");
				String tmp = Util.addLengthStartOfString(msg.toString(),
						properties.getInt("serverSocket.quantityBytesLength"));
				log.info(tmp);
				if (socket.writeDataSocket(tmp)) {
					UtilityFile.createWriteDataFile(getEyesFileName(properties),
							"RCV_PTOS_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
									+ frame.getHeader().get(3) + "|END|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|Puntos procesados.\n",
							true);
				} else
					UtilityFile.createWriteDataFile(getEyesFileName(properties),
							"RCV_PTOS_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
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
						"RCV_PTOS_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al procesar los puntos.\n",
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

	private void updatePuntos(String data, String fechaCont, String storeCode) throws Exception {
		try {
			initTx();
			String[] campos = data.split("\\|");
			Integer store = Integer.valueOf(storeCode);
			String terminal = campos[0];
			while (terminal.length() < 4) {
				terminal = "0" + terminal;
			}
			Date fecha = formatter.parse(fechaCont);
			Integer trxNum = Integer.valueOf(campos[1]);
			for (int i = 2; i < campos.length; i++) {
				String[] subcampos = campos[i].split("\\:");
				Long item = Long.valueOf(subcampos[0]);
				Integer ordNum = Integer.valueOf(subcampos[1]);
				Integer ptos = Integer.valueOf(subcampos[2]);
				String cdPromo = subcampos[3];
				Query query = sesion.createSQLQuery(
						"INSERT INTO CO_PTS_DT (CD_STR_RT, CD_WS, DC_DY_BSN, AI_TRN, CD_ITM, ORDL_NBR, PTS, CD_PRM) VALUES (:valor1, :valor2, :valor3, :valor4, :valor5, :valor6, :valor7, :valor8)");
				query.setParameter("valor1", store);
				query.setParameter("valor2", terminal);
				query.setParameter("valor3", fecha);
				query.setParameter("valor4", trxNum);
				query.setParameter("valor5", item);
				query.setParameter("valor6", ordNum);
				query.setParameter("valor7", ptos);
				query.setParameter("valor8", cdPromo);
				log.info("Query: " + query.getQueryString());
				query.executeUpdate();
			}
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

	public void savePuntosToFile(String data, String fechaCont) {
		
		
		String puntosFileName = "C:/ALLC/ArmsServer/PtosXArt"+ fechaCont +".dat";

		try {
			File puntosFile = new File(puntosFileName);
			if (!puntosFile.exists())
				puntosFile.createNewFile();
			BufferedWriter bwr = new BufferedWriter(new FileWriter(puntosFile, true));
			bwr.write(data);
			bwr.newLine();
			if (bwr != null) {
				bwr.close();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}


	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo ReceivePuntosOperation...");
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
		log.info("Finalizo la Operacion de Recepción de Puntos.");
		return true;
	}

	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

}
