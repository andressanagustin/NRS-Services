package com.allc.oswin.event;

import org.apache.log4j.Logger;

import com.allc.main.constants.Constants;
import com.allc.main.properties.PropertiesWinEQ;
import com.allc.saf.Saf;



public class EventWinLogProcess {

	static Logger log = Logger.getLogger(EventWinLogProcess.class);
	
	
	public static void storedWinEQEvent(EventWin eventWin){
		
		try{
			/**Instantiate from SAF pojo to save the data**/
			Saf saf = new Saf();
			/**set the parameters to stored events**/
			saf.setFileStored(eventWin.getFileStoredEventWin());
			saf.setCrlf(eventWin.getCrlf());
			saf.setCantDatosHeader(Constants.Comunicacion.CANTIDAD_DATOS_HEADER);
			saf.setCar(Constants.Comunicacion.CAR);
			/**Instantiate from EventWinLog to process the windows event logs**/
			EventWinLog eventWinLog = new EventWinLog(eventWin);
			while(true){
				/**set the execution time to get the Windows event logs**/
				Thread.sleep(PropertiesWinEQ.ParamWinEQ.TIME_EVENT_WIN_LOG);
				/**Fire the windows event log process**/
				eventWinLog.registerEvents(eventWin);
			}
		} catch (Exception e) {
			log.error("storedWinEQEvent: " + e);
		}
	}
	
	
	
	



}
