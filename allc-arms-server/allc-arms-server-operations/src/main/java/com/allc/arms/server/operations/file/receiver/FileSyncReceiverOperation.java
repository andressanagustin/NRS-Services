package com.allc.arms.server.operations.file.receiver;

//  com.allc.arms.server.operations.file.receiver.FileSyncReceiverOperation 
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream; 
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
 
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.arms.utils.file.UtilityFile;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation; 
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
 

public class FileSyncReceiverOperation extends AbstractOperation {

	protected static Logger log;
	protected String descriptorOperation = "FILE_SEND_O";
	protected ConnSocketClient socketClient;
	protected static Pattern p = Pattern.compile(ArmsServerConstants.Communication.REGEX);
	protected boolean borrar = false;
	protected boolean anexarDatos = false;

	public boolean shutdown(long timeToWait) {
		// TODO Auto-generated method stub
		return false;
	}

	protected void initialize() {
		log = Logger.getLogger(FileSyncReceiverOperation.class);
		log.info("Inicia operacion de recibir Sync de AEF");
	}

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {

		initialize();
		try {

			String fileName = (String) frame.getBody().get(0);
			long fileSize = (Long.valueOf((String) frame.getBody().get(1))).longValue();
			log.info("Me llego el archivo " + fileName + " de tamanio " + fileSize);

			StringBuilder msgRespuesta = new StringBuilder();
			boolean receiveFile = false;
			borrar = false;
			long bytesAEnviar = 0;
			if (UtilityFile.fileExists(fileName)) {
				int tamanioArchivo = UtilityFile.readDataFile(fileName).length();
				log.info("Archivo si existe, pesa: " + tamanioArchivo);
				if (tamanioArchivo < fileSize) {
					log.info("Caso 2 envia D cantidad de Bytes");
					bytesAEnviar = fileSize - tamanioArchivo;
					msgRespuesta.append(frame.getHeaderStr()).append(frame.getSeparator()).append("2")
							.append(frame.getSeparator()).append(bytesAEnviar);// 1 debe enviar bytes
					receiveFile = true;
					anexarDatos = true;

				} else if (tamanioArchivo == fileSize) {
					log.info("Caso 4, no envia datos");
					msgRespuesta.append(frame.getHeaderStr()).append(frame.getSeparator()).append("4")
							.append(frame.getSeparator()).append(0);// 0 no debe enviar bytes
					receiveFile = false;
				} else if (tamanioArchivo > fileSize) {
					log.info("Caso 3, archivo antiguo se debe sobreescribir archivo ");
					borrar = true;
					bytesAEnviar = fileSize;
					msgRespuesta.append(frame.getHeaderStr()).append(frame.getSeparator()).append("3")
							.append(frame.getSeparator()).append(fileSize);
					receiveFile = true;
				}

			} else {
				log.info("Caso 1, archivo no existe, debiera crearse");
				bytesAEnviar = fileSize;
				msgRespuesta.append(frame.getHeaderStr()).append(frame.getSeparator()).append("1")
						.append(frame.getSeparator()).append(fileSize);
				receiveFile = true;
			}
			log.info("msgRespuesta: " + msgRespuesta);
			String tmp = Util.addLengthStartOfString(msgRespuesta.toString(),
					properties.getInt("serverSocket.quantityBytesLength"));
			boolean pudoEnviar = socket.writeDataSocket(tmp); // log
			log.info("pudo enviar por socket respuesta? = " + pudoEnviar); // log

			// Recibe respuesta, los bytes del archivo
 
			if (receiveFile) {

				String fileNameTemp = "TMP-" + fileName.substring(fileName.lastIndexOf(File.separator) + 1);
				String fileTempPath = fileName.substring(0, fileName.lastIndexOf(File.separator) + 1);
				File fileTempPathDir = new File(fileTempPath);
				if(!fileTempPathDir.exists()){
					fileTempPathDir.mkdirs();
				}
				String fileTemp = fileTempPath + fileNameTemp;
				log.info("File-Temp: " + fileTemp);

				File fileExist = new File(fileTempPath + fileName.substring(fileName.lastIndexOf(File.separator) + 1));
				int cont = 0;
				while(fileExist.exists() && !anexarDatos){
					log.info("El archivo: "+fileExist.getName()+ (cont > 0 ? cont : "") +" existe en el destino y se renombrara sin anexar");
					cont++;
					File tempFile = new File(fileTempPath + fileName.substring(fileName.lastIndexOf(File.separator) + 1)+"_"+cont);
					if(tempFile.exists())
						fileExist = new File(fileTempPath + fileName.substring(fileName.lastIndexOf(File.separator) + 1)+"_"+cont);
					else
						fileExist.renameTo(new File(fileTempPath + fileName.substring(fileName.lastIndexOf(File.separator) + 1)+"_"+cont));
				}
				if(fileExist.exists() && anexarDatos){
					log.info("debo anexar al archivo original");
					fileTemp = fileName;
					 
				}
				byte[] receivedData;
				long totalBytesToRead = bytesAEnviar;
				log.info("totalBytesToRead: "+totalBytesToRead);
				
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileTemp,true));
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
						log.info("1");
//						UtilityFile.createWriteDataFile(getEyesFileName(properties),
//								descriptorOperation + "|" + properties.getHostName() + "|3|" + properties.getHostAddress()
//									+ "|" + frame.getHeader().get(3) + "|PRC|"
//									+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
//									+ "|Archivo recibido correctamente.\n",
//									true);
					}else{
						log.info("2");
//						UtilityFile.createWriteDataFile(getEyesFileName(properties),
//								descriptorOperation + "" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
//										+ frame.getHeader().get(3) + "|WAR|"
//										+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
//										+ "|No se pudo renombrar el archivo: " + fileNameTemp + ".\n",
//								true);
					}
				} else {
				if (!anexarDatos) {
					log.info("debiera borrar archivo: "+fileTemp);
					(new File(fileTemp)).delete();
				}
					log.info("Error al recibir el archivo");
//					UtilityFile
//							.createWriteDataFile(getEyesFileName(properties),
//									descriptorOperation + "|" + properties.getHostName() + "|3|"
//											+ properties.getHostAddress() + "|" + frame.getHeader().get(3) + "|ERR|"
//											+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(
//													new Date())
//											+ "|Error al recibir el archivo: " + fileName + ".\n",
//									true);
				}
			}

			// FIN recibe respuesta

		} catch (Exception e) {
			log.error(e.getMessage(), e);

		}
		return false;
	}

	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

}
