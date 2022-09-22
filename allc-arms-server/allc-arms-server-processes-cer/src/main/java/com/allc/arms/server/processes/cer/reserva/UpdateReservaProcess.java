/**
 * 
 */
package com.allc.arms.server.processes.cer.reserva;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.item.ItemDAO;
import com.allc.arms.server.persistence.moto.Moto;
import com.allc.arms.server.persistence.moto.MotoDAO;
import com.allc.arms.server.persistence.reserva.ReservaDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.arms.utils.tsl.TSLConstants;
import com.allc.core.process.AbstractProcess;
import com.allc.entities.Item;
import com.allc.entities.Reserva;
import com.allc.entities.RetailStore;
import com.allc.properties.PropFile;

/**
 * @author gustavo
 *
 */
public class UpdateReservaProcess extends AbstractProcess {

	protected static Logger log = Logger.getLogger(UpdateReservaProcess.class);
	protected Session sesion;
	private File inFolder;
	private File outFolder;
	private int sleepTime;
	protected Pattern p = Pattern.compile("\\|");
	private Iterator filesToProcess = null;
	public boolean isEnd = false;
	private Session session = null;
	protected boolean finished = false;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	private ReservaDAO reservaDAO = new ReservaDAO();
	protected ItemDAO itemDAO = new ItemDAO();
	String store = null;
	
	protected void inicializar() {
		isEnd = false;
		try {
			inFolder = new File(properties.getObject("updateMoto.in.folder.path"));
			outFolder = new File(properties.getObject("updateMoto.out.folder.path"));
			sleepTime = properties.getInt("updateMoto.sleeptime");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void run() {
		log.info("Iniciando UpdateReservaProcess...");
		inicializar();
		store = properties.getObject("eyes.store.code");
		while (!isEnd) {
			String filename = null;
			try {
				File rsvFile = getNextReservaFile();

				if (rsvFile != null) {
					UtilityFile.createWriteDataFile(getEyesFileName(), "UPD_RSV_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Iniciando procesamiento de: "+filename+".\n", true);

					filename = rsvFile.getName().toUpperCase();
					log.info("Archivo a procesar: " + filename);
					boolean procesado = false;
					if (filename.startsWith("RSVLOTE")) {
						openSession();
						procesado = updateReservaFile(rsvFile);
					}
					if (procesado && !isEnd) {
						File out = new File(outFolder, rsvFile.getName());
						rsvFile.renameTo(out);
						log.info("Archivo procesado correctamente.");
						UtilityFile.createWriteDataFile(getEyesFileName(), "UPD_RSV_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo: "+filename+" procesado.\n", true);
					} else {
						log.error("Error al procesar el archivo.");
						UtilityFile.createWriteDataFile(getEyesFileName(), "UPD_RSV_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el Archivo: "+filename+".\n", true);
					}
				} 
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					UtilityFile.createWriteDataFile(getEyesFileName(), "UPD_RSV_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar actualización de Reservas.\n", true);
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
			}
			finally {
				if(session != null){
					session.close();
					session = null;
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
	

	public void openSession() {
		while (sesion == null) {
			try {
				sesion = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				sesion = null;
			}
			if (sesion == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA SESION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}
	
	private boolean updateReservaFile(File inFile) {
		int i = 0;
		while (i < 3) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(inFile));
				String line = null;
				while ((line = reader.readLine()) != null && !isEnd) {
					String[] rsvInfo = line.split("\\|");
					Reserva reserva = new Reserva();
					
					reserva.setCodReserva(Long.valueOf(rsvInfo[0]));
					reserva.setNumSerie(rsvInfo[1]);
												
					Item item = itemDAO.getItem(sesion, rsvInfo[2]);
					
					if(item != null)
						reserva.setItemID(item.getItemID());
					
					RetailStore retailStore = reservaDAO.getRetailStoreByCode(sesion, Integer.valueOf(rsvInfo[3]));
					
					reserva.setRetailStoreID(retailStore.getRetailStoreID());
					
					Date fechaInicio = TSLConstants.Formatters.TLOG_SUFIX_DATE_TIME_FORMATTER_ACE_2.parse(rsvInfo[4]);
					reserva.setFechaDesde(fechaInicio);
					Date fechaFin = TSLConstants.Formatters.TLOG_SUFIX_DATE_TIME_FORMATTER_ACE_2.parse(rsvInfo[5]);
					reserva.setFechaHasta(fechaFin);
					
					sesion.clear();
					if(item != null)
						reservaDAO.insertReserva(sesion, reserva);
					else
						UtilityFile.createWriteDataFile(getEyesFileName(), "UPD_RSV_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Reserva: "+reserva.getCodReserva()+" no guardada por item inexistente.\n", true);
				}
				reader.close();
				return true;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			i++;
		}
		return false;
	}

	
	private File getNextReservaFile() {
		log.info("Buscando archivos de actualizacion de reservas");
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd)
					return null;
				File[] files = inFolder.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						log.debug("Pathname: " + pathname.getName().toUpperCase());
						return pathname.isFile() && pathname.getName().toUpperCase().startsWith("RSVLOTE");
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
							sequence1 = Integer.parseInt(name1.substring(6, 9));
							sequence2 = Integer.parseInt(name2.substring(6, 9));

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
	
	private String getEyesFileName(){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo UpdateReservaProcess...");
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
		log.info("Finaliza el Proceso de Busqueda de Archivos de Actualizacion de Reservas.");
		return true;
	}

}
