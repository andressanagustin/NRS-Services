package com.allc.main.properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class WinEQprop {
	static Logger log = Logger.getLogger(WinEQprop.class);
	/** Atributo para singleton.*/
	private static WinEQprop instancia;
	/** Atributo para hacer la conexion con el archivo de propiedades **/
	private static Properties props;
	
	private String ruta = "";
	private String PropFileName = "WinEQ.properties";
	
	private WinEQprop()
	{
		props = new Properties();
		try{
			props.load(new FileInputStream(ruta + PropFileName));
        }catch(IOException e){
			log.fatal("No se encontr� el archivo " + ruta + PropFileName +" que contiene los valores para la configuraci�n de la aplicaci�n.");
		}
	}
	
	/**
	 * Obtiene instancia para singleton.
	 * @return
	 */
	public static WinEQprop getInstancia(){
		if(instancia == null){
			instancia = new WinEQprop();
		}
		return instancia;
	} 
	
	
	/**
	 * @return the IPSERVER
	 */
	public String getIPSERVER() {
		return props.getProperty("IPSERVER","127.0.0.1").trim();
	}
	
	/**
	 * @return the PORTSERVER
	 */
	
	public int getPORTSERVER() {
		return Integer.parseInt(props.getProperty("PORTSERVER","10000").trim());
	}
	
	/**
	 * @return the REINTENTOSCX
	 */
	
	public int getREINTENTOSCX() {
		return Integer.parseInt(props.getProperty("REINTENTOSCX","10000").trim());
	}
	
	/**
	 * @return the CADENA
	 */
	public String getCADENA() {
		return props.getProperty("CADENA","NO ASIGNADA").trim();
	}
		
	/**
	 * @return the STORENUMBER
	 */
	public String getSTORENUMBER() {
		return props.getProperty("STORENUMBER","NO ASIGNADO").trim();
	}

	/**
	 * @return the TERMINALID
	 */
	public String getTERMINALID() {
		return props.getProperty("TERMINALID","").trim();
	}	
	
	/**
	 * @return the EVENTSOURCE
	 */
	public String getEVENTSOURCE() {
		return props.getProperty("EVENTSOURCE","").trim();
	}	
	
	/**
	 * @return the EVENTGROUP
	 */
	public String getEVENTGROUP() {
		return props.getProperty("EVENTGROUP","").trim();
	}	
	
	/**
	 * @return the TIMEOUTSLEEP
	 */
	
	public int getTIMEOUTSLEEP() {
		return Integer.parseInt(props.getProperty("TIMEOUTSLEEP","400").trim());
	}
	
	/**
	 * @return the TIMEOUTCONEXION
	 */
	
	public int getTIMEOUTCONEXION() {
		return Integer.parseInt(props.getProperty("TIMEOUTCONEXION","10000").trim());
	}
	
	/**
	 * @return the TIMESLEEPDEBUG
	 */
	
	public int getTIMESLEEPDEBUG() {
		return Integer.parseInt(props.getProperty("TIMESLEEPDEBUG","10000").trim());
	}
	
	/**
	 * @return the FILESTORE
	 */
	public String getFILESTORE() {
		return props.getProperty("FILESTORE","AMBOS").trim();
	}
	
	/**
	 * @return the FILESEEK
	 */
	public String getFILESEEK() {
		return props.getProperty("FILESEEK","AMBOS").trim();
	}
	
	/**
	 * @return the FILESTORE
	 */
	public String getUPSFileName() {
		return props.getProperty("UPS.FILENAME","AMBOS").trim();
	}
	
	/**
	 * @return the FILESEEK
	 */
	public String getUPSFileSeek() {
		return props.getProperty("UPS.FILESEEK","AMBOS").trim();
	}
	
	/**
	 * @return the TIMESAF
	 */
	public int getTIMESAF() {
		return Integer.parseInt(props.getProperty("TIMESAF","AMBOS").trim());
	}	

	/**
	 * @return the FILESTORE
	 */
	public String getFILESEEKWINLOG() {
		return props.getProperty("FILESEEKWINLOG","AMBOS").trim();
	}	
	
	
	/**
	 * @return the TIMEEVENTWINLOG
	 */
	public int getTIMEEVENTWINLOG() {
		return Integer.parseInt(props.getProperty("TIMEEVENTWINLOG","40000").trim());
	}	
	
	/**
	 * @return the MAXPROC
	 */
	public int getMAXPROC() {
		return Integer.parseInt(props.getProperty("MAXPROC","4").trim());
	}	

	
	/**
	 * @return the COMPUTERSOURCE
	 */
	public String getCOMPUTERSOURCE() {
		return props.getProperty("COMPUTERSOURCE",null).trim();
	}
	
}
