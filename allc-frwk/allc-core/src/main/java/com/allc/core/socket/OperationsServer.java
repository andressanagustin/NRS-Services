/**
 * 
 */
package com.allc.core.socket;

import java.net.SocketException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.allc.AllcUtils;
import com.allc.comm.frame.Frame;
import com.allc.comm.pipe.ConnPipeServer;
import com.allc.comm.socket.ConnSocketServer;
import com.allc.core.operation.AbstractOperation;
import com.allc.core.receiver.ReceiverPipe;
import com.allc.properties.PropFile;

/**
 * @author gustavo
 *
 */
public class OperationsServer implements Runnable {
	static Logger log = Logger.getLogger(OperationsServer.class);
	private ConnSocketServer socketServer;
	private ConnPipeServer pipeServer;
	private Pattern p = Pattern.compile(AllcUtils.Communication.REGEX);
	private boolean isFinish = false;
	private PropFile properties = null;
	private Map operations;
	public Map staticProperties = new HashMap();
	private long aliveTime = 60000;
	public static boolean isEnd = false;

	public OperationsServer(ConnSocketServer socketServer, PropFile properties) {
		this.socketServer = socketServer;
		try {
			socketServer.getClient().setSoLinger(true, 0);
		} catch (SocketException e) {
			log.error(e.getMessage(), e);
		}
		this.properties = properties;
	}

	public OperationsServer(ConnPipeServer pipeServer, PropFile properties) {
		this.pipeServer = pipeServer;
		this.properties = properties;
	}

	public void run() {
		int numBytesToRead;
		String operacionType;
		boolean permanentConn = false;
		long startTime = Calendar.getInstance().getTimeInMillis();
		String data = "";

		try {
			operations = properties.getMap("core.operations");
			aliveTime = properties.getLong("core.operations.aliveTime");
			while (!isFinish && !isEnd) {

				numBytesToRead = readCountBytesToRead();

				if (numBytesToRead > AllcUtils.Communication.NUMBER_CERO) {

					data = readData(numBytesToRead);
					if (data != null && !data.equals("")) {
						log.info("Received data: " + data);
						List list = Arrays.asList(p.split(data));
						Frame trama = new Frame(list, AllcUtils.Communication.QTY_MEMBERS_HEADER, AllcUtils.Communication.FRAME_SEP);
						if (trama.loadData()) {
							log.debug(trama.toString());
							permanentConn = ((String) trama.getHeader().get(trama.PERMANENT_CONN)).equals("1") ? true : false;
							operacionType = (String) trama.getHeader().get(trama.OPERATION_REQUEST);
							if (operations.containsKey(operacionType)) {
								String operaName = (String) operations.get(operacionType);
								try {
									Class clase = Class.forName(operaName);
									AbstractOperation operation = (AbstractOperation) clase.newInstance();
									operation.staticProperties = staticProperties;
									processOperation(operation, trama);
								} catch (Exception e) {
									log.error(e.getMessage(), e);
									ReceiverPipe.waitAvailable = false;
								}
							} else {
								log.info("Operación no reconocida: "+operacionType);
								ReceiverPipe.waitAvailable = false;
							}
							if (!permanentConn)
								isFinish = true;
						}
					}
				}
				if (!isFinish) {
					long dif = Calendar.getInstance().getTimeInMillis() - startTime;
					if (dif >= aliveTime) {
						isFinish = true;
					}
				}
			}
		} catch (Exception x) {
			log.error(x.getMessage(), x);
		}
		finally {
			try {
				//Thread.sleep(1000);
				Thread.sleep(300);
				closeConn();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private void closeConn() {
		if (socketServer != null) {
			log.info("Cerrando socket");
			socketServer.closeConnectionServer();
		} else if (pipeServer != null) {
			log.info("Cerrando pipe");
			pipeServer.closePipe();
		}
	}

	private void processOperation(AbstractOperation operation, Frame trama) {
		if (socketServer != null) {
			operation.process(socketServer, trama, properties);
		} else if (pipeServer != null) {
			operation.process(pipeServer, trama, properties);
		}
	}

	private int readCountBytesToRead() {
		if (socketServer != null) {
			return socketServer.readLengthDataSocket();
		} else if (pipeServer != null) {
			return pipeServer.readLengthDataPipe();
		}
		return 0;
	}

	private String readData(int countBytesToRead) {
		if (socketServer != null) {
			return socketServer.readDataSocket(countBytesToRead);
		} else if (pipeServer != null) {
			return pipeServer.readDataPipe(countBytesToRead);
		}
		return "";
	}
}
