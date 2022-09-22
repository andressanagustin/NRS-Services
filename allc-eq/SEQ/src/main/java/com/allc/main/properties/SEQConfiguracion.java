package com.allc.main.properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;



public class SEQConfiguracion {
	static Logger log = Logger.getLogger(SEQConfiguracion.class);
	Properties props;
	String PropFileName = "SEQ.properties";
	//public EQSConfiguracion(String ruta)
	public SEQConfiguracion()
	{

		props = new Properties();
		try 
		{
			props.load(new FileInputStream(/*ruta +*/ PropFileName));
        }
		catch(IOException e)
		{
			log.fatal("No se encontr� el archivo " /*+ ruta*/ + PropFileName +" que contiene los valores para la configuraci�n de la aplicaci�n.");
		}
	}
	
	
	/**
	 * @return the TIMEOUT1
	 */
	public int getTIMEOUTSERVER() {
		return Integer.parseInt(props.getProperty("TIMEOUTSERVER","10000").trim());
	}
	/**
	 * @return the TIMEOUT2
	 */
	public int getTIMEOUTSLEEP() {
		return Integer.parseInt(props.getProperty("TIMEOUTSLEEP","250").trim());
	}

	/**
	 * @return the LOCALPORT
	 */

	public int getLOCALPORT() {
		return Integer.parseInt(props.getProperty("LOCALPORT","7453").trim());
	}

	
	
	/**
	 * @return the MAXPROC
	 */
	public int getMAXPROC() {
		
		return Integer.parseInt(props.getProperty("MAXPROC","8").trim());
	}	
	
	/**
	 * @return the PORTSERVERDB
	 */
	public String getIPSERVERDB() {
		
		return props.getProperty("IPSERVERDB","128.2.175.209").trim();
	}	
	
	/**
	 * @return the PORTSERVERDB
	 */
	public String getPORTSERVERDB() {
		
		return props.getProperty("PORTSERVERDB","1521").trim();
	}	
	
	/**
	 * @return the 
	 */
	public String getSID() {
		return props.getProperty("SID","RHRDB").trim();
	}

	/**
	 * @return the USER
	 */
	public String getUSER() {
		return props.getProperty("USER","SYSTEM").trim();
	}
	/**
	 * @return the PASSWORD
	 */
	public String getPASSWORD() {
		return props.getProperty("PASSWORD","SYSTEM").trim();
	}
		
	
	public String getRUTAREGISTROINFO(){
		return props.getProperty("RUTAREGISTROINFO","").trim();
	}
	
	
	public int getREGISTERINBD(){
		return Integer.parseInt(props.getProperty("REGISTERINBD","0").trim());
	}
	

	/**
	 * @return the IPSERVER
	 */
	public String getIPARMSSERVER() {
		return props.getProperty("IPARMSSERVER","127.0.0.1").trim();
	}
	
	/**
	 * @return the PORTSERVER
	 */
	
	public int getPORTARMSSERVER() {
		return Integer.parseInt(props.getProperty("PORTARMSSERVER","8000").trim());
	}
	
	/**
	 * @return the REINTENTOSCX
	 */
	
	public int getRETRIESARMSSERVER() {
		return Integer.parseInt(props.getProperty("RETRIESARMSSERVER","2").trim());
	}

	/**
	 * @return the TIMEOUTSLEEP
	 */
	
	public int getTIMESLEEPARMSSERVER() {
		return Integer.parseInt(props.getProperty("TIMESLEEPARMSSERVER","300").trim());
	}
	
	/**
	 * @return the TIMEOUTCONEXION
	 */
	
	public int getTIMEOUTARMSSERVER() {
		return Integer.parseInt(props.getProperty("TIMEOUTARMSSERVER","5000").trim());
	}
	

}
