package com.allc.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileInputStream4690;
import com.ibm.OS4690.FileOutputStream4690;



public class Files {
	static Logger log = Logger.getLogger(Files.class);
	
	public static void creaArchivo(String nombreArchivo){
		try {
			File f = new File(nombreArchivo);
			f.createNewFile();
		} catch (Exception ioe){
			log.error(ioe);
		}
	}
	
	public static void creaArchivo4690(String nombreArchivo){
		try {
			File4690 f = new File4690(nombreArchivo);
			f.createNewFile();
		} catch (Exception ioe){
			log.error(ioe);
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
			log.error("CreaEscribeDataArchivo: No se puede escribir en el archivo: " + nombreArchivo + " " +e1);
			return false;
		}
    }
	
    public static boolean creaEscribeDataArchivo4690(String nombreArchivo, String data, boolean append){
    	try {
    		File4690 file = new File4690(nombreArchivo);
    		if(!file.exists())
    			file.createNewFile();
    		FileOutputStream4690 fos = new FileOutputStream4690(nombreArchivo, append);
    		fos.write(data.getBytes(), 0, data.length());
    		fos.close();
			return true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
    }
    
    public static void adicionaDataArchivo(String nombreArchivo, String data){
    	try {
    	 
	    	PrintWriter fileaPos = new PrintWriter(new BufferedWriter(new FileWriter(nombreArchivo, true)));
			fileaPos.write(data, 0, data.length());
			fileaPos.close();
			//log.info("Archivo: " + nombreArchivo + " Se escribio data: " + data );

		} catch (Exception e1) {
			log.error("No se puede escribir en el archivo: " + nombreArchivo + " " +e1);
		}
    }

    
    public static boolean fileExists(String nombreArchivo)
    {
    	File fichero = new File(nombreArchivo);
    	return fichero.exists();
    }
    
    public static boolean fileExists4690(String nombreArchivo)
    {
    	File4690 fichero = new File4690(nombreArchivo);
    	return fichero.exists();
    }
    
    public static String readLineOfFile(String nombreArchivo){
    	
		String linea = null; 
		try {
			
			FileReader fr = new FileReader(nombreArchivo);
			BufferedReader br = new BufferedReader(fr);
			
			linea = br.readLine();

			br.close();
			fr.close();
			
			
		} catch (Exception e1) {
			log.error("readLineOfFile: cannot open file: " + nombreArchivo + " " +e1);
		}
		return linea;
    }
    
    public static String readLineOfFile4690(String nombreArchivo){
    	
		String linea = null; 
		try {
			
			InputStreamReader fr = new InputStreamReader(new FileInputStream4690(nombreArchivo));
			BufferedReader br = new BufferedReader(fr);
			
			linea = br.readLine();

			br.close();
			fr.close();
			
			
		} catch (Exception e1) {
			log.error("readLineOfFile: cannot open file: " + nombreArchivo + " " +e1);
		}
		return linea;
    }
    
    public static String readLineOfFile4690FindString(String nombreArchivo, String stringParam){
    	
		String linea = null; 
		boolean findLine = false;
		try {
			
			InputStreamReader fr = new InputStreamReader(new FileInputStream4690(nombreArchivo));
			BufferedReader br = new BufferedReader(fr);
			
			while((linea = br.readLine()) != null && findLine != true) {
				if(linea.contains(stringParam)) {
					findLine = true;
				}
			}
			br.close();
			fr.close();
		} catch (Exception e1) {
			log.error("readLineOfFile: cannot open file: " + nombreArchivo + " " +e1);
		}
		return linea;
    }
    
    public static boolean leeDataArchivo(String rutaNombreFile, ArrayList array){
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
	    	  log.error("leeDataArchivo: " +e);
	    }finally{
	         try{      
	        	if(null != br){
	        		br.close();
	        	}
	            if( null != fr ){   
	               fr.close();     
	            }                  
	         }catch (Exception e2){ 
	        	 log.error("leeDataArchivo: " +e2);
	         }
        }
    	return b_exito;
    }
    
    public static boolean leeDataArchivo4690(String rutaNombreFile, ArrayList array){
    	InputStreamReader fr = null;
	    BufferedReader br = null;
	    String linea;
	    boolean b_exito = false;
    	try {
    		fr = new InputStreamReader(new FileInputStream4690(rutaNombreFile));
			br = new BufferedReader(fr);
			
			while((linea=br.readLine())!=null){
				if(linea.trim()!="")
					array.add(linea);
			}
			b_exito = true;
		}catch(Exception e){
	    	  log.error("leeDataArchivo: " +e);
	    }finally{
	         try{      
	        	if(null != br){
	        		br.close();
	        	}
	            if( null != fr ){   
	               fr.close();     
	            }                  
	         }catch (Exception e2){ 
	        	 log.error("leeDataArchivo: " +e2);
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
	    	  log.error("leeDataArchivo: " +e);
	    	  dato = null;
	      }finally{

	         try{
		        if(null != br){
		        	br.close();
		        }
	            if( null != fr ){   
	               fr.close();     
	            }                  
	         }catch (Exception e2){ 
	        	 log.error("leeDataArchivo: " +e2);
	         }
	      }
	   }
	
	public static void leeDataArchivo4690(String rutaNombreFile, String dato){
		  InputStreamReader fr = null;
	      BufferedReader br = null;

	      try {
	         // Apertura del fichero y creacion de BufferedReader para poder
	         // hacer una lectura comoda (disponer del metodo readLine()).
	         fr = new InputStreamReader(new FileInputStream4690(rutaNombreFile));
	         br = new BufferedReader(fr);

	         // Lectura del fichero
	         String linea;
	         
	         while((linea=br.readLine())!=null){
	            System.out.println(linea);
	            dato = dato + linea;
	         }
	      }
	      catch(Exception e){
	    	  log.error("leeDataArchivo: " +e);
	    	  dato = null;
	      }finally{

	         try{
		        if(null != br){
		        	br.close();
		        }
	            if( null != fr ){   
	               fr.close();     
	            }                  
	         }catch (Exception e2){ 
	        	 log.error("leeDataArchivo: " +e2);
	         }
	      }
	   }
	
	public static String leeDataArchivo(String rutaNombreFile){
	      String dato = "";
	      DataInputStream dis = null;
	      File file;
	      try {
	    	    file = new File(rutaNombreFile);
				dis = new DataInputStream(new FileInputStream(file));
				byte[] b = new byte[1];
				String a="";
				while ((dis.read(b)) != -1)
				{
					a = a +  (new String(b));
				}		
	      }
	      catch(Exception e){
	    	  log.error("CreaEscribeDataArchivoError: " +e);
	    	  dato = null;
	      }finally{
	         try{        
	        	 if( null != dis )
	        		 dis.close();                  
	         }catch (Exception e2){ 
	        	 log.error("CreaEscribeDataArchivoError: " +e2);
	         }
	      }
	      return dato;
	   }
	
	public static boolean borraArchivo(String nombreFile){
		boolean b_exito = false;
		try{
			if(fileExists(nombreFile))
				if(deleteArchivo(nombreFile))
					b_exito = true;
		}catch(Exception e){
			log.error("borraArchivo: " + e);
	    }
		return b_exito;
	}
	
	public static boolean borraArchivo4690(String nombreFile){
		boolean b_exito = false;
		try{
			if(fileExists4690(nombreFile))
				if(deleteArchivo4690(nombreFile))
					b_exito = true;
		}catch(Exception e){
			log.error("borraArchivo: " + e);
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
			log.error("deleteArchivo: " + e);
	    }
		return b_exito;
	}
	
	public static boolean deleteArchivo4690(String nombreFile){
		boolean b_exito = false;
		try{
			File fichero = new File(nombreFile);
			if (fichero.delete())
				b_exito = true;
		}catch(Exception e){
			log.error("deleteArchivo: " + e);
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
