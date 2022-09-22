package com.allc.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	

	//Crea y escribe un archivo 
	private static void CreaEscribeDataArchivo(String nombreArchivo, String data){
	   		try {
	   			PrintWriter fileaPos = new PrintWriter(new BufferedWriter(new FileWriter(nombreArchivo)));// el archivo de respuesta al pipe
				fileaPos.write(data, 0, data.length());
				fileaPos.close();
				//logWriter.write(threadName,"Escribio data al Archivo: " + nombreArchivo );
				log.info("Escribio data al Archivo: " + nombreArchivo);
	   			} catch (Exception e1) {
	   				//logWriter.write(threadName, "No se puede escribir en el archivo: " + nombreArchivo + " " +e1.getMessage());
	   				log.error("No se puede escribir en el archivo: " + nombreArchivo + " " +e1.getMessage());
	   			}

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
    
	public static String obtieneMensajeError(int CodigoError, String mensaje){
		String EstadoTrx = ""; // + util2.rpad(""," ",63) + "\r\n";
		try{
			
			String codigoError = Util2.lpad(String.valueOf(CodigoError), "0", 2);
			switch (CodigoError) {
			case 99:
				EstadoTrx = codigoError + Util2.lpad("Servidor no Activo", " ", 63) ;
				break;
			case 94:
				EstadoTrx = codigoError + Util2.lpad("Time out Respuesta", " ", 63);// + "\r\n";
				break;
			case 11:// aqui se coloca el mensaje de cualquier exception
				EstadoTrx = codigoError + Util2.lpad(mensaje, " ", 63);
				break;
			case 30:
				EstadoTrx = codigoError + Util2.lpad("Orden de compra no existe", " ", 63) ;
				break;
			case 40:
				EstadoTrx = codigoError + Util2.lpad("Existe m�s de un usuario con el mismo id", " ", 63) ;
				break;
			case 50:
				EstadoTrx = codigoError + Util2.lpad("Error BD desconocido", " ", 63) ;
				break;				
			default:
				EstadoTrx = "11" + Util2.lpad(mensaje, " ", 63) ; // Error cualquiera, el mensaje se envia x parametro
				break;
			}
		}catch(Exception e){

			log.error("obtieneMensajeError: " +e.getMessage());
		}

		log.info("obtieneMensajeError: " +EstadoTrx);
		return EstadoTrx + "\r\n";
	}
	
	public static void CreaEscribeDataArchivoError(String nombreArchivo, int codigoError, String mensajeError){
		String linea;
		try{
			linea = obtieneMensajeError(codigoError, mensajeError);
			CreaEscribeDataArchivo(nombreArchivo, linea);
		}catch(Exception e){
			log.info("Error en CreaEscribeDataArchivoError: " +e.getMessage());
		}
		
	}
	
    public static Date convierteStringADate(String fecha, String formatoFecha){

        SimpleDateFormat formato = new SimpleDateFormat(formatoFecha);
        String strFecha = fecha;
        Date fechaDate = null;

        try {
            fechaDate = formato.parse(strFecha);

            return fechaDate;
        } catch (ParseException ex) {
            log.error("convierteStringADate " + ex);
            return fechaDate;
        }

    }

}
