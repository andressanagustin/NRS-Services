package com.allc.arms.server.processes.cer.secsubsecupdate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
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
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.params.ParamValue;
import com.allc.arms.server.params.ParamsDAO;
import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.entities.MerchandiseHierarchyGroup;
import com.allc.entities.POSDepartment;
import com.allc.files.helper.FilesHelper;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class UpdateSecSubSecProcess extends AbstractProcess {

	private Iterator filesToProcess = null;
	private File inFolder;
	private String outFolderPart1;
	private String outFolderPart2;
	private File bkpFolder;
	private int sleepTime;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	private static Logger logger = Logger.getLogger(UpdateSecSubSecProcess.class);
	private boolean isEnd = false;
	protected boolean finished = false;
	private BufferedReader reader;
	private Session session = null;
	private Session sessionArts = null;
	private String store = null;
	private Transaction tx;
	private String nameFileController = null;
	private String nameFileSecGui = null;
	private String linea1ArchivoCMD = null;
	private String linea2ArchivoCMD = null;
	private String porcDescEmpDeafutl = null;
	private String porcBonoSolDefault = null;
	protected ConnSocketClient socketClient;
	StoreDAO storeDAO = new StoreDAO();
	private static Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);

	protected void inicializar() {
		try {
			iniciarSesion("Saadmin");
			store = properties.getObject("eyes.store.code");
			ParamsDAO paramsDAO = new ParamsDAO();
			ParamValue paravalue = paramsDAO.getParamByClave(session, store,
					ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "DIR_SAP");
			inFolder = new File(properties.getObject("SUITE_ROOT") + paravalue.getValor()
					+ properties.getObject("updateSecSubSec.in.folder.path"));
			inFolder.mkdirs();
			bkpFolder = new File(properties.getObject("SUITE_ROOT") + paravalue.getValor()
					+ properties.getObject("updateSecSubSec.bkp.folder.path"));
			bkpFolder.mkdirs();
			ParamValue paravalue2 = paramsDAO.getParamByClave(session, store,
					ArmsServerConstants.AmbitoParams.SUITE_PARAMS, "NOM_ARCDPTOS");
			nameFileController = paravalue2.getValor();
			ParamValue paravalue3 = paramsDAO.getParamByClave(session, store,
					ArmsServerConstants.AmbitoParams.SUITE_PARAMS, "RUTA_DPTOS");
			linea1ArchivoCMD = paravalue3.getValor();
			ParamValue paravalue4 = paramsDAO.getParamByClave(session, store,
					ArmsServerConstants.AmbitoParams.SUITE_PARAMS, "CMD_DPT_REC");
			linea2ArchivoCMD = paravalue4.getValor();
			ParamValue paravalue5 = paramsDAO.getParamByClave(session, store,
					ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "SYNC_IN");
			outFolderPart1 = properties.getObject("SUITE_ROOT") + File.separator + paravalue5.getValor()
					+ File.separator;
			outFolderPart2 = File.separator + properties.getObject("fileUpdaterDown.folder.name");
			sleepTime = properties.getInt("updateSecSubSec.timesleep");
			ParamValue paravalue6 = paramsDAO.getParamByClave(session, store,
					ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "PORC_DSC_EMP_DEF");
			porcDescEmpDeafutl = paravalue6.getValor();
			ParamValue paravalue7 = paramsDAO.getParamByClave(session, store,
					ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "PORC_BON_SOL_DEF");
			porcBonoSolDefault = paravalue7.getValor();
			ParamValue paravalue8 = paramsDAO.getParamByClave(session, store,
					ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "FILE_SEC_GUI");
			nameFileSecGui = paravalue8.getValor();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void run() {
		logger.info("Iniciando UpdateSecSubSecProcess...");
		inicializar();
		File fileToProcess = null;
		boolean procesado = false;
		while (!isEnd) {
			try {
				fileToProcess = getNextSecSubSecFile();
				if (fileToProcess != null) {
					logger.info("Archivo a procesar: " + fileToProcess.getName().toUpperCase());
					UtilityFile.createWriteDataFile(getEyesFileName(),
							"UPD_SECSUBSEC_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
									+ store + "|STR|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|Archivo a Procesar: " + fileToProcess.getName() + ".\n",
							true);
					reader = new BufferedReader(new FileReader(fileToProcess));
					iniciaOperacion();

					if (fileToProcess.getName().toUpperCase().startsWith("SEC")) {
						procesado = updateSecFile(fileToProcess);
					} else if (fileToProcess.getName().toUpperCase().startsWith("SUBSEC")) {
						procesado = updateSubSecFile(fileToProcess);
					}
					if (procesado) {
						File outDel = new File(bkpFolder, fileToProcess.getName());
						if (outDel.exists())
							outDel.delete();

						File out = new File(bkpFolder, fileToProcess.getName());
						logger.info("Archivo de salida: " + out.getAbsolutePath());
						// cerramos el reader para que permita mover el archivo
						reader.close();
						if (Integer.valueOf(store) == 0) {
							boolean enviado = enviarArchivoATiendas(fileToProcess);
							if (enviado)
								logger.info("Archivo enviado correctamente a todas las tiendas con servidor local.");
							else
								logger.info("Error al enviar el archivo a todas las tiendas con servidor local.");
						}
						if (fileToProcess.renameTo(out)) {
							logger.info("Archivo procesado correctamente.");
							UtilityFile.createWriteDataFile(getEyesFileName(),
									"UPD_SECSUBSEC_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + store + "|END|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
													.format(new Date())
											+ "|Archivo: " + fileToProcess.getName() + " procesado correctamente.\n",
									true);

						} else
							logger.info("El archivo no se pudo mover.");

					} else {
						logger.error("Error al procesar el archivo.");
						UtilityFile.createWriteDataFile(getEyesFileName(),
								"UPD_SECSUBSEC_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
										+ "|" + store + "|ERR|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|Error al procesar el archivo: " + fileToProcess.getName() + ".\n",
								true);
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							logger.error(e.getMessage(), e);
						}
					}

				}

				if (procesado && !filesToProcess.hasNext()) {

					boolean creado = false;
					creado = crearArchivosParaControlador();
					if (creado) {
						logger.info("Archivo creado y copiado a folder de tiendas activas en forma correcta.");
						UtilityFile.createWriteDataFile(getEyesFileName(),
								"UPD_SECSUBSEC_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
										+ "|" + store + "|END|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|Archivos creados/copiados para ser enviados a los controladores.\n",
								true);

					} else {
						logger.error("Error al crear y copiar los archivos a enviar al controlador.");
						UtilityFile.createWriteDataFile(getEyesFileName(),
								"UPD_SECSUBSEC_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
										+ "|" + store + "|WAR|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|No se pudieron crear/copiar los archivos para enviar al controlador.\n",
								true);
					}
					procesado = false;

				}
			} catch (Exception e) {
				UtilityFile.createWriteDataFile(getEyesFileName(),
						"UPD_SECSUBSEC_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ store + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al procesar el archivo: " + fileToProcess.getName() + ".\n",
						true);
				logger.error(e.getMessage(), e);
			} finally {
				try {
					if (reader != null)
						reader.close();
				} catch (Exception e1) {
					logger.error(e1.getMessage(), e1);
				}

			}
		}
		cierraSesion();
		finished = true;
	}

	private void iniciaOperacion() {
		while (sessionArts == null && !isEnd) {
			try {
				sessionArts = HibernateSessionFactoryContainer.getSessionFactory("Arts").openSession();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				sessionArts = null;
			}
			if (sessionArts == null)
				try {
					logger.error("OCURRIÓ UN ERROR AL CREAR LA CONEXIÓN A LA BD, SE REINTENTARÁ EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}
	}

	protected void iniciarSesion(String name) {
		while (session == null) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory(name).openSession();
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

	private File getNextSecSubSecFile() {
		logger.info("Buscando archivos de actualizacion de Seccion y SubSeccion.");
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd)
					return null;
				File[] files = inFolder.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						// log.info("Pathname: " +
						// pathname.getName().toUpperCase());
						return pathname.isFile() && ((pathname.getName().toUpperCase().startsWith("SEC") && pathname.getName().length() == 18)
								|| (pathname.getName().toUpperCase().startsWith("SUBSEC") && pathname.getName().length() == 21));
					}
				});
				if (files.length == 0) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
				} else {
					Arrays.sort(files, new Comparator() {
						public int compare(Object obj1, Object obj2) {
							try {
								String name1 = ((File) obj1).getName().toUpperCase();
								long sequence1 = 0;
								String name2 = ((File) obj2).getName().toUpperCase();
								long sequence2 = 0;
								sequence1 = Long.valueOf(name1.substring(name1.length() - 14, name1.length()));
								sequence2 = Long.valueOf(name2.substring(name2.length() - 14, name2.length()));
								if (sequence1 == sequence2) {
									if (name1.startsWith("SEC"))
										return -1;
								}
								if (sequence1 < sequence2)
									return -1;

							} catch (Exception e) {
								logger.error(e.getMessage(), e);
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

	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	private void cierraSesion() {
		if (session != null && sessionArts != null) {
			try {
				session.close();
				sessionArts.close();
				session = null;
				sessionArts = null;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private boolean updateSecFile(File inFile) {
		logger.info("Iniciando procesamiento archivo SEC.");
		int i = 0;
		while (i < 3 && !isEnd) {
			try {
				String line = null;
				while ((line = reader.readLine()) != null && !isEnd) {
					if(line.trim().length() ==  72){
					initTx();
					try {
						POSDepartment posDeparment = getPOSDepartmentByCodeCer(line.substring(0, 4));
						posDeparment.setName(line.substring(4, 54));
						posDeparment.setCodNegocio(line.substring(54, 58));
						posDeparment.setPorcentajeRecargo(Integer.valueOf(line.substring(58, 62)));
						String porcentajeDescEmp = line.substring(64, 68);
						if (line.substring(62, 63).equals("1")) {
							if (!porcentajeDescEmp.equals("0000"))
								posDeparment.setPorcentajeDscEmp(Integer.valueOf(porcentajeDescEmp));
							else
								posDeparment.setPorcentajeDscEmp(Integer.valueOf(porcDescEmpDeafutl));
						} else
							posDeparment.setPorcentajeDscEmp(Integer.valueOf("0"));
						String porcentajeBonSol = line.substring(68, 72);
						if (line.substring(63, 64).equals("1")) {
							if (!porcentajeBonSol.equals("0000"))
								posDeparment.setPorcentajeBonSol(Integer.valueOf(porcentajeBonSol));
							else
								posDeparment.setPorcentajeBonSol(Integer.valueOf(porcBonoSolDefault));
						} else
							posDeparment.setPorcentajeBonSol(Integer.valueOf("0"));
						sessionArts.saveOrUpdate(posDeparment);
						tx.commit();
					} catch (Exception e) {
						tx.rollback();
						logger.error(e.getMessage(), e);
					}
				}
				}
				return !isEnd && true;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			i++;
		}
		return false;
	}

	private boolean updateSubSecFile(File inFile) {
		logger.info("Iniciando procesamiento archivo SUBSEC.");
		int i = 0;
		while (i < 3 && !isEnd) {
			try {
				String line = null;
				while ((line = reader.readLine()) != null && !isEnd) {
					if(line.trim().length() ==  61){
					POSDepartment posDeparment = getPOSDepartment(line.substring(0, 4));
					if (posDeparment != null) {
						initTx();
						try {
							MerchandiseHierarchyGroup mhg = getMerchandiseHierarchyGroupByCode(line.substring(4, 7),
									posDeparment);
							if(mhg.getCode() == null){
								int codeFliaNext = posDeparment.getQtyFlias() + 1;
								mhg.setCode(codeFliaNext);
								posDeparment.setQtyFlias(codeFliaNext);
							}
							mhg.setName(line.substring(7, 57));
							mhg.setDescription(line.substring(7, 57));
							mhg.setPosDepartment(posDeparment);
							mhg.setPorcentajeRecargo(Integer.valueOf(line.substring(57, 61)));
							sessionArts.saveOrUpdate(mhg);
							sessionArts.saveOrUpdate(posDeparment);
							tx.commit();
						} catch (Exception e) {
							tx.rollback();
							logger.error(e.getMessage(), e);
						}
					} else
						UtilityFile.createWriteDataFile(getEyesFileName(),
								"UPD_SECSUBSEC_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
										+ "|" + store + "|WAR|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|No existe el departamento asociado a la familia: " + line.substring(0, 4)
										+ ".\n",
								true);
					}
				}
				return !isEnd && true;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			i++;
		}
		return false;
	}

	private POSDepartment getPOSDepartmentByCodeCer(String posDepartmentCode) {

		Query query = sessionArts
				.createQuery("from com.allc.entities.POSDepartment where codDptoCer = '" + posDepartmentCode + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (POSDepartment) list.get(0);
		POSDepartment posDepartment = new POSDepartment();
		posDepartment.setCodDptoCer(posDepartmentCode);
		return posDepartment;
	}

	private POSDepartment getPOSDepartment(String posDepartmentCode) {

		Query query = sessionArts
				.createQuery("from com.allc.entities.POSDepartment where codDptoCer = '" + posDepartmentCode + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (POSDepartment) list.get(0);

		return null;
	}

	public List getAllPOSDeparment() {
		try {
			Query query = sessionArts.createQuery(" FROM com.allc.entities.POSDepartment ORDER BY ID_DPT_PS ASC");
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return rows;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public List<Object[]> getAllMerchandiseHierarchyGroup() {
		try {
			SQLQuery query = sessionArts.createSQLQuery(
					"SELECT ID_DPT_PS.ID_DPT_PS AS DEPTO, CO_MRHRC_GP.CD_MRHRC_GP AS FAMILIA, CO_MRHRC_GP.PORC_REC AS PORC_FAMILIA, ID_DPT_PS.PORC_DESEMP AS PORC_DESEMP, ID_DPT_PS.PORC_BOSOL AS PORC_BOSOL FROM ID_DPT_PS INNER JOIN CO_MRHRC_GP ON CO_MRHRC_GP.ID_DPT_PS = ID_DPT_PS.ID_DPT_PS  ORDER BY ID_DPT_PS.ID_DPT_PS ASC, CO_MRHRC_GP.ID_MRHRC_GP ASC");
			List<Object[]> rows = query.list();
			return rows;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private MerchandiseHierarchyGroup getMerchandiseHierarchyGroupByCode(String merchandiseHierarchyGroupCode,
			POSDepartment posDepartment) {

		Query query = sessionArts.createQuery("from com.allc.entities.MerchandiseHierarchyGroup where codMRHCer = '"
				+ merchandiseHierarchyGroupCode + "' and posDepartment.id = " + posDepartment.getId());
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (MerchandiseHierarchyGroup) list.get(0);

		MerchandiseHierarchyGroup merchandiseHierarchyGroup = new MerchandiseHierarchyGroup();
		merchandiseHierarchyGroup.setCodMRHCer(merchandiseHierarchyGroupCode);
		return merchandiseHierarchyGroup;
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

	public void initTx() {
		while (tx == null || !tx.isActive()) {
			try {
				tx = sessionArts.beginTransaction();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				tx = null;
			}
			if (tx == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA TRANSACCION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}
	}

	private boolean crearArchivosParaControlador() {
		List tiendasActivas = getAllActiveStore(0);
		if(Integer.valueOf(store).compareTo(0)!=0)
			logger.info("Tienda: "+store);
		if(tiendasActivas != null && !tiendasActivas.isEmpty())
			logger.info("Tiendas activas 0");
		if (Integer.valueOf(store).compareTo(0)!=0 || (tiendasActivas != null && !tiendasActivas.isEmpty())) {
			logger.info("Iniciando creacion de archivos de secciones y subsecciones para controlador.");
	
			List deptos = getAllPOSDeparment();
			if (deptos != null && !deptos.isEmpty()) {
				try {
					Iterator itDpts = deptos.iterator();
					File dptFile = new File(inFolder, nameFileController + ".DAT");
					File secGui = new File(inFolder, nameFileSecGui + ".DAT");
					BufferedWriter bwr = new BufferedWriter(new FileWriter(dptFile, true));
					BufferedWriter bwrGui = new BufferedWriter(new FileWriter(secGui, true));
					int cantSec = 0;
					while (itDpts.hasNext()) {
						POSDepartment posDepto = (POSDepartment) itDpts.next();
	
						String codDepto = String.valueOf(posDepto.getId());
						while (codDepto.length() < 4)
							codDepto = "0" + codDepto;
						String porcDsct = "0" + String.valueOf(posDepto.getPorcentajeRecargo() / 100);
						String porcDsctFinal = null;
						String flDscEmp = null;
						String flBonSol = null;
						porcDsctFinal = porcDsct.substring(0, 2);
	
						if (posDepto.getPorcentajeDscEmp() != null && posDepto.getPorcentajeDscEmp() > 0)
							flDscEmp = "1";
						else
							flDscEmp = "0";
						if (posDepto.getPorcentajeBonSol() != null && posDepto.getPorcentajeBonSol() > 0)
							flBonSol = "1";
						else
							flBonSol = "0";
	
						bwr.write(codDepto + "0000" + porcDsctFinal + flDscEmp + flBonSol);
						bwr.newLine();
	
						bwrGui.write(posDepto.getCodDptoCer() + "-" + posDepto.getName().trim());
						if (cantSec < deptos.size() - 1)
							bwrGui.write("|");
						cantSec++;
	
					}
					if (bwr != null && bwrGui != null) {
						bwr.close();
						bwrGui.close();
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			} else
				return false;
	
			deptos = null;
			List<Object[]> dataFlias = getAllMerchandiseHierarchyGroup();
			if (dataFlias != null && !dataFlias.isEmpty()) {
	
				try {
					File dptFile = new File(inFolder, nameFileController + ".DAT");
					BufferedWriter bwr = new BufferedWriter(new FileWriter(dptFile, true));
					for (int i = 0; i < dataFlias.size(); i++) {
						Object[] row = dataFlias.get(i);
						String codDepto = row[0].toString();
						while (codDepto.length() < 4)
							codDepto = "0" + codDepto;
	
						String codFlia = row[1].toString();
						while (codFlia.length() < 4)
							codFlia = "0" + codFlia;
						String porcRec = row[2].toString();
						String porcRecFinal = null;
						if (porcRec.length() == 4)
							porcRecFinal = porcRec.substring(0, 2);
						else if (porcRec.length() == 3 || porcRec.length() == 1)
							porcRecFinal = "0" + porcRec.substring(0, 1);
	
						Integer porcDesEmp = Integer.valueOf(row[3].toString());
						Integer porcBonSol = Integer.valueOf(row[4].toString());
						String flDscEmp = null;
						String flBonSol = null;
						if (porcDesEmp > 0)
							flDscEmp = "1";
						else
							flDscEmp = "0";
						if (porcBonSol > 0)
							flBonSol = "1";
						else
							flBonSol = "0";
						bwr.write(codDepto + codFlia + porcRecFinal + flDscEmp + flBonSol);
						bwr.newLine();
	
					}
					if (bwr != null)
						bwr.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
	
			}
	
			try {
	
				File dptFile = new File(inFolder, nameFileController + ".CMD");
				File secFileGui = new File(inFolder, nameFileSecGui + ".CMD");
				BufferedWriter bwr = new BufferedWriter(new FileWriter(dptFile, true));
				BufferedWriter bwrGUI = new BufferedWriter(new FileWriter(secFileGui, true));
				bwr.write(linea1ArchivoCMD);
				bwrGUI.write(linea1ArchivoCMD);
				bwr.newLine();
				bwr.write(linea2ArchivoCMD);
				bwr.newLine();
				if (bwr != null && bwrGUI != null) {
					bwr.close();
					bwrGUI.close();
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			if(Integer.valueOf(store).intValue() != 0) {
				Store storeToSend = storeDAO.getStoreByCode(session, Integer.valueOf(store));
				tiendasActivas = new ArrayList();
				tiendasActivas.add(storeToSend);
			}
			logger.info("Inicia copiado a folder de tiendas activas.");
			Iterator itStores = tiendasActivas.iterator();
			while (itStores.hasNext()) {
				Store tienda = (Store) itStores.next();
				String codTienda = tienda.getKey().toString();
				while (codTienda.length() < 3)
					codTienda = "0" + codTienda;
				File pathTienda = new File(outFolderPart1 + codTienda + outFolderPart2);

				try {
					FilesHelper.copyFile(inFolder.getAbsolutePath(), pathTienda.getAbsolutePath(),
							nameFileController + ".DAT", nameFileController + ".DAT");
					FilesHelper.copyFile(inFolder.getAbsolutePath(), pathTienda.getAbsolutePath(),
							nameFileController + ".CMD", nameFileController + ".CMD");
					FilesHelper.copyFile(inFolder.getAbsolutePath(), pathTienda.getAbsolutePath(),
							nameFileSecGui + ".DAT", nameFileSecGui + ".DAT");
					FilesHelper.copyFile(inFolder.getAbsolutePath(), pathTienda.getAbsolutePath(),
							nameFileSecGui + ".CMD", nameFileSecGui + ".CMD");
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}

			}
			File datFile = new File(inFolder, nameFileController + ".DAT");
			File cmdFile = new File(inFolder, nameFileController + ".CMD");
			File datFileGUI = new File(inFolder, nameFileSecGui + ".DAT");
			File cmdFileGUI = new File(inFolder, nameFileSecGui + ".CMD");
			datFile.delete();
			cmdFile.delete();
			datFileGUI.delete();
			cmdFileGUI.delete();

		} else
			logger.info("No existen tiendas activas para copiar los archivos.");

		return true;
	}

	public boolean enviarArchivoATiendas(File fileToProcess) {

		List activeStoreWihtLocalServer = getAllActiveStore(1);

		if (activeStoreWihtLocalServer != null && !activeStoreWihtLocalServer.isEmpty()) {
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

				List list = Arrays.asList(p.split(data.toString()));
				Frame frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
						ArmsServerConstants.Communication.FRAME_SEP);
				if (frame.loadData()) {
					boolean send = sendFrame(frame, properties, 0);
					closeClient();
					if (send) {
						logger.info("Archivo " + fileToProcess.getName() + " enviado correctamente a la tienda: "+tienda.getKey().toString()+".");
					} else {
						logger.error("Error al enviar el archivo " + fileToProcess.getName() + " a la tienda: "+tienda.getKey().toString()+".");
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
	
	protected boolean connectClient(PropFile properties,  Integer tienda) {

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
		logger.info("Deteniendo UpdateSecSubSecProcess...");
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
		logger.info("Finalizó el Proceso de Actualización de Secciones y Subsecciones.");
		return true;
	}

}
