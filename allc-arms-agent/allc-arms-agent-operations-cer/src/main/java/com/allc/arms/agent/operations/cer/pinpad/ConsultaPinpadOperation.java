package com.allc.arms.agent.operations.cer.pinpad;

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
import com.lectora.cnx.EnvioSeg;

public class ConsultaPinpadOperation extends AbstractOperation{
	
	private static Logger logger = Logger.getLogger(ConsultaPinpadOperation.class);
	
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		logger.info("Consulta Pinpad Operation iniciada...");

		try {
			Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_PINP_OP|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Consultando trama para pinpad.\n", true);
			String tipo = (String) frame.getBody().get(0);
			String mensaje = (String) frame.getBody().get(1);
			
			String rta = null;
			if(tipo.equals("0")){
				rta = EnvioSeg.Genera_Componente(mensaje, 1);
			} else if(tipo.equals("1")){
				rta = Integer.valueOf(EnvioSeg.Validar_Seguridad(mensaje, 1)).toString();
			}
			String codRta = "0";
			if(rta != null)
				codRta = "1";
			StringBuilder sb = new StringBuilder(frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + codRta);
			if(rta != null)
				sb.append(ArmsAgentConstants.Communication.FRAME_SEP).append(rta);
			logger.info("Respuesta a enviar:"+sb.toString());
			if(socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")))){
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_PINP_OP|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Trama para pinpad enviada.\n", true);
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_PINP_OP|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_PINP_OP|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al Consultar la trama para pinpad.\n", true);
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
