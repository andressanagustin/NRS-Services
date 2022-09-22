package com.allc.os4690;





/**
 * Esta clase empaqueta cadenas ascii usando el formato bcd 
 * 
 * @author Alex Padilla
 * @version 1.0
 *  
 */
public class BCD {
	public BCD() {	}
	
	/**
	 * Desempaqueta una cadena empaquetada
	 * 
	 * @parametro b : el arreglo empaquetado usando formato BCD
	 * @return la cadena original desencriptada
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
	
    public static String unpack(byte abyte0[], boolean flag)
    {
        StringBuffer stringbuffer = new StringBuffer();
        boolean flag1 = false;
        if(flag)
        {
            char c = mapToChar((byte)((abyte0[0] & 0xf0) >> 4));
            char c2 = mapToChar((byte)(abyte0[0] & 0xf));
            switch(c)
            {
            default:
                break;

            case 70: // 'F'
                if(c2 == 'D')
                    stringbuffer.append('-');
                else
                    stringbuffer.append(c2);
                flag1 = true;
                break;

            case 68: // 'D'
                stringbuffer.append('-');
                stringbuffer.append(c2);
                flag1 = true;
                break;

            case 48: // '0'
                if(c2 == 'D')
                {
                    stringbuffer.append('-');
                    flag1 = true;
                }
                break;
            }
        }
        for(int i = flag1 ? 1 : 0; i < abyte0.length; i++)
        {
            char c1 = mapToChar((byte)((abyte0[i] & 0xf0) >> 4));
            char c3 = mapToChar((byte)(abyte0[i] & 0xf));
            stringbuffer.append(c1);
            stringbuffer.append(c3);
        }

        return stringbuffer.toString();
    }

    protected static char mapToChar(byte byte0)
    {
        char c;
        if(byte0 < 10)
            c = (char)(48 + byte0);
        else
            c = (char)(65 + (byte0 - 10));
        return c;
    }	
    
    public static String convertBCDToString(byte abyte0[])
    {
        StringBuffer s = new StringBuffer();
        for(int i = 0; i < abyte0.length; i++)
        {
            byte j = (new Byte(abyte0[i])).byteValue();
            s.append(j >> 4 & 0xf);
            s.append(j & 0xf);
        }

        return s.toString();
    }
    

/*
	//recibe la cadena y la cantidad de digitos que tendra la longitud de la cadena que se agregara al inicio de la cadena
    public static String agregaLongitudInicioCadena(String cadena, int cantidadDigitos){
    	String ceros = "";
    	for (int i = 0; i<cantidadDigitos; i++)
    		ceros += "0";
    	
    	return (ceros + String.valueOf(cadena.length())).substring(String.valueOf(cadena.length()).length()) + cadena;
    }
    
    public static String lpad(String cadena, String car, int cant){
    	String ceros = "";
    	for(int i = 0; i<cant; i++)
    		ceros+=0;
    	
    	return (ceros + cadena).substring((ceros + cadena).length() - ceros.length());
    }
    
	 public static void main(String args[])
	    {
		 
		 QuiebreFilaComponente.logIn("0099999999", "99999999", "C:\\ADX_IDT1\\EAMOPERA.DAT");
			
	    }*/	
}