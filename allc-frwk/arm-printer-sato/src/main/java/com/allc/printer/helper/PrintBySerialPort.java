/**
 * 
 */
package com.allc.printer.helper;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * @author gustavo
 *
 */
public class PrintBySerialPort extends AbstractPrinter {
	private static Logger log = Logger.getLogger(PrintBySerialPort.class);
	private String serialPortName;
	private OutputStream out;
	private SerialPort serialPort;
	private CommPort commPort;

	/**
	 * 
	 */
	public PrintBySerialPort(String serialPortName) {
		this.serialPortName = serialPortName;
	}

	public boolean openSerialPort() {
		try {
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(serialPortName);
			if (portIdentifier.isCurrentlyOwned()) {
				System.out.println("Error: Port is currently in use");
			} else {
				commPort = portIdentifier.open(this.getClass().getName(), 2000);
				if (commPort instanceof SerialPort) {
					serialPort = (SerialPort) commPort;
					serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
					out = serialPort.getOutputStream();
					return true;
				} else {
					log.error("Error: Only serial ports are handled by this example.");
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	public void closeSerialPort() {
		try {
			out.close();
			serialPort.close();
			commPort.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void print(String articulo, String ean, String descripcion, String tamano, String marca, String referencia, String indImpto,
			String proveedor, String uxc, double precio, int cantEtq, char tipoEtiq, double porcRecargo, double porcIva, String modelBarra,
			boolean flMsjNoAfiliado) {
		try {
			PrinterUtils pu = new PrinterUtils();
			String messageString = pu.imprimeBarraSato(articulo, ean, descripcion, tamano, marca, referencia, indImpto, proveedor, uxc,
					precio, cantEtq, tipoEtiq, porcRecargo, porcIva, modelBarra, flMsjNoAfiliado);
			log.info("Escribiendo en puerto serie: " + messageString);
			out.write(messageString.getBytes());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void printSup(String codEan, String nombre) {
		// TODO Auto-generated method stub
		
	}
}
