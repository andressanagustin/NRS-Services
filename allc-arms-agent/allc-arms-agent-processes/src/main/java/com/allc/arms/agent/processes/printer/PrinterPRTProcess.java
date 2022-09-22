package com.allc.arms.agent.processes.printer;

//com.allc.arms.agent.processes.printer.PrinterPRTProcess
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.PrintModeStyle;
import com.github.anastaciocintra.escpos.PrintModeStyle.FontName;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileFilter4690;

public class PrinterPRTProcess extends AbstractProcess {

	private boolean isEnd = false;
	protected boolean finished = false;
	protected static Logger log;
	protected static String ipImpresora;
	protected static int puertoImpresora;
	protected String[] extensions;
	protected String searchFolder;
	protected long sleepTime;
	protected Iterator filesToProcess = null;
	protected File4690 inFolder;
	protected static File4690 seek;
	protected static PrintModeStyle normal, title, subtitle, bold, centrado;
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);

	public void init() {
		log = Logger.getLogger(PrinterPRTProcess.class);
		log.info("Inicia el proceso de impresion de tickets PRT.");
		ipImpresora = properties.getObject("aef.printer.ip"); // "172.20.105.6";
		puertoImpresora = Integer.parseInt(properties.getObject("aef.printer.port")); // 9100;
		extensions = new String[] { "PRT" };
		sleepTime = 5000;
		inFolder = new File4690("F:/ALLC/AEF");
		seek = new File4690("prtseek.dat");

		// estilos
		title = new PrintModeStyle().setFontName(FontName.Font_B).setFontSize(true, true)
				.setJustification(EscPosConst.Justification.Center);
		subtitle = new PrintModeStyle().setFontName(FontName.Font_B).setUnderline(true);
		bold = new PrintModeStyle().setFontName(FontName.Font_B).setBold(true);
		centrado = new PrintModeStyle().setFontName(FontName.Font_B).setJustification(EscPosConst.Justification.Center);
		normal = new PrintModeStyle().setFontName(FontName.Font_B); 
		// Fin estilos
	}

	public void run() {

		init();

		printFile();
		isEnd = true;
	}

	protected void printFile() {
		while (!isEnd) {
			String filename = "";
			long fileSize;
			try {
				File4690 fileToSend = getNextFile();

				if (fileToSend != null) {
					filename = fileToSend.getAbsolutePath();
					fileSize = fileToSend.length();
					String extensionArchivo = filename.substring(filename.length() - 3, filename.length());
					if (Arrays.asList(extensions).contains(extensionArchivo)) {
//						log.info("Archivo a imprimir: " + filename);
						leeArchivo(fileToSend);

					}
				} else {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						log.error(e.getMessage(), e);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

		}
	}

	protected File4690 getNextFile() {

		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd) {
					return null;
				}

//				log.info("Folder:" + inFolder);

				File4690[] files = inFolder.listFiles(new FileFilter4690() {
					public boolean accept(File4690 pathname) {
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
					Arrays.sort(files, new Comparator() {
						public int compare(Object obj1, Object obj2) {
							long lastMod1 = ((File4690) obj1).lastModified();
							long lastMod2 = ((File4690) obj2).lastModified();
							if (lastMod1 == lastMod2) {
								return 0;
							}
							if (lastMod1 < lastMod2) {
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

	protected static void leeArchivo(File4690 fileToPrint) throws Exception {

		String data = "";
		int lineaALeer = 1;
		String lineaPuntero = "";
		int prtPuntero = 0;
		String[] listaSeek = new String[99];
		int auxSeek = 99;
		int i = 2;
		try {

			if (!validarFecha(seek.getName())) {
				log.info("Fecha no fue la actual");
				Files.creaEscribeDataArchivo4690(seek.getName(), fechaActual(), false);
			}
			while (Files.readSpecifictLineOfFile4690(seek.getName(), i) != null) {
				lineaPuntero = Files.readSpecifictLineOfFile4690(seek.getName(), i);
				listaSeek[i - 2] = lineaPuntero;
//				log.info("Ruta Seek"+ seek.getAbsolutePath());
//				log.info("linaPuntero: " + lineaPuntero);
				int aux = lineaPuntero.indexOf(" ");
				String auxNombre = lineaPuntero.substring(0, aux);
				if (auxNombre.equalsIgnoreCase(fileToPrint.getName())) {
					prtPuntero = Integer.parseInt(lineaPuntero.substring(aux, lineaPuntero.length()).trim());
//					log.info("prtPuntero: " + prtPuntero);
					lineaALeer = prtPuntero + 1;
					auxSeek = i;

				}
				i++;

			}
//			log.info("Valor fuera de auxSeek: " + auxSeek);
			if (auxSeek == 99) {
				log.info("valor de i:" + i);
//				 No se encontró la clave valor, se debe anexar en el archivo seeek
				listaSeek[i - 2] = fileToPrint.getName() + " 0";
			}

			while (Files.readSpecifictLineOfFile4690(fileToPrint.getAbsolutePath(), lineaALeer) != null) {
				data += Files.readSpecifictLineOfFile4690(fileToPrint.getAbsolutePath(), lineaALeer) + "\r\n";
				lineaALeer++;
			}
//			log.info("auxseek: " + auxSeek + " dato reemplazo: " + fileToPrint.getName() + " " + (lineaALeer - 1));
			if (auxSeek == 99) {
				log.info("Seek es " + auxSeek);
				listaSeek[i - 2] = fileToPrint.getName() + " " + (lineaALeer - 1);
			} else {
//				log.info("Seek es "+auxSeek);
				listaSeek[auxSeek - 2] = fileToPrint.getName() + " " + (lineaALeer - 1);
			}

			Files.creaEscribeDataArchivo4690(seek.getName(), fechaActual(), false);

			int j = 0;
			while (listaSeek[j] != null) {
//				log.info("cambio linea " + j + "con: "+listaSeek[j]);
				Files.creaEscribeDataArchivo4690(seek.getName(), "\r\n" + listaSeek[j], true);
				j++;

			}

		} catch (Exception e) {
			while (Files.readSpecifictLineOfFile4690(fileToPrint.getAbsolutePath(), lineaALeer) != null) {
				data += Files.readSpecifictLineOfFile4690(fileToPrint.getAbsolutePath(), lineaALeer) + "\r\n";
				lineaALeer++;
			}
//			log.info("Error al leer archivo seek, creara archivo: " + seek.getAbsolutePath() + " con data: "+ fileToPrint.getName() + " " + (lineaALeer - 1));
			Files.creaEscribeDataArchivo4690(seek.getName(), fechaActual(), false);
			Files.creaEscribeDataArchivo4690(seek.getAbsolutePath(), fileToPrint.getName() + " " + (lineaALeer - 1),
					true);
		}

		if (data.length() > 0) {
			log.info("data a imprimir: " + data);
			boolean imprimio = imprimeData(data);
			if (!imprimio) {
				log.info("No pudo imprimir archivo, regresa puntero anterior");
				if (auxSeek == 99) {
					listaSeek[i - 2] = fileToPrint.getName() + " " + prtPuntero;
				} else {
					listaSeek[auxSeek - 2] = fileToPrint.getName() + " " + prtPuntero;
				}

				int j = 0;
				Files.creaEscribeDataArchivo4690(seek.getName(), fechaActual(), false);
				while (listaSeek[j] != null) {
//					log.info("cambio linea " +j);
					Files.creaEscribeDataArchivo4690(seek.getName(), "\r\n" + listaSeek[j], true);
					j++;

				}

			}
		}
	}

	protected static boolean imprimeData(String data) {
		boolean resultado = false;
		// IMPRIMER DATA
		Socket tcp, tcp1;
		OutputStream salida;

		try {
			Thread.sleep(1000);
			log.info("PRINTER IP: " + ipImpresora + " PRINTER PORT: " + puertoImpresora);
			tcp = new Socket(ipImpresora, puertoImpresora);
			salida = tcp.getOutputStream();
			Thread.sleep(2000);
			EscPos impresora = new EscPos(salida);
//			DATOS HEXADECIMAL
			char corchete = '[';//[\x1B]
			for (int i = 0; i < data.length(); i++) {
				if (data.charAt(i) == corchete) {
					data = data.replace(data.substring(i, i + 6), traductor(data.substring(i + 3, i + 5)));
				}

			}
			// FIN DATOS HEXADECIMAL

			String[] lineas = data.split("\r\n"); 
			log.info("Imprime sin caracteres +5 y "+lineas.length+ " lineas");
			for (int i = 0; i < lineas.length; i++) {
				if (lineas[i].trim().length() != 0) {
					lineas[i]= lineas[i].replace("\r", "");
					lineas[i]= lineas[i].replace("\n", "");
					log.info(i + ": " + lineas[i]); 
					impresora.write(normal,lineas[i].toString()+"\n");  
				}
			} // separador de lineas
//			impresora.cut(EscPos.CutMode.FULL);
			salida.close();
			impresora.close();
			tcp.close();
			resultado = true;
			log.info("No dio error al imprimir");
		} catch (Exception e) {
			log.info("Error al tratar de imprimir: " + e.toString());
		}

		// FIN IMPRIME DATA

		return resultado;
	}

	public static String traductor(String hexa) {

		StringBuilder builder = new StringBuilder();
		Integer n = Integer.valueOf(hexa, 16);
		int a = n.intValue();
		builder.append((char) a);
		return builder.toString();
	}

	public static boolean validarFecha(String fileName) {
		boolean validador = false;
		try {
//			log.info("Va a empezar el try de validar fecha");
			// debe leer primer registro del archivo y debe ser igual a la fecha actual
			String fechaLeida = Files.readSpecifictLineOfFile4690(fileName, 1).trim();
//			log.info("Primer registro del archivo: " + fechaLeida);
//			log.info("fechaActual(): " + fechaActual().trim());
			if (fechaLeida.equals(fechaActual().trim())) {
//				log.info("Leyo, el archivo pertenece a la fecha actual ");
				validador = true;
				return validador;
			} else {
//				log.info("Leyo, el archivo no es actual");
				validador = false;
				return validador;
			}
		} catch (Exception e) {
			log.info("validarFecha() Catch, No pudo leer archivo: " + e.toString());
			validador = false;
			return validador;
		}
	}

	public static String fechaActual() {
		String fecha;
		Calendar cd = Calendar.getInstance();
		fecha = String.valueOf(cd.get(Calendar.YEAR)) + String.valueOf(cd.get(Calendar.MONTH) + 1)
				+ String.valueOf(cd.get(Calendar.DAY_OF_MONTH));
		return fecha;
	}

	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo PrinterPRTProcess...");
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
		log.info("Finalizó el Proceso de Impresion de tickets PRT.");
		return true;
	}

}
