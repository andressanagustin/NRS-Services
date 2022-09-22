package com.allc.main.properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.allc.main.constants.Constants;

public class E4690Qprop {
	static Logger log = Logger.getLogger(E4690Qprop.class);
	/** Atributo para singleton.*/
	private static E4690Qprop instancia;
	/** Atributo para hacer la conexion con el archivo de propiedades **/
	private static Properties props;
	
	//private String ruta = "M://sts//4690EQ//";
	private String ruta = Constants.Componente.RUTA_APP;
	private String PropFileName = "4690EQ.properties";
	
	private E4690Qprop()
	{
		props = new Properties();
		try{
			props.load(new FileInputStream(PropFileName));
        }catch(IOException e){
			log.fatal("No se encontr� el archivo " + ruta + PropFileName +" que contiene los valores para la configuraci�n de la aplicaci�n.");
		}
	}
	
	/**
	 * Obtiene instancia para singleton.
	 * @return
	 */
	public static E4690Qprop getInstancia(){
		if(instancia == null){
			instancia = new E4690Qprop();
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
		return Integer.parseInt(props.getProperty("REINTENTOSCX","5").trim());
	}
	
	/**
	 * @return the CNTRTXTFILENAME
	 */
	public String getCNTRTXTFILENAME() {
		return props.getProperty("CNTRTXTFILENAME","ADX_SPGM:ADXCSOMF.DAT").trim();
	}
	
	/**
	 * @return the TERMTXTFILENAME
	 */
	public String getTERMTXTFILENAME() {
		return props.getProperty("TERMTXTFILENAME","ADX_SPGM:ADXTSTWF.DAT").trim();
	}
	
	/**
	 * @return the APPLTXTFILENAME
	 */
	public String getAPPLTXTFILENAME() {
		return props.getProperty("APPLTXTFILENAME","ADX_IPGM:ADXCSOZF.DAT").trim();
	}
	
	/**
	 * @return the PI2EVENT
	 */
	public String getPI2EVENT() {
		return props.getProperty("PI2EVENT","pi:ADXCSOUP").trim();
	}
	
	/**
	 * @return the PI2FALCON
	 */
	public String getPI2FALCON() {
		return props.getProperty("PI2FALCON","pi:FALCON").trim();
	}
	
	/**
	 * @return the FILESIZEPI2
	 */
	
	public int getFILESIZEPI2() {
		return Integer.parseInt(props.getProperty("FILESIZEPI2","10000").trim());
	}
	
	/**
	 * @return the CADENA
	 */
	public String getCADENA() {
		return props.getProperty("CADENA","NO ASIGNADA").trim();
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
		return Integer.parseInt(props.getProperty("TIMESLEEPDEBUG","100000").trim());
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
	 * @return the UPS.SEEKFILE
	 */
	public String getUPSSeekFile() {
		return props.getProperty("UPS.SEEKFILE","AMBOS").trim();
	}
	
	/**
	 * @return the UPS.FILENAME
	 */
	public String getUPSFileName() {
		return props.getProperty("UPS.FILENAME","AMBOS").trim();
	}
	
	/**
	 * @return the UPS.PATH
	 */
	public String getUPSPath() {
		return props.getProperty("UPS.PATH","AMBOS").trim();
	}
	
	/**
	 * @return the TIMESAF
	 */
	public int getTIMESAF() {
		return Integer.parseInt(props.getProperty("TIMESAF","20000").trim());
	}	

	/**
	 * @return the MAXPROC
	 */
	public int getMAXPROC() {
		return Integer.parseInt(props.getProperty("MAXPROC","4").trim());
	}	

	/**
	 * @return the FILENAMEEXCEPTIONLOG
	 */
	public String getFILENAMEEXCEPTIONLOG() {
		return props.getProperty("FILENAMEEXCEPTIONLOG","eamexcpt").trim();
	}

	/**
	 * @return the FILESEEKEXCEPTIONLOG
	 */
	public String getFILESEEKEXCEPTIONLOG() {
		return props.getProperty("FILESEEKEXCEPTIONLOG","EQSeekEL.dat").trim();
	}
	
	/**
	 * @return the TIMEEXCLOG
	 */
	public int getTIMEEXCLOG() {
		return Integer.parseInt(props.getProperty("TIMEEXCLOG","1000000").trim());
	}

	/**
	 * @return the FILTERBYCONTROLLER
	 */
	public String getFILTERBYCONTROLLER() {
		return props.getProperty("FILTERBYCONTROLLER","S").trim();
	}

	/**
	 * @return the REDIRECTEDEVENTS
	 */
	public int getREDIRECTEDEVENTS(){
		return Integer.parseInt(props.getProperty("REDIRECTEDEVENTS","0").trim());
	}

	/**
	 * @return the PI2REDIRECTED
	 */
	public String getPI2REDIRECTED(){
		return props.getProperty("PI2REDIRECTED","pi:ADXCSOZP").trim();
	}

	
	public String getFILEMESSAGETOAVOID(){
		return props.getProperty("FILEMESSAGETOAVOID", "").trim();
	}
	
	
}
