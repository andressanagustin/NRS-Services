/**
 * 
 */
package com.allc.arms.server.operations.cer.suspTrans;

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

/**
 * @author gustavo
 *
 */
public class GenerateSuspTransOperation extends AbstractOperation {
	protected static Logger log = Logger.getLogger(GenerateSuspTransOperation.class);
	protected boolean isEnd = false;
	protected boolean finished = false;
	private int sleepTime;

	public void initialize(PropFile properties) {
		try {
			sleepTime = properties.getInt("generateSuspTrans.sleeptime");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.allc.core.operation.AbstractOperation#shutdown(long)
	 */
	@Override
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo Generate SuspTrans Operation...");
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
		log.info("Finalizó la Operación de Generación de Transacciones suspendidas.");
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.allc.core.operation.AbstractOperation#process(com.allc.comm.socket.ConnSocketServer, com.allc.comm.frame.Frame,
	 * com.allc.properties.PropFile)
	 */
	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		log.info("Iniciando Generate SuspTrans Operation...");
		StringBuilder msg = new StringBuilder();
		initialize(properties);
		try {
			UtilityFile.createWriteDataFile(getEyesFileName(properties), "GEN_SUTRX_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Generando Transacción Suspendida: "+frame.getBody().get(0)+".\n", true);
			String suspTransFile = (String) frame.getBody().get(0);
			String storeCode = suspTransFile.split("\\.")[1];
			String folder4690 = properties.getObject("generateSuspTrans.in.folder.path")+File.separator+storeCode+File.separator+properties.getObject("generateSuspTrans.in.folder.name");
			File file4690 = new File(folder4690, suspTransFile);
			boolean procesado = file4690.delete();
			if (procesado && !isEnd) {
				log.info("Archivo: " + suspTransFile + " procesado correctamente.");
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("0");
				String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
				log.info(tmp);
				if(socket.writeDataSocket(tmp)){
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "GEN_SUTRX_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Transacción Suspendida: "+frame.getBody().get(0)+" generada.\n", true);
				} else
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "GEN_SUTRX_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
			} else {
				log.error("Error al procesar el archivo.");
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("-1");
				String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
				log.info(tmp);
				if(socket.writeDataSocket(tmp)){
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "GEN_SUTRX_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar la Transacción Suspendida: "+frame.getBody().get(0)+".\n", true);
				} else
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "GEN_SUTRX_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "GEN_SUTRX_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al generar Transacción Suspendida: "+frame.getBody().get(0)+".\n", true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
		finished = true;
		return false;
	}
	
	private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.allc.core.operation.AbstractOperation#process(com.allc.comm.pipe.ConnPipeServer, com.allc.comm.frame.Frame,
	 * com.allc.properties.PropFile)
	 */
	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}

}
