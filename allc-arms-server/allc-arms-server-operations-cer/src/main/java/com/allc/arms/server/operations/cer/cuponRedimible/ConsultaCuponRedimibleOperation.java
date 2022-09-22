/**
 * 
 */
package com.allc.arms.server.operations.cer.cuponRedimible;

import java.util.Date;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.cuponRedimible.CouponToRedemptionDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.entities.CouponToRedemption;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * @author gustavo
 *
 */
public class ConsultaCuponRedimibleOperation extends AbstractOperation {
	static Logger log = Logger.getLogger(ConsultaCuponRedimibleOperation.class);
	static Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	private CouponToRedemptionDAO dao = new CouponToRedemptionDAO();
	private Session session = null;

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		log.info("Iniciando Consulta Cupón Redimible...");

		try {
			String couponID = (String) frame.getBody().get(0);
			UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CURE_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Consultando Cupón: "+couponID+".\n", true);

			String message = null;
			iniciarSesion("Arts");
			CouponToRedemption coupon = null;
			if (couponID != null) {
				coupon = dao.getCouponById(session, couponID);
				String status = coupon.getStatus();
				
				if ("G".equalsIgnoreCase(status)) {
					coupon.setStatus("U");
					dao.insertCoupon(session, coupon);
				}

				StringBuffer sb = new StringBuffer();
				sb.append(coupon.getTimeStamp()).append(ArmsServerConstants.Communication.FRAME_SEP).append(coupon.getStatus())
						.append(ArmsServerConstants.Communication.FRAME_SEP).append(coupon.getFechaInicial())
						.append(ArmsServerConstants.Communication.FRAME_SEP).append(coupon.getFechaExpiracion())
						.append(ArmsServerConstants.Communication.FRAME_SEP).append(coupon.getMaxRedemptions())
						.append(ArmsServerConstants.Communication.FRAME_SEP).append(coupon.getnRedemptions())
						.append(ArmsServerConstants.Communication.FRAME_SEP).append(coupon.getLastRedemption())
						.append(ArmsServerConstants.Communication.FRAME_SEP).append(coupon.getPercentOff());
				message = sb.toString();
			}
			StringBuilder sb = new StringBuilder(frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + couponID);
			if (message != null)
				sb.append(ArmsServerConstants.Communication.FRAME_SEP + message);
			if (!socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")))) {
				if (coupon != null && "U".equalsIgnoreCase(coupon.getStatus())) {
					coupon.setStatus("G");
					dao.insertCoupon(session, coupon);
				}
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CURE_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Cupón: "+frame.getBody().get(0)+" encontrado.\n", true);
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CURE_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
			if (session != null && session.isOpen())
				session.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CURE_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al buscar el Cupón: "+frame.getBody().get(0)+".\n", true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
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
					log.error("OCURRIÓ UN ERROR AL CREAR LA CONEXIÓN A LA BD, SE REINTENTARÁ EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	@Override
	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

}
