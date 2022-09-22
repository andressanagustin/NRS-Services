package com.allc.arms.server.operations.cer.itemUpdate.central;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.fleje.Fleje;
import com.allc.arms.server.persistence.fleje.FlejesDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
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

public class UpdateItemOperation extends AbstractOperation {
	public static final String ACTION_ADD = "A";
	public static final String ACTION_DELETE = "B";
	public static final String ACTION_UPDATE = "M";
	private Session sesion;
	private Session sessionFlejes = null;
	private Transaction tx;
	BufferedReader reader = null;
	public boolean isEnd = false;
	protected boolean finished = false;
	static Logger log = Logger.getLogger(UpdateItemOperation.class);
	protected Map stringToInt;

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		StringBuilder msg = new StringBuilder();
		FlejesDAO flejesDAO = new FlejesDAO();
		loadHashCharToInt();
		try {
			String itemfile = (String) frame.getBody().get(ArmsServerConstants.Body.UpdateItem.ITEM_NAME);
			UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_ITEM_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Iniciando procesamiento de: "+itemfile+".\n", true);
			String eanfile = frame.getBody().size() > 1 ? (String) frame.getBody().get(ArmsServerConstants.Body.UpdateItem.EAN_NAME) : null;
			log.info("Archivo a procesar: " + itemfile);
			File outFolder = new File(properties.getObject("updateItem.bd.out.folder.path"));
			File fileToProcess = new File(properties.getObject("updateItem.bd.in.folder.path"), itemfile);
			String storeCode = itemfile.split("\\.")[1];
			String folder4690 = properties.getObject("updateItem.bd.4690.folder.path")+File.separator+storeCode+File.separator+properties.getObject("updateItem.bd.4690.folder.name");
			reader = new BufferedReader(new FileReader(fileToProcess));
			if (itemfile.toUpperCase().startsWith("LO")) {
				openSession();
				iniciarFlejesSesion();
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
						updateItemBarcode();
					}
					if (!isEnd) {
						try {
							reader.close();
						} catch (Exception e1) {
							log.error(e1.getMessage(), e1);
						}
						file4690 = new File(folder4690, fileToProcess.getName());
						file4690.delete();
						if(properties.getObject("updateItem.bd.central.folder.path") != null && !properties.getObject("updateItem.bd.central.folder.path").isEmpty()){
							File fileCentral = new File(properties.getObject("updateItem.bd.central.folder.path")+File.separator+storeCode+File.separator+properties.getObject("updateItem.bd.central.folder.name"));
							FilesHelper.copyFile(properties.getObject("updateItem.bd.in.folder.path"), fileCentral.getPath(), eanfile, eanfile);
							log.info("Archivo copiado a: " + fileCentral.getPath());
						}
						if (fileToProcess.renameTo(new File(outFolder, fileToProcess.getName())))
							log.info("Archivo movido a: " + outFolder);
						else
							log.error("No se pudo mover el archivo.");
						log.info("Archivo procesado correctamente.");
					}
				}
				//copiamos al final el LO para evitar que se procese en central antes de que se copie el PE
				if(properties.getObject("updateItem.bd.central.folder.path") != null && !properties.getObject("updateItem.bd.central.folder.path").isEmpty()){
					File fileCentral = new File(properties.getObject("updateItem.bd.central.folder.path")+File.separator+storeCode+File.separator+properties.getObject("updateItem.bd.central.folder.name"));
					FilesHelper.copyFile(properties.getObject("updateItem.bd.in.folder.path"), fileCentral.getPath(), itemfile, itemfile);
					log.info("Archivo copiado a: " + fileCentral.getPath());
				}
				if (!isEnd) {
					Fleje fleje = flejesDAO.getFleje(sessionFlejes, itemfile.toUpperCase());
					if(fleje != null){
						fleje.setStatus(8);
						flejesDAO.insertaFleje(sessionFlejes, fleje);
						Integer status = flejesDAO.getMinStatusPorArcsap(sessionFlejes, fleje.getArchivo().getId());
						if(status != null && status.compareTo(fleje.getArchivo().getStatus()) != 0){
							fleje.getArchivo().setStatus(status);
							flejesDAO.insertaArchivo(sessionFlejes, fleje.getArchivo());
						}
					}
					msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("0");
					String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
					log.info(tmp);
					if(socket.writeDataSocket(tmp)){
						UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_ITEM_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo: "+frame.getBody().get(ArmsServerConstants.Body.UpdateItem.ITEM_NAME)+" procesado.\n", true);
					} else
						UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_ITEM_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("-1");
				String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
				log.info(tmp);
				socket.writeDataSocket(tmp);
			} catch (Exception ex) {
				log.error(e.getMessage(), e);
			}
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_ITEM_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el archivo: "+frame.getBody().get(ArmsServerConstants.Body.UpdateItem.ITEM_NAME)+".\n", true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
		finally {
			sessionFlejes.close();
			sesion.close();
			sesion = null;
			tx = null;
		}
		finished = true;
		return false;
	}
	
	private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
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
					log.error("OCURRIÓ UN ERROR AL CREAR LA SESIÓN A LA BD, SE REINTENTARÁ EN 3 seg.");
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
					log.error("OCURRIÓ UN ERROR AL CREAR LA CONEXIÓN A LA BD, SE REINTENTARÁ EN 3 seg.");
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
					log.error("OCURRIÓ UN ERROR AL CREAR LA TRANSACCIÓN A LA BD, SE REINTENTARÁ EN 3 seg.");
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
		log.info("Finalizó la Operación de Actualización de Maestro de Ítems.");
		return true;
	}

	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

}
