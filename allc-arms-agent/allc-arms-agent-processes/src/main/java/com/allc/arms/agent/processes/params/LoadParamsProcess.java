package com.allc.arms.agent.processes.params;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.core.process.AbstractProcess;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;

public class LoadParamsProcess extends AbstractProcess {

	protected Logger log = Logger.getLogger(LoadParamsProcess.class);
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	private static Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
	private String store;
	private boolean isMaster = true;
	protected ConnSocketClient socketClient;
	protected boolean finished = false;

	protected void init() {
		try {
			ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
			log.info("Controlador ID:"+controllerStatusData.getControllerId());
			log.info("Master Controlador ID:"+controllerStatusData.getMasterControllerId());
			if(controllerStatusData.getControllerId().equalsIgnoreCase(controllerStatusData.getMasterControllerId()))
				isMaster = true;
			else
				isMaster = false;
			log.info("Controlador:"+(isMaster ? "Maestro" : "Alterno"));
			store = (new Integer(controllerStatusData.getStoreNumber())).toString();
			while (store.length() < 3)
				store = "0" + store;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private String getEyesFileName() {
		return properties.getObject("eyes.ups.file.name") + "_"
				+ ArmsAgentConstants.DateFormatters.ddMMyy_format.format(new Date());
	}

	public void run() {
		log.info("Iniciando LoadParamsProcess...");
		init();
		
		try {
			if(properties.getInt("agent.params.loadLocal") == 1){
				log.info("Los parametros han sido cargados localmente en forma exitosa");
			} else {
			
				StringBuffer data = new StringBuffer();
				data.append("S").append(ArmsAgentConstants.Communication.FRAME_SEP)
						.append(ArmsAgentConstants.Process.LOAD_PARAMS_PROCESS)
						.append(ArmsAgentConstants.Communication.FRAME_SEP).append("000")
						.append(ArmsAgentConstants.Communication.FRAME_SEP).append(store)
						.append(ArmsAgentConstants.Communication.FRAME_SEP)
						.append(ArmsAgentConstants.Communication.TEMP_CONN)
						.append(ArmsAgentConstants.Communication.FRAME_SEP)
						.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"))
						.append(ArmsAgentConstants.Communication.FRAME_SEP)
						.append(ArmsAgentConstants.AmbitoParams.ARMS_AGENT_PARAMS)
						.append(ArmsAgentConstants.Communication.FRAME_SEP);
	
				List list = Arrays.asList(p.split(data.toString()));
				Frame frame = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
						ArmsAgentConstants.Communication.FRAME_SEP);
				if (frame.loadData()) {
					Frame respuesta = sendParams(frame);
					closeClient();
					if (respuesta != null) {
						try {
							String param = null;
							Iterator it = respuesta.getBody().iterator();
	
							//movemos un lugar el indice para saltear el campo "ambito"
							it.next();
							properties.clear(); //LIMPIO LOS PARAMETROS
							while (it.hasNext()) {
								param = (String) it.next();
								String paramArray[] = param.split("=");
								if(!isMaster && ("core.processes".equalsIgnoreCase(paramArray[0]) || "core.operations".equalsIgnoreCase(paramArray[0]))){
									log.info("El parametro "+paramArray[0]+" no se cargara porque el controlador es alterno.");
								} else if(!isMaster && "core.processes.alterno".equalsIgnoreCase(paramArray[0])){
									properties.setObject("core.processes",paramArray[1]);
									log.info("El parametro "+paramArray[0]+" se cargara porque el controlador es alterno.");
								} else if(!isMaster && "core.operations.alterno".equalsIgnoreCase(paramArray[0])){
									properties.setObject("core.operations",paramArray[1]);
									log.info("El parametro "+paramArray[0]+" se cargara porque el controlador es alterno.");
								} else
									properties.setObject(paramArray[0], paramArray.length > 1 ? paramArray[1] : "");
							}
							Files.creaEscribeDataArchivo4690(getEyesFileName(), "LOAD_PARAMS_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|STR|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Cargando parámetros de tienda: "+store+" y ámbito: "+ArmsAgentConstants.AmbitoParams.ARMS_AGENT_PARAMS+".\n", true);
							
							log.info("Los par�metros han sido cargados en forma exitosa");
							Files.creaEscribeDataArchivo4690(getEyesFileName(), "LOAD_PARAMS_P|"+properties.getHostName()+"|3|"+properties.getHostAddress()+"|"+store+"|END|"+ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())+"|Parámetros cargados correctamente.\n", true);
							properties.storeToFile4690(ArmsAgentConstants.PROP_FILE_NAME);
						} catch (Exception e) {
							properties.setObject("agent.params.loaded","false");
							log.error(e.getMessage(), e);
						}
	
					} else {
						Files.creaEscribeDataArchivo4690(getEyesFileName(),
								"LOAD_PARAMS_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|"
										+ store + "|WAR|"
										+ ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
										+ "|No se pudo cargar los parametros.\n",
								true);
						log.error("Error durante la carga de los parametros.");
					}
				}
			}
		} catch (Exception e)

		{
			try {
				Files.creaEscribeDataArchivo4690(getEyesFileName(),
						"LOAD_PARAMS_P|" + properties.getHostName() + "|1|" + properties.getHostAddress() + "|" + store
								+ "|ERR|" + ArmsAgentConstants.DateFormatters.yyyyMMddHHmmss_format.format(new Date())
								+ "|Error al intentar cargar los parametros.\n",
						true);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
			log.error(e.getMessage(), e);
		}

		finished = true;

	}

	protected Frame sendParams(Frame frame) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected())
				connectClient();
			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
			log.info("Trama a enviar: " + trama);
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
						log.info("Respuesta recibida: " + frameRpta.toString());
						frameRpta.loadData();
						return frameRpta;
					}
				}
			} else {
				socketClient.setConnected(false);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			socketClient.setConnected(false);
		}
		return null;
	}

	protected boolean connectClient() {
		if (socketClient == null) {
			socketClient = new ConnSocketClient();
			socketClient.setIpServer(properties.getObject("clientSocket.ipCentral"));
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

	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public LoadParamsProcess() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

}
