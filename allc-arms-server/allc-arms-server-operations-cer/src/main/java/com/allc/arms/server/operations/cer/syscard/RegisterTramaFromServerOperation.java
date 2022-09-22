package com.allc.arms.server.operations.cer.syscard;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.server.persistence.syscard.SyscardFrame;
import com.allc.arms.server.persistence.syscard.SyscardFrameDAO;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class RegisterTramaFromServerOperation extends AbstractOperation{

	private Session sesion;
	public boolean isEnd = false;
	protected boolean finished = false;
	static Logger log = Logger.getLogger(RegisterTramaFromServerOperation.class);
	protected ConnSocketClient socketClient;
	protected SyscardFrameDAO syscardFrameDAO = new SyscardFrameDAO();
	
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		StringBuilder msg = new StringBuilder(frame.getHeaderStr());
		try {
			UtilityFile.createWriteDataFile(getEyesFileName(properties), "REG_TR_FRM_SRVR_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|STR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Iniciando registro de tramas del Server.\n", true);
			if (frame.getBody().size() > 0) {
				openSession();
				updateFrame(frame.getBody());
				
				msg.append(frame.getSeparator()).append("0");
				String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
				log.info(tmp);
				if(socket.writeDataSocket(tmp)){
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "REG_TR_FRM_SRVR_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Tramas del server procesadas.\n", true);
				} else
					UtilityFile.createWriteDataFile(getEyesFileName(properties), "REG_TR_FRM_SRVR_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("-1");
				String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
				log.info(tmp);
				socket.writeDataSocket(tmp);
			} catch (Exception ex) {
				log.error(e.getMessage(), e);
			}
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "REG_TR_FRM_SRVR_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|ERR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al procesar tramas del server.\n", true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
		finally {
			sesion.close();
			sesion = null;
		}
		finished = true;
		return false;
	}
	
	private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	private void updateFrame(List data) throws Exception {
		try {
			SyscardFrame syscardFrame =  new SyscardFrame();
			for(int i = 0; i < data.size(); i++){
				String campo = (String) data.get(i);
				
				if(campo.startsWith("MT="))
					syscardFrame.setMessageType(campo.substring(3));
				else if(campo.startsWith("BM1="))
					syscardFrame.setBitMap1(campo.substring(4));
				else if(campo.startsWith("BM2="))
					syscardFrame.setBitMap2(campo.substring(4));
				else if(campo.startsWith("PAC="))
					syscardFrame.setPrimaryAccNum(campo.substring(4));
				else if(campo.startsWith("PC="))
					syscardFrame.setProcessingCode(campo.substring(3));
				else if(campo.startsWith("AM="))
						syscardFrame.setTrxAmount(campo.substring(3));
				else if(campo.startsWith("STAN="))
					syscardFrame.setSystemTraceAuditNum(campo.substring(5));
				else if(campo.startsWith("LTT="))
					syscardFrame.setLocalTrxTime(campo.substring(4));
				else if(campo.startsWith("LTD="))
					syscardFrame.setLocalTrxDate(campo.substring(4));
				else if(campo.startsWith("ED="))
					syscardFrame.setExpirationDate(campo.substring(3));
				else if(campo.startsWith("PEM="))
					syscardFrame.setPosEntryMode(campo.substring(4));
				else if(campo.startsWith("NII="))
					syscardFrame.setNetworkIntID(campo.substring(4));
				else if(campo.startsWith("PCC="))
					syscardFrame.setPosConditionCode(campo.substring(4));
				else if(campo.startsWith("T2D="))
					syscardFrame.setTrack2(campo.substring(4));
				else if(campo.startsWith("RRN="))
					syscardFrame.setRetrievalRefNum(campo.substring(4));
				else if(campo.startsWith("AN="))
					syscardFrame.setAutorizationNum(campo.substring(3));
				else if(campo.startsWith("RC="))
					syscardFrame.setResponseCode(campo.substring(3));
				else if(campo.startsWith("TI="))
					syscardFrame.setTerminalID(campo.length() > 11 ? campo.substring(3, 11) : campo.substring(3));
				else if(campo.startsWith("MI="))
					syscardFrame.setMerchantID(campo.length() > 18 ? campo.substring(3, 18) : campo.substring(3));
				else if(campo.startsWith("T1D="))
					syscardFrame.setTrack1(campo.length() > 84 ? campo.substring(4, 84) : campo.substring(4));
				else if(campo.startsWith("NLA="))
					syscardFrame.setNumLoteAct(campo.length() > 10 ? campo.substring(4, 10) : campo.substring(4));
				else if(campo.startsWith("AD="))
					syscardFrame.setAdditionalData105(campo.length() > 21 ? campo.substring(3, 21) : campo.substring(3));
				else if(campo.startsWith("AD2="))
					syscardFrame.setAdditionalData112(campo.length() > 504 ? campo.substring(4, 504) : campo.substring(4));
				else if(campo.startsWith("AD3="))
					syscardFrame.setAdditionalData114(campo.length() > 6 ? campo.substring(4, 6) : campo.substring(4));
				else if(campo.startsWith("RFNU="))
					syscardFrame.setReservedNatUse(campo.length() > 13 ? campo.substring(5, 13) : campo.substring(5));
				else if(campo.startsWith("AD4="))
					syscardFrame.setAdditionalData120(campo.length() > 504 ? campo.substring(4, 504) : campo.substring(4));
				else if(campo.startsWith("AD5="))
					syscardFrame.setAdditionalData122(campo.length() > 704 ? campo.substring(4, 704) : campo.substring(4));
				else if(campo.startsWith("RFPU="))
					syscardFrame.setReservedPrivUse(campo.length() > 505 ? campo.substring(5, 505) : campo.substring(5));
				else if(campo.startsWith("RV="))
					syscardFrame.setReserved(campo.length() > 503 ? campo.substring(3, 503) : campo.substring(3));
			}
			log.info("SyscardFrame:"+syscardFrame.toString());
			syscardFrameDAO.insertSyscardFrame(sesion, syscardFrame);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void openSession() {
		while (sesion == null) {
			try {
				sesion = HibernateSessionFactoryContainer.getSessionFactory("Eyes").openSession();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				sesion = null;
			}
			if (sesion == null)
				try {
					log.error("OCURRIO UN ERROR AL CREAR LA SESION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
		}
	}
	
	public boolean shutdown(long timeToWait) {
		isEnd = true;
		long startTime = Calendar.getInstance().getTimeInMillis();
		log.info("Deteniendo RegisterTramaFromServerOperation...");
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
		log.info("Finalizo la Operacion de Registro de Tramas desde el Server a Syscard.");
		return true;
	}

	@Override
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		// TODO Auto-generated method stub
		return false;
	}


}
