package com.allc.arms.server.processes.status;

import java.io.File;
import java.io.FileFilter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.params.ParamValue;
import com.allc.arms.server.params.ParamsDAO;
import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.status.StoreStatus;
import com.allc.arms.server.persistence.status.StoreStatusDAO;
import com.allc.arms.server.persistence.status.TerminalStatus;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.server.processes.utils.AbstractProcessPrincipal;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.files.helper.ControllerFiles;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class StoreStatusUpdateProcess extends AbstractProcessPrincipal{
	
	protected Logger log = Logger.getLogger(StoreStatusUpdateProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	public boolean isEnd = false;
	protected Session session = null;
	protected Session sessionSaadmin = null;
	protected boolean finished = false;
	private String storeCode;
	private Iterator filesToProcess = null;
	protected HashMap activeStoreIp = new HashMap();
	protected List pathActiveStore = new ArrayList();
	private int storeCodeHasta;
	private int sleepTime;
	private Transaction tx;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmm");
	private StoreStatusDAO storeStatusDAO = new StoreStatusDAO();
	protected String storeIP;
	protected StoreDAO storeDAO = new StoreDAO();
	protected Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	protected ConnSocketClient socketClient = null;
	
	protected void inicializar() {
		isEnd = false;
		try {
			storeCode = properties.getObject("eyes.store.code");
			while(storeCode.length() < 3)
				storeCode = "0"+storeCode;
			iniciarSesion("Saadmin");
			sleepTime = properties.getInt("storeStatusUpdate.sleeptime");
			ParamsDAO paramsDAO = new ParamsDAO();
			ParamValue paravalue = null;
			if (Integer.valueOf(storeCode) == 0) {
				List tiendasActivas = storeDAO.getAllActiveStore(sessionSaadmin);
				if (tiendasActivas != null && !tiendasActivas.isEmpty()) {

					Iterator itStores = tiendasActivas.iterator();

					while (itStores.hasNext()) {
						Store tienda = (Store) itStores.next();
						String codTienda = tienda.getKey().toString();
						while (codTienda.length() < 3)
							codTienda = "0" + codTienda;
						activeStoreIp.put(codTienda, tienda.getIp());
						paravalue = paramsDAO.getParamByClave(sessionSaadmin, Integer.valueOf(codTienda).toString(),
								ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "SYNC_OUT");
						String dirTemp = properties.getObject("SUITE_ROOT") + paravalue.getValor() + codTienda
								+ File.separator + properties.getObject("accountingTotals.in.folder.path");
						(new File(dirTemp)).mkdirs();
						pathActiveStore.add(dirTemp);

					}
				} else {
					log.info("No existen tiendas activas disponibles.");
				}
			} else {
				String storeIP = storeDAO.getStoreByCode(sessionSaadmin, Integer.valueOf(storeCode)).getIp();
				activeStoreIp.put(storeCode, storeIP);
				paravalue = paramsDAO.getParamByClave(sessionSaadmin, Integer.valueOf(storeCode).toString(),
						ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "SYNC_OUT");
				pathActiveStore.add(properties.getObject("SUITE_ROOT") + File.separator + paravalue.getValor()
						+ File.separator + storeCode + File.separator + properties.getObject("accountingTotals.in.folder.path"));
			}
			storeCodeHasta = properties.getObject("accountingTotals.in.folder.path").length() + 1;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void run() {
		log.info("Iniciando StoreStatusUpdateProcess...");
		inicializar();
		
		storeIP = storeDAO.getStoreByCode(sessionSaadmin, Integer.valueOf(storeCode)).getIp();
		
		while (!isEnd) {
                    if(isPrincipal()){
			try {
//				File storeStatusFile = new File(inFolder,"EAMTERMS.DAT");
				File storeStatusFile = getNextFile();
				if (storeStatusFile != null) {
					String path = storeStatusFile.getAbsolutePath();
					String pathFinal = path.substring(0, path.lastIndexOf(File.separator));
					String tiendaCode = pathFinal.substring(pathFinal.length() - (storeCodeHasta + 3),
							pathFinal.length() - storeCodeHasta);
					try{
						iniciarSesion();
						UtilityFile.createWriteDataFile(getEyesFileName(), "STR_STS_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo a procesar: EAMTERMS.DAT Tienda: "+tiendaCode+"\n", true);
						log.info("Archivo a procesar: "+storeStatusFile.getPath());
//						RandomAccessFile f = new RandomAccessFile(inFolder + File.separator + "EAMTERMS.DAT", "r");
						RandomAccessFile f = new RandomAccessFile(storeStatusFile, "r");
						byte[] b = new byte[(int)f.length()];
						f.readFully(b);
						StringBuffer sb = new StringBuffer(new String(b));
						f.close();
						procesarArchivo(sb, tiendaCode);
						enviarTrama("EAMTERMS.DAT", tiendaCode);
						UtilityFile.createWriteDataFile(getEyesFileName(), "STR_STS_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Procesamiento del Archivo EAMTERMS.DAT Tienda: "+tiendaCode+" finalizado.\n", true);
						log.info("Procesamiento del archivo EAMTERMS.DAT exitoso.");
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						try {
							UtilityFile.createWriteDataFile(getEyesFileName(), "STR_STS_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el archivo EAMTERMS.DAT Tienda: "+tiendaCode+".\n", true);
						} catch (Exception e1) {
							log.error(e1.getMessage(), e1);
						}
					}
					if(!filesToProcess.hasNext()){
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							log.error(e.getMessage(), e);
						}
					}
					
				} 
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					UtilityFile.createWriteDataFile(getEyesFileName(), "STR_STS_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el archivo.\n", true);
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
			} finally {
				try {
					session.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				session = null;
			}
                    }
		}
		finished = true;
	}
	
	private File getNextFile() {

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
							return pathname.isFile() && pathname.getName().toUpperCase().equals("EAMTERMS.DAT");
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
					} catch (InterruptedException e) {
						log.error(e.getMessage(), e);
					}
				}

			} while (((this.filesToProcess == null) || !filesToProcess.hasNext()));
		}
		return (File) this.filesToProcess.next();
	}
	
	private boolean enviarTrama(String filename, String tienda){
		try {
			String tiendaIP = (String) activeStoreIp.get(tienda);
			connectClient(tiendaIP);
			String today = ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date());
			StringBuffer message = new StringBuffer();
			message.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(ArmsServerConstants.Process.STORE_STATUS_UPDATE_PROCESS)
					.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
					.append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(storeCode)
					.append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(ArmsServerConstants.Communication.TEMP_CONN)
					.append(ArmsServerConstants.Communication.FRAME_SEP).append(today)
					.append(ArmsServerConstants.Communication.FRAME_SEP)
					.append(filename);
			List list = Arrays.asList(p.split(message.toString()));
			Frame frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
					ArmsServerConstants.Communication.FRAME_SEP);
			if(frame.loadData()){
				String trama = Util.addLengthStartOfString(frame.getString().toString(), properties.getInt("serverSocket.quantityBytesLength"))
						.toString();
				log.info("Trama a enviar: " + trama);
				if (socketClient.writeDataSocket(trama)) {
					Frame frameRta = leerRespuesta();
					frameRta.loadData();
					closeConnection();
					if (frameRta.getStatusTrama() == 0) {
						return true;
					}
				}
			}
		} catch (Exception e){
			log.error(e.getMessage(), e);
		}
		return false;
	}
	
	private void procesarArchivo(StringBuffer sb, String tienda) {
		
		try{
			
			for(int i = 512; i < sb.length(); i = i + 512){
				String bloque = sb.substring(i, i +512);
				for(int c = 4; c < 508; c = c + 72){
					String subbloque = bloque.substring(c, c+72);
					Integer terminal = Integer.valueOf(ControllerFiles.unpack(subbloque.substring(0, 2).getBytes()));
					
					if(terminal.compareTo(Integer.valueOf(0)) != 0){
//						log.info("TERMINAL: " + terminal);
						if(terminal.compareTo(Integer.valueOf(9999)) == 0){
							procesaStoreRecord(subbloque, tienda);
						} else {
							procesaTerminalRecord(subbloque, tienda);
						}
					}
				}
		    }
		}catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private String getEyesFileName(){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
	
	private void iniciarSesion() {
		while (session == null && !isEnd) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory("Eyes").openSession();
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
	
	protected void iniciarSesion(String name) {
		while (sessionSaadmin == null) {
			try {
				sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory(name).openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionSaadmin == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}
	
	protected boolean connectClient(String ip) {
		if (socketClient != null && !socketClient.getIpServer().equals(ip)) {
			if (socketClient.isConnected())
				socketClient.closeConnection();
			socketClient = null;
		}
		if (socketClient == null || !socketClient.isConnected()) {
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(ip);
			socketClient.setPortServer(properties.getInt("clientSocket.port"));
			socketClient.setRetries(properties.getInt("clientSocket.retries"));
			socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}

	protected void closeConnection() {
		if (socketClient != null)
			socketClient.closeConnection();
	}
	
	protected Frame leerRespuesta() {
		int numberOfBytes = socketClient.readLengthDataSocket();
		if (numberOfBytes > 0) {
			String str = socketClient.readDataSocket(numberOfBytes);
			if (StringUtils.isNotBlank(str)) {
				List list = Arrays.asList(p.split(str));
				Frame frameRpta = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
						ArmsServerConstants.Communication.FRAME_SEP);
				log.info("Respuesta recibida: " + frameRpta.toString());
				return frameRpta;
			}
		}
		if( numberOfBytes == 0)
			numberOfBytes = socketClient.readLengthDataSocket();
		
		log.info("No se recibio respuesta.");
		return null;
	}	

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo StoreStatusUpdateProcess...");
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
		log.info("Finalizó el Proceso de Actualización de StoreStatus.");
		return true;
	}
	
	private Integer convertToInt(byte[] bytes){
		final ByteBuffer bb = ByteBuffer.wrap(bytes);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    if(bytes.length == 2)
	    	return new Integer(bb.getShort());
	    else
	    	return new Integer(bb.getInt());
	}
	
	private void procesaStoreRecord(String bloque, String tienda) {
	 	try {
			StoreStatus storeStatus = storeStatusDAO.getStoreStatus(session, Integer.valueOf(storeCode));
			initTx();
			storeStatus.setStoreCode(Integer.valueOf(tienda));
			storeStatus.setTerminal(Integer.valueOf(ControllerFiles.unpack(bloque.substring(0, 2).getBytes())));
			storeStatus.setsLogName(bloque.substring(2, 10));
			storeStatus.setNumClose(convertToInt(bloque.substring(10, 12).getBytes()));
			int closeFlg = bloque.substring(12, 13).getBytes() [0];
			storeStatus.setCloseFlg(closeFlg);
			Date fecha = formatter.parse(ControllerFiles.unpack(bloque.substring(13, 18).getBytes()));
			storeStatus.setDateTime(fecha);
			storeStatus.setMonitor(Integer.valueOf(ControllerFiles.unpack(bloque.substring(18, 20).getBytes())));
			storeStatus.setCloseControlFlg(convertToInt(bloque.substring(20, 22).getBytes()));
			storeStatus.setIndicat0(convertToInt(bloque.substring(22, 24).getBytes()));
			storeStatus.setIndicat1(convertToInt(bloque.substring(24, 26).getBytes()));
			storeStatus.setReserved(bloque.substring(26, 72));
			
			session.saveOrUpdate(storeStatus);
			tx.commit();
		
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				tx.rollback();
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
		
	}
	
	private void procesaTerminalRecord(String bloque, String tienda) {
		try {
			String terminal = ControllerFiles.unpack(bloque.substring(0, 2).getBytes());
			Integer period = Integer.valueOf(terminal.substring(0, 1));
			Integer terminalNumber = Integer.valueOf(terminal.substring(1));
			TerminalStatus terminalStatus = storeStatusDAO.getTerminalStatusByTerminalAndStoreCode(session, terminalNumber, Integer.valueOf(tienda));
			initTx();
			terminalStatus.setStoreCode(Integer.valueOf(tienda));
			terminalStatus.setTerminalNumber(terminalNumber);
			terminalStatus.setPeriod(period);
			terminalStatus.setOperator(Integer.valueOf(ControllerFiles.unpack(bloque.substring(2, 7).getBytes())));
			terminalStatus.setTransNum(Integer.valueOf(ControllerFiles.unpack(bloque.substring(7, 9).getBytes())));
			terminalStatus.setNumLoans(convertToInt(bloque.substring(9, 11).getBytes()));
			terminalStatus.setAmtLoans(convertToInt(bloque.substring(11, 15).getBytes()));
			terminalStatus.setNumPkups(convertToInt(bloque.substring(15, 17).getBytes()));
			terminalStatus.setAmtPkups(convertToInt(bloque.substring(17, 21).getBytes()));
			terminalStatus.setGrossPos(convertToInt(bloque.substring(21, 25).getBytes()));
			terminalStatus.setGrossNeg(convertToInt(bloque.substring(25, 29).getBytes()));
			terminalStatus.setAmtMisc(convertToInt(bloque.substring(29, 33).getBytes()));
			terminalStatus.setNumTrans(convertToInt(bloque.substring(33, 35).getBytes()));
			terminalStatus.setTillAmtCash(convertToInt(bloque.substring(35, 39).getBytes()));
			terminalStatus.setTillAmtCheck(convertToInt(bloque.substring(39, 43).getBytes()));
			terminalStatus.setTillAmtFoods(convertToInt(bloque.substring(43, 47).getBytes()));
			terminalStatus.setTillAmtMisc1(convertToInt(bloque.substring(47, 51).getBytes()));
			terminalStatus.setTillAmtMisc2(convertToInt(bloque.substring(51, 55).getBytes()));
			terminalStatus.setTillAmtMisc3(convertToInt(bloque.substring(55, 59).getBytes()));
			terminalStatus.setTillAmtManuf(convertToInt(bloque.substring(59, 63).getBytes()));
			terminalStatus.setTillAmtStore(convertToInt(bloque.substring(63, 67).getBytes()));
			terminalStatus.setTranType(Integer.valueOf(ControllerFiles.unpack(bloque.substring(67, 68).getBytes())));
			terminalStatus.setStatus(convertToInt(bloque.substring(68, 70).getBytes()));
			terminalStatus.setStatus2(convertToInt(bloque.substring(70, 72).getBytes()));
			session.saveOrUpdate(terminalStatus);
			tx.commit();
		
		}catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				tx.rollback();
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
	}
	
	public void initTx() {
		while (tx == null || !tx.isActive()) {
			try {
				tx = session.beginTransaction();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				tx = null;
			}
			if (tx == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA TRANSACCION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}
	
	

}
