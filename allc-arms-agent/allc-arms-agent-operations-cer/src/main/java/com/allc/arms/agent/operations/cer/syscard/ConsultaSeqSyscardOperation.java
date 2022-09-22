package com.allc.arms.agent.operations.cer.syscard;

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
import com.ibm.OS4690.POSFile;
import com.ibm.OS4690.RandomAccessFile4690;

public class ConsultaSeqSyscardOperation extends AbstractOperation{
	
	private static Logger logger = Logger.getLogger(ConsultaSeqSyscardOperation.class);
	protected POSFile posFileSeqWriter = null;
	protected RandomAccessFile4690 randSeqRead = null;
	protected String syscardSeqFileName;

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		logger.info("Consulta Secuencia Syscard Operation iniciada...");

		try {
			Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_SQ_SYS_TND|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Consultando sequencial para Syscard.\n", true);
			
			syscardSeqFileName = properties.getObject("sequenceSyscard.file");
			int sequence =  getSequence()+1;
			posFileSeqWriter = new POSFile(syscardSeqFileName, "rw", POSFile.SHARED_READ_WRITE_ACCESS);
			String valorPosicion = Util.rpad(String.valueOf(sequence), ArmsAgentConstants.Communication.SPACE, 20)
					+ ArmsAgentConstants.Communication.CRLF;
			posFileSeqWriter.write(valorPosicion.getBytes(), 0, POSFile.FROM_START_OF_FILE, POSFile.FLUSH,
					valorPosicion.length());
			try {
				posFileSeqWriter.closeFull();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			try {
				randSeqRead.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			StringBuilder sb = new StringBuilder(frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + Integer.valueOf(sequence).toString());
			logger.info("Respuesta a enviar:"+sb.toString());
			if(socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")))){
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_SQ_SYS_TND|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Secuencial: "+sequence+" enviado.\n", true);
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_SQ_SYS_TND|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties), "CONS_SQ_SYS_TND|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al Consultar el secuencial para Syscard.\n", true);
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

	/**
	 * Obtiene el secuencial a utilizar para enviar a Syscard
	 * 
	 * @return Secuencial
	 */
	private int getSequence() {
		int seq = 0;
		try {

			if (!Files.fileExists4690(syscardSeqFileName)) {
				Files.creaEscribeDataArchivo4690(syscardSeqFileName, Util.rpad(ArmsAgentConstants.Communication.CERO, ArmsAgentConstants.Communication.SPACE, 20)
					+ ArmsAgentConstants.Communication.CRLF, false);
			}

			randSeqRead = new RandomAccessFile4690(syscardSeqFileName, "r");
			seq = readSeq();
			logger.info("Sequence: "+seq);
			if (seq > 999998) {
				/**
				 * si el secuencial es 999999 se debe reiniciar a 0
				 **/
				seq = 0;
				logger.info("getSequence: Se alcanzó el secuencial máximo: 999999, se reinicia a 0.");
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return seq;
	}
	
	/**
	 * Obtain the file offset
	 * 
	 * @param fileName
	 *            File that contains the offset
	 * @return Position
	 */
	private int readSeq() {
		int seq;
		String data;
		try {
			data = randSeqRead.readLine();
			randSeqRead.seek(0);

			if (null == data)
				seq = 0;
			else
				try {
					seq = Integer.parseInt(data.replaceAll(" ", ""));
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					seq = -1;
				}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			seq = -1;
		}
		return seq;
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
