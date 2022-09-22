/**
 * 
 */
package com.allc.format.helper;

/**
 * @author GUSTAVOK
 * 
 */
public class Formatter {

	public static String formatYear(String year) {
		return "20" + year;
	}

	public static String formatMonth(String month) {
		try {
			new Integer(month);
		} catch (NumberFormatException e) {
		}
		if ("10".equalsIgnoreCase(month)) {
			return "A";
		} else if ("11".equalsIgnoreCase(month)) {
			return "B";
		} else if ("12".equalsIgnoreCase(month)) {
			return "C";
		}
		return month.substring(1,2);
	}
	
	public static String rellenarConCerosIzq(String cadena, int cantidad){
		String retorno = cadena;
		for (int i = cadena.length(); i < cantidad; i++){
			retorno = "0"+retorno;
		}
		return retorno;
	}
}
