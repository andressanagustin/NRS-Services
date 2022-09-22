/**
 * 
 */
package com.allc.comm.pipe;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.ibm.OS4690.POSPipeInputStream;
import com.ibm.OS4690.POSPipeOutputStream;

/**
 * @author gustavo
 *
 */
public class ConnPipeServer {
	static Logger logger = Logger.getLogger(ConnPipeServer.class);
	private POSPipeInputStream pipeInputStream;
	private POSPipeOutputStream pipeOutputStream;
	private int quantityBytesLength;
	private long timeOutConnection;
	private long timeOutSleep;

	/**
	 * 
	 */
	public ConnPipeServer(POSPipeInputStream pipeInputStream, int quantityBytesLength, long timeOutConnection, long timeOutSleep) {
		this.pipeInputStream = pipeInputStream;
		this.quantityBytesLength = quantityBytesLength;
		this.timeOutConnection = timeOutConnection;
		this.timeOutSleep = timeOutSleep;
	}

	/**
	 * Funcion que lee una cantidad de bytes = quantityBytesToRead
	 * 
	 * @param quantityBytesToRead
	 * @return null si ocurrio un error al leer del pipe
	 */
	public String readDataPipe(int quantityBytesToRead) {
		String dato = null;
		try {
			byte[] bufbyte = new byte[quantityBytesToRead];
			int totalleido = 0;
			int totalleidos = 0;
			logger.info("Comenzando lectura de "+quantityBytesToRead+" bytes");
			if (!timeOutPipe(quantityBytesToRead)) {
				logger.info("Listo para leer");
				while ((quantityBytesToRead - totalleidos) > 0) {
					totalleido = pipeInputStream.read(bufbyte, totalleidos, quantityBytesToRead - totalleidos);
					logger.info("Total leído:"+totalleido);
					if (totalleido == -1) {
						return null;
					}
					totalleidos = totalleidos + totalleido;
				}
				dato = new String(bufbyte);
				logger.info("Dato: "+ dato);
			}
			logger.info("Terminó lectura");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			dato = null;
		}
		return dato;
	}

	/**
	 * Lee la longitud de una trama
	 * 
	 * @return la cantidad de bytes que tiene la trama. -1 si ocurrio un error o si ocurrio timeout al leer la longitud de la trama
	 */
	public int readLengthDataPipe() {
		byte[] cbufHeader;
		int totbytesaleer = 0;
		try {
			cbufHeader = new byte[quantityBytesLength];
			if (!timeOutPipe(quantityBytesLength)) {
				pipeInputStream.read(cbufHeader, 0, quantityBytesLength);
				logger.info("Cant a leer: " + new String(cbufHeader));
				totbytesaleer = Integer.parseInt(new String(cbufHeader));
			} else {
				totbytesaleer = -1;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			totbytesaleer = -1;
		}
		return totbytesaleer;
	}

	/**
	 * Método encargado de verificar que hay disponible para leer del pipe quantityBytesToWait bytes.
	 * 
	 * @param quantityBytesToWait
	 * @return
	 */
	public boolean timeOutPipe(int quantityBytesToWait) {
		boolean timeExpired = false;
		long startTime = Calendar.getInstance().getTimeInMillis();
		try {
			int available = pipeInputStream.available();
			while (available < quantityBytesToWait) {
				long dif = Calendar.getInstance().getTimeInMillis() - startTime;
				if (dif >= timeOutConnection) {
					timeExpired = true;
					break;
				}
				Thread.sleep(timeOutSleep);
				available = pipeInputStream.available();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if (timeExpired) {
			logger.error("timeOutPipe: Proceso despreciado por TimeOut. Tiempo Maximo: " + timeOutConnection + " Tiempo Transcurrido: "
					+ String.valueOf(Calendar.getInstance().getTimeInMillis() - startTime));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Close the pipe
	 * 
	 * @return
	 */
	public boolean closePipe() {
		boolean result = false;
		try {
//			if (pipeInputStream != null)
//				pipeInputStream.close();
			result = true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}
	

	/**
	 * @return the pipeOutputStream
	 */
	public POSPipeOutputStream getPipeOutputStream() {
		return pipeOutputStream;
	}

	/**
	 * @param pipeOutputStream the pipeOutputStream to set
	 */
	public void setPipeOutputStream(POSPipeOutputStream pipeOutputStream) {
		this.pipeOutputStream = pipeOutputStream;
	}

	/**
	 * 
	 * @param pipeName
	 *            Un caracter (A to Z) que identifica el pipe.
	 * @param pos
	 *            Nro terminal (001-999) o nodo del controlador (node (xy), where xy is C-Z, AA, or BB.)
	 * @param data
	 *            Data que se enviara
	 */
	public boolean sendData(String pipeName, String pos, String data) {
		boolean result = false;
		try {
			logger.info("Letra: "+pipeName.charAt(0) + " pos: "+ pos);
			if(pipeOutputStream == null)
				pipeOutputStream = new POSPipeOutputStream(pipeName.charAt(0), pos);
			pipeOutputStream.write((data).getBytes(), 0, (data).getBytes().length);
			//pipeOutputStream.close();
			result = true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}
}
