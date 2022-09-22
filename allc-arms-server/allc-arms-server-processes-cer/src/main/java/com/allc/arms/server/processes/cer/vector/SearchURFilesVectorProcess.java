package com.allc.arms.server.processes.cer.vector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.core.process.AbstractProcess;
import com.allc.properties.PropFile;

public class SearchURFilesVectorProcess extends AbstractProcess {

	private static Logger logger = Logger.getLogger(SearchURFilesVectorProcess.class);
	private boolean isEnd = false;
	protected boolean finished = false;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	private File inFolder;
	private File bkpFolder;
	private int sleepTime;
	private Iterator filesToProcess = null;
	private BufferedReader reader;
	protected Session sessionVector;
	private Transaction tx;
	String store = null;

	protected void inicializar() {
		try {
			//iniciarVectorSesion();
			inFolder = new File(properties.getObject("searchUrFilesVector.in.folder.path"));
			inFolder.mkdirs();
			bkpFolder = new File(properties.getObject("searchUrFilesVector.bkp.folder.path"));
			bkpFolder.mkdirs();
			sleepTime = properties.getInt("searchUrFilesVector.sleeptime");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void run() {

		logger.info("Iniciando SearchURFilesVectorProcess...");
		inicializar();
		String promocion = "";
		String codItem = "";
		String codItemFinal = "";
		long idPromo;
		long idItem;
		long idItemAll;
		List<Long> promosBBDR = null;
		store = properties.getObject("eyes.store.code");

		while (!isEnd) {
			String filename = null;
			try {
				File vectorFile = getNextURVectorFile();

				if (vectorFile != null) {
					iniciarVectorSesion();
					UtilityFile
							.createWriteDataFile(getEyesFileName(),
									"UPD_ITM_VEC_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + store + "|STR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(
													new Date())
											+ "|Iniciando procesamiento de: " + filename + ".\n",
									true);

					filename = vectorFile.getName().toUpperCase();
					logger.info("Archivo a procesar: " + filename);

					reader = new BufferedReader(new FileReader(vectorFile));

					String line;
					while ((line = reader.readLine()) != null) {

						promocion = line.substring(0, 10);
						codItem = line.substring(10, 28);
						long codItemAux = Long.valueOf(codItem);
						codItemFinal = String.valueOf(codItemAux);

						if (promocion.equalsIgnoreCase("0000000000")) {
							logger.info("Se quita el item: " + codItemFinal + " de todas las promociones BBDR.");
							promosBBDR = getPromocionesBBDR();
							if (promosBBDR != null && !promosBBDR.isEmpty()) {
								Iterator fkCampaign = promosBBDR.iterator();
								while (fkCampaign.hasNext()) {

									Long fkPromo = ((BigInteger) fkCampaign.next()).longValue();
									// logger.info("Ingresa por fkCampaign = " +
									// fkPromo);
									idItemAll = getIDItem(fkPromo, codItemFinal);
									if (idItemAll > 0L) {
										// logger.info("Borra atributo de la
										// promo " + fkPromo + " idItem: " +
										// idItemAll);
										updateItemAtributeInVectorInPromo(fkPromo, idItemAll);
									}
									updateItemInVectorInPromo(fkPromo, codItemFinal);
								}
							}
							// updateItemInVectorAllPromo(codItemFinal);
						} else {
							logger.info("Se quita el item: " + codItemFinal + " de la promocion: " + promocion);
							idPromo = getIdPromo("BBDR" + promocion);
							if (idPromo > 0L) {
								idItem = getIDItem(idPromo, codItemFinal);
								if (idItem > 0L)
									updateItemAtributeInVectorInPromo(idPromo, idItem);

								updateItemInVectorInPromo(idPromo, codItemFinal);
							}
						}
					}

					reader.close();
					File out = new File(bkpFolder, vectorFile.getName());
					vectorFile.renameTo(out);
					logger.info("Archivo: " + filename + " procesado correctamente.");
					UtilityFile
							.createWriteDataFile(getEyesFileName(),
									"UPD_ITM_VEC_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + store + "|END|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
													.format(new Date())
											+ "|Archivo: " + filename + " procesado.\n",
									true);

				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				try {
					logger.error("Error al procesar el archivo.");
					UtilityFile
							.createWriteDataFile(getEyesFileName(),
									"UPD_ITM_VEC_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + store + "|ERR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(
													new Date())
											+ "|Error al procesar actualización de Promociones.\n",
									true);
				} catch (Exception e1) {
					logger.error(e1.getMessage(), e1);
				}
			} finally {
				if (sessionVector != null) {
					sessionVector.close();
					sessionVector = null;
				}
			}
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}

		finished = true;
	}

	private File getNextURVectorFile() {
		logger.info("Buscando archivos de actualizacion de Items en promociones.");

		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd)
					return null;
				File[] files = inFolder.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isFile() && pathname.getName().toUpperCase().startsWith("UR");
					}
				});
				if (files.length == 0) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
				} else

					this.filesToProcess = Arrays.asList(files).iterator();

			} while (((this.filesToProcess == null) || !filesToProcess.hasNext()));
		}
		return (File) this.filesToProcess.next();
	}

	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	public Long getIdPromo(String codPromo) {
		try {
			//logger.info("Select ID From CAMPAIGN Where CODE = '" + codPromo + "'");
			SQLQuery query = sessionVector.createSQLQuery("Select ID From CAMPAIGN Where CODE = '" + codPromo + "'");
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return ((BigInteger) rows.get(0)).longValue();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return 0L;
	}

	public Long getIDItem(long idPromo, String codItem) {
		try {
//			logger.info("Select ID From CAMPAIGN_PRODUCT_RESULT Where FK_CAMPAIGN = '" + idPromo
//					+ "' and REC_VALUE like '%" + codItem + "'");
			SQLQuery query = sessionVector.createSQLQuery("Select ID From CAMPAIGN_PRODUCT_RESULT Where FK_CAMPAIGN = '"
					+ idPromo + "' and REC_VALUE like '%" + codItem + "'");
			List rows = query.list();
			if (rows != null && !rows.isEmpty())
				return ((BigInteger) rows.get(0)).longValue();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return 0L;
	}

	public boolean updateItemInVectorInPromo(long idPromo, String codItem) {
		tx = null;
		try {
			tx = sessionVector.beginTransaction();
			// logger.info("DELETE FROM CAMPAIGN_PRODUCT_RESULT WHERE
			// FK_CAMPAIGN = '"
			// + idPromo + "' and REC_VALUE like '%" + codItem + "'");
			Query query = sessionVector.createSQLQuery("DELETE FROM CAMPAIGN_PRODUCT_RESULT WHERE FK_CAMPAIGN = '"
					+ idPromo + "' and REC_VALUE like '%" + codItem + "'");
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}

	public boolean updateItemAtributeInVectorInPromo(long idPromo, long idItem) {
		tx = null;
		try {
			tx = sessionVector.beginTransaction();
			// logger.info("DELETE FROM CAMPAIGN_PRODUCT_RESULT WHERE
			// FK_CAMPAIGN = '"
			// + idPromo + "' and FK_CAMPAIGN_PRODUCT_RESULT ='" + idItem +
			// "'");
			Query query = sessionVector.createSQLQuery("DELETE FROM CAMPAIGN_PRODUCT_RESULT WHERE FK_CAMPAIGN = '"
					+ idPromo + "' and FK_CAMPAIGN_PRODUCT_RESULT ='" + idItem + "'");
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}

	public boolean updateItemInVectorAllPromo(String codItem) {
		tx = null;
		try {
			tx = sessionVector.beginTransaction();
			Query query = sessionVector.createSQLQuery(
					"DELETE FROM CAMPAIGN_PRODUCT_RESULT WHERE FK_CAMPAIGN IN (SELECT ID FROM CAMPAIGN WHERE CODE like 'BBDR%') and REC_VALUE like '%"
							+ codItem + "'  ");
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}

	private List<Long> getPromocionesBBDR() {
		try {
			SQLQuery query = sessionVector.createSQLQuery("SELECT ID FROM CAMPAIGN WHERE CODE like 'BBDR%'");

			List<Long> list = query.list();
			if (list != null && !list.isEmpty())
				return list;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private void iniciarVectorSesion() {
		while (sessionVector == null && !isEnd) {
			try {
				sessionVector = HibernateSessionFactoryContainer.getSessionFactory("Vector").openSession();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (sessionVector == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}
	}

	@Override
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		logger.info("Deteniendo SearchURFilesVectorProcess...");
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
		logger.info("Finalizó el Proceso de busqueda de archivo UR de Interfaz con Vector.");
		return true;
	}

}
