/**
 * 
 */
package com.allc.os4690.procesos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.allc.conexion.Conexion;
import com.allc.conexion.ConexionCliente;
import com.allc.main.constants.Constants;
import com.allc.main.properties.Properties4690EQ;
import com.allc.util.Util2;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileInputStream4690;
import com.ibm.OS4690.FileOutputStream4690;
import com.ibm.OS4690.FilenameFilter4690;
import com.ibm.OS4690.POSFile;
import com.ibm.OS4690.RandomAccessFile4690;


/**
 * @author gustavo
 *
 */
public class UpdateProcessStatus extends Thread {
	static Logger log = Logger.getLogger(UpdateProcessStatus.class);

	private RandomAccessFile4690 randSeekRead = null;
	private RandomAccessFile4690 randFileRead = null;
	private POSFile posFileSeekWriter = null;
	private String tslSeekFileName;
	private Pattern p = Pattern.compile(Constants.Comunicacion.REGEX);
	public boolean sent = false;
	public boolean isAnswer = false;
	private boolean endReader = false;
	private String storeNumber;
	private String ctrlNode;
	private String fileName = null;
	private String actualDate = null;
	private ConexionCliente cnxion = null;

	public Object init() {
		Boolean result = Boolean.FALSE;
		try {
			tslSeekFileName = Properties4690EQ.ParamUPS.FILE_SEEK;
			actualDate = getFechaActual();
			fileName = Properties4690EQ.ParamUPS.FILE_NAME + "_" + actualDate;

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
		boolean sendAnterior = false;
		final int CANT_MAX_ENVIO = 3;
		int cantidadEnvio = 0;
		try {

			/**
			 * Obtain the last position since where must send the information
			 **/
			filePointer = getOffset();
			if (filePointer >= 0) {
				try {
					posFileSeekWriter = new POSFile(tslSeekFileName, "rw", POSFile.SHARED_READ_WRITE_ACCESS);
					/** open the fileSeek to Read **/
					randFileRead = new RandomAccessFile4690(fileName, "r");
					/** goto position randStore in the Exception Log File **/
					randFileRead.seek(filePointer);
					/** Read file line by line **/
					line = randFileRead.readLine();
					while (!endReader) {
//						line = randFileRead.readLine();
						if (sendAnterior) {
							line = randFileRead.readLine();
							sendAnterior = false;
							cantidadEnvio = 0;
						} else {
							cantidadEnvio++;
							log.info("No manda línea, prueba a mandar de nuevo.");
						}
						if (line != null) {
							if (cantidadEnvio > CANT_MAX_ENVIO) { //Lo duerme por si queda engancha algo mal
								Thread.sleep(30000);
								cantidadEnvio=0;
							}
							log.info("LINE: " + line);
							if (line.trim().length() > 0) {
								if (connectClient()) {
									log.info(Thread.currentThread().getName() + ": Connected.");
									if (log.isDebugEnabled())
										log.debug(Thread.currentThread().getName() + ": Connected.");
									// si hay data se envia.
									sent = send(cnxion, line);
								} else {
									log.info(Thread.currentThread().getName() + ": Not Connected.");
									if (log.isDebugEnabled())
										log.debug(Thread.currentThread().getName() + ": Not connected.");
									continue;
								}
								if (sent) {
									sendAnterior = true;
									tmp = filePointer++;
									String valorPosicion = Util2.rpad(String.valueOf(tmp), " ", 20)
											+ Constants.Comunicacion.CRLF;
									posFileSeekWriter.write(valorPosicion.getBytes(), 0, POSFile.FROM_START_OF_FILE,
											POSFile.FLUSH, valorPosicion.length());
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
									String nomArchivo = Properties4690EQ.ParamUPS.FILE_NAME + "_" + actualDate;
									File4690 fileToReadAux = new File4690(nomArchivo);
									if (fileToReadAux.exists())
										break;

								}
								if (actualDate.equals(getFechaActual())) {
									fileName = Properties4690EQ.ParamUPS.FILE_NAME + "_" + actualDate;
									File4690 file = new File4690(fileName);
									if (!file.exists()) {
										file.createNewFile();
									}
									deletePreviousFiles();
								}
								filePointer = 0;
								// actualDate = getFechaActual();
								fileName = Properties4690EQ.ParamUPS.FILE_NAME + "_" + actualDate;
								try {
									randFileRead.close();
								} catch (Exception e) {
									log.error(e.getMessage(), e);
								}

								randFileRead = new RandomAccessFile4690(fileName, "r");
								randFileRead.seek(filePointer);
								creaEscribeDataArchivo(tslSeekFileName,
										Util2.rpad(Constants.Comunicacion.CERO, Constants.Comunicacion.SPACE, 20)
												+ Constants.Comunicacion.CRLF,
										false);
								creaEscribeDataArchivo(tslSeekFileName, fileName + Constants.Comunicacion.CRLF, true);
								log.info("Se detectó un cambio de archivo a procesar. Archivo a procesar: " + fileName);
							}
							sendAnterior = true; //Para que lea la nueva linea
						}
					}

				} catch (Exception e) {
					log.error(e.getMessage(), e);

				} finally {
					/** seek writer **/
					try {
						closeConnection();
						posFileSeekWriter.closeFull();
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
					/** seek reader **/
					try {
						closeConnection();
						randSeekRead.close();
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
					/** EL file reader **/
					try {
						closeConnection();
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
	
	/**
	 * Elimina archivos anteriores
	 * @return true: elimino archivos - false: hubo un error o no hay archivos
	 */
	private boolean deletePreviousFiles() {
		try {
			final String FILE_START_WITH = Properties4690EQ.ParamUPS.FILE_NAME.replace(Properties4690EQ.ParamUPS.PATH,"") + "_"; 
			final int AMOUNT_DAY_NOT_DELETE = 2;
	        List<String> fileNotDelete = new ArrayList<String>();
	        //BUSCO LOS NOMBRES DE LOS ARCHIVO A NO ELIMINAR
	        for (int i = 0; i <= AMOUNT_DAY_NOT_DELETE; i++) {
	            final Calendar calendar = Calendar.getInstance();
	            calendar.add(Calendar.DATE, -i);
	            fileNotDelete.add(FILE_START_WITH + Util2.convertDateToString(calendar.getTime(), "ddMMyy"));
	        }
			//LISTO LOS ARCHIVOS DEL PATH CON EL FILTRO
			File4690 path = new File4690(Properties4690EQ.ParamUPS.PATH);
			FilenameFilter4690 filter = new FilenameFilter4690() {
                public boolean accept(File4690 f, String name)
                {
                    return name.startsWith(FILE_START_WITH);
                }
            };
            File4690[] files = path.listFiles(filter);
            if (files == null || files.length == 0){
                log.info("No hay archivos anteriores a eliminar o no existe el directorio.");
                return false;
            }
            //RECORRO LOS ARCHIVOS Y LOS ELIMINO
            for (int i = 0; i < files.length; i++) {
            	if (fileNotDelete.contains(files[i].getName())) continue;
            	if (files[i].delete()) log.info("Archivo eliminado: " + files[i].getName());
            	else log.error("Archivo no eliminado: " + files[i]);
            }
            return true;
		} catch (Exception e) {
			log.error("Error al eliminar los archivos: " + e.getMessage(), e);
			return false;
		}
	}

	private boolean send(ConexionCliente conexionCliente, String linea) {
		String data;
		List list = null;
		int numOfBytesToRead = 0;
		StringBuffer sb = new StringBuffer();
		String today = Util2.convertDateToString(new Date(), "yyyyMMddHHmmss");
		sb.append("S").append(Constants.Comunicacion.CAR).append(Constants.ProcessConstants.LAUNCH_UPS)
				.append(Constants.Comunicacion.CAR).append(ctrlNode).append(Constants.Comunicacion.CAR)
				.append(Properties4690EQ.Param4690.DES_CADENA).append(Constants.Comunicacion.CAR).append(storeNumber)
				.append(Constants.Comunicacion.CAR).append(today).append(Constants.Comunicacion.CAR).append(linea);
		linea = Util2.agregaLongitudInicioCadena(sb.toString(), Constants.Comunicacion.CANTIDAD_BYTES_LONGITUD);
		if (conexionCliente.escribeDataSocket(linea)) {
			if (null != (data = conexionCliente.timeOutSocket(conexionCliente.getCantidadBytesLongitud()))) {
				try {
					numOfBytesToRead = Integer.parseInt(data);
					if (null != (data = conexionCliente.timeOutSocket(numOfBytesToRead))) {
						list = Arrays.asList(p.split(data));

						if (list.get(Constants.Comunicacion.CANTIDAD_DATOS_HEADER).toString()
								.equals(Constants.Comunicacion.CERO)) {
							return true;
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);

				}
			}
		}
		else closeConnection(); //Si no puede escribir desconecta para volver a conectar
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
			File4690 fichero = new File4690(tslSeekFileName);
			if (!fichero.exists()) {
				if (null != fileName) {
					creaEscribeDataArchivo(tslSeekFileName,
							Util2.rpad(Constants.Comunicacion.CERO, Constants.Comunicacion.SPACE, 20)
									+ Constants.Comunicacion.CRLF,
							false);
					creaEscribeDataArchivo(tslSeekFileName, fileName + Constants.Comunicacion.CRLF, true);

					File4690 file = new File4690(fileName);
					if (!file.exists()) {
						file.createNewFile();
						deletePreviousFiles();
					}
				}
			} else {
				fileName = readSpecifictLineOfFile4690(tslSeekFileName, 2);
				actualDate = fileName.substring(fileName.length() - 6);
				File4690 file = new File4690(fileName);
				if (!file.exists()) {
					file.createNewFile();
					deletePreviousFiles();
				}
			}

			randSeekRead = new RandomAccessFile4690(tslSeekFileName, "r");
			filePointer = getOffsetFile();
			if (filePointer > 0) {
				randFileRead = new RandomAccessFile4690(fileName, "r");
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

			File4690 file = new File4690(nombreArchivo);
			if (!file.exists()){
				file.createNewFile();
			}
			FileOutputStream4690 fos = new FileOutputStream4690(nombreArchivo, append);
			fos.write(data.getBytes(), 0, data.length());
			fos.close();
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

	public static String readSpecifictLineOfFile4690(String fileName, long row) {

		BufferedReader br = null;
		String linea = "";
		long cont = 0;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream4690(fileName)));
			while (null != (linea = br.readLine())) {
				cont++;
				if (cont == row) {
					break;
				}
			}
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);

		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
		}
		return linea;
	}
	
	protected boolean connectClient() {
		if (cnxion == null || !cnxion.getConectado()) {
			log.info("Conecta con server");
			Conexion cx = new Conexion();
			/** Set the attributes **/
			cx.setIp(Properties4690EQ.ParamSEQ.IP_SERVER);
			cx.setPuerto(Properties4690EQ.ParamSEQ.PORT_SERVER);
			cx.setReintentos(Properties4690EQ.ParamSEQ.REINTENTOS_CX);
			cx.setTimeOutConexion(Properties4690EQ.ParamSEQ.TIME_OUT_CONEXION);
			cx.setTimeOutSleep(Properties4690EQ.ParamSEQ.TIME_OUT_SLEEP);
			cx.setCantidadBytesLongitud(Constants.Comunicacion.CANTIDAD_BYTES_LONGITUD);
			cnxion = null;
			cnxion = new ConexionCliente(cx.getIp(), cx.getPuerto(), cx.getReintentos(),
					cx.getTimeOutConexion(), cx.getTimeOutSleep(), cx.getCantidadBytesLongitud(),
					null);
			return cnxion.ConectaSocketReintentos();
		}
		else {
			return true;
		}
		
	}
	
	protected void closeConnection() {
		if (cnxion != null){
			cnxion.closeConexion();
			cnxion = null;
		}
	}

	/**
	 * @param storeNumber
	 *            the storeNumber to set
	 */
	public void setStoreNumber(String storeNumber) {
		this.storeNumber = storeNumber;
	}

	/**
	 * @param ctrlNode
	 *            the ctrlNode to set
	 */
	public void setCtrlNode(String ctrlNode) {
		this.ctrlNode = ctrlNode;
	}
}
