package com.allc.string.helper;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;



public class Util {
	
	static Logger log = Logger.getLogger(Util.class);

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

	/**
	 * right pad of a String
	 * @param cadena	String to add characters
	 * @param car		Characters to be added
	 * @param cant		number of characters to be added 
	 * @return	the String plus the characters added to the right
	 */
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
	
	public static String getHostAddress(){
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			log.error("getIp: " + e);
			return null;
		}
	}
	
	public static String getHostName(){
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			log.error("getHostName: " + e);
			return null;
		}
	}	
	
	//recibe la cadena y la cantidad de digitos que tendra la longitud de la cadena que se agregara al inicio de la cadena
    public static String addLengthStartOfString(String cadena, int cantidadDigitos){
    	String ceros = "";
    	String length = String.valueOf(cadena.length());
    	for (int i = 0; i<cantidadDigitos-length.length(); i++)
    		ceros += "0";
    	return (ceros + length)+ cadena;
    }
	
    public static BitSet createBitSetFromString(String s) {
	    BitSet t = new BitSet(s.length());
	    int lastBitIndex = s.length() - 1;
	    int i = lastBitIndex;
	    while ( i >= 0) {
	        if ( s.charAt(i) == '1'){
	            t.set(lastBitIndex - i);            
	            i--;
	        }
	        else
	            i--;                
	    }
	   
	    return t;
	}
    
    
    
    public static long binaryToDecimal(String strBits){
        int num_c;
        String car1[];
        String car2[];
        long num_c2 = 0;
    	try {
            num_c=strBits.length();
            car1=new String[num_c];
            car2=new String[num_c];
          //Extraer caracter por caracter de la variable num y guardarlo en un arreglo
            for(int a=0;a<num_c;a++){
                    int b=a+1;
                    car1[a]=strBits.substring(a,b);
            }
            //Ordena al revez los numeros
            for(int a=0;a<num_c;a++){
                    car2[a]=car1[num_c-(a+1)];
            }
            //Calcular lugar de arreglos y pontencias
            for(int a=0;a<num_c;a++){
                    if((a==0)&&(car2[a].equals("1"))){
                            num_c2=num_c2+1;
                    }
                    if((a==1)&&(car2[a].equals("1"))){
                            num_c2=num_c2+2;
                    }
                    if((a>1)&&(car2[a].equals("1"))){
                            int potencia=2;
                            for(int b=1;b<a;b++){
                               potencia=potencia*2;
                            }
                            num_c2=num_c2+potencia;
                    }
            }
    		
		} catch (Exception e) {
			log.error(e);
		}
    	return num_c2;
    }
    
    
    public byte[] intToBytes( int i ) {
        ByteBuffer bb = ByteBuffer.allocate(4); 
        bb.putInt(i); 
        return bb.array();
    }
    
    public static byte[] shortToBytes( short value ) {
        ByteBuffer bb = ByteBuffer.allocate(2); 
        bb.putShort(value); 
        return bb.array();
    }
    
    public static short bytesToShort(byte[] value) {
        ByteBuffer bb = ByteBuffer.allocate(2); 
        return bb.wrap(value).getShort();
    }
    
    public static int bytesToInteger(byte[] value) {
        ByteBuffer bb = ByteBuffer.allocate(4); 
        return bb.wrap(value).getInt();
    }
    
    public static byte[] longToBytes( long value){
        ByteBuffer bb = ByteBuffer.allocate(8); 
        bb.putLong(value); 
        return bb.array();
    }
        
}
