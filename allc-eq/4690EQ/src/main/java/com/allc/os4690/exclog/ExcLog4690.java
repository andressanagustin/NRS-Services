package com.allc.os4690.exclog;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.allc.conexion.Trama;
import com.allc.files.Files;
import com.allc.main.constants.Constants;
import com.allc.main.properties.Properties4690EQ;
import com.allc.os4690.BCD;
import com.allc.saf.SAF;
import com.allc.saf.SAFProcess;
import com.allc.util.Util2;
import com.ibm.OS4690.POSFile;
import com.ibm.OS4690.RandomAccessFile4690;

public class ExcLog4690 {

	static Logger log = Logger.getLogger(ExcLog4690.class);
	static RandomAccessFile4690 randSeekRead = null;
	static RandomAccessFile4690 randELDataRead = null;
	static POSFile posFileSeekWriter = null;
	static final String valorEnCero = Util2.rpad(String.valueOf(0), " ", 20);
	
	
	ExcLog excLog;
	Pattern R = Pattern.compile(Constants.Comunicacion.REGEX2);
	
	/**Constructor class**/
	public ExcLog4690(ExcLog excLog) {
		this.excLog = excLog;
		try {
			if(!Files.fileExists4690(excLog.getFileSeekExcLog())){
				Files.creaEscribeDataArchivo4690(excLog.getFileSeekExcLog(), valorEnCero + excLog.getCrlf(), false);
				/*POSFile posFileSeekWriter = new POSFile(excLog.getFileSeekExcLog(), "rw", POSFile.SHARED_READ_WRITE_ACCESS, 50);
				posFileSeekWriter.write((valorEnCero + excLog.getCrlf()).getBytes(), 0, POSFile.FROM_START_OF_FILE, POSFile.FLUSH, (valorEnCero + excLog.getCrlf()).length());
				posFileSeekWriter.closeFull();*/

			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public boolean init(){
		boolean result = false;
		try {
			randSeekRead = new RandomAccessFile4690(excLog.getFileSeekExcLog(),"r");
			//randELDataRead = new RandomAccessFile(excLog.getFileNameExcLog(),"r");
			posFileSeekWriter = new POSFile(excLog.getFileSeekExcLog(), "rw", POSFile.SHARED_READ_WRITE_ACCESS);
			
			result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;

	}
	
	public boolean closure(){
		boolean result = false;
		try {
			/**close the files**/
			randSeekRead.close();
			//randELDataRead.close();
			posFileSeekWriter.closeFull();
			
			result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}	
	
	
	
	
	/**
	 * Method to save Exception log Event
	 * @param trama
	 * @param saf
	 */
	public void storeExceptionLogEvent(Trama trama, SAF saf){
		try {
			 if(log.isDebugEnabled())
				 log.debug(trama.toString());
			 String msgToSend = (trama.getHeaderStr() + saf.getCar() + trama.getBodyStr()).trim();
			 
			 SAFProcess.stored(msgToSend, Constants.Comunicacion.CRLF);

		}catch (Exception e) {
			log.error("storeExceptionLogEvent: " + e);
		}
	}

	/**
	 * verifies if exists data to send
	 * @return
	 */
	public boolean permiteRegistroLog(){
		boolean result = false;
		long filePointer;
		String linea;
		try {
			/**Verifies if exists ExceptionLog File, otherwise there�s nothing to send**/
    		if(!Files.fileExists4690(excLog.getFileNameExcLog())) return result;
    		/**the offset of the Exception log**/
    		if(!Files.fileExists4690(excLog.getFileSeekExcLog())){
    			filePointer = 0;
    		}else{
    			filePointer = getOffset(excLog.getFileSeekExcLog(), excLog.getFileNameExcLog());
    		}

    		if(filePointer >= 0){
    			try{
    				randELDataRead = new RandomAccessFile4690(excLog.getFileNameExcLog(),"r");
    				randELDataRead.seek(filePointer);//Seek to end of file
					linea=randELDataRead.readLine();
					randELDataRead.close();
					if(linea!=null){
						result = true;
					}
    			}catch ( Exception e ){
    				log.error(e.getMessage(), e);
    				randELDataRead.close();
    			}
    		}
    	}catch ( Exception e ){
    		log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Obtain the file offset
	 * @param fileName File that contains the offset 
	 * @return	Position
	 */
	long getOffsetFile(String fileName) {
		long filePointer;
		String data;
		try {
			data = randSeekRead.readLine();
			randSeekRead.seek(0);

			if(null == data)
				filePointer = 0;
			else
				try {
					filePointer = Long.parseLong(data.replaceAll(" ", ""));
				} catch (Exception e) {
					log.error("getOffsetFile: the file " + fileName + " not contain a number as a pointer. " ,  e);
					filePointer = -1;
				}		
		}catch ( Exception e ){
			log.error(e.getMessage(), e);
			filePointer = -1;
		}
		return filePointer;
	}

	/**
	 * Obtiene la posicion del archivo dataFileName desde la que se debe obtener la informacion para registrarla como ExceptionLog
	 * @param dataFileSeekName	Nombre del archivo que indica la ultima posicion en el archivo dataFileName hasta donde se obtuvo la informacion para registrarla como ExceptionLog
	 * @param dataFileName		Nombre del archivo que contiene la data que se registra como Exceptionlog
	 * @return					La posicion en el archivo dataFileName desde la que se debe de tomar la informacion para registrarla como ExceptionLog
	 */
	long getOffset(String dataFileSeekName, String dataFileName){
		long filePointer = 0, lengthDataFile = 0;
		try {
			filePointer = getOffsetFile(dataFileSeekName);
			if(filePointer > 0){
				randELDataRead = new RandomAccessFile4690(excLog.getFileNameExcLog(),"r");
				lengthDataFile = randELDataRead.length(); 
				randELDataRead.close();
				/**si la posicion del archivo es mayor al tama�o del archivo que contiene la data, entonces hubo un cierre y se trata de un archivo nuevo. Se registrara la info desde el inicio**/
				if(filePointer > lengthDataFile){
					filePointer = 0;
					log.info("getOffset: Se detecto cierre. se enviara la informacion desde el inicio. " + dataFileSeekName + ": " +filePointer + " vs " + dataFileName + ": " +lengthDataFile);
				}
			}
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return filePointer;
	}

	/**
	 * Obtain the Exception Log and save into the file
	 * @param saf	SAF class
	 */
	public void storeExceptionLog(SAF saf){
		long filePointer;
		String line;
		String data;
		StringBuffer mess = new StringBuffer("");
		Trama trama;
		List list;
		List lista;
		Trama tramaTmp;
		long tmp = 0;
		try {
			/**Obtain the last position since where must send the information**/
			filePointer = getOffset(excLog.getFileSeekExcLog(), excLog.getFileNameExcLog());
			if(filePointer >= 0){
		    	try{
		    		randELDataRead = new RandomAccessFile4690(excLog.getFileNameExcLog(),"r");
					/**goto position randStore in the Exception Log File**/
		    		randELDataRead.seek(filePointer);
					/**Read file line by line**/
					while((line=randELDataRead.readLine())!=null && !ExcLog4690Process.isFinExLog){
						line = line.substring(1, line.length()-1);
						/**Obtain the data to manipulate**/
						data = ident(line);
						/**load the data in Trama class using contructor**/
						list = Arrays.asList(excLog.getP().split(data));
						mess.setLength(0);
						mess.append(list.get(11)).append(Constants.Comunicacion.CAR).append(list.get(12)).append(Constants.Comunicacion.CAR).append(list.get(13)).append(Constants.Comunicacion.CAR).append(list.get(14)).append(Constants.Comunicacion.CAR).append(list.get(15)).append(Constants.Comunicacion.CAR).append(list.get(16));
						/**si no se filtra el mensaje**/
						if(isMessageAllowed(excLog.getHash(), mess.toString())){
							/**Instantiate from Trama Class**/
							trama = new Trama(list, saf.getCantDatosHeader(), saf.getCar());
	
							/**get the list**/
							lista = trama.getList();
							/**set the additional attributes**/
							lista.set(2, excLog.getCtrlNode());
							lista.set(3, Properties4690EQ.Param4690.DES_CADENA);
							lista.set(4, excLog.getStoreNumber());
							lista.set(8, excLog.getIp());
							
							/**se modificara el elemento del body que esta en la posicion 3, el header tiene CANTIDADDATOSHEADER elementos, restandole 1 me da la posicion final del header
							 * 4 posiciones despues viene el nodo del controlador ( posicion 2 del body )**/
							lista.set(saf.getCantDatosHeader() + 3, excLog.getCtrlNode());
							/**Instantiate from Trama Class**/
							tramaTmp = new Trama(lista, saf.getCantDatosHeader(), saf.getCar());
							/**load the without errors**/
							if(tramaTmp.loadData()){
								storeExceptionLogEvent(tramaTmp, saf);
								tmp = randELDataRead.getFilePointer();
								posFileSeekWriter.write((Util2.rpad(String.valueOf(tmp), " ", 20) + saf.getCrlf()).getBytes(), 0, POSFile.FROM_START_OF_FILE, POSFile.FLUSH, 20);
								if(log.isDebugEnabled())
									log.debug("file pointer moved to: " + tmp);
								continue;
							}else{
								log.error("Wrong Trama: " + tramaTmp.toString() + " - " + tramaTmp.getError());
								break;
							}
						}
					}
					
		    	}catch(Exception e){
		    		log.error(e.getMessage(), e);
		    		
		    	}finally{
		    		randELDataRead.close();
		    	}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		ExcLog4690Process.isFinStoreExcLog = true;
	}
	
	 
	private String campos(List entrada, String accion ){
		 String campossal="";
		 int cant=0;
		 int acc;
		 
		 try{
			 cant = entrada.size()-1;
			 int max = cant <= accion.length()-1 ? cant : accion.length()-1;
			 for (int ind = 0; ind <= max; ind++){
				 if(ind != 2 && ind != 3){
					acc = (new Integer(accion.substring(ind,ind+1))).intValue();
					campossal=campossal+obtiene(entrada,ind,acc)+ Constants.Comunicacion.CAR2;
				 }
			 }
		 } catch (Exception e) {
			 log.error(e.getMessage(), e);
		 }			 
		 return campossal;
	 }
	
	private String obtiene(List entrada,int pos,int opc ){
		String salida="";
		byte[] arregloB;
		try{
			 if (opc == 1){
				 salida=entrada.get(pos).toString();
				 if ( salida.length() > 0 ) {
				     arregloB =  salida.getBytes();
				     salida=BCD.unpack(arregloB,false);
				 }
				 else salida = "";
			 }
			 else {
				 salida=entrada.get(pos).toString();	 			 
			 }
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}			 
		 return salida;
	 }	

    //private String parser(List reg, String ident){
    private String parser(List reg, int pp){

		 //int pp = (new Integer(ident)).intValue();
		 String paso="";
		 String salida="";

		 try {
			 paso = obtiene(reg,2,1);
			 String tmpMes = paso.substring(2, 4);
			 String tmpDia = paso.substring(4, 6);
			 /**The String system datetime**/
			 String sysDate = Util2.fechaFormato(Calendar.getInstance().getTime());
			 
			 /**If exception log date from exception log comes wrong(check only month and day) then set the exception log date with the system date**/
			 if( Integer.valueOf(tmpMes).intValue() < 0  || Integer.valueOf(tmpMes).intValue() > 12 || Integer.valueOf(tmpDia).intValue() < 0 || Integer.valueOf(tmpDia).intValue() > 31)
				 paso = sysDate.substring(2, sysDate.length() - 2);
			 
			 salida = "S|2||||"+ sysDate +"|20"+paso+"00|ExcLog.jar|||";
			 salida= salida + String.valueOf(pp);
			 switch(pp) {
			 
			 case 1: 
				 salida = salida +"|V|001|||5|"+
				          campos(reg,"111111100000111110");
			     break;
			 case 2: 
				 salida = salida +"|V|002|||5|"+ 
				          campos(reg,"111110011110");
				 break;
			 case 3: 
				 salida = salida +"|V|003|||5|"+ 
				          campos(reg,"1111100100110");
				 break;
			 case 4: 
				 salida = salida +"|V|004|||5|"+ 
				          campos(reg,"111110110");
				 break;
			 case 5: 
				 salida = salida +"|V|005|||5|"+ 
				          campos(reg,"11111000110");
		         break;
			 case 6: 
				 salida = salida +"|V|006|||5|"+ 
				          campos(reg,"1111100110");
				 break;
			 case 7: 
				 salida = salida +"|V|007|||5|"+ 
				          campos(reg,"111110000");
				 break;
			 case 8: 
				 salida = salida +"|V|008|||5|"+ 
				          campos(reg,"11111100");
				 break;
			 case 9: 
				 salida = salida +"|V|009|||5|"+ 
				          campos(reg,"111111111110");
				 break;
			 case 10: 
				 salida = salida +"|V|010|||5|"+ 
				          campos(reg,"111110010");
				 break;
			 case 11: 
				 salida = salida +"|V|011|||5|"+ 
				          campos(reg,"11111000110");
				 break;
			 case 12: 
				 salida = salida +"|V|012|||5|"+ 
				          campos(reg,"11111000");
				 break;
			 case 13: 
				 salida = salida +paso+"|V|013|||5|"+ 
				          campos(reg,"11111000000000110");
				 break;
			 case 14: 
				 salida = salida +"|V|014|||5|"+ 
	                     campos(reg,"11111000000000110");
			     break;
			 case 15: 
				 salida = salida +"|V|015|||5|"+ 
	                     campos(reg,"11111010");
			     break;
			 case 16: 
				 salida = salida +"|V|016|||5|"+ 
	                     campos(reg,"1111100");
			     break;
			 case 17: 
				 salida = salida +"|V|017|||5|"+ 
	                     campos(reg,"1111100");
			     break;
			 case 18: 
				 salida = salida +"|V|018|||5|"+ 
	                     campos(reg,"111110000000000");
				 break;
			 case 19: 
				 salida = salida +"|V|019|||5|"+ 
	                     campos(reg,"111110");
				 break;
			 case 20: 
				 salida = salida +"|V|020|||5|"+ 
	                     campos(reg,"011111000000000");
				 break;
			 case 21: 
				 salida = salida +"|V|021|||5|"+ 
	                     campos(reg,"011111000000000");
			     break;
			 case 22: 
				 salida = salida +"|V|022|||5|"+ 
	                     campos(reg,"011111000000000");
				 break;
			 case 23: 
				 salida = salida +"|V|023|||5|"+ 
	                     campos(reg,"011111000000000");
			     break;
			 case 24: 
				 salida = salida +"|V|024|||5|"+ 
	                     campos(reg,"0111100000000");
			     break;
			 case 25: 
				 salida = salida +"|V|025|||5|"+ 
	                     campos(reg,"011111000011");
			     break;
			 case 26: 
				 salida = salida +"|V|026|||5|"+ 
	                     campos(reg,"01111010");
			     break;
			 case 30: 
				 salida = salida +"|V|030|||5|"+ 
	                     campos(reg,"011110100");
			     break;
			 case 31: 
				 salida = salida +"|V|031|||5|"+ 
	                     campos(reg,"011110100");
			     break;
			 case 32: 
				 salida = salida +"|V|032|||5|"+ 
	                     campos(reg,"011110100");
			     break;
			 case 33: 
				 salida = salida +"|V|033|||5|"+ 
	                     campos(reg,"0111101");
			     break;
			 case 34:
				 salida = salida +"|V|034|||5|"+ 
	                     campos(reg,"0111101");
				 break;
			 case 35: 
				 salida = salida +"|V|035|||5|"+ 
	                     campos(reg,"011110");
			     break;
			 case 40: 
				 salida = salida +"|V|040|||5|"+ 
	                     campos(reg,"011110100");
			     break;
			 case 41: 
				 salida = salida +"|V|041|||5|"+ 
	                     campos(reg,"0111110000");
			     break;
			 case 42: 
				 salida = salida +"|V|042|||5|"+ 
	                     campos(reg,"0111110");
			     break;
			 case 43: 
				 salida = salida +"|V|043|||5|"+ 
	                     campos(reg,"0111110");
			     break;
			 case 44: 
				 salida = salida +"|V|044|||5|"+ 
	                     campos(reg,"0111110100");
			     break;
			 case 45: 
				 salida = salida +"|V|045|||5|"+ 
	                     campos(reg,"0111110100");
			     break;
			 case 46: 
				 salida = salida +"|V|046|||5|"+ 
	                     campos(reg,"0111110100");
			     break;
			 case 47: 
				 salida = salida +"|V|047|||5|"+ 
	                     campos(reg,"011110100");
			     break;
			 case 50: 
				 salida = salida +"|V|050|||5|"+ 
	                     campos(reg,"011111");
			     break;
			 case 51: 
				 salida = salida +"|V|051|||5|"+ 
	                     campos(reg,"0111100");
			     break;
			 case 52: 
				 salida = salida +"|V|052|||5|"+ 
	                     campos(reg,"01111011");
			     break;
			 case 89: 
				 salida = salida +"|V|089|||5|"+ 
	                     campos(reg,"111110");
			     break;
			 case 99: 
				 salida = salida +"|V|099|||5|"+ 
	                     campos(reg,"111110");
			     break;

			 default:
				 if(log.isDebugEnabled())
				    log.debug("parser: " + pp + " not found");
			     break;		 
			 }			
		} catch (Exception e) {
			log.error("parser: " + e);
		}

		return salida;
    }   
	
	/**
	 * 
	 * @param dato
	 * @return
	 */
	private String ident(String dato){
		 String sal1="";
		 String salida = "";
		 try{
			 /**Se carga la data en una lista**/
			 List list = Arrays.asList(R.split(dato));
			 sal1 = obtiene(list,3,1);
			 try{
				 sal1 = sal1.replace('F', '0');
				 int pp = (new Integer(sal1)).intValue();
				 salida =parser(list, pp);
			 }catch(Exception e){
				 log.error(e.getMessage() + " dato: " + dato,  e);
			 }

		 }catch (Exception e) {
			 log.error("ident: " + e);
		 }		 
		 return salida;
	}
	
	private boolean isMessageAllowed(Hashtable hash, String key){
		boolean result = true;
		try {
			if(hash.containsKey(key))
				result = false;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}
}
