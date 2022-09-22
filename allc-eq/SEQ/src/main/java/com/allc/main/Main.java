package com.allc.main;

import java.io.IOException;
import java.util.Timer;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;

import com.allc.main.constants.Constants;
import com.allc.util.Interrupcion;



public class Main extends AbstractService {
	private static Process process = null;
	private static Logger log ;

	public static void main(String[] args) {
		try {
			PropertyConfigurator.configure(Constants.Componente.NOMBRE_LOG);
			log = Logger.getLogger(Main.class);
			log.info(Constants.Componente.VERSION +" - "+Constants.Componente.FECHA_VERSION);
			/***********************************************
			 * Hook the app to prevent shutdown
			 **********************************************/		
			final Timer queueRunner = new Timer();
			Runtime.getRuntime().addShutdownHook(new Interrupcion(queueRunner)); 
			log.info("Iniciando aplicaci�n...");
			process = new Process();
			process.start();
		}catch (Exception e) {
			log.error(e);
		}	
			
	}
	
    /**
     * Termina el servicio de Lectura del mensajes
     * @param valor			indica si se termina o no el servicio.
     * @throws IOException
     */
	public static void setEndOfService(boolean valor) throws IOException{
		try{
			log.info("Bajando aplicación.");
			if(process != null){
				process.stopProcess();
				int count = 0;
				while(!process.isEnd() && count < 15){
					Thread.sleep(1000);
					count++;
				}
				if(count == 15)
					process.interrupt();
			}
			log.info("Aplicación detenida con éxito.");
		} catch (Exception e){
			log.error(e.getMessage(), e);
		}
		
	}

//	@Override 
	public int serviceMain(String[] arg0) throws ServiceException {
		try {
			PropertyConfigurator.configure(Constants.Componente.NOMBRE_LOG);
			log = Logger.getLogger(Main.class);
			log.info(Constants.Componente.VERSION +" - "+Constants.Componente.FECHA_VERSION);
			/***********************************************
			 * Hook the app to prevent shutdown
			 **********************************************/		
			final Timer queueRunner = new Timer();
			Runtime.getRuntime().addShutdownHook(new Interrupcion(queueRunner)); 
			log.info("Iniciando aplicaci�n...");
			process = new Process();
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
		}catch (Exception e) {
			log.error(e);
		}
		return 0;
	}
	
}
