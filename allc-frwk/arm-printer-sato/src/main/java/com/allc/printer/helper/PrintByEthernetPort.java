/**
 * 
 */
package com.allc.printer.helper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.allc.socket.ClientSocket;

/**
 * @author gustavo
 *
 */
public class PrintByEthernetPort extends AbstractPrinter {
	private static Logger log = Logger.getLogger(PrintByEthernetPort.class);
	private ClientSocket clientSocket;
	private Socket socket;
	private InputStream is;
	private DataOutputStream dos;
	private InputStreamReader isr;
	private BufferedReader in;
	private int length;
	private int timeoutSocket;
	private int timeoutSleep;
	private String ip;
	private int port;

	/**
	 * 
	 */
	public PrintByEthernetPort(String ip, int port, int timeOut, int timeOutSleep, int length) {
		this.ip = ip;
		this.port = port;
		this.timeoutSocket = timeOut;
		this.timeoutSleep = timeOutSleep;
		this.length = length;
	}

	public boolean openSocket() {
		try {
			log.info("Iniciando socket...");
			socket = new Socket(ip, port);
			socket.setSoTimeout(timeoutSocket);
			dos = new DataOutputStream(socket.getOutputStream());
			is = socket.getInputStream();
			isr = new InputStreamReader(is);
			in = new BufferedReader(isr);
			clientSocket = new ClientSocket(socket, in, dos, timeoutSocket, length, timeoutSocket, timeoutSleep);
			return true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	public void closeSocket() {
		if (socket != null)
			try {
				socket.close();
				socket = null;
			} catch (IOException e) {
				log.error("Error al cerrar el servidor.", e);
			}
	}

	public void print(String articulo, String ean, String descripcion, String tamano, String marca, String referencia, String indImpto,
			String proveedor, String uxc, double precio, int cantEtq, char tipoEtiq, double porcRecargo, double porcIva, String modelBarra,
			boolean flMsjNoAfiliado) {
		try {
			PrinterUtils pu = new PrinterUtils();
			String messageString = pu.imprimeBarraZebra(articulo, ean, descripcion, tamano, marca, referencia, indImpto, proveedor, uxc,
					precio, cantEtq, tipoEtiq, porcRecargo, porcIva, modelBarra, flMsjNoAfiliado);
			clientSocket.sendMessage(messageString);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void printSup(String codEan, String nombre){
		try {
			PrinterUtils pu = new PrinterUtils();
			String messageString = pu.imprimeBarraSupZebra(codEan, nombre);
			clientSocket.sendMessage(messageString);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
