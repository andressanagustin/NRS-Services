package com.allc.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.log4j.Logger;



public class Files {
	static Logger log = Logger.getLogger(Files.class);
	
	public static void creaArchivo(String nombreArchivo){
		try {
			File f = new File(nombreArchivo);
			f.createNewFile();
		} catch (Exception ioe){
			log.error(ioe.getMessage());
		}
	}

	/**
	 * 
	 * @param nombreArchivo	nombreArchivo
	 * @param data		  	data
	 * @param append		true  adiciona, false = como si creara el archivo y guardara la data
	 */
    public static boolean creaEscribeDataArchivo(String nombreArchivo, String data, boolean append){
    	try {
    	
	    	PrintWriter fileaPos = new PrintWriter(new BufferedWriter(new FileWriter(nombreArchivo, append)));
			fileaPos.write(data, 0, data.length());
			fileaPos.close();
			return true;
			//log.info("Archivo: " + nombreArchivo + " Se escribio data: " + data );
		} catch (Exception e1) {//FileNotFoundException e1
			log.error("CreaEscribeDataArchivo: No se puede escribir en el archivo: " + nombreArchivo + " " +e1, e1);
			return false;
		}
    }
    
    public static void adicionaDataArchivo(String nombreArchivo, String data){
    	try {
    	 
	    	PrintWriter fileaPos = new PrintWriter(new BufferedWriter(new FileWriter(nombreArchivo, true)));
			fileaPos.write(data, 0, data.length());
			fileaPos.close();
			log.info("Archivo: " + nombreArchivo + " Se escribio data: " + data );

		} catch (Exception e1) {
			log.error("No se puede escribir en el archivo: " + nombreArchivo + " " +e1.getMessage());
		}
    }

    
    public static boolean archivoExiste(String nombreArchivo)
    {
    	File fichero = new File(nombreArchivo);
    	return fichero.exists();
    }
    
    public static String leerLineaArchivo(String nombreArchivo){
    	
		String linea; 
		try {
			
			FileReader fr = new FileReader(nombreArchivo);
			BufferedReader br = new BufferedReader(fr);

			linea = br.readLine();
			log.info("data:" +linea);
			if( linea == null )
				log.info("Error al leer el archivo: " + nombreArchivo + " posiblemente este vacio");
			br.close();
			fr.close();
			
			return linea;
		} catch (Exception e1) {
			log.error("No se puede abrir el archivo: " + nombreArchivo + " " +e1.getMessage());
			return null;
		}
    	
    }
    
    public static boolean leeDataArchivo(String rutaNombreFile, ArrayList<String> array){
	    FileReader fr = null;
	    BufferedReader br = null;
	    String linea;
	    boolean b_exito = false;
    	try {
    		fr = new FileReader(rutaNombreFile);
			br = new BufferedReader(fr);
			
			while((linea=br.readLine())!=null){
				if(linea.trim()!="")
					array.add(linea);
			}
			b_exito = true;
		}catch(Exception e){
	    	  log.error("leeDataArchivo: " +e.getMessage());
	    }finally{
	         try{                    
	            if( null != fr ){   
	               fr.close();     
	            }                  
	         }catch (Exception e2){ 
	        	 log.error("leeDataArchivo: " +e2.getMessage());
	         }
        }
    	return b_exito;
    }
    
	public static void leeDataArchivo(String rutaNombreFile, String dato){
		  File archivo = null;
	      FileReader fr = null;
	      BufferedReader br = null;

	      try {
	         // Apertura del fichero y creacion de BufferedReader para poder
	         // hacer una lectura comoda (disponer del metodo readLine()).
	         archivo = new File (rutaNombreFile);
	         fr = new FileReader (archivo);
	         br = new BufferedReader(fr);

	         // Lectura del fichero
	         String linea;
	         
	         while((linea=br.readLine())!=null){
	            System.out.println(linea);
	            dato = dato + linea;
	         }
	      }
	      catch(Exception e){
	    	  log.error("CreaEscribeDataArchivoError: " +e.getMessage());
	    	  dato = null;
	      }finally{

	         try{                    
	            if( null != fr ){   
	               fr.close();     
	            }                  
	         }catch (Exception e2){ 
	        	 log.error("CreaEscribeDataArchivoError: " +e2.getMessage());
	         }
	      }
	   }
	
	public static String leeDataArchivo(String rutaNombreFile){
		  

	      String dato = "";
	      
	      DataInputStream dis = null;
	      File file;
	      try {
	    	  
	    	  
	    	    file = new File(rutaNombreFile);
				//log.info("CHARSET: " + informacion[19] );
				//isr = new InputStreamReader(new FileInputStream(file),"UTF-8");
				//osw = new OutputStreamWriter(new FileOutputStream(file), JAVA_ENCODING);
				dis = new DataInputStream(new FileInputStream(file));
				//InputStream inStream = clb.getAsciiStream();// .getBinaryStream(); OutputStreamWriter 
				
				//InputStreamReader isr = new InputStreamReader(inStream);
				/*
				int size = (int)isr.length();
				char[] buffer = new char[size];
				*/
				byte[] b = new byte[1];
			
				String a="";
				while ((dis.read(b)) != -1)
				{
					a = a +  (new String(b));
				}		
	    	  
	    	  
	    	  
	    	  
	    	  
	    	  
/*	         // Apertura del fichero y creacion de BufferedReader para poder
	         // hacer una lectura comoda (disponer del metodo readLine()).
	         archivo = new File (rutaNombreFile);
	         fr = new FileReader (archivo);
	         br = new BufferedReader(fr);

	         // Lectura del fichero
	         int linea;

	         while((linea=br.read())!=null){
	            System.out.println(linea);
	            dato = dato + linea;
	         }*/
	         
	      }
	      catch(Exception e){
	    	  log.error("CreaEscribeDataArchivoError: " +e.getMessage());
	    	  dato = null;
	      }finally{

	         try{        
	        	 if( null != dis )
	        		 dis.close();                  
	         }catch (Exception e2){ 
	        	 log.error("CreaEscribeDataArchivoError: " +e2.getMessage());
	         }
	      }
	      return dato;
	   }
	
	public static boolean borraArchivo(String nombreFile){
		boolean b_exito = false;
		try{
			if(archivoExiste(nombreFile))
				if(deleteArchivo(nombreFile))
					b_exito = true;
		}catch(Exception e){
			log.error("borraArchivo: " + e.getMessage());
	    }
		return b_exito;
	}
	
	public static boolean deleteArchivo(String nombreFile){
		boolean b_exito = false;
		try{
			File fichero = new File(nombreFile);
			if (fichero.delete())
				b_exito = true;
		}catch(Exception e){
			log.error("deleteArchivo: " + e.getMessage());
	    }
		return b_exito;
	}
	
	/**
	 * crea una ruta de directorios
	 * @param ruta
	 * @return		true: si crea la ruta de directorios o el directorio ya existe
	 * 				false: si no puede crearla.
	 */
	public static boolean creaDirectorio(String ruta){
		boolean b_exito = false;
		try{
			File folder = new File(ruta);
			if(folder.exists())
			   b_exito = true;
			else{ 
			   folder.mkdirs();
			   b_exito = true;
			}
		}catch (SecurityException e) {
			log.error("creaDirectorio: Verifique los permisos " + e);
		}catch(Exception e){
			log.error("creaDirectorio: " + e);
	    }
		return b_exito;	
	}
	
	/**
	 * obtiene el separador de archivos
	 * @return
	 */
	public static String fileSeparator(){
		return File.separator;
	}
	
	/**
	 * Se usa para obtener un separador de rutas pejem en windows seria ";"   c:/xxx/yyy;d:/abc/ddd
	 * @return
	 */
	public static String pathSeparator(){
		return File.pathSeparator;
	}
}
