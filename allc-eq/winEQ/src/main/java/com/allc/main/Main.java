package com.allc.main;


import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;

import com.allc.conexion.Conexion;
import com.allc.main.constants.Constants;
import com.allc.main.properties.PropertiesWinEQ;
import com.allc.oswin.event.EventWin;
import com.allc.oswin.event.EventWinLogProcess;
import com.allc.oswin.procesos.UpdateProcessStatus;
import com.allc.saf.Saf;
import com.allc.saf.SafProcess;
import com.allc.util.Interrupcion;
import com.allc.util.Util2;

/**
 * Main Class to obtain the windows Events
 * @author Alexander Padilla
 * @version 1.0
 * @created 03-may-2013 10:28:58
 */
public class Main extends AbstractService {

	/**
	 * @param args
	 */
	static Logger log ;
	public static String IP;
	private static String STORE_NUMBER;
	private static String DES_CADENA;
	private static boolean  CLEANED 		= false;
	private static boolean  END  = false;
	private static String  PC_NAME = "";
	private static ExecutorService EXECUTOR;
	private static UpdateProcessStatus ups;
	static Pattern P;
	
	public static void main(String[] args) {
		try {
			initialize();
			/***********************************************
			 * Fire the threads
			 ***********************************************/
			launcher();
			while (!END) {
				Thread.sleep(20000);
				//Runnable worker = new Task(socket.accept());
				//executor.execute(worker);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}		
	
	
    /**
     * Termina el servicio de Lectura del mensajes
     * @param valor			indica si se termina o no el servicio.
     * @throws IOException
     */
	public static void setEndOfService(boolean valor) {
		try{
			log.info("Bajando aplicación.");
			ups.closure();
			//TODO COMENTADO TEMPORALMENTE
//			EXECUTOR.shutdown();
//			while (!EXECUTOR.isTerminated()) {
//				
//		    }
//			log.info("Finished all threads");
			Main.END = true;
			log.info("Aplicación detenida con éxito.");
		} catch (Exception e){
			log.error(e.getMessage(), e);
		}
		
	}
	
	public int serviceMain(String[] arg0) throws ServiceException {
		try {
			initialize();
			/***********************************************
			 * Fire the threads
			 ***********************************************/
			launcher();
			while(!shutdown){
				Thread.sleep(5000);
			}
			if(shutdown){
				log.info("Bajando servicio");
				ups.closure();
				//TODO COMENTADO TEMPORALMENTE
//				EXECUTOR.shutdown();
//				while (!EXECUTOR.isTerminated()) {
//					
//			    }
//				log.info("Finished all threads");
			}
			log.info("Servicio detenido con exito.");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return 0;
	}
	
	private static void initialize(){
		try {
			P = Pattern.compile(Constants.Comunicacion.REGEX);
			PropertyConfigurator.configure(Constants.Componente.NOMBRE_LOG);
			log = Logger.getLogger(Main.class);
			log.info(Constants.Componente.VERSION + " - " + Constants.Componente.FECHA_VERSION);
			/***********************************************
			 * Hook to prevent the application shutdown
			 **********************************************/
			final Timer queueRunner = new Timer();
			Runtime.getRuntime().addShutdownHook(new Interrupcion(queueRunner));
			/** Obtain the name associate to the computer **/
			InetAddress localHost = InetAddress.getLocalHost();
			PC_NAME = InetAddress.getLocalHost().getHostName();

			/** Obtain one of them IP addresses of the computer **/
			IP = localHost.getHostAddress();
			if (log.isDebugEnabled()) {
				Thread.sleep(PropertiesWinEQ.ParamSEQ.TIME_SLEEP_DEBUG);
				log.debug("Debug Mode: " + PropertiesWinEQ.ParamSEQ.TIME_SLEEP_DEBUG);
			}
			/** The store's number **/
			STORE_NUMBER = PropertiesWinEQ.Parameters.STORE_NUMBER;
			/** Global Store description **/
			DES_CADENA = PropertiesWinEQ.Parameters.DES_CADENA;

			/** if not pass the validation then abort **/
			if (Util2.isBlankOrNull(STORE_NUMBER)) {
				log.error("Parameter STORENUMBER can't be null");
				System.exit(1);
			}
			/** if not pass the validation then abort **/
			if (Util2.isBlankOrNull(DES_CADENA)) {
				log.error("Parameter CADENA can't be null");
				System.exit(1);
			}
			/** if not pass the validation then abort **/
			if (Util2.isBlankOrNull(PropertiesWinEQ.Parameters.TERMINAL_ID)) {
				log.error("Parameter TERMINALID can't be null");
				System.exit(1);
			}

			log.info("TIME_OUT_SLEEP: " + String.valueOf(PropertiesWinEQ.ParamSEQ.TIME_OUT_SLEEP));
			log.info("IP_SERVER: " + PropertiesWinEQ.ParamSEQ.IP_SERVER);
			log.info("PORT_SERVER: " + String.valueOf(PropertiesWinEQ.ParamSEQ.PORT_SERVER));
			log.info("TIME_OUT_CONEXION: " + String.valueOf(PropertiesWinEQ.ParamSEQ.TIME_OUT_CONEXION));
			log.info("FILE_STORE: " + PropertiesWinEQ.ParamSAF.FILE_STORE);
			log.info("FILE_SEEK: " + PropertiesWinEQ.ParamSAF.FILE_SEEK);
			log.info("TIME_SAF: " + PropertiesWinEQ.ParamSAF.TIME_SAF);
			log.info("TERMINAL_ID: " + PropertiesWinEQ.Parameters.TERMINAL_ID);
			log.info("PC_NAME: " + PC_NAME);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}
		
	/**
	 * Fire the thread process
	 */
	private static void launcher(){

		try{
			Runnable worker;
			/**ThreadPool**/
			//TODO COMENTADO TEMPORALMENTE
//			EXECUTOR = Executors.newFixedThreadPool(PropertiesWinEQ.ParamSEQ.CANT_THREAD);
//			log.info("Limit of concurrent processes: " + PropertiesWinEQ.ParamSEQ.CANT_THREAD);
//			
//			log.info("Starting SAF cleaner");
//			/**Instantiate of SAF class**/
//			Saf saf = new Saf();
//			/**set the attributes required by the methods**/
//			saf.setCrlf(Constants.Comunicacion.CRLF);
//			saf.setFileSeek(PropertiesWinEQ.ParamSAF.FILE_SEEK);
//			saf.setFileStored(PropertiesWinEQ.ParamSAF.FILE_STORE);
//			/**Cleaning the data sent**/
//			SafProcess.cleaner(saf);
//
//			/**Wait for finish the Cleaner process **/
//			while(!CLEANED)
//				Thread.sleep(2000);
//			log.info("SAF cleaned");
			
			ups = new UpdateProcessStatus();
			ups.init();
			ups.setStoreNumber(STORE_NUMBER);
			
			log.info("WinEQ Starting processes!");
			ups.start();
			/**fire the SAF thread process**/
			//TODO COMENTADO TEMPORALMENTE
//			worker = makeRunnable(Constants.ProcessConstants.SAF_PROCESS);
//			EXECUTOR.execute(worker);
			
			/**fire the HEARTBEAT Thread process**/
			//TODO COMENTADO TEMPORALMENTE
//			worker = makeRunnable(Constants.ProcessConstants.HEART_BEAT_PROCESS);
//			EXECUTOR.execute(worker);
			
			/**fire the Win event queue process**/
			//TODO COMENTADO TEMPORALMENTE
//			worker = makeRunnable(Constants.ProcessConstants.SAVE_WIN_EVENT_LOG_PROCESS);
//			EXECUTOR.execute(worker);
			



		} catch(Exception e){
			log.error(e.getMessage(), e);
			}		
	}
	
	public static Runnable makeRunnable(final int tipoOperacion /*, final Trama trama*/){
		return new Runnable() {
			public void run() {

				try {
					switch(tipoOperacion){
						/*****************************************************************
						 * Send the events
						 *****************************************************************/
						case Constants.ProcessConstants.SAF_PROCESS:{
							/**set the thread name**/
							Thread.currentThread().setName("WinEQsaf");
							log.info("SAF process started");
							/**Instantiate from Conexion class**/
							Conexion cx = new Conexion();
							/**set the attributes**/
							cx.setIp(PropertiesWinEQ.ParamSEQ.IP_SERVER);
							cx.setPuerto(PropertiesWinEQ.ParamSEQ.PORT_SERVER);
							cx.setReintentos(PropertiesWinEQ.ParamSEQ.REINTENTOS_CX);
							cx.setTimeOutConexion(PropertiesWinEQ.ParamSEQ.TIME_OUT_CONEXION);
							cx.setTimeOutSleep(PropertiesWinEQ.ParamSEQ.TIME_OUT_SLEEP);
							cx.setCantidadBytesLongitud(Constants.Comunicacion.CANTIDAD_BYTES_LONGITUD);
							/**Instantiate from Saf class**/
							Saf saf = new Saf();
							/**set the attributes**/
							saf.setFileSeek(PropertiesWinEQ.ParamSAF.FILE_SEEK);
							saf.setFileStored(PropertiesWinEQ.ParamSAF.FILE_STORE);
							saf.setTimeSAF(PropertiesWinEQ.ParamSAF.TIME_SAF);
							saf.setConexion(cx);
							saf.setCrlf(Constants.Comunicacion.CRLF);
							saf.setCantBytesLongitud(Constants.Comunicacion.CANTIDAD_BYTES_LONGITUD);
							saf.setCantDatosHeader(Constants.Comunicacion.CANTIDAD_DATOS_HEADER);
							saf.setCar(Constants.Comunicacion.CAR);
							saf.setP(P);
							/**execute the process who reads from event log stored and send the information to the SEQ Central service**/
							SafProcess.forward( saf );							
							break;
						}
						/*****************************************************************
						 * send the heart beat
						 *****************************************************************/						
						case Constants.ProcessConstants.HEART_BEAT_PROCESS:{
							/**set the thread name**/
							Thread.currentThread().setName("WinEQbeat");
							log.info("Hearth Beat process started");

							break;
						}
						/*****************************************************************
						 * stored windows event log
						 *****************************************************************/
						case Constants.ProcessConstants.SAVE_WIN_EVENT_LOG_PROCESS:{
							/**set the thread name**/
							Thread.currentThread().setName("WinEQget");
							log.info("Message win event queue process started");
							/**Instantiate from EventWin class**/
							EventWin eventWin = new EventWin();
							/**set the attributes**/
							eventWin.setCrlf(Constants.Comunicacion.CRLF);

							eventWin.setFileSeekEventWin(PropertiesWinEQ.ParamWinEQ.FILE_SEEK_WIN_LOG);
							eventWin.setFileStoreEventWin(PropertiesWinEQ.ParamSAF.FILE_STORE);
							eventWin.setIp(IP);
							eventWin.setP(P);
							eventWin.setStoreNumber(STORE_NUMBER);
							eventWin.setDesCadena(DES_CADENA);
							eventWin.setCtrlNode(PC_NAME);
							/**execute the process who reads from windows event log  and stored the data in the event log **/
							EventWinLogProcess.storedWinEQEvent(eventWin);

							break;
						}
						default: {
							log.error("Operation not defined: " + tipoOperacion);
							break;
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				
				
				
				
				
			}
		};
	}
				
	
	public static void setLimpio(boolean limpio) {
		Main.CLEANED = limpio;
	}	

}
