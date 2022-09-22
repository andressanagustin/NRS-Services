package com.allc.properties;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.allc.files.helper.UtilityFile;
import com.ibm.OS4690.FileOutputStream4690;

public class PropFile {
	static Logger log = Logger.getLogger(PropFile.class);
	/** Atributo para singleton. */
	private static PropFile instance = null;
	/** Atributo para hacer la conexion con el archivo de propiedades **/
	public static Properties props;
	/** IP del Host */
	private String hostAddress;
	/** Nombre del Host */
	private String hostName;
	
	private PropFile(String propFileName) {
		props = new Properties();
		FileInputStream is = null;
		try {
			try {
				log.info("Generando backup de archivo de propiedades.");
				UtilityFile.copyFile(propFileName, null, "." + UtilityFile.fileSeparator() + "bak");
				log.info("Cargando archivo de propiedades.");
				is = new FileInputStream(propFileName);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				log.info("Coopiando archivo de propiedades desde backup");
				UtilityFile.copyFile(propFileName, "." + UtilityFile.fileSeparator() + "bak", "." + UtilityFile.fileSeparator());
				log.info("Cargando archivo de propiedades.");
				is = new FileInputStream(propFileName);
			}
			props.load(is);
			log.info("Archivo de propiedades cargado con exito.");
			log.info("Cargando configuracion del Host.");
			log.info("Host Name:"+this.getHostName());
			log.info("Host Address:"+this.getHostAddress());
			log.info("Configuracion del Host cargada con exito.");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			props = null;
		}
		finally {
			try {
				is.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public void storeToFile4690(String propFileName) throws Exception{
		props.store(new FileOutputStream4690(propFileName), null);
	}
	
	public void storeToFile(String propFileName) throws Exception{
		props.store(new FileOutputStream(propFileName), null);
	}
	/**
	 * Obtiene instancia para singleton.
	 * 
	 * @return
	 */
	public static PropFile getInstance(String propFileName) {
		if (instance == null) {
			instance = new PropFile(propFileName);
		}
		return instance;
	}

	public String getObject(String keyName) {
		return props.getProperty(keyName);
	}
	
	public void setObject(String keyName, String value) {
		props.setProperty(keyName, value);
	}

	public int getInt(String prop) {
		int value = 0;
		try {
			value = Integer.valueOf(getObject(prop)).intValue();
		} catch (Exception e) {
			log.error("Error al cargar la propiedad: " + prop);
			log.error(e.getMessage(), e);
		}
		return value;
	}

	public long getLong(String prop) {
		long value = 0;
		try {
			value = Long.valueOf(getObject(prop)).longValue();
		} catch (Exception e) {
			log.error("Error al cargar la propiedad: " + prop);
			log.error(e.getMessage(), e);
		}
		return value;
	}

	public Map getMap(String prop) {
		String value = getObject(prop);
		Map map = new HashMap();
		try {
			String[] list = value.split("\\,");
			for (int i = 0; i < list.length; i++) {
				String[] keyValue = list[i].split("\\:");
				map.put(keyValue[0], keyValue[1]);
			}
		} catch (Exception e) {
			log.error("Error al cargar la propiedad: " + prop);
			log.error(e.getMessage(), e);
		}
		return map;
	}

	public List getList(String prop) {
		String value = getObject(prop);
		List list = new ArrayList();
		try {
			if(value != null) {
				String[] values = value.split("\\,");
				for (int i = 0; i < values.length; i++) {
					list.add(values[i]);
				}
			}
		} catch (Exception e) {
			log.error("Error al cargar la propiedad: " + prop);
			log.error(e.getMessage(), e);
		}
		return list;
	}
	
	public String getHostAddress(){
		try {
			if(hostAddress == null)
				hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return hostAddress;
	}
	
	public String getHostName(){
		try {
			if(hostName == null)
				hostName = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return hostName;
	}
	
	public void clear() {
		props.clear();
	}
}
