package com.allc.arms.agent.operations.comm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketClient;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.core.receiver.ReceiverPipe;
import com.allc.files.helper.Files;
import com.allc.properties.PropFile;
import com.allc.string.helper.Util;
import com.ibm.OS4690.File4690;

public class SendFileContentToSocketOperation extends AbstractOperation{
	
	private Logger logger = Logger.getLogger(SendFileContentToSocketOperation.class);
	protected String storeCode;

	public boolean process(ConnSocketServer socket, Frame frame, PropFile properties) {
		return false;
	}
	
	public boolean process(ConnPipeServer pipe, Frame frame, PropFile properties) {
		logger.info("Iniciando SendFileContentToSocketOperation...");

		Map vectorMsgQueueByPos = null;
		List queue = null;
		try {
			String msg = (String) frame.getBody().get(0);
			String posResponseChannel = (String) frame.getHeader().get(Frame.POS_SOURCE);
			logger.info("Mensaje recibido por pipe: " + msg);
			File4690 requestFile = new File4690(msg);
			ConnSocketClient socketClient = null;
			if(requestFile != null && requestFile.exists()){
				String requestMsg = Files.readSpecifictLineOfFile4690(msg, 1);
				if(staticProperties != null && staticProperties.containsKey("VectorMsgQueueByPos")){
					vectorMsgQueueByPos = (Map) staticProperties.get("VectorMsgQueueByPos");
					if(vectorMsgQueueByPos.containsKey(posResponseChannel)){
						logger.info("Existe cola de mensajes con vector.");
						queue = (ArrayList) vectorMsgQueueByPos.get(posResponseChannel);
					} else {
						queue = new ArrayList();
						vectorMsgQueueByPos.put(posResponseChannel, queue);
					}
				} else {
					vectorMsgQueueByPos = new HashMap();
					queue = new ArrayList();
					vectorMsgQueueByPos.put(posResponseChannel, queue);
					staticProperties.put("VectorMsgQueueByPos", vectorMsgQueueByPos);
				}

				byte[] msgBytes = requestMsg.getBytes("UTF-8");
				if(requestMsg.indexOf("TransactionStartEvent") >= 0){
					queue.clear();
				} else if(requestMsg.indexOf("TransactionStartEvent") >= 0){
					//TODO borrar todos los archivos que haya de la terminal
				}
				queue.add(this.getName());

				//liberamos pipeServer
				ReceiverPipe.waitAvailable = false;
				
				logger.info("Esperando el turno para " + this.getName());
				while(!((String) queue.get(0)).equals(this.getName())){
					Thread.sleep(50);
				}
				logger.info("Llego el turno");
				if(staticProperties != null && staticProperties.containsKey("VectorSockets")){
					Map vectorSockets = (Map) staticProperties.get("VectorSockets");
					if(vectorSockets.containsKey(posResponseChannel)){
						logger.info("Existe conexión con vector.");
						socketClient = (ConnSocketClient) vectorSockets.get(posResponseChannel);
					}
				}
				if(socketClient == null){
					socketClient = initSocket(properties, posResponseChannel);
				}
				if (socketClient.isConnected() || socketClient.connectSocketUsingRetries()) {
					logger.info("Socket conectado");
					setLong(msgBytes, msgBytes.length - 4, 0, 3);
					boolean enviado = socketClient.writeByteArraySocket(msgBytes);
					if(!enviado){
						logger.info("Reiniciamos socket");
						//si no se envió, puede que se haya perdido la comunicación, reiniciamos la misma
						socketClient = initSocket(properties, posResponseChannel);
						enviado = socketClient.writeByteArraySocket(msgBytes);
					}
					if (enviado) {
						logger.info("Mensaje enviado: "+requestMsg);
						if(requestMsg.indexOf("GetInLineOffersRequest") >= 0 || requestMsg.indexOf("GetTransactionOffersRequest") >= 0 || requestMsg.indexOf("GetTenderOffersRequest") >= 0 || requestMsg.indexOf("GetTrailerPrintMessagesRequest") >= 0){
							logger.info("El mensaje requiere leer respuesta.");
							if (!socketClient.timeOutSocket()) {
								logger.info("Leyendo respuesta");
								
								byte[] temp = new byte[4];
								System.arraycopy(socketClient.readDataSocket(4).getBytes("UTF-8"), 0, temp, 0, 4);
								int longResp = (int) arr2long(temp);
								logger.info("Longitud de Respuesta:" + longResp);
								int available = socketClient.getDis().available();
								logger.info("Available de Respuesta:" + available);
								if(available < longResp)
									longResp = available;
								String data = socketClient.readDataSocket(longResp);
								logger.info("Respuesta recibida:" + data);
								while((data != null && !data.endsWith("Result>")) || (data == null && longResp > 0)){
									longResp = longResp / 2;
									String leido = socketClient.readDataSocket(data == null && longResp > 0 ? longResp : 1);
									if(leido != null)
										data += leido;
									else 
										data = null;
								}
								logger.info("Respuesta recibida final:" + data);
								Files.deleteFile4690(msg+"RSP");
								Files.creaEscribeDataArchivo4690(msg+"RSP", data, true);
								logger.info("Respuesta registrada en archivo: " + msg + "RSP");
								String resp = Util.addLengthStartOfString(msg + "RSP", properties.getInt("serverSocket.quantityBytesLength"));
								pipe.sendData("F", (String) frame.getHeader().get(Frame.POS_SOURCE), resp);
							}
						} else {
							logger.info("El mensaje NO requiere leer respuesta.");
						}
					}
				} else {
					logger.info("Socket NO conectado");
				}
			}
			
		} catch (Exception e){
			logger.error(e.getMessage(), e);
		} finally {
			if(queue != null){
				if(queue.remove(this.getName()))
					logger.info("Eliminado de la cola");
				else
					logger.info("No Eliminado de la cola");		
			}
		}
		
		return false;
	}
	
	private ConnSocketClient initSocket(PropFile properties, String posResponseChannel) {
		logger.info("Nueva conexión con vector.");
		ConnSocketClient socketClient = new ConnSocketClient();
		socketClient.setIpServer(properties.getObject("sendTramaToVector.server.ip"));
		socketClient.setPortServer(properties.getInt("sendTramaToVector.server.port"));
		socketClient.setRetries(properties.getInt("sendTramaToVector.server.retries"));
		socketClient.setTimeOutConnection(properties.getInt("sendTramaToVector.server.timeOut"));
		socketClient.setTimeOutSleep(properties.getInt("sendTramaToVector.server.timeSleep"));
		socketClient.setQuantityBytesLength(0);
		Map vectorSockets = null;
		if(staticProperties != null && staticProperties.containsKey("VectorSockets")){
			vectorSockets = (Map) staticProperties.get("VectorSockets");
		} else {
			vectorSockets = new HashMap();
			staticProperties.put("VectorSockets", vectorSockets);
		}
		vectorSockets.put(posResponseChannel, socketClient);
		return socketClient;
	}
	private final long arr2long(byte[] arr)
	{
		long accum = 0;
		for (int shiftBy = 0, i = arr.length - 1; shiftBy < 8 * arr.length; shiftBy += 8, i--)
		{
			accum |= ((long) (arr[i] & 0xff)) << shiftBy;
		}
		return accum;
	}
	
	protected final void setLong(byte[] data, long val, int start, int end)
	{
		int len = end - start + 1;
		for (int shiftBy = (len - 1) * 8, i = start; shiftBy >= 0; shiftBy -= 8, i++)
		{
			byte b = (byte) ((val >> shiftBy) & 0xff);
			data[i] = b;
		}
	}

	public boolean shutdown(long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
