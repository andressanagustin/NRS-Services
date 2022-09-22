/**
 * 
 */
package com.allc.arms.server.processes.journal;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.params.ParamValue;
import com.allc.arms.server.params.ParamsDAO;
import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * @author gustavo
 *
 */
public class UpdateJournalsFileProcess extends AbstractProcess {
	private static Logger log = Logger.getLogger(UpdateJournalsFileProcess.class);
	private File outFolder;
	private File eyesFolder;
	private File syncFolder;
	private int sleepTime;
	private Iterator filesToProcess = null;
	public boolean isEnd = false;
	private Session sessionSaAdmin = null;
	protected boolean finished = false;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	protected ConnSocketClient socketClient;
	StoreDAO storeDAO = new StoreDAO();
	private static Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	protected Store centralStore;
	protected String storeCode;
	protected HashMap activeStoreIp = new HashMap();
	protected List pathActiveStore = new ArrayList();
	private int storeCodeHasta;
	private File syncWithCentral;
	private String storeLocalIP;
	
	protected void inicializar() {
		isEnd = false;
		try {
			iniciarSaAdminSesion();
//			inFolder = new File("C:\\ALLC\\WWW\\allc_dat\\out\\010\\EJ\\j\\");
//			inFolder.mkdirs();
//			sleepTime = 30000;//TODO:properties.getInt("updateJournal.sleeptime");
			sleepTime = properties.getInt("updateJournal.sleeptime");
			centralStore = storeDAO.getStoreByCode(sessionSaAdmin, 0);
			storeCode = properties.getObject("eyes.store.code");
			while (storeCode.length() < 3)
				storeCode = "0" + storeCode;
			ParamsDAO paramsDAO = new ParamsDAO();
			ParamValue paravalue = null;
//			outFolder = new File("C:\\ALLC\\WWW\\EYES\\TSSERVER");//TODO:properties.getObject("updateJournal.out.folder.path"));//
			outFolder = new File(properties.getObject("updateJournal.out.folder.path"));
			outFolder.mkdirs();
			
			paravalue = paramsDAO.getParValSpecifiByClave(sessionSaAdmin,
					Integer.valueOf(properties.getObject("eyes.store.code")),
					ArmsServerConstants.AmbitoParams.ARMS_AGENT_PARAMS, "clientSocket.ip");
			if(storeCode.equals("000"))
				storeLocalIP = centralStore.getIp();
			else
				storeLocalIP = paravalue.getValor();
			
			if (Integer.valueOf(storeCode) == 0) {
				List tiendasActivas = storeDAO.getAllActiveStores(sessionSaAdmin);
				if (tiendasActivas != null && !tiendasActivas.isEmpty()) {

					Iterator itStores = tiendasActivas.iterator();

					while (itStores.hasNext()) {
						Store tienda = (Store) itStores.next();
						String codTienda = tienda.getKey().toString();
						while (codTienda.length() < 3)
							codTienda = "0" + codTienda;
						activeStoreIp.put(codTienda, tienda.getIp());
						
						paravalue = paramsDAO.getParamByClave(sessionSaAdmin, Integer.valueOf(codTienda).toString(),
								ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "SYNC_OUT");
						String dirTemp = properties.getObject("SUITE_ROOT") + paravalue.getValor() + codTienda
								+ File.separator + properties.getObject("updateJournal.in.folder");
//								+ File.separator + "EJ\\J";//TODO:properties.getObject("updateJournal.in.folder");
						log.info("dirTemp:"+dirTemp);
						(new File(dirTemp)).mkdirs();
						pathActiveStore.add(dirTemp);

					}

				} else {
					log.info("No existen tiendas activas disponibles.");
				}
			} else {
				String storeIP = storeDAO.getStoreByCode(sessionSaAdmin, Integer.valueOf(storeCode)).getIp();
				activeStoreIp.put(storeCode, storeIP);
				paravalue = paramsDAO.getParamByClave(sessionSaAdmin, Integer.valueOf(storeCode).toString(),
						ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "SYNC_OUT");
				String dirTemp = properties.getObject("SUITE_ROOT") + File.separator + paravalue.getValor()
						+ File.separator + storeCode + File.separator + properties.getObject("updateJournal.in.folder");
//						+ File.separator + storeCode + File.separator + "EJ\\J";//TODO:properties.getObject("updateJournal.in.folder");
				(new File(dirTemp)).mkdirs();
				pathActiveStore.add(dirTemp);
			}
			storeCodeHasta = properties.getObject("updateJournal.in.folder").length() + 1;

//			storeCodeHasta = "EJ\\J".length()+1;//TODO:properties.getObject("updateJournal.in.folder").length() + 1;
//			syncWithCentral = new File(properties.getObject("updateJournal.out.folder.path") + File.separator
//					+ properties.getObject("updateJournal.sync.folder.path"));
//			syncWithCentral.mkdirs();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void run() {
		log.info("Iniciando UpdateJournalsFileProcess...");
		inicializar();

		String store = properties.getObject("eyes.store.code");

		UtilityFile
		.createWriteDataFile(getEyesFileName(), "UPD_JOUR_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + store + "|STR|"
						+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Iniciando Procesamiento de archivos Journal.\n",true);
		SimpleDateFormat sdf = null;
		try{
			sdf = new SimpleDateFormat("yyyyMMdd");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		String actualDate = sdf.format(new Date());
		String year = actualDate.substring(0, 4);
		String month = actualDate.substring(4, 6);
		String day = actualDate.substring(6, 8);

//		String year = "2017";
//		String month = "09";
//		String day = "21";
		while (!isEnd) {

			String filename = null;
			try {
				iniciarSaAdminSesion();
				File journalFile = getNextJournalFile(getMonthAbr(month), day);
				log.info("Archivo a procesar: "+journalFile);
				if(filesToProcess != null && !filesToProcess.hasNext()){
					actualDate = sdf.format(new Date());
					year = actualDate.substring(0, 4);
					month = actualDate.substring(4, 6);
					day = actualDate.substring(6, 8);
					log.info("Se realizo cambio de fecha.Fecha:  " + day + "-" + month + "-" + year);
//					int monthInt = Integer.valueOf(month).intValue();
//					monthInt++;
//					if(monthInt>12)
//						monthInt=1;
//					month = monthInt < 10 ? "0"+Integer.valueOf(monthInt).toString() : Integer.valueOf(monthInt).toString();
//					int dayInt = Integer.valueOf(day).intValue();
//					dayInt++;
//					if(dayInt>31)
//						dayInt=1;
//					day = dayInt < 10 ? "0"+Integer.valueOf(dayInt).toString() : Integer.valueOf(dayInt).toString();
//					if(monthInt == 9 && dayInt == 07){
//						log.info("Carga inicial finalizada");
//						break;
//					}
				} 
				if(journalFile != null) {
					filename = journalFile.getName().toUpperCase();
					// copiar archivo a carpeta de sync con central
//					if (Integer.valueOf(storeCode) > 0) {
//						String absolutePath = journalFile.getAbsolutePath();
//						String filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
//						FilesHelper.copyFile(filePath, syncWithCentral.getAbsolutePath(), journalFile.getName(),
//								journalFile.getName());
//					}
					
					// obtener del journalFile el codigo de la tienda
					String path1 = journalFile.getAbsolutePath();
					String pathFinal = path1.substring(0, path1.lastIndexOf(File.separator));
					log.info("Path:"+pathFinal+" storeCodeHasta:"+storeCodeHasta);
					String tiendaCode = pathFinal.substring(pathFinal.length() - (storeCodeHasta + 3),
							pathFinal.length() - storeCodeHasta);
					
					String path = outFolder + File.separator + tiendaCode + File.separator + year + File.separator + getMonthName(filename.charAt(1));
					log.info("Archivo destino: "+path);
					UtilityFile.createDir(path);
					UtilityFile.copyFile(filename, pathFinal, path);
					if (Integer.valueOf(storeCode) > 0) {
						enviarArchivoCentral(pathFinal, filename, tiendaCode);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					UtilityFile
							.createWriteDataFile(getEyesFileName(),
									"UPD_JOUR_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + store + "|ERR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(
													new Date())
											+ "|Error al procesar los archivos Journal.\n",
									true);
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
			} finally {
				sessionSaAdmin.close();
				sessionSaAdmin = null;
			}
			try {
				if(!isEnd && (filesToProcess == null || !filesToProcess.hasNext())){
					log.info("Se duerme el proceso.");
					Thread.sleep(sleepTime);
				}
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}

		finished = true;
	}
	
	private void enviarArchivoCentral(String inFolder, String fileToSend, String tienda){
		StringBuffer data = new StringBuffer();
		data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(ArmsServerConstants.Process.FILE_SENDER_OPERATION)
				.append(ArmsServerConstants.Communication.FRAME_SEP).append(tienda)
				.append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
				.append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(ArmsServerConstants.Communication.PERM_CONN)
				.append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
				.append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(inFolder + File.separator + fileToSend)
				.append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(inFolder)
				.append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(centralStore.getIp())
				.append(ArmsServerConstants.Communication.FRAME_SEP)
				.append(properties.getObject("serverSocket.port"));

		List list = Arrays.asList(p.split(data.toString()));
		Frame frame = new Frame(list,
				ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
				ArmsServerConstants.Communication.FRAME_SEP);
		if (frame.loadData()) {
			boolean send = sendFrame(frame, properties, tienda);
			if (send) {
				log.info("Archivo enviado al central: "+fileToSend);
			} else {
				log.error("Error al enviar al server.");
			}
		}
	}
	
	protected boolean sendFrame(Frame frame, PropFile properties, String tienda) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			log.info("IP:"+storeLocalIP);
			connectClient(storeLocalIP);
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
			} else {
				socketClient.setConnected(false);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			socketClient.setConnected(false);
		}
		return false;
	}

	protected boolean connectClient(String ip) {
		if (socketClient != null && !socketClient.getIpServer().equals(ip)) {
			if (socketClient.isConnected())
				socketClient.closeConnection();
			socketClient = null;
		}
		if (socketClient == null || !socketClient.isConnected()) {
			socketClient = new ConnSocketClient();
			log.info("IP:"+ip);
			log.info("port:"+properties.getInt("serverSocket.port"));
			socketClient.setIpServer(ip);
			socketClient.setPortServer(properties.getInt("serverSocket.port"));
			socketClient.setRetries(2);
			socketClient.setTimeOutConnection(properties.getInt("serverSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("serverSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("serverSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}

	protected void closeConnection() {
		if (socketClient != null)
			socketClient.closeConnection();
	}

	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
	
	protected String getMonthName(char m){
		String name = null;
		switch (m) {
		case '1':
			name = "01";
			break;
		case '2':
			name = "02";
			break;
		case '3':
			name = "03";
			break;
		case '4':
			name = "04";
			break;
		case '5':
			name = "05";
			break;
		case '6':
			name = "06";
			break;
		case '7':
			name = "07";
			break;
		case '8':
			name = "08";
			break;
		case '9':
			name = "09";
			break;
		case 'A':
			name = "10";
			break;
		case 'B':
			name = "11";
			break;
		case 'C':
			name = "12";
			break;
		}
		return name;
	}


	protected String getMonthAbr(String m){
		if("01".equalsIgnoreCase(m))
			return "1";
		if("02".equalsIgnoreCase(m))
			return "2";
		if("03".equalsIgnoreCase(m))
			return "3";
		if("04".equalsIgnoreCase(m))
			return "4";
		if("05".equalsIgnoreCase(m))
			return "5";
		if("06".equalsIgnoreCase(m))
			return "6";
		if("07".equalsIgnoreCase(m))
			return "7";
		if("08".equalsIgnoreCase(m))
			return "8";
		if("09".equalsIgnoreCase(m))
			return "9";
		if("10".equalsIgnoreCase(m))
			return "A";
		if("11".equalsIgnoreCase(m))
			return "B";
		if("12".equalsIgnoreCase(m))
			return "C";
		return null;
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
	
	private File getNextJournalFile(final String month, final String day) {
		File inFolder = null;
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd)
					return null;
				Iterator itInFolders = pathActiveStore.iterator();
				File[] files = null;
				List filesStore = new ArrayList();
				int count = 0;
				while (itInFolders.hasNext()) {
					inFolder = new File((String) itInFolders.next());
					File[] tempFiles = inFolder.listFiles(new FileFilter() {
						public boolean accept(File pathname) {
							return pathname.isFile() && pathname.getName().toUpperCase().startsWith("J"+month+day)
							&& !pathname.getName().toUpperCase().endsWith(".C");
						}
					});
					if (tempFiles != null) {
						filesStore.add(tempFiles);
						count = count + tempFiles.length;
					}
				}
				if (count > 0) {
					files = new File[count];
					Iterator itFiles = filesStore.iterator();
					int countAux = 0;
					while (itFiles.hasNext()) {
						File[] temp = (File[]) itFiles.next();
						System.arraycopy(temp, 0, files, countAux, temp.length);
						countAux = countAux + temp.length;
					}
					this.filesToProcess = Arrays.asList(files).iterator();
				} else {
					try {
						Thread.sleep(sleepTime);
						return null;
					} catch (InterruptedException e) {
						log.error(e.getMessage(), e);
					}
				}
			} while (((this.filesToProcess == null) || !filesToProcess.hasNext()));
		}
		return (File) this.filesToProcess.next();
	}

//	private File getNextJournalFile1(final String month, final String day) {
//		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
//			do {
//				if (isEnd)
//					return null;
//				log.info("infolder:"+inFolder);
//				log.info("month:"+month+" day:"+day);
//				File[] files = inFolder.listFiles(new FileFilter() {
//					public boolean accept(File pathname) {
//						log.info("Filename:"+pathname.getName());
//						log.info("return:"+(pathname.isFile() && pathname.getName().toUpperCase().startsWith("J"+month+day)
//								&& !pathname.getName().toUpperCase().endsWith(".C")));
//						return pathname.isFile() && pathname.getName().toUpperCase().startsWith("J"+month+day)
//								&& !pathname.getName().toUpperCase().endsWith(".C");
//					}
//				});
//				if (files.length == 0) {
//					try {
//						Thread.sleep(sleepTime);
//					} catch (InterruptedException e) {
//						log.error(e.getMessage(), e);
//					}
//				} else {
//					this.filesToProcess = Arrays.asList(files).iterator();
//				}
//			} while (((this.filesToProcess == null) || !filesToProcess.hasNext()));
//		}
//		return (File) this.filesToProcess.next();
//	}

	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo UpdateJournalsFileProcess...");
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
		log.info("Finalizó el Proceso de Update de Archivos de Journals.");
		return true;
	}

}
