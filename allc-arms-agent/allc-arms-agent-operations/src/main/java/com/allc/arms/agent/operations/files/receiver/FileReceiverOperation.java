package com.allc.arms.agent.operations.files.receiver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import org.apache.commons.net.nntp.NewGroupsOrNewsQuery;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileOutputStream4690; 

public class FileReceiverOperation extends AbstractOperation { //A modifica los FILES

	protected static Logger log;
	protected String descriptorOperation = "RMA_SEND_P";

	public boolean shutdown(long timeToWait) { 
		return false;
	}

	protected void initialize() {
		log = Logger.getLogger(FileReceiverOperation.class);
	}

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {

		initialize();
		try { 
			Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
					descriptorOperation + "|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando recepción de archivo.\n",
					true);
			log.info("Escribi en archivo: " + (getEyesFileName(properties) + descriptorOperation + "|"
					+ properties.getHostName() + "|3|" + properties.getHostAddress() + "|" + frame.getHeader().get(3)
					+ "|STR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())));
			String fileName = (String) frame.getBody().get(0);
			long fileSize = (Long.valueOf((String) frame.getBody().get(1))).longValue();
			boolean receiveFile = false;
			StringBuilder msg = new StringBuilder();
			if (!fileName.trim().isEmpty() && fileSize > 0) {
				File4690 file4690 = new File4690(fileName.substring(0, fileName.lastIndexOf(File4690.separator)+1));
//				File file = new File(fileName.substring(0, fileName.lastIndexOf(File.separator) + 1));
				log.info("File: " + file4690.getPath());
				file4690.mkdirs();
				File4690 file1 = new File4690(fileName);
				if (file1.exists())
					file1.delete();
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("0");
				receiveFile = true;
			} else {
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("1");
			}
			String tmp = Util.addLengthStartOfString(msg.toString(),
					properties.getInt("serverSocket.quantityBytesLength"));
			socket.writeDataSocket(tmp);

			if (receiveFile) { 
				String fileNameTemp = "TMP-" + fileName.substring(fileName.lastIndexOf(File4690.separator) + 1);
				String fileTempPath = fileName.substring(0, fileName.lastIndexOf(File4690.separator) + 1);
				String fileTemp = fileTempPath + fileNameTemp;
				log.info("File-Temp: " + fileTemp);

				File4690 fileExist = new File4690(fileTempPath + fileName.substring(fileName.lastIndexOf(File4690.separator) + 1));
				int cont = 0;
				while (fileExist.exists()) {
					log.info("El archivo: " + fileExist.getName() + (cont > 0 ? String.valueOf(cont) : "")
							+ " existe en el destino y se renombrara");
					cont++;
					File4690 tempFile = new File4690(fileTempPath
							+ fileName.substring(fileName.lastIndexOf(File4690.separator) + 1) + "_" + cont);
					if (tempFile.exists())
						fileExist = new File4690(fileTempPath
								+ fileName.substring(fileName.lastIndexOf(File4690.separator) + 1) + "_" + cont);
					else
						fileExist.renameTo(new File4690(fileTempPath
								+ fileName.substring(fileName.lastIndexOf(File4690.separator) + 1) + "_" + cont));
				}
				byte[] receivedData;
				long totalBytesToRead = fileSize;
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream4690(fileTemp));
				do {
					int bytesToRead = totalBytesToRead > 8192 ? 8192 : (int) totalBytesToRead;
					receivedData = socket.readDataSocketToBytesArray(bytesToRead);
					if (receivedData != null) {
						bos.write(receivedData, 0, receivedData.length);
						totalBytesToRead = totalBytesToRead - bytesToRead;
					} else
						break;
				} while (receivedData != null && totalBytesToRead > 0);
				bos.close();
				if ((new File4690(fileTemp)).length() == fileSize) {
					log.info("Archivo recibido correctamente");
					if ((new File4690(fileTemp)).renameTo((new File4690(fileName)))) {
						socket.writeDataSocket(tmp);
						Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
								descriptorOperation + "|" + properties.getHostName() + "|3|"
										+ properties.getHostAddress() + "|" + frame.getHeader().get(3) + "|PRC|"
										+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|Archivo recibido correctamente.\n",
								true);
					} else {
						Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
								descriptorOperation + "" + properties.getHostName() + "|3|"
										+ properties.getHostAddress() + "|" + frame.getHeader().get(3) + "|WAR|"
										+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|No se pudo renombrar el archivo: " + fileNameTemp + ".\n",
								true);
					}
				} else {
					(new File4690(fileTemp)).delete();
					log.info("Error al recibir el archivo");
					Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
							descriptorOperation + "|" + properties.getHostName() + "|3|" + properties.getHostAddress()
									+ "|" + frame.getHeader().get(3) + "|ERR|"
									+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|Error al recibir el archivo: " + fileName + ".\n",
							true);
				}
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
						descriptorOperation + "" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|WAR|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al recibir los datos del archivo.\n",
						true);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
						descriptorOperation + "|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|ERR|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al recibir el archivo.\n",
						true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
		return false;
	}

	protected String getEyesFileName(PropFile properties) {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

}
