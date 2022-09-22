package com.allc.arms.server.operations.cer.codopera;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class UpdateCodeOperaOperation extends AbstractOperation {

	
	protected static Logger log = Logger.getLogger(UpdateCodeOperaOperation.class);
	protected boolean isEnd = false;
	protected boolean finished = false;
	private int sleepTime;

	public void initialize(PropFile properties) {
		try {
			sleepTime = properties.getInt("updateCodOpera.sleeptime");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		log.info("Iniciando Update CodOpera Operation...");
		StringBuilder msg = new StringBuilder();
		initialize(properties);
		try {
			UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_CODOPERA_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Procesando Archivo codOpera: "+frame.getBody().get(0)+".\n", true);
			String codOperaFile = (String) frame.getBody().get(0);
			log.info("Archivo: " + codOperaFile);
			String storeCode = codOperaFile.split("\\.")[1];
			//log.info("Store: " + storeCode);
			String folder4690 = properties.getObject("updateCodOpera.in.folder.path")+File.separator+storeCode+File.separator+properties.getObject("updateCodOpera.in.folder.name");
			//log.info("Ruta: " + folder4690);
			File file4690 = new File(folder4690, codOperaFile);
			boolean procesado = file4690.delete();
			if (procesado && !isEnd) {
				log.info("Archivo: " + codOperaFile + " procesado correctamente.");
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("0");
				String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
				log.info(tmp);
				if(socket.writeDataSocket(tmp)){
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_CODOPERA_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivo codOpera: "+frame.getBody().get(0)+" procesado.\n", true);
				} else
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_CODOPERA_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
			} else {
				log.error("Error al procesar el archivo.");
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("-1");
				String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
				log.info(tmp);
				if(socket.writeDataSocket(tmp)){
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_CODOPERA_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el archivo codOpera: "+frame.getBody().get(0)+".\n", true);
				} else
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_CODOPERA_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_CODOPERA_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar el archivo codOpera: "+frame.getBody().get(0)+".\n", true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
		finished = true;
		return false;
	}

	@Override
	public boolean process(ConnPipeServer arg0, Frame arg1, PropFile arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo Update CodOpera Operation...");
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
		log.info("Finaliza la Operacion Update CodOpera.");
		return false;
	}
	
	private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
}
