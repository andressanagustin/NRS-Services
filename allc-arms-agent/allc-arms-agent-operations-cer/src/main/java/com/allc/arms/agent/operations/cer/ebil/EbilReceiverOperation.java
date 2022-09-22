package com.allc.arms.agent.operations.cer.ebil;

import java.util.Date;

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

public class EbilReceiverOperation extends AbstractOperation {
	
	protected static Logger log;
	protected String descriptorOperation = "EBIL_RCV_O";
	
	protected void initialize(){
		log = Logger.getLogger(EbilReceiverOperation.class);
	}

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		initialize();
		try {
			Files.creaEscribeDataArchivo4690(getEyesFileName(properties), descriptorOperation+"|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Iniciando recepción de archivo.\n", true);

			String fileName = (String) frame.getBody().get(0);
			long fileSize = (Long.valueOf((String)frame.getBody().get(1))).longValue();
			boolean receiveFile = false;
			StringBuilder msg = new StringBuilder();
			File4690 file = null;
			if (!fileName.trim().isEmpty() && fileSize > 0) {
				file = new File4690(fileName);
				log.info("File: "+file.getAbsolutePath());
				if(!file.exists())
	    			file.createNewFile();
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("0");
				receiveFile = true;
			} else {
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("1");
			}
			String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
			socket.writeDataSocket(tmp);
			
			if(receiveFile){
				byte[] receivedData;
				long totalBytesToRead = fileSize;
				FileOutputStream4690 fos = null;
				if(file != null)
					fos = new FileOutputStream4690(file, true);
				
				do {
					int bytesToRead = totalBytesToRead > 8192 ? 8192 : (int) totalBytesToRead;
					log.info("Bytes a leer: " + bytesToRead);
					receivedData = socket.readDataSocketToBytesArray(bytesToRead);
					log.info("Received data: " + receivedData.toString());
					fos.write(receivedData, 0, receivedData.length);
					totalBytesToRead = totalBytesToRead - bytesToRead;
				} while (receivedData != null && totalBytesToRead > 0);
				fos.close();
				if((new File4690(fileName)).length() == fileSize){
					log.info("Archivo recibido correctamente");
					socket.writeDataSocket(tmp);
					Files.creaEscribeDataArchivo4690(getEyesFileName(properties), descriptorOperation+"|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|PRC|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo recibido correctamente.\n", true);
				} else {
					log.info("Error al recibir el archivo");
					Files.creaEscribeDataArchivo4690(getEyesFileName(properties), descriptorOperation+"|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al recibir el archivo: "+fileName+".\n", true);
				}
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), descriptorOperation+""+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al recibir los datos del archivo.\n", true);
				
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), descriptorOperation+"|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al recibir el archivo.\n", true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
		return false;
	}
	
	private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	public boolean process(ConnPipeServer arg0, Frame arg1, PropFile arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
