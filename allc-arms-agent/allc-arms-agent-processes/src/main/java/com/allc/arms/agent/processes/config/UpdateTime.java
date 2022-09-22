package com.allc.arms.agent.processes.config;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.arms.utils.ControllerDateTime;
import com.allc.arms.utils.tsl.TSLUtility;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FilenameFilter4690;

/**
 * Proceso que permite cambiar la Hora del controlador segun el server 
*/
public class UpdateTime extends AbstractProcess{
	private Logger logger = Logger.getLogger(UpdateTime.class);
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	private static Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
	protected ConnSocketClient socketClient = null;
	public boolean finished = false;
	
	public void run() {
		logger.info("Iniciando UpdateTime...");
		List listToSend;
		StringBuilder message = new StringBuilder();
		Frame frame;
		String storeNumber = getStoreNumber();
		try {
			if (!thereSales()) {
				Files.creaEscribeDataArchivo4690(getEyesFileName(), "UP_TIME_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Iniciando proceso actualizacion de hora del controlador.\n", true);
				
				String today = ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date());
				message.append("S").append(ArmsAgentConstants.Communication.FRAME_SEP)
						.append(ArmsAgentConstants.Process.UPDATE_TIME)
						.append(ArmsAgentConstants.Communication.FRAME_SEP).append("000")
						.append(ArmsAgentConstants.Communication.FRAME_SEP).append(storeNumber)
						.append(ArmsAgentConstants.Communication.FRAME_SEP).append("")
						.append(ArmsAgentConstants.Communication.FRAME_SEP).append(today)
						.append(ArmsAgentConstants.Communication.FRAME_SEP).append("");
				logger.info("Trama a enviar: " + message);
				listToSend = Arrays.asList(p.split(message.toString()));
				frame = new Frame(listToSend, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
										ArmsAgentConstants.Communication.FRAME_SEP);
				if (frame.loadData()) {
					Frame respuesta = sendTramaToServer(frame);
					if (respuesta != null) {
						Date date = ArmsAgentConstants.DateFormatters.HHmmss_format.parse(respuesta.getBody().get(1).toString());
						ControllerDateTime.setTimeController(date);
					} else {
						logger.info("No recibe respuesta del server");
					}
				} else {
					logger.info("Trama mal armada. No envia");
				}
			} else {
				logger.info("Hay ventas, no actualiza");
			}
		} catch (Exception e) {
			Files.creaEscribeDataArchivo4690(getEyesFileName(), "UP_TIME_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|ERR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Error al actualizar la hora del controlador.\n", true);
			logger.error(e.getMessage(), e);
		}
		finally {
			finished = true;
			Files.creaEscribeDataArchivo4690(getEyesFileName(), "UP_TIME_P|"+properties.getHostName()+"|1|"+properties.getHostAddress()+"|"+storeNumber+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Finalizó el proceso actualizacion de hora del controlador.\n", true);
		}
	}
	
	private String getStoreNumber() {
		try {
			ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
			logger.info("Controlador ID:"+controllerStatusData.getControllerId());
			logger.info("Master Controlador ID:"+controllerStatusData.getMasterControllerId());
			
			String store = (new Integer(controllerStatusData.getStoreNumber())).toString();
			while (store.length() < 3)
				store = "0" + store;
			return store;	
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
	
	private boolean thereSales() {
		try {
			Thread.sleep(30000); //Espera a que se genere el archivo
			final String FILE_START_WITH = properties.getObject("update.time.start.json"); 
			String pathName = properties.getObject("update.time.path.json");
			logger.info("Lee carpeta " + pathName + " Archivos " + FILE_START_WITH);
			File4690 path = new File4690(pathName);
			FilenameFilter4690 filter = new FilenameFilter4690() {
                public boolean accept(File4690 f, String name)
                {
                    return name.startsWith(FILE_START_WITH);
                }
            };
            File4690[] files = path.listFiles(filter);
            if (files == null || files.length == 0){
                logger.info("No hay archivo a leer.");
                return true;
            }
            logger.info("Lee archivo " + pathName + "/" + files[0].getName());
            String text = TSLUtility.leerArchivo4690(pathName + "/" + files[0].getName());
            JSONObject obj = new JSONObject(text);
            int ventas = obj.getInt("cant_operaciones");
            logger.info("Cantidad operaciones " + ventas);
            if (ventas > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return true;
		}
	}
	
	private Frame sendTramaToServer(Frame frame) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected())
				connectClient();
			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
			logger.info("Trama a enviar: " + trama);
			if (socketClient.writeDataSocket(trama)) {
				int numberOfBytes = 0;
				int timeOutCycles = 0;
				while (numberOfBytes == 0) {
					numberOfBytes = socketClient.readLengthDataSocket();
					if (timeOutCycles == 5) {
						// cada 5 timeouts escribimos una trama vacía para
						// asegurarnos que el socket esté activo
						String mje = Util.addLengthStartOfString("", qtyBytesLength).toString();
						if (!socketClient.writeDataSocket(mje)) {
							socketClient.setConnected(false);
							closeClient();
							return null;
						}
						timeOutCycles = 0;
					}
					timeOutCycles++;
				}
				if (numberOfBytes > 0) {
					str = socketClient.readDataSocket(numberOfBytes);
					if (StringUtils.isNotBlank(str)) {
						list = Arrays.asList(p.split(str));
						frameRpta = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
								ArmsAgentConstants.Communication.FRAME_SEP);
						logger.info("Respuesta recibida: " + frameRpta.toString());
						frameRpta.loadData();
						closeClient();
						return frameRpta;
					}
				}
			} else {
				socketClient.setConnected(false);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			socketClient.setConnected(false);
		}
		closeClient();
		return null;
	}
	
	protected boolean connectClient() {
		if (socketClient == null) {
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(properties.getObject("clientSocket.ip"));
			socketClient.setPortServer(properties.getInt("clientSocket.port"));
			socketClient.setRetries(properties.getInt("clientSocket.retries"));
			socketClient.setTimeOutConnection(properties.getInt("clientSocket.timeOutConnection"));
			socketClient.setTimeOutSleep(properties.getInt("clientSocket.timeOutSleep"));
			socketClient.setQuantityBytesLength(properties.getInt("clientSocket.quantityBytesLength"));
		}
		return socketClient.connectSocketUsingRetries();
	}

	protected void closeClient() {
		if (socketClient != null && socketClient.isConnected())
			socketClient.closeConnection();
	}
	
	private String getEyesFileName(){
		return properties.getObject("eyes.ups.file.name")+"_"+ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}
	
	@Override
	public boolean shutdown(long timeToWait) {
		finished = true;
		closeClient();
		logger.info("Finaliza el Proceso UpdateTime.");
		return true;
	}

}
