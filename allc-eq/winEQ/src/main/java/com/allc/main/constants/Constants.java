package com.allc.main.constants;


public class Constants {
	
	public static class ProcessConstants {
		/**Codigo que indica el proceso saf**/
		public static final int SAF_PROCESS = 999;
		/**Codigo que indica el proceso que verificara que los otros threads esten corriendo**/
		//public static final int IS_ALIVE_PROCESS = 0;
		/**Codigo que indica el envio del HeartBeat**/
		public static final int HEART_BEAT_PROCESS = 998;
		/**Codigo que indica el proceso que realizara la obtenecion de los eventos de la cola de mensajes del SO Windows*/
		public static final int SAVE_WIN_EVENT_LOG_PROCESS = 4;

		public static final int LAUNCH_UPS = 5;
		
	}
	
	public static class Componente{
		/**Version del componente**/
		public static final String VERSION = "1.0.5";
		/**Fecha de la version**/
		public static final String FECHA_VERSION = "28/05/2013";
		/**Ruta donde residira la aplicacion**/
		public static final String NOMBRE_LOG = "log4jWinEQ.properties";
		/**Nombre del programa**/
		public static final String WIN_EQ_JAR = "WinEQ.jar";
		
		
	}
	
	public static class Comunicacion{

		/**Cantidad de bytes para la longitud de la trama**/
		public static final int CANTIDAD_BYTES_LONGITUD = 5;
		/**cantidad de datos del header**/
		public static final int CANTIDAD_DATOS_HEADER = 6;
		/**Caracter que separa cada dato de la trama**/
		public static final String CAR="|";
		/**Caracter que se usara para separar datos de la trama en 2do nivel**/
		public static final String CAR2=",";
		/**Caracter para expresiones regulares**/
		public static final String REGEX = "\\" + CAR;
		/**Caracter para expresiones regulares**/
		public static final String REGEX2 = "\",\"";
		/**Cadena utilizada para fin de linea**/
		public static final String CRLF = "\r\n";
		/**Cadena Espacio**/
		public static final String SPACE = " ";
		/**cadena vacia**/
		public static final String VACIO = "";		
		/**Canal de comunicacion via socket**/
		public static final String SOCKET_CHANNEL = "S";
		/**Canal de comunicacion via pipe**/
		public static final String PIPE_CHANNEL = "P";

		public static final String CERO = "0";
	}
	
	public static class Event{
		
		public static final String MESSAGE_GROUP = "V";
		
		public static final String SOURCE = "S";
		
		public static final String EVENT = "E";
	}
	
	public static class Severity{
		public static final int CODE_INFORMATION = 5;
		public static final int CODE_AUDIT_SUCCESS = 5;
		public static final int CODE_WARNING = 4;
		public static final int CODE_AUDIT_FAILURE = 2;
		public static final int CODE_ERROR = 1;
	}
	public static class DescSeverity{
		public static final String DESC_CODE_INFORMATION = "Informational";
		public static final String DESC_CODE_AUDIT_SUCCESS = "AuditSuccess";
		public static final String DESC_CODE_WARNING = "Warning";
		public static final String DESC_CODE_AUDIT_FAILURE = "AuditFailure";
		public static final String DESC_CODE_ERROR = "Error";
		
	}
}
