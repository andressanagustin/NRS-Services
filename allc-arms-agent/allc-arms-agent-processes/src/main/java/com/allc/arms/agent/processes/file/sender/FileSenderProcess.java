package com.allc.arms.agent.processes.file.sender;

import java.io.BufferedInputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
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
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileFilter4690;
import com.ibm.OS4690.FileInputStream4690;

public class FileSenderProcess extends AbstractProcess {
	protected Logger log;
	protected File4690 inFolder;
//	protected File outFolder;
	protected String outFolder;
	protected int sleepTime;
	protected Iterator filesToProcess = null;
	public boolean isEnd = false;
	protected boolean finished = false;
	protected String storeCode;
	protected String ip;
	protected int port;
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	protected static Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
	protected ConnSocketClient socketClient;
	protected String descriptorProceso = "FILE_SEND_P";

	protected void inicializar() {
		log = Logger.getLogger(FileSenderProcess.class);
		isEnd = false;
		try {
			log.info("Iniciando FileSenderProcess...");
			Files.creaEscribeDataArchivo4690(getEyesFileName(),
					descriptorProceso + "|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ storeCode + "|STR|"
							+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando envío de archivos.\n",
					true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void run() {
		inicializar();

		perform();

		finished = true;
	}

	protected void perform() {
		while (!isEnd) {
			String filename = null;
			try {
				File4690 fileToSend = getNextFile();
				if (fileToSend != null) {
					filename = fileToSend.getName();
					log.info("Archivo a procesar: " + filename);
					//obtenemos las subcarpetas si existen
					String subdirs = fileToSend.getAbsolutePath().substring(inFolder.getAbsolutePath().length() + 1, fileToSend.getAbsolutePath().length() - filename.length());

					StringBuffer data = getFrameHeader();
//					data.append(ArmsAgentConstants.Communication.FRAME_SEP).append(outFolder + File.separator + filename).append(ArmsAgentConstants.Communication.FRAME_SEP).append(fileToSend.length());
					data.append(ArmsAgentConstants.Communication.FRAME_SEP).append(outFolder + "/" + subdirs.replace("\\", "/") + filename)
							.append(ArmsAgentConstants.Communication.FRAME_SEP).append(fileToSend.length());
					List list = Arrays.asList(p.split(data.toString()));

					Frame frame = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
							ArmsAgentConstants.Communication.FRAME_SEP);
					if (frame.loadData()) {
						boolean send = sendFileHeader(frame) && sendFileBytes(fileToSend);

						if (send) {
							fileToSend.delete();
							log.info("Archivo enviado correctamente.");
							Files.creaEscribeDataArchivo4690(getEyesFileName(),
									descriptorProceso + "|" + properties.getHostName() + "|3|"
											+ properties.getHostAddress() + "|" + storeCode + "|PRC|"
											+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
											+ "|Archivo enviado: " + filename + ".\n",
									true);
						} else {
							log.error("Error al enviar el archivo.");
							Files.creaEscribeDataArchivo4690(getEyesFileName(),
									descriptorProceso + "|" + properties.getHostName() + "|3|"
											+ properties.getHostAddress() + "|" + storeCode + "|ERR|"
											+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
											+ "|Error al enviar el archivo: " + filename + ".\n",
									true);
						}
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
				try {
					Files.creaEscribeDataArchivo4690(getEyesFileName(),
							descriptorProceso + "|" + properties.getHostName() + "|3|" + properties.getHostAddress()
									+ "|" + storeCode + "|ERR|"
									+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|Error al enviar el archivo: " + filename + ".\n",
							true);
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
			} finally {
				closeClient();
			}

		}
	}

	protected StringBuffer getFrameHeader() {
		StringBuffer data = new StringBuffer();
		data.append("S").append(ArmsAgentConstants.Communication.FRAME_SEP)
//				.append(ArmsAgentConstants.Process.FILE_RECEIVER_OPERATION)
				.append("19").append(ArmsAgentConstants.Communication.FRAME_SEP).append("000")
				.append(ArmsAgentConstants.Communication.FRAME_SEP).append(storeCode)
				.append(ArmsAgentConstants.Communication.FRAME_SEP).append(ArmsAgentConstants.Communication.TEMP_CONN)
				.append(ArmsAgentConstants.Communication.FRAME_SEP)
				.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
		return data;
	}

	protected String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	protected boolean connectClient() {
		if (socketClient == null) {
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(ip);
			socketClient.setPortServer(port);
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

	protected boolean sendFileHeader(Frame frame) {
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
						// cada 5 timeouts escribimos una trama vacía para asegurarnos que el socket
						// esté activo
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

	protected boolean sendFileBytes(File4690 fileToSend) {

		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");

		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream4690(fileToSend));
			long totalBytesToRead = fileToSend.length();
			byte[] byteArray = new byte[(int) (totalBytesToRead < 8192 ? totalBytesToRead : 8192)];

			while (bis.read(byteArray) != -1 && totalBytesToRead > 0) {
				socketClient.writeByteArraySocket(byteArray);
				totalBytesToRead = totalBytesToRead - 8192;
				if (totalBytesToRead < 8192 && totalBytesToRead > 0)
					byteArray = new byte[(int) totalBytesToRead];
			}
			bis.close();
			int numberOfBytes = 0;
			int timeOutCycles = 0;
			while (numberOfBytes == 0) {
				numberOfBytes = socketClient.readLengthDataSocket();
				if (timeOutCycles == 5) {
					// cada 5 timeouts escribimos una trama vacía para asegurarnos que el socket
					// esté activo
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
				String str = socketClient.readDataSocket(numberOfBytes);
				if (StringUtils.isNotBlank(str)) {
					List list = Arrays.asList(p.split(str));
					Frame frameRpta = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
							ArmsAgentConstants.Communication.FRAME_SEP);
					log.info("Respuesta recibida: " + frameRpta.toString());
					if (frameRpta.getStatusTrama() == 0) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			socketClient.setConnected(false);
		}
		return false;
	}

	public static File4690[] obtenerArchivosSubcarpetas(File4690 dir){
		
		File4690[] files = dir.listFiles(new FileFilter4690() {
			public boolean accept(File4690 pathname) {
				return pathname.isFile();
			}
		});
		File4690[] dirs = dir.listFiles(new FileFilter4690() {
			public boolean accept(File4690 pathname) {
				return pathname.isDirectory();
			}
		});
		for(int i = 0; i < dirs.length; i++){
			File4690[] subfiles = obtenerArchivosSubcarpetas(dirs[i]);
			if(subfiles.length > 0)
				files = (File4690[])ArrayUtils.addAll(files, subfiles);
		}

		return files;
	}
	
	protected File4690 getNextFile() {

		if ((this.filesToProcess == null) || !filesToProcess.hasNext()) {
			do {
				if (isEnd) {
					return null;
				}

				log.info("Folder:" + inFolder);

				File4690[] files = obtenerArchivosSubcarpetas(inFolder);

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

	public void setEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}

	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo SenderFileProcess...");
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
		log.info("Finalizó el Proceso de Envío de archivos.");
		return true;
	}

}
