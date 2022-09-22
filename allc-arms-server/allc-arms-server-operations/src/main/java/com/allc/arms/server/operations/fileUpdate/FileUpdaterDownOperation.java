package com.allc.arms.server.operations.fileUpdate;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.allc.arms.server.params.ParamValue;
import com.allc.arms.server.params.ParamsDAO;
import com.allc.arms.server.persistence.HibernateSessionFactoryContainer;
import com.allc.arms.utils.ArmsServerConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.files.helper.UtilityFile;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;

public class FileUpdaterDownOperation extends AbstractOperation{
	
	private Logger logger = Logger.getLogger(FileUpdaterDownOperation.class);
	private File inFolder;
	protected String storeCode;
	private Session session = null;

	@Override
	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		
		StringBuilder msg = new StringBuilder();
		
		logger.info("Iniciando FileUpdaterDownOperation...");

		try {
			storeCode = (String) frame.getHeader().get(Frame.POS_SOURCE);
			String file = (String) frame.getBody().get(0);
			String file2 = (String) frame.getBody().get(1);
			
			iniciarSesion("Saadmin");
			ParamsDAO paramsDAO = new ParamsDAO();
			ParamValue paravalue = paramsDAO.getParamByClave(session, storeCode, ArmsServerConstants.AmbitoParams.DIR_INTERFACE, "SYNC_IN");
			while(storeCode.length() < 3)
				storeCode = "0"+storeCode;
			logger.info("StoreCode:"+storeCode);
			inFolder = new File(properties.getObject("SUITE_ROOT")+File.separator+paravalue.getValor()+File.separator+storeCode+File.separator+properties.getObject("fileUpdaterDown.folder.name"));
			
			UtilityFile.createWriteDataFile(getEyesFileName(properties),
					"UPD_FILE_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
							+ frame.getHeader().get(3) + "|STR|"
							+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
							+ "|Iniciando borrado de los archivos: " + frame.getBody().get(0)+ " y " + frame.getBody().get(1)+ ".\n",
					true);

			File fileToDelete = new File(inFolder.getAbsolutePath(), file);
			File fileToDelete2 = new File(inFolder.getAbsolutePath(), file2);
			logger.info("Archivo a borrar:"+fileToDelete.getAbsolutePath()+File.separator+fileToDelete.getName());
			fileToDelete.delete();
			logger.info("Archivo a borrar:"+fileToDelete2.getAbsolutePath()+File.separator+fileToDelete2.getName());
			fileToDelete2.delete();
			
			msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("0");
			String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
			logger.info(tmp);
			
			if(socket.writeDataSocket(tmp)){
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_FILE_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|END|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Archivos: "+frame.getBody().get(0)+" y " +frame.getBody().get(1) +" eliminados.\n", true);
			} else
				UtilityFile.createWriteDataFile(getEyesFileName(properties), "UPD_FILE_O|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+frame.getHeader().get(3)+"|WAR|"+ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|No se pudo enviar la respuesta.\n", true);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			try {
				msg.append(frame.getHeaderStr()).append(frame.getSeparator()).append("1");
				String tmp = Util.addLengthStartOfString(msg.toString(), properties.getInt("serverSocket.quantityBytesLength"));
				logger.info(tmp);
				socket.writeDataSocket(tmp);
			} catch (Exception ex) {
				logger.error(e.getMessage(), e);
			}
			try {
				UtilityFile.createWriteDataFile(getEyesFileName(properties),
						"UPD_FILE_O|" + properties.getHostName() + "|3|" + properties.getHostAddress() + "|"
								+ frame.getHeader().get(3) + "|ERR|"
								+ ArmsServerConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al intentar borrar el archivo " + frame.getBody().get(0) +", y el archivo " + frame.getBody().get(1) + ".\n",
						true);
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

		return false;
	}
	
	protected void iniciarSesion(String name) {
		while (session == null) {
			try {
				session = HibernateSessionFactoryContainer.getSessionFactory(name).openSession();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (session == null)
				try {
					logger.error("OCURRIO UN ERROR AL CREAR LA CONEXION A LA BD, SE REINTENTARA EN 3 seg.");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
		}
	}

	@Override
	public boolean process(ConnPipeServer arg0, Frame arg1, PropFile arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	private String getEyesFileName(PropFile properties){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsServerConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
}
