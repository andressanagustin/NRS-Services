package com.allc.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;



public class Util2 {
	
	static Logger log = Logger.getLogger(Util2.class);

	public static byte[] codificaLongitud(int i){
		byte[] buffer = new byte[2];
		buffer[0] = (byte) (i/256);
		buffer[1] = (byte) (i%256);
		//System.out.println("buffer[0]" + buffer[0]);
		//System.out.println("buffer[1]" + buffer[1]);	

		return buffer;
	}

	public static int decodificaLongitud(String valor){
		int result1 = valor.charAt(0)*256 ;
		int result2 = valor.charAt(1);
		//System.out.println("result: " + result1);
		//System.out.println("result: " + result2);
		
		return result1 + result2;
	}

	public static int decodificaLongitud(byte[] valor){
	
		int msb = (int)valor[0]; 
		int lsb = ((int)(valor[1]) & 255);
		
		return msb*256 + lsb;
	}	
	
	public static String validaNotNull(String valor){
		
		return valor==null?"":valor;
	}
		 
	//Pad por la izquierda a una cadena (cadena), una cantidad (cant), con un caracter (car)
	public static String lpad(String cadena, String car, int cant){
		   	String cad = "";
		   	for(int i = 0; i<cant; i++)
		   		cad+=car;
		   	return (cad + cadena).substring((cad + cadena).length() - cad.length());
	}
	//Pad por la derecha a una cadena (cadena), una cantidad (cant), con un caracter (car)
	public static String rpad(String cadena, String car, int cant){
			   	String cad = "";
			   	for(int i = 0; i<cant; i++)
			   		cad+=car;
			   	return (cadena + cad).substring(0,cant);
	}

	//elimina un caracter de una cadena
	public static String removeChar ( String s, char c ) { 

			    String r = "" ; 
			    for ( int i = 0 ; i < s.length () ; i ++ ) { 
			       if ( s.charAt ( i ) != c ) r += s.charAt ( i ) ; 
			    } 
			    return r; 
	}

	/*
	 * funcion que indica si un numero n1 es multiplo de n2
	 * return boolean   true: es multiplo
	 * 					false: no es multiplo
	 */
	public static boolean esMultiplo(int n1,int n2){
		if (n1%n2==0)
			return true;
		else
			return false;
	}
	
	public static boolean isNumeric(String cadena){
		try {
			Integer.parseInt(cadena);
			return true;	
		} catch (NumberFormatException nfe){
				return false;
		}
	}
	
	//recibe la cadena y la cantidad de digitos que tendra la longitud de la cadena que se agregara al inicio de la cadena
    public static String agregaLongitudInicioCadena(String cadena, int cantidadDigitos){
    	String ceros = "";
    	for (int i = 0; i<cantidadDigitos; i++)
    		ceros += "0";
    	//log.info((ceros + String.valueOf(cadena.length())).substring(String.valueOf(cadena.length()).length()) + cadena);
    	return (ceros + String.valueOf(cadena.length())).substring(String.valueOf(cadena.length()).length()) + cadena;
    }
    
    public static String convertDateToString(Date fecha, String formatoFecha){
        SimpleDateFormat formateador = new SimpleDateFormat(formatoFecha, new Locale("ES_ES"));
        return formateador.format(fecha).toString();
    }

    public static Date convertStringToDate(String fecha, String formatoFecha){

        SimpleDateFormat formato = new SimpleDateFormat(formatoFecha);
        String strFecha = fecha;
        Date fechaDate = null;

        try {
            fechaDate = formato.parse(strFecha);
            return fechaDate;
        } catch (ParseException ex) {
            log.error("convertStringToDate " + ex);
            return fechaDate;
        }

    }
    
    public static String fechaFormato(Date fecha){
        //Date fecha = new Date();
          SimpleDateFormat formateador = new SimpleDateFormat("yyyyMMddHHmmss", new Locale("ES_ES"));
     return formateador.format(fecha).toString();
    }

	public static boolean isBlankOrNull(String value){
	    return (value == null) || (value.trim().length() == 0);
	}    
}
