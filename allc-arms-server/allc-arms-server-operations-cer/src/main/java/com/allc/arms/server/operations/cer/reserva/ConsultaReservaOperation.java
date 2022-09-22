package com.allc.arms.server.operations.cer.reserva;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.reserva.ReservaDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.entities.Reserva;
import com.allc.entities.RetailStore;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class ConsultaReservaOperation extends AbstractOperation{
	
	private Logger logger = Logger.getLogger(ConsultaReservaOperation.class);
	private Session session = null;
	private ReservaDAO reservaDAO = new ReservaDAO();

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		logger.info("Iniciando Consulta Reserva...");
		

		try {
			
			Integer store = Integer.valueOf((String)frame.getHeader().get(Frame.POS_SOURCE));
			String numSerie = (String) frame.getBody().get(0);
			String numReserva = (String) frame.getBody().get(1);
			Integer codResp = 0;
			boolean reservaEncontrada = false;
			
			UtilityFile.createWriteDataFile(getEyesFileName(properties),
					"CONS_RSV_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Consultando Reserva para la Tienda: " + frame.getBody().get(0) + ".\n",
					true);

			if (numSerie != null && numReserva != null) {
				String message = null;
				iniciarSesion("Arts");

				Reserva reserva = reservaDAO.getReservaByNumRsvAndNumSerie(session, Integer.valueOf(numReserva));

				if (reserva != null) {
					logger.info("Reserva para la Tienda: " + store + " encontrada.");
					//Long numSerieLong = Long.valueOf(numSerie);
					if(reserva.getNumSerie().equals(numSerie))
					{
						RetailStore retailStore = reservaDAO.getRetailStoreByCode(session, store);
						
						if(retailStore != null && reserva.getRetailStoreID().equals(retailStore.getRetailStoreID()))
						{
							Date fechaActual = new Date();
							if(fechaActual.after(reserva.getFechaDesde()) && fechaActual.before(reserva.getFechaHasta())) 
							{
								if(reserva.getRsvUsada().equals(0))
									codResp=0;
								else
									codResp=5;
							}
							else
								codResp=3;
						}
						else
							codResp=2;
					}
					else
						codResp=1;
				}	
				else{
					codResp=4;
					logger.info("Reserva para la Tienda: " + store + " no encontrada.");
					}
				
				message = codResp + ArmsServerConstants.Communication.FRAME_SEP + numSerie + ArmsServerConstants.Communication.FRAME_SEP + numReserva;
				StringBuilder sb = new StringBuilder(frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + message);
				logger.info("Respuesta a enviar:"+sb.toString());
				
				if(socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")))){
					if(reservaEncontrada)
						UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_RSV_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Reserva: "+frame.getBody().get(1)+" encontrada.\n", true);
					else
						UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_RSV_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Reserva: "+frame.getBody().get(1)+" no encontrada.\n", true);
				} else
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_RSV_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
			}  else
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_RSV_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Requerimiento inválido.\n", true);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_RSV_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al buscar la Reserva: "+frame.getBody().get(1)+".\n", true);
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
