package com.allc.arms.server.processes.cer.moto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.params.ParamValue;
import com.allc.arms.server.params.ParamsDAO;
import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.moto.carta.CartaMoto;
import com.allc.arms.server.persistence.moto.carta.CartaMotoDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;

public class SendCartaMotoProcess extends AbstractProcess {

	private static Logger log = Logger.getLogger(SendCartaMotoProcess.class);

	private Session session = null;
	private Session sessionSAADMIN = null;
	public boolean isEnd = false;
	protected boolean finished = false;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	public final Integer TOSEND = new Integer(1);
	public final Integer SENT = new Integer(2);
	private File outFolder;
	private File bkpFolder;
	private int sleepTime;
	private String serverIP = null;
	private String username = null;
	private String password = null;
	private int retries;

	protected void inicializar() {
		isEnd = false;
		try {
			iniciarSesionSAADMIN();
			ParamsDAO paramsDAO = new ParamsDAO();
			String store = properties.getObject("eyes.store.code");
			String suiteDir = properties.getObject("SUITE_ROOT");
			String clave = "DIR_CVM";
			log.info("Store: "+ store);
			ParamValue paravalue = paramsDAO.getParamByClave(sessionSAADMIN, Integer.valueOf(store).toString(), ArmsServerConstants.AmbitoParams.DIR_INTERFACE, clave);
			log.info("Valor del parametro consultado a la BD: " + paravalue.getValor());
			outFolder = new File(suiteDir + paravalue.getValor() + "/OUT");
			bkpFolder = new File(suiteDir + paravalue.getValor() + "/BKP");
			log.info("USER: " + properties.getObject("sendCartaMoto.username"));
			log.info("SLEEP: " + properties.getObject("sendCartaMoto.sleeptime"));
			sleepTime = properties.getInt("sendCartaMoto.sleeptime");
			retries = properties.getInt("sendCartaMoto.retries");
			username = properties.getObject("sendCartaMoto.username");
			password = properties.getObject("sendCartaMoto.password");
			serverIP = properties.getObject("sendCartaMoto.server.serverIP");
			sessionSAADMIN.close();
			sessionSAADMIN = null;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void run() {
		List list;
		log.info("Iniciando SendCartaMotoProcess...");

		
		inicializar();
		while (!isEnd) {

			log.info("Buscando cartas de motos para enviar...");
			String store = properties.getObject("eyes.store.code");
			CartaMotoDAO cartaMotoDAO = new CartaMotoDAO();
			try {
				iniciarSesion();
				List cartasMotosToSend = cartaMotoDAO.getCartasMotosToSend(session, TOSEND);
				if (cartasMotosToSend != null && !cartasMotosToSend.isEmpty()) {
					log.info("Se enviaran " + cartasMotosToSend.size() + " cartas de Moto.");
					Iterator itCartasMotos = cartasMotosToSend.iterator();
					while (itCartasMotos.hasNext()) {
						CartaMoto cartaMoto = null;
						try {
							cartaMoto = (CartaMoto) itCartasMotos.next();
							UtilityFile.createWriteDataFile(getEyesFileName(),
									"SEND_CARTA_MOT_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + store + "|STR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
													.format(new Date())
											+ "|Iniciando el envio de la Carta Moto: " + cartaMoto.getNombArchXML()
											+ ".\n",
									true);
							String nombreXMLCartaMoto = cartaMoto.getNombArchXML();
							File cartaMotoFile = new File(outFolder, nombreXMLCartaMoto);

							boolean enviado = sendCartaMotoToServer(nombreXMLCartaMoto);

							if (enviado) {
								cartaMotoFile.renameTo(new File(bkpFolder, cartaMotoFile.getName()));
								cartaMoto.setEstado(SENT);
								cartaMotoDAO.updateCartaMoto(session, cartaMoto);
								
								log.info("Archivo movido a la carpeta Bkp.");

								UtilityFile.createWriteDataFile(getEyesFileName(),
										"SEND_CARTA_MOT_P|" + properties.getHostName() + "|3|"
												+ properties.getHostAddress() + "|" + store + "|END|"
												+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
														.format(new Date())
												+ "|Carta Moto: " + cartaMoto.getNombArchXML() + " enviada.\n",
										true);
							} else {
								try {
									Thread.sleep(sleepTime);
								} catch (InterruptedException e) {
									log.error(e.getMessage(), e);
								}
							}
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
					}
				} else {
					
					log.info("No hay Cartas de Motos para enviar.");
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						log.error(e.getMessage(), e);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				try {
					if (session != null) {
						session.close();
						session = null;
					}
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}

			}
		}
		finished = true;
	}

	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	private void iniciarSesion() {
		while (session == null && !isEnd) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory("CVMT").openSession();
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

	protected void iniciarSesionSAADMIN() {
		while (sessionSAADMIN == null) {
			try {
				sessionSAADMIN = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionSAADMIN == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	public boolean sendCartaMotoToServer(String nombreCartaMoto) {

		FTPClient ftpClient = null;
		int reintentos = 0;
		
		log.info("Inicio de transferencia de carta al servidor SRI." + nombreCartaMoto);
		
		while (reintentos < retries) {
			log.info("Carta a transferir: " + nombreCartaMoto);
			try {
				ftpClient = new FTPClient();
				ftpClient.connect(serverIP);
				ftpClient.login(username, password);
				ftpClient.enterLocalPassiveMode();
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
				File firstLocalFile = new File(outFolder, nombreCartaMoto);
				String firstRemoteFile = nombreCartaMoto;
				InputStream inputStream = new FileInputStream(firstLocalFile);
				boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
				inputStream.close();
				try {
					if (ftpClient != null && ftpClient.isConnected()) {
						ftpClient.logout();
						ftpClient.disconnect();
					}
				} catch (IOException ex) {
					log.error(ex.getMessage(), ex);
				}
				if (done) {
					log.info("La carta fue transferida con exito.");
					return true;
				} else
					reintentos++;

			}catch (Exception e) {
				log.error("Se produjo un error durante el intento de transferencia.");
				log.error(e.getMessage(), e);
				reintentos++;

			}
		}
		return false;
	}

	@Override
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo SendCartaMotoProcess...");
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
		log.info("Finalizó el Proceso de Envío de Cartas de Moto.");
		return true;
	}

}
