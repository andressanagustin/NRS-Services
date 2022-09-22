package com.allc.persistence.util;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;


public class TDprop {
	static Logger log = Logger.getLogger(TDprop.class);
	/** Atributo para singleton.*/
	private static TDprop instance = null;
	/** Atributo para hacer la conexion con el archivo de propiedades **/
	public static Properties props;
	
	private String PropFileName = "TDconf.properties";
	
	private TDprop(){
		props = new Properties();
		FileInputStream is = null;
		try{
			try {
				log.info("Generando backup de archivo de propiedades.");
				Files.copyFile(PropFileName, null, "."+Files.fileSeparator()+"bak");
				log.info("Cargando archivo de propiedades.");
				is = new FileInputStream(PropFileName);
			} catch(Exception e){
				log.error(e.getMessage(), e);
				log.info("Coopiando archivo de propiedades desde backup");
				Files.copyFile(PropFileName, "."+Files.fileSeparator()+"bak", "."+Files.fileSeparator());
				log.info("Cargando archivo de propiedades.");
				is = new FileInputStream(PropFileName);
			}
			props.load(is);
			log.info("Archivo de propiedades cargado con exito.");
        }catch(Exception e){
			log.error(e.getMessage(), e);
			props = null;
		} finally {
			try {
				is.close();
			} catch (Exception e){
				log.error(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Obtiene instancia para singleton.
	 * @return
	 */
	public static TDprop getInstance(){
		if(instance == null){
			instance = new TDprop();
		}
		return instance;
	} 
	
	public Object getObject(String keyName){
		return props.getProperty(keyName);
	}	
	
	/**
	 * @return the FolderIn
	 */	
	public String getFolderInPath(){
		return props.getProperty("reader.folder.in.path","").trim();
	}

	
	/**
	 * @return the ProcessedFolder
	 */
	
	public String getProcessedFolderName() {
		return props.getProperty("reader.folder.name.processed","processedTlog").trim();
	}
	
	/**
	 * @return pipeName
	 */
	public String getErrorFolderName(){
		return props.getProperty("reader.folder.name.error","errorTlogs").trim();
	}
	
	public String getDuplicatedFolderName() {
		return props.getProperty("reader.folder.name.duplicated","duplicatedTlog").trim();
	}
	
	public String getCalculateTaxFlag() {
		return props.getProperty("calculateTax.flag","T").trim();
	}
	
	public String getNumberTransactionsToProcess() {
		return props.getProperty("number.transactions.to.process","10").trim();
	}
}
