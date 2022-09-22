/**
 * 
 */
package com.allc.arms.server.processes.cer.moto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.item.ItemDAO;
import com.allc.arms.server.persistence.moto.MotoDAO;
import com.allc.arms.server.persistence.moto.file.MotoFile;
import com.allc.arms.server.persistence.moto.file.MotoFileDAO;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.entities.Item;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * @author gustavo
 *
 */
public class SearchMotoFileProcess extends AbstractProcess {
	private static Logger log = Logger.getLogger(SearchMotoFileProcess.class);
	private File inFolder;
	private File prcFolder;
	private File bkpFolder;
	private int sleepTime;
	private Iterator filesToProcess = null;
	public boolean isEnd = false;
	private Session session = null;
	private Session sessionFleje = null;
	private Session sessionSaAdmin = null;
	protected boolean finished = false;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	protected ConnSocketClient socketClient;
	StoreDAO storeDAO = new StoreDAO();
	private static Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	protected Store centralStore;
	
	protected void inicializar() {
		isEnd = false;
		try {
			iniciarSaAdminSesion();
			inFolder = new File(properties.getObject("searchMoto.in.folder.path"));
			inFolder.mkdirs();
			prcFolder = new File(properties.getObject("searchMoto.prc.folder.path"));
			prcFolder.mkdirs();
			bkpFolder = new File(properties.getObject("searchMoto.bkp.folder.path"));
			bkpFolder.mkdirs();
			sleepTime = properties.getInt("searchMoto.sleeptime");
			centralStore = storeDAO.getStoreByCode(sessionSaAdmin, 0);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void run() {
		log.info("Iniciando SearchMotoFileProcess...");
		inicializar();
		ItemDAO itemDAO = new ItemDAO();
		MotoDAO motoDAO = new MotoDAO();
		MotoFileDAO motoFileDAO = new MotoFileDAO();
		String store = properties.getObject("eyes.store.code");
		while (!isEnd) {
			String filename = null;
			try {
				iniciarSesion();
				iniciarFlejeSesion();
				iniciarSaAdminSesion();
				File motoFile = getNextItemFile();

				if (motoFile != null) {
					filename = motoFile.getName().toUpperCase();
					String[] parts = filename.split("\\.");
					String sequence = parts[1];
					String fecha = parts[0].substring(2, 8);
					UtilityFile
							.createWriteDataFile(getEyesFileName(),
									"SRCH_MOTO_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + store + "|STR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
													.format(new Date())
											+ "|Archivo a Procesar: " + filename + ".\n",
									true);
					log.info("Archivo a procesar: " + filename);

					if (!motoFileDAO.existeMotoFile(sessionFleje, filename)) {
						String storeCode = parts[1];

						FileReader fr = new FileReader(motoFile);
						BufferedReader br = new BufferedReader(fr);
						BufferedWriter writer = null;
						File errorItemFile = null;
						Map filesMap = new HashMap();
						String linea = br.readLine();
						while (linea != null) {
							if (linea.length() < 320) {
								if (errorItemFile == null) {
									errorItemFile = new File(inFolder, "ERRM" + fecha + "." + sequence);
									writer = new BufferedWriter(
											new OutputStreamWriter(new FileOutputStream(errorItemFile)));
								}
								writer.write(linea);
								writer.newLine();
							} else {
								log.info("Se agrega MOLOTE");
								if (motoDAO.motoVendida(session, linea.substring(41, 61))
										|| motoDAO.existeMotoByNumSerie(session, linea.substring(41, 61))) {
									log.error("Moto ya existente. Serial: " + linea.substring(41, 61));
									UtilityFile
									.createWriteDataFile(getEyesFileName(),
											"SRCH_MOTO_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
													+ "|" + store + "|ERR|"
													+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
															.format(new Date())
													+ "|Moto ya existente. Serial: " + linea.substring(41, 61) +".\n",
											true);
								} else {
									String codSap = linea.substring(3, 21);
									String codTienda = linea.substring(315, 319).trim();
									Item item = itemDAO.getItem(session, codSap);
									String codItem = Util.lpad((item != null && item.getItemCode() != null
											? item.getItemCode().toString() : ""),
											ArmsServerConstants.Communication.CERO, 12);
									File loteFile = null;
									if (!filesMap.containsKey(codTienda))
										filesMap.put(codTienda, "MOLOTE" + sequence + "." + codTienda);

									loteFile = new File(inFolder, (String) filesMap.get(codTienda));
									BufferedWriter bw = new BufferedWriter(new FileWriter(loteFile, true));
									bw.write(linea + codItem);
									bw.newLine();
									bw.close();
								}
							}
							linea = br.readLine();
						}
						br.close();
						fr.close();
						if (writer != null)
							writer.close();
						motoFile.renameTo(new File(bkpFolder, motoFile.getName()));
						MotoFile motoFileToSave = new MotoFile();
						motoFileToSave.setMotoFileName(filename);
						motoFileDAO.insertaMotoFile(sessionFleje, motoFileToSave);
						log.info("Moto File registrado.");
						if (errorItemFile != null)
							errorItemFile.renameTo(new File(bkpFolder, errorItemFile.getName()));
						Iterator itFiles = filesMap.values().iterator();
						while (itFiles.hasNext()) {
							String name = (String) itFiles.next();
							String[] partsAux = name.split("\\.");
							String codStore = partsAux[1];
							log.info("MotoLote tienda: " + codStore);
							File file = new File(inFolder, name);
							if (Integer.valueOf(codStore) > 0
									&& storeDAO.hayServidorLocal(sessionSaAdmin, Integer.valueOf(codStore))) {
								Store tiendaAux = storeDAO.getStoreByCode(sessionSaAdmin, Integer.valueOf(codStore));
								log.info("IP TIENDA: " + tiendaAux.getIp());
								enviarArchivoATienda(file, codStore, tiendaAux.getIp());

							}
							file.renameTo(new File(prcFolder, file.getName()));
							UtilityFile.createWriteDataFile(getEyesFileName(),
									"SRCH_MOTO_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|"
											+ store + "|PRC|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
													.format(new Date())
											+ "|Archivo Generado: " + file.getName() + ".\n",
									true);
						}
						UtilityFile
								.createWriteDataFile(getEyesFileName(),
										"SRCH_MOTO_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
												+ "|" + store + "|END|"
												+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
														.format(new Date())
												+ "|Archivo: " + filename + " procesado.\n",
										true);

					} else {
						UtilityFile.createWriteDataFile(getEyesFileName(),
								"SRCH_MOTO_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
										+ store + "|END|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|El archivo: " + filename + " ya estaba registrado.\n",
								true);
						log.info("Archivo ya estaba registrado.");
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					UtilityFile
							.createWriteDataFile(getEyesFileName(),
									"SRCH_MOTO_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + store + "|ERR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(
													new Date())
											+ "|Error al procesar el archivo: " + filename + ".\n",
									true);
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
			} finally {
				sessionFleje.close();
				session.close();
				sessionSaAdmin.close();
				session = null;
				sessionFleje = null;
				sessionSaAdmin = null;
			}
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
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
				session = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
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

	private void iniciarFlejeSesion() {
		while (sessionFleje == null && !isEnd) {
			try {
				sessionFleje = HibernateSessionFactoryContainer.getSessionFactory("Flejes").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionFleje == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	private void iniciarSaAdminSesion() {
		while (sessionSaAdmin == null && !isEnd) {
			try {
				sessionSaAdmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionSaAdmin == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
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
						return pathname.isFile() && pathname.getName().toUpperCase().startsWith("MO")
								&& !pathname.getName().toUpperCase().startsWith("MOLOTE");
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
							int sequence1 = 0;
							String name2 = ((File) obj2).getName().toUpperCase();
							int sequence2 = 0;
							sequence1 = Integer.parseInt(name1.substring(4, 8));
							sequence2 = Integer.parseInt(name2.substring(4, 8));
							if (sequence1 == sequence2) {
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

	public boolean enviarArchivoATienda(File motoFileToSend, String tienda, String tiendaIp) {

		try {
			StringBuffer data = new StringBuffer();
			data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(ArmsServerConstants.Process.FILE_SENDER_OPERATION)
					.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
					.append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
					.append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(ArmsServerConstants.Communication.PERM_CONN)
					.append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
					.append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(motoFileToSend.getAbsolutePath())
					.append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(prcFolder.getAbsolutePath())
					.append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(tiendaIp)
					.append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(properties.getObject("serverSocket.port"));

			List list = Arrays.asList(p.split(data.toString()));
			Frame frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
					ArmsServerConstants.Communication.FRAME_SEP);
			if (frame.loadData()) {
				boolean send = sendFrame(frame, properties, centralStore.getIp());

				if (send) {
					log.info("Archivo " + motoFileToSend.getName() + " enviado correctamente a la tienda: " + tienda
							+ ".");
				} else {
					log.error(
							"Error al enviar el archivo " + motoFileToSend.getName() + " a la tienda: " + tienda + ".");
				}
			}
			return true;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return false;
	}

	protected boolean sendFrame(Frame frame, PropFile properties, String tiendaIP) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected())
				connectClient(properties, tiendaIP);
			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
			log.info("Trama a enviar: " + trama);
			if (socketClient.writeDataSocket(trama)) {
				int numberOfBytes = 0;
				int timeOutCycles = 0;
				while (numberOfBytes == 0) {
					numberOfBytes = socketClient.readLengthDataSocket();
					if (timeOutCycles == 5) {
						// cada 5 timeouts escribimos una trama vacía para
						// asegurarnos que el socket esté activo
						String mje = Util.addLengthStartOfString("", qtyBytesLength).toString();
						if (!socketClient.writeDataSocket(mje)) {
							socketClient.setConnected(false);
							return false;
						}
						timeOutCycles = 0;
					}
					timeOutCycles++;
				}
				if (numberOfBytes > 0) {
					str = socketClient.readDataSocket(numberOfBytes);
					if (StringUtils.isNotBlank(str)) {
						list = Arrays.asList(p.split(str));
						frameRpta = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
								ArmsServerConstants.Communication.FRAME_SEP);
						log.info("Respuesta recibida: " + frameRpta.toString());
						if (frameRpta.getStatusTrama() == 0) {
							return true;
						}
					}
				}
				closeClient();
			} else {
				socketClient.setConnected(false);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			socketClient.setConnected(false);
		}
		return false;
	}

	protected boolean connectClient(PropFile properties, String tiendaIP) {

		if (socketClient == null || !socketClient.isConnected()) {
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(tiendaIP);
			socketClient.setPortServer(properties.getInt("serverSocket.port"));
			socketClient.setRetries(3);
			socketClient.setTimeOutConnection(properties.getInt("serverSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("serverSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("serverSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}

	protected void closeClient() {
		if (socketClient != null)
			socketClient.closeConnection();
	}

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo SearchMotoFileProcess...");
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
		log.info("Finalizó el Proceso de Búsqueda de Archivos de Moto.");
		return true;
	}

}
