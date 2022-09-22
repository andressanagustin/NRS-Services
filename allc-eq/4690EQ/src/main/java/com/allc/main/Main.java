package com.allc.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.allc.conexion.Conexion;
import com.allc.conexion.Trama;
import com.allc.main.constants.Constants;
import com.allc.main.properties.Properties4690EQ;
import com.allc.os4690.Util4690;
import com.allc.os4690.event.Event;
import com.allc.os4690.event.Event4690Process;
import com.allc.os4690.exclog.ExcLog;
import com.allc.os4690.exclog.ExcLog4690;
import com.allc.os4690.exclog.ExcLog4690Process;
import com.allc.os4690.pipe.Pi2DAO;
import com.allc.os4690.pipe.Pi2Process;
import com.allc.os4690.pipe.receiver.Receiver;
import com.allc.os4690.pipe.receiver.ReceiverManager;
import com.allc.os4690.procesos.UpdateProcessStatus;
import com.allc.saf.SAF;
import com.allc.saf.SAFCleaner;
import com.allc.saf.SAFProcess;
import com.allc.threadpool.ThreadPool;
import com.allc.threadpool.ThreadUtilities;
import com.allc.util.Interrupcion;
import com.allc.util.Util2;
import com.ibm.OS4690.ControllerApplicationServices;
import com.ibm.OS4690.ControllerStatusData;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileInputStream4690;


/**
 * Main Class:
 * 		4690 Event Queue 
 * 		4690 Exception Log
 * 		Save general events receipt through pi2
 * 
 * @author Alexander Padilla
 * @version X.X
 * @created 02-jan-2013 18:28:58
 */

public class Main {

	static Logger log ;
	static boolean FINMAIN = false;
	static boolean finE4690Q = false;
	static Pattern P;

	private static ControllerStatusData LOCAL_CONTROLLER_STATUS_DATA ;

	public static String IP;
	private static String STORE_NUMBER;
	private static String NODO_CTRL;
	private static Pi2DAO PI2_4690;
	private static Pi2DAO PI2_DAO_FALCON;
	private static Pi2DAO PI2_REDIRECT;
	private static SAF saf;
	private static ExcLog4690 excLog4690;
	private static UpdateProcessStatus ups;
	private static SAF safEL;
	private static ThreadPool pool = null;
	private static Hashtable hashMessages = null;
	public static boolean isFinMain() {
		return FINMAIN;
	}

	public static boolean isFinE4690Q() {
		return finE4690Q;
	}

	public static void main(String[] args) {

		try{
		   P = Pattern.compile(Constants.Comunicacion.REGEX);

		   PropertyConfigurator.configure(Constants.Componente.NOMBRE_LOG);
		   log = Logger.getLogger(Main.class);

		   log.info("===========================");
		   log.info(Constants.Componente.VERSION +" - "+Constants.Componente.FECHA_VERSION);
		   log.info("===========================");
		   /***********************************************
			* Interruption Point
			**********************************************/
		   final Timer queueRunner = new Timer();
		   Runtime.getRuntime().addShutdownHook(new Interrupcion(queueRunner));

		   if(log.isDebugEnabled()){
			  Thread.sleep(Properties4690EQ.ParamSEQ.TIME_SLEEP_DEBUG);
			  log.debug("Debug Mode: " + Properties4690EQ.ParamSEQ.TIME_SLEEP_DEBUG);
		   }
		   
		   LOCAL_CONTROLLER_STATUS_DATA = ControllerApplicationServices.getControllerStatus();

		   NODO_CTRL = LOCAL_CONTROLLER_STATUS_DATA.getControllerId();
		   STORE_NUMBER =  LOCAL_CONTROLLER_STATUS_DATA.getStoreNumber();
		   
	       log.info("PI2EVENT: " + Properties4690EQ.Param4690.PI2_EVENT);
	       log.info("PI2FALCON: " + Properties4690EQ.Param4690.PI2_FALCON);
	       log.info("CNTR TXT FILE_NAME: " + Properties4690EQ.Param4690.CNTR_TXT_FILE_NAME);
	       log.info("TERM TXT FILE_NAME: " + Properties4690EQ.Param4690.TERM_TXT_FILE_NAME);
	       log.info("APPL TXT FILE_NAME: " + Properties4690EQ.Param4690.APPL_TXT_FILE_NAME);
	       log.info("TIMEOUT SLEEP: " + String.valueOf(Properties4690EQ.ParamSEQ.TIME_OUT_SLEEP));
	       log.info("IP SERVER: " + Properties4690EQ.ParamSEQ.IP_SERVER);
	       log.info("PORT SERVER: " + String.valueOf(Properties4690EQ.ParamSEQ.PORT_SERVER));
	       log.info("TIMEOUT CONNECTION: " + String.valueOf(Properties4690EQ.ParamSEQ.TIME_OUT_CONEXION));
	       log.info("FILE STORE: " + Properties4690EQ.ParamSAF.FILE_STORE);
	       log.info("FILE SEEK: " + Properties4690EQ.ParamSAF.FILE_SEEK);
	       log.info("TIME SAF: " + Properties4690EQ.ParamSAF.TIME_SAF);
	       log.info("FILTER BY CONTROLLER: " + Properties4690EQ.Param4690.FILTER_BY_CONTROLLER);
	       log.info("REDIRECTED EVENTS: " + Properties4690EQ.Param4690.REDIRECTED_EVENTS);
	       log.info("CONTROLLER ID: " + NODO_CTRL);
	       log.info("STORE NUMBER: " + STORE_NUMBER);

	       if(Util2.isBlankOrNull(Properties4690EQ.Param4690.DES_CADENA)){
	        	log.error("Parameter CADENA can't be null");
	        	System.exit(1);
	       }
	       
	       /**validation about pipe pi2: ADXCSOUP**/
	       if(Util2.isBlankOrNull(Properties4690EQ.Param4690.PI2_EVENT)){
	        	log.error("Parameter PI2EVENT can't be null");
	        	System.exit(1);
	       }
	       /**try to create the pipe, if it has successful then abort, because the SO doesn't create the pipe**/
	       PI2_4690 = Pi2Process.createPi2ToRead(Properties4690EQ.Param4690.PI2_EVENT, Properties4690EQ.Param4690.FILE_SIZE_PI2);
	       /**if the pi2 was created successful, then abort**/
  		   if(null != PI2_4690){
  			   /**close the pipe created**/
  			   PI2_4690.closePi2Created();
  			   log.error("The pipe " + Properties4690EQ.Param4690.PI2_EVENT + " doesn't exist, is not possible to get event queue messages from OS4690");
  			   System.exit(1);
  		   }else{/**the pi2 is null**/
  		       /**Open the pipe pi2 from 4690 to receive incoming events**/
  		       PI2_4690 = Pi2Process.openPi2ToRead(Properties4690EQ.Param4690.PI2_EVENT, Constants.Comunicacion.CANTIDAD_BYTES_LEE_PIPE_RMA);
  		       if(null != PI2_4690){
  		    	   log.info("pipe: " + PI2_4690.getPi2DTO().getName() + " opened in read mode.");
  		       }else{
  		    	   /**abort if the pipe can't be opened**/
		    	   System.exit(1);
  		       }
  		    	   
  		   }

	       /**Create/Open pi2 to redirect events **/
	       switch (Properties4690EQ.Param4690.REDIRECTED_EVENTS) {
		       	 case	Constants.Redirection.CREATE_PIPE_REDIRECTION:{
		       			PI2_REDIRECT = Pi2Process.createPi2ToBeReadByBorC(Properties4690EQ.Param4690.PI2_REDIRECTED, Properties4690EQ.Param4690.FILE_SIZE_PI2);
		       			break;
		       	}case	Constants.Redirection.OPEN_PIPE_REDIRECTION_IN_WRITE_MODE:{
		       			PI2_REDIRECT = Pi2Process.openPi2ToWrite(Properties4690EQ.Param4690.PI2_REDIRECTED);
		       			break;
		       	}case	Constants.Redirection.NOT_CREATE_PIPE_REDIRECTION:{
		       			log.info("REDIRECTED_EVENTS = 0: not create redirection pipe");
		       			PI2_REDIRECT = null;
		       			break;
		       	}case   Constants.Redirection.OPEN_PIPE_REDIRECTION_IN_WRITE_MODE_IF_FAIL_THEN_CREATE_PIPE_REDIRECTION:{
		       			PI2_REDIRECT = Pi2Process.openPi2ToWrite(Properties4690EQ.Param4690.PI2_REDIRECTED);
		       			if(null == PI2_REDIRECT)
		       				PI2_REDIRECT = Pi2Process.createPi2ToBeReadByBorC(Properties4690EQ.Param4690.PI2_REDIRECTED, Properties4690EQ.Param4690.FILE_SIZE_PI2);
		       			break;
		       	}default:{
		       			log.error("value REDIRECTEDEVENTS: " + Properties4690EQ.Param4690.REDIRECTED_EVENTS + "is not considered, can't redirect events to another pi2");
		       	}
	       }

	       /**open the pipe to receive requirements PI2_FALCON**/
		   PI2_DAO_FALCON = Pi2Process.createPi2ToRead(Properties4690EQ.Param4690.PI2_FALCON, Properties4690EQ.Param4690.FILE_SIZE_PI2);
		   if(null != PI2_DAO_FALCON){
			   log.info("pipe: " + PI2_DAO_FALCON.getPi2DTO().getName() + " created to be read."); 
		   }

		   IP = Util4690.getIpAddress(NODO_CTRL);
		   log.info("Controller's IP: " + IP);

		   /**loading hashtable messages**/
		   if(!Properties4690EQ.Param4690.FILE_MESSAGE_TO_AVOID.equals("")){
			   hashMessages = new Hashtable();
			   loadHashMessage(Properties4690EQ.Param4690.FILE_MESSAGE_TO_AVOID, hashMessages);
		   }

	       /**Star the operations**/
	       launcher();

		}catch(Exception e){
			log.error(e.getMessage(), e);
		}
		log.info("Bye!");
	}

	/**
	 * Fire the processes
	 */
	private static void launcher(){
		try{
			pool = new ThreadPool(Properties4690EQ.ParamSEQ.CANT_THREAD, Properties4690EQ.ParamSEQ.THREAD_GROUP_NAME);
			log.info("Limit of concurrent processes: " + Properties4690EQ.ParamSEQ.CANT_THREAD);
			Runnable rd;

			log.info("Starting SAF cleaner");
			/**Instantiate from class SAF (pojo)**/
			saf = new SAF();
			/**Set the attributes required by the methods**/
			saf.setCrlf(Constants.Comunicacion.CRLF);
			saf.setFileSeek(Properties4690EQ.ParamSAF.FILE_SEEK);
			saf.setFileStore(Properties4690EQ.ParamSAF.FILE_STORE);
			saf.setTimeSAF(Properties4690EQ.ParamSAF.TIME_SAF);
			saf.setCantBytesLongitud(Constants.Comunicacion.CANTIDAD_BYTES_LONGITUD);
			saf.setCantDatosHeader(Constants.Comunicacion.CANTIDAD_DATOS_HEADER);
			saf.setCar(Constants.Comunicacion.CAR);
			saf.setP(P);

			/**Clean the sent data**/
			SAFCleaner cleaner = new SAFCleaner();
			cleaner.cleaner(saf);
			log.info("SAF cleaned");
			/**open the files to work with saf**/
			SAFProcess.init(saf);
			
			/**Instantiate from class ExcLog (pojo)**/
			ExcLog excLog = new ExcLog();
			/**Set the attributes**/
			excLog.setCrlf(Constants.Comunicacion.CRLF);
			excLog.setCtrlNode(NODO_CTRL);
			excLog.setFileNameExcLog(Properties4690EQ.ExceptionLog.FILE_NAME_EXC_LOG);
			excLog.setFileSeekExcLog(Properties4690EQ.ExceptionLog.FILE_SEEK_EXC_LOG);
			excLog.setIp(IP);
			excLog.setP(P);
			excLog.setStoreNumber(STORE_NUMBER);
			excLog.setHash(hashMessages);
			
			/**SAF 4 exception log**/
			safEL = new SAF();
			safEL.setCrlf(excLog.getCrlf());
			safEL.setCantDatosHeader(Constants.Comunicacion.CANTIDAD_DATOS_HEADER);
			safEL.setCar(Constants.Comunicacion.CAR);
			
			excLog4690 = new ExcLog4690(excLog);
			/**open the files to work with exception log**/
			excLog4690.init();
			ups = new UpdateProcessStatus();
			ups.init();
			ups.setCtrlNode(NODO_CTRL);
			ups.setStoreNumber(STORE_NUMBER);
			log.info("4690EQ Starting processes");
			ups.start();
			/**Fire the SAF Thread Permanent**/
			rd = makeRunnable(Constants.ProcessConstants.SAF_PROCESS, null);
			pool.execute(rd);
			
			/**Fire the Permanent Thread to get the 4690 messages event queue **/
			rd = makeRunnable(Constants.ProcessConstants.SAVE_4690_EVENT_QUEUE_PROCESS, null);
			pool.execute(rd);
			
			if(!Properties4690EQ.ExceptionLog.FILE_NAME_EXC_LOG.equals(Constants.Comunicacion.VACIO)){
				/**Fire the permanent Thread to get the Execption log messages event queue**/
				rd = makeRunnable(Constants.ProcessConstants.SAVE_4690_EXCEPTION_LOG_PROCESS, null);
				pool.execute(rd);
			}else{
				log.info("Exception log file not defined. configure the parameter FILENAMEEXCEPTIONLOG");
			}
			
			/**Fire the permanent thread to ensure that the others processes still alive**/
			//rd = makeRunnable(Constants4690EQ.ProcessConstants.IS_ALIVE_PROCESS, null);
			//pool.execute(rd);
			
			ReceiverManager receiverManager = null;
			if(null != PI2_DAO_FALCON){
				/**Instantiate from class Receiver (pojo)**/
				Receiver receiver = new Receiver();
				/**Set the attributes**/
				receiver.setCantBytesLongitud(Constants.Comunicacion.CANTIDAD_BYTES_LONGITUD);
				receiver.setCantDatosHeader(Constants.Comunicacion.CANTIDAD_DATOS_HEADER);
				receiver.setCar(Constants.Comunicacion.CAR);
				receiver.setNombrePipe(Properties4690EQ.Param4690.PI2_FALCON);
				receiver.setP(P);
				receiver.setPosPipeInputStream(PI2_DAO_FALCON.getPi2DTO().getPosPipeInputStream());
				receiver.setTimeOutSleep(Properties4690EQ.ParamSEQ.TIME_OUT_SLEEP);
				/**Instantiate from class Receiver to get data from pi2**/
				receiverManager = new ReceiverManager(receiver);
				log.info("PI2FalconReader process started");
			}
		
			/**Declare the trama object to receive data from ReceiverManager Class**/
			Trama trama;
			int tipoOperacion;
			/**Read the general pipe**/
			while (!FINMAIN) {
				if(null != PI2_DAO_FALCON){
					/**capture the trama get from pipe pi2**/
					trama = receiverManager.capture();
					/**if exists data**/
					if (!(null == trama)){
						/**Get the operation type from the trama object**/
						tipoOperacion = Integer.parseInt((String)trama.getHeader().get(1));
						/**Fire the requirement thread**/
						Runnable rs = makeRunnable(tipoOperacion, trama);
						pool.execute(rs);
					} else
						if(log.isDebugEnabled())
							log.debug("got null object Trama from " + Properties4690EQ.Param4690.PI2_FALCON);
				}else{
					Thread.sleep(2000);
				}
			}
		} catch(Exception e){
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Method to fire one thread in a ThreadPool
	 * @param OperationType		Identifies the execution process 	
	 * @param trama				Object who contain the data relative to the process
	 * @return
	 */
	public static Runnable makeRunnable(final int OperationType , final Trama trama){
		return new Runnable() {
			public void run() {
				
				switch(OperationType){
					/*****************************************************************
					 * SEND THE EVENTS FROM 4690
					 *****************************************************************/
					case Constants.ProcessConstants.SAF_PROCESS:{
						/**Set the thread's name**/
						Thread.currentThread().setName("4690EQsaf");
						log.info("SAF process started");
						/**Instantiate the Conexion class (pojo)**/
						Conexion cx = new Conexion();
						/**Set the attributes**/
						cx.setIp(Properties4690EQ.ParamSEQ.IP_SERVER);
						cx.setPuerto(Properties4690EQ.ParamSEQ.PORT_SERVER);
						cx.setReintentos(Properties4690EQ.ParamSEQ.REINTENTOS_CX);
						cx.setTimeOutConexion(Properties4690EQ.ParamSEQ.TIME_OUT_CONEXION);
						cx.setTimeOutSleep(Properties4690EQ.ParamSEQ.TIME_OUT_SLEEP);
						cx.setCantidadBytesLongitud(Constants.Comunicacion.CANTIDAD_BYTES_LONGITUD);

						saf.setConexion(cx);

						/**Fire the process who reads and sent the data**/
						SAFProcess.forward( saf, hashMessages );
						break;
					}
					/*****************************************************************
					 * assurance of the processes still running 
					 *****************************************************************/						
					case Constants.ProcessConstants.IS_ALIVE_PROCESS:{
						try {
							/**Set the thread's name**/
							Thread.currentThread().setName("4690EQWatcher");
							//while(true){
								
								//Thread.sleep(TIMESAF * 10);
								Thread.sleep(30L);
								Thread[] tarray = ThreadUtilities.getGroupThreads(Properties4690EQ.ParamSEQ.THREAD_GROUP_NAME);
								List list = Arrays.asList(tarray);
								for(int i=0; i < list.size();i++){
									log.info(((Thread)list.get(i)).getName() +  (((Thread)list.get(i)).isAlive() ? " Alive" : " Death")) ;
									//if(!((Thread)list.get(i)).isAlive()){
										/**fire thread to verify that the others processes still alive**/
										//rd = makeRunnable(Constants4690EQ.ProcessConstants.IS_ALIVE_PROCESS, null);
										//pool.execute(rd);
									//}
								}
							//}
						} catch (Exception e) {
							log.error(e);
						}
						break;
					}
					/*****************************************************************
					 * GET AND SAVE EVENTS FROM 4690 
					 *****************************************************************/						
					case Constants.ProcessConstants.SAVE_4690_EVENT_QUEUE_PROCESS:{
						/**Set the thread's name**/
						Thread.currentThread().setName("4690EQget");
						log.info("Message Event Queue process started");
						/**Instantiate from class Event (pojo)**/
						Event event = new Event();
						/**Set the attributes**/
						event.setControllerId(NODO_CTRL);
						event.setStoreNumber(STORE_NUMBER);
						event.setApplTxtFileName(Properties4690EQ.Param4690.APPL_TXT_FILE_NAME);
						event.setCntrTxtFileName(Properties4690EQ.Param4690.CNTR_TXT_FILE_NAME);
						event.setTermTxtFileName(Properties4690EQ.Param4690.TERM_TXT_FILE_NAME);
						event.setCantBytesLeePipe(Constants.Comunicacion.CANTIDAD_BYTES_LEE_PIPE_RMA);
						event.setDesCadena(Properties4690EQ.Param4690.DES_CADENA);
						event.setProgramSource(Constants.Componente.EQ_4690_JAR);
						event.setIp(IP);
						event.setCrlf(Constants.Comunicacion.CRLF);
						event.setCar(Constants.Comunicacion.CAR);
						event.setFileStore(Properties4690EQ.ParamSAF.FILE_STORE);
						event.setTimeOutSleep(Properties4690EQ.ParamSEQ.TIME_OUT_SLEEP);
						event.setCantDatosHeader(Constants.Comunicacion.CANTIDAD_DATOS_HEADER);
						event.setHash(hashMessages);
						/**pipe to get the information events from os4690**/
						event.setPi24690(PI2_4690);
						/**pipe to redirect the events to another pipe pi2**/
						event.setPi2Redirected(PI2_REDIRECT);
						/**set the action: redirect events: 1, 2, 3, no redirect events: 0**/
						event.setRedirectedEvents(Properties4690EQ.Param4690.REDIRECTED_EVENTS);
						/**start the process to read the event queue from 4690 to save it in the event log**/
						Event4690Process.stored4690Event(event);
						break;
					}
					/*****************************************************************
					 * GET AND SAVE EVENTS FROM EXCEPTION LOG
					 *****************************************************************/
					case Constants.ProcessConstants.SAVE_4690_EXCEPTION_LOG_PROCESS:{
						/**Set the thread's name**/
						Thread.currentThread().setName("4690ELget");
						log.info("Exception Log process started");
						/**start the process to read the exception log from 4690 to save it in the event log**/
						ExcLog4690Process.storeExcLogEvent(excLog4690, safEL);
						break;
					}
					/*****************************************************************
					 * SAVE EVERY MESSAGE WHO COMES FROM PI2
					 ****************************************************************/
					case Constants.ProcessConstants.LAUNCH_PROCESS_4690:{
						/**Set the thread's name**/
						Thread.currentThread().setName("4690EQPi2");
						/**Instantiate from SAF class (pojo)**/
						SAF saf = new SAF();
						/**set the attributes**/
						saf.setFileSeek(Properties4690EQ.ParamSAF.FILE_SEEK);
						saf.setFileStore(Properties4690EQ.ParamSAF.FILE_STORE);
						saf.setTimeSAF(Properties4690EQ.ParamSAF.TIME_SAF);
						saf.setCrlf(Constants.Comunicacion.CRLF);
						saf.setCar(Constants.Comunicacion.CAR);
						/**get the list**/
						List lista = trama.getList();
						/**set the additional attributes**/
						lista.set(3, Properties4690EQ.Param4690.DES_CADENA);
						lista.set(4, STORE_NUMBER);
						lista.set(8, IP);
						/**modify the element number 3 of body, The header has CANTIDADDATOSHEADER elements, minus 1 gets the final position of header
						 * 4 positions after comes the controller ID ( position 2 of body )**/
						lista.set(Constants.Comunicacion.CANTIDAD_DATOS_HEADER + 3, NODO_CTRL);
						/**Instantiate from Trama Class using contructor**/
						Trama tramaTmp = new Trama(lista, Constants.Comunicacion.CANTIDAD_DATOS_HEADER, Constants.Comunicacion.CAR);
						/**without problems**/
						if(tramaTmp.loadData()){
							/**Instantiate from ExcLog4690 Class**/
							ExcLog4690 excLog4690 = new ExcLog4690(null);
							/**save the event log**/
							excLog4690.storeExceptionLogEvent(tramaTmp, saf);
						}else{
							log.error("Wrong Object trama: " + tramaTmp.toString() + " - " + tramaTmp.getError());
						}
						break;
					}
					default: {
						log.error("Operation not defined: " + OperationType);
						break;
					}
				}

				
			}
		};
	}				
	
	
	public static void loadHashMessage(String fileName, Hashtable hash){
		try {

		    BufferedReader br = null;
		    String linea;
	    	try {
	    		File4690 file = new File4690(fileName);
	    		if(!file.exists())
	    			file.createNewFile();
	    		br = new BufferedReader(new InputStreamReader(new FileInputStream4690(fileName)));
				while((linea=br.readLine())!=null){
					if(linea.trim()!="")
						hash.put(linea, "");
				}
			}catch(Exception e){
				log.error(e.getMessage(), e);
		    }finally{
		         try{      
		        	if(null != br){
		        		br.close();
		        	}                
		         }catch (Exception e2){ 
		         }
	        }
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	
    /**
     * End the service
     * @param valor			Boolean true = finish the service
     * @throws IOException
     */
	public static void setEndOfService(boolean valor) throws IOException{

			try{
				log.info("Finalizando Proceso.");
				finE4690Q = valor;
				/**close the files to work with saf**/
				SAFProcess.closure();
				/**close the files to work with exception log**/
				ExcLog4690Process.closure(excLog4690);
				ups.closure();
				/**stop the thread pool**/
				pool.stopRequestIdleWorkers();
				pool.stopRequestAllWorkers();
				/**finish the application**/
				FINMAIN = valor;
				log.info("Proceso Finalizado.");
			}catch(Exception e){
				log.error(e.getMessage(), e);
			}

	}
	
	
	
}
