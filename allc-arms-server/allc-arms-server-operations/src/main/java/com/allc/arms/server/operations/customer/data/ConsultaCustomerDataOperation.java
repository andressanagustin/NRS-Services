/**
 * 
 */
package com.allc.arms.server.operations.customer.data;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.customer.data.CustomerData;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * @author gustavo
 *
 */
public class ConsultaCustomerDataOperation extends AbstractOperation {
	private static Logger log = Logger.getLogger(ConsultaCustomerDataOperation.class);
	private Session session = null;


	@Override
	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		log.info("Iniciando Consulta Customer Data Operation...");
		try {
			UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CUDA_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Consultando Datos del Cliente: "+frame.getBody().get(0)+".\n", true);
			StringBuilder message = new StringBuilder();
			iniciarSesion();

			String codCliente = (String) frame.getBody().get(0);

			CustomerData customerData = (CustomerData) session.get(CustomerData.class, codCliente);
			boolean clienteEncontrado = false;
			if (customerData != null) {
				message.append(ArmsServerConstants.Communication.CERO).append(frame.getSeparator()).append(customerData.getNombre()).append(frame.getSeparator()).append(customerData.getApellidoP()).append(frame.getSeparator()).append(customerData.getApellidoM()).append(frame.getSeparator()).append(customerData.getDireccion()).append(frame.getSeparator()).append(customerData.getTelefono());
				clienteEncontrado = true;
			} else
				message.append("1");
			session.close();

			StringBuilder sb = new StringBuilder(frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + message);
			log.info("Respuesta a enviar: " + sb.toString());
			if(socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")))){
				if(clienteEncontrado)
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CUDA_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Datos del Cliente: "+frame.getBody().get(0)+" encontrado.\n", true);
				else
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CUDA_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Datos del Cliente: "+frame.getBody().get(0)+" no encontrado.\n", true);
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CUDA_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CUDA_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al buscar los Datos del Cliente: "+frame.getBody().get(0)+".\n", true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
		return false;
	}
	
	private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	protected void iniciarSesion() {
		while (session == null) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory("COTZ").openSession();
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
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

}
