package com.allc.files.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.log4j.Logger;



public class UtilityFile {
	static Logger log = Logger.getLogger(UtilityFile.class);
	
	public static void createFile(String fileName){
		try {
			File f = new File(fileName);
			f.createNewFile();
		} catch (Exception e){
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param fileName	nombreArchivo
	 * @param data		  	data
	 * @param append		true  adiciona, false = como si creara el archivo y guardara la data
	 */
    public static boolean createWriteDataFile(String fileName, String data, boolean append){
    	try {
    	
	    	PrintWriter fileaPos = new PrintWriter(new BufferedWriter(new FileWriter(fileName, append)));
			fileaPos.write(data, 0, data.length());
			fileaPos.close();
			return true;
			//log.info("Archivo: " + nombreArchivo + " Se escribio data: " + data );
		} catch (Exception e) {//FileNotFoundException e1
			log.error(e.getMessage(), e);
			return false;
		}
    }
    
    public static void appendDataFile(String fileName, String data){
    	try {
    	 
	    	PrintWriter fileaPos = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
			fileaPos.write(data, 0, data.length());
			fileaPos.close();
			log.info("Archivo: " + fileName + " Se escribio data: " + data );

		} catch (Exception e1) {
			log.error("appendDataFile: No se puede escribir en el archivo: " + fileName + " " +e1.getMessage());
		}
    }

    
    public static boolean fileExists(String nombreArchivo)
    {
    	File fichero = new File(nombreArchivo);
    	return fichero.exists();
    }
    
    public static String readLineFromFile(String fileName){
    	
		String linea; 
		try {
			
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);

			linea = br.readLine();
			//log.info("data:" +linea);
			//if( null == linea )
				//log.info("Error al leer el archivo: " + nombreArchivo + " posiblemente este vacio");
			br.close();
			fr.close();
			
			return linea;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
    	
    }
    
    public static boolean readDataFile(String rutaNombreFile, ArrayList array){
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
			log.error(e.getMessage(), e);
	    }finally{
	         try{                    
	            if( null != fr ){   
	               fr.close();     
	            }                  
	         }catch (Exception e){ 
	        	 log.error(e.getMessage(), e);
	         }
        }
    	return b_exito;
    }
    
	public static void readDataFile(String fileName, String dato){
		  File archivo = null;
	      FileReader fr = null;
	      BufferedReader br = null;

	      try {
	         // Apertura del fichero y creacion de BufferedReader para poder
	         // hacer una lectura comoda (disponer del metodo readLine()).
	         archivo = new File (fileName);
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
	    	  log.error(e.getMessage(), e);
	    	  dato = null;
	      }finally{

	         try{                    
	            if( null != fr ){   
	               fr.close();     
	            }                  
	         }catch (Exception e){ 
	        	 log.error(e.getMessage(), e);
	         }
	      }
	   }
	
	
    public static String readLineFromFile(String fileName, int row){
        
	    FileReader fr = null;
	    BufferedReader br = null;
		String linea = ""; 
		int cont=0;
		try {
			
			fr = new FileReader(fileName);
			br = new BufferedReader(fr);

			while((linea=br.readLine())!=null){
				cont++;
				if(cont == row){
					break;
				}
			}
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			
		} finally{
			try {br.close();} catch (IOException e) {}
			try {fr.close();} catch (IOException e) {}
		}
    	return linea;
    }	
    
	public static String readDataFile(String fileName){
		  

	      String dato = "";
	      
	      DataInputStream dis = null;
	      File file;
	      try {
	    	  
	    	  
	    	    file = new File(fileName);
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
	    	  log.error(e.getMessage(), e);
	    	  dato = null;
	      }finally{

	         try{        
	        	 if( null != dis )
	        		 dis.close();                  
	         }catch (Exception e){ 
	        	 log.error(e.getMessage(), e);
	         }
	      }
	      return dato;
	   }
	
	public static boolean deleteFile(String fileName){
		boolean b_exito = false;
		try{
			if(fileExists(fileName))
				if(deleteFile2(fileName))
					b_exito = true;
		}catch(Exception e){
			log.error(e.getMessage(), e);
	    }
		return b_exito;
	}
	
	public static boolean deleteFile2(String nombreFile){
		boolean b_exito = false;
		try{
			File fichero = new File(nombreFile);
			if (fichero.delete())
				b_exito = true;
		}catch(Exception e){
			log.error(e.getMessage(), e);
	    }
		return b_exito;
	}
	
	/**
	 * crea una ruta de directorios
	 * @param ruta
	 * @return		true: si crea la ruta de directorios o el directorio ya existe
	 * 				false: si no puede crearla.
	 */
	public static boolean createDir(String ruta){
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
			log.error("check the user's permission ", e);
		}catch(Exception e){
			log.error(e.getMessage(), e);
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
	
	public static boolean copyFile(String filename, String dirOrig, String dirDest){
		boolean retorno = true;
		InputStream in = null;
		OutputStream out = null;
		try{
			File origen = null;
			if(dirOrig!=null)
				origen = new File(dirOrig+fileSeparator()+filename);
			else
				origen = new File(filename);
			createDir(dirDest);
			File destino = new File(dirDest+fileSeparator()+filename);
			in = new FileInputStream(origen);
			out = new FileOutputStream(destino);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
			  out.write(buf, 0, len);
			}
		} catch (Exception e){
			log.error(e.getMessage(), e);
			retorno = false;
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				retorno = false;
			}
		}
		return retorno;
	}
}
