/**
 * 
 */
package com.allc.arms.server.processes.cer.itemUpdate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.allc.arms.server.persistence.cer.itemBalanza.ItemBalanzaDAO;
import com.allc.arms.server.persistence.fleje.ArchivoImp;
import com.allc.arms.server.persistence.fleje.ArchivoSAP;
import com.allc.arms.server.persistence.fleje.Fleje;
import com.allc.arms.server.persistence.fleje.FlejesDAO;
import com.allc.arms.server.persistence.operator.Operator;
import com.allc.arms.server.persistence.store.RetailStoreDAO;
import com.allc.arms.server.persistence.store.Store;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.entities.Item;
import com.allc.entities.ItemStore;
import com.allc.entities.MerchandiseHierarchyGroup;
import com.allc.entities.POSDepartment;
import com.allc.entities.RetailStore;
import com.allc.files.helper.FilesHelper;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * @author gustavo
 *
 */
public class SearchItemFileProcess extends AbstractProcess {
	private static Logger log = Logger.getLogger(SearchItemFileProcess.class);
	private File inFolder;
	private File inInitFolder;
	private File prcFolder;
	private File bkpFolderFlejes;
	private File bkpFolder;
	private File ctrlInFolder;
	private int sleepTime;
	private Iterator filesToProcess = null;
	public boolean isEnd = false;
	private Session session = null;
	private Session sessionArts = null;
	private Session sessionSaadmin = null;
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
	private boolean cargaInicial = false;
	private List filesToSend = null;
	protected ConnSocketClient socketClient;
	protected StoreDAO storeDAO = new StoreDAO();
	protected Store centralStore;
	private static Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	private String numArchivoReg = null;
	FlejesDAO flejesDAO = new FlejesDAO();
	private Transaction tx;

	protected void inicializar() {
		isEnd = false;
		try {
			inFolder = new File(properties.getObject("searchItem.in.folder.path"));
			inFolder.mkdirs();

			iniciarSaadminSesion();
			ParamsDAO paramsDAO = new ParamsDAO();

			log.info("Iniciando SEarchItem");
			ParamValue paravalue = paramsDAO.getParamByClave(sessionSaadmin,
					Integer.valueOf(properties.getObject("eyes.store.code")).toString(),
					ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "DIR_SAP");
			inInitFolder = new File(properties.getObject("SUITE_ROOT") + File.separator + paravalue.getValor()
					+ properties.getObject("searchItem.init.folder.path"));
			log.info("init Folder:" + inInitFolder);
			inInitFolder.mkdirs();

			centralStore = storeDAO.getStoreByCode(sessionSaadmin, 0);

			prcFolder = new File(properties.getObject("updateItem.bd.in.folder.path"));
			bkpFolderFlejes = new File(properties.getObject("searchPrintFile.printer.out.folder.path"));
			bkpFolder = new File(properties.getObject("updateItem.bd.out.folder.path"));
			sleepTime = properties.getInt("searchItem.sleeptime");

			numArchivoReg = properties.getObject("FILE_REG_ITEM");

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void run() {
		log.info("Iniciando SearchItemFileProcess...");
		inicializar();
		//FlejesDAO flejesDAO = new FlejesDAO();
		RetailStoreDAO retailStoreDAO = new RetailStoreDAO();

		String filename = null;
		String storeCode = properties.getObject("eyes.store.code");
		while (!isEnd) {
			try {
				log.info("Buscando archivos");
				File itemFile = getNextItemFile();
				filesToSend = new ArrayList();
				log.info("Buscando archivos2");
				if (itemFile != null) {
					iniciarSesion();
					iniciarArtsSesion();
					filename = itemFile.getName().toUpperCase();
					String[] parts = filename.split("\\.");
					// Integer store = new Integer(parts[1]);
					Integer store = new Integer(parts[0].substring(4, 7));
					String storeAux = parts[0].substring(4, 7);
					String sequence = parts[1];
					RetailStore retailStore = retailStoreDAO.getRetailStoreByCode(sessionArts, store);
					ctrlInFolder = new File(properties.getObject("updateItem.bd.4690.folder.path") + File.separator
							+ storeAux + File.separator + properties.getObject("updateItem.bd.4690.folder.name"));
					// String sequence = parts[0].substring(4, 8);

					UtilityFile
							.createWriteDataFile(getEyesFileName(),
									"SRCH_ITEM_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + storeCode + "|STR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
													.format(new Date())
											+ "|Archivo a Procesar: " + filename + ".\n",
									true);
					log.info("Archivo a procesar: " + filename + " STORE: " + store);
					if (!flejesDAO.existeArcSAP(session, filename, store)) {
						boolean clean = cleanDirectory(storeAux, sequence);
						if (clean)
							log.info("Directorio limpiado con exito.");
						else
							log.info("El directorio no pudo ser limpiado con exito.");
						
						boolean cleanDB = cleanDataBase(storeAux, filename);
						if (cleanDB)
							log.info("Base de Datos limpiada con exito.");
						else
							log.info("La Base de Datos no tenia informacion para borrar o no pudo ser limpiada con exito.");
						
						
						
						numEeans = 0;
						numErres = 0;
						numItems = 0;
						numErris = 0;
						// File eanFile = new File(inFolder, "EAN" + sequence +
						// "." + parts[1]);
						log.info("NOMBRE EAN: " + "EAN" + storeAux + "." + sequence);
						File eanFile = new File(cargaInicial ? inInitFolder : inFolder,
								"EAN" + storeAux + "." + sequence);
						if (eanFile.exists()) {
							Map loteFilesMap = new HashMap();
							String loteAutoFileDesc = sequence + "." + storeAux;
							// Map hash = cargarHash(eanFile, sequence,
							// parts[1]);
							Map hash = cargarHash(eanFile, sequence, parts[0].substring(4, 7));
							if (hash != null && !hash.isEmpty()) {
								FileReader fr = new FileReader(itemFile);
								BufferedReader br = new BufferedReader(fr);
								BufferedWriter writer = null;
								File errorItemFile = null;
								String linea = br.readLine();
								while (linea != null) {
									numItems++;
									if (linea.length() < 250) {
										if (errorItemFile == null) {
											// errorItemFile = new
											// File(inFolder, "ERRI" + sequence
											// + "." + parts[1]);
											errorItemFile = new File(inFolder, "ERRI" + sequence + "." + storeAux);
											writer = new BufferedWriter(
													new OutputStreamWriter(new FileOutputStream(errorItemFile)));
										}
										writer.write(linea);
										writer.newLine();
										numErris++;
									} else {
										String codSap = linea.substring(68, 86);
										String codItem = linea.substring(1, 13);
										String accion = linea.substring(0, 1);
										POSDepartment dep = getPOSDepartmentByCode(linea.substring(31, 35));
										MerchandiseHierarchyGroup mhg = null;
										boolean itemValido = true;
										if (dep != null) {
											mhg = getMerchandiseHierarchyGroupByCode(linea.substring(35, 38), dep);
											if (mhg == null)
												itemValido = false;
										} else
											itemValido = false;
										if (itemValido) {
											boolean existeIS = existeItemStore(new Long(codItem), store);
											boolean imprimir = validarAtribImp(new Long(codItem), store, linea);

											// si es un registro a borrar debe
											// ser desatendido
											if (ACTION_DELETE.equalsIgnoreCase(accion))
												imprimir = false;
											if (cargaInicial)
												imprimir = false;

											String codDepart = dep.getId().toString();
											while (codDepart.length() < 3)
												codDepart = "0" + codDepart;
											// String fileDesc = sequence +
											// codDepart + "." + parts[1];
											String fileDesc = sequence + codDepart + "." + storeAux;
											String barcode = "000000000000";
											if (hash.containsKey(codSap)) {
												List barcodes = (List) hash.get(codSap);
												Item item = getItemByCode(codSap);

												List<BigInteger> data = null;

												if (item != null)
													data = getBarcodesXItem(item.getItemID(), retailStore.getRetailStoreID());
												
												File peanFile = new File(inFolder,
														"PE" + (imprimir ? fileDesc : loteAutoFileDesc));
												BufferedWriter bwr = new BufferedWriter(new FileWriter(peanFile, true));
												for (int i = 0; i < barcodes.size(); i++) {
													String barcodeAux = (String) barcodes.get(i);
													if (barcodeAux.endsWith("P"))
														barcode = barcodeAux.substring(0, 12);

													if (data != null && !data.isEmpty()) {
														Iterator barCodeBd = data.iterator();
														boolean exiteBC = false;
														int indice = 0;
														while (barCodeBd.hasNext()) {
															BigInteger bcAux = (BigInteger) barCodeBd.next();
//															log.info("BARCODE BD: " + bcAux);
//															log.info("BARCODE FILE: " + new BigInteger(barcodeAux.substring(0,12)));
															if(bcAux.compareTo(new BigInteger(barcodeAux.substring(0,12))) == 0){
																barCodeBd.remove();
															}
														}
//														 if(data.contains(Long.parseLong(barcodeAux.substring(0,12))))
//															 data.remove(Long.parseLong(barcodeAux.substring(0, 12)));
													}
													bwr.write(accion + codItem + barcodeAux);
													bwr.newLine();
												}

												if (data != null && !data.isEmpty() && cargaInicial == false) {

													for (int i = 0; i < data.size(); i++) {

														String barcodeAuxTmp = StringUtils.leftPad(String.valueOf((BigInteger) data.get(i)),12,"0");
//														log.info("BARCODE A BORRAR: " + barcodeAuxTmp);
														bwr.write(ACTION_DELETE + codItem + barcodeAuxTmp + "A");
														bwr.newLine();
													}
												}

												bwr.close();
											}
											File loteFile = null;
											Fleje fleje = null;
											if (!loteFilesMap
													.containsKey("LO" + (imprimir ? fileDesc : loteAutoFileDesc))) {
												fleje = new Fleje();
												fleje.setName(filename);
												fleje.setNumItems(0);
												fleje.setStatus(5);
												fleje.setStore(store);
												fleje.setLote("LO" + (imprimir ? fileDesc : loteAutoFileDesc));
												fleje.setCodNegocio(
														imprimir ? Integer.valueOf(dep.getCodNegocio()) : 0);
												fleje.setCodDepto(imprimir ? dep.getId() : 0);
												loteFilesMap.put("LO" + (imprimir ? fileDesc : loteAutoFileDesc),
														fleje);
												filesToSend.add("LO" + (imprimir ? fileDesc : loteAutoFileDesc));
												filesToSend.add("PE" + (imprimir ? fileDesc : loteAutoFileDesc));
											} else {
												fleje = (Fleje) loteFilesMap
														.get("LO" + (imprimir ? fileDesc : loteAutoFileDesc));
											}
											if (imprimir) {
												// generamos archivos de
												// impresion
												File printFile = getPrintFileName(fleje, fileDesc);
												BufferedWriter bwr = new BufferedWriter(
														new FileWriter(printFile, true));
												StringBuffer sb = new StringBuffer();
												String tax = retailStore.getTax1().toString();
												if (tax.length() > 2)
													tax = tax.substring(0, tax.length() - 2) + "."
															+ tax.substring(tax.length() - 2);
												String porcRec = "00" + (mhg.getPorcentajeRecargo().intValue() > 0
														? mhg.getPorcentajeRecargo().toString()
														: dep.getPorcentajeRecargo().toString());
												porcRec = porcRec.substring(0, porcRec.length() - 2) + "."
														+ porcRec.substring(porcRec.length() - 2);

												sb.append(StringUtils.rightPad(linea.substring(31, 35), 12, "0"))
														.append(StringUtils.leftPad(barcode, 18, "0"))
														.append(linea.substring(86, 126) + "     ")
														.append(StringUtils.leftPad(linea.substring(125, 135), 20, " "))
														.append(StringUtils.leftPad(linea.substring(216, 231), 20, " "))
														.append(StringUtils.leftPad(linea.substring(183, 198), 20, " "))
														.append("Y".equalsIgnoreCase(String.valueOf(linea.charAt(64)))
																? "0" : "1")
														.append(StringUtils.leftPad(linea.substring(146, 164), 20, " "))
														.append(StringUtils.leftPad(linea.substring(231, 237), 8, " "))
														.append("0" + linea.substring(38, 44) + "."
																+ linea.substring(44, 46))
														.append(StringUtils.leftPad("1", 8, "0")).append("P")
														.append(StringUtils.leftPad(porcRec, 6, "0"))
														.append(StringUtils.leftPad(tax, 6, "0")).append("      ")
														.append((mhg.getPorcentajeRecargo().intValue() > 0
																|| dep.getPorcentajeRecargo().intValue() > 0) ? 1 : 0);
												bwr.write(sb.toString());
												bwr.newLine();
												bwr.close();
												ArchivoImp arcImp = getArcImp(fleje, printFile);
												if (arcImp == null) {
													arcImp = new ArchivoImp();
													arcImp.setEstado(0);
													arcImp.setArchivo(printFile.getName());
													filesToSend.add(printFile.getName());
													if (fleje.getArchivoImpList() == null)
														fleje.setArchivoImpList(new ArrayList());
													fleje.getArchivoImpList().add(arcImp);
												}
											}
											loteFile = new File(inFolder,
													"LO" + (imprimir ? fileDesc : loteAutoFileDesc));
											BufferedWriter bw = new BufferedWriter(new FileWriter(loteFile, true));
											fleje.setNumItems(fleje.getNumItems() + 1);
											if (linea.substring(248, 249).equals("N")) {
												log.info("Linea:" + linea);
											} else {
												if (Integer.valueOf(linea.substring(248, 250)).intValue() == 2) {
													// TODO: cuando envien bien
													// estos flags para recarga,
													// esta linea se debe
													// eliminar.
													linea = linea.substring(0, 50) + "N" + linea.substring(51, 55) + "N"
															+ linea.substring(56);
													// } else {
													// linea =
													// linea.substring(0, 60) +
													// "Y" +
													// linea.substring(61);
												}
												if(linea.substring(247, 248).equals("Y")){
													long unid = Long.valueOf(linea.substring(231, 237)).longValue();
													long precCaja = Long.valueOf(linea.substring(237, 247)).longValue();
													long precUnit = Long.valueOf(linea.substring(38, 46)).longValue();
													if(precCaja > unid * precUnit){
														//si el precio mayorista es mayor al precio normal, seteamos el flag para que no permita venta mayorista, porque es un error
														linea = linea.substring(0, 247) + "N" + linea.substring(248);
														UtilityFile.createWriteDataFile(getEyesFileName(), "SRCH_ITEM_P|" + properties.getHostName() + "|3|"
																		+ properties.getHostAddress() + "|" + storeCode	+ "|WAR|" + ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
																		+ "|Excepción al procesar el Archivo: " + filename + " - Ítem " + codItem + " con precio por caja invalido.\n", true);
													}
												}

												linea = linea.substring(0, 31) + "0" + codDepart
														+ StringUtils.leftPad(mhg.getCode().toString(), 3, "0")
														+ linea.substring(38);
												bw.write(linea + barcode);
												bw.newLine();
												bw.close();
												if (ACTION_ADD.equalsIgnoreCase(accion) && existeIS) {
													UtilityFile.createWriteDataFile(getEyesFileName(),
															"SRCH_ITEM_P|" + properties.getHostName() + "|3|"
																	+ properties.getHostAddress() + "|" + storeCode
																	+ "|WAR|"
																	+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
																			.format(new Date())
																	+ "|Excepción al procesar el Archivo: " + filename
																	+ " - Alta de ítem " + codItem + " existente.\n",
															true);
												}
												if (ACTION_UPDATE.equalsIgnoreCase(accion) && !existeIS) {
													UtilityFile.createWriteDataFile(getEyesFileName(),
															"SRCH_ITEM_P|" + properties.getHostName() + "|3|"
																	+ properties.getHostAddress() + "|" + storeCode
																	+ "|WAR|"
																	+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
																			.format(new Date())
																	+ "|Excepción al procesar el Archivo: " + filename
																	+ " - Actualización de ítem " + codItem
																	+ " inexistente.\n",
															true);
												}
												if (ACTION_DELETE.equalsIgnoreCase(accion) && !existeIS) {
													UtilityFile.createWriteDataFile(getEyesFileName(),
															"SRCH_ITEM_P|" + properties.getHostName() + "|3|"
																	+ properties.getHostAddress() + "|" + storeCode
																	+ "|WAR|"
																	+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
																			.format(new Date())
																	+ "|Excepción al procesar el Archivo: " + filename
																	+ " - Baja de ítem " + codItem + " inexistente.\n",
															true);
												}
											}
										} else {
											UtilityFile.createWriteDataFile(getEyesFileName(),
													"SRCH_ITEM_P|" + properties.getHostName() + "|3|"
															+ properties.getHostAddress() + "|" + storeCode + "|WAR|"
															+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
																	.format(new Date())
															+ "|Excepción al procesar el Archivo: " + filename
															+ " - Item " + codItem + " Inválido.\n",
													true);
										}
									}
									linea = br.readLine();
								}
								br.close();
								fr.close();
								if (writer != null)
									writer.close();
							}

							ArchivoSAP archivo = new ArchivoSAP();
							archivo.setCodTienda(store);
							archivo.setNumLote(Long.valueOf(sequence));
							archivo.setNombreItem(filename);
							archivo.setNumItems(numItems);
							// archivo.setNombreEan("EAN" + sequence + "." +
							// parts[1]);
							archivo.setNombreEan("EAN" + storeAux + "." + sequence);
							archivo.setNumEans(numEeans);
							// archivo.setNombreErri("ERRI" + sequence + "." +
							// parts[1]);
							archivo.setNombreErri("ERRI" + storeAux + "." + sequence);
							archivo.setNumErris(numErris);
							// archivo.setNombreErre("ERRE" + sequence + "." +
							// parts[1]);
							archivo.setNombreErre("ERRE" + storeAux + "." + sequence);
							archivo.setNumErres(numErres);
							// seteo este estado para que la suite no lo
							// encuentre
							archivo.setStatus(-1);
							boolean hayError = false;
							Iterator itFiles = loteFilesMap.values().iterator();
							while (itFiles.hasNext()) {
								Fleje fleje = (Fleje) itFiles.next();
								fleje.setArchivo(archivo);
								if (flejesDAO.insertaFleje(session, fleje) && flejesDAO.insertaMov(session,
										fleje.getFlejesId(), fleje.getStatus(), hourFormat.format(new Date()))) {
									// session.flush();
									// if(fleje.getArchivoImpList() != null){
									// Fleje flejeAux =
									// flejesDAO.getFleje(session,
									// fleje.getName());
									// Iterator itArcImp =
									// fleje.getArchivoImpList().iterator();
									// while(itArcImp.hasNext()){
									// ArchivoImp arcImp =
									// (ArchivoImp)itArcImp.next();
									// arcImp.setFlejesId(flejeAux.getFlejesId());
									// flejesDAO.insertaArchivoImp(session,
									// arcImp);
									// }
									// }
									emailSender.send(filename);
									log.info("Fleje registrado.");
								} else {
									hayError = true;
									log.error("Fleje no registrado: " + fleje.getLote());
								}
							}
							limpiarBalanza(store);
							String tiendaToNameFile = String.valueOf(store);
							while (tiendaToNameFile.length() < 3)
								tiendaToNameFile = "0" + tiendaToNameFile;
							String regItemFileName = numArchivoReg + tiendaToNameFile + "." + sequence;

							if (storeDAO.hayServidorLocal(sessionSaadmin, store)) {
								try {
									File regItemFile = new File(inFolder, regItemFileName);
									BufferedWriter bwr = new BufferedWriter(new FileWriter(regItemFile, true));
									String tienda = StringUtils.leftPad(String.valueOf(archivo.getCodTienda()), 6, "0");
									String numLote = StringUtils.leftPad(String.valueOf(archivo.getNumLote()), 14, "0");
									String arcItem = StringUtils.leftPad(archivo.getNombreItem(), 50, "");
									String numItems = StringUtils.leftPad(String.valueOf(archivo.getNumItems()), 6,
											"0");
									String arcEan = StringUtils.leftPad(archivo.getNombreEan(), 50, "");
									String numEans = StringUtils.leftPad(String.valueOf(archivo.getNumEans()), 6, "0");
									String arcErri = StringUtils.leftPad(archivo.getNombreErri(), 50, "");
									String numErri = StringUtils.leftPad(String.valueOf(archivo.getNumErris()), 6, "0");
									String arcErre = StringUtils.leftPad(archivo.getNombreErre(), 50, "");
									String numErres = StringUtils.leftPad(String.valueOf(archivo.getNumErres()), 6,
											"0");
									String status = String.valueOf(archivo.getStatus());
									bwr.write("ARC_SAP" + tienda + numLote + arcItem + numItems + arcEan + numEans
											+ arcErri + numErri + arcErre + numErres + status);
									bwr.newLine();
									Iterator itFlejes = loteFilesMap.values().iterator();
									while (itFlejes.hasNext()) {
										Fleje fleje = (Fleje) itFlejes.next();
										String nomFleje = StringUtils.leftPad(fleje.getName(), 50, "");
										String numItem = StringUtils.leftPad(String.valueOf(fleje.getNumItems()), 6,
												"0");
										String codTienda = StringUtils.leftPad(String.valueOf(fleje.getStore()), 6,
												"0");
										String statusFleje = String.valueOf(fleje.getStatus());
										String loteFleje = StringUtils.leftPad(fleje.getLote(), 50, "");
										String codNegocio = StringUtils.leftPad(String.valueOf(fleje.getCodNegocio()),
												3, "0");
										String codDepto = StringUtils.leftPad(String.valueOf(fleje.getCodDepto()), 4,
												"0");
										bwr.write("ARC_PRC" + nomFleje + numItem + codTienda + statusFleje + loteFleje
												+ codNegocio + codDepto);
										bwr.newLine();
										if (fleje.getArchivoImpList() != null && !fleje.getArchivoImpList().isEmpty()) {
											Iterator itItems = fleje.getArchivoImpList().iterator();
											while (itItems.hasNext()) {
												ArchivoImp arcImp = (ArchivoImp) itItems.next();
												String estado = String.valueOf(arcImp.getEstado());
												String archivoItem = StringUtils.leftPad(arcImp.getArchivo(), 50, "");
												bwr.write("ARC_ITE" + estado + archivoItem);
												bwr.newLine();
											}

										}
									}
									if (bwr != null) {
										bwr.close();
									}
								} catch (Exception e) {
									log.error(e.getMessage(), e);
								}
								filesToSend.add(regItemFileName);
								// enviartodolosarchivos
								// si el archivo a enviar es de impresion se
								// mueve a bkp de flejes
								Iterator itFilesToSend = filesToSend.iterator();
								Store storeToSend = storeDAO.getStoreByCode(sessionSaadmin, store);
								int retries = 3;
								String fileToSend = null;
								while (itFilesToSend.hasNext() && retries > 0) {
									if (retries == 3)
										fileToSend = (String) itFilesToSend.next();
									File itemAuxFile = new File(inFolder, fileToSend);
									if (itemAuxFile.exists()) {
										StringBuffer data = new StringBuffer();
										data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
												.append(ArmsServerConstants.Process.FILE_SENDER_OPERATION)
												.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
												.append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
												.append(ArmsServerConstants.Communication.FRAME_SEP)
												.append(ArmsServerConstants.Communication.PERM_CONN)
												.append(ArmsServerConstants.Communication.FRAME_SEP)
												.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
												.append(ArmsServerConstants.Communication.FRAME_SEP)
												.append(inFolder.getAbsolutePath() + File.separator + fileToSend)
												.append(ArmsServerConstants.Communication.FRAME_SEP)
												.append(inFolder.getAbsolutePath())
												.append(ArmsServerConstants.Communication.FRAME_SEP)
												.append(storeToSend.getIp())
												.append(ArmsServerConstants.Communication.FRAME_SEP)
												.append(properties.getObject("serverSocket.port"));

										List list = Arrays.asList(p.split(data.toString()));
										Frame frame = new Frame(list,
												ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
												ArmsServerConstants.Communication.FRAME_SEP);
										if (frame.loadData()) {
											boolean send = sendFrame(frame, properties, centralStore);
											if (send) {
												retries = 3;
												log.info("Archivo enviado correctamente.");
												if (fileToSend.startsWith("A")) {
													FilesHelper.copyFile(inFolder.getAbsolutePath(),
															bkpFolderFlejes.getAbsolutePath(), fileToSend, fileToSend);
													itemAuxFile.delete();
												} else if ((fileToSend.startsWith("LO")
														&& !fileToSend.equals("LO" + loteAutoFileDesc))
														|| (fileToSend.startsWith("PE")
																&& !fileToSend.equals("PE" + loteAutoFileDesc))) {
													FilesHelper.copyFile(inFolder.getAbsolutePath(),
															prcFolder.getAbsolutePath(), fileToSend, fileToSend);
													itemAuxFile.delete();
												}
											} else {
												retries--;
												log.error("Error al enviar al server.");
											}
										}
									}
								}
								if (retries < 0)
									hayError = true;
								closeClient();
							}

							if (loteFilesMap.containsKey("LO" + loteAutoFileDesc)) {
								String loteName = "LO" + loteAutoFileDesc;
								File loteAutFile = new File(inFolder, loteName);
								FilesHelper.copyFile(inFolder.getAbsolutePath(), prcFolder.getAbsolutePath(), loteName,
										loteName);
								FilesHelper.copyFile(inFolder.getAbsolutePath(), ctrlInFolder.getAbsolutePath(),
										loteName, loteName);
								loteAutFile.delete();
								String peanName = "PE" + loteAutoFileDesc;
								File peanAutFile = new File(inFolder, peanName);
								FilesHelper.copyFile(inFolder.getAbsolutePath(), prcFolder.getAbsolutePath(), peanName,
										peanName);
								FilesHelper.copyFile(inFolder.getAbsolutePath(), ctrlInFolder.getAbsolutePath(),
										peanName, peanName);
								peanAutFile.delete();
								// FilesHelper.copyFile((cargaInicial ?
								// inInitFolder : inFolder).getAbsolutePath(),
								// bkpFolder.getAbsolutePath(),
								// itemFile.getName(), itemFile.getName());
								// FilesHelper.copyFile((cargaInicial ?
								// inInitFolder : inFolder).getAbsolutePath(),
								// bkpFolder.getAbsolutePath(),
								// eanFile.getName(), eanFile.getName());
								// itemFile.delete();
								// eanFile.delete();
							}
							FilesHelper.copyFile((cargaInicial ? inInitFolder : inFolder).getAbsolutePath(),
									bkpFolder.getAbsolutePath(), itemFile.getName(), itemFile.getName());
							FilesHelper.copyFile((cargaInicial ? inInitFolder : inFolder).getAbsolutePath(),
									bkpFolder.getAbsolutePath(), eanFile.getName(), eanFile.getName());
							itemFile.delete();
							eanFile.delete();
							boolean send = false;
							if (!hayError) {
								if (storeDAO.hayServidorLocal(sessionSaadmin, store)) {
									StringBuffer data = new StringBuffer();
									data.append("S").append(ArmsServerConstants.Communication.FRAME_SEP)
											.append(ArmsServerConstants.Process.REGISTRAR_ITEM_FILES_OPERATION)
											.append(ArmsServerConstants.Communication.FRAME_SEP).append("000")
											.append(ArmsServerConstants.Communication.FRAME_SEP).append(0)
											.append(ArmsServerConstants.Communication.FRAME_SEP)
											.append(ArmsServerConstants.Communication.TEMP_CONN)
											.append(ArmsServerConstants.Communication.FRAME_SEP)
											.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
											.append(ArmsServerConstants.Communication.FRAME_SEP)
											.append(regItemFileName);

									List list = Arrays.asList(p.split(data.toString()));
									Frame frame = new Frame(list, ArmsServerConstants.Communication.QTY_MEMBERS_HEADER,
											ArmsServerConstants.Communication.FRAME_SEP);
									if (frame.loadData()) {
										Store storeToSend = storeDAO.getStoreByCode(sessionSaadmin, store);
										log.info(
												"StoreToSend: " + storeToSend.getKey().toString() + " IP: " + storeToSend.getIp());
										send = sendFrame(frame, properties, storeToSend);

										if (send) {
											log.info("Archivo enviado correctamente.");
										} else {
											log.error("Error al enviar al server.");
										}
									}
									// siempre queda en 0 para que la suite
									// copie
									// los archivos al temp y permita procesar
									// Se copia el archivo REG_ITEM al
									// directorio de
									// BKP

									File fileRegToDelete = new File(inFolder, regItemFileName);
									if (fileRegToDelete.exists()) {
										FilesHelper.copyFile(inFolder.getAbsolutePath(), bkpFolder.getAbsolutePath(),
												regItemFileName, regItemFileName);
										fileRegToDelete.delete();
									}
								}
								
								if (!hayError && send) {
									archivo.setStatus(0);
									flejesDAO.insertaArchivo(session, archivo);
								}
								
								UtilityFile.createWriteDataFile(getEyesFileName(),
										"SRCH_ITEM_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
												+ "|" + storeCode + "|END|"
												+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
														.format(new Date())
												+ "|El archivo: " + filename + " se procesó correctamente.\n",
										true);
							} else {
								UtilityFile.createWriteDataFile(getEyesFileName(),
										"SRCH_ITEM_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
												+ "|" + storeCode + "|WAR|"
												+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
														.format(new Date())
												+ "|El archivo: " + filename + " se procesó pero con observaciones.\n",
										true);
							}
						} else {
							UtilityFile.createWriteDataFile(getEyesFileName(),
									"SRCH_ITEM_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + storeCode + "|WAR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
													.format(new Date())
											+ "|El archivo EAN : " + eanFile.getName().toUpperCase() + " no existe.\n",
									true);
							log.info("Archivo EAN no encontrado.");
						}
					} else {
						UtilityFile.createWriteDataFile(getEyesFileName(),
								"SRCH_ITEM_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
										+ storeCode + "|END|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|El archivo: " + filename + " ya estaba registrado.\n",
								true);
						log.info("Archivo ya estaba registrado.");
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					UtilityFile
							.createWriteDataFile(getEyesFileName(),
									"SRCH_ITEM_P|" + properties.getHostName() + "|3|" + properties.getHostAddress()
											+ "|" + storeCode + "|ERR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(
													new Date())
											+ "|Error al procesar el archivo: " + filename + ".\n",
									true);
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
			} finally {
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
		if (sessionSaadmin != null) {
			sessionSaadmin.close();
			sessionSaadmin = null;
		}
		finished = true;
	}

	protected void limpiarBalanza(Integer tienda) {
		ItemBalanzaDAO itemBalanzaDAO = new ItemBalanzaDAO();
		if (itemBalanzaDAO.updateProcesadoAll(session, 'N', new Date(), tienda))
			log.info("Tabla de balanza limpiada.");
		else
			log.info("No se pudo limpiar la tabla de Balanza");
	}

	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
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
					List barcodes = null;
					if (hash.containsKey(code))
						barcodes = (List) hash.get(code);
					if (barcodes == null)
						barcodes = new ArrayList();
					barcodes.add(line.substring(18, 30) + line.substring(31, 32));
					// barcode
					hash.put(code, barcodes);
				} else {
					if (errorEanFile == null) {
						errorEanFile = new File(inFolder, "ERRE" + sequence + "." + storeCode);
						// errorEanFile = new File(inFolder, "ERRE" + storeCode
						// + "." + sequence);
						bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(errorEanFile)));
					}
					bw.write(line);
					bw.newLine();
					numErres++;
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
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
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
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
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	private void iniciarSaadminSesion() {
		while (sessionSaadmin == null && !isEnd) {
			try {
				sessionSaadmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
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

	private File getNextItemFile() {
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd)
					return null;
				File[] files = inInitFolder.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isFile() && (pathname.getName().toUpperCase().startsWith("ITEM"));
					}
				});
				if (files != null && files.length > 0)
					cargaInicial = true;
				else {
					cargaInicial = false;
					files = inFolder.listFiles(new FileFilter() {
						public boolean accept(File pathname) {
							return pathname.isFile() && (pathname.getName().toUpperCase().startsWith("ITEM"));
						}
					});
				}
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
							long sequence1 = 0;
							String name2 = ((File) obj2).getName().toUpperCase();
							long sequence2 = 0;
							sequence1 = Long.valueOf(name1.substring(8, 22));
							sequence2 = Long.valueOf(name2.substring(8, 22));
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

	private POSDepartment getPOSDepartmentByCode(String posDepartmentCode) {
		Query query = sessionArts
				.createQuery("from com.allc.entities.POSDepartment where codDptoCer = '" + posDepartmentCode + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (POSDepartment) list.get(0);
		return null;
	}

	private MerchandiseHierarchyGroup getMerchandiseHierarchyGroupByCode(String merchandiseHierarchyGroupCode,
			POSDepartment posDepartment) {
		if (posDepartment != null && posDepartment.getId() != null) {
			Query query = sessionArts.createQuery("from com.allc.entities.MerchandiseHierarchyGroup where codMRHCer = '"
					+ merchandiseHierarchyGroupCode + "' and posDepartment.id = " + posDepartment.getId());
			List list = query.list();
			if (list != null && !list.isEmpty())
				return (MerchandiseHierarchyGroup) list.get(0);
		}
		return null;
	}

	private ItemStore getItemStore(Integer itemId, Integer retailStoreId) {
		if (itemId != null && retailStoreId != null) {
			Query query = sessionArts.createQuery("from com.allc.entities.ItemStore where itemID = '" + itemId
					+ "' and retailStoreID = '" + retailStoreId + "'");
			List list = query.list();
			if (list != null && !list.isEmpty())
				return (ItemStore) list.get(0);
		}
		ItemStore itemStore = new ItemStore();
		itemStore.setRetailStoreID(retailStoreId);
		return itemStore;
	}

	private Item getItemByCode(Long itemCode) {
		Query query = sessionArts.createQuery("from com.allc.entities.Item where itemCode = '" + itemCode + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (Item) list.get(0);
		Item item = new Item();
		item.setItemCode(itemCode);
		return item;
	}

	private RetailStore getRetailStoreByCode(Integer retailStoreCode) {
		Query query = sessionArts
				.createQuery("from com.allc.entities.RetailStore where code = '" + retailStoreCode + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (RetailStore) list.get(0);
		RetailStore retailStore = new RetailStore();
		retailStore.setCode(retailStoreCode);
		return retailStore;
	}

	private boolean validarAtribImp(Long itemCode, Integer storeCode, String line) {
		HashMap atributos = (HashMap) properties.getMap("searchItem.atrib.item.imprimible");
		Item item = getItemByCode(itemCode);
		RetailStore retailStore = getRetailStoreByCode(storeCode);
		ItemStore itemStore = getItemStore(item.getItemID(), retailStore.getRetailStoreID());
		boolean imprimir = false;
		if (atributos != null && item != null && itemStore != null) {
			if (itemStore.getSalesPrice() == null || item.getName() == null || item.getDescription() == null
					|| itemStore.getFlagMayoreo() == null || itemStore.getCantidadMayoreo() == null)
				imprimir = true;
			else {
				if (atributos.containsKey("precio") && ((String) atributos.get("precio")).trim().equals("1")) {
					Double salePrice = new Double(line.substring(38, 46));
					if (salePrice.compareTo(itemStore.getSalesPrice()) != 0)
						imprimir = true;
				}
				if (atributos.containsKey("name") && ((String) atributos.get("name")).trim().equals("1")) {
					String name = line.substring(13, 31);
					if (name.compareTo(item.getName()) != 0)
						imprimir = true;
				}
				if (atributos.containsKey("descripcion")
						&& ((String) atributos.get("descripcion")).trim().equals("1")) {
					String descrip = line.substring(86, 126);
					if (descrip.compareTo(item.getDescription()) != 0)
						imprimir = true;
				}
				if (itemStore.getFlagMayoreo().compareTo(1) == 0 && atributos.containsKey("cantidadMayoreo")
						&& ((String) atributos.get("cantidadMayoreo")).trim().equals("1")) {
					int cantidadMayoreo = Integer.parseInt(line.substring(231, 237));
					if (cantidadMayoreo != itemStore.getCantidadMayoreo())
						imprimir = true;
				}
			}
		}
		return imprimir;
	}

	private boolean existeItemStore(Long itemCode, Integer storeCode) {
		Query query;
		try {
			query = sessionArts.createSQLQuery(
					"SELECT COUNT(AS_ITM_STR.ID_ITM) FROM AS_ITM, PA_STR_RTL, AS_ITM_STR WHERE AS_ITM_STR.ID_BSN_UN = PA_STR_RTL.ID_BSN_UN AND AS_ITM_STR.ID_ITM = AS_ITM.ID_ITM AND AS_ITM.CD_ITM = "
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

	private ArchivoImp getArcImp(Fleje fleje, File printFile) {
		if (fleje.getArchivoImpList() == null)
			return null;
		Iterator itArcImp = fleje.getArchivoImpList().iterator();
		while (itArcImp.hasNext()) {
			ArchivoImp arcImp = (ArchivoImp) itArcImp.next();
			if (arcImp.getArchivo().equalsIgnoreCase(printFile.getName()))
				return arcImp;
		}
		return null;
	}

	private File getPrintFileName(Fleje fleje, String fileDesc) {
		Integer seq = fleje.getArchivoImpList() == null ? 1 : fleje.getArchivoImpList().size();
		long dif = 0;
		File printFile = null;
		while (dif == 0) {
			try {
				printFile = new File(inFolder, "A" + StringUtils.leftPad(seq.toString(), 4, "0") + "CP" + fileDesc);
				if (!printFile.exists())
					return printFile;
				BufferedReader brd = new BufferedReader(new FileReader(printFile));
				String sCadena = null;
				long lNumeroLineas = 0;
				while ((sCadena = brd.readLine()) != null) {
					lNumeroLineas++;
				}
				dif = 500 - lNumeroLineas;
				brd.close();
				seq++;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return printFile;
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

	protected boolean sendFrame(Frame frame, PropFile properties, Store tienda) {
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

	protected boolean connectClient(PropFile properties, Store tienda) {
		if (socketClient == null || !socketClient.isConnected()) {
			log.info("Store IP: " + tienda.getIp());
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(tienda.getIp());
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

	protected boolean cleanDirectory(String store, String sequence) {

		File[] filesToDelete = getItemFilesToDelete(store, sequence);

		if (filesToDelete != null) {
			for (int i = 0; i < filesToDelete.length; i++) {
				File fileDel = new File(filesToDelete[i].getAbsolutePath());
				fileDel.delete();
			}
			return true;
		} else
			return false;
	}
	
	protected boolean cleanDataBase(String store, String name) {

		List flejes = flejesDAO.getFlejes(session, name);
		
		if (flejes != null && !flejes.isEmpty()) {
		
			log.info("Inicia proceso de borrado de registros de flejes.");
			
			List idsFlejes = getIdsFlejes(name);
			int id = 0;
			Iterator<Integer> itID = idsFlejes.iterator();
			while (itID.hasNext()) {
				id = itID.next();
				updateFlejes(id);
			}
			
			Fleje fleje = new Fleje();
			Iterator<Fleje> it = flejes.iterator();

			while (it.hasNext()) {
				fleje = it.next();
				log.info("Se borro Fleje: " + fleje.getName());
				session.delete(fleje);
				
			}
			
			return true;
		} else
			return false;
	}
	
	private List<Integer> getIdsFlejes(String nameFile) {
		try {
			SQLQuery query = session.createSQLQuery("SELECT ID_ARCPRC FROM ARC_PRC WHERE NOM_ARCPRC = '" + nameFile + "'");
			List<Integer> rows = query.list();
			return rows;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public boolean updateFlejes(int idArcPrc) {
		
		tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createSQLQuery(
					"DELETE FROM ARC_MOV WHERE ID_ARCPRC = " + idArcPrc);
			query.executeUpdate();
			tx.commit();
			log.info("Se borro ID_ARCPRC: " + idArcPrc);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		
		return true;
	}

	private File[] getItemFilesToDelete(final String store, final String sequence) {

		if (isEnd)
			return null;
		File[] files = inFolder.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isFile()
						&& ((pathname.getName().toUpperCase().startsWith("LO")
								&& pathname.getName().substring(2, 16).equals(sequence)
								&& pathname.getName().substring((pathname.getName().length()) - 3,
										pathname.getName().length()).equals(store))
								|| (pathname.getName().toUpperCase().startsWith("PE")
										&& pathname.getName().substring(2, 16).equals(sequence)
										&& pathname.getName()
												.substring((pathname.getName().length()) - 3,
														pathname.getName().length())
												.equals(store))
								|| (pathname.getName().toUpperCase().startsWith("A")
										&& pathname.getName().substring(7, 21).equals(sequence)
										&& pathname.getName()
												.substring((pathname.getName().length()) - 3,
														pathname.getName().length())
												.equals(store))
								|| (pathname.getName().toUpperCase().startsWith("REG_ITEM")
										&& pathname.getName()
												.substring((pathname.getName().length()) - 14,
														pathname.getName().length())
												.equals(sequence)
										&& pathname.getName().substring(8, 11).equals(store))
								|| (pathname.getName().toUpperCase().startsWith("ERRI")
										&& pathname.getName()
												.substring((pathname.getName().length()) - 14,
														pathname.getName().length())
												.equals(sequence)
										&& pathname.getName().substring(4, 7).equals(store))
								|| (pathname.getName().toUpperCase().startsWith("ERRE")
										&& pathname.getName()
												.substring((pathname.getName().length()) - 14,
														pathname.getName().length())
												.equals(sequence)
										&& pathname.getName().substring(4, 7).equals(store)));
			}
		});
		if (files != null && files.length > 0)
			return files;
		else
			return null;
	}

	private Item getItemByCode(String itemCod) {
		Long itemCode = new Long(itemCod);
		Query query = sessionArts.createQuery("from com.allc.entities.Item where itemCode = '" + itemCode + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (Item) list.get(0);

		return null;
	}

	private List<BigInteger> getBarcodesXItem(Integer itemID, Integer retailStoreID) {
//		log.info("ITEM ID PARA CONSULTAR BD: " + itemID);
		try {
			SQLQuery query = sessionArts
					.createSQLQuery("Select ID_ITM_PS  From ID_PS Where ID_ITM = " + itemID + "AND ID_BSN_UN = " + retailStoreID);

			List<BigInteger> list = query.list();
			if (list != null && !list.isEmpty())
				return list;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
}
