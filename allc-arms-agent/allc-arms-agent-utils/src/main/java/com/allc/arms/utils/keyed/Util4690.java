package com.allc.arms.utils.keyed;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.allc.arms.utils.ArmsAgentConstants;
import com.allc.files.helper.Files;

/**
 * Util methods for OS4690
 * file parsing
 */
public class Util4690 {
 
	/**
	 * We want to prohibit construction
	 * so the constructor is set to private
	 */
	private Util4690(){}

	/**
	 * Gets a bytes buffer fragment that starts in offset and ends
	 * in offset + n and returns the int (backwards) associated,
	 * as a String, ie. performs a big to little-endian or viceversa
	 * conversion.
	 * @param buffer of bytes to be ordered
	 * @param offset int used as the starting point
 	 * @param n int used as the offset relative ending
	 * @return String of the re-ordered int number within buffer
	 * @exception IllegalArgumentException if N isnt pair or if the
	 *         fragment doesnt fit the buffer.
	 */
	public static String getInt(byte buffer[], int offset, int n)
	throws IllegalArgumentException{
		int m = (int)n/2;
		if (n % 2 != 0 || n < 2) {
			throw new IllegalArgumentException("n value not valid:"+n);
		}
		if (buffer.length < offset + n) {
			throw new IllegalArgumentException("buffer outbounded:"+n);
		}
 		if (m==1) {
			return ""+ ( ( buffer[offset+1] << 8) | buffer[offset] );
		} else {
			return getInt(buffer, offset+m, m) + getInt(buffer, offset, m);
		}
	}

	/**
	 * Orders the bytes gotten from a String using the byte processing version
	 * @param s String to be ordered
	 * @return String of the int number within String
	 * @exception IllegalArgumentException if N isnt pair or if the
	 *         fragment doesnt fit the buffer.
	 */
	public static String getInt(String s)
	throws IllegalArgumentException{
		if(s.compareTo("")==0) {
			return "0";
		}
		byte b[] = s.getBytes();
		return getInt(b,0,b.length);
	}

	/**
	 * Gets a bytes buffer fragment that starts in offset and ends
	 * in offset + N and returns the unpacked number associated
	 * as a String. If it finds a '?' or '0' value then the substring without it
	 * is returned..
	 * @param byte[] buffer to be read, must be non-null
	 * @param offset int used as the starting point
 	 * @param length int used as the offset relative ending
	 * @return String of unpacked numbers without ? = F+0x30.
	 * @exception IllegalArgumentException if the fragment doesnt fit the buffer.
	 */
	public static String unpackNum(String s) throws IllegalArgumentException{
		if (s.compareTo("") == 0) return "0";
		return clean(clean(unpack(s),'?'),'0');
	}

	/**
	 * Gets a bytes buffer fragment that starts in offset and ends
	 * in offset + N and returns the unpacked number associated
	 * as a String. The characters '?' or '0' will be removed from the left front
	 * before returning the two halfs of the unpacked number as a 2 length array.
	 * @param byte[] buffer to be read, must be non-null
	 * @param offset int used as the starting point
 	 * @param length int used as the offset relative ending
	 * @return String[] of unpacked halfs.
	 * @exception IllegalArgumentException if the fragment doesnt fit the buffer.
	 */
	public static String[] unpackNums(String s)
	throws IllegalArgumentException {
		String half[] = new String[2];
		String upd = unpack(s);
		int l = upd.length();
		String h1 = upd.substring(0,(int)l/2);
		String h2 = upd.substring((int)l/2,l);
		half[0] = clean(clean(h1,'?'),'0');
		half[1] = clean(clean(h2,'?'),'0');
		if (l == 3) {
			throw new IllegalArgumentException(""+half[1]);
		}
		return half;
	}

	/**
	 * Receives a String of numbers and removes every consecutive
	 * character toRmv in the left front
	 * @param String toClean of numbers
	 * @param char to remove
	 * @return the String of numbers without toRmv in the front.
	 */
	public static String clean(String toClean, char toRmv) {
		int i = 0;
		while (i < toClean.length()){
			if(toClean.charAt(i) != toRmv) break;
			i++;
		}
		if (i == toClean.length()) return "0";
		return toClean.substring(i,toClean.length());
	}

	/**
	 * Gets a bcd packed string and returns it unpacked.
	 * @param String of packed number
	 * @return String of unpacked numbers.
	 * @exception IllegalArgumentException if the fragment doesnt fit the buffer.
	 */
	public static String unpack(String s)
	throws IllegalArgumentException {
		String ret = "";
		if (s == null) {
			throw new IllegalArgumentException();
		}
		try {
			//byte b[] = s.getBytes("ISO-8859-1");
			byte b[] = s.getBytes("UTF-8");
			ret = unpack(b,0,b.length);
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}
		return ret;
	}

	/**
	 * Gets a bytes b fragment that starts in offset and ends
	 * in offset + N and returns the unpacked number associated,
	 * as a String.
	 * @param byte[] b to be read, must be non-null
	 * @param offset int used as the starting point
 	 * @param length int used as the offset relative ending
	 * @return String of unpacked numbers.
	 * @exception IllegalArgumentException if the fragment doesnt fit the buffer b
	 */
	public static String unpack(byte b[], int offset, int length)
	throws IllegalArgumentException{
		if (b.length < offset + length) {
			throw new IllegalArgumentException("Invalid fragment");
		}
		//byte unpacked[] = new byte[length*2];
		StringBuffer s = new StringBuffer();
		for(int i = offset; i < offset + length ; i++) {
			//unpacked[2*i]   = (byte) (((b[i+offset] & 0xFF) >> 4) + 0x30);
			//unpacked[2*i+1] = (byte) (( b[i+offset] & 0xF) + 0x30);
            byte j = (new Byte(b[i])).byteValue();
            s.append((j >> 4) & 0x0F);
            s.append(j & 0x0F);
		}
	
		//return new String(unpacked);
		return s.toString();
}

	/**
	 * Used only with jdk versions under 1.4.
	 * For upper versions use String.split(String regex)
	 * @param String s to be splitted.
	 * @param char c, the separator.
	 * @return Array of splited Strings.
	 */
	public static String[] split(String s, char c) {
		int from  =-1;
		int count = 0;

		while ( (from = s.indexOf(c, from+1))  != -1)
			count++;

		String r[] = new String[++count];
		for (int i = 0 ; i < count; i++) {
			int to = s.indexOf(c, from+1);
			if (to == -1) {
				r[i] = s.substring(from+1);
				break;
			} else {
				r[i] = s.substring(from+1,to);
			}
			from = to;
		}
		return r;
	}

	/**
	 * Removes char c from both sides of s
	 * @param String s to be char-removed.
	 * @param c, the sides char.
	 * @return the string with chars removed.
	 */
	public static String[] trim(String s[], char c) {
		String t[] = new String[s.length];
		for (int i = 0; i < s.length; i++) {

			if (s[i].charAt(0)==c && s[i].charAt(s[i].length()-1)==c){
				t[i] = s[i].substring(1,s[i].length()-1);
			} else {
				t[i] = s[i];
			}
		}
		return t;
	}

	/**
	 * Gets a String of unpacked numbers and packs it into a
	 * byte[] buffer
 	 * @param String unpacked, string of unpacked numbers
	 * @return byte[] buffer with half the size of the parameter string
	 * @exception IllegalArgumentException if the String isnt a base 10 int.
	 */
	public static byte[] pack(String unpacked)
	throws IllegalArgumentException{

		byte up[] = unpacked.getBytes();
//		byte packed[] = new byte[46];
//		for (int i = 0; i < 6 ; i++) 
		byte packed[] = new byte[(unpacked.length()/2)];
		for (int i = 0; i < packed.length ; i++) {
				packed[i] |= (byte) ( (up[2*i]   - 0x30) << 4);
				packed[i] |= (byte) ( (up[2*i+1] - 0x30) );
		}
		return packed;
	}

	/**
	 * Stores a file into a byte buffer of file`s size
	 * @param File file to be stored
	 * @return byte[] buffer with file`s size.
	 * @exception IllegalArgumentException if file size is bigger than
	 *		   buffer�s max length (Integer.MAX_VALUE)
	 */
	public static byte[] getBytesFromFile(File file)
	throws IOException, IllegalArgumentException {

		InputStream is = new FileInputStream(file);
		long length = file.length();
		//Check the file length
		if (length > Integer.MAX_VALUE)
			throw new IllegalArgumentException("File size too big"+file.length());

		//Create the byte array to hold the data
		byte[] bytes = new byte[(int)length];

		//Read in the bytes
		int offset  = 0;
		int numRead = 0;
		while (offset < bytes.length
		&& (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0)
			offset += numRead;
		//Ensure all the bytes have been read in
		if (offset < bytes.length)
			throw new IOException("Incomplete read of"+file.getName());
		//Close the input stream and return bytes
		is.close();
		return bytes;
	}

	/**
	 * Reads a buffer fragments to check if every byte is zero
	 * @param offset int used as the starting point
 	 * @param length int used as the offset relative ending
	 * @return byte[] buffer with file`s size.
	 * @exception IllegalArgumentException if the fragment doesnt fit the buffer.
	 */
	public static boolean isZero(byte buffer[], int offset, int length) {
		if (buffer.length < offset + length) {
			throw new IllegalArgumentException("Invalid fragment");
		}
		for (int i=offset; i < offset+length; i++){
			if (buffer[i] != 0){
				return false;
			}
		}
		return true;
	}

	/**
	 * Answers the "is a numerical int" question.
	 * @param String s te be checked
	 * @return boolean true if s is a base 10 int number.
	 */
	public static boolean isBaseTen(String s) {
		if (s == null) return false;
		for (int i =  0; i < s.length(); i++){
			if (!Character.isDigit(s.charAt(i)))
				return false;
		}
		return true;
	}
	
	/**
	 * pack de string
	 *  
	 */

    public static void packUPD(byte Array[], int Offset, String Value)
    {
        if(Value.length() % 2 == 1)
            Value = '\377' + Value;
        int i = 0;
        for(int j = Offset; i < Value.length(); j++)
        {
            Array[j] = (byte)(((Value.charAt(i) & 0xf) << 4) + (Value.charAt(i + 1) & 0xf));
            i += 2;
        }
    }
	
	/**
	 * obtiene el Ip del controlador
	 * @param nodo		nodo del controlador
	 * @param fileName	archivo donde se define la ip del controlador
	 * @return			ip del controlador
	 */
	public static String getIpAddress(String nodo){
		String ip = "127.0.0.1";
		String cadenaBusqueda = ArmsAgentConstants.Communication.SPACE + "lan0" + ArmsAgentConstants.Communication.SPACE;
		ArrayList arrayDatos = new ArrayList(1);
		String cadena = "";
		String fileName = "";
		int posicion = 0;
		try {
			/**Obtenemos el nombre y ruta del archivo donde se debe de buscar la IP**/
			fileName = ArmsAgentConstants.Component.RUTA_FILE_IP + "adxip" + nodo + "z.bat";
			
			/**leemos la data en un arrayList**/
			if (Files.leeDataArchivo(fileName, arrayDatos)){
				for(int i=0; i < arrayDatos.size();i++){
					cadena = ((String)arrayDatos.get(i)).trim();
					if(cadena.length()>0)
						/**si la linea no es comentario**/
						if(  !(cadena.substring(0,3).equals(ArmsAgentConstants.Component.COMMENT_CHAR)) ){
							/**Obtenemos la primera ocurrencia del nodo dentro de la cadena, se busca con un espacio antes**/
							posicion = cadena.indexOf(cadenaBusqueda);
							if(posicion != -1){
								/**Obtenemos el ip desde la primera posicion hasta el primer espacio en blanco**/
								ip = getIp(cadena.substring(posicion + 6));//posicion + len(" lan0 ")
								break;
							}
						}
				}
			}
			
		} catch (Exception e) {
			//log.error("getIpAddress: " + e);
			
		}
		return ip;
	}
	
	
	/**
	 * dada una cadena del archivo de configuracion de IP, obtiene el ip que se encuentra en las primeras posiciones
	 * @param cadena	cadena leida del archivo de configuracion.
	 * @return			el ip de la cadena
	 */
	private static String getIp(String cadena){
		String valor;
		int posFin;
		try {
			posFin = obtPosNesimaOcurrencia(cadena, ArmsAgentConstants.Communication.SPACE, 1);
			/**Si encontro la **/
			if(posFin != -1){
				valor = cadena.substring(0, posFin);
			}else{
				valor = "127.0.0.1";
				//"getIp: cadena en la que se busco el ip: " + cadena + " devolvera 127.0.0.1 por defecto");
			}
		} catch (Exception e) {
			//throw new IOException("getIp: " + e);
			valor = "127.0.0.1";
		}
		return valor;
	}
	
	/**
	 * Obtiene la posicion de inicio de la Nesima ocurrencia de una cadena dentro de otra
	 * @param cadena		cadena donde se realizara la busqueda
	 * @param cadenaBuscar	cadena a buscar
	 * @param n				numero de ocurrencia que se busca
	 * @return				posicion donde se encuentra la Nesima ocurrencia de CadenaBuscar dentro de cadena
	 * 						-1 si no encontro la Nesima ocurrencia.
	 */
	public static int obtPosNesimaOcurrencia(String cadena, String cadenaBuscar, int n){
		int pos = 0;
		try {
			int count = 0, start = 0, len = cadenaBuscar.length();
			while((start = cadena.indexOf(cadenaBuscar, start+=len)) > -1){ 
				count++;
				if(n == count){
				   pos = start;
				   return pos;
				}
			}
			pos = -1;
		} catch (Exception e) {
			//log.error("obtieneOcurrencia " + e);
		} 
		return pos;
	}
}