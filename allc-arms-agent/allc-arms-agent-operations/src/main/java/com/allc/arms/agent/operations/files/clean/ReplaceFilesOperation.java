package com.allc.arms.agent.operations.files.clean;

import java.util.Date;

import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.Files;
import com.allc.files.helper.FilesHelper;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class ReplaceFilesOperation extends AbstractOperation {

	private static Logger logger = Logger.getLogger(ReplaceFilesOperation.class);

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		logger.info("Replace Files Operation iniciada...");
		String codRta = "0";
		String rta = null;
		try {
			Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
					"RPL_FILE_O|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando reemplazo de archivos.\n",
					true);

			String nameFile = (String) frame.getBody().get(0);
			String pathOri = (String) frame.getBody().get(1);
			String pathDes = (String) frame.getBody().get(2);

			try {
				FilesHelper.copyFile4690(pathOri, pathDes, nameFile, nameFile);
			} catch (Exception e1) {
				codRta = "1";
				logger.error(e1.getMessage(), e1);
			}

			StringBuilder sb = new StringBuilder(
					frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + codRta);
			
			logger.info("Respuesta a enviar:" + sb.toString());
			
			if(socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")))){
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "RPL_FILE_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Respuesta de reemplazo de archivo enviada.\n", true);
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "RPL_FILE_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
			

		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
						"RPL_FILE_O|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|ERR|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error durante operación de reemplazo de archivos.\n",
						true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
			logger.error(e.getMessage(), e);
		}

		return false;
	}

	public boolean process(ConnPipeServer arg0, Frame arg1, PropFile arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	private String getEyesFileName(PropFile properties) {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

}
