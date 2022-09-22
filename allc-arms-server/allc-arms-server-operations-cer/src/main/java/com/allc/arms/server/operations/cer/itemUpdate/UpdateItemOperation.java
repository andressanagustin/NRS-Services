package com.allc.arms.server.operations.cer.itemUpdate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.fleje.Fleje;
import com.allc.arms.server.persistence.fleje.FlejesDAO;
import com.allc.arms.server.persistence.store.StoreDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.entities.Item;
import com.allc.entities.ItemBalanza;
import com.allc.entities.ItemStore;
import com.allc.entities.MerchandiseHierarchyGroup;
import com.allc.entities.POSDepartment;
import com.allc.entities.POSIdentity;
import com.allc.entities.RetailStore;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class UpdateItemOperation extends AbstractOperation {
	public static final String ACTION_ADD = "A";
	public static final String ACTION_DELETE = "B";
	public static final String ACTION_UPDATE = "M";
	private Session sesion;
	private Session sessionFlejes = null;
	private Session sessionSaAdmin = null;
	private Transaction tx;
	private Transaction tx2;
	BufferedReader reader = null;
	public boolean isEnd = false;
	protected boolean finished = false;
	static Logger log = Logger.getLogger(UpdateItemOperation.class);
	protected Map stringToInt;
	protected ConnSocketClient socketClient;
	private static Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	protected StoreDAO storeDAO = new StoreDAO();
	protected String storeIP;
	protected Map itemsBalanza = null;
	
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		StringBuilder msg = new StringBuilder(frame.getHeaderStr());
		StringBuilder msgCentral = new StringBuilder();
		FlejesDAO flejesDAO = new FlejesDAO();
		loadHashCharToInt();
		try {
			String itemfile = (String) frame.getBody().get(ArmsServerConstants.Body.UpdateItem.ITEM_NAME);
			UtilityFile.createWriteDataFile(getEyesFileName(properties),
					"UPD_ITEM_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando procesamiento de: " + itemfile + ".\n",
					true);
			String eanfile = frame.getBody().size() > 1
					? (String) frame.getBody().get(ArmsServerConstants.Body.UpdateItem.EAN_NAME) : null;
			log.info("Archivo a procesar: " + itemfile);
			File outFolder = new File(properties.getObject("updateItem.bd.out.folder.path"));
			File fileToProcess = new File(properties.getObject("updateItem.bd.in.folder.path"), itemfile);
			String storeCode = itemfile.split("\\.")[1];
			String folder4690 = properties.getObject("updateItem.bd.4690.folder.path") + File.separator + storeCode
					+ File.separator + properties.getObject("updateItem.bd.4690.folder.name");
			storeCode = properties.getObject("eyes.store.code");
			reader = new BufferedReader(new FileReader(fileToProcess));
			if (itemfile.toUpperCase().startsWith("LO")) {
				openSession();
				iniciarFlejesSesion();
				iniciarSaAdminSesion();
				updateItemPrice(itemfile);
			}
			if (!isEnd) {
				try {
					reader.close();
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
				File file4690 = new File(folder4690, fileToProcess.getName());
				file4690.delete();
				if (fileToProcess.renameTo(new File(outFolder, fileToProcess.getName())))
					log.info("Archivo movido a: " + outFolder);
				else
					log.error("No se pudo mover el archivo.");
				log.info("Archivo procesado correctamente.");
				if (eanfile != null) {
					log.info("Archivo a procesar: " + eanfile);
					fileToProcess = new File(properties.getObject("updateItem.bd.in.folder.path"), eanfile);
					reader = new BufferedReader(new FileReader(fileToProcess));
					if (eanfile.toUpperCase().startsWith("PE")) {
						updateItemBarcode(eanfile);
					}
					if (!isEnd) {
						try {
							reader.close();
						} catch (Exception e1) {
							log.error(e1.getMessage(), e1);
						}
						file4690 = new File(folder4690, fileToProcess.getName());
						file4690.delete();
						if (fileToProcess.renameTo(new File(outFolder, fileToProcess.getName())))
							log.info("Archivo movido a: " + outFolder);
						else
							log.error("No se pudo mover el archivo.");
						log.info("Archivo procesado correctamente.");
					}
				}
				if (!isEnd) {
					Fleje fleje = flejesDAO.getFleje(sessionFlejes, itemfile.toUpperCase());
					if (fleje != null) {
						log.info("Fleje procesado.");
						fleje.setStatus(8);
						flejesDAO.insertaFleje(sessionFlejes, fleje);
						Integer status = flejesDAO.getMinStatusPorArcsap(sessionFlejes, fleje.getArchivo().getId());
						if (status != null && status.compareTo(fleje.getArchivo().getStatus()) != 0) {
							fleje.getArchivo().setStatus(status);
							flejesDAO.insertaArchivo(sessionFlejes, fleje.getArchivo());
						}
					} else
						log.info("Fleje no actualizado.");

					//// MANDA EL MISMO MENSAJE QUE LE LLEGO DESDE EL
					//// CONTROLADOR A CENTRAL SI LA TIENDA ES MAYOR 0
					if (Integer.valueOf(storeCode) > 0) {
						// msgCentral.append(frame.getHeaderStr())
						// .append(frame.getBodyStr());
						// String msgTmp =
						// Util.addLengthStartOfString(msgCentral.toString(),
						// properties.getInt("serverCentralSocket.quantityBytesLength"));
						// log.info(msgTmp);
						if (frame.loadData()) {
							boolean send = sendFrameToCentral(frame, properties);
							closeClient();
							if (send) {
								log.info("Archivo procesado correctamente en Central.");
								msg.append(frame.getSeparator()).append("0");
							} else {
								log.error("Error al informar al Central.");
								msg.append(frame.getSeparator()).append("1");
								UtilityFile.createWriteDataFile(getEyesFileName(properties),
										"UPD_ITEM_O|" + properties.getHostName() + "|3|" + properties.getHostAddress()
												+ "|" + frame.getHeader().get(3) + "|ERR|"
												+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
														.format(new Date())
												+ "|Error al procesar el archivo: "
												+ frame.getBody().get(ArmsServerConstants.Body.UpdateItem.ITEM_NAME)
												+ " en Central.\n",
										true);
							}
						}

					} else
						msg.append(frame.getSeparator()).append("0");
					String tmp = Util.addLengthStartOfString(msg.toString(),
							properties.getInt("serverSocket.quantityBytesLength"));
					log.info(tmp);
					if (socket.writeDataSocket(tmp)) {
						UtilityFile
								.createWriteDataFile(getEyesFileName(properties),
										"UPD_ITEM_O|" + properties.getHostName() + "|3|" + properties.getHostAddress()
												+ "|" + frame.getHeader().get(3) + "|END|"
												+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
														.format(new Date())
												+ "|Archivo: "
												+ frame.getBody().get(ArmsServerConstants.Body.UpdateItem.ITEM_NAME)
												+ " procesado.\n",
										true);
						Thread.sleep(3000);
					} else
						UtilityFile
								.createWriteDataFile(getEyesFileName(properties),
										"UPD_ITEM_O|" + properties.getHostName() + "|3|" + properties.getHostAddress()
												+ "|" + frame.getHeader().get(3) + "|WAR|"
												+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format
														.format(new Date())
												+ "|No se pudo enviar la respuesta.\n",
										true);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("-1");
				String tmp = Util.addLengthStartOfString(msg.toString(),
						properties.getInt("serverSocket.quantityBytesLength"));
				log.info(tmp);
				socket.writeDataSocket(tmp);
			} catch (Exception ex) {
				log.error(e.getMessage(), e);
			}
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"UPD_ITEM_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al procesar el archivo: "
								+ frame.getBody().get(ArmsServerConstants.Body.UpdateItem.ITEM_NAME) + ".\n",
						true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		} finally {
			sessionFlejes.close();
			sessionSaAdmin.close();
			sesion.close();
			sesion = null;
			tx = null;
		}
		finished = true;
		return false;
	}

	private String getEyesFileName(PropFile properties) {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	private void updateItemPrice(String filename) throws Exception {
		String line = "";
		int cantMax = 2000;
		String storeCode = filename.split("\\.")[1];
		RetailStore retailStore = getRetailStoreByCode(storeCode);
		if (retailStore.getRetailStoreID() == null)
			sesion.save(retailStore);
		int cantProc = 0;
		while (line != null && !isEnd) {
			try {
				initTx();
				while (line != null && cantMax > 0 && !isEnd) {
					if (!line.trim().isEmpty()) {
						try {
							Item item = getItemByCode(line.substring(1, 13));
							ItemStore itemStore = getItemStore(item.getItemID(), retailStore.getRetailStoreID());
							ItemBalanza itemBalanza = getItemBalanza(item.getItemID(), retailStore.getCode());
							if (ACTION_DELETE.equalsIgnoreCase(line.substring(0, 1)) && itemStore.getItemID() != null) {

								Iterator<ItemStore> iter = item.getItemsStore().iterator();
								boolean storeFound = false;
								while (iter.hasNext() && storeFound == false) {
									if (iter.next().getRetailStoreID().equals(retailStore.getRetailStoreID())) {
										storeFound = true;
										iter.remove();
										sesion.saveOrUpdate(item);
									}
								}

								sesion.delete(itemStore);
								if (itemBalanza.getDescripcion() != null) {
									initTxFleje();
									sessionFlejes.delete(itemBalanza);
									tx2.commit();
								}
							} else {
								item.setName(line.substring(13, 31));
								String desc = line.substring(86, 126);
								item.setDescription(desc);
								item.setFlWorM(line.charAt(49) == 'Y' ? 1 : 0);
								item.setFlPriceReq(line.charAt(50) == 'Y' ? 1 : 0);
								item.setFlDscItm(line.charAt(52) == 'Y' ? 1 : 0);
								item.setFlQuantityAllw(line.charAt(55) == 'Y' ? 1 : 0);
								item.setFlQuantityReq(line.charAt(56) == 'Y' ? 1 : 0);
								item.setItemType(0);
								String codSAP = line.substring(68, 86).trim();
								item.setCodigoSAP(new Long(codSAP != null && !codSAP.isEmpty() ? codSAP : "0"));
								POSDepartment posDepartment = getPOSDepartmentByCode(line.substring(31, 35));
								MerchandiseHierarchyGroup mhg = getMerchandiseHierarchyGroupByCode(
										line.substring(35, 38), posDepartment);
								item.setMerchandiseHierarchyGroup(mhg);
								item.setReferenciaSAP(line.substring(183, 198));
								item.setJerarquia(line.substring(136, 146));
								Double salePrice = new Double(line.substring(38, 46));
								itemStore.setSalesPrice(salePrice);
								itemStore.setFlAuthorizedForSale(line.charAt(57) == 'Y' ? 1 : 0);
								itemStore.setFlAcumMovDat(line.charAt(48) == 'Y' ? 1 : 0);
								itemStore.setFlCouponComUsed(line.charAt(51) == 'Y' ? 1 : 0);
								itemStore.setFlMultVales(line.charAt(53) == 'Y' ? 1 : 0);
								itemStore.setFlSaleItemExcepLog(line.charAt(54) == 'Y' ? 1 : 0);
								itemStore.setFlLogtochangefile(line.charAt(58) == 'Y' ? 1 : 0);
								itemStore.setFlPointsOnlyItemCoupon(line.charAt(59) == 'Y' ? 1 : 0);
								itemStore.setFlPointsApplytoItem(line.charAt(60) == 'Y' ? 1 : 0);
								itemStore.setFlItemLinkstoDeposit(line.charAt(61) == 'Y' ? 1 : 0);
								itemStore.setFlRestrictedSale(line.charAt(62) == 'Y' ? 1 : 0);
								itemStore.setFlFuelVolumeRequired(line.charAt(63) == 'Y' ? 1 : 0);
								itemStore.setTaxA(line.charAt(64) == 'Y' ? 1 : 0);
								itemStore.setTaxB(line.charAt(65) == 'Y' ? 1 : 0);
								itemStore.setTaxC(line.charAt(66) == 'Y' ? 1 : 0);
								itemStore.setTaxD(line.charAt(67) == 'Y' ? 1 : 0);
								itemStore.setTaxE(new Integer("0"));
								itemStore.setTaxF(new Integer("0"));
								itemStore.setTaxG(new Integer("0"));
								itemStore.setTaxH(new Integer("0"));
								itemStore.setPresentacion(line.substring(126, 136));
								itemStore.setProveedor(line.substring(146, 164));
								itemStore.setDeducible(line.substring(180, 183));
								itemStore.setColor(line.substring(198, 208));
								itemStore.setMedida(line.substring(208, 212));
								itemStore.setDiseno(line.substring(212, 216));
								itemStore.setMarca(line.substring(216, 231));
								int cantidadMayoreo = Integer.parseInt(line.substring(231, 237));
								itemStore.setCantidadMayoreo(cantidadMayoreo);
								Double precioMayoreo = new Double(line.substring(237, 247));
								itemStore.setPrecioMayoreo(precioMayoreo);
								itemStore.setFlagMayoreo(line.charAt(247) == 'Y' ? 1 : 0);
								itemStore.setSpecialFamily(new Integer(line.substring(248, 250)));
								itemStore.setRestricSaleType(new Integer(line.substring(250, 252)));
								if (item.getItemsStore() == null)
									item.setItemsStore(new ArrayList());
								item.getItemsStore().add(itemStore);
								sesion.saveOrUpdate(item);
								if ((item.getFlWorM().equals(1) || item.getFlQuantityReq().equals(1))
										&& salePrice > 0) {
									itemBalanza.setDescripcion(desc);
									String precio = line.substring(38, 46);
									while (precio.length() < 3) {
										precio = "0" + precio;
									}
									String precioFinal = precio.substring(0, precio.length() - 2) + "."
											+ precio.substring(precio.length() - 2, precio.length());
									itemBalanza.setPrecio_pub(new Double(precioFinal));
									itemBalanza.setPrecio_com(new Double(precioFinal));
									String porcentaje = String.valueOf(posDepartment.getPorcentajeRecargo());
									while (porcentaje.length() < 3) {
										porcentaje = "0" + porcentaje;
									}
									String porcentajeFinal = porcentaje.substring(0, porcentaje.length() - 2) + "."
											+ porcentaje.substring(porcentaje.length() - 2, porcentaje.length());
									itemBalanza.setPorc_recargo(new Double(porcentajeFinal));
									itemBalanza.setInd_al_peso(item.getFlWorM().equals(1) ? '1' : '2');
									itemBalanza.setJerarquia_sap(line.substring(136, 146));
									itemBalanza.setInd_empacado('0');

									String desNegocio = getDescNegocio(posDepartment.getCodNegocio());

									if (!desNegocio.equals(null))
										itemBalanza.setNombre_grupo(desNegocio + "-" + posDepartment.getName() + "-"
												+ mhg.getDescription());
									else
										itemBalanza.setNombre_grupo(posDepartment.getCodNegocio() + "-"
												+ posDepartment.getName() + "-" + mhg.getDescription());
									if (itemBalanza.getNombre_grupo().length() > 40)
										itemBalanza.setNombre_grupo(itemBalanza.getNombre_grupo().substring(0, 40));

//									if (posDepartment.getCodDptoCer().equals("00B7")
//											|| posDepartment.getCodDptoCer().equals("00V0")) {
//										if (itemBalanza.getJerarquia_sap().trim().equals("SB7003009")
//												|| itemBalanza.getJerarquia_sap().trim().equals("SB7003010")
//												|| itemBalanza.getJerarquia_sap().trim().equals("SB7003011")
//												|| itemBalanza.getJerarquia_sap().trim().equals("SB7003013")
//												|| itemBalanza.getJerarquia_sap().trim().equals("SB7003014")
//												|| itemBalanza.getJerarquia_sap().trim().equals("SB7003015")
//												|| itemBalanza.getJerarquia_sap().trim().equals("SB7003016")
//												|| itemBalanza.getJerarquia_sap().trim().equals("SB7004017")) {
//
//											itemBalanza.setDias_refriger(new Integer(0));
//											itemBalanza.setDias_congelac(new Integer(0));
//										} else {
//											itemBalanza.setDias_refriger(new Integer(5));
//											itemBalanza.setDias_congelac(new Integer(120));
//										}
//									}
									itemBalanza.setDias_refriger(new Integer(line.substring(252, 255)));
									itemBalanza.setDias_congelac(new Integer(line.substring(255, 258)));
									itemBalanza.setFechaEmision(line.charAt(258));
									
									itemBalanza.setFch_ult_cambio(new Date());
									itemBalanza.setCod_seccion(posDepartment.getCodDptoCer());
									itemBalanza.setCod_subsec(mhg.getCodMRHCer());
									itemBalanza.setProcesado('N');
									itemBalanza.setInd_iva(line.charAt(64) == 'Y' ? '0' : '2');
									if(itemsBalanza == null){
										itemsBalanza = new HashMap();
									}
									itemsBalanza.put(item.getItemID(), itemBalanza);
									
								} else if (itemBalanza.getDescripcion() != null) {
									/*
									 * Se agrega esta condicion para que si se
									 * detecta que el articulo no es pesable y
									 * esta en la tabla de balanzas se borre de
									 * dicha tabla.
									 */
									initTxFleje();
									sessionFlejes.delete(itemBalanza);
									tx2.commit();
								}

							}
						} catch (Exception e) {
							log.error("Problem Line: " + line);
							log.error(e.getMessage(), e);
						}
					}
					cantMax--;
					cantProc++;
					line = reader.readLine();
				}
				tx.commit();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					tx.rollback();
					tx2.rollback();
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}
			log.info("Registros procesados: " + cantProc);
			sesion.clear();
			sessionFlejes.clear();
			cantMax = 2000;
		}
	}

	private void updateItemBarcode(String filename) throws Exception {
		int cantMax = 2000;
		int cantProc = 0;
		String line = "";
		String storeCode = filename.split("\\.")[1];
		RetailStore retailStore = getRetailStoreByCode(storeCode);
		if (retailStore.getRetailStoreID() == null)
			sesion.save(retailStore);
		while (line != null && !isEnd) {
			try {
				initTx();
				while (line != null && cantMax > 0 && !isEnd) {
					if (!line.trim().isEmpty()) {
						try {
							
							String accion = line.substring(0, 1);
							String barcode = line.substring(13, 25);
							String itemCode = line.substring(1, 13);
							String priority = line.substring(25, 26);
							Item item = getItemByCode(itemCode);
							if (ACTION_DELETE.equalsIgnoreCase(accion)){
								POSIdentity posIdentityDelete = getPosIdentity(item.getItemID(), new Long(barcode), retailStore.getRetailStoreID());
								sesion.delete(posIdentityDelete);
								//deleteBarcodeForItem(item.getItemID(), new Long(barcode));
							}
							else{
								if (item != null && item.getItemID() != null) {
									POSIdentity posIdentity = getPosIdentity(item.getItemID(), new Long(barcode), retailStore.getRetailStoreID());
									posIdentity.setPriority(priority);
									sesion.saveOrUpdate(posIdentity);
									barcode = posIdentity.getPosIdentityID().toString();
									if(barcode.length() <= 6 && itemsBalanza != null && itemsBalanza.containsKey(item.getItemID())){
										initTxFleje();
										ItemBalanza itemBalanza = (ItemBalanza) itemsBalanza.get(item.getItemID());
										while(barcode.length() < 6)
											barcode = "0" + barcode;
										itemBalanza.setArticulo(barcode);
										sessionFlejes.saveOrUpdate(itemBalanza);
										tx2.commit();
									}
								} else
									log.error("Item: " + itemCode + " no existe.");
							}
						} catch (Exception e) {
							log.error("Problem Line: " + line);
							log.error(e.getMessage(), e);
						}
					}
					cantMax--;
					cantProc++;
					line = reader.readLine();
				}
				tx.commit();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					tx.rollback();
					tx2.rollback();
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}
			log.info("Registros procesados: " + cantProc);
			sesion.clear();
			cantMax = 2000;
		}
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

	private void iniciarFlejesSesion() {
		while (sessionFlejes == null) {
			try {
				sessionFlejes = HibernateSessionFactoryContainer.getSessionFactory("Flejes").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionFlejes == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	private void iniciarSaAdminSesion() {
		while (sessionSaAdmin == null) {
			try {
				sessionSaAdmin = HibernateSessionFactoryContainer.getSessionFactory("Saadmin").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (sessionSaAdmin == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	public void initTx() {
		while (tx == null || !tx.isActive()) {
			try {
				tx = sesion.beginTransaction();
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

	public void initTxFleje() {
		while (tx2 == null || !tx2.isActive()) {
			try {
				tx2 = sessionFlejes.beginTransaction();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				tx2 = null;
			}
			if (tx2 == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA TRANSACCION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	private Item getItemByCode(String itemCod) {
		Long itemCode = new Long(itemCod);
		Query query = sesion.createQuery("from com.allc.entities.Item where itemCode = '" + itemCode + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (Item) list.get(0);
		Item item = new Item();
		item.setItemCode(itemCode);
		return item;
	}

	private POSDepartment getPOSDepartmentByCode(String posDepartmentID) {

		Query query = sesion.createQuery(
				"from com.allc.entities.POSDepartment where id = " + Integer.valueOf(posDepartmentID) + " ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (POSDepartment) list.get(0);

		return null;
	}

	private MerchandiseHierarchyGroup getMerchandiseHierarchyGroupByCode(String merchandiseHierarchyGroupCode,
			POSDepartment posDepartment) {

		if (posDepartment != null && posDepartment.getId() != null) {
			Query query = sesion.createQuery("from com.allc.entities.MerchandiseHierarchyGroup where code = "
					+ Integer.valueOf(merchandiseHierarchyGroupCode) + " and posDepartment.id = "
					+ posDepartment.getId());
			List list = query.list();
			if (list != null && !list.isEmpty())
				return (MerchandiseHierarchyGroup) list.get(0);
		}

		return null;
	}

	private RetailStore getRetailStoreByCode(String retailStoreCode) {
		Integer code = new Integer(retailStoreCode);
		Query query = sesion.createQuery("from com.allc.entities.RetailStore where code = '" + code + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (RetailStore) list.get(0);
		RetailStore retailStore = new RetailStore();
		retailStore.setCode(code);
		return retailStore;
	}

	private ItemStore getItemStore(Integer itemId, Integer retailStoreId) {
		if (itemId != null && retailStoreId != null) {
			Query query = sesion.createQuery("from com.allc.entities.ItemStore where itemID = '" + itemId
					+ "' and retailStoreID = '" + retailStoreId + "'");
			List list = query.list();
			if (list != null && !list.isEmpty())
				return (ItemStore) list.get(0);
		}
		ItemStore itemStore = new ItemStore();
		itemStore.setRetailStoreID(retailStoreId);
		return itemStore;
	}

	private ItemBalanza getItemBalanza(Integer itemId, Integer retailStoreId) {
		if (itemId != null && retailStoreId != null) {
			Query query = sessionFlejes.createQuery("from com.allc.entities.ItemBalanza where id_itm = '" + itemId
					+ "' and des_clave = '" + retailStoreId + "'");
			List list = query.list();
			if (list != null && !list.isEmpty())
				return (ItemBalanza) list.get(0);
		}
		ItemBalanza itemBalanza = new ItemBalanza();
		itemBalanza.setId_itm(itemId);
		itemBalanza.setDes_clave(retailStoreId);
		return itemBalanza;
	}

	private POSIdentity getPosIdentity(Integer itemID, Long barcode, Integer retailStoreID) {
		Query query = sesion.createQuery("from com.allc.entities.POSIdentity where posIdentityID = '" + barcode
				+ "' and itemID = '" + itemID + "' and retailStoreID = " + retailStoreID);
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (POSIdentity) list.get(0);
		POSIdentity posIdentity = new POSIdentity();
		posIdentity.setPosIdentityID(barcode);
		posIdentity.setItemID(itemID);
		posIdentity.setRetailStoreID(retailStoreID);
		return posIdentity;
	}

	public String getDescNegocio(String codNeg) {
		try {
			SQLQuery query = sessionSaAdmin
					.createSQLQuery("select m.DES_NEGOCIO from MN_NEGOCIO m where COD_NEGOCIO = '" + codNeg + "'");
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return rows.get(0).toString();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo UpdateItemOperation...");
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
		log.info("Finalizo la Operacion de Actualizacion de Maestro de Items.");
		return true;
	}

	protected boolean sendFrameToCentral(Frame frame, PropFile properties) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected())
				connectClient(properties);
			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
			log.info("Trama a enviar: " + trama);
			if (socketClient.writeDataSocket(trama)) {
				int numberOfBytes = 0;
				int timeOutCycles = 0;
				while (numberOfBytes == 0) {
					numberOfBytes = socketClient.readLengthDataSocket();
					if (timeOutCycles == 5) {
						// cada 5 timeouts escribimos una trama vacía para asegurarnos que el socket esté activo
						String mje = Util.addLengthStartOfString("", qtyBytesLength).toString();
						if(!socketClient.writeDataSocket(mje)){
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

	protected boolean connectClient(PropFile properties) {

		storeIP = storeDAO.getStoreByCode(sessionSaAdmin, 0).getIp();

		if (socketClient == null) {
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
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean deleteBarcodeForItem(Integer idItem, Long barcode) {
		//Transaction txAux = null;
		log.info("Barcode: " + barcode + " IDItem: " + idItem);
		//try {
		//	tx = sesion.beginTransaction();
			Query query = sesion
					.createSQLQuery("DELETE FROM ID_PS WHERE ID_ITM_PS = " + barcode + " and ID_ITM = " + idItem);
			query.executeUpdate();
//			tx.commit();
//		} catch (Exception e) {
//			log.error(e.getMessage(), e);
//			txAux.rollback();
//			return false;
//		}
		return true;
	}

}
