package com.allc.arms.agent.operations.cer.ebil;

import java.io.File;
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

public class UpdateEbilFileOperation extends AbstractOperation {
	
	private static Logger logger = Logger.getLogger(UpdateEbilFileOperation.class);
	
	
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		
		logger.info("UpdateEbilFileOperation iniciada...");

		try {
			Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "UPD_EBIL_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Iniciando borrado del archivo: "+frame.getBody().get(0)+".\n", true);
			String filename = (String) frame.getBody().get(0);
			String message = "1";
			if(filename != null && filename.length() > 0) {
				logger.info("Directorio: "+properties.getObject("updateEbil.in.folder"));
				File4690 ebilFile = new File4690(properties.getObject("updateEbil.in.folder")+File.separator+filename);
				logger.info("Borrando archivo: "+ebilFile.getAbsolutePath()+File.separator+ebilFile.getName());
				if(ebilFile != null && ebilFile.exists() && ebilFile.delete()){
					message = "0";
					
				}
			}
			StringBuilder sb = new StringBuilder(frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + message);
			String trama = Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength"));
			logger.info("Respuesta a enviar: " + trama);
			if(socket.writeDataSocket(trama)){
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "UPD_EBIL_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo: "+frame.getBody().get(0)+" borrado.\n", true);
			} else {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "UPD_EBIL_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No hay conexi??n con el server para informar el procesamiento del archivo: "+frame.getBody().get(0)+".\n", true);
			}
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "UPD_EBIL_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al borrar el archivo: "+frame.getBody().get(0)+".\n", true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
			logger.error(e.getMessage(), e);
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
