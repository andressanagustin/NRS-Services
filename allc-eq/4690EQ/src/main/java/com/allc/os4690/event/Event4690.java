package com.allc.os4690.event;

import java.util.Arrays;

import org.apache.log4j.Logger;

import com.ibm.OS4690.FlexosException;
import com.ibm.OS4690.InvalidParameterException;
import com.ibm.OS4690.POSFile;

public class Event4690 {
	static Logger log = Logger.getLogger(Event4690.class);
	

	private static int appTxtFileLength = 0;
	private static int lastTimeAppTxtFileLengthWas = 0;	
	private static int termTxtFileLength = 0;
	private static int lastTimeTermTxtFileLengthWas = 0;
	private static int controllerTxtFileLength = 0;
	private static int lastTimeControllerTxtFileLengthWas = 0;

	private static String cntrTxtFileName;
	private static String termTxtFileName;
	private static String applTxtFileName;
	 
	private static byte[] cntrlHdrBuffer = new byte[442];
	private static byte[] rdAllCntrlFile = null;
	 
	private static byte[] termHdrBuffer = new byte[58];
	private static byte[] rdAllTermFile = null;
	
	private static byte[] applBuffer = new byte[1];
	private static boolean is_rdApp1stTime = false;
	
	
	public void ld(byte[] paramArrayOfByte, int paramInt1, long paramLong, boolean paramBoolean, int paramInt2){
        int i = 0;
        int j = paramInt1 + paramInt2 - 1;
    
        if (paramLong < 0L) {
  	      i = 1;
  	      paramLong = (paramLong ^ 0xFFFFFFFF) + 1L;
        }
    
        byte k = (byte)((paramBoolean) && (i == 0) ? 48 : 32); //si i=0 y paramboolean => 48 (CERO) sino 32 (SPACE)
    
        while ((paramLong > 0L) && (paramInt2 > 0)) {
  	      paramArrayOfByte[j] = ((byte)(int)(paramLong % 10L + 48L));
  	      j--;
  	      paramLong /= 10L;
  	      paramInt2--;
        }

        if ((paramInt2 > 0) && (i != 0)) {
  	      paramArrayOfByte[j] = 45; //45 = -
  	      j--;
  	      paramInt2--;
        }
    
        while (paramInt2 > 0) {
            paramArrayOfByte[j] = k;
            j--;
            paramInt2--;
        }
      }
    
      public void lh(byte[] paramArrayOfByte, int paramInt1, long paramLong, boolean paramBoolean, int paramInt2){
        byte j = (byte)(paramBoolean ? 48 : 32); //si paramboolean => CERO sino SPACE
        int k = paramInt1 + paramInt2 - 1;
    
        while ((paramLong > 0L) && (paramInt2 > 0)) {
  	      int i = (short)(int)(paramLong & 0xF);
  	      paramArrayOfByte[k] = ((byte)((byte)i + (i < 10 ? 48 : 55))); // i + 48 <=> i < 10; i + 55 <=> i >= 10
  	      k--;
  	      paramLong >>= 4;
  	      paramInt2--;
        }
    
        while (paramInt2 > 0) {
            paramArrayOfByte[k] = j;
            k--;
            paramInt2--;
        }
      }
      
      
      /**
  	  * Function : return 
  	  * @param paramArrayOfByte
  	  * @return
  	  */
  	  public String buffer2HexString(char[] paramArrayOfByte){
  		StringBuffer localStringBuffer = new StringBuffer();
  		if (paramArrayOfByte != null)
  		   for (int i = 0; i < paramArrayOfByte.length; i++) {
  			   if (paramArrayOfByte[i] <= 15)
  			      localStringBuffer.append('0');
  			   localStringBuffer.append(Long.toHexString(0xFF & paramArrayOfByte[i]));
  		   }
  		return localStringBuffer.toString();
  	  }      

  	  
  	  /**
  	    * Busca si el arreglo paramArrayOfByte a partir de la posicion paramInt contiene los datos "B^B^/S^S^/E^E^"
  	    * @param paramArrayOfByte 	arreglo que contiene la data a verificar
  	    * @param paramInt			posicion a partir de la cual se iniciara la verificacion
  	    * @return					retorna 14 si es que tiene la coincidencia completa, caso contrario devuelve 0
  	    */
  	    public int checkBSE(byte[] paramArrayOfByte, int paramInt){
  		     byte[] arrayOf14BytesBSE = { 66, 94, 66, 94, 47, 83, 94, 83, 94, 47, 69, 94, 69, 94 };
  		     //					  	       B   ^   B   ^   /  S   ^   S   ^   /   E   ^   E   ^

  		     int i = 1;
  		     //for (int j = 0; j < 14; paramInt++) {
  		     for (int j = 0; j < arrayOf14BytesBSE.length; paramInt++) {
  		         if (paramArrayOfByte[paramInt] != arrayOf14BytesBSE[j]) {
  		        	 i = 0;
  		        	 break;
  		         }
  		         j++;
  		     }
  		 
  		     if (i == 1)
  		       i = 14;
  		     return i;
  	   }	
  		
  		/**
  		 * Funcion que convierte un arreglo de bytes a una cadena de caracteres
  		 * @param paramArrayOfByte arreglo de bytes con la data
  		 * @return cadena de caracteres que representa el arreglo de bytes
  		 */
  		public String buffer2String(byte[] paramArrayOfByte){
  		     char[] arrayOfChar = new char[paramArrayOfByte.length];
  		 
  		     if (paramArrayOfByte != null) {
  		       for (int i = 0; i < paramArrayOfByte.length; i++) {
  		         arrayOfChar[i] = ((char)paramArrayOfByte[i]);
  		       }
  		     }
  		     String str = new String(arrayOfChar);
  		     return str.trim();
  		}


  		
  		/**
  		 * devuelve una cadena del numero paramInt de longitud 3 con formato de ceros a la izquierda
  		 * @param paramInt Integer numero a dar formato
  		 * @return String
  		 */
  	    public String formatInt(int paramInt){

  	     if (paramInt < 10){
  	       return "00" + paramInt;
  	     }
  	     if (paramInt < 100){
  	       return "0" + paramInt;
  	     }

  	     return Integer.toString(paramInt);
  	    } 
  	    
  		public byte[] hexStringToByteArray(String s) {

  		    int len = s.length();
  		    byte[] data = new byte[len / 2];
  			
  			try{
  			    for (int i = 0; i < len; i += 2) {
  			        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
  			                             + Character.digit(s.charAt(i+1), 16));
  			    }
  			}catch (Exception e) {
  				//log.error("hexStringToByteArray: " + e);
  			}
  			return data;

  		}
  	    
  	    /**
  	     * Funcion que en un arreglo de bytes paramArrayOfByte1 como plantilla, inserta los valores de arrayOf18BytesUniqueData en el.
  	     * @param paramArrayOfByteTemplate es un mensaje del archivo ADXCSOMF.DAT/ADXTSTWF.DAT/ADXCSOZF.DAT pejem:  "HA FINALIZADO EL PROGRAMA ^10SS08^                   B^B^/S^S^/E^E^ REASON=^00DB01^ TYPE=^01DB01^ RC=^06XL08^|                                    "
  	     * @param arrayOf18BytesUniqueData contiene la data variable, que sera insertada en el mensaje anterior
  	     * @return el arreglo con las inserciones ya realizadas.
  	     */
  	    private byte[] formatMsg(byte[] paramArrayOfByteTemplate, char[] arrayOf18BytesUniqueData){
  	    	
  	    	log.debug("formatMsg, CHR buffer: " + new String(paramArrayOfByteTemplate));
  	    	log.debug("formatMsg, DEC buffer: " + paramArrayOfByteTemplate);
  	    	//log.debug("formatMsg, HEX buffer: " + buffer2HexString(paramArrayOfByteTemplate));
  	    	

  	      int i = 0;
  	      int j = 0;
  	  
  	      byte[] arrayOfByte = new byte[paramArrayOfByteTemplate.length];
  	  
  	      while((j < paramArrayOfByteTemplate.length) && (paramArrayOfByteTemplate[j] != 124)) { //mientras no se temina el arrego y no es 124 (124 ES EL | QUE INDICA FIN DEL TEXTO)
  		       log.debug("j: " + j + " char: " + (char)paramArrayOfByteTemplate[j]);
  		       if (paramArrayOfByteTemplate[j] != 94) //SI ES diferente de ^   (94 decimal)
  			       if ((paramArrayOfByteTemplate[j] == 32) && (j + 1 >= paramArrayOfByteTemplate.length)) //si es un espacio (32 decimal) y es el final del arreglo
  			            j++;
  			       else{
  			            if ((paramArrayOfByteTemplate[j] == 32) && (paramArrayOfByteTemplate[(j + 1)] == 32)) // si es un espacio y sigue un espacio
  			               j++;
  			            else if ((paramArrayOfByteTemplate[j] == 66) && (paramArrayOfByteTemplate[(j + 1)] == 94)) //si es una B y le sigue un  ^ 
  				            if (checkBSE(paramArrayOfByteTemplate, j) != 0)  //si encontro la coincidencia de la cadena "B^B^/S^S^/E^E^"
  				               j += 14; //se pasan los 14 caracteres
  		
  			            arrayOfByte[i] = paramArrayOfByteTemplate[j];
  			            i++;
  			            j++;
  			       }
  		       else{//empieza a buscar en 00SS18 BUSCA EL 00S EN LAS 3 ASIGNACIONES SIGUIENTES
  		        	j++;
  			        log.debug("i: " + j + " char: " + (char)paramArrayOfByteTemplate[j]);
  			        int k = Character.digit((char)paramArrayOfByteTemplate[j], 10) * 10;
  			        j++;
  			        log.debug("i: " + j + " char: " + (char)paramArrayOfByteTemplate[j]);
  			        int m = Character.digit((char)paramArrayOfByteTemplate[j], 10);
  			        j++;
  			        log.debug("i: " + j + " char: " + (char)paramArrayOfByteTemplate[j]);
  			        int n = k + m; //aqui se obtiene la posicion desde la que se leera en el Unique data ( en el ejemplo 00)
  			        int i1 = (char)paramArrayOfByteTemplate[j];

  			        if (i1 == 83){// 83 = S  ( luego de esta S sigue otra S )
  			           j += 2; //busca el numero
  			           log.debug("i: " + j + " char: " + (char)paramArrayOfByteTemplate[j]);
  			           k = Character.digit((char)paramArrayOfByteTemplate[j], 10) * 10;
  			           j++;
  			           log.debug("i: " + j + " char: " + (char)paramArrayOfByteTemplate[j]);
  			           m = Character.digit((char)paramArrayOfByteTemplate[j], 10);
  			           j++;
  			           log.debug("i: " + j + " char: " + (char)paramArrayOfByteTemplate[j]);
  			           //i2 = k + m;
  			           int ii5;
  			  		   int i3 = 0;
  			  		   for (int i4 = 0; i4 <  k + m ; i4++) { //aqui es loop es la cantidad de datos que se lee del unique data( en el ejemplo 18 )
  			                ii5 = (char)arrayOf18BytesUniqueData[(i4 + n)];
  			                if ((i3 != 0) || (ii5 == 0)) {
  			                	arrayOfByte[i] = 32; //32 = SPACE
  			                	i3 = 1;
  			                }else 
  			                	arrayOfByte[i] = ((byte)ii5);
  		
  			                i++;
  			           }
  			        }
  		
  			        j++;
  		
  			        int i2 = (char)paramArrayOfByteTemplate[j];
  			        long l = 0L;
  			        switch (i2){
  			          	  case 76:  //L
  					            l = 0xF000 & arrayOf18BytesUniqueData[n] << 24;
  					            l += (arrayOf18BytesUniqueData[(n + 1)] << 16);
  					            l += (arrayOf18BytesUniqueData[(n + 2)] << 8);
  					            l += arrayOf18BytesUniqueData[(n + 3)];
  					            log.debug("Long substitution value: " + l); 
  					            break;
  				          case 87:  //W
  					            l = 0xF0 & arrayOf18BytesUniqueData[n] << 8;
  					            l += arrayOf18BytesUniqueData[(n + 1)];
  					            log.debug("Word substitution value: " + l); 
  					            break;
  				          case 66:  //B
  					            l = 0xF & arrayOf18BytesUniqueData[n];
  					            log.debug("Byte substitution value: " + l);
  					            break;
  				    }
  			  
  			        j++;
  			  
  			        k = Character.digit((char)paramArrayOfByteTemplate[j], 10) * 10;
  			        j++;
  			  
  			        m = Character.digit((char)paramArrayOfByteTemplate[j], 10);
  			        j++;
  			        int i5 = k + m;
  			  
  			        log.debug("Substitution size: " + i5);
  			  
  	  	            if (i1 == 88)  // 88 = X
  			            lh(arrayOfByte, i, l, true, i5);
  			        else
  			        	ld(arrayOfByte, i, l, true, i5);

  	  	            i += Math.abs(i5);
  			        j++;
  		       }
  	      }
  	  
  	      return arrayOfByte;
  	    }
  	    
  	  

  	    
  	    private String readController(String fileMsgCtrl, char messageGroup, int messageNumber, char[] arrayOf18BytesUniqueData)
  	    {
  	      log.info("E4690Q -> Cntrl -> Inside readController");
  	      
  	      byte[] arrayOf100Bytes = new byte[100];
  	      //se llena el arreglo con SPACE
  	      for (int i2 = 0; i2 < 100; i2++)
  	        arrayOf100Bytes[i2] = 32;
  	      
  	      //byte[] arrayOfByte2 = new byte[''];
  	 	  byte[] arrayOf128BytesMessageTemplate = new byte[200];
  	      int longHeader = 442;
  	  
  	      POSFile localPOSFile = null;
  	      try {
  		      localPOSFile = new POSFile(fileMsgCtrl, "r", POSFile.SHARED_READ_ACCESS);
  		  
  		      controllerTxtFileLength = localPOSFile.length();//48314
  		      
  		  
  		      if (controllerTxtFileLength != lastTimeControllerTxtFileLengthWas) {
  		          
  		         log.debug("E4690Q -> Cntrl -> Inside try for controller file read. It is first time or file data has changed");
  		          //lee 442 bytes
  		          //localPOSFile.read(cntrlHdrBuffer, 0, 0, 1, longHeader); //(buffer en el que se lee la dara, Posicion en la que se comenzara a leer, FROM_START_OF_FILE, READ_FROM_DISK / READ_FROM_CACHE, Nro bytes a leer )
  		         localPOSFile.read(cntrlHdrBuffer, 0, POSFile.FROM_START_OF_FILE, POSFile.READ_FROM_DISK, longHeader); //(buffer en el que se lee la dara, Posicion en la que se comenzara a leer, FROM_START_OF_FILE, READ_FROM_DISK / READ_FROM_CACHE, Nro bytes a leer )
  		          
  		         log.debug("E4690Q -> Cntrl -> Header: " + buffer2String(cntrlHdrBuffer));
  		          
  		         rdAllCntrlFile = new byte[controllerTxtFileLength];
  		         localPOSFile.read(rdAllCntrlFile, 0, 0, 1, controllerTxtFileLength);
  		  
  		         lastTimeControllerTxtFileLengthWas = controllerTxtFileLength;
  		  
  		         localPOSFile.closeFull();
  		         localPOSFile = null;
  		           
  		         log.debug("E4690Q -> Cntrl -> After controller file close");
  		      }
  	        
  		      if (localPOSFile != null)
  		    	 localPOSFile.closeFull();
  	        
  	      }catch (FlexosException localFlexosException) {
  	          log.error("Error reading controller file: " + fileMsgCtrl + localFlexosException);
  	      }catch (InvalidParameterException localInvalidParameterException) {
  	          log.error("Error reading controller file: " + fileMsgCtrl + localInvalidParameterException);
  	      }

  	      int k = 0xFF & cntrlHdrBuffer[0];
  	      int m = 0xFF & cntrlHdrBuffer[1];
  	      int i4 = m << 8 | k;
  	      
  	      log.debug("E4690Q -> Cntrl -> Valid Ranges :" + i4);
  	  
  	      int[] arrayOfInt1 = new int[i4];
  	      int[] arrayOfInt2 = new int[i4];
  	      long[] arrayOfLong = new long[i4];
  	      int i1 = 0;
  	      //Para cada rango
  	      for (int n = 0; n < i4; n++) {
  	    	  i1 = n * 8;
  		      int i = 0xFF & cntrlHdrBuffer[(i1 + 2)];
  		      int j = 0xFF & cntrlHdrBuffer[(i1 + 3)];
  		      arrayOfInt1[n] = (j << 8 | i);
  	        
  	          k = 0xFF & cntrlHdrBuffer[(i1 + 4)];
  	          m = 0xFF & cntrlHdrBuffer[(i1 + 5)];
  	          arrayOfInt2[n] = (m << 8 | k);
  	        
  	          i = 0xFF & cntrlHdrBuffer[(i1 + 6)];
  	          j = 0xFF & cntrlHdrBuffer[(i1 + 7)];
  	          k = 0xFF & cntrlHdrBuffer[(i1 + 8)];
  	          m = 0xFF & cntrlHdrBuffer[(i1 + 9)];
  	          arrayOfLong[n] = ((m << 24 | k << 16 | j << 8 | i) & 0xFFFFFFFF);
  	  
  	          if ((messageNumber >= arrayOfInt1[n]) && (messageNumber <= arrayOfInt2[n])) {
  	          
  	        	 log.debug("E4690Q -> Cntrl -> Found Match for Message Number: " + messageNumber);

  	        	 long l = arrayOfLong[n] + 128 * (messageNumber - arrayOfInt1[n]);
  	        	 if (l + 128L < controllerTxtFileLength) {
  	        		Arrays.fill(arrayOf128BytesMessageTemplate, (byte)32); //se llena el arreglo con SPACE
  	        		System.arraycopy(rdAllCntrlFile, (int)l, arrayOf128BytesMessageTemplate, 0, 128);
  	        		arrayOf100Bytes = formatMsg(arrayOf128BytesMessageTemplate, arrayOf18BytesUniqueData);

  	        		log.info("E4690Q -> Cntrl -> Formatted Buffer:" + buffer2String(formatMsg(arrayOf128BytesMessageTemplate, arrayOf18BytesUniqueData)));

  	        	 }
  	          }
  	      }
  	  
  	      return buffer2String(arrayOf100Bytes);
  	    }  	    
  		

  	    private String readTerminal(String fileMsgTerm, char messageGroup, int messageNumber, char[] arrayOf18BytesUniqueData){
  	       
  	      log.info("E4690Q -> Term -> Inside readTerminal");
  	      
  	      POSFile localPOSFile = null;
  	      byte[] arrayOf100Bytes = new byte[100];
  	      for (int i = 0; i < 100; i++) {
  	        arrayOf100Bytes[i] = 32;
  	      }
  	      byte[] arrayOf42BytesMessageTemplate = new byte[42];
  	      int j = 58;
  	      try {
  	        localPOSFile = new POSFile(fileMsgTerm, "r", POSFile.SHARED_READ_ACCESS);
  	  
  	        termTxtFileLength = localPOSFile.length();
  	  
  	        if (termTxtFileLength != lastTimeTermTxtFileLengthWas) {
  	           
  	            log.debug("E4690Q -> Term -> Inside terminal message read. It is first time or file data has changed");
  	          
  	          rdAllTermFile = new byte[termTxtFileLength];
  	          //Lee 58 bytes
  	          localPOSFile.read(termHdrBuffer, 0, POSFile.FROM_START_OF_FILE, POSFile.READ_FROM_DISK, j);
  	  
  	          localPOSFile.read(rdAllTermFile, 0, POSFile.FROM_START_OF_FILE, POSFile.READ_FROM_DISK, termTxtFileLength);

  	          lastTimeTermTxtFileLengthWas = termTxtFileLength;

  	          localPOSFile.closeFull();
  	          localPOSFile = null;
  	           
  	          log.debug("E4690Q -> Term -> After terminal file close");
  	        }
  	        if (localPOSFile != null)
  	          localPOSFile.closeFull();
  	        
  	      }catch (FlexosException localFlexosException) {
  	          log.error("Error reading terminal file: " + fileMsgTerm + localFlexosException);
  	      }catch (InvalidParameterException localInvalidParameterException){
  	    	  log.error("Error reading terminal file: " + fileMsgTerm + localInvalidParameterException);
  	      }
  	  
  	      int n = 0xFF & termHdrBuffer[0];
  	      int i1 = 0xFF & termHdrBuffer[1];
  	      int i4 = i1 << 8 | n;

  	      log.debug("E4690Q -> Term -> Valid Ranges :" + i4);
  	  
  	      int[] arrayOfInt1 = new int[i4];
  	      int[] arrayOfInt2 = new int[i4];
  	      long[] arrayOfLong = new long[i4];
  	      int i3 = 0;
  	      for (int i2 = 0; i2 < i4; i2++) {
  	        i3 = i2 * 8;
  	        int k = 0xFF & termHdrBuffer[(i3 + 2)];
  	        int m = 0xFF & termHdrBuffer[(i3 + 3)];
  	        arrayOfInt1[i2] = (m << 8 | k);
  	        n = 0xFF & termHdrBuffer[(i3 + 4)];
  	        i1 = 0xFF & termHdrBuffer[(i3 + 5)];
  	        arrayOfInt2[i2] = (i1 << 8 | n);
  	        k = 0xFF & termHdrBuffer[(i3 + 6)];
  	        m = 0xFF & termHdrBuffer[(i3 + 7)];
  	        n = 0xFF & termHdrBuffer[(i3 + 8)];
  	        i1 = 0xFF & termHdrBuffer[(i3 + 9)];
  	        arrayOfLong[i2] = ((i1 << 24 | n << 16 | m << 8 | k) & 0xFFFFFFFF);
  	  
  	        if ((messageNumber >= arrayOfInt1[i2]) && (messageNumber <= arrayOfInt2[i2])) {

  	          log.debug("E4690Q -> Term -> Found Match for Message Number: " + messageNumber);
  	          long l = arrayOfLong[i2] + 42 * (messageNumber - arrayOfInt1[i2]);
  	          l += 5L;
  	  
  	          if (l + 37L < termTxtFileLength) {
  	            Arrays.fill(arrayOf42BytesMessageTemplate, (byte)32);
  	            System.arraycopy(rdAllTermFile, (int)l, arrayOf42BytesMessageTemplate, 0, 37);
  	            arrayOf100Bytes = formatMsg(arrayOf42BytesMessageTemplate, arrayOf18BytesUniqueData);

  	            log.info("E4690Q -> Term -> Formatted Buffer:" + buffer2String(formatMsg(arrayOf42BytesMessageTemplate, arrayOf18BytesUniqueData)));
  	          }
  	        }
  	      }
  	  
  	      return buffer2String(arrayOf100Bytes);
  	    }
  	  
  	    private String readApplication(String fileMsgApp, char messageGroup, int messageNumber, char[] arrayOf18BytesUniqueData){

  	      log.info("E4690Q -> Appl -> Inside readApplication");
  	      
  	      POSFile localPOSFile = null;
  	      byte[] arrayOf100Bytes = new byte[100];
  	      for (int i = 0; i < 100; i++) {
  	        arrayOf100Bytes[i] = 32;
  	      }
  	      char[] arrayOfChar = new char[3];
  	      byte[] arrayOf111BytesMessageTemplate = new byte[111];
  	      int j = 0;
  	      try{
  		      localPOSFile = new POSFile(fileMsgApp, "r", POSFile.SHARED_READ_ACCESS);
  		  
  		      appTxtFileLength = localPOSFile.length();
  		  
  		      if (appTxtFileLength != lastTimeAppTxtFileLengthWas) {

  	              log.debug("E4690Q -> Appl -> Inside Application Message Read. It is first time or file data has changed");

  		          applBuffer = new byte[appTxtFileLength];
  		          localPOSFile.read(applBuffer, 0, POSFile.FROM_START_OF_FILE, POSFile.READ_FROM_DISK, appTxtFileLength);

  		          lastTimeAppTxtFileLengthWas = appTxtFileLength;
  		          is_rdApp1stTime = true;
  		          localPOSFile.closeFull();
  		          localPOSFile = null;

  	              log.debug("E4690Q -> After application file close");
  		      }
  		  
  		      if (localPOSFile != null) {
  		          localPOSFile.closeFull();
  		      }

  	      }catch (FlexosException localFlexosException){
  	    	  log.error("E4690Q -> Appl -> " + fileMsgApp + " does not exist or file is invalid, skip it ");
  	      }catch (InvalidParameterException localInvalidParameterException){
  	    	  log.error("E4690Q -> Appl -> " + fileMsgApp + " does not exist, has invalid parameters or file is invalid, skip it ");
  	      }

  	      if (j + 111 <= appTxtFileLength) {
  		        Arrays.fill(arrayOf111BytesMessageTemplate, (byte)32);
  		        try {
  		        	System.arraycopy(applBuffer, 0, arrayOf111BytesMessageTemplate, 0, 111);
  		        }catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException1) {
  		        	Arrays.fill(arrayOf111BytesMessageTemplate, (byte)32);
  		        	log.error("E4690Q -> Appl -> information inside the file is not valid, skip it ");
  		        }
  		        
  		        j += 111;
  		        int k;
  		        if ((is_rdApp1stTime) && (messageGroup == arrayOf111BytesMessageTemplate[0])) {
  			          for (int m = 0; m < 3; m++)
  			        	  arrayOfChar[m] = ((char)arrayOf111BytesMessageTemplate[(m + 1)]);
  			          
  			          try{
  			        	  k = Integer.parseInt(new String(arrayOfChar));
  			          }catch (NumberFormatException localNumberFormatException1) {
  			        	  k = -1;
  			        	  log.error("E4690Q -> Appl -> MessageNumber in file is invalid, skip it ");
  			          }
  			          log.debug("E4690Q -> Appl -> MessageNumber: " + k);
  			          
  			          if (k == messageNumber) {
  				          arrayOf100Bytes = formatMsg(arrayOf111BytesMessageTemplate, arrayOf18BytesUniqueData);
  				          log.error("E4690Q -> ApplBuffer: " + buffer2String(formatMsg(arrayOf111BytesMessageTemplate, arrayOf18BytesUniqueData)));
  			          }
  		        }
  		  
  		        while (j < appTxtFileLength){
  		          Arrays.fill(arrayOf111BytesMessageTemplate, (byte)32);
  		          try {
  		        	  System.arraycopy(applBuffer, j, arrayOf111BytesMessageTemplate, 0, 111);
  		          } catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException2) {
  		        	  Arrays.fill(arrayOf111BytesMessageTemplate, (byte)32);
  		        	  log.error("E4690Q -> Appl -> information inside the file is invalid, skip it ");
  		          }
  		          j += 111;
  		          if (messageGroup == arrayOf111BytesMessageTemplate[0]) {
  			          for (int n = 0; n < 3; n++)
  			              arrayOfChar[n] = ((char)arrayOf111BytesMessageTemplate[(n + 1)]);
  			          try{
  			              k = Integer.parseInt(new String(arrayOfChar));
  			          }catch (NumberFormatException localNumberFormatException2) {
  			              k = -1;
  			              log.error("E4690Q -> Appl -> MessageNumber in file is invalid, skip it, counter ");
  			          }
  		
  			          log.debug("E4690Q -> Appl -> MessageNumber: " + k);
  			          if (k == messageNumber) {
  			              arrayOf100Bytes = formatMsg(arrayOf111BytesMessageTemplate, arrayOf18BytesUniqueData);
  			              log.info("E4690Q -> ApplBuffer: " + buffer2String(formatMsg(arrayOf111BytesMessageTemplate, arrayOf18BytesUniqueData)));
  			              return buffer2String(arrayOf100Bytes);
  			          }
  		          }
  		        }
  	      }
  	  
  	      return buffer2String(arrayOf100Bytes);
  	    }  	    
  	    
  	    /**
  	     * Obtiene el mensaje de la cola de mensajes con formato
  	     * @param messageGroup				Grupo del mensaje, se utiliza para obtener el formato del mensaje 
  	     * @param messageNumber				Numero del mensaje, se utiliza para obtener el formato del mensaje
  	     * @param arrayOf18BytesUniqueData	Arreglo que contiene la data del mensaje sin formato
  	     * @return	Un String con el mensaje con formato.
  	     */
  	    public String getMsgs(char messageGroup, int messageNumber, char[] arrayOf18BytesUniqueData){
  	      String str = "";
  	  
  	      if (messageGroup != 'W') {
  	    	  log.debug("E4690Q -> User Message, msg group: " + messageGroup);
  	    	  str = readApplication(applTxtFileName, messageGroup, messageNumber, arrayOf18BytesUniqueData);
  	      } else if (messageNumber < 500){
  	    	  log.debug("E4690Q -> Terminal Message, msgNum: " + messageNumber);
  	    	  str = readTerminal(termTxtFileName, messageGroup, messageNumber, arrayOf18BytesUniqueData);
  	      }else{
  	    	  log.debug("E4690Q -> Controller Message, msg group: " + messageGroup + ", msgNum: " + messageNumber);
  	    	  str = readController(cntrTxtFileName, messageGroup, messageNumber, arrayOf18BytesUniqueData);
  	      }

  	      if (str.length() < 1)
  	    	  str = "No message text found for MessageGroup:" + messageGroup + " MessageNumber:" + messageNumber;
  	      return str;
  	    }

  	    /**
  	     * Dado un entero, obtiene una cadena de caracteres representando el nivel de bits
  	     * @param value			valor a representar en bits
  	     * @param cantBits		cantidad de bits a usar para la representacion
  	     * @return			cadena con el valor en bits ( Bit mas significativo esta a la derecha, bit menos significativo a la izquierda)
  	     */
	  	private String getBits( int value, int cantBits ){
  	      int displayMask = 1 << (cantBits - 1);
  	      StringBuffer buf = new StringBuffer( cantBits );

  	      for ( int c = 1; c <= cantBits; c++ ) {
  	         buf.append(( value & displayMask ) == 0 ? '0' : '1' );
  	         value <<= 1;

  	         //if ( c % 8 == 0 )
  	         //   buf.append( ' ' );
  	      }

  	      return buf.toString();
	  	}
	  	
  	    /**
  	     * Dado un valor entero, convierte este valor en una representacion de bits, de longitud canBits, y extrae una porcion de bits desde
  	     * posInicial hasta posInicial + longBits y convierte esta porcion a un valor entero.
  	     * 
  	     * @param valor			valor a pasar a arreglo de bits
  	     * @param canBits		cantidad de bits que tendra la representacion en el arreglo de bits
  	     * @param posInicial	posicion del arreglo de bits desde donde se obtendra el valor
  	     * @param longBits		cantidad de caracteres que se tomaran a partir de posInicial para obtener el valor
  	     * @return				el numero entero de la porcion de bits indicada
  	     */
  	    public int getData(int valor, int canBits, int posInicial, int longBits){
  	    	String cad = getBits(valor, canBits);
  	    	int res = 0;
  	    	for(int i=0; i<longBits; i++) { 
  	    		int ind = canBits - (i + 1) - posInicial;
  	    		String val = cad.substring  (ind, ind + 1);
  	    		//System.out.print("[" + ind  + "]" + " = " + val);
  	    		
  	    		int valtmp = (val.equals("0") ? 0 : 1);
  	    		
  	    		valtmp = valtmp* (int)Math.pow(2,i);
  	    		//System.out.println(" por 2 elevado a la " + i + " total: " + valtmp);
  	    		res = res + valtmp;

  	    	}
  	    	
  	    	return res;
  	    }
  	    
  	    public String fecha(int val){
  	    	return " ";
  	    }
  	      	   
  	    public String hora(int val){
  	    	return " ";
  	    }

		public String getCntrTxtFileName() {
			return cntrTxtFileName;
		}

		public String getTermTxtFileName() {
			return termTxtFileName;
		}

		public String getApplTxtFileName() {
			return applTxtFileName;
		}

		public void setCntrTxtFileName(String cntrTxtFileName) {
			Event4690.cntrTxtFileName = cntrTxtFileName;
		}

		public void setTermTxtFileName(String termTxtFileName) {
			Event4690.termTxtFileName = termTxtFileName;
		}

		public void setApplTxtFileName(String applTxtFileName) {
			Event4690.applTxtFileName = applTxtFileName;
		}

 	    
 }
