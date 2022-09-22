package com.allc.main.properties;



public class PropertiesWinEQ {

	private static WinEQprop winEQprop = WinEQprop.getInstancia();
	
	/**Parameters to connect with the SEQ Central Service**/
	public static class ParamSEQ {
		/****/
		public static final String 	IP_SERVER			= winEQprop.getIPSERVER();
		public static final int 	PORT_SERVER			= winEQprop.getPORTSERVER();
		public static final int 	CANT_THREAD 	  	= winEQprop.getMAXPROC();
		public static final String 	THREAD_GROUP_NAME 	= "4690EQGroup";
		public static final int    	TIME_OUT_CONEXION	= winEQprop.getTIMEOUTCONEXION();
		public static final int 	REINTENTOS_CX		= winEQprop.getREINTENTOSCX();	
		public static final int 	TIME_OUT_SLEEP		= winEQprop.getTIMEOUTSLEEP();
		public static final int 	TIME_SLEEP_DEBUG	= winEQprop.getTIMESLEEPDEBUG();
		/****/
		
	}

	public static class ParamSAF{
		/**Path and file name where will be store the information**/
		public static final String FILE_STORE	 	  	= winEQprop.getFILESTORE();
		/**Path and file name  where will be registered the position about FILE_STORE to start sending the information**/
		public static final String FILE_SEEK	 		= winEQprop.getFILESEEK();
		/**time of every single execution of saf process**/
		public static final int    TIME_SAF			= winEQprop.getTIMESAF();
	}
	
	public static class ParamUPS{
		/**Path and file name where will be store the information**/
		public static final String FILE_NAME	 	  	= winEQprop.getUPSFileName();
		/**Path and file name  where will be registered the position about FILE_STORE to start sending the information**/
		public static final String FILE_SEEK	 		= winEQprop.getUPSFileSeek();
	}
	
	public static class ParamWinEQ{
		/**File who contains the last event stored in FILE_STORE**/
		public static final String FILE_SEEK_WIN_LOG	= winEQprop.getFILESEEKWINLOG();	
		/**Time of every single execution of WinEQ process**/
		public static final int TIME_EVENT_WIN_LOG = winEQprop.getTIMEEVENTWINLOG();
		
	}

	public static class Parameters {
		/**Computer's name that indicate **/
		public static final String COMPUTER_SOURCE		= winEQprop.getCOMPUTERSOURCE();
		/**Name of branch associated to stores**/
		public static final String DES_CADENA			= winEQprop.getCADENA();
		/**Store number**/
		public static final String STORE_NUMBER			= winEQprop.getSTORENUMBER();
		/**List of Name of events to be filtered**/
		public static final String EVENT_SOURCE			= winEQprop.getEVENTSOURCE();
		/**terminal number associated to point of sale **/
		public static final String TERMINAL_ID			= winEQprop.getTERMINALID();
		
		public static final String EVENT_GROUP			= winEQprop.getEVENTGROUP();
	}


	





	

	

}
