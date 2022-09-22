/**
 * 
 */
package com.allc.oswin.procesos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.allc.conexion.Conexion;
import com.allc.conexion.ConexionCliente;
import com.allc.files.Files;
import com.allc.main.constants.Constants;
import com.allc.main.properties.PropertiesWinEQ;
import com.allc.util.Util2;

/**
 * @author gustavo
 *
 */
public class UpdateProcessStatus extends Thread {
	static Logger log = Logger.getLogger(UpdateProcessStatus.class);

	private RandomAccessFile randSeekRead = null;
	private RandomAccessFile randFileRead = null;
	private String tslSeekFileName;
	private Pattern p = Pattern.compile(Constants.Comunicacion.REGEX);
	public boolean sent = false;
	public boolean isAnswer = false;
	private boolean endReader = false;
	private String storeNumber;
	private String fileName = null;
	private String actualDate = null;

	public Object init() {
		Boolean result = Boolean.FALSE;
		try {
			tslSeekFileName = PropertiesWinEQ.ParamUPS.FILE_SEEK;
			actualDate = getFechaActual();
			fileName = PropertiesWinEQ.ParamUPS.FILE_NAME + "_" + actualDate;
			result = Boolean.TRUE;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	public Object closure() {
		Boolean result = Boolean.FALSE;
		try {
			endReader = true;
			result = Boolean.TRUE;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	public void run() {
		log.info("Proceso UPS iniciado.");
		reader();
	}

	/**
	 * Obtain the Exception Log and save into the file
	 * 
	 * @param saf
	 *            SAF class
	 */
	public void reader() {
		long filePointer;
		String line;
		long tmp = 0;
		try {
			if (!Files.fileExists(tslSeekFileName)) {
				Files.creaEscribeDataArchivo(tslSeekFileName,
						Util2.rpad(Constants.Comunicacion.CERO, Constants.Comunicacion.SPACE, 20)
								+ Constants.Comunicacion.CRLF,
						false);
				Files.creaEscribeDataArchivo(tslSeekFileName, fileName + Constants.Comunicacion.CRLF, true);
			} else {
				fileName = Files.readSpecifictLineOfFile(tslSeekFileName, 3);
				int index = PropertiesWinEQ.ParamUPS.FILE_NAME.length() + 1;
				log.info("FileName: " + fileName + " indice:" + index);
				actualDate = fileName.substring(index);
				log.info("ActualDate" + actualDate);
			}

			/**
			 * Obtain the last position since where must send the information
			 **/
			filePointer = getOffset();
			if (filePointer >= 0) {
				try {
					/** open the fileSeek to Read **/
					randFileRead = new RandomAccessFile(fileName, "r");
					/** goto position randStore in the Exception Log File **/
					randFileRead.seek(filePointer);
					log.info("Puntero:" + filePointer);
					/** Read file line by line **/
					while (!endReader) {
						line = randFileRead.readLine();
						if (line != null) {
							log.info("LINE: " + line);
							if (line.trim().length() > 0) {
								Conexion cx = new Conexion();
								/** Set the attributes **/
								cx.setIp(PropertiesWinEQ.ParamSEQ.IP_SERVER);
								cx.setPuerto(PropertiesWinEQ.ParamSEQ.PORT_SERVER);
								cx.setReintentos(PropertiesWinEQ.ParamSEQ.REINTENTOS_CX);
								cx.setTimeOutConexion(PropertiesWinEQ.ParamSEQ.TIME_OUT_CONEXION);
								cx.setTimeOutSleep(PropertiesWinEQ.ParamSEQ.TIME_OUT_SLEEP);
								cx.setCantidadBytesLongitud(Constants.Comunicacion.CANTIDAD_BYTES_LONGITUD);
								ConexionCliente cnxion = null;
								cnxion = new ConexionCliente(cx.getIp(), cx.getPuerto(), cx.getReintentos(),
										cx.getTimeOutConexion(), cx.getTimeOutSleep(), cx.getCantidadBytesLongitud(),
										null);
								if (cnxion.ConectaSocket()) {
									log.info(Thread.currentThread().getName() + ": Connected.");
									if (log.isDebugEnabled())
										log.debug(Thread.currentThread().getName() + ": Connected.");
									// si hay data se envia.
									sent = send(cnxion, line);
									cnxion.closeConexion();
								} else {
									// log.info(Thread.currentThread().getName()
									// + ": Not Connected.");
									if (log.isDebugEnabled())
										log.debug(Thread.currentThread().getName() + ": Not connected.");
									continue;
								}
								if (sent) {
									tmp = randFileRead.getFilePointer();
									String valorPosicion = Util2.rpad(String.valueOf(tmp), " ", 20)
											+ Constants.Comunicacion.CRLF;
									Files.creaEscribeDataArchivo(tslSeekFileName,
											String.valueOf(valorPosicion) + Constants.Comunicacion.CRLF, false);
									Files.creaEscribeDataArchivo(tslSeekFileName,
											fileName + Constants.Comunicacion.CRLF, true);
									sent = false;
								} else {
									log.error("the Process Status Record was not sent successfully");
								}
							}
						} else {
							Thread.sleep(30000);
							if (!actualDate.equals(getFechaActual())) {
								Iterator itFechas = obtenerFechasIntermedias(actualDate, getFechaActual()).iterator();
								while (itFechas.hasNext()) {
									actualDate = (String) itFechas.next();
									String nomArchivo = PropertiesWinEQ.ParamUPS.FILE_NAME + "_" + actualDate;
									File fileToReadAux = new File(nomArchivo);
									if (fileToReadAux.exists())
										break;

								}

								filePointer = 0;
								fileName = PropertiesWinEQ.ParamUPS.FILE_NAME + "_" + actualDate;
								try {
									randFileRead.close();
								} catch (Exception e) {
									log.error(e.getMessage(), e);
								}
								randFileRead = new RandomAccessFile(fileName, "r");
								randFileRead.seek(filePointer);
								Files.creaEscribeDataArchivo(tslSeekFileName,
										Util2.rpad(Constants.Comunicacion.CERO, Constants.Comunicacion.SPACE, 20)
												+ Constants.Comunicacion.CRLF,
										false);
								Files.creaEscribeDataArchivo(tslSeekFileName, fileName + Constants.Comunicacion.CRLF,
										true);
								log.info("Se detectó un cambio de archivo a procesar. Archivo a procesar: " + fileName);

							}
						}
					}

				} catch (Exception e) {
					log.error(e.getMessage(), e);

				} finally {
					/** seek reader **/
					try {
						randSeekRead.close();
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
					/** EL file reader **/
					try {
						randFileRead.close();
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info("Proceso UPS finalizado.");
	}

	private String getFechaActual() {
		return Util2.convertDateToString(new Date(), "ddMMyy");
	}

	private boolean send(ConexionCliente conexionCliente, String linea) {
		String data;
		List list = null;
		StringBuffer sb = new StringBuffer();
		try {
			String today = Util2.convertDateToString(new Date(), "yyyyMMddHHmmss");
			sb.append(Constants.Comunicacion.SOCKET_CHANNEL).append(Constants.Comunicacion.CAR)
					.append(Constants.ProcessConstants.LAUNCH_UPS).append(Constants.Comunicacion.CAR)
					.append(InetAddress.getLocalHost().getHostName()).append(Constants.Comunicacion.CAR)
					.append(PropertiesWinEQ.Parameters.DES_CADENA).append(Constants.Comunicacion.CAR)
					.append(storeNumber).append(Constants.Comunicacion.CAR).append(today)
					.append(Constants.Comunicacion.CAR).append(linea);
			linea = Util2.agregaLongitudInicioCadena(sb.toString(), Constants.Comunicacion.CANTIDAD_BYTES_LONGITUD);

			if (conexionCliente.escribeDataSocket(linea)) {
				if (!conexionCliente.timeOutSocket()) {

					data = conexionCliente.leeDataSocket(conexionCliente.leeLongitudDataSocket());
					list = Arrays.asList(p.split(data));
					if (list.get(Constants.Comunicacion.CANTIDAD_DATOS_HEADER).toString()
							.equals(Constants.Comunicacion.CERO)) {
						return true;
					}

				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);

		}
		return false;
	}

	/**
	 * Obtiene la posicion del archivo dataFileName desde la que se debe obtener
	 * la informacion para registrarla como ExceptionLog
	 * 
	 * @param dataFileSeekName
	 *            Nombre del archivo que indica la ultima posicion en el archivo
	 *            dataFileName hasta donde se obtuvo la informacion para
	 *            registrarla como ExceptionLog
	 * @param dataFileName
	 *            Nombre del archivo que contiene la data que se registra como
	 *            Exceptionlog
	 * @return La posicion en el archivo dataFileName desde la que se debe de
	 *         tomar la informacion para registrarla como ExceptionLog
	 */
	private long getOffset() {
		long filePointer = 0;
		try {
			randSeekRead = new RandomAccessFile(tslSeekFileName, "r");
			filePointer = getOffsetFile();
			if (filePointer > 0) {
				randFileRead = new RandomAccessFile(fileName, "r");
				randFileRead.close();
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return filePointer;
	}

	/**
	 * 
	 * @param nombreArchivo
	 *            nombreArchivo
	 * @param data
	 *            data
	 * @param append
	 *            true adiciona, false = como si creara el archivo y guardara la
	 *            data
	 */
	public boolean creaEscribeDataArchivo(String nombreArchivo, String data, boolean append) {
		try {

			PrintWriter fileaPos = new PrintWriter(new BufferedWriter(new FileWriter(nombreArchivo, append)));
			fileaPos.write(data, 0, data.length());
			fileaPos.close();
			return true;
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			return false;
		}
	}

	/**
	 * Obtain the file offset
	 * 
	 * @param fileName
	 *            File that contains the offset
	 * @return Position
	 */
	private long getOffsetFile() {
		long filePointer;
		String data;
		try {
			data = randSeekRead.readLine();
			randSeekRead.seek(0);

			if (null == data)
				filePointer = 0;
			else
				try {
					filePointer = Long.parseLong(data.replaceAll(" ", ""));
				} catch (Exception e) {
					log.error("getOffsetFile: the file " + tslSeekFileName + " not contain a number as a pointer. ", e);
					filePointer = -1;
				}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			filePointer = -1;
		}
		return filePointer;
	}

	/**
	 * @param storeNumber
	 *            the storeNumber to set
	 */
	public void setStoreNumber(String storeNumber) {
		this.storeNumber = storeNumber;
	}

	private static List obtenerFechasIntermedias(String fechaIni, String fechaFin) {
		int ano1 = (new Integer(fechaIni.substring(4, 6))).intValue();
		int ano2 = (new Integer(fechaFin.substring(4, 6))).intValue();
		int mes1 = (new Integer(fechaIni.substring(2, 4))).intValue();
		int mes2 = (new Integer(fechaFin.substring(2, 4))).intValue();
		int dia1 = (new Integer(fechaIni.substring(0, 2))).intValue();
		int dia2 = (new Integer(fechaFin.substring(0, 2)).intValue());
		List fechas = new ArrayList();
		int diaMax = 31;
		int mesMax = 12;
		log.info("ano1: " + ano1 + "ano2: " + ano2);
		while (ano1 <= ano2) {
			log.info("mes1: " + mes1 + "mes2: " + mes2);
			while (mes1 <= mesMax && mes1 <= mes2) {
				log.info("dia1: " + dia1 + "dia2: " + dia2);
				while (dia1 <= diaMax) {
					if (dia1 == diaMax) {
						dia1++;
					} else {
						dia1++;
						String dia = (new Integer(dia1).toString());
						String mes = (new Integer(mes1).toString());
						String ano = (new Integer(ano1).toString());
						fechas.add((dia.length() < 2 ? "0" + dia : dia) + (mes.length() < 2 ? "0" + mes : mes)
								+ (ano.length() < 2 ? "0" + ano : ano));
					}
				}
				dia1 = 0;
				mes1++;
			}
			mes1 = 1;
			ano1++;
		}
		fechas.add(fechaFin);
		return fechas;
	}
}
