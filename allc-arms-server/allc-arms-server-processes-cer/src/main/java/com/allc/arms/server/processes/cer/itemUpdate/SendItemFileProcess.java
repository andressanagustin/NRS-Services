/**
 * 
 */
package com.allc.arms.server.processes.cer.itemUpdate;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.core.process.AbstractProcess;
import com.allc.properties.PropFile;

/**
 * Proceso encargado de enviar los archivos de actualización de precios y códigos de barra a los controladores.
 * 
 * @author gustavo
 *
 */
public class SendItemFileProcess extends AbstractProcess {
	private static Logger log = Logger.getLogger(SendItemFileProcess.class);
	private File inFolder;
	private File outFolder;
	private File serverFolder;
	private int sleepTime;
	private Iterator filesToProcess = null;
	public boolean isEnd = false;
	protected boolean finished = false;
	private Session sesion;
	private int port = 0;
	private String username = null;
	private String password = null;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);

	protected void inicializar() {
		isEnd = false;
		try {
			inFolder = new File(properties.getObject("sendItem.in.folder.path"));
			outFolder = new File(properties.getObject("sendItem.out.folder.path"));
			serverFolder = new File(properties.getObject("sendItem.server.folder.path"));
			sleepTime = properties.getInt("sendItem.sleeptime");
			username = properties.getObject("sendItem.username");
			password = properties.getObject("sendItem.password");
			port = properties.getInt("sendItem.server.port");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void run() {
		log.info("Iniciando SendItemFileProcess...");
		inicializar();
		StoreDAO storeDAO = new StoreDAO();
		while (!isEnd) {
			FTPClient ftpClient = null;
			try {
				File itemFile = getNextItemFile();

				if (itemFile != null) {
					iniciaOperacion();
					String filename = itemFile.getName().toUpperCase();
					log.info("Archivo a transferir: " + filename);

					String storeCode = filename.split("\\.")[1];
					log.info("Obteniendo configuración de la tienda: " + storeCode);
					Store store = storeDAO.getStoreByCode(sesion, Integer.valueOf(storeCode));
					log.info("Conf obtenida. IP: " + store.getIp());
					ftpClient = new FTPClient();
					ftpClient.connect(store.getIp(), port);
					ftpClient.login(username, password);
					ftpClient.enterLocalPassiveMode();
					ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

					File firstLocalFile = new File(inFolder, filename);
					String firstRemoteFile = serverFolder + UtilityFile.fileSeparator() + filename;
					InputStream inputStream = new FileInputStream(firstLocalFile);
					boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
					inputStream.close();
					if (done) {
						log.info("Transferencia realizada con éxito.");
						itemFile.renameTo(new File(outFolder, itemFile.getName()));
						log.info("Archivo movido a la carpeta Out.");
					} else {
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							log.error(e.getMessage(), e);
						}
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			finally {
				try {
					if(sesion!=null){
						sesion.close();
						sesion = null;
					}
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
				try {
					if (ftpClient != null && ftpClient.isConnected()) {
						ftpClient.logout();
						ftpClient.disconnect();
					}
				} catch (IOException ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		}
		finished = true;
	}

	public void iniciaOperacion() {
		while (sesion == null && !isEnd) {
			try {
				sesion = HibernateSessionFactoryContainer.getSessionFactory("Store").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				sesion = null;
			}
			if (sesion == null)
				try {
					log.error("OCURRIÓ UN ERROR AL CREAR LA CONEXIÓN A LA BD, SE REINTENTARÁ EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	private File getNextItemFile() {
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd)
					return null;
				File[] files = inFolder.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						log.info("Pathname: " + pathname.getName().toUpperCase());
						return pathname.isFile()
								&& (pathname.getName().toUpperCase().startsWith("LO") || pathname.getName().toUpperCase()
										.startsWith("PE"));
					}
				});
				if (files.length == 0) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						log.error(e.getMessage(), e);
					}
				} else {
					Arrays.sort(files, new Comparator() {
						public int compare(Object obj1, Object obj2) {
							String name1 = ((File) obj1).getName().toUpperCase();
							String name2 = ((File) obj2).getName().toUpperCase();
							int sequence1 = Integer.parseInt(name1.substring(2, 6));
							int sequence2 = Integer.parseInt(name2.substring(2, 6));

							if (sequence1 == sequence2) {
								if(name1.startsWith("PE") && name2.startsWith("LO"))
									return -1;
								if(name1.startsWith("LO") && name2.startsWith("PE"))
									return 1;
								return 0;
							}
							if (sequence1 < sequence2) {
								return -1;
							}
							return 1;

						}
					});
					this.filesToProcess = Arrays.asList(files).iterator();
				}
			} while (((this.filesToProcess == null) || !filesToProcess.hasNext()));
		}
		return (File) this.filesToProcess.next();
	}

	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}

	@Override
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo SendItemFileProcess...");
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
		log.info("Finalizó el Proceso de Envío de Archivos de Ítems.");
		return true;
	}

}
