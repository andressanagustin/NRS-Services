package com.allc.main.properties;



public class PropertiesSEQ {
	private static SEQConfiguracion SEQConf = new SEQConfiguracion();
	

	
	/**Parametros para conexion a BD **/
	public static class ConnectBD {
		/****/
		public static final String IP_SERVER_DB	= SEQConf.getIPSERVERDB();
		/****/
		public static final String	PORT_SERVER_DB	= SEQConf.getPORTSERVERDB();
		/****/
		public static final String SID				= SEQConf.getSID();
		/****/
		public static final String USER			= SEQConf.getUSER();
		/****/
		public static final String PASSWORD		= SEQConf.getPASSWORD();
		/****/
		
	}
	
	/**Parametros de comunicacion**/
	public static class Comunication {
		/****/
		public static final int   	CANT_THREAD	 		= SEQConf.getMAXPROC();
		/****/
		public static final int   	TIME_OUT_SERVER  	= SEQConf.getTIMEOUTSERVER();
		/****/
		//public static final int    TIME_OUT_1 	 	= SEQConf.getTIMEOUT1();
		/****/
		public static final int    TIME_OUT_SLEEP 		= SEQConf.getTIMEOUTSLEEP();
		/****/
		public static final int 	LOCAL_SERVER_PORT 	= SEQConf.getLOCALPORT();
		/****/
		
		
	}
	
	public static class ComunicationArmsServer {
		/****/
		public static final String IP = SEQConf.getIPARMSSERVER();
		/****/
		public static final int PORT = SEQConf.getPORTARMSSERVER();
		/****/
		public static final int RETRIES = SEQConf.getRETRIESARMSSERVER();
		/****/
		public static final int TIMESLEEP = SEQConf.getTIMESLEEPARMSSERVER();
		/****/
		public static final int TIMEOUT = SEQConf.getTIMEOUTARMSSERVER();
		/****/
		
	}
	
	public static class Parametros {
		//ruta donde se registrara la informacion 
		public static final String	RUTA_REGISTRO_INFO = SEQConf.getRUTAREGISTROINFO();
		//flag que indica si se debe registrar en BD
		public static final int	REGISTER_IN_BD = SEQConf.getREGISTERINBD();

	}
	
	
}
