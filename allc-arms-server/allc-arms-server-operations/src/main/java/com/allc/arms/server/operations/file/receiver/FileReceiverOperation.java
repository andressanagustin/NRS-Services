package com.allc.arms.server.operations.file.receiver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class FileReceiverOperation extends AbstractOperation {

	protected static Logger log;
	protected String descriptorOperation = "FILE_SEND_O";

	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	protected void initialize() {
		log = Logger.getLogger(FileReceiverOperation.class);
	}

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		String storeNumber;
		initialize();
		try {
			storeNumber = (String) frame.getHeader().get(3);
			if (storeNumber == "") storeNumber="000";
			UtilityFile.createWriteDataFile(getEyesFileName(properties),
					descriptorOperation + "|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ storeNumber+ "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando recepción de archivo.\n",
					true);

			String fileName = (String) frame.getBody().get(0);
			long fileSize = (Long.valueOf((String) frame.getBody().get(1))).longValue();
			boolean receiveFile = false;
			StringBuilder msg = new StringBuilder();
			if (!fileName.trim().isEmpty() && fileSize > 0) {
				File file = new File(fileName.substring(0, fileName.lastIndexOf(File.separator) + 1));
				log.info("File: " + file.getPath());
				file.mkdirs();
				File file1 = new File(fileName);
				if(file1.exists())
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

				String fileNameTemp = "TMP-" + fileName.substring(fileName.lastIndexOf(File.separator) + 1);
				String fileTempPath = fileName.substring(0, fileName.lastIndexOf(File.separator) + 1);
				String fileTemp = fileTempPath + fileNameTemp;
				log.info("File-Temp: " + fileTemp);

				File fileExist = new File(fileTempPath + fileName.substring(fileName.lastIndexOf(File.separator) + 1));
				int cont = 0;
				while(fileExist.exists()){
					log.info("El archivo: "+fileExist.getName()+ (cont > 0 ? cont : "") +" existe en el destino y se renombrara");
					cont++;
					File tempFile = new File(fileTempPath + fileName.substring(fileName.lastIndexOf(File.separator) + 1)+"_"+cont);
					if(tempFile.exists())
						fileExist = new File(fileTempPath + fileName.substring(fileName.lastIndexOf(File.separator) + 1)+"_"+cont);
					else
						fileExist.renameTo(new File(fileTempPath + fileName.substring(fileName.lastIndexOf(File.separator) + 1)+"_"+cont));
				}
				byte[] receivedData;
				long totalBytesToRead = fileSize;
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileTemp));
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
				if ((new File(fileTemp)).length() == fileSize) {
					log.info("Archivo recibido correctamente");
					if((new File(fileTemp)).renameTo((new File(fileName)))){
						socket.writeDataSocket(tmp);
						UtilityFile.createWriteDataFile(getEyesFileName(properties),
								descriptorOperation + "|" + properties.getHostName() + "|3|" + properties.getHostAddress()
									+ "|" + storeNumber+ "|PRC|"
									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
									+ "|Archivo recibido correctamente.\n",
									true);
					}else{
						UtilityFile.createWriteDataFile(getEyesFileName(properties),
								descriptorOperation + "|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
										+ storeNumber + "|WAR|"
										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|No se pudo renombrar el archivo: " + fileNameTemp + ".\n",
								true);
					}
				} else {
					(new File(fileTemp)).delete();
					log.info("Error al recibir el archivo");
					UtilityFile
							.createWriteDataFile(getEyesFileName(properties),
									descriptorOperation + "|" + properties.getHostName() + "|3|"
											+ properties.getHostAddress() + "|" + storeNumber + "|ERR|"
											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(
													new Date())
											+ "|Error al recibir el archivo: " + fileName + ".\n",
									true);
				}
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						descriptorOperation + "" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ storeNumber + "|WAR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al recibir los datos del archivo.\n",
						true);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				storeNumber = (String) frame.getHeader().get(3);
				if (storeNumber == "") storeNumber="000";
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						descriptorOperation + "|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ storeNumber + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
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
				+ ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

}
