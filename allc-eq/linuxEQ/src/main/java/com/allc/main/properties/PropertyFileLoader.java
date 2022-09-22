 package com.allc.main.properties;
 
 import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import org.apache.log4j.Logger;
 
 public class PropertyFileLoader
 {
	 
   static Logger log = Logger.getLogger(PropertyFileLoader.class);
 
   public static HashMap<String, String> getProperties(String paramString)
     throws IOException
   {
     HashMap<String, String> localHashMap = new HashMap<String, String>();
 
     Properties localProperties = new Properties();
 
     //PropertyFileLoader localPropertyFileLoader = new PropertyFileLoader();
 
     //InputStream localInputStream = localPropertyFileLoader.getClass().getResourceAsStream(paramString);
     
     InputStream localInputStream = new FileInputStream(paramString);
 
     if (null != localInputStream) {
       try
       {
         localProperties.load(localInputStream);
 
         @SuppressWarnings("rawtypes")
		Enumeration localEnumeration = localProperties.propertyNames();
 
         while (localEnumeration.hasMoreElements())
         {
           String str1 = (String)localEnumeration.nextElement();
 
           String str2 = localProperties.getProperty(str1);
 
           localHashMap.put(str1.trim(), str2.trim());
         }
 
       }
       catch (Exception localException)
       {
    	   localHashMap = null;
         log.error("Exception while loading property file " + localException.getMessage());
         
       }
 
     }
 
     return localHashMap;
   }
 }