package com.allc.arms.agent.operations.colas;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.core.receiver.ReceiverPipe;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;

public class RegistroPersonasEnColaOperation extends AbstractOperation {

	protected Logger log = Logger.getLogger(RegistroPersonasEnColaOperation.class);
	protected String storeNumber = "";
	protected String terminal = "";
	protected String personasCola = "";
	protected String numeroTrx = "";
	protected String storeCode;
	protected String resp;
	protected static Pattern p = Pattern.compile(ArmsAgentConstants.Communication.REGEX);
	protected PropFile properties = PropFile.getInstance(ArmsAgentConstants.PROP_FILE_NAME);
	protected ConnSocketClient socketClient;

	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		log.info("Inicio de operacion COLAS V0");
		resp = "2";
		try {
			ReceiverPipe.waitAvailable = false;
			ControllerStatusData controllerStatusData = ControllerApplicationServices.getControllerStatus();
			storeNumber = controllerStatusData.getStoreNumber();
			terminal = (String) frame.getHeader().get(3);
			numeroTrx = (String) frame.getBody().get(0);
			personasCola = (String) frame.getBody().get(1);

			log.info("storeNumber: " + storeNumber);
			log.info("terminal: " + terminal);
			log.info("numeroTrx: " + numeroTrx);
			log.info("personasCola: " + personasCola);

			StringBuffer data = getFrameHeader();
			data.append(ArmsAgentConstants.Communication.FRAME_SEP).append(storeNumber)
					.append(ArmsAgentConstants.Communication.FRAME_SEP).append(terminal)
					.append(ArmsAgentConstants.Communication.FRAME_SEP).append(numeroTrx)
					.append(ArmsAgentConstants.Communication.FRAME_SEP).append(personasCola);
			List list = Arrays.asList(p.split(data.toString()));

			Frame frameS = new Frame(list, ArmsAgentConstants.Communication.QTY_MEMBERS_HEADER,
					ArmsAgentConstants.Communication.FRAME_SEP);

			if (frameS.loadData()) { 
				try {
					sendFileHeader(frameS);
					log.info("Si pudo enviar mensaje ");
				} catch (Exception e) {
					log.info("No pudo enviar mensaje " + e);
				}
			}
			log.info("resp: " + resp);
//			String sb = Util.addLengthStartOfString(
//					frame.getHeaderStr() + ArmsAgentConstants.Communication.FRAME_SEP + resp,
//					properties.getInt("serverSocket.quantityBytesLength"));
//			log.info("sb: " + sb);
//			pipe.sendData("F", (String) frame.getHeader().get(Frame.POS_SOURCE), sb); // envia la respuesta al CC

		} catch (Exception e) {
			log.info("ERROR: " + e);
		}

		return false;
	}

	public boolean process(ConnSocketServer arg0, Frame arg1, PropFile arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	protected StringBuffer getFrameHeader() {
		StringBuffer data = new StringBuffer();
		data.append("S").append(ArmsAgentConstants.Communication.FRAME_SEP).append("57")
				.append(ArmsAgentConstants.Communication.FRAME_SEP).append("000")
				.append(ArmsAgentConstants.Communication.FRAME_SEP).append(storeCode)
				.append(ArmsAgentConstants.Communication.FRAME_SEP).append(ArmsAgentConstants.Communication.TEMP_CONN)
				.append(ArmsAgentConstants.Communication.FRAME_SEP)
				.append(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
		return data;
	}

	protected boolean sendFileHeader(Frame frame) {
		String str;
		List list;
		Frame frameRpta;
		int qtyBytesLength = properties.getInt("clientSocket.quantityBytesLength");
		try {
			if (socketClient == null || !socketClient.isConnected())
				log.info("socket 1");
			connectClient();
			log.info("socket 2");
			String trama = Util.addLengthStartOfString(frame.getString().toString(), qtyBytesLength).toString();
			log.info("Trama a enviar: " + trama);
			if (socketClient.writeDataSocket(trama)) {
				int numberOfBytes = 0;
				int timeOutCycles = 0;
				while (numberOfBytes == 0) {
					numberOfBytes = socketClient.readLengthDataSocket();
					if (timeOutCycles == 5) {
						// cada 5 timeouts escribimos una trama vacía para asegurarnos que el socket
						// esté activo
						String mje = Util.addLengthStartOfString("", qtyBytesLength).toString();
						if (!socketClient.writeDataSocket(mje)) {
							socketClient.setConnected(false);
							return false;
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
						frameRpta.loadData(); 
						resp = (String) frameRpta.getBody().get(0); 
						if (frameRpta.getStatusTrama() == 0) {
							return true;
						}
					}
				}
			} else {
				socketClient.setConnected(false);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			socketClient.setConnected(false);
		}
		return false;
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

	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
