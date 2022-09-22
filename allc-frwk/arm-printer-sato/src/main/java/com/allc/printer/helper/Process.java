/**
 * 
 */
package com.allc.printer.helper;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.allc.socket.ClientSocket;

/**
 * @author gustavo
 *
 */
public class Process {
	private static Logger log = Logger.getLogger(Process.class);
	private ClientSocket clientSocket;
	private Socket socket;
	private InputStream is;
	private DataOutputStream dos;
	private InputStreamReader isr;
	private BufferedReader in;
	private int length;
	private int timeout;
	private int timeoutSocket;
	private int timeoutSleep;
	private boolean isEnd;

	/**
	 * @param args
	 * @throws Exception
	 */
	public void procesar() throws Exception {
		isEnd = false;
		Properties prop = new Properties();
		is = new FileInputStream("configurator.properties");
		prop.load(is);
		is.close();
		String tipoEt = new String(prop.getProperty("printer.tipoEtq"));
		String ip = new String(prop.getProperty("server.ip"));
		int port = new Integer(prop.getProperty("server.port")).intValue();
		timeoutSocket = new Integer(prop.getProperty("socket.timeout")).intValue();
		length = new Integer(prop.getProperty("socket.length")).intValue();
		timeoutSleep = new Integer(prop.getProperty("socket.timeoutSleep")).intValue();
		timeout = new Integer(prop.getProperty("process.timeout")).intValue();
		 log.info("Iniciando socket...");
		 socket = new Socket(ip, port);
		 socket.setSoTimeout(timeoutSocket);
		 dos = new DataOutputStream(socket.getOutputStream());
		 is = socket.getInputStream();
		 isr = new InputStreamReader(is);
		 in = new BufferedReader(isr);
		 clientSocket = new ClientSocket(socket, in, dos, timeoutSocket, length, timeout, timeoutSleep);
		PrinterUtils pu = new PrinterUtils();
		String msg = pu.imprimeBarraSato("", "1000949700105", "DESCRIPCIÓN", "", "CLUB", "8689", "0", "6", "", 79.99,
			1, tipoEt.charAt(0), 1.07, 19, "", true);
//		msg = msg + pu.imprimeBarraSato("111000111", "101010101111001", "Articulo 1", "", "Marca Prueba", "Ref.", "3", "1", "1", 300.00,
//				2, tipoEt.charAt(0), 10, 19, "", false);
		log.info("msg: " + msg);
//		escribir(msg);
//		write(msg);
		// Parameters settings = new Parameters();
		// settings.setPort("COM1");
		// settings.setBaudRate(Baud._9600);
		// settings.setByteSize("8");
		// settings.setStopBits("1");
		// settings.setParity("n");
		// //Instance COM1.
		// Com com1 = new Com(settings);
		// //Write COM1.
		// com1.sendSingleData(msg);
		// com1.close();
		 clientSocket.sendMessage(msg);
		 while (!isEnd) {
		 if (socket != null) {
		 if (!socket.isClosed()) {
		 String resp = clientSocket.receiveMessage();
		 if (resp != null){
		 log.info("Respuesta recibida: "+resp);
		 shutdown();
		 } else
		 Thread.sleep(1000);
		 }
		 } else {
		 Thread.sleep(1000);
		 }
		 }
	}

	private void write(String messageString) throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier("COM1");
		if (portIdentifier.isCurrentlyOwned()) {
			System.out.println("Error: Port is currently in use");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

				OutputStream out = serialPort.getOutputStream();
				serialPort.notifyOnDataAvailable(true);
				Thread.sleep(3000);
				log.info("Escribiendo en puerto: " + messageString);
				out.write(messageString.getBytes());
				out.close();
				serialPort.close();
				commPort.close();
			} else {
				System.out.println("Error: Only serial ports are handled by this example.");
			}
		}
	}
	
	private void escribir(String msg){
		 FileWriter fichero = null;
	        PrintWriter pw = null;
	        try
	        {
	            fichero = new FileWriter("c:/LEX/Printer/prueba.txt");
	            pw = new PrintWriter(fichero);
	            pw.println(msg);
	 
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	           try {
	           if (null != fichero)
	              fichero.close();
	           } catch (Exception e2) {
	              e2.printStackTrace();
	           }
	        }
	}

	public static StringBuffer addLengthStartOfString(String msg) {
		StringBuffer a = null;
		try {
			a = new StringBuffer(StringUtils.leftPad(String.valueOf(msg.length()), 5, "0"));
			a.append(msg);
		} catch (Exception e) {
			log.error("addlengthStartString: " + e);
		}
		return a;
	}

	public void shutdown() {
		log.info("Bajando servidor...");
		if (socket != null)
			try {
				socket.close();
				socket = null;
			} catch (IOException e) {
				log.error("Error al cerrar el servidor.", e);
			}
		isEnd = true;
		log.info("Aplicaci�n finalizada.");
	}
}
