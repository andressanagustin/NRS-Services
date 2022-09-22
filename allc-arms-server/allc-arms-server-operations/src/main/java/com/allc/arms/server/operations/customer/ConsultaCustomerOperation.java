/**
 * 
 */
package com.allc.arms.server.operations.customer;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.customer.Activity;
import com.allc.arms.server.persistence.customer.Customer;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentCollectionConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentMapConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentSortedMapConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentSortedSetConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernateProxyConverter;
import com.thoughtworks.xstream.hibernate.mapper.HibernateMapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * @author gustavo
 *
 */
public class ConsultaCustomerOperation extends AbstractOperation {
	private static Logger log = Logger.getLogger(ConsultaCustomerOperation.class);
	private Session session = null;
	private final static XStream xstream = new XStream() {
		protected MapperWrapper wrapMapper(final MapperWrapper next) {
			return new HibernateMapper(next);
		}
	};

	@Override
	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		log.info("Iniciando Consulta Customer Operation...");
		try {
			UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CUST_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Consultando Cliente: "+frame.getBody().get(0)+".\n", true);
			StringBuilder message = new StringBuilder();
			xstream.registerConverter(new HibernateProxyConverter());
			xstream.registerConverter(new HibernatePersistentCollectionConverter(xstream.getMapper()));
			xstream.registerConverter(new HibernatePersistentMapConverter(xstream.getMapper()));
			xstream.registerConverter(new HibernatePersistentSortedMapConverter(xstream.getMapper()));
			xstream.registerConverter(new HibernatePersistentSortedSetConverter(xstream.getMapper()));
			iniciarSesion();

			String codCliente = (String) frame.getBody().get(0);
			Customer customer = (Customer) session.get(Customer.class, codCliente);
			boolean clienteEncontrado = false;
			if (customer != null) {
				log.info(customer.toString());
				xstream.alias("Customer", Customer.class);
				xstream.alias("Activity", Activity.class);
				String xmlCustomer = xstream.toXML(customer);
				message.append(ArmsServerConstants.Communication.CERO).append(frame.getSeparator()).append(xmlCustomer);
				Activity act = new Activity();
				act.setCustomerId(codCliente);
				Activity activity = (Activity) session.get(Activity.class, act);
				if (activity != null) {
					activity.setCustomerId(customer.getCustomerId());
					log.info(activity.toString());
					String xmlActivity = xstream.toXML(activity);
					message.append(frame.getSeparator()).append(xmlActivity);
				} else
					log.info("No Activities for customer: " + codCliente);
				clienteEncontrado = true;
			} else
				message.append("1");
			session.close();

			StringBuilder sb = new StringBuilder(frame.getHeaderStr() + ArmsServerConstants.Communication.FRAME_SEP + message);
			log.info("Respuesta a enviar: " + sb.toString());
			if(socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")))){
				if(clienteEncontrado)
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CUST_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Cliente: "+frame.getBody().get(0)+" encontrado.\n", true);
				else
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CUST_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Cliente: "+frame.getBody().get(0)+" no encontrado.\n", true);
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CUST_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "CONS_CUST_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al buscar el Cliente: "+frame.getBody().get(0)+".\n", true);
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
				session = HibernateSessionFactoryContainer.getSessionFactory("CRM").openSession();
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
