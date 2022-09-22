/**
 * 
 */
package com.allc.printer.helper;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.Timer;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author GUSTAVOK
 * 
 */
public class Runner {
	private static Logger log;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Properties prop = new Properties();
			FileInputStream fis = new FileInputStream("log4jPrinterTester.properties");
			prop.load(fis);
			fis.close();
			PropertyConfigurator.configure(prop);
			log = Logger.getLogger(Runner.class);
			Process process = new Process();
			final Timer queueRunner = new Timer();
			Runtime.getRuntime().addShutdownHook(new Interrupcion(queueRunner, process));
			process.procesar();
			Runtime.getRuntime().exit(1);
		} catch (Exception e) {
			log.error("Error al ejecutar la aplicaci�n.", e);
		}
	}
	


}
