package com.allc.arms.server.operations.cer.itemUpdate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.params.ParamValue;
import com.allc.arms.server.params.ParamsDAO;
import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.fleje.ArchivoImp;
import com.allc.arms.server.persistence.fleje.ArchivoSAP;
import com.allc.arms.server.persistence.fleje.Fleje;
import com.allc.arms.server.persistence.fleje.FlejesDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.FilesHelper;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class RegisterItemFilesOperation extends AbstractOperation {

	private Logger logger = Logger.getLogger(RegisterItemFilesOperation.class);
	private File prcFolder;
	private File bkpFolder;
	private File ctrlInFolder;
	private File inFolder;
	private Session session = null;
	private Session sessionSaadmin = null;
	public static final String FILE_SAP = "ARC_SAP";
	public static final String FILE_ITEM = "ARC_ITE";
	public static final String FILE_PRC = "ARC_PRC";
	ArchivoSAP archivoSap = null;
	private List flejes = new ArrayList();
	private SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {

		logger.info("Iniciando RegisterItemFilesOperation...");
		String storeCode = properties.getObject("eyes.store.code");
		StringBuilder msg = new StringBuilder();

		try {
			boolean hayError = false;

			String fileToBd = (String) frame.getBody().get(0);
			iniciarSaadminSesion();
			ParamsDAO paramsDAO = new ParamsDAO();
			FlejesDAO flejesDAO = new FlejesDAO();
			ParamValue paravalue = paramsDAO.getParamByClave(sessionSaadmin,
					Integer.valueOf(properties.getObject("eyes.store.code")).toString(),
					ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "DIR_SAP");
			inFolder = new File(properties.getObject("searchItem.in.folder.path"));
			prcFolder = new File(properties.getObject("updateItem.bd.in.folder.path"));
			bkpFolder = new File(properties.getObject("updateItem.bd.out.folder.path"));
			ctrlInFolder = new File(properties.getObject("updateItem.bd.4690.folder.path") + File.separator + storeCode
					+ File.separator + properties.getObject("updateItem.bd.4690.folder.name"));

			UtilityFile.createWriteDataFile(getEyesFileName(properties),
					"REG_ITM_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando proceso de registrar archivos en BD de la tienda " + storeCode + ".\n",
					true);

			File fileToProcess = new File(inFolder, fileToBd);

			if (fileToProcess != null) {
				FileReader fr = new FileReader(fileToProcess);
				BufferedReader br = new BufferedReader(fr);
				String linea = br.readLine();
				if (FILE_SAP.equalsIgnoreCase(linea.substring(0, 7))) {
					archivoSap = new ArchivoSAP();
					archivoSap.setCodTienda(Integer.valueOf(linea.substring(7, 13)));
					archivoSap.setNumLote(Long.valueOf(linea.substring(13, 27)));
					archivoSap.setNombreItem(linea.substring(27, 77).trim());
					archivoSap.setNumItems(Integer.valueOf(linea.substring(77, 83)));
					archivoSap.setNombreEan(linea.substring(83, 133).trim());
					archivoSap.setNumEans(Integer.valueOf(linea.substring(133, 139)));
					archivoSap.setNombreErri(linea.substring(139, 189).trim());
					archivoSap.setNumErris(Integer.valueOf(linea.substring(189, 195)));
					archivoSap.setNombreErre(linea.substring(195, 245).trim());
					archivoSap.setNumErres(Integer.valueOf(linea.substring(245, 251)));
					archivoSap.setStatus(Integer.valueOf(linea.substring(251, 253)));

					while (linea != null) {

						if (FILE_PRC.equalsIgnoreCase(linea.substring(0, 7))) {

							Fleje fleje = new Fleje();
							fleje.setName(linea.substring(7, 57).trim());
							fleje.setNumItems(Integer.valueOf(linea.substring(57, 63)));
							fleje.setStore(Integer.valueOf(linea.substring(63, 69)));
							fleje.setStatus(Integer.valueOf(linea.substring(69, 70)));
							fleje.setLote(linea.substring(70, 120).trim());
							fleje.setCodNegocio(Integer.valueOf(linea.substring(120, 123)));
							fleje.setCodDepto(Integer.valueOf(linea.substring(123, 127)));
							fleje.setArchivo(archivoSap);
							flejes.add(fleje);

						} else if (FILE_ITEM.equalsIgnoreCase(linea.substring(0, 7))) {
							ArchivoImp arcImp = new ArchivoImp();
							arcImp.setEstado(Integer.valueOf(linea.substring(7, 8)));
							arcImp.setArchivo(linea.substring(8, 58).trim());
							Fleje lastFleje = (Fleje) flejes.get(flejes.size() - 1);
							if (lastFleje.getArchivoImpList() == null)
								lastFleje.setArchivoImpList(new ArrayList());
							lastFleje.getArchivoImpList().add(arcImp);

						}
						linea = br.readLine();
					}
					br.close();
					fr.close();
					iniciarSesion();
					Iterator itFlejes = flejes.iterator();
					while (itFlejes.hasNext()) {
						Fleje fleje = (Fleje) itFlejes.next();
						if (flejesDAO.insertaFleje(session, fleje) && flejesDAO.insertaMov(session, fleje.getFlejesId(),
								fleje.getStatus(), hourFormat.format(new Date()))) {
							logger.info("Fleje registrado.");
						} else {
							hayError = true;
							logger.error("Fleje no registrado: " + fleje.getLote());
						}
					}

					if (!hayError) {
						UtilityFile
								.createWriteDataFile(getEyesFileName(properties),
										"REG_ITM_O|" + properties.getHostName() + "|3|" + properties.getHostAddress()
												+ "|" + storeCode + "|END|"
												+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
														.format(new Date())
												+ "|El archivo: " + fileToBd
												+ " se guardo correctamente en la Base de Datos.\n",
										true);
					} else {
						UtilityFile
								.createWriteDataFile(getEyesFileName(properties),
										"REG_ITM_O|" + properties.getHostName() + "|3|" + properties.getHostAddress()
												+ "|" + storeCode + "|WAR|"
												+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
														.format(new Date())
												+ "|El archivo: " + fileToBd
												+ " se guardo parcialmente en Base de Datos.\n",
										true);
					}
					boolean archivosCopiados = true;
					// Se copian los archivos que son desatendidos
					try {
						Iterator itFlejesToCopy = flejes.iterator();
						while (itFlejesToCopy.hasNext()) {
							Fleje fleje = (Fleje) itFlejesToCopy.next();
							if (fleje.getArchivoImpList() == null || fleje.getArchivoImpList().isEmpty()){
								
								if (copiarArchivosDesatendidos(fleje))
									logger.info("Archivos para el fleje: " + fleje.getName() + " copiados con exito.");
								else {
									archivosCopiados = false;
									logger.info("No se pudieron copiar los archivos  para el fleje: " + fleje.getName()
											+ ".");
								}
							}
						}
					} catch (Exception e) {
						archivosCopiados = false;
						logger.error(e.getMessage(), e);
					}

					if (archivosCopiados) {
						//Se copia el archivo REG_ITEM al directorio de BKP
						FilesHelper.copyFile(inFolder.getAbsolutePath(), bkpFolder.getAbsolutePath(), fileToBd, fileToBd);
						fileToProcess.delete();
						
						// Se responde a central para finalizar la operacion
						msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("0");
						String tmp = Util.addLengthStartOfString(msg.toString(),
								properties.getInt("serverSocket.quantityBytesLength"));
						logger.info(tmp);
						if (socket.writeDataSocket(tmp)) {
							UtilityFile.createWriteDataFile(getEyesFileName(properties),
									"REG_ITM_O|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + storeCode + "|END|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
													.format(new Date())
											+ "|Archivo: " + fileToProcess + " procesado e informado a Central.\n",
									true);
							archivoSap.setStatus(0);
							flejesDAO.insertaArchivo(session, archivoSap);
						} else
							UtilityFile
									.createWriteDataFile(getEyesFileName(properties),
											"REG_ITM_O|" + properties.getHostName() + "|3|"
													+ properties.getHostAddress() + "|" + storeCode + "|WAR|"
													+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
															.format(new Date())
													+ "|No se pudo enviar la respuesta a Central.\n",
											true);
					} else {
						logger.info("Archivos no copiados a PRC y Carpeta del Controlador.");
						try {
							msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("1");
							String tmp = Util.addLengthStartOfString(msg.toString(),
									properties.getInt("serverSocket.quantityBytesLength"));
							logger.info(tmp);
							socket.writeDataSocket(tmp);
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
					}

				} else {
					logger.info("Archivo con formato incorrecto.");
					try {
						msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("1");
						String tmp = Util.addLengthStartOfString(msg.toString(),
								properties.getInt("serverSocket.quantityBytesLength"));
						logger.info(tmp);
						socket.writeDataSocket(tmp);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
					UtilityFile.createWriteDataFile(getEyesFileName(properties),
							"REG_ITM_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
									+ storeCode + "|WAR|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|El archivo: " + fileToBd + " tiene un formato incorrecto.\n",
							true);
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			try {
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("1");
				String tmp = Util.addLengthStartOfString(msg.toString(),
						properties.getInt("serverSocket.quantityBytesLength"));
				logger.info(tmp);
				socket.writeDataSocket(tmp);
			} catch (Exception ex) {
				logger.error(e.getMessage(), e);
			}
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"REG_ITM_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ storeCode + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error durante el proceso de registro de archivos en tienda.\n",
						true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}

		}
		return false;
	}

	private String getEyesFileName(PropFile properties) {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	private void iniciarSaadminSesion() {
		while (sessionSaadmin == null) {
			try {
				sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (sessionSaadmin == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}
	}

	private void iniciarSesion() {
		while (session == null) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory("Flejes").openSession();
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

	public boolean copiarArchivosDesatendidos(Fleje fleje) {

		logger.info("Inicia proceso de copiado de archivos LO y PE.");
		try {

			String loteName = fleje.getLote();
			File loteAutFile = new File(inFolder, loteName);
			String peanName = "PE" + fleje.getLote().substring(2);
			File peanAutFile = new File(inFolder, peanName);
			if (loteAutFile.exists() && peanAutFile.exists()) {
				FilesHelper.copyFile(inFolder.getAbsolutePath(), prcFolder.getAbsolutePath(), loteName, loteName);
				FilesHelper.copyFile(inFolder.getAbsolutePath(), ctrlInFolder.getAbsolutePath(), loteName, loteName);
				loteAutFile.delete();
				logger.info("LO: " + loteName + " copiado con exito.");
				FilesHelper.copyFile(inFolder.getAbsolutePath(), prcFolder.getAbsolutePath(), peanName, peanName);
				FilesHelper.copyFile(inFolder.getAbsolutePath(), ctrlInFolder.getAbsolutePath(), peanName, peanName);
				peanAutFile.delete();
				logger.info("PE: " + peanName + " copiado con exito.");

				return true;
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	@Override
	public boolean process(ConnPipeServer arg0, Frame arg1, PropFile arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
