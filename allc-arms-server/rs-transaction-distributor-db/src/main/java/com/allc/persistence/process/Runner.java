/**
 * 
 */
package com.allc.persistence.process;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;

/**
 * @author gustavo
 *
 */
public class Runner extends AbstractService{

	private static Logger log;
	
	public static void main(String[] args) {
		try {
			//configuramos el archivo de log
			Properties prop = new Properties();
			FileInputStream is = new FileInputStream("log4j.properties");
			prop.load(is);
			is.close();
			PropertyConfigurator.configure(prop);
			log = Logger.getLogger(Runner.class);
			log.info("Iniciando aplicaci�n...");
			Process process = new Process();
			process.start();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public int serviceMain(String[] arg0) throws ServiceException {
		try {
			//configuramos el archivo de log
			Properties prop = new Properties();
			FileInputStream is = new FileInputStream("log4j.properties");
			prop.load(is);
			is.close();
			PropertyConfigurator.configure(prop);
			log = Logger.getLogger(Runner.class);
			log.info("Iniciando aplicaci�n...");
			Process process = new Process();
			process.start();
			while(!shutdown){
				Thread.sleep(5000);
			}
			if(shutdown){
				log.info("Bajando servicio");
				process.stopProcess();
				int count = 0;
				while(!process.isEnd() && count < 15){
					Thread.sleep(1000);
					count++;
				}
				if(count == 15)
					process.interrupt();
			}
			log.info("Servicio detenido con exito.");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return 0;
	}
}
