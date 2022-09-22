/**
 * 
 */
package com.allc.arms.agent.processes.cer.itemUpdate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileFilter4690;
import com.ibm.OS4690.FileInputStream4690;

/**
 * @author gustavo
 *
 */
public class UpdateItemProcess extends AbstractProcess {
	protected Logger log = Logger.getLogger(UpdateItemProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	private Iterator filesToProcess = null;
	private File4690 inFolder;
	private ItemPriceKeyed itemPriceKeyedFile;
	private int itemPriceKeyLength;
	private int itemPriceRecordLength;
	private ItemBarcodeKeyed itemBarcodeKeyedFile;
	private String tipoImpuesto;
	private int sleepTime;
	private String store;
	private static Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
	private boolean isEnd = false;
	protected boolean finished = false;
	protected ConnSocketClient socketClient;

	protected void init() {
		try {
			inFolder = new File4690(properties.getObject("updateItem.in.folder.path"));
			inFolder.mkdir();
			itemPriceKeyedFile = new ItemPriceKeyed();
			itemPriceKeyLength = properties.getInt("updateItem.itemPrice.key.length");
			itemPriceRecordLength = properties.getInt("updateItem.itemPrice.record.length");
			if (itemPriceKeyLength == 6)
				if(itemPriceRecordLength == 169)
					itemPriceKeyedFile.initACE(properties.getObject("updateItem.itemPrice.file"));
				else
					itemPriceKeyedFile.initSuperMarket(properties.getObject("updateItem.itemPrice.file"));
			else
				itemPriceKeyedFile.init(properties.getObject("updateItem.itemPrice.file"));
			itemBarcodeKeyedFile = new ItemBarcodeKeyed();
			itemBarcodeKeyedFile.init(properties.getObject("updateItem.itemBarcode.file"));
			sleepTime = properties.getInt("updateItem.sleepTime");
			tipoImpuesto = properties.getObject("updateItem.tipoImpuesto");

			ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
			store = (new Integer(controllerStatusData.getStoreNumber())).toString();
			while(store.length() < 3)
				store = "0" + store;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private String getEyesFileName(){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	public void run() {
		log.info("Iniciando UpdateItemProcess...");
		init();
		String loteName = null;
		while (!isEnd) {
			try {
				File4690 loteFile = getNextItemFile();
				loteName = loteFile.getName();
				Files.creaEscribeDataArchivo4690(getEyesFileName(), "UPD_ITEM_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Iniciando procesamiento de: "+loteName+".\n", true);
				File4690 peanFile = null;
				if (loteFile != null) {
					log.info("Archivo a procesar: " + loteFile.getName().toUpperCase());
					boolean procesado = false;
					if (loteFile.getName().toUpperCase().startsWith("LO")) {
						procesado = updateItemPriceFile(loteFile);
					}
					if (procesado) {
						String filename = loteFile.getName().toUpperCase();
						String secondPart = filename.substring(2);
						peanFile = new File4690(inFolder, "PE" + secondPart);
						if (peanFile.exists()) {
							log.info("Archivo a procesar: " + peanFile.getName().toUpperCase());
							procesado = updateItemBarcodeFile(peanFile);
						} else {
							log.error("El archivo "+peanFile.getName().toUpperCase()+" no existe.");
						}
					}
					if (procesado) {
						StringBuffer data = new StringBuffer();
						data.append("S").append(ArmsAgentConstants.Communication.FRAME_SEP)
								.append(ArmsAgentConstants.Process.UPDATE_ITEM_DATA_PROCESS)
								.append(ArmsAgentConstants.Communication.FRAME_SEP).append("000")
								.append(ArmsAgentConstants.Communication.FRAME_SEP).append(store)
								.append(ArmsAgentConstants.Communication.FRAME_SEP).append(ArmsAgentConstants.Communication.TEMP_CONN)
								.append(ArmsAgentConstants.Communication.FRAME_SEP)
								.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
								.append(ArmsAgentConstants.Communication.FRAME_SEP).append(loteFile.getName());
						if (peanFile != null && peanFile.exists())
							data.append(ArmsAgentConstants.Communication.FRAME_SEP).append(peanFile.getName());
						List list = Arrays.asList(p.split(data.toString()));

						Frame frame = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
								ArmsAgentConstants.Communication.FRAME_SEP);
						if (frame.loadData()) {
							boolean send = sendItemFiles(frame);
							closeClient();
							if (send) {
								loteFile.delete();
								if(peanFile !=null && peanFile.exists())
									peanFile.delete();
								log.info("Archivo procesado correctamente.");
							} else {
								log.error("Error al informar al server.");
							}
						}
					} else {
						Files.creaEscribeDataArchivo4690(getEyesFileName(), "UPD_ITEM_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|El proceso del archivo: "+loteName+" no finalizó correctamente.\n", true);
						log.error("Error al procesar el archivo.");
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							log.error(e.getMessage(), e);
						}
					}
				}
				Files.creaEscribeDataArchivo4690(getEyesFileName(), "UPD_ITEM_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Finalizó el proceso del archivo: "+loteName+".\n", true);
			} catch (Exception e) {
				try {
					Files.creaEscribeDataArchivo4690(getEyesFileName(), "UPD_ITEM_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el archivo: "+loteName+".\n", true);
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
				log.error(e.getMessage(), e);
			}
		}
		finished = true;
	}

	protected boolean connectClient() {
		if (socketClient == null) {
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(properties.getObject("clientSocket.ip"));
			socketClient.setPortServer(properties.getInt("clientSocket.port"));
			socketClient.setRetries(properties.getInt("clientSocket.retries"));
			socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}

	protected void closeClient() {
		if (socketClient != null)
			socketClient.closeConnection();
	}

	protected boolean sendItemFiles(Frame frame) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected())
				connectClient();
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
						frameRpta = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
								ArmsAgentConstants.Communication.FRAME_SEP);
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

	private boolean updateItemPriceFile(File4690 inFile) {
		int i = 0;
		while (i < 3 && !isEnd) {
			int cantMax = 2000;
			int cantProc = 0;
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream4690(inFile)));
				String line = null;
				while ((line = reader.readLine()) != null && !isEnd) {
					if (cantMax == 0) {
						log.info("Registros procesados: " + cantProc);
						Files.creaEscribeDataArchivo4690(getEyesFileName(), "UPD_ITEM_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|PRC|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Procesando archivo: "+inFile.getName()+". Registros procesados: "+cantProc+".\n", true);
						cantMax = 2000;
					}
					cantProc++;
					cantMax--;
					if (itemPriceKeyLength == 6)
						if(itemPriceRecordLength == 169)
							itemPriceKeyedFile.writeItemACE(line, tipoImpuesto);
						else
							itemPriceKeyedFile.writeItemSuperMarket(line, tipoImpuesto);
					else
						itemPriceKeyedFile.writeItem(line, tipoImpuesto);
				}

				reader.close();
				if (!isEnd)
					log.info("Archivo procesado correctamente.");
				return true;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException ie) {
					log.error(ie.getMessage(), ie);
				}
			}
			i++;
		}
		return false;
	}

	private boolean updateItemBarcodeFile(File4690 inFile) {
		int i = 0;
		while (i < 3 && !isEnd) {
			int cantMax = 2000;
			int cantProc = 0;
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream4690(inFile)));
				String line = null;
				while ((line = reader.readLine()) != null && !isEnd) {
					if (cantMax == 0) {
						log.info("Registros procesados: " + cantProc);
						Files.creaEscribeDataArchivo4690(getEyesFileName(), "UPD_ITEM_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+store+"|PRC|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Procesando archivo(EAN): "+inFile.getName()+". Registros procesados: "+cantProc+".\n", true);
						cantMax = 2000;
					}
					cantProc++;
					cantMax--;
					itemBarcodeKeyedFile.writeItem(line);
				}

				reader.close();
				if (!isEnd)
					log.info("Archivo procesado correctamente.");
				return true;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException ie) {
					log.error(ie.getMessage(), ie);
				}
			}
			i++;
		}
		return false;
	}

	private File4690 getNextItemFile() {
		log.info("Buscando archivos de actualizacion de item para store: " + store);
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				File4690[] files = inFolder.listFiles(new FileFilter4690() {
					public boolean accept(File4690 pathname) {
						log.info("Pathname: " + pathname.getName().toUpperCase());
						return pathname.isFile() && pathname.getName().toUpperCase().startsWith("LO")
								&& pathname.getName().endsWith("." + store);
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
							String name1 = ((File4690) obj1).getName().toUpperCase();
							int sequence1 = 0;
							String name2 = ((File4690) obj2).getName().toUpperCase();
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
		return (File4690) this.filesToProcess.next();
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
		log.info("Finalizó el Proceso de Actualización de Maestro de Ítems.");
		return true;
	}

}
