/**
 * 
 */
package com.allc.arms.server.processes.cer.cedpadruc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
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

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.cedpadruc.CedPadRucDAO;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.entities.CedRegElec;
import com.allc.entities.CedRuc;
import com.allc.entities.Padron;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * Proceso encargado de actualizar en BD los datos que vienen en los archivos CED, PAD o RUC.
 * 
 * @author gustavo
 *
 */
public class UpdateCedPadRucProcess extends AbstractProcess {
	private Session sesion;
	private Session sesionSaadmin;
	private Iterator filesToProcess = null;
	private File inFolder;
	private File outFolder;
	private int sleepTime;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	private static Pattern p = Pattern.compile("\\|");
	private static Pattern pFrame = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	private static Logger log = Logger.getLogger(UpdateCedPadRucProcess.class);
	private boolean isEnd = false;
	protected boolean finished = false;
	private CedPadRucDAO cedPadRucDAO = new CedPadRucDAO();
	protected StoreDAO storeDAO = new StoreDAO();
	private BufferedReader reader;
	protected ConnSocketClient socketClient;

	protected void inicializar() {
		try {
			inFolder = new File(properties.getObject("updateCedPadRuc.in.folder.path"));
			inFolder.mkdir();
			outFolder = new File(properties.getObject("updateCedPadRuc.out.folder.path"));
			outFolder.mkdir();
			sleepTime = properties.getInt("updateCedPadRuc.sleeptime");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void run() {
		log.info("Iniciando UpdateCedPadRucProcess...");
		inicializar();
		File fileToProcess = null;
		String store = properties.getObject("eyes.store.code");
		while (!isEnd) {
			try {
				fileToProcess = getNextCedRucPadFile();
				if (fileToProcess != null) {
					log.info("Archivo a procesar: " + fileToProcess.getName().toUpperCase());
					UtilityFile.createWriteDataFile(getEyesFileName(), "UPD_CPR_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo a Procesar: "+fileToProcess.getName()+".\n", true);
					reader = new BufferedReader(new FileReader(fileToProcess));
					iniciaOperacion();
					boolean procesado = false;
					if (fileToProcess.getName().toUpperCase().startsWith("CED")) {
						procesado = updateCedulaFile(fileToProcess);
					} else if (fileToProcess.getName().toUpperCase().startsWith("PAD")) {
						procesado = updatePadronFile(fileToProcess);
					} else if (fileToProcess.getName().toUpperCase().startsWith("RUC")) {
						procesado = updateRucFile(fileToProcess);
					}
					if (procesado) {
						File outDel = new File(outFolder, fileToProcess.getName());
						if(outDel.exists())
							outDel.delete();

						File out = new File(outFolder, fileToProcess.getName());
						log.info("Archivo de salida: " + out.getAbsolutePath());
						//cerramos el reader para que permita mover el archivo
						reader.close();
						if (Integer.valueOf(store) == 0) {
							boolean enviado = enviarArchivoATiendas(fileToProcess);
							if (enviado)
								log.info("Archivo enviado correctamente a las tiendas con servidor local.");
							else
								log.info("Error al enviar el archivo a las tiendas con servidor local.");
						}
						if(fileToProcess.renameTo(out))
							log.info("Archivo procesado correctamente.");
						else
							log.info("El archivo no se pudo mover.");
						UtilityFile.createWriteDataFile(getEyesFileName(), "UPD_CPR_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo: "+fileToProcess.getName()+" procesado correctamente.\n", true);
					} else {
						log.error("Error al procesar el archivo.");
						UtilityFile.createWriteDataFile(getEyesFileName(), "UPD_CPR_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el archivo: "+fileToProcess.getName()+".\n", true);
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							log.error(e.getMessage(), e);
						}
					}
				}
			} catch (Exception e) {
				UtilityFile.createWriteDataFile(getEyesFileName(), "UPD_CPR_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el archivo: "+fileToProcess.getName()+".\n", true);
				log.error(e.getMessage(), e);
			}
			finally {
				try {
					reader.close();
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1); 
				}
				cierraSesion();
			}
		}
		finished = true;
	}
	

	public boolean enviarArchivoATiendas(File fileToProcess) {
		iniciarSesion("Saadmin");
		List activeStoreWihtLocalServer = getAllActiveStore(1);

		if (activeStoreWihtLocalServer != null && !activeStoreWihtLocalServer.equals("")) {
			Iterator itStore = activeStoreWihtLocalServer.iterator();
			while (itStore.hasNext()) {
				Store tienda = (Store) itStore.next();

				StringBuffer data = new StringBuffer();
				data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
						.append(ArmsServerConstants.Process.FILE_SENDER_OPERATION)
						.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
						.append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
						.append(ArmsServerConstants.Communication.FRAME_SEP)
						.append(ArmsServerConstants.Communication.TEMP_CONN)
						.append(ArmsServerConstants.Communication.FRAME_SEP)
						.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
						.append(ArmsServerConstants.Communication.FRAME_SEP)
						.append(fileToProcess.getAbsolutePath())
						.append(ArmsServerConstants.Communication.FRAME_SEP)
						.append(inFolder.getAbsolutePath())
						.append(ArmsServerConstants.Communication.FRAME_SEP)
						.append(tienda.getIp())
						.append(ArmsServerConstants.Communication.FRAME_SEP)
						.append(properties.getObject("serverSocket.port"));

				List list = Arrays.asList(pFrame.split(data.toString()));
				Frame frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
						ArmsServerConstants.Communication.FRAME_SEP);
				if (frame.loadData()) {
					boolean send = sendFrame(frame, properties, 0);
					closeClient();
					if (send) {
						log.info("Archivo " + fileToProcess.getName() + " enviado correctamente a la tienda: "+tienda.getKey().toString()+".");
					} else {
						log.error("Error al enviar el archivo " + fileToProcess.getName() + " a la tienda: "+tienda.getKey().toString()+".");
					}
				}
			}
			return true;
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
						list = Arrays.asList(pFrame.split(str));
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
	
	protected boolean connectClient(PropFile properties,  Integer tienda) {

		String storeIP = storeDAO.getStoreByCode(sesionSaadmin, tienda).getIp();
	
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

	protected void iniciarSesion(String name) {
		while (sesionSaadmin == null) {
			try {
				sesionSaadmin = HibernateSessionFactoryContainer.getSessionFactory(name).openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sesionSaadmin == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}
	
	public List getAllActiveStore(Integer flLocalServer) {

		try {
			Query query = sesionSaadmin
					.createQuery("from com.allc.arms.server.persistence.store.Store where status = 1 and localServer = "
							+ flLocalServer);
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return rows;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	
	private String getEyesFileName(){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
	
	private void iniciaOperacion() {
		while (sesion == null && !isEnd) {
			try {
				sesion = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
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

	private void cierraSesion() {
		if (sesion != null) {
			try {
				sesion.close();
				sesion = null;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private boolean updateCedulaFile(File inFile) {
		int i = 0;
		int c = 0;
		while (i < 3 && !isEnd) {
			try {
				String line = null;
				while ((line = reader.readLine()) != null && !isEnd) {
					c++;
					if(c > 0){
						List list = Arrays.asList(p.split(line));
						CedRuc cedula = new CedRuc();
						cedula.setRegistroElec(new CedRegElec());
						if(list.size() > 2)
							cedula.getRegistroElec().setProvincia(((String) list.get(2)).length() > 25 ? ((String) list.get(2)).substring(0, 25) : ((String) list.get(2)));
						if(list.size() > 4)
							cedula.getRegistroElec().setCanton(((String) list.get(4)).length() > 25 ? ((String) list.get(4)).substring(0, 25) : ((String) list.get(4)));
						if(list.size() > 3)
							cedula.getRegistroElec().setCircunscripcion(((String) list.get(3)).length() > 75 ? ((String) list.get(3)).substring(0, 75) : ((String) list.get(3)));
						if(list.size() > 5)
							cedula.getRegistroElec().setParroquia(((String) list.get(5)).length() > 30 ? ((String) list.get(5)).substring(0, 30) : ((String) list.get(5)));
						if(list.size() > 6)
							cedula.getRegistroElec().setZona(((String) list.get(6)).length() > 40 ? ((String) list.get(6)).substring(0, 40) : ((String) list.get(6)));
						if(list.size() > 8)
							cedula.getRegistroElec().setRecinto(((String) list.get(8)).length() > 80 ? ((String) list.get(8)).substring(0, 80) : ((String) list.get(8)));
						cedula.getRegistroElec().setMesa("");
						if(list.size() > 10)
							cedula.getRegistroElec().setJunta(((String) list.get(10)).length() > 5 ? ((String) list.get(10)).substring(0, 5) : ((String) list.get(10)));
						cedula.getRegistroElec().setFuncion("");
						// if(list.size() > 13)
						// cedula.getRegistroElec().setFuncion("");
						// if(list.size() > 17)
						if(list.size() > 7)
							cedula.getRegistroElec().setGenero(((String) list.get(7)).length() > 10 ? ((String) list.get(7)).substring(0, 10) : ((String) list.get(7)));
						if(list.size() > 1)
							cedula.setNombre(((String) list.get(1)).length() > 250 ? ((String) list.get(1)).substring(0, 250) : ((String) list.get(1)));
						cedula.setId(Long.valueOf((String) list.get(0)));
						cedula.setCodigo((String) list.get(0));
						cedula.setTipo("C");
						cedula.setRegInter(0);
						if(list.size() > 7)
							cedula.setGenero(((String) list.get(7)).length() > 10 ? ((String) list.get(7)).substring(0, 10) : ((String) list.get(7)));
						sesion.clear();
						cedPadRucDAO.insertCedRuc(sesion, cedula);
					}
				}
				return !isEnd && true;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			i++;
		}
		return false;
	}

	private boolean updateRucFile(File inFile) {
		int i = 0;
		while (i < 3 && !isEnd) {
			try {
				String line = null;
				while ((line = reader.readLine()) != null && !isEnd) {
					List list = Arrays.asList(p.split(line));
					CedRuc ruc = new CedRuc();
					ruc.setId(Long.valueOf((String) list.get(0)));
					ruc.setCodigo((String) list.get(0));
					ruc.setNombre((String) list.get(1));
					ruc.setTipo("R");
					ruc.setRegInter(0);
					sesion.clear();
					cedPadRucDAO.insertCedRuc(sesion, ruc);
				}
				return !isEnd && true;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			i++;
		}
		return false;
	}

	private boolean updatePadronFile(File inFile) {
		int i = 0;
		while (i < 3 && !isEnd) {
			try {
				String line = null;
				while ((line = reader.readLine()) != null && !isEnd) {
					List list = Arrays.asList(p.split(line));
					Padron padron = new Padron();
					padron.setId(Long.valueOf((String) list.get(0)));
					padron.setCodigo((String) list.get(0));
					padron.setFuncion((String) list.get(1));
					padron.setRecinto((String) list.get(2));
					padron.setDireccion((String) list.get(3));
					padron.setMesa((String) list.get(4));
					padron.setJunta((String) list.get(5));
					sesion.clear();
					cedPadRucDAO.insertPadron(sesion, padron);
				}
				return !isEnd && true;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			i++;
		}
		return false;
	}

	private File getNextCedRucPadFile() {
		log.info("Buscando archivos de actualizacion de Cédula/Ruc/Padrón.");
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd)
					return null;
				File[] files = inFolder.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						//log.info("Pathname: " + pathname.getName().toUpperCase());
						return pathname.isFile()
								&& (pathname.getName().toUpperCase().startsWith("CED")
										|| pathname.getName().toUpperCase().startsWith("RUC") || pathname.getName().toUpperCase()
										.startsWith("PAD"));
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
							int sequence1 = Integer.parseInt(name1.substring(3, 7));
							int sequence2 = Integer.parseInt(name2.substring(3, 7));
							if (name1.substring(0, 3).equalsIgnoreCase(name2.substring(0, 3))) {
								if (sequence1 == sequence2) {
									return 0;
								}
								if (sequence1 < sequence2) {
									return -1;
								}
							} else if (name1.startsWith("CED"))
								return -1;
							else if (name2.startsWith("CED"))
								return 1;
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

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo UpdateCedPadRucProcess...");
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
		log.info("Finalizó el Proceso de Actualización de Cédulas, Padrones y RUCs.");
		return true;
	}
}
