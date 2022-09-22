package com.allc;
/**
 * 
 */

/**
 * @author gustavo
 *
 */
public final class AllcUtils {

	public static class Communication{

		public static final String FRAME_SEP = "'|'";
		/**Cantidad de bytes para la longitud de la trama**/
		public static final int QTY_BYTES_LENGTH_FRAME = 5;
		/**cantidad de datos del header**/
		public static final int QTY_MEMBERS_HEADER = 6;
		/**Caracter que separa cada dato de la trama**/
		public static final String MEM_SEP_CHR="\\'\\|\\'"; 
		/**Caracter que se usara para separar datos de la trama en 2do nivel**/
		public static final String MEM_SEP_CHR2=",";
		/**Caracter para expresiones regulares**/
		public static final String REGEX = MEM_SEP_CHR;
		/**Caracter para expresiones regulares**/
		public static final String REGEX2 = "\",\"";
		/**Cadena utilizada para fin de linea**/
		public static final String CRLF = "\r\n";
		/**Cadena Espacio**/
		public static final String SPACE = " ";
		/**cadena vacia**/
		public static final String EMPTY_STR = "";		
		/**Canal de comunicacion via socket**/
		public static final String SOCKET_CHANNEL = "S";
		/**Canal de comunicacion via pipe**/
		public static final String PIPE_CHANNEL = "P";
		
		public static final String CERO = "0";
		
		public static int NUMBER_CERO = 0;
	}
}
