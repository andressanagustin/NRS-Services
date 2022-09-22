package com.allc.arms.agent.operations.cer.syscard;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
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
import com.ibm.OS4690.RandomAccessFile4690;

/**
 * Operación encagada de guardar la trama de respuesta de Syscard para una consulta de cliente.
 * 
 * @author gustavo
 *
 */
public class ConsultaSyscardMsgOperation extends AbstractOperation {
	
	private static Logger logger = Logger.getLogger(ConsultaSyscardMsgOperation.class);
	protected SyscardMsgKeyed keyed = new SyscardMsgKeyed();

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		logger.info("Consultar Mensaje Syscard Operation iniciada...");

		try {
			Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CN_MSG_SYS_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Guardando trama enviada a Syscard.\n", true);

			String terminal = (String) frame.getBody().get(0);
			String trans = (String) frame.getBody().get(1);
			
//			keyed.init(properties.getObject("syscard.pointer.file.name"));

			keyed.init("C:/ADX_UDT1/LXSEEKSU.DAT");
			String key = StringUtils.leftPad(terminal, 4, ArmsAgentConstants.Communication.CERO) + StringUtils.leftPad(trans, 4, ArmsAgentConstants.Communication.CERO);
			String pointer = keyed.readPointer(key);
			String msg = "1";
			File4690 msgFile = new File4690("F:/allc_pgm/ArmsAgent/syscardRecMsg.dat");
			if(pointer != null && pointer.trim().length() > 0 && msgFile.exists()) {
				int puntero = Integer.valueOf(pointer).intValue();
//				RandomAccessFile randFileRead = new RandomAccessFile(getFileName(properties), "r");
				RandomAccessFile4690 randFileRead = new RandomAccessFile4690("F:/allc_pgm/ArmsAgent/syscardRecMsg.dat", "r");
				randFileRead.seek(puntero);
				byte [] byteArray = new byte[3072];
				randFileRead.read(byteArray, 0, 3072);
				randFileRead.close();
				msg = "0" + ArmsAgentConstants.Communication.FRAME_SEP + (new String(byteArray)).trim();
			}
			keyed.closure();
			StringBuilder sb = new StringBuilder(frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + msg);
			String msgToSend = Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength"));
			logger.info("Respuesta a enviar:"+msgToSend);
			if(socket.writeDataSocket(msgToSend)){
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CN_MSG_SYS_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Trama guardada.\n", true);
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CN_MSG_SYS_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo guardar la trama.\n", true);
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CN_MSG_SYS_O|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al guardar la trama.\n", true);
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
	
	private String getFileName(PropFile properties){
		return properties.getObject("syscard.msg.file.name")+".DAT";
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
