/**
 * 
 */
package com.allc.arms.server.processes.cer.itemUpdate;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.allc.arms.utils.ArmsServerConstants;
import com.allc.properties.PropFile;

/**
 * @author gustavo
 *
 */
public class EmailSender {
	private static Logger logger;
	private String mailFrom = "abcdef@gmail.com";
	private String mailTo = "abcdef@gmail.com";
	private String server = "186.67.251.159";

	public static void main(String[] args) {
		PropertyConfigurator.configure(ArmsServerConstants.LOG4J_PROP_FILE_NAME);
		logger = Logger.getLogger(EmailSender.class);
		String mailFrom = "abcdef@elrosado.com";
		String mailTo = "abcdef@gmail.com";
		String server = "localhost";

		// Obtenemos las propiedades del sistema
		Properties propiedades = System.getProperties();

		// Configuramos el servidor de correo
		propiedades.setProperty("mail.smtp.host", server);

		// Obtenemos la sesión por defecto
		Session sesion = Session.getDefaultInstance(propiedades);

		try {
			// Creamos un objeto mensaje tipo MimeMessage por defecto.
			MimeMessage mensaje = new MimeMessage(sesion);

			// Asignamos el “de o from” al header del correo.
			mensaje.setFrom(new InternetAddress(mailFrom));

			// Asignamos el “para o to” al header del correo.
			mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));

			// Asignamos el asunto
			mensaje.setSubject("ACTUALIZACIÓN DE PRECIOS");

			// Asignamos el mensaje como tal
			mensaje.setText("Atención! Se ha recibido un lote de actualización de precios a procesar.");

			// Enviamos el correo
			Transport.send(mensaje);
			System.out.println("Mensaje enviado");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void init() {
		PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
		if (null == properties)
			throw new NullPointerException("cannot load the properties file LSconf.properties");

		mailFrom = properties.getObject("searchItem.email.from").trim();
		mailTo = properties.getObject("searchItem.email.to").trim();
		server = properties.getObject("searchItem.email.server").trim();
	}

	public void send(String filename) {
		try {
			init();
			// Obtenemos las propiedades del sistema
			Properties propiedades = System.getProperties();

			// Configuramos el servidor de correo
			propiedades.setProperty("mail.smtp.host", server);

			// Obtenemos la sesión por defecto
			Session sesion = Session.getDefaultInstance(propiedades);
			// Creamos un objeto mensaje tipo MimeMessage por defecto.
			MimeMessage mensaje = new MimeMessage(sesion);

			// Asignamos el “de o from” al header del correo.
			mensaje.setFrom(new InternetAddress(mailFrom));

			// Asignamos el “para o to” al header del correo.
			mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));

			// Asignamos el asunto
			mensaje.setSubject("ACTUALIZACIÓN DE PRECIOS");

			// Asignamos el mensaje como tal
			mensaje.setText(
					"Atención! Se ha recibido un lote de actualización de precios a procesar. Archivo a procesar: "
							+ filename);

			// Enviamos el correo
			Transport.send(mensaje);
			System.out.println("Mensaje enviado");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initDataFast() {
		PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
		if (null == properties)
			throw new NullPointerException("cannot load the properties file LSconf.properties");

		mailFrom = properties.getObject("cnlDatafast.email.from").trim();
		mailTo = properties.getObject("cnlDatafast.email.to").trim();
		server = properties.getObject("cnlDatafast.email.server").trim();
	}

	public void sendDataFast(List filenames, Map tamFiles) {
		try {
			logger.info("Inicio proceso de envio de correo de notificación para DataFast.");
			initDataFast();
			String filename = (String)filenames.get(0);
			// Obtenemos las propiedades del sistema
			Properties propiedades = System.getProperties();

			// Configuramos el servidor de correo
			propiedades.setProperty("mail.smtp.host", server);

			// Obtenemos la sesión por defecto
			Session sesion = Session.getDefaultInstance(propiedades);
			// Creamos un objeto mensaje tipo MimeMessage por defecto.
			MimeMessage mensaje = new MimeMessage(sesion);

			// Asignamos el “de o from” al header del correo.
			mensaje.setFrom(new InternetAddress(mailFrom));
			
			// Asignamos el “para o to” al header del correo.
			mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));

			// Asignamos el asunto
			mensaje.setSubject("Cierre Datafast " + filename.substring(0, 2) + "-" + filename.substring(2, 4) + "-" + filename.substring(4, 8));
			
			String mensajeEnviar = "Estimados\n" + "Se han transferido los siguientes archivos: \n" ;
			
			if(filenames != null && !filenames.isEmpty()){
				Iterator itFiles = filenames.iterator();
				while (itFiles.hasNext()) {
					String nombreArch = (String) itFiles.next();
					Long tamArch = (Long)tamFiles.get(nombreArch);
					mensajeEnviar = mensajeEnviar + nombreArch + "   " + String.valueOf(tamArch) + "\n";
				}
				mensajeEnviar = mensajeEnviar + "\n" + "TOTAL ARCHIVOS ENVIADOS: " + filenames.size();
			}
			
			// Asignamos el mensaje como tal
			mensaje.setText(mensajeEnviar);

			// Enviamos el correo
			Transport.send(mensaje);
			System.out.println("Mensaje enviado");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initMedianet() {
		PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
		if (null == properties)
			throw new NullPointerException("cannot load the properties file LSconf.properties");

		mailFrom = properties.getObject("cnlMedianet.email.from").trim();
		mailTo = properties.getObject("cnlMedianet.email.to").trim();
		server = properties.getObject("cnlMedianet.email.server").trim();
	}

	public void sendMedianet(List filenames, Map tamFiles) {
		try {
			logger.info("Inicio proceso de envio de correo de notificación para Medianet.");
			initMedianet();
			String filename = (String)filenames.get(0);
			String[] parts = filename.split("_");
			String fecha = parts[1].substring(0	, 8);
			// Obtenemos las propiedades del sistema
			Properties propiedades = System.getProperties();

			// Configuramos el servidor de correo
			propiedades.setProperty("mail.smtp.host", server);

			// Obtenemos la sesión por defecto
			Session sesion = Session.getDefaultInstance(propiedades);
			// Creamos un objeto mensaje tipo MimeMessage por defecto.
			MimeMessage mensaje = new MimeMessage(sesion);

			// Asignamos el “de o from” al header del correo.
			mensaje.setFrom(new InternetAddress(mailFrom));

			// Asignamos el “para o to” al header del correo.
			mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));

			// Asignamos el asunto
			mensaje.setSubject("Cierre CREDIMATIC " + fecha);
			
			String mensajeEnviar = "Estimados\n" + "Se han transferido los siguientes archivos: \n" ;
			
			if(filenames != null && !filenames.isEmpty()){
				Iterator itFiles = filenames.iterator();
				while (itFiles.hasNext()) {
					String nombreArch = (String) itFiles.next();
					Long tamArch = (Long)tamFiles.get(nombreArch);
					mensajeEnviar = mensajeEnviar + nombreArch + "   " + String.valueOf(tamArch) + "\n";
				}
				mensajeEnviar = mensajeEnviar + "\n" + "TOTAL ARCHIVOS ENVIADOS: " + filenames.size();
			}

			// Asignamos el mensaje como tal
			mensaje.setText(mensajeEnviar);

			// Enviamos el correo
			Transport.send(mensaje);
			System.out.println("Mensaje enviado");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
