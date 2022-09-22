/**
 * 
 */
package com.allc.arms.utils;

/**
 * @author gustavo
 *
 */
public class ParamsUtils {

	// desencripta para 6 o mas caracteres
	public static String desencriptar(String token){
		int cantidad = token.length(); //14 minimo
		
		String cadena = token.substring(10, 11) + token.substring(12, 13) + token.substring(5, 6) + token.substring(2, 3) +
				token.substring(3, 4) + token.substring(13, 14);
		
		for(int i = 16; i <= cantidad;i+=2) {
			cadena += token.substring(i-1,i);
		}
	
		return cadena;
	}
	
	// desencripta para 8 CARACTERES
	public static String desencriptar8(String token){
		String cadena = token.substring(12, 13) + token.substring(14, 15) + token.substring(5, 6) + token.substring(2, 3) + 
				token.substring(3, 4) + token.substring(15, 16) + token.substring(11, 12) + token.substring(7, 8);
		return cadena;
	}
	
}
