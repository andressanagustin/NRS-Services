package com.allc.arms.server.operations.cer.arqueo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.arqueo.ArqueoDAO;
import com.allc.arms.server.persistence.status.StoreStatus;
import com.allc.arms.server.persistence.status.StoreStatusDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class ConsultaArqueoPorMedioDePago extends AbstractOperation{
	
	private Logger logger = Logger.getLogger(ConsultaArqueoPorMedioDePago.class);
	private ArqueoDAO arqueoDAO= new ArqueoDAO(); 
	private Session session = null;
	public boolean isEnd = false;
	protected Session sessionEyes = null;
	private StoreStatusDAO storeStatusDAO = new StoreStatusDAO();

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		
		logger.info("Iniciando Consulta Arqueo por Medio de Pago...");

		try {
			String terminal = (String) frame.getBody().get(0);
			String operador = (String) frame.getBody().get(1);
			int store = Integer.valueOf(properties.getObject("eyes.store.code"));
			
			iniciarSesionEyes();
			
			StoreStatus storeStatus = storeStatusDAO.getStoreStatus(sessionEyes, store);
			Date fecha = storeStatus.getDateTime();
			DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			String fechaC = df.format(fecha);

			UtilityFile.createWriteDataFile(getEyesFileName(properties),
					"CONS_ARQ_TND|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Consultando Arqueo para la terminal: " +frame.getBody().get(0) + ".\n",
					true);

			if (terminal != null && operador != null) {
				String message = null;
				iniciarSesion("Arts");
				boolean arqueoRealizado = true;
				StringBuffer sb = new StringBuffer();
				
				List<Object[]> data = arqueoDAO.getArqueoPorMedioPago(session, terminal, operador, fechaC);
				if(data != null && !data.isEmpty()){
					logger.info("Inicia carga de datos dentro del mensaje.");
					sb.append("0").append(ArmsServerConstants.Communication.FRAME_SEP);

			    	for(int i = 0; i < data.size(); i++){	
			    		Object[] row = data.get(i);
			    		
			    		sb.append(row[0].toString())
			    		.append(";")
			    		.append(row[1].toString())
			    		.append(";")
			    		.append(row[2].toString());
			    		if(row[3] != null){
			    			sb.append(";").append(row[3].toString());
			    			sb.append(";").append(row[4].toString());
			    		}
			    		if( i < data.size()-1)
			    			sb.append(ArmsServerConstants.Communication.FRAME_SEP);
					}
			    	logger.info("Finaliza carga de datos dentro del mensaje.");
					message = sb.toString();
				}
				
				List<Object[]> data2 = arqueoDAO.getArqueoPorCheque(session, terminal, operador, fechaC);
				if(data2 != null && !data2.isEmpty()){
					logger.info("Inicia carga de datos de cheques dentro del mensaje.");
					int size = sb.length(); 
					if(size == 0)
						sb.append("0").append(ArmsServerConstants.Communication.FRAME_SEP);
					else
						sb.append(ArmsServerConstants.Communication.FRAME_SEP);
					
			    	for(int i = 0; i < data2.size(); i++){	
			    		Object[] row = data2.get(i);
			    		
			    		sb.append(row[0].toString())
			    		.append(";")
			    		.append(row[1].toString())
			    		.append(";")
			    		.append(row[2].toString())
			    		.append(";")
			    		.append(row[3].toString());
			    					    		
			    		if( i < data2.size()-1)
			    			sb.append(ArmsServerConstants.Communication.FRAME_SEP);
					}

					message = sb.toString();
					logger.info("Finaliza carga de datos de cheques dentro del mensaje.");
				}
				
				if (message == null) {
					logger.info("Mensaje vacio. Se retorna respuesta con 1.");
					StringBuffer sb2 = new StringBuffer();
					arqueoRealizado = false;
					sb2.append("1").append(ArmsServerConstants.Communication.FRAME_SEP).append(terminal)
					.append(ArmsServerConstants.Communication.FRAME_SEP).append(operador)
					.append(ArmsServerConstants.Communication.FRAME_SEP).append(frame.getBody().get(2));
					message = sb2.toString();
				}
				
				StringBuilder sb3 = new StringBuilder(
						frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + message);
				if (socket.writeDataSocket(Util.addLengthStartOfString(sb3.toString(),
						properties.getInt("serverSocket.quantityBytesLength")))) {
					if (arqueoRealizado)
						UtilityFile.createWriteDataFile(getEyesFileName(properties),
								"CONS_ARQ_TND|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
										+ frame.getHeader().get(3) + "|END|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|Arqueo para la terminal: " + frame.getBody().get(0) +" realizado con exito.\n",
								true);
					else
						UtilityFile
								.createWriteDataFile(getEyesFileName(properties),
										"CONS_ARQ_TND|" + properties.getHostName() + "|3|"
												+ properties.getHostAddress() + "|" + frame.getHeader().get(3) + "|END|"
												+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
														.format(new Date())
												+ "|Arqueo para la terminal: " + frame.getBody().get(0) +" no se realizo con exito.\n",
										true);
				} else
					UtilityFile.createWriteDataFile(getEyesFileName(properties),
							"CONS_ARQ_TND|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
									+ frame.getHeader().get(3) + "|WAR|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|No se pudo enviar la respuesta.\n",
							true);
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"CONS_ARQ_TND|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|WAR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Requerimiento invalido.\n",
						true);
			session.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"CONS_ARQ_TND|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error durante el Arqueo para la terminal: " + frame.getBody().get(1) +".\n",
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
	
	private void iniciarSesionEyes() {
		while (sessionEyes == null && !isEnd) {
			try {
				sessionEyes = HibernateSessionFactoryContainer.getSessionFactory("Eyes").openSession();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (sessionEyes == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}
	}

	private String formatDate(String date) {
		String ano = date.substring(0, 4);
		String mes = date.substring(4, 6);
		String dia = date.substring(6, 8);
		return dia + "/" + mes + "/" + ano;
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
