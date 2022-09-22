/**
 * 
 */
package com.allc.arms.server.processes.cer.itemUpdate.store;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.fleje.ArchivoException;
import com.allc.arms.server.persistence.fleje.ArchivoSAP;
import com.allc.arms.server.persistence.fleje.Fleje;
import com.allc.arms.server.persistence.fleje.FlejesDAO;
import com.allc.arms.server.processes.cer.itemUpdate.EmailSender;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.core.process.AbstractProcess;
import com.allc.entities.POSDepartment;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;

/**
 * @author gustavo
 *
 */
public class SearchLoteFileProcess extends AbstractProcess {
	private static Logger log = Logger.getLogger(SearchLoteFileProcess.class);
	private File inFolder;
	private int sleepTime;
	protected Map stringToInt;
	private Iterator filesToProcess = null;
	public boolean isEnd = false;
	private Session session = null;
	private Session sessionArts = null;
	private EmailSender emailSender = new EmailSender();
	protected boolean finished = false;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	public static final String ACTION_ADD = "A";
	public static final String ACTION_DELETE = "B";
	public static final String ACTION_UPDATE = "M";
	private int numEeans = 0;
	private int numErres = 0;
	private int numItems = 0;
	private int numErris = 0;
	private SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");

	protected void inicializar() {
		isEnd = false;
		try {
			inFolder = new File(properties.getObject("searchItem.in.folder.path"));
			sleepTime = properties.getInt("searchItem.sleeptime");
			loadHashCharToInt();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void run() {
		log.info("Iniciando SearchLoteFileProcess...");
		inicializar();
		FlejesDAO flejesDAO = new FlejesDAO();
		String filename = null;
		String storeCode = properties.getObject("eyes.store.code");
		while (!isEnd) {
			try {
				File itemFile = getNextLoteFile();

				if (itemFile != null) {
					iniciarSesion();
					iniciarArtsSesion();
					filename = itemFile.getName().toUpperCase();
					String[] parts = filename.split("\\.");
					Integer store = new Integer(parts[1]);
					String sequence = parts[0].substring(2, 6);
					UtilityFile.createWriteDataFile(getEyesFileName(), "SRCH_ITEM_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo a Procesar: "+filename+".\n", true);
					log.info("Archivo a procesar: " + filename + " STORE: " + store);
					if (!flejesDAO.existeFleje(session, filename, store)) {
						File eanFile = new File(inFolder, "PE" + sequence + "." + parts[1]);
						if (eanFile.exists()) {
							

							ArchivoSAP archivo = new ArchivoSAP();
							archivo.setCodTienda(store);
							archivo.setNumLote(Long.valueOf(sequence));
							archivo.setNombreItem(filename);
							archivo.setNumItems(numItems);
							archivo.setNombreEan("EAN" + sequence + "." + parts[1]);
							archivo.setNumEans(numEeans);
							archivo.setNombreErri("ERRI" + sequence + "." + parts[1]);
							archivo.setNumErris(numErris);
							archivo.setNombreErre("ERRE" + sequence + "." + parts[1]);
							archivo.setNumErres(numErres);
							// seteo este estado para que la suite no lo encuentre
							archivo.setStatus(-1);
							boolean hayError = false;
//							Iterator itFiles = loteFilesMap.values().iterator();
//							while (itFiles.hasNext()) {
//								Fleje fleje = (Fleje) itFiles.next();
//								fleje.setArchivo(archivo);
//								if (flejesDAO.insertaFleje(session, fleje)
//										&& flejesDAO.insertaMov(session, fleje.getFlejesId(), fleje.getStatus(),
//												hourFormat.format(new Date()))) {
//									emailSender.send(filename);
//									log.info("Fleje registrado.");
//								} else {
//									hayError = true;
//									log.error("Fleje no registrado: " + fleje.getLote());
//								}
//							}
							if (!hayError) {
								// siempre queda en 0 para que la suite copie los archivos al temp y permita procesar
								archivo.setStatus(0);
								flejesDAO.insertaArchivo(session, archivo);
								UtilityFile.createWriteDataFile(getEyesFileName(), "SRCH_ITEM_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|El archivo: "+filename+" se procesó correctamente.\n", true);
							} else {
								UtilityFile.createWriteDataFile(getEyesFileName(), "SRCH_ITEM_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|El archivo: "+filename+" se procesó pero con observaciones.\n", true);
							}
						} else {
							UtilityFile.createWriteDataFile(getEyesFileName(), "SRCH_ITEM_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|El archivo EAN : "+eanFile.getName().toUpperCase()+" no existe.\n", true);
							log.info("Archivo EAN no encontrado.");
						}
					} else {
						UtilityFile.createWriteDataFile(getEyesFileName(), "SRCH_ITEM_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|El archivo: "+filename+" ya estaba registrado.\n", true);
						log.info("Archivo ya estaba registrado.");
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					UtilityFile.createWriteDataFile(getEyesFileName(), "SRCH_ITEM_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+storeCode+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el archivo: "+filename+".\n", true);
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
			}
			finally {
				if (session != null) {
					session.close();
					session = null;
				}
				if (sessionArts != null) {
					sessionArts.close();
					sessionArts = null;
				}
			}
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
		finished = true;
	}
	
	private String getEyesFileName(){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	private void loadHashCharToInt() {
		stringToInt = new HashMap();
		stringToInt.put("A", "10");
		stringToInt.put("B", "11");
		stringToInt.put("C", "12");
		stringToInt.put("D", "13");
		stringToInt.put("E", "14");
		stringToInt.put("F", "15");
		stringToInt.put("G", "16");
		stringToInt.put("H", "17");
		stringToInt.put("I", "18");
		stringToInt.put("J", "19");
		stringToInt.put("K", "20");
		stringToInt.put("L", "21");
		stringToInt.put("M", "22");
		stringToInt.put("N", "23");
		stringToInt.put("O", "24");
		stringToInt.put("P", "25");
		stringToInt.put("Q", "26");
		stringToInt.put("R", "27");
		stringToInt.put("S", "28");
		stringToInt.put("T", "29");
		stringToInt.put("U", "30");
		stringToInt.put("V", "31");
		stringToInt.put("W", "32");
		stringToInt.put("X", "33");
		stringToInt.put("Y", "34");
		stringToInt.put("Z", "35");
	}

	private Integer translateToInt(String code) {
		String newCode = "";
		for (int i = code.length(); i > 0; i--) {
			if (stringToInt.containsKey(code.substring(i - 1, i)))
				newCode = ((String) stringToInt.get(code.substring(i - 1, i))) + newCode;
			else
				newCode = code.substring(i - 1, i) + newCode;
		}
		return new Integer(newCode);
	}

	private Map cargarHash(File eanFile, String sequence, String storeCode) {
		Map hash = new HashMap();
		BufferedReader reader = null;
		File errorEanFile = null;
		BufferedWriter bw = null;
		try {
			reader = new BufferedReader(new FileReader(eanFile));
			String line = null;
			while ((line = reader.readLine()) != null) {
				numEeans++;
				if (line.length() > 30) {
					// codigo SAP
					String code = line.substring(0, 18);
					// barcode
					hash.put(code, line.substring(18, 30));
				} else {
					if (errorEanFile == null) {
						errorEanFile = new File(inFolder, "ERRE" + sequence + "." + storeCode);
						bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(errorEanFile)));
					}
					bw.write(line);
					bw.newLine();
					numErres++;
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		finally {
			try {
				if (reader != null)
					reader.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			try {
				if (bw != null)
					bw.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return hash;
	}

	private void iniciarSesion() {
		while (session == null && !isEnd) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory("Flejes").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (session == null)
				try {
					log.error("OCURRIÓ UN ERROR AL CREAR LA CONEXIÓN A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	private void iniciarArtsSesion() {
		while (sessionArts == null && !isEnd) {
			try {
				sessionArts = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionArts == null)
				try {
					log.error("OCURRIÓ UN ERROR AL CREAR LA CONEXIÓN A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	private File getNextLoteFile() {
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd)
					return null;
				File[] files = inFolder.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isFile() && (pathname.getName().toUpperCase().startsWith("LO"));
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
							sequence1 = Integer.parseInt(name1.substring(2, 6));
							sequence2 = Integer.parseInt(name2.substring(2, 6));
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

	private POSDepartment getPOSDepartmentByCodeCer(String posDepartmentCode) {
		
		Query query = sessionArts.createQuery("from com.allc.entities.POSDepartment where codDptoCer = '" + posDepartmentCode + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (POSDepartment) list.get(0);
		POSDepartment posDepartment = new POSDepartment();
		posDepartment.setCodDptoCer(posDepartmentCode);
		posDepartment.setName("Departamento " + posDepartmentCode);
		return posDepartment;
	}

	private boolean existeItemStore(Long itemCode, Integer storeCode) {
		Query query;
		try {
			query = sessionArts
					.createSQLQuery("SELECT COUNT(AS_ITM_STR.ID_ITM) FROM AS_ITM, PA_STR_RTL, AS_ITM_STR WHERE AS_ITM_STR.ID_BSN_UN = PA_STR_RTL.ID_BSN_UN AND AS_ITM_STR.ID_ITM = AS_ITM.ID_ITM AND AS_ITM.CD_ITM = "
							+ itemCode + " AND PA_STR_RTL.CD_STR_RT = " + storeCode);
			List rows = query.list();
			if (rows != null && !rows.isEmpty() && Integer.valueOf(rows.get(0).toString()).intValue() > 0) {
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo SearchItemFileProcess...");
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
		log.info("Finaliza el Proceso de Busqueda de Archivos de Items.");
		return true;
	}

}
