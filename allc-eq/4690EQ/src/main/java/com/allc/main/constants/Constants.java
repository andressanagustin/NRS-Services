package com.allc.main.constants;


public class Constants {
	
	public static class ProcessConstants {
		/**Codigo que indica el proceso saf**/
		public static final int SAF_PROCESS = 999;
		/**Codigo que indica el proceso que verificara que los otros threads esten corriendo**/
		public static final int IS_ALIVE_PROCESS = 0;
		/**Codigo que indica el proceso que realizara la obtenecion de los eventos de la cola de mensajes del os4690**/
		public static final int SAVE_4690_EVENT_QUEUE_PROCESS = 1;
		/**Codigo que indica el proceso que realiza la obtencion de los exception log del os4690**/
		public static final int SAVE_4690_EXCEPTION_LOG_PROCESS = 2;
		/**Codigo que indica el inicio de un proceso del os4690**/
		public static final int LAUNCH_PROCESS_4690 = 3;

		public static final int LAUNCH_UPS = 5;
	}
	
	public static class Componente{
		/**Ruta donde residira la aplicacion**/
		public static final String RUTA_APP = "F:\\allc_pgm\\4690eq\\";
		/**Version del componente**/
		public static final String VERSION = "1.1.6";
		/**Fecha de la version**/
		public static final String FECHA_VERSION = "21/12/2021";
		/**Ruta donde residira la aplicacion**/
		public static final String NOMBRE_LOG = "log4j4690EQ.properties";
		
		public static final String EQ_4690_JAR = "4690EQ.jar";
		
		public static final String COMMENT_CHAR = "REM";
		
		public static final String RUTA_FILE_IP = "c:/adx_sdt1/";
		
	}
	
	public static class Comunicacion{
		/**Cantidad de bytes que se leera del pipe del que se obtienen los mensajes del OS4690**/
		public static int CANTIDAD_BYTES_LEE_PIPE_RMA = 32;
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
		/**Cadena Vacia**/
		public static final String VACIO = "";		
		/**Canal de comunicacion via socket**/
		public static final String SOCKET_CHANNEL = "S";
		/**Canal de comunicacion via pipe**/
		public static final String PIPE_CHANNEL = "P";
		
		public static final String CERO = "0";
	}
	
	public static class Redirection{
		/**do nothing**/
		public static final int NOT_CREATE_PIPE_REDIRECTION = 0;
		/**create pipe to redirect the events**/
		public static final int CREATE_PIPE_REDIRECTION = 1;
		/**open pipe to redirect the events in write mode**/
		public static final int OPEN_PIPE_REDIRECTION_IN_WRITE_MODE = 2;
		/**try to open if fail then**/
		public static final int OPEN_PIPE_REDIRECTION_IN_WRITE_MODE_IF_FAIL_THEN_CREATE_PIPE_REDIRECTION = 3;
		
	}
	
}
