package com.allc.oswin.event;


import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.allc.conexion.Trama;
import com.allc.files.Files;
import com.allc.main.constants.Constants;
import com.allc.main.properties.PropertiesWinEQ;
import com.allc.main.properties.PropertyFileLoader;
import com.allc.saf.Saf;
import com.allc.saf.SafProcess;
import com.allc.util.Util2;
import com.sun.jna.platform.win32.Advapi32Util.EventLogIterator;
import com.sun.jna.platform.win32.Advapi32Util.EventLogRecord;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.EVENTLOGRECORD;

public class EventWinLog {

	static Logger log = Logger.getLogger(EventWinLog.class);
	static RandomAccessFile randStore = null;
	EventWin eventWin;
	/**Constructor**/
	public EventWinLog(EventWin eventWin) {
		this.eventWin = eventWin;
	}

	/**
	 * Method to stored the event logs
	 * @param trama
	 * @param saf
	 */
	public void storedWinLogEvent(Trama trama, Saf saf){
		try {
			 if(log.isDebugEnabled())
				 log.debug(trama.toString());
			 String msgToSend = trama.listToStr();// trama.getHeaderStr() + saf.getCar() + trama.getBodyStr();
			 //log.info("msgToSend: " + msgToSend);
			 SafProcess.stored(saf, msgToSend);

		}catch (Exception e) {
			log.error("storedWinLogEvent: " + e);
		}
	}

/*	*//**
	 * Obtain the last date time event stored in fileName
	 * @param fileName file who contains the data
	 * @return	The last date event stored
	 *//*
	Date getLastEventDateStored(String fileName) {
		Date datePointer = null;
		String data;
		try {
			data = Files.leerLineaArchivo(fileName, 1);
			if(Util2.isBlankOrNull(data))
				datePointer = Util2.convertStringToDate("19750606010101","yyyyMMddHHmmss");
			else
				try {
					datePointer = Util2.convertStringToDate(data,"yyyyMMddHHmmss");
				} catch (Exception e) {
					log.error("getLastEventDateStored: " + e + " datePointer: " + datePointer + " now we've to put a default date: 19750606010101");
					datePointer = Util2.convertStringToDate("19750606010101","yyyyMMddHHmmss");
				}
		}catch ( Exception e ){
			log.error("getLastEventDateStored: " + e + " now we put a default date: 19750606010101");
			datePointer = Util2.convertStringToDate("19750606010101","yyyyMMddHHmmss");
		}
		return datePointer;
	}

	*//**
	 * Obtain the last event record number stored
	 * @param fileName who contains the data
	 * @return	the last event record number stored
	 *//*
	long getLastEventNumberRecordStored(String fileName) {
		String  lastEventNumber = null;
		long data;
		try {
			lastEventNumber = Files.leerLineaArchivo(fileName,2);
			if(Util2.isBlankOrNull(lastEventNumber))
				data = 0;
			else
				try {
					data = Long.parseLong(lastEventNumber.trim());
				} catch (Exception e) {
					log.error("getLastEventNumberRecordStored: " + e + " lastEventNumber: " + lastEventNumber + " now we've to put a default: -1");
					data = -1;
				}
		}catch ( Exception e ){
			log.error("getLastEventNumberRecordStored: " + e + " now we put a default lastEventNumber: -1");
			data = -1;
		}
		return data;
	}*/

	
	/**
	 * Method to stored a win log event.
	 * @param saf
	 * @param event
	 * @param record
	 * @return	true if the win log event was registered, false in other case
	 */
	public boolean storedWinLog(Saf saf, EventWin event, EventLogRecord record){
		boolean b_exito = false;
		long recordNumber;
		String data = "";
		Trama trama;
		int severity;
		String eventData = "";
		long epoch;
		String eventDateTime;
		try {
			 recordNumber = record.getRecordNumber();
			 severity = getSeverity(String.valueOf(record.getType()));
			 /**try to obtain the human event datetime**/
			 EVENTLOGRECORD eventLogRecord= record.getRecord();
			 epoch = Long.valueOf(String.valueOf(eventLogRecord.readField("TimeGenerated"))).longValue();
			 /**convert the epoch format time to yyyyMMddHHmmss format**/
			 eventDateTime = obtainEpochDate(epoch);
			 /**messages**/
		     String[] messages = record.getStrings();
		     if(null != messages){
			    for(int i = 0; i < messages.length ; i++){
			    	eventData = (Util2.isBlankOrNull(eventData)?"":eventData + " ") + messages[i];
			    }
			    if(log.isDebugEnabled())
			    	log.debug("Strings: " + eventData);
		     }
		     /**Another messages**/
			 if(null != record.getData()){
				 String tmpData = new String(record.getData());
				eventData = (Util2.isBlankOrNull(eventData)?"":eventData + " ") + tmpData;
				if(log.isDebugEnabled())
					log.debug("data: " + Util2.validaNotNull(tmpData) );
			 }
			 /**Remove Spaces**/
			 eventData = eventData.trim();
			 /**If is empty data then put a space**/
			 eventData = eventData.equals(Constants.Comunicacion.VACIO) ? Constants.Comunicacion.SPACE : eventData;
			 
			 /**assembling the data to save**/
			 data = /**Communication Channel**/
				   Constants.Comunicacion.SOCKET_CHANNEL + saf.getCar() +
				   /**Request Operation Type**/
				   Constants.ProcessConstants.SAVE_WIN_EVENT_LOG_PROCESS + saf.getCar() + 
				   /**Idle**/
				   Constants.Comunicacion.VACIO + saf.getCar() + 
				   /**Global Store description**/
				   event.getDesCadena() + saf.getCar() + 
				   /**Store Number**/
				   event.getStoreNumber() + saf.getCar() + 
				   /**Communication Date Time**/
				   Util2.fechaFormato(Calendar.getInstance().getTime()) + saf.getCar() + 
				   /**Event Date Time**/
				   eventDateTime + saf.getCar() + 
				   /**Process ID**/
				   Constants.Componente.WIN_EQ_JAR + saf.getCar() + 
				   /**IP Source**/
				   event.getIp() + saf.getCar() + 
				   /**Controller ID/PC Name**/
				   event.getCtrlNode() + saf.getCar() + 
				   /**Terminal ID**/
				   PropertiesWinEQ.Parameters.TERMINAL_ID + saf.getCar() + 
				   /**Message Group**/
				   Constants.Event.MESSAGE_GROUP + saf.getCar() + 
				   /**Message Number**/
				   recordNumber + saf.getCar() + 
				   /**SourceNumber**/
				   Constants.Event.SOURCE + record.getSource() + saf.getCar() + 
				   /**EventNumber**/
				   Constants.Event.EVENT + record.getEventId() + saf.getCar() + 
				   /**Severity**/
				   severity + saf.getCar() + 
				   /**Data**/
				   eventData ;
			 
			/**remove all 0D0A**/
			data = data.replaceAll("[" + Constants.Comunicacion.CRLF + "]", Constants.Comunicacion.SPACE);
			/**Create List from String separated by "|" **/
			List<String> list = Arrays.asList(eventWin.getP().split(data));
			/**Instantiate of Trama Class**/
			trama = new Trama(list, saf.getCantDatosHeader(), saf.getCar());
			/**Trama Data loaded**/
			if(trama.loadData()){
				/**stored the event**/
				storedWinLogEvent(trama, saf);
				/**stored the last event record number, and the datetime event in the file eventWin.getFileSeekEventWin()**/
				//if(Files.creaEscribeDataArchivo(eventWin.getFileSeekEventWin(), eventDateTime + saf.getCrlf() + String.valueOf(recordNumber) + saf.getCrlf(), false)){
				//	log.info("Last record stored: " + recordNumber + " occurred at " + eventDateTime);
					b_exito = true;
				//}
			}else{
				log.error("Wrong Trama: " + trama.toString() + " - " + trama.getError());
			}
		} catch (Exception e) {
			log.error("storedWinLog: " + e);
		}
		return b_exito;
	}
	
	/**
	 * Data la cadena tipo del evento, se obtiene el numero de severidad que le corresponde
	 * @param eventType
	 * @return			numero de severidad del evento( 1 mayor severidad al 5 menor severidad)
	 */
	private int getSeverity(String eventType){
		int severity = 0;
		try {
			if(Constants.DescSeverity.DESC_CODE_AUDIT_SUCCESS.equals(eventType))
				severity = Constants.Severity.CODE_AUDIT_SUCCESS;
			else
				if(Constants.DescSeverity.DESC_CODE_INFORMATION.equals(eventType))
					severity = Constants.Severity.CODE_INFORMATION;
				else
					if(Constants.DescSeverity.DESC_CODE_WARNING.equals(eventType))
						severity = Constants.Severity.CODE_WARNING;	
					else
						if(Constants.DescSeverity.DESC_CODE_AUDIT_FAILURE.equals(eventType))
							severity = Constants.Severity.CODE_AUDIT_FAILURE;
						else
							if(Constants.DescSeverity.DESC_CODE_ERROR.equals(eventType))
								severity = Constants.Severity.CODE_ERROR;
							else{
								log.error("Severity non identified: " + eventType);
								severity = 0;
							}
		} catch (Exception e) {
			log.error("getSeverity: " + e);
		}
		return severity;
	}
	
	/**
	 * realiza un barrido de los eventos de windows, identificando los eventos de aplicaciones que desea registrar
	 * registra el ultimo numero de registro de windows procesado, para que cuando se ejecute la siguiente vez, solo procese a partir de este registro.
	 * @param eventWin	Pojo con los parametros 
	 */
	public void registerEvents(EventWin eventWin){
		List<String> list;
		/**define the course to get the event records, by default is forward**/
		int route ;
		Date eventDateTime;
		String dat = null;
		EventLogIterator iter = null;
		Date lastRecordDate = null;
		long lastRecordNumber = 0;
		/**map used to register the last records checked & saved**/
		HashMap<String, String> hash = null ;
		boolean isRecordRegistered;
		EventLogRecord record;
		long recordNumber;
		EVENTLOGRECORD eventLogRecord;

		try {
			
			/**If file exists then is not the first execution, then looking for backwards events, from the end to the start**/
			if(Files.fileExists(PropertiesWinEQ.ParamWinEQ.FILE_SEEK_WIN_LOG)){
				route = WinNT.EVENTLOG_BACKWARDS_READ;
				/**create a new HashMap with the values of the file PropertiesWinEQ.ParamWinEQ.FILE_SEEK_WIN_LOG**/
				hash = PropertyFileLoader.getProperties(PropertiesWinEQ.ParamWinEQ.FILE_SEEK_WIN_LOG);
				/**if load without errors**/
				if(null != hash){
					lastRecordDate = getLastRecordDate(hash);
					lastRecordNumber = getLastRecordNumber(hash);
				}else{
					/**load with errors**/
					hash = new HashMap<String, String>();
					lastRecordDate = Util2.convertStringToDate("19750606010101","yyyyMMddHHmmss");
					lastRecordNumber = 0;
				}

			/**if not, it means is the first execution**/
			}else{
				route = WinNT.EVENTLOG_FORWARDS_READ;
				/**The file PropertiesWinEQ.ParamWinEQ.FILE_SEEK_WIN_LOG not exist, create the HashMap**/
				hash = new HashMap<String, String>();
				/**Default values 'cause is the first execution**/
				lastRecordDate = Util2.convertStringToDate("19750606010101","yyyyMMddHHmmss");
				lastRecordNumber = 0;
			}
			if(log.isDebugEnabled()){
				log.debug("lastRecordNumber: " + lastRecordNumber + " lastRecordDate: " + lastRecordDate);
				log.debug("Processing: " + (route==WinNT.EVENTLOG_FORWARDS_READ?"FORWARDS_READ":"BACKWARDS_READ") );
			}
			/**Instantiate from SAF pojo to do the register process**/
			Saf saf = new Saf();
			/**to do the stored only this data is required**/
			saf.setFileStored(eventWin.getFileStoredEventWin());
			saf.setCrlf(eventWin.getCrlf());
			saf.setCantDatosHeader(Constants.Comunicacion.CANTIDAD_DATOS_HEADER);
			saf.setCar(Constants.Comunicacion.CAR);

			Pattern P = Pattern.compile(Constants.Comunicacion.REGEX);
			/**Obtain the filters Event Source in a list**/
			list = Arrays.asList(P.split(PropertiesWinEQ.Parameters.EVENT_SOURCE));
			/**set the data level to obtain**/
			//EventLogIterator iter = new EventLogIterator("Application");
			//iter = new EventLogIterator("SRVSTSAND01", PropertiesWinEQ.Parameters.EVENT_TYPES , route);
			iter = new EventLogIterator(PropertiesWinEQ.Parameters.COMPUTER_SOURCE==""?null:PropertiesWinEQ.Parameters.COMPUTER_SOURCE, PropertiesWinEQ.Parameters.EVENT_GROUP , route);
			/**Iteration**/
			while(iter.hasNext()) {
				isRecordRegistered = false;
			    record = iter.next();
			    recordNumber = record.getRecordNumber();
		    	/**we've to get the event time to stored only the events no registered, read in backwards**/
		    	eventLogRecord= record.getRecord();
		    	try{
		    		dat = obtainEpochDate(Long.valueOf(String.valueOf(eventLogRecord.readField("TimeGenerated"))).longValue());
		    		eventDateTime = Util2.convertStringToDate(dat,"yyyyMMddHHmmss");
		    		if(log.isDebugEnabled())
		    			log.debug("Processing RecordNumber: " + recordNumber + " DatetimeEventWin: " + eventDateTime );
		    	}catch(Exception e){
		    		log.error("Error: Record Number: " + recordNumber + " " + e );
		    		continue;
		    	}			    
			    
			    /**for being the first time that get the WinEventLog, get all events without take care about the event datetime **/
			    if(route == WinNT.EVENTLOG_FORWARDS_READ){
			    	isRecordRegistered = registerWinLogEvent(eventWin, list, saf, record);
			    /**read in WinNT.EVENTLOG_BACKWARDS_READ;**/
			    }else{

				    /**if there is new events(date of events are more recently that the last one stored, or it�s a new event occurred at the same time that the last event stored, but it wasn't the last event record number stored)**/
				    if( (eventDateTime.getTime() > lastRecordDate.getTime()) || ((eventDateTime.getTime() == lastRecordDate.getTime()) && (recordNumber > lastRecordNumber) ) ){
					    /**Stored the win log event**/
				    	isRecordRegistered = registerWinLogEvent(eventWin, list, saf, record);
				    }else{
				    	if(log.isDebugEnabled())
				    		log.debug("Stop getting events BACKWARDS");
				    	/**since the direction is backwards, when i found the first event that was stored then we must finish the loop because the coming events were registered successfully**/
				    	break;
				    }
			    }
			    /**always mark the last date & number record checked**/
			    hash.put("lastRecordDateChecked", dat);
			    hash.put("lastRecordNumberChecked", String.valueOf(recordNumber));
			    /**if a record was registered then save the records into file**/
			    if(isRecordRegistered){
			    	hash.put("lastRecordDateStored", dat);
			    	hash.put("lastRecordNumberStored", String.valueOf(recordNumber));
			    	saveHashMapIntoFile(hash, PropertiesWinEQ.ParamWinEQ.FILE_SEEK_WIN_LOG);
			    	log.info("Last record Number stored: " + recordNumber + " occurred at " + eventDateTime);
			    }
			    	
			}
		} catch (Exception e) {
			log.error("registerEvents: " + e);
		} finally{
			if(null != iter)
			   iter.close();

			/**only save the hashmap into the file if there was a new event in the windows event queue**/
			if( getLastRecordDate(hash).getTime() > lastRecordDate.getTime() ){
				/**to ensure the last Date & number register reviewed , save the data into file**/
				saveHashMapIntoFile(hash, PropertiesWinEQ.ParamWinEQ.FILE_SEEK_WIN_LOG);
				log.info("Hashmap save into file: " + PropertiesWinEQ.ParamWinEQ.FILE_SEEK_WIN_LOG);
				log.info("Last record Number stored: " + hash.get("lastRecordNumberStored") + " occurred at " + hash.get("lastRecordDateStored") + " Last record Number checked: " + hash.get("lastRecordNumberChecked") + " occurred at " + hash.get("lastRecordDateChecked"));
			}
		}

	}

	/**
	 * @param eventWin
	 * @param list
	 * @param saf
	 * @param record
	 */
	private boolean registerWinLogEvent(EventWin eventWin, List<String> list,	Saf saf, EventLogRecord record) {
		boolean registro = false;
		/**if we want all events (*) **/
		if(PropertiesWinEQ.Parameters.EVENT_SOURCE.equals("*")){
			log.info("Record number: " + record.getRecordNumber() + ": Event ID: " + record.getEventId() + ", Event Type: " + record.getType() + ", Event Source: " + record.getSource() /*+ ", Event log data: " + new String(record.getData())*/);
			registro = storedWinLog(saf, eventWin, record);
		}else{
			/**we want only some application event's**/
			if(incluyeRegistro(record, list)){
				log.info("Record number: " + record.getRecordNumber() + ": Event ID: " + record.getEventId() + ", Event Type: " + record.getType() + ", Event Source: " + record.getSource() /*+ ", Event log data: " + new String(record.getData())*/);
				registro = storedWinLog(saf, eventWin, record);	
			}
		}
		return registro;
	}
	
	/**
	 * indica si el registro de Windows incluye el Event Source indicado
	 * @param record	registro de windows
	 * @return
	 */
	private static boolean incluyeRegistro(EventLogRecord record, List<String> list){
		boolean b_exito = false;
		try {
			if(list.contains((String)record.getSource()))
				b_exito = true;
			
		} catch (Exception e) {
			log.error("incluyeRegistro: " + e);
		}
		return b_exito;
	}
	
	/**
	 * Obtain the dateTime event generation in format yyyyMMddHHmmss
	 * @param epoch		This value is time of seconds since 1970, in UTC. 
	 * @return			dateTime Human readable form.
	 */
	private String obtainEpochDate(long epoch){
		String date = "";
		try {
		    date = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date (epoch*1000));
		} catch (Exception e) {
			log.error("obtainEpochDate: " + e);
		}
		return date;
	}
	
	public static boolean loadFileIntoHashMap(String FileName, HashMap<String, String> hashmap){
		boolean exito = false;
		try {
			hashmap = PropertyFileLoader.getProperties(PropertiesWinEQ.ParamWinEQ.FILE_SEEK_WIN_LOG);
			exito = true;
		} catch (Exception e) {
			log.error("loadFileintoHashMap: " + e);
		}
		return exito;
	}
	
	public static Date getLastRecordDate(HashMap<String, String> hash){
		Date lastRecordDateStored = Util2.convertStringToDate(hash.get("lastRecordDateStored"),"yyyyMMddHHmmss");
		Date lastRecordDateChecked = Util2.convertStringToDate(hash.get("lastRecordDateChecked"),"yyyyMMddHHmmss");
		
		
		if(null == lastRecordDateChecked)
			return lastRecordDateStored;
		if(lastRecordDateChecked.getTime() > lastRecordDateStored.getTime())
			return lastRecordDateChecked;
		else
			return lastRecordDateStored;
	}
	
	public static long getLastRecordNumber(HashMap<String, String> hash){
		long lastRecordNumberChecked = Long.parseLong(hash.get("lastRecordNumberChecked")); 
		long lastRecordNumberStored = Long.parseLong(hash.get("lastRecordNumberStored"));	
		
		if(lastRecordNumberChecked > lastRecordNumberStored)
			return lastRecordNumberChecked;
		else
			return lastRecordNumberStored;
	}
	
	public static boolean saveHashMapIntoFile(HashMap<String, String> hashmap, String fileName){
		boolean exito = false;
		String data = "";
		try {
		 
		    String key = "lastRecordDateStored";
		    String value = hashmap.get(key);
		    data = data + "lastRecordDateStored=" + value + Constants.Comunicacion.CRLF;
		    
		    key = "lastRecordNumberStored";
		    value = hashmap.get(key);
			data = data + "lastRecordNumberStored=" + value + Constants.Comunicacion.CRLF;
			
			key = "lastRecordDateChecked";
		    value = hashmap.get(key);
		    data = data + "lastRecordDateChecked=" + value + Constants.Comunicacion.CRLF;
		    
		    key = "lastRecordNumberChecked";
		    value = hashmap.get(key);
			data = data + "lastRecordNumberChecked=" + value + Constants.Comunicacion.CRLF;
			
			Files.creaEscribeDataArchivo(fileName, data, false);
			exito = true;
		} catch (Exception e) {
			log.error("loadFileintoHashMap: " + e);
		}
		return exito;
	}


	
	
}
