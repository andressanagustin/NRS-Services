package com.allc.main.properties;



public class Properties4690EQ {

	private static E4690Qprop e4690Qprop = E4690Qprop.getInstancia();
	
	/**Parametros para conexion con el Servidor SEQ **/
	public static class ParamSEQ {
		/****/
		public static String IP_SERVER			= e4690Qprop.getIPSERVER();
		public static int 	 PORT_SERVER		= e4690Qprop.getPORTSERVER();
		public static int 	 CANT_THREAD 	  	= e4690Qprop.getMAXPROC();
		public static String THREAD_GROUP_NAME 	= "4690EQGroup";
		public static int    TIME_OUT_CONEXION	= e4690Qprop.getTIMEOUTCONEXION();
		public static int 	 REINTENTOS_CX		= e4690Qprop.getREINTENTOSCX();	
		public static int 	 TIME_OUT_SLEEP		= e4690Qprop.getTIMEOUTSLEEP();
		public static int 	 TIME_SLEEP_DEBUG	= e4690Qprop.getTIMESLEEPDEBUG();
		/****/
		
	}

	public static class Param4690{
		
		public static final String PI2_EVENT 		    = e4690Qprop.getPI2EVENT();
		public static final String PI2_FALCON			= e4690Qprop.getPI2FALCON();
		public static final int    FILE_SIZE_PI2 		= e4690Qprop.getFILESIZEPI2();
		public static final String CNTR_TXT_FILE_NAME = e4690Qprop.getCNTRTXTFILENAME();
		public static final String TERM_TXT_FILE_NAME = e4690Qprop.getTERMTXTFILENAME();
		public static final String APPL_TXT_FILE_NAME	= e4690Qprop.getAPPLTXTFILENAME();
		public static final String DES_CADENA			= e4690Qprop.getCADENA();
		public static final String FILTER_BY_CONTROLLER = e4690Qprop.getFILTERBYCONTROLLER();
		public static final int    REDIRECTED_EVENTS	= e4690Qprop.getREDIRECTEDEVENTS();
		public static final String PI2_REDIRECTED		= e4690Qprop.getPI2REDIRECTED();
		public static final String FILE_MESSAGE_TO_AVOID = e4690Qprop.getFILEMESSAGETOAVOID();
	}

	public static class ParamSAF{
		
		public static String FILE_STORE	 	  	= e4690Qprop.getFILESTORE();
		public static String FILE_SEEK	 		= e4690Qprop.getFILESEEK();
		public static int    TIME_SAF			= e4690Qprop.getTIMESAF();
	}
	
	public static class ParamUPS{
		public static String FILE_NAME	 	  	= e4690Qprop.getUPSFileName();
		public static String FILE_SEEK	 		= e4690Qprop.getUPSSeekFile();
		public static String PATH	 	  		= e4690Qprop.getUPSPath();
	}

	public static class Parametros {
		//ruta donde se registrara la informacion 
		//public static final String	RUTA_REGISTRO_INFO = e4690Qprop.getRUTAREGISTROINFO();
	}

	public static class ExceptionLog{
		public static String FILE_NAME_EXC_LOG 	= e4690Qprop.getFILENAMEEXCEPTIONLOG();
		public static String FILE_SEEK_EXC_LOG	= e4690Qprop.getFILESEEKEXCEPTIONLOG();
		public static int 	 TIME_EXC_LOG		= e4690Qprop.getTIMEEXCLOG();
		
	}

	

    



	

	

}
