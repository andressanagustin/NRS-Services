package com.allc.main.properties;



public class PropertiesLinuxEQ {

	private static LinuxEQprop linuxEQprop = LinuxEQprop.getInstancia();
	
	/**Parameters to connect with the SEQ Central Service**/
	public static class ParamSEQ {
		/****/
		public static final String 	IP_SERVER			= linuxEQprop.getIPSERVER();
		public static final int 	PORT_SERVER			= linuxEQprop.getPORTSERVER();
		public static final int 	CANT_THREAD 	  	= linuxEQprop.getMAXPROC();
		public static final String 	THREAD_GROUP_NAME 	= "4690EQGroup";
		public static final int    	TIME_OUT_CONEXION	= linuxEQprop.getTIMEOUTCONEXION();
		public static final int 	REINTENTOS_CX		= linuxEQprop.getREINTENTOSCX();	
		public static final int 	TIME_OUT_SLEEP		= linuxEQprop.getTIMEOUTSLEEP();
		public static final int 	TIME_SLEEP_DEBUG	= linuxEQprop.getTIMESLEEPDEBUG();
		/****/
		
	}

	public static class ParamSAF{
		/**Path and file name where will be store the information**/
		public static final String FILE_STORE	 	  	= linuxEQprop.getFILESTORE();
		/**Path and file name  where will be registered the position about FILE_STORE to start sending the information**/
		public static final String FILE_SEEK	 		= linuxEQprop.getFILESEEK();
		/**time of every single execution of saf process**/
		public static final int    TIME_SAF			= linuxEQprop.getTIMESAF();
	}
	
	public static class ParamUPS{
		/**Path and file name where will be store the information**/
		public static final String FILE_NAME	 	  	= linuxEQprop.getUPSFileName();
		/**Path and file name  where will be registered the position about FILE_STORE to start sending the information**/
		public static final String FILE_SEEK	 		= linuxEQprop.getUPSFileSeek();
		/**Path where will be store the information**/
		public static final String PATH	 	  	= linuxEQprop.getUPSPath();
	}
	
	public static class ParamLinuxEQ{
		/**File who contains the last event stored in FILE_STORE**/
		public static final String FILE_SEEK_LINUX_LOG	= linuxEQprop.getFILESEEKWINLOG();	
		/**Time of every single execution of LinuxEQ process**/
		public static final int TIME_EVENT_LINUX_LOG = linuxEQprop.getTIMEEVENTWINLOG();
		
	}

	public static class Parameters {
		/**Computer's name that indicate **/
		public static final String COMPUTER_SOURCE		= linuxEQprop.getCOMPUTERSOURCE();
		/**Name of branch associated to stores**/
		public static final String DES_CADENA			= linuxEQprop.getCADENA();
		/**Store number**/
		public static final String STORE_NUMBER			= linuxEQprop.getSTORENUMBER();
		/**List of Name of events to be filtered**/
		public static final String EVENT_SOURCE			= linuxEQprop.getEVENTSOURCE();
		/**terminal number associated to point of sale **/
		public static final String TERMINAL_ID			= linuxEQprop.getTERMINALID();
		
		public static final String EVENT_GROUP			= linuxEQprop.getEVENTGROUP();
	}


	





	

	

}
