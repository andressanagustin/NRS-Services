/**
 * 
 */
package com.allc.arms.server.processes.cer.printer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.core.process.AbstractProcess;
import com.allc.printer.helper.AbstractPrinter;
import com.allc.printer.helper.PrintByEthernetPort;
import com.allc.printer.helper.PrintBySerialPort;
import com.allc.properties.PropFile;

/**
 * @author gustavo
 *
 */
public class SearchPrintFileProcess extends AbstractProcess {
	private static Logger log = Logger.getLogger(SearchPrintFileProcess.class);
	private File inFolder;
	private File outFolder;
	private int sleepTime;
	private Iterator filesToProcess = null;
	private String connectionType;
	private String serialPortName;
	private String ip;
	private int port;
	private int timeOut;
	private int timeOutSleep;
	private int length;
	public boolean isEnd = false;
	protected boolean finished = false;
	protected PropFile properties = PropFile.getInstance(ArmsServerConstants.PROP_FILE_NAME);

	protected void inicializar() {
		isEnd = false;
		try {
			inFolder = new File(properties.getObject("searchPrintFile.printer.in.folder.path"));
			outFolder = new File(properties.getObject("searchPrintFile.printer.out.folder.path"));
			sleepTime = properties.getInt("searchPrintFile.printer.sleeptime");
			serialPortName = properties.getObject("searchPrintFile.printer.serial.port");
//			ip = properties.getObject("searchPrintFile.printer.ethernet.ip");
			port = properties.getInt("searchPrintFile.printer.ethernet.port");
			timeOut = properties.getInt("searchPrintFile.printer.ethernet.timeout");
			timeOutSleep = properties.getInt("searchPrintFile.printer.ethernet.timeoutSleep");
			length = properties.getInt("searchPrintFile.printer.ethernet.length");
			connectionType = properties.getObject("searchPrintFile.printer.connectionType");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void run() {
		log.info("Iniciando SearchPrintFileProcess...");
		inicializar();
		String store = properties.getObject("eyes.store.code");
		while (!isEnd) {
			AbstractPrinter printer = null;
			String filename = null;
			try {
				File printFile = getNextPrintFile();

				if (printFile != null) {
					
					filename = printFile.getName().toUpperCase();
					UtilityFile.createWriteDataFile(getEyesFileName(), "SRCH_PRNT_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo a Imprimir: "+filename+".\n", true);
					log.info("Archivo a procesar: " + filename);
					FileReader fr = new FileReader(printFile);
					BufferedReader br = new BufferedReader(fr);
					ip = br.readLine().trim();
					log.info("IP de impresora: " + ip);
					
					try {
						if (connectionType.equalsIgnoreCase("serial")) {
							printer = new PrintBySerialPort(serialPortName);
							((PrintBySerialPort) printer).openSerialPort();
						} else {
							printer = new PrintByEthernetPort(ip, port, timeOut, timeOutSleep, length);
							((PrintByEthernetPort) printer).openSocket();
						}
					} catch (Exception e) {
						log.error(e.getMessage(),e);
					}
					log.info("Conexión con la impresora iniciada.");
					
					String linea = br.readLine();
					while (linea != null) {
						log.debug("Línea leida: " + linea);
						if(filename.startsWith("SUP"))
							printSup(linea, printer);
						else
							printFlejes(linea, printer);
						linea = br.readLine();
					}
					br.close();
					fr.close();

					UtilityFile.createWriteDataFile(getEyesFileName(), "SRCH_PRNT_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Impresión del Archivo: "+filename+" finalizada.\n", true);
					log.info("Impresión del archivo " + filename + " exitosa.");
					moveFile(printFile.getName());
					log.info("Archivo movido a la carpeta Out.");
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				try {
					UtilityFile.createWriteDataFile(getEyesFileName(), "SRCH_PRNT_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al imprimir el archivo: "+filename+".\n", true);
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
			}
			finally {
				if(printer != null){
					if (connectionType.equalsIgnoreCase("serial")) {
						((PrintBySerialPort) printer).closeSerialPort();
					} else {
						((PrintByEthernetPort) printer).closeSocket();
					}
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
	
	private void printFlejes(String linea, AbstractPrinter printer){
		String codArt = linea.substring(0, 12);
		String codEan = linea.substring(12, 30);
		String descrip = linea.substring(30, 75);//48);
		String tamano = linea.substring(75, 95);//48, 68);
		String marca = linea.substring(95, 115);//68, 88);
		String ref = linea.substring(115, 135);//88, 108);
		String indImp = linea.substring(135, 136);//108, 109);
		String proveed = linea.substring(136, 156);//109, 129);
		String unidXcaja = linea.substring(156, 164);//129, 137);
		String preUni = linea.substring(164, 174);//137, 147);
		preUni = preUni.replaceAll("\\,", "");
		double precioUnit = Double.valueOf(preUni).doubleValue();
		int cantEtiq = Integer.valueOf(linea.substring(174, 182)).intValue();//147, 155)).intValue();
		char tipoEtiq = linea.charAt(182);//155);
		double porcRec = Double.valueOf(linea.substring(183, 189)).doubleValue();//156, 162)).doubleValue();
		double porcIva = Double.valueOf(linea.substring(189, 195)).doubleValue();//162, 168)).doubleValue();
		String modBarra = linea.substring(195, 201);//168, 174);
		boolean indNoAfil = linea.substring(201, 202).equals("1") ? true : false;//174, 175).equals("1") ? true : false;
		printer.print(codArt, codEan, descrip, tamano, marca, ref, indImp, proveed, unidXcaja, precioUnit, cantEtiq,
				tipoEtiq, porcRec, porcIva, modBarra, indNoAfil);
	}
	
	private void printSup(String linea, AbstractPrinter printer){
		String codEan = linea.substring(0, 19);
		String nombre = linea.substring(19);
		printer.printSup(codEan, nombre);
	}
	
	private String getEyesFileName(){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	private void moveFile(String filename) {
		String newFilename = filename;
		String[] parts = filename.split("\\.");
		String name = parts[0];
		String extension = parts[1];
		File outFile = new File(outFolder, newFilename);
		int seq = 1;
		if (outFile.exists()) {
			boolean finish = false;
			while (!finish) {
				newFilename = name + "_" + seq + "." + extension;
				outFile = new File(outFolder, newFilename);
				if (outFile.exists())
					seq++;
				else
					finish = true;
			}
		}
		File inFile = new File(inFolder, filename);
		if (inFile.exists())
			inFile.renameTo(new File(outFolder, newFilename));
	}

	private File getNextPrintFile() {
		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd)
					return null;
				File[] files = inFolder.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isFile();
					}
				});
				if (files.length == 0) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						log.error(e.getMessage(), e);
					}
				} else {
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
		log.info("Deteniendo SearchPrintFileProcess...");
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
		log.info("Finalizó el Proceso de Búsqueda de Archivos para imprimir.");
		return true;
	}

}
