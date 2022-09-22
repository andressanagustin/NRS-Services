package com.allc.persistence.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.allc.persistence.ws.devsu.AutorizacionRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;



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
    
    public static String readSpecifictLineOfFile(String fileName, int row){
        
	    BufferedReader br = null;
		String linea = ""; 
		int cont=0;
		try {
			
			br = new BufferedReader( new FileReader(fileName));

			while(null != (linea = br.readLine())){
				cont++;
				//log.info("leyo " + cont);
				if(cont == row){
					break;
				}
			}
			
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			
		} finally{
			try {br.close();} catch (IOException e) {}
		}
    	return linea;
    }
    
    

	

	
	public static boolean deleteFile(String nombreFile){
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
	
	public static String leerArchivo(String nombreArchivo) {
		String respuesta = "";
		try {
			
			FileReader fr = new FileReader(nombreArchivo);
			BufferedReader entrada = new BufferedReader(fr);
			String linea;
			while ((linea = entrada.readLine()) != null) {
				respuesta = respuesta.concat(linea);
			}
			entrada.close();
			fr.close();
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			respuesta = "";
		}
		return respuesta;
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
			log.error("createDir: Verifique los permisos " + e);
		}catch(Exception e){
			log.error("createDir: " + e);
	    }
		return b_exito;	
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
	
	public static String toStringJson (Object obj) throws JsonProcessingException
    {
		//Creating the ObjectMapper object
        ObjectMapper mapper = new ObjectMapper();
        //Converting the Object to JSONString
        String jsonString = mapper.writeValueAsString(obj);
        return jsonString;
    	
    }
}
