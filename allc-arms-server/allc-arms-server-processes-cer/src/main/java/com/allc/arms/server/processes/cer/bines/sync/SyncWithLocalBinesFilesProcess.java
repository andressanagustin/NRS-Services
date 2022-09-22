package com.allc.arms.server.processes.cer.bines.sync;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.allc.arms.server.params.ParamValue;
import com.allc.arms.server.params.ParamsDAO;
import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class SyncWithLocalBinesFilesProcess extends AbstractProcess {

	private static Logger logger = Logger.getLogger(SyncWithLocalBinesFilesProcess.class);
	private boolean isEnd = false;
	protected boolean finished = false;
	private Iterator filesToSend = null;
	private int sleepTime;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	private Session session = null;
	private String store = null;
	protected ConnSocketClient socketClient;
	StoreDAO storeDAO = new StoreDAO();
	private static Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	String absolutePath = null;

	protected void inicializar() {
		try {

			iniciarSesionSaadmin();
			sleepTime = properties.getInt("syncBinFilesProces.sleeptime");
			absolutePath = properties.getObject("syncBinFilesProces.in.folder.path");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void run() {
		logger.info("Iniciando SyncWithLocalBinesFilesProcess...");
		inicializar();
		File fileToSend = null;
		File inFolder = null;
		while (!isEnd) {
			try {

				ParamsDAO paramsDAO = new ParamsDAO();

				ParamValue paravalue = paramsDAO.getParamByClave(session, "000",
						ArmsServerConstants.AmbitoParams.SUITE_PARAMS, "SYNC_UPDATE");

				List activeStoreWihtLocalServer = getAllActiveStore(1);

				logger.info("Cantidad de tiendas activas: " + activeStoreWihtLocalServer.size());

				if (activeStoreWihtLocalServer != null && !activeStoreWihtLocalServer.isEmpty()) {
					Iterator itStore = activeStoreWihtLocalServer.iterator();
					while (!isEnd && itStore.hasNext()) {

						Store tienda = (Store) itStore.next();
						String tiendaCode = tienda.getKey().toString();
						while (tiendaCode.length() < 3)
							tiendaCode = "0" + tiendaCode;

						logger.info("FilePath para buscar archivos: " + absolutePath + File.separator + tiendaCode
								+ File.separator + paravalue.getValor());

						inFolder = new File(
								absolutePath + File.separator + tiendaCode + File.separator + paravalue.getValor());
						inFolder.mkdirs();

						do {
							fileToSend = getNextBinesFile(inFolder, tiendaCode);

							if (fileToSend != null) {
								logger.info("Archivo a enviar: " + fileToSend.getName().toUpperCase());
								UtilityFile.createWriteDataFile(getEyesFileName(),
										"SYNC_BINES_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
												+ "|" + store + "|STR|"
												+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
														.format(new Date())
												+ "|Archivo a enviar: " + fileToSend.getName() + ".\n",
										true);

								boolean enviado = enviarArchivoATiendaLocal(inFolder, fileToSend, tienda.getKey().toString());

								if (enviado) {
									fileToSend.delete();
									logger.info("Archivo " + fileToSend.getName()
											+ " enviado correctamente a la tienda local: " + tiendaCode + ".");
								} else
									logger.info("Error al enviar el archivo " + fileToSend.getName()
											+ " a la tienda local: " + tiendaCode + ".");
							}

						} while (!isEnd && filesToSend != null && filesToSend.hasNext());

					}
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
				} else {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
				}

			} catch (Exception e) {
				UtilityFile.createWriteDataFile(getEyesFileName(),
						"SYNC_BINES_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store
								+ "|ERR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al enviar el archivo: " + fileToSend.getName() + ".\n",
						true);
				logger.error(e.getMessage(), e);
			}
		}
		cierraSesion();
		finished = true;
	}

	public boolean enviarArchivoATiendaLocal(File folder, File fileToProcess, String tienda) {

		Store store = storeDAO.getStoreByCode(session, Integer.valueOf(tienda));

		StringBuffer data = new StringBuffer();
		data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(ArmsServerConstants.Process.FILE_SENDER_OPERATION)
				.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
				.append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
				.append(ArmsServerConstants.Communication.FRAME_SEP).append(ArmsServerConstants.Communication.TEMP_CONN)
				.append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
				.append(ArmsServerConstants.Communication.FRAME_SEP).append(fileToProcess.getAbsolutePath())
				.append(ArmsServerConstants.Communication.FRAME_SEP).append(folder.getAbsolutePath())
				.append(ArmsServerConstants.Communication.FRAME_SEP).append(store.getIp())
				.append(ArmsServerConstants.Communication.FRAME_SEP).append(properties.getObject("serverSocket.port"));

		List list = Arrays.asList(p.split(data.toString()));
		Frame frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
				ArmsServerConstants.Communication.FRAME_SEP);
		if (frame.loadData()) {
			boolean send = sendFrame(frame, properties, 0);
			closeClient();
			if (send) {
				logger.info("Archivo " + fileToProcess.getName() + " enviado correctamente a la tienda: "
						+ store.getKey() + ".");
				return true;
			} else {
				logger.error("Error al enviar el archivo " + fileToProcess.getName() + " a la tienda: " + store.getKey()
						+ ".");
			}
		}

		return false;
	}

	protected boolean sendFrame(Frame frame, PropFile properties, Integer tienda) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected())
				connectClient(properties, tienda);
			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
			logger.info("Trama a enviar: " + trama);
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
						logger.info("Respuesta recibida: " + frameRpta.toString());
						if (frameRpta.getStatusTrama() == 0) {
							return true;
						}
					}
				}
			} else {
				socketClient.setConnected(false);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			socketClient.setConnected(false);
		}
		return false;
	}

	private File getNextBinesFile(File folder, String codTienda) {
		logger.info("Buscando archivos de actualizacion de Bines para la tienda: " + codTienda + ".");
		
		do {
			if (isEnd)
				return null;
			File[] files = null;
			if ((this.filesToSend == null) || !filesToSend.hasNext()) {
				files = folder.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isFile();
					}
				});
			}
			if (files!=null && files.length != 0) {

				Arrays.sort(files, new Comparator() {
					public int compare(Object obj1, Object obj2) {
						try {
							String name1 = ((File) obj1).getName().toUpperCase();

							String name2 = ((File) obj2).getName().toUpperCase();

							String ext1 = name1.substring(name1.length() - 3, name1.length());
							String ext2 = name2.substring(name2.length() - 3, name2.length());

							if (ext1.equalsIgnoreCase("CMD") && !ext2.equalsIgnoreCase("CMD"))
								return -1;
							else if (!ext1.equalsIgnoreCase("CMD") && ext2.equalsIgnoreCase("CMD"))
								return 1;
							
							return (-1) * name1.compareTo(name2);
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
						return 1;
					}
				});
				this.filesToSend = Arrays.asList(files).iterator();
			} else {
				this.filesToSend = null;
				return null;
			}
		} while (((this.filesToSend == null) || !filesToSend.hasNext()));
		return (File) this.filesToSend.next();
	}

	protected void iniciarSesionSaadmin() {
		while (session == null) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
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

	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}

	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	private void cierraSesion() {
		if (session != null) {
			try {
				session.close();
				session = null;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public List getAllActiveStore(Integer flLocalServer) {

		try {
			Query query = session
					.createQuery("from com.allc.arms.server.persistence.store.Store where status = 1 and localServer = "
							+ flLocalServer);
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return rows;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	protected boolean connectClient(PropFile properties, Integer tienda) {

		String storeIP = storeDAO.getStoreByCode(session, tienda).getIp();

		if (socketClient == null || !socketClient.isConnected()) {
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(storeIP);
			socketClient.setPortServer(properties.getInt("serverSocket.port"));
			socketClient.setRetries(2);
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

	@Override
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		logger.info("Deteniendo SyncWithLocalBinesFilesProcess...");
		while (!finished)
			try {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= timeToWait) {
					return false;
				}
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		logger.info("Finalizó el Proceso de Sincronizacion de archivos de bines con los locales.");
		return true;
	}

}
