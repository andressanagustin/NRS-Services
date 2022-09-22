package com.allc.arms.server.operations.config;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.allc.arms.server.operations.params.LoadParamsOperation;
import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.store.horas.StoreTime;
import com.allc.arms.server.persistence.store.horas.StoreTimeDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * Operacion que Devuelve la Hora al controlador para actualizarlo 
*/
public class UpdateTime extends AbstractOperation{
	private static Logger log = Logger.getLogger(UpdateTime.class);
	private Session sessionEyes = null;
	
	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		log.info("Iniciando UpdateTime...");
		String message = "0";
		String timeServer = "";
		Integer codTienda = 0;
		try {
			iniciarEyesSesion();
			codTienda = Integer.valueOf((String) frame.getHeader().get(Frame.POS_SOURCE));
			UtilityFile.createWriteDataFile(getEyesFileName(properties), "UP_TIME_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Actualizando hora controlador: "+codTienda+".\n", true);
			timeServer = ArmsServerConstants.DateFormatters.HHmmss_format.format(new Date());
			StoreTimeDAO storeTimeDAO = new StoreTimeDAO();
			List<StoreTime> storeTime = storeTimeDAO.getStoreByCode(sessionEyes, codTienda, ArmsServerConstants.DateFormatters.YYYY_MM_dd_format.format(new Date()));
			if (storeTime != null) {
				for (StoreTime item : storeTime) {
					if (executeToday(item.getDays().split(""))) {
						log.info("MINUTOS A SUMAR: " + item.getMinutos());
						timeServer = ArmsServerConstants.DateFormatters.HHmmss_format.format(
										new Date((new Date()).getTime() + (item.getMinutos() * 60000))
									);
						item.setModificado(true);
						storeTimeDAO.updateStoreTime(sessionEyes, item);
						break;
					}					
				}
			}
			log.info("HORA A ENVIAR: " + timeServer);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			message = "1";
			UtilityFile.createWriteDataFile(getEyesFileName(properties), "UP_TIME_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al actualizar hora del controlador: "+codTienda+".\n", true);
		} finally {
			finalizarEyesSesion();
			StringBuilder sb = new StringBuilder(frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + message + ArmsServerConstants.Communication.FRAME_SEP + timeServer);
            String trama = Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength"));
			log.info("Respuesta a enviar: " + trama);
			if(socket.writeDataSocket(trama)){
				log.info("Trama enviada al server");
			} else {
				log.info("NO se envia la trama al server");
			}
			UtilityFile.createWriteDataFile(getEyesFileName(properties), "UP_TIME_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Finaliza actualizacion hora controlador : "+codTienda+".\n", true);
		}
		return false;
	}
	
	/**
	 * Verifica si esta configurado para ejecutar al dia actual
	 * @param days: Array de los dias de la semana empezando por LU. a DO.
	 * @return true: Si se ejecuta para el dia de la semana actual
	 */
	private boolean executeToday(String[] days) {
		int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1; //DE DO. A LU. EMPIEZA EN 1
		//CONVIERTODE LU. A DO.
		if (dayOfWeek == 0) {
			dayOfWeek = 6;
		} else {
			dayOfWeek--;
		}
		return days[dayOfWeek].equals("1");
	}
	
	private void iniciarEyesSesion() {
        while (sessionEyes == null) {
            try {
                sessionEyes = HibernateSessionFactoryContainer.getSessionFactory("Eyes").openSession();
            } catch (HibernateException e) {
                log.error(e.getMessage(), e);
            }
            if (sessionEyes == null) {
                try {
                    log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
    private void finalizarEyesSesion() {
    	if (sessionEyes != null) {
            sessionEyes.close();
            sessionEyes = null;
        }
    }
    
    private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
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
