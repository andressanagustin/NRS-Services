/**
 * 
 */
package com.allc.arms.server.processes.cer.moto;

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
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.core.process.AbstractProcess;
import com.allc.entities.Item;
import com.allc.properties.PropFile;

/**
 * @author gustavo
 *
 */
public class UpdateMotoProcess extends AbstractProcess {

	protected static Logger log = Logger.getLogger(UpdateMotoProcess.class);
	protected Session sesion;
	private File inFolder;
	private File outFolder;
	private int sleepTime;
	protected Pattern p = Pattern.compile("\\|");
	protected MotoDAO motoDAO = new MotoDAO();
	protected ItemDAO itemDAO = new ItemDAO();
	private Iterator filesToProcess = null;
	public boolean isEnd = false;
	private Session session = null;
	protected boolean finished = false;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	protected String store = null;
	
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
		log.info("Iniciando UpdateMotoProcess...");
		inicializar();
		store = properties.getObject("eyes.store.code");
		while (!isEnd) {
			String filename = null;
			try {
				File motoFile = getNextMotoFile();

				if (motoFile != null) {
					UtilityFile.createWriteDataFile(getEyesFileName(), "UPD_MOTO_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Iniciando procesamiento de: "+filename+".\n", true);

					filename = motoFile.getName().toUpperCase();
					log.info("Archivo a procesar: " + filename);
					boolean procesado = false;
					if (filename.startsWith("MOLOTE")) {
						openSession();
						procesado = updateMotoFile(motoFile);
					}
					if (procesado && !isEnd) {
						File out = new File(outFolder, motoFile.getName());
						motoFile.renameTo(out);
						log.info("Archivo procesado correctamente.");
						UtilityFile.createWriteDataFile(getEyesFileName(), "UPD_MOTO_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo: "+filename+" procesado.\n", true);
					} else {
						log.error("Error al procesar el archivo.");
						UtilityFile.createWriteDataFile(getEyesFileName(), "UPD_MOTO_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el Archivo: "+filename+".\n", true);
					}
				} 
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					UtilityFile.createWriteDataFile(getEyesFileName(), "UPD_MOTO_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar actualización de Motos.\n", true);
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
	
	private boolean updateMotoFile(File inFile) {
		int i = 0;
		while (i < 3) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(inFile));
				String line = null;
				while ((line = reader.readLine()) != null && !isEnd) {
					if (motoDAO.motoVendida(sesion, line.substring(41, 61))
							|| motoDAO.existeMotoByNumSerie(sesion, line.substring(41, 61))) {
						log.error("Moto ya existente. Serial: " + line.substring(41, 61));
						UtilityFile
						.createWriteDataFile(getEyesFileName(),
								"UPD_MOTO_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
										+ "|" + store + "|ERR|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
												.format(new Date())
										+ "|Moto ya existente. Serial: " + line.substring(41, 61) +".\n",
								true);
					} else {
						Moto moto = new Moto();
						moto.setMandante(line.substring(0, 3).trim());
						Integer codSAP = new Integer(line.substring(3, 21).trim());
						moto.setMaterial(codSAP.toString());
						moto.setMotor(line.substring(21, 41).trim());
						moto.setSerie(line.substring(41, 61).trim());
						moto.setNumSRI(line.substring(61, 81).trim());
						moto.setChasis(line.substring(81, 101).trim());
						moto.setAnoFabricacion(line.substring(101, 105).trim());
						moto.setClase(line.substring(105, 120).trim());
						moto.setColor(line.substring(120, 135).trim());
						moto.setCilindraje(line.substring(135, 145).trim());
						moto.setCapAsiento(line.substring(145, 148).trim());
						moto.setNumCPN(line.substring(148, 168).trim());
						moto.setTieneNumCPN(line.substring(168, 169).trim());
						moto.setFechaCPN(line.substring(169, 177).trim());
						moto.setSubcategoria(line.substring(177, 187).trim());
						moto.setTipoCombustible(line.substring(187, 202).trim());
						moto.setTipoCarroceria(line.substring(202, 217).trim());
						moto.setNumCKD(line.substring(217, 237).trim());
						moto.setMarca(line.substring(237, 257).trim());
						moto.setModelo(line.substring(257, 275).trim());
						moto.setPaisOrigen(line.substring(275, 295).trim());
						moto.setTonelaje(line.substring(295, 315).trim());
						moto.setCentro(line.substring(315, 319).trim());
						moto.setStatus(line.substring(319, 320).trim());
						moto.setEjes(line.substring(320, 321).trim());
						moto.setRuedas(line.substring(321, 322).trim());
						sesion.clear();
						Item item = itemDAO.getItem(sesion, codSAP.toString());
						if(item != null)
							moto.setItemID(item.getItemID());
						motoDAO.insertMoto(sesion, moto);
					}
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

	
	private File getNextMotoFile() {
		log.info("Buscando archivos de actualizacion de motos");
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd)
					return null;
				File[] files = inFolder.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						log.debug("Pathname: " + pathname.getName().toUpperCase());
						return pathname.isFile() && pathname.getName().toUpperCase().startsWith("MOLOTE");
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
		log.info("Deteniendo UpdateMotoProcess...");
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
		log.info("Finaliza el Proceso de Busqueda de Archivos de Actualizacion de Motos.");
		return true;
	}

}
