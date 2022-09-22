/**
 * 
 */
package com.allc.comm.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Calendar;

import org.apache.log4j.Logger;

/**
 * @author gustavo
 *
 */
public class ConnSocketClient implements ConnSocketClientI{
	private Logger logger = Logger.getLogger(ConnSocketClient.class);
	private Socket client;
	private DataOutputStream dos;
	private DataInputStream dis;
	private int retries;
	private long timeOutConnection;
	private long timeOutSleep;
	private String ipServer;
	private int portServer;
	private int quantityBytesLength;
	private boolean connected = false;

	/**
	 * 
	 */
	public ConnSocketClient() {
	}

	/**
	 * Funcion Utilizada para escribir data a un Socket
	 * 
	 * @param data
	 *            Dato que se envia por socket
	 * @return true si se logro escribir la data al socket, false si no se logro escribir la data al socket
	 */
	public boolean writeDataSocket(String data) {
		return writeDataSocketDOS(data);
	}

	/**
	 * Funcion utilizada para escribir data a un socket
	 * 
	 * @param data
	 *            Dato que se envia por socket
	 * @return true si se logro escribir la data al socket false si no se logro escribir la data al socket
	 */
	private boolean writeDataSocketDOS(String data) {
		try {
			getDos().writeBytes(data);
			getDos().flush();
			logger.debug("Data enviada al socket: " + data);
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Funcion Utilizada para escribir un arreglo de bytes a un Socket
	 * 
	 * @param data
	 *            Dato que se envia por socket
	 * @return true si se logro escribir la data al socket false si no se logro escribir la data al socket
	 */
	public synchronized boolean writeByteArraySocket(byte[] data) {
		return writeByteArraySocketDOS(data);
	}

	/**
	 * Funcion utilizada para escribir un arreglo de bytes a un socket UTILIZA DataOutputStream
	 * 
	 * @param data
	 *            Dato que se envia por socket
	 * @return true si se logro escribir la data al socket false si no se logro escribir la data al socket
	 */
	private synchronized boolean writeByteArraySocketDOS(byte[] data) {
		try {

			getDos().write(data);
			getDos().flush();
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Método encargado de verificar que hay disponible para leer del socket quantityBytesToWait bytes.
	 * 
	 * @param quantityBytesToWait
	 * @return
	 */
	private boolean timeOutSocket(long quantityBytesToWait) {
		boolean timeExpired = false;
		long startTime = Calendar.getInstance().getTimeInMillis();
		try {
			int available = getDis().available();
			while (available < quantityBytesToWait) {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= getTimeOutConnection()) {
					timeExpired = true;
					break;
				}
				Thread.sleep(getTimeOutSleep());
				available = getDis().available();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if (timeExpired) {
			logger.error("timeOutSocket: Proceso despreciado por TimeOut. Tiempo Maximo: " + timeOutConnection + " Tiempo Transcurrido: "
					+ String.valueOf(Calendar.getInstance().getTimeInMillis() - startTime));
			return true;
		} else {
			return false;
		}
	}

	public boolean timeOutSocket() {
		boolean tiempoAgotado = false;

		long tiempoInicial = Calendar.getInstance().getTimeInMillis();
		try {
			while (getDis().available() == 0) {
				if ((Calendar.getInstance().getTimeInMillis() - tiempoInicial) >= getTimeOutConnection()) {
					tiempoAgotado = true;
					break;
				}

				Thread.sleep(timeOutSleep);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if (tiempoAgotado) {
			logger.error("timeOutSocket: Proceso despreciado por TimeOut. Tiempo Maximo: " + timeOutConnection + " Tiempo Transcurrido: "
					+ String.valueOf(Calendar.getInstance().getTimeInMillis() - tiempoInicial));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Método encargado de leer los qtyBytesLength primeros bytes para obtener el total de bytes de la trama.
	 * 
	 * @return -1 si no se pudieron leer los qtyBytesLength bytes, >-1 si se pudo leer los qtyBytesLength bytes.
	 */
	public int readLengthDataSocket() {
		int cantBytesAleer = getQuantityBytesLength();
		byte[] cbufHeader;
		int totbytesaleer = 0;
		try {
			cbufHeader = new byte[cantBytesAleer];
			if (!timeOutSocket(cantBytesAleer)) {
				getDis().read(cbufHeader, 0, cantBytesAleer);
				logger.debug("Cant a leer: " + new String(cbufHeader));
				totbytesaleer = Integer.parseInt(new String(cbufHeader));
			} else {
				totbytesaleer = 0;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			totbytesaleer = -1;
		}
		return totbytesaleer;
	}
	
	public synchronized int bytesToInt(byte[] bytes) {
	    return (bytes[0]<<8)&0x0000ff00|(bytes[1]<<0)&0x000000ff;
	}

	public synchronized int leeLongitudDataHexaSocket(){
		int cantBytesAleer = getQuantityBytesLength();
		byte[] cbufHeader;
		int totalleido = 0;
		int totalleidos = 0;
		int totbytesaleer = 0;
		try{
			cbufHeader = new byte[cantBytesAleer];
			if(!timeOutSocket(getQuantityBytesLength())) {
				while((getQuantityBytesLength() - totalleidos) > 0){
					totalleido = getDis().read(cbufHeader, totalleidos, getQuantityBytesLength() - totalleidos);
					if (totalleido == -1) {
						return 0;
					}
					totalleidos = totalleidos + totalleido;
				}
				totbytesaleer = bytesToInt(cbufHeader);
			}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			totbytesaleer = -1;
		}
		return totbytesaleer;
	}
	
	/**
	 * Método encargado de leer quantityBytesToRead bytes del socket.
	 * 
	 * @param quantityBytesToRead
	 * @return
	 */
	public String readDataSocket(int quantityBytesToRead) {
		String dato = null;
		try {
			byte[] bufbyte = new byte[quantityBytesToRead];
			int totalleido = 0;
			int totalleidos = 0;

			if (!timeOutSocket(quantityBytesToRead)) {
				while ((quantityBytesToRead - totalleidos) > 0) {
					totalleido = getDis().read(bufbyte, totalleidos, quantityBytesToRead - totalleidos);
					if (totalleido == -1) {
						return null;
					}
					totalleidos = totalleidos + totalleido;
				}
				dato = new String(bufbyte,"ISO-8859-1");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			dato = null;
		}
		return dato;
	}

	/**
	 * Método que realiza la conexión al socket.
	 * 
	 * @return true si se conecta, false si no se conecta.
	 */
	public boolean connectSocket() {
		try {
			setClient(new Socket(getIpServer(), getPortServer()));
			setDos(new DataOutputStream(getClient().getOutputStream()));
			setDis(new DataInputStream(getClient().getInputStream()));
			setConnected(true);
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			setClient(null);
			setDis(null);
			setDos(null);
			setConnected(false);
			return false;
		}
	}

	/**
	 * Método que intentará retries reintentos de conexión al socket.
	 * 
	 * @return
	 */
	public boolean connectSocketUsingRetries() {
		boolean result = false;
		try {
			for (int i = 0; i < getRetries(); i++) {
				if (connectSocket()) {
					result = true;
					break;
				} else
					Thread.sleep(3000);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Método que cierra la conexión al socket.
	 */
	public void closeConnection() {
		try {
			closeDos();
			closeDis();
			closeClient();
			setConnected(false);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void closeDos() {
		try {
			if (getDos() != null)
				getDos().close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void closeDis() {
		try {
			if (getDis() != null)
				getDis().close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void closeClient() {
		try {
			if (getClient() != null && !getClient().isClosed())
				getClient().close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * @return the client
	 */
	public Socket getClient() {
		return client;
	}

	/**
	 * @param client
	 *            the client to set
	 */
	public void setClient(Socket client) {
		this.client = client;
	}

	/**
	 * @return the dos
	 */
	public DataOutputStream getDos() {
		return dos;
	}

	/**
	 * @param dos
	 *            the dos to set
	 */
	public void setDos(DataOutputStream dos) {
		this.dos = dos;
	}

	/**
	 * @return the dis
	 */
	public DataInputStream getDis() {
		return dis;
	}

	/**
	 * @param dis
	 *            the dis to set
	 */
	public void setDis(DataInputStream dis) {
		this.dis = dis;
	}

	/**
	 * @return the retries
	 */
	public int getRetries() {
		return retries;
	}

	/**
	 * @param retries
	 *            the retries to set
	 */
	public void setRetries(int retries) {
		this.retries = retries;
	}

	/**
	 * @return the ipServer
	 */
	public String getIpServer() {
		return ipServer;
	}

	/**
	 * @param ipServer
	 *            the ipServer to set
	 */
	public void setIpServer(String ipServer) {
		this.ipServer = ipServer.trim();
	}

	/**
	 * @return the portServer
	 */
	public int getPortServer() {
		return portServer;
	}

	/**
	 * @param portServer
	 *            the portServer to set
	 */
	public void setPortServer(int portServer) {
		this.portServer = portServer;
	}

	/**
	 * @return the connected
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * @param connected
	 *            the connected to set
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	/**
	 * @return the timeOutConnection
	 */
	public long getTimeOutConnection() {
		return timeOutConnection;
	}

	/**
	 * @param timeOutConnection
	 *            the timeOutConnection to set
	 */
	public void setTimeOutConnection(long timeOutConnection) {
		this.timeOutConnection = timeOutConnection;
	}

	/**
	 * @return the quantityBytesLength
	 */
	public int getQuantityBytesLength() {
		return quantityBytesLength;
	}

	/**
	 * @param quantityBytesLength
	 *            the quantityBytesLength to set
	 */
	public void setQuantityBytesLength(int quantityBytesLength) {
		this.quantityBytesLength = quantityBytesLength;
	}

	/**
	 * @return the timeOutSleep
	 */
	public long getTimeOutSleep() {
		return timeOutSleep;
	}

	/**
	 * @param timeOutSleep
	 *            the timeOutSleep to set
	 */
	public void setTimeOutSleep(long timeOutSleep) {
		this.timeOutSleep = timeOutSleep;
	}

}
