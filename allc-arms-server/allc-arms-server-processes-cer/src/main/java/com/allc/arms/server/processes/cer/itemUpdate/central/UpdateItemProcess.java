/**
 * 
 */
package com.allc.arms.server.processes.cer.itemUpdate.central;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.ArrayList;
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
import org.hibernate.Transaction;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.fleje.Fleje;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.core.process.AbstractProcess;
import com.allc.entities.Item;
import com.allc.entities.ItemStore;
import com.allc.entities.MerchandiseHierarchyGroup;
import com.allc.entities.POSDepartment;
import com.allc.entities.POSIdentity;
import com.allc.entities.RetailStore;
import com.allc.files.helper.FilesHelper;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

/**
 * @author gustavo
 *
 */
public class UpdateItemProcess extends AbstractProcess{
	public static final String ACTION_ADD = "A";
	public static final String ACTION_DELETE = "D";
	public static final String ACTION_UPDATE = "U";
	private static Logger log = Logger.getLogger(UpdateItemProcess.class);
	private File inFolder;
	private File bkpFolder;
	private int sleepTime;
	private Iterator filesToProcess = null;
	private Session sesion;
	private Transaction tx;
	BufferedReader reader = null;
	public boolean isEnd = false;
	protected boolean finished = false;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);
	protected Map stringToInt;

	protected void inicializar() {
		isEnd = false;
		try {
			inFolder = new File(properties.getObject("searchItem.in.folder.path"));
			bkpFolder = new File(properties.getObject("searchItem.bkp.folder.path"));
			sleepTime = properties.getInt("searchItem.sleeptime");
			loadHashCharToInt();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void run() {
		log.info("Iniciando UpdateItemProcess...");
		inicializar();
		String filename = null;
		String storeCode = properties.getObject("eyes.store.code");
		while (!isEnd) {
			try {
				File loteFile = getNextItemFile();

				if (loteFile != null) {
					filename = loteFile.getName().toUpperCase();
					String[] parts = filename.split("\\.");
					Integer store = new Integer(parts[1]);
					String sequence = parts[0].substring(2, 6);
					UtilityFile.createWriteDataFile(getEyesFileName(),
							"UPD_ITEM_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode + "|STR|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|Archivo a Procesar: "
									+ filename + ".\n", true);
					log.info("Archivo a procesar: " + filename + " STORE: " + store);
					
					reader = new BufferedReader(new FileReader(loteFile));
					if (filename.toUpperCase().startsWith("LO")) {
						openSession();
						updateItemPrice(filename);
					}
//					if (!isEnd) {
//						try {
//							reader.close();
//						} catch (Exception e1) {
//							log.error(e1.getMessage(), e1);
//						}
//						File file4690 = new File(folder4690, fileToProcess.getName());
//						file4690.delete();
//						if (fileToProcess.renameTo(new File(outFolder, fileToProcess.getName())))
//							log.info("Archivo movido a: " + outFolder);
//						else
//							log.error("No se pudo mover el archivo.");
//						log.info("Archivo procesado correctamente.");
//						if (eanfile != null) {
//							log.info("Archivo a procesar: " + eanfile);
//							fileToProcess = new File(properties.getObject("updateItem.bd.in.folder.path"), eanfile);
//							reader = new BufferedReader(new FileReader(fileToProcess));
//							if (eanfile.toUpperCase().startsWith("PE")) {
//								updateItemBarcode();
//							}
//							if (!isEnd) {
//								try {
//									reader.close();
//								} catch (Exception e1) {
//									log.error(e1.getMessage(), e1);
//								}
//								file4690 = new File(folder4690, fileToProcess.getName());
//								file4690.delete();
//								if(properties.getObject("updateItem.bd.central.folder.path") != null && !properties.getObject("updateItem.bd.central.folder.path").isEmpty()){
//									File fileCentral = new File(properties.getObject("updateItem.bd.central.folder.path")+File.separator+storeCode+File.separator+properties.getObject("updateItem.bd.central.folder.name"));
//									FilesHelper.copyFile(properties.getObject("updateItem.bd.in.folder.path"), fileCentral.getPath(), eanfile, eanfile);
//									log.info("Archivo copiado a: " + fileCentral.getPath());
//								}
//								if (fileToProcess.renameTo(new File(outFolder, fileToProcess.getName())))
//									log.info("Archivo movido a: " + outFolder);
//								else
//									log.error("No se pudo mover el archivo.");
//								log.info("Archivo procesado correctamente.");
//							}
//						}
//						//copiamos al final el LO para evitar que se procese en central antes de que se copie el PE
//						if(properties.getObject("updateItem.bd.central.folder.path") != null && !properties.getObject("updateItem.bd.central.folder.path").isEmpty()){
//							File fileCentral = new File(properties.getObject("updateItem.bd.central.folder.path")+File.separator+storeCode+File.separator+properties.getObject("updateItem.bd.central.folder.name"));
//							FilesHelper.copyFile(properties.getObject("updateItem.bd.in.folder.path"), fileCentral.getPath(), itemfile, itemfile);
//							log.info("Archivo copiado a: " + fileCentral.getPath());
//						}
//						if (!isEnd) {
//							Fleje fleje = flejesDAO.getFleje(sessionFlejes, itemfile.toUpperCase());
//							if(fleje != null){
//								fleje.setStatus(8);
//								flejesDAO.insertaFleje(sessionFlejes, fleje);
//								Integer status = flejesDAO.getMinStatusPorArcsap(sessionFlejes, fleje.getArchivo().getId());
//								if(status != null && status.compareTo(fleje.getArchivo().getStatus()) != 0){
//									fleje.getArchivo().setStatus(status);
//									flejesDAO.insertaArchivo(sessionFlejes, fleje.getArchivo());
//								}
//							}
//							msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("0");
//							String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
//							log.info(tmp);
//							if(socket.writeDataSocket(tmp)){
//								UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_ITEM_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo: "+frame.getBody().get(ArmsServerConstants.Body.UpdateItem.ITEM_NAME)+" procesado.\n", true);
//							} else
//								UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_ITEM_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
//						}
//					}
					FilesHelper.copyFile(inFolder.getPath(), bkpFolder.getPath(), filename, filename);
					loteFile.delete();
					File eanFile = new File(inFolder, "EAN" + sequence + "." + parts[1]);
					if (eanFile.exists()) {
						String eanName = eanFile.getName().toUpperCase();
						FilesHelper.copyFile(inFolder.getPath(), bkpFolder.getPath(), eanName, eanName);
						eanFile.delete();
						UtilityFile.createWriteDataFile(getEyesFileName(),
								"UPD_ITEM_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode + "|END|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date()) + "|El archivo: "
										+ filename + " se proces?? correctamente.\n", true);
					} else {
						UtilityFile.createWriteDataFile(getEyesFileName(),
								"UPD_ITEM_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode + "|WAR|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|El archivo EAN : " + eanFile.getName().toUpperCase() + " no existe.\n", true);
						log.info("Archivo EAN no encontrado.");
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					UtilityFile.createWriteDataFile(getEyesFileName(),
							"UPD_ITEM_P|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + storeCode + "|ERR|"
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
							if (ACTION_DELETE.equalsIgnoreCase(line.substring(0, 1))) {
								sesion.delete(itemStore);
							} else {
								item.setName(line.substring(13, 31));
								String desc = line.substring(86, 126);
								item.setDescription(desc);
								item.setFlWorM(line.charAt(49) == 'Y' ? 1 : 0);
								item.setFlPriceReq(line.charAt(50) == 'Y' ? 1 : 0);
								item.setFlQuantityAllw(line.charAt(55) == 'Y' ? 1 : 0);
								item.setFlQuantityReq(line.charAt(56) == 'Y' ? 1 : 0);
								String codSAP = line.substring(68, 86).trim();
								item.setCodigoSAP(new Long(codSAP != null && !codSAP.isEmpty() ? codSAP : "0"));
								POSDepartment posDepartment = getPOSDepartmentByCode(line.substring(31, 35));
								MerchandiseHierarchyGroup mhg = getMerchandiseHierarchyGroupByCode(line.substring(35, 38),
										posDepartment);
								item.setMerchandiseHierarchyGroup(mhg);
								itemStore.setSalesPrice(new Double(line.substring(38, 46)));
								itemStore.setFlAuthorizedForSale(line.charAt(57) == 'Y' ? 1 : 0);
								itemStore.setTaxA(line.charAt(64) == 'Y' ? 1 : 0);
								itemStore.setTaxB(line.charAt(65) == 'Y' ? 1 : 0);
								itemStore.setTaxC(line.charAt(66) == 'Y' ? 1 : 0);
								itemStore.setTaxD(line.charAt(67) == 'Y' ? 1 : 0);
								itemStore.setTaxE(new Integer("0"));
								itemStore.setTaxF(new Integer("0"));
								itemStore.setTaxG(new Integer("0"));
								itemStore.setTaxH(new Integer("0"));
								itemStore.setSpecialFamily(new Integer(line.substring(248, 250)));
								if (item.getItemsStore() == null)
									item.setItemsStore(new ArrayList());
								item.getItemsStore().add(itemStore);
								sesion.saveOrUpdate(item);
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
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}
			log.info("Registros procesados: " + cantProc);
			sesion.clear();
			cantMax = 2000;
		}
	}

	private void updateItemBarcode() throws Exception {
		int cantMax = 2000;
		int cantProc = 0;
		String line = "";
		while (line != null && !isEnd) {
			try {
				initTx();
				while (line != null && cantMax > 0 && !isEnd) {
					if (!line.trim().isEmpty()) {
						try {
							String barcode = line.substring(12, 24);
							String itemCode = line.substring(0, 12);
							Item item = getItemByCode(itemCode);
							if (item != null && item.getItemID() != null) {
								POSIdentity posIdentity = getPosIdentity(item.getItemID(), new Long(barcode));
								sesion.saveOrUpdate(posIdentity);
							} else
								log.error("Item: " + itemCode + " no existe.");
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

	private Integer translateToInt(String code) {
		StringBuffer newCode = new StringBuffer();
		for (int i = 0; i < code.length(); i++) {
			String key = code.substring(i, i + 1);
			if (stringToInt.containsKey(key))
				newCode.append((String) stringToInt.get(key));
			else
				newCode.append(key);
		}
		return new Integer(newCode.toString());
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
					log.error("OCURRI?? UN ERROR AL CREAR LA SESI??N A LA BD, SE REINTENTAR?? EN 3 seg.");
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
					log.error("OCURRI?? UN ERROR AL CREAR LA TRANSACCI??N A LA BD, SE REINTENTAR?? EN 3 seg.");
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

	private POSDepartment getPOSDepartmentByCode(String posDepartmentCode) {
	
		Query query = sesion.createQuery("from com.allc.entities.POSDepartment where codDptoCer = '" + posDepartmentCode + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (POSDepartment) list.get(0);
		POSDepartment posDepartment = new POSDepartment();
		posDepartment.setCodDptoCer(posDepartmentCode);
		posDepartment.setName("Departamento " + posDepartmentCode);
		return posDepartment;
	}

	private MerchandiseHierarchyGroup getMerchandiseHierarchyGroupByCode(String merchandiseHierarchyGroupCode, POSDepartment posDepartment) {
		
		if (posDepartment != null && posDepartment.getId() != null) {
			Query query = sesion.createQuery("from com.allc.entities.MerchandiseHierarchyGroup where codMRHCer = '"
					+ merchandiseHierarchyGroupCode + "' and posDepartment.id = " + posDepartment.getId());
			List list = query.list();
			if (list != null && !list.isEmpty())
				return (MerchandiseHierarchyGroup) list.get(0);
		}
		MerchandiseHierarchyGroup merchandiseHierarchyGroup = new MerchandiseHierarchyGroup();
		merchandiseHierarchyGroup.setCodMRHCer(merchandiseHierarchyGroupCode);
		merchandiseHierarchyGroup.setName("Familia " + merchandiseHierarchyGroupCode);
		merchandiseHierarchyGroup.setDescription("Familia " + merchandiseHierarchyGroupCode);
		merchandiseHierarchyGroup.setPosDepartment(posDepartment);
		return merchandiseHierarchyGroup;
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
			Query query = sesion.createQuery("from com.allc.entities.ItemStore where itemID = '" + itemId + "' and retailStoreID = '"
					+ retailStoreId + "'");
			List list = query.list();
			if (list != null && !list.isEmpty())
				return (ItemStore) list.get(0);
		}
		ItemStore itemStore = new ItemStore();
		itemStore.setRetailStoreID(retailStoreId);
		return itemStore;
	}

	private POSIdentity getPosIdentity(Integer itemID, Long barcode) {
		Query query = sesion.createQuery("from com.allc.entities.POSIdentity where posIdentityID = '" + barcode + "' and itemID = '"
				+ itemID + "'");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (POSIdentity) list.get(0);
		POSIdentity posIdentity = new POSIdentity();
		posIdentity.setPosIdentityID(barcode);
		posIdentity.setItemID(itemID);
		return posIdentity;
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

	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo UpdateItemProcess...");
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
		log.info("Finaliz?? el Proceso de Actualizaci??n de ??tems.");
		return true;
	}

}
