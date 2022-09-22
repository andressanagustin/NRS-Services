package com.allc.oslinux.event;

import org.apache.log4j.Logger;

import com.allc.main.constants.Constants;
import com.allc.main.properties.PropertiesLinuxEQ;
import com.allc.saf.Saf;



public class EventLinuxLogProcess {

	static Logger log = Logger.getLogger(EventLinuxLogProcess.class);
	
	
	public static void storedLinuxEQEvent(EventLinux eventWin){
		
		try{
			/**Instantiate from SAF pojo to save the data**/
			Saf saf = new Saf();
			/**set the parameters to stored events**/
			saf.setFileStored(eventWin.getFileStoredEventWin());
			saf.setCrlf(eventWin.getCrlf());
			saf.setCantDatosHeader(Constants.Comunicacion.CANTIDAD_DATOS_HEADER);
			saf.setCar(Constants.Comunicacion.CAR);
			/**Instantiate from EventWinLog to process the windows event logs**/
			EventLinuxLog eventWinLog = new EventLinuxLog(eventWin);
			while(true){
				/**set the execution time to get the Windows event logs**/
				Thread.sleep(PropertiesLinuxEQ.ParamLinuxEQ.TIME_EVENT_LINUX_LOG);
				/**Fire the windows event log process**/
				eventWinLog.registerEvents(eventWin);
			}
		} catch (Exception e) {
			log.error("storedWinEQEvent: " + e);
		}
	}
	
	
	
	



}
