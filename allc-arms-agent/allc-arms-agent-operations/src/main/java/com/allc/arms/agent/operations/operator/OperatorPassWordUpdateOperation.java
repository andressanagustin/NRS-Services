package com.allc.arms.agent.operations.operator;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.ParamsUtils;
import com.allc.arms.utils.keyed.KeyedFileBean;
import com.allc.arms.utils.keyed.KeyedFileMethods;
import com.allc.arms.utils.keyed.Util4690;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.FlexosException;
import com.ibm.OS4690.KeyedFile;
import com.ibm.OS4690.security.AuthorizationRecord;
import com.ibm.OS4690.security.EnhancedSecurity;
import com.ibm.OS4690.security.EnhancedSecuritySession;

public class OperatorPassWordUpdateOperation extends AbstractOperation {

	protected static Logger log = Logger.getLogger(OperatorPassWordUpdateOperation.class);
	protected boolean finished = false;
	protected KeyedFileBean keyedFileBean = new KeyedFileBean();
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);;
	protected String operatorKeyedFileName;

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		log.info("OperatorPassWordUpdate Operation iniciada.");
		String message;
		String operatorID = null;
		String claveSupervisor = null;
		//String claveSupervisorArmada = null;
		EnhancedSecuritySession ess = null;

		try {
			operatorID = String.valueOf(frame.getBody().get(0));
			claveSupervisor = (String) frame.getBody().get(1);
			
			//log.info("Clave antes de armar: " + claveSupervisor);

			Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
					"OPER_SUP_UPD_O|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Actualizando clave del operadorSupervisor: " + operatorID + ".\n",
					true);

			EnhancedSecurity ass = new EnhancedSecurity();
			try {
				log.info("Inicia el proceso de actualizaci�n de password");
				//claveSupervisorArmada = construirClaveSupervisor(claveSupervisor);
				//log.info("Clave armada: " + claveSupervisorArmada);
				
				if (ass.isAvailable()) {

					log.info("EnhancedSecurity is Available");
					ess = ass.startSession(properties.getObject("operator.master.user"),
							ParamsUtils.desencriptar(properties.getObject("operator.master.password")));
					AuthorizationRecord ar = null;

					if (claveSupervisor != null) {
						ar = ess.getRecord(operatorID);
						ar.lock();
					}
					log.debug("UpdatePassword-Inicio.");
					ar.setPassword(claveSupervisor, false);
					log.debug("UpdatePassword-Finalizado");

					log.info("Finaliza el proceso de actualizaci�n de password con seguridad mejorada activada.");
					
					ess.commit();

				} else {
					log.info("EnhancedSecurity No Available");
					boolean result = registrarPasswordEAMOPERA(operatorID, claveSupervisor);
					if(result)
						log.info("Proceso de actualizacion del EAMOPERA finalizado con �xito.");
					else{
						log.info("Proceso de actualizacion del EAMOPERA con error.");
						return false;
					}
				}
			} catch (FlexosException e) {
				log.error(e.getMessage(), e);
				if (ess != null) {
					try {
						log.info("Cancelando session.");
						ess.cancel();
					} catch (Exception ex) {
						log.error(ex.getMessage(), ex);
					}
				}
				return false;
			}
			Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
					"OPER_SUP_UPD_O|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|END|"
							+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "| Clave del OperadorSupervisor: " + operatorID + " actualizada.\n",
					true);
			
			message = "0";
			StringBuffer sb = new StringBuffer(frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + message);
			socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")));
			
			
		} catch (Exception e) {
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(properties),
						"OPER_SUP_UPD_O|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|ERR|"
								+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al actualizar la clave del operadorSupervisor: " + operatorID + ".\n",
						true);
				message = "1";
				StringBuffer sb = new StringBuffer(frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + message);
				socket.writeDataSocket(Util.addLengthStartOfString(sb.toString(), properties.getInt("serverSocket.quantityBytesLength")));
				
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
			log.error(e.getMessage(), e);
		}
		return false;

	}

	private String getEyesFileName(PropFile properties) {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	public boolean process(ConnPipeServer arg0, Frame arg1, PropFile arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public String construirClaveSupervisor(String claveSupervisor) {

		log.info("Comienza metodo de construcci�n de clave.");

		String claveSupFinalArmada = "";

		if (claveSupervisor != null && claveSupervisor.length() >= 7) {
			
			claveSupFinalArmada = claveSupFinalArmada.concat(String.valueOf(claveSupervisor.charAt(2)))
					.concat(String.valueOf(claveSupervisor.charAt(5))).concat(String.valueOf(claveSupervisor.charAt(0)))
					.concat(String.valueOf(claveSupervisor.charAt(3))).concat(String.valueOf(claveSupervisor.charAt(6)))
					.concat(String.valueOf(claveSupervisor.charAt(1))).concat(String.valueOf(claveSupervisor.charAt(4)))
					.concat(String.valueOf(claveSupervisor.charAt(7)));

			log.info("Finaliza metodo de construcci�n de clave.");
			return claveSupFinalArmada;

		} else {
			log.info("Finaliza metodo de construcci�n de clave. Clave = null");
			return null;
		}
	}

	public boolean init() {
		boolean result = false;
		try {

			operatorKeyedFileName = (String) properties.getObject("operator.keyedFile.name");
			keyedFileBean.setPathAndFileName(operatorKeyedFileName);
			keyedFileBean.setMode("rw");
			keyedFileBean.setAccess(KeyedFile.SHARED_READ_WRITE_ACCESS);
			keyedFileBean.setFileType(KeyedFile.MIRRORED_FILE);
			keyedFileBean.setDistributionMethod(KeyedFile.COMPOUND_FILE);
			keyedFileBean.setKeyLength(5);
			keyedFileBean.setRecordSize(properties.getInt("operator.keyedFile.record.length"));
			if (KeyedFileMethods.openFile(keyedFileBean))
				result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	public boolean registrarPasswordEAMOPERA(String operadorID, String claveArmada) {

		
		byte[] field;
		init();
		try {
			log.info("Comienza proceso de actualizacion de la password en el maestro de Operadores");
			if (0 != properties.getInt("operator.password.encripted.flag")) {
				if (keyedFileBean.getKeyedFile() == null)
					return false;
				int recordSize = keyedFileBean.getKeyedFile().getRecordSize();
				byte[] operatorRecord = new byte[recordSize];
				byte[] key = Util4690.pack(StringUtils.leftPad(operadorID, 10, ArmsAgentConstants.Communication.CERO));
				System.arraycopy(key, 0, operatorRecord, 0, key.length);
				keyedFileBean.getKeyedFile().read(operatorRecord, 1);

				field = Util4690.pack(StringUtils
						.right(StringUtils.leftPad(claveArmada, 8, ArmsAgentConstants.Communication.CERO), 8));
				
				System.arraycopy(field, 0, operatorRecord, 5, field.length);
				keyedFileBean.getKeyedFile().closeFull();
				log.info("Finaliza proceso de actualizacion de la password en el maestro de Operadores");
				return true;
			}
			

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
		return false;
	}

}
