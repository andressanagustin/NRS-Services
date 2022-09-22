/**
 * 
 */
package com.allc.arms.server.processes.cer.itemUpdate.central;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsServerConstants;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.FilesHelper;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;

/**
 * @author gustavo
 *
 */
public class SearchItemFileProcess extends AbstractProcess {
	private static Logger log = Logger.getLogger(SearchItemFileProcess.class);
	private File inFolder;
	private File outFolder;
	private File bkpFolder;
	private int sleepTime;
	private Iterator filesToProcess = null;
	public boolean isEnd = false;
	protected boolean finished = false;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);

	protected void inicializar() {
		isEnd = false;
		try {
			inFolder = new File(properties.getObject("searchItem.in.folder.path"));
			bkpFolder = new File(properties.getObject("searchItem.bkp.folder.path"));
			sleepTime = properties.getInt("searchItem.sleeptime");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void run() {
		log.info("Iniciando SearchItemFileProcess...");
		inicializar();
		String filename = null;
		String storeCode = properties.getObject("eyes.store.code");
		while (!isEnd) {
			try {
				File itemFile = getNextItemFile();

				if (itemFile != null) {
					filename = itemFile.getName().toUpperCase();
					String[] parts = filename.split("\\.");
					Integer store = new Integer(parts[1]);
					String sequence = parts[0].substring(4, 8);
					UtilityFile.createWriteDataFile(getEyesFileName(),
							"SRCH_ITEM_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode + "|STR|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Archivo a Procesar: "
									+ filename + ".\n", true);
					log.info("Archivo a procesar: " + filename + " STORE: " + store);
					outFolder = new File(properties.getObject("searchItem.out.folder.path") + File.separator + store + File.separator
							+ properties.getObject("searchItem.out.folder.name"));

					FilesHelper.copyFile(inFolder.getPath(), outFolder.getPath(), filename, filename);
					FilesHelper.copyFile(inFolder.getPath(), bkpFolder.getPath(), filename, filename);
					itemFile.delete();
					File eanFile = new File(inFolder, "EAN" + sequence + "." + parts[1]);
					if (eanFile.exists()) {
						String eanName = eanFile.getName().toUpperCase();
						FilesHelper.copyFile(inFolder.getPath(), outFolder.getPath(), eanName, eanName);
						FilesHelper.copyFile(inFolder.getPath(), bkpFolder.getPath(), eanName, eanName);
						eanFile.delete();
						UtilityFile.createWriteDataFile(getEyesFileName(),
								"SRCH_ITEM_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode + "|END|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|El archivo: "
										+ filename + " se procesó correctamente.\n", true);
					} else {
						UtilityFile.createWriteDataFile(getEyesFileName(),
								"SRCH_ITEM_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode + "|WAR|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|El archivo EAN : " + eanFile.getName().toUpperCase() + " no existe.\n", true);
						log.info("Archivo EAN no encontrado.");
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					UtilityFile.createWriteDataFile(getEyesFileName(),
							"SRCH_ITEM_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode + "|ERR|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|Error al procesar el archivo: " + filename + ".\n", true);
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
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

	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_" + ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	private File getNextItemFile() {
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd)
					return null;
				File[] files = inFolder.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isFile() && (pathname.getName().toUpperCase().startsWith("ITEM"));
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
		log.info("Finalizó el Proceso de Búsqueda de Archivos de Ítems.");
		return true;
	}

}
