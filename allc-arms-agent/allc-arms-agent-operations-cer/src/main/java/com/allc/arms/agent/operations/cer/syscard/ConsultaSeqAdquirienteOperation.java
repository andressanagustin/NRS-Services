package com.allc.arms.agent.operations.cer.syscard;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.keyed.KeyedFileBean;
import com.allc.arms.utils.keyed.KeyedFileMethods;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.KeyedFile;
import com.ibm.OS4690.POSFile;
import com.ibm.OS4690.RandomAccessFile4690;

public class ConsultaSeqAdquirienteOperation extends AbstractOperation {

	private static Logger logger = Logger.getLogger(ConsultaSeqAdquirienteOperation.class);
	protected POSFile posFileSeqWriter = null;
	protected RandomAccessFile4690 randSeqRead = null;
	protected String syscardSeqFileName;
	KeyedFileBean keyedFileBean = new KeyedFileBean();

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		logger.info("Consulta Secuencia Adquiriente Operation iniciada...");
		boolean result = false;
		int sequence = 0;
		try {
			Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
					"CONS_SQ_ADQ_TND|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Consultando sequencial para Arquiente.\n",
					true);

			String adquiriente = StringUtils.leftPad((String) frame.getBody().get(0), 2, "0");

			keyedFileBean.setPathAndFileName("C:/ADX_UDT1/SEQBYADQ.DAT");
			keyedFileBean.setMode("rw");
			keyedFileBean.setAccess(KeyedFile.SHARED_READ_WRITE_ACCESS);
			keyedFileBean.setFileType(KeyedFile.MIRRORED_FILE);
			keyedFileBean.setDistributionMethod(KeyedFile.COMPOUND_FILE);
			keyedFileBean.setKeyLength(2);
			keyedFileBean.setRecordSize(8);
			if (KeyedFileMethods.openFile(keyedFileBean))
				result = true;
			if (result) {
				sequence = getSequence(adquiriente) + 1;
				boolean actualizado = (boolean) writeSeq(adquiriente, StringUtils.leftPad(String.valueOf(sequence), 6, "0"));
				KeyedFileMethods.closeFile(keyedFileBean);
				if (actualizado)
					logger.info("Archivo de secuencias por adquiriente actualizado en forma correcta.");
				else
					logger.info("Error al actualizar archivo de secuencias por adquiriente.");
			}
			StringBuilder sb = new StringBuilder(frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP
					+ Integer.valueOf(sequence).toString());
			logger.info("Respuesta a enviar:" + sb.toString());
			if (socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(),
					properties.getInt("serverSocket.quantityBytesLength")))) {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
						"CONS_SQ_ADQ_TND|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|END|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Secuencial: " + sequence + " enviado.\n",
						true);
			} else
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
						"CONS_SQ_ADQ_TND|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|WAR|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|No se pudo enviar la respuesta.\n",
						true);
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
						"CONS_SQ_ADQ_TND|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|ERR|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al Consultar el secuencial para Syscard.\n",
						true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
			logger.error(e.getMessage(), e);
		}

		return false;
	}

	private String getEyesFileName(PropFile properties) {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	public boolean writeSeq(String key, String pointerData) {
		boolean result = false;
		int numberBytesWritten = 0;
		try {
			byte[] pointerRecord = new byte[keyedFileBean.getRecordSize()];
			logger.info("key: " + key);
			logger.info("pointerData: " + pointerData);
			
			System.arraycopy(key.getBytes(), 0, pointerRecord, 0, key.length());

			System.arraycopy(pointerData.getBytes(), 0, pointerRecord, 2, pointerData.length());
			numberBytesWritten = keyedFileBean.getKeyedFile().write(pointerRecord, KeyedFile.NO_UNLOCK,
					KeyedFile.NO_HOLD);

			if (numberBytesWritten == keyedFileBean.getKeyedFile().getRecordSize())
				result = true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	public String readSeq(String key) {
		try {
			int recordSize = keyedFileBean.getKeyedFile().getRecordSize();
			byte[] pointerRecord = new byte[recordSize];
			logger.info("Adquiriente: " + key);
			System.arraycopy(key.getBytes(), 0, pointerRecord, 0, key.length());
			keyedFileBean.getKeyedFile().read(pointerRecord, 1);
			String record = new String(pointerRecord);
			return record;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return "0";
	}

	private int getSequence(String adquiriente) {
		int seq = 0;
		try {
			String record = readSeq(adquiriente);
			seq = Integer.valueOf(record.substring(record.length()-6)).intValue();
			logger.info("Sequence: " + seq);
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

	public boolean process(ConnPipeServer arg0, Frame arg1, PropFile arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
