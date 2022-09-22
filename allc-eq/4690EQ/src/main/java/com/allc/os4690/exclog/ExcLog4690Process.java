package com.allc.os4690.exclog;

import org.apache.log4j.Logger;

import com.allc.main.properties.Properties4690EQ;
import com.allc.saf.SAF;




public class ExcLog4690Process {
	static Logger log = Logger.getLogger(ExcLog4690Process.class);

	/**
	 * Get the  events from exception log
	 * @param excLog	object who contain the data
	 */
	public static boolean isFinExLog = false;
	public static boolean isFinStoreExcLog = false;
	
	
	public static void storeExcLogEvent(ExcLog4690 excLog4690, SAF saf){
		try {

			while(!isFinExLog){
				try{
					Thread.sleep(Properties4690EQ.ExceptionLog.TIME_EXC_LOG);
				}catch(InterruptedException e){
					continue;
				}
				/**Verifying if exists data to read from exception log**/
				if(excLog4690.permiteRegistroLog()){
					/**get & save the exception log messages**/
					excLog4690.storeExceptionLog(saf);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		isFinStoreExcLog = true;
	}
	
	
	public static void init(){
		try {
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public static void closure(ExcLog4690 excLog4690){
		try {
			log.info("Finalizando ExcLog Process.");
			isFinExLog = true;
			/**wait until the process finish**/
			while(!isFinStoreExcLog)
				Thread.sleep(500);
			log.info("Finalizando ExcLog.");
			excLog4690.closure();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	
}
