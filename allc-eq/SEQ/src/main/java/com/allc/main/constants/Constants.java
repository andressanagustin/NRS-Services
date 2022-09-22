package com.allc.main.constants;

import com.allc.files.Files;

public class Constants {
	public static class ControlMensajes {
		
	    public static final int RESULT_CX_NOK_SERVER_SWITCH_NO_ACTIVE = 99;
	    public static final int RESULT_CX_NOK_TRAMA_ERROR = 98;
	    public static final int RESULT_CX_NOK_NO_DATA_FOUND_FILE = 97;
	    public static final int RESULT_CX_NOK_FILE_NOT_FOUND = 96;
	    public static final int RESULT_CX_NOK_RQ_UNKNOWN = 95;
	    public static final int RESULT_CX_NOK_SERVICE_NOT_CONNECTED_TO_SWITCH = 94;
	    public static final int RESULT_CX_NOK_SWITCH_NOT_CONNECTED_TO_SERVICE = 93;
	    public static final int RESULT_CX_NOK_TRAMA_INCOMPLETE = 92;
	    public static final int RESULT_CX_NOK_TIME_OUT_SWITCH = 91; //no respondio el Pool de Conexiones
	    
	    public static final int RESULT_CX_NOK_FILE_CANT_SAVE_IN_CTRL = 90;
	    public static final int RESULT_CX_NOK_CANT_READ_FROM_SOCKET = 89;
	    public static final int RESULT_CX_NOK_CANT_WRITE_TO_SOCKET = 88;

		/**
		 * Constantes para el manejo de consulta a la BD
		 */
	    public static final int RESULT_CX_NOK_POOL_NOT_GET_A_CONNECTION = 80;
	    public static final int RESULT_CX_NOK_NULL_DATA = 79;
	    public static final int RESULT_CX_NOK_DATABASE_NOT_GET_A_CONNECTION = 78;
	    
	    
	    public static final int RESULT_JAVA_EXCEPTION = 77;
		
		public static final int RESULT_MS_OK = 00;
		
		public static final int RESULT_MS_NOT_DATA_FOUND = 76;
		public static final int RESULT_MS_TIME_OUT = 75;
		//public static final int RESULT_CX_NOT_SAVE_DATA = 74;
		public static final int RESULT_CX_HEADER_NOT_FOUND_IN_LIST = 73;
		public static final int RESULT_CX_LIST_LENGTH_ERROR = 72;
		public static final int RESULT_CX_GENERIC_ERROR_EXCEPTION = 71;
		public static final int RESULT_CX_RESPONSE_ERROR = 70;
		
		/**
		 * Constantes para el manejo de consulta de clientes
		 */
		public static final int RESULT_QUERY_NO_DATA_FOUND = 69;
		public static final int RESULT_NO_SE_REGISTRO_AL_CLIENTE = 68;
		
		/**
		 * Constantes para el manejo de consulta de Articulos, operadores, supervisores
		 */
		public static final int RESULT_OPERADOR_NO_REGISTRADO = 59;
		public static final int RESULT_ARTICULO_NO_REGISTRADO = 58;	
		public static final int RESULT_SUPERVISOR_NO_REGISTRADO = 57;
		public static final int RESULT_OPERADOR_CLAVE_NO_COINCIDE = 56;
		public static final int RESULT_SUPERVISOR_CLAVE_NO_COINCIDE = 55;
		
		/**
		 * Constantes para el manejo de consulta a otra background
		 */
		public static final int RESULT_BACKGRND_FOLIO_NO_RESPONDE = 45;
		public static final int RESULT_NO_HAY_CX_BACKGRND_FOLIO = 44;
		
		/**
		 * Constantes para el manejo de consulta a la BD
		 */
		public static final int RESULT_ERROR_EN_EL_PARSER_DEL_DOC = 39;
		public static final int RESULT_ERROR_AL_GENERAR_LA_TRAMA_CON_EL_DOC = 39; 
		
		/**
		 * Para manipular Pipes PI2
		 */
		public static final int RESULT_ERROR_AL_ABRIR_PIPE = 30;
		public static final int RESULT_ERROR_AL_CERRAR_PIPE = 29;
		public static final int RESULT_ERROR_AL_LEER_PIPE = 28;
		public static final int RESULT_ERROR_AL_ESCRIBIR_PIPE = 27;
		/**
		 * particulares
		 */
		public static final int RESULT_ERROR_NO_SE_CREO_DIRECTORIO = 20;
		public static final int RESULT_ERROR_NO_SE_REGISTRO_DATA_EN_FILE = 19;
		
	}

	public static class DescriptorMensajes {
		
		public static final String DES_CX_NOK_SERVER_SWITCH_NO_ACTIVE = "El servidor al que se intenta conectar no esta activo.";
		
		
		public static final String DES_CX_NOK_TRAMA_ERROR = "Error en la trama.";
	    public static final String DES_CX_NOK_NO_DATA_FOUND_FILE = "No se encontro data en el archivo.";
	    public static final String DES_CX_NOK_FILE_NOT_FOUND = "No se encontro el archivo.";
	    public static final String DES_CX_NOK_RQ_UNKNOWN = "Tipo de requerimiento desconocido";
	    public static final String DES_CX_NOK_SERVICE_NOT_CONNECTED_TO_SWITCH = "El servicio no se pudo conectar al Switch.";
	    public static final String DES_CX_NOK_SWITCH_NOT_CONNECTED_TO_SERVICE = "El switch no se pudo conectar al servicio.";
	    public static final String DES_CX_NOK_TRAMA_INCOMPLETE = "Trama incompleta.";
	    public static final String DES_CX_NOK_TIME_OUT_SWITCH = "El switch no respondio el requerimiento."; //no respondio el Pool de Conexiones
	    
	    public static final String DES_CX_NOK_FILE_CANT_SAVE_IN_CTRL = "No se pudo registrar la informacion.";
	    public static final String DES_CX_NOK_CANT_READ_FROM_SOCKET = "No se puede leer del socket.";
	    public static final String DES_CX_NOK_CANT_WRITE_TO_SOCKET = "No se puede escribir en el socket.";
	    
		/**
		 * Constantes para el manejo de Base de datos
		 */
	    public static final String DES_CX_NOK_POOL_NOT_GET_A_CONNECTION = "No se puede obtener una conexion del Pool de BD.";
	    public static final String DES_CX_NOK_NULL_DATA = "Fallo la Conexion con la Base de datos.";
	    public static final String DES_CX_NOK_DATABASE_NOT_GET_A_CONNECTION = "La BD no brindo una conexion.";
		
	    
		public static final String DES_CX_OK = "Proceso terminado ok.";
		public static final String DES_CX_NOT_DATA_FOUND = "Lista recepcionada sin datos.";
		public static final String DES_CX_TIME_OUT = "Tiempo de espera agotado para el envio de respuesta.";
		public static final String DES_CX_HEADER_NOT_FOUND_IN_LIST = "Cabecera no recivida.";
		public static final String DES_CX_LIST_LENGTH_ERROR = "El largo especificado en la cabecera no coincide con el total de data recepcionada.";
		public static final String DES_CX_GENERIC_ERROR_EXCEPTION = "Excepcion Error Generico.";
		public static final String DES_CX_RESPONSE_ERROR = "Error al responder a central.";
		
		/**
		 * Constantes para el manejo de consulta de clientes
		 */
		public static final String DES_QUERY_NO_DATA_FOUND = "El Cliente no esta registrado";
		public static final String DES_NO_SE_REGISTRO_AL_CLIENTE = "No se ha registrado al cliente.";
		
		/**
		 * Constantes para el manejo de consulta de Articulos, operadores, supervisores
		 */
		public static final String DES_OPERADOR_NO_REGISTRADO = "El Operador no esta registrado";
		public static final String DES_ARTICULO_NO_REGISTRADO = "El articulo no esta registrado";		
		public static final String DES_SUPERVISOR_NO_REGISTRADO = "El supervisor no esta registrado";
		public static final String DES_OPERADOR_CLAVE_NO_COINCIDE = "La constrase�a es incorrecta";
		public static final String DES_SUPERVISOR_CLAVE_NO_COINCIDE = "La constrase�a es incorrecta";
		
		/**
		 * Constantes para el manejo de consulta a otra background
		 */
		public static final String DES_BACKGRND_FOLIO_NO_RESPONDE = "Hay problemas con la respuesta de la BCKGRND de Folios";
		public static final String DES_NO_HAY_CX_BACKGRND_FOLIO = "No se puede escribir a la BCKGRND de Folios";
		
		
		/**
		 * Constantes para el manejo de consulta a la BD
		 */
		public static final String DES_ERROR_EN_EL_PARSER_DEL_DOC = "Error al realizar el parser del doc.";
		public static final String DES_ERROR_AL_GENERAR_LA_TRAMA_CON_EL_DOC = "Error al generar la trama del documento solicitado";
		
		
		public static final String DES_ERROR_AL_ABRIR_PIPE = "No se pudo abrir el pipe ";
		public static final String DES_ERROR_AL_CERRAR_PIPE = "No se pudo cerrar el pipe ";
		public static final String DES_ERROR_AL_LEER_PIPE = "No se pudo leer del pipe ";
		public static final String DES_ERROR_AL_ESCRIBIR_PIPE = "No se pudo escribir al pipe ";
		
		public static final String DES_ERROR_NO_SE_CREO_DIRECTORIO = "No se pudo registrar el directorio ";
		public static final String DES_ERROR_NO_SE_REGISTRO_DATA_EN_FILE = "No se pudo registrar la informacion ";
	}
	
	public static class Sistema{
		/** Cantidad de datos separados por "|" de la data recivida de central.*/
		public static String FS = Files.fileSeparator();

	}
	
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
		/**Codigo que indica el proceso que realiza la obtencion de los eventos de la cola de mensaje del SO Windows**/
		public static final int SAVE_WIN_EVENT_LOG_PROCESS = 4;
		
		public static final int LAUNCH_UPS = 5;
	}
	
	public static class Componente{
		/**Ruta donde residira la aplicacion**/
		public static final String NOMBRE_LOG = "log4jSEQ.properties";
		/**Version del componente**/
		public static final String VERSION = "1.0.1";
		/**Fecha de la version**/
		public static final String FECHA_VERSION = "01/07/2013";
		/****/
		
	}
	
	public static class Comunicacion{
		/**Cantidad de bytes que se leera del pipe con letra**/
		//public static int CANTIDAD_BYTES_LEE_PIPE = 120;
		/**Cantidad de bytes para la longitud de la trama**/
		public static final int CANTIDAD_BYTES_LONGITUD = 5;
		/**cantidad de datos del header**/
		public static final int CANTIDAD_DATOS_HEADER = 6;
		/**Caracter que separa cada dato de la trama**/
		public static final String CAR="|";
		/**Caracter para expresiones regulares**/
		public static final String REGEX = "\\|";
		/**Cadena utilizada para fin de linea**/
		public static final String CRLF = "\r\n";		
		/**cadena utilizada como espacio**/
		public static final String SPACE=" ";
		/**Utilizado para dar formato al archivo de Folios**/
		//public static final int CANTIDAD_CAR_X_LINEA = 80;
		
	}	
	

	public static class ArmsServerCommunication {

		public static final String FRAME_SEP = "'|'";
		public static final String MEM_SEP_CHR = "\\'\\|\\'";
		/** Caracter para expresiones regulares **/
		public static final String REGEX = MEM_SEP_CHR;
		/** Canal de comunicacion via socket **/
		public static final String SOCKET_CHANNEL = "S";
		/** Canal de comunicacion via pipe **/
		public static final String PIPE_CHANNEL = "P";
		/** Constante que indica que la conexión es permanente **/
		public static final String PERM_CONN = "1";
		/** Constante que indica que la conexión no es permanente **/
		public static final String TEMP_CONN = "0";

		public static final String CERO = "0";
	}

	
}
