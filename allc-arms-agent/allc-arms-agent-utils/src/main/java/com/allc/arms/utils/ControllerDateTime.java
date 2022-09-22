package com.allc.arms.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ibm.OS4690.File4690;
import com.ibm.OS4690.Runtime4690;

/**
 * Clase para poder obtener/cambiar la fecha/hora del controlador
 */
public class ControllerDateTime {
	private static Logger logger = Logger.getLogger(ControllerDateTime.class);
	private static final String OPC_SET_TIME = "T"; // SETEA LA HORA - FORMATO: HHMMSS
	private static final String OPC_SET_DATE_TIME = "D"; //SETEA EL DIA Y LA HORA - FORMATO: HHMMSS YYMMDD
	private static final String OPC_GET_DATE_TIME = "S"; //OBTIENE EL DIA Y LA HORA - FORMATO: 
	
	public static Date getDateTimeController() {
		String result = process(OPC_GET_DATE_TIME, "");
		try {
			result = result.replace(" ", ""); //SACAR LOS ESPACIOS DEL MEDIO
			Date fecha = ArmsAgentConstants.DateFormatters.yyMMddHHmmss_format.parse(result);
			logger.info("FECHA OBTENIDA: " + fecha);
			return fecha;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
	
	public static void setDateTimeController(Date date) {
		String dateTime = ArmsAgentConstants.DateFormatters.HHmmss_yyMMdd_format.format(date);
		process(OPC_SET_DATE_TIME, dateTime);
	}
	
	public static void setTimeController(Date date) {
		String time = ArmsAgentConstants.DateFormatters.HHmmss_format.format(date);
		logger.info("HORA A CAMBIAR: " + time);
		process(OPC_SET_TIME, time);
	}
	
	/**
	 * Ejecuta programa TIMER.286 escrito en BASIC que obtiene/setea la hora del controlador
	 * @param type: Los OPC definidos en atributos
	 * @param value: Valor a setear 
	 */
	private static String process(String type, String value) {
		try {
			String cmd = "TIME " + type + value;
			Runtime4690 rt = Runtime4690.getRuntime();
			Process proc = rt.exec(cmd, null, new File4690(""));
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			String result = null;

			// Read the output from the command
			logger.info("Here is the standard output of the command:");
			String s = null;
			while ((s = stdInput.readLine()) != null) {
			    logger.info(s);
			    if (s.equals("S")) continue; //IMPRIME EL PARAMETRO ENVIADO, LO DESCARTO
			    result = s; 
			}

			// Read any errors from the attempted command
			logger.info("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
				logger.error(s);
				//VER --> CAPTURAR ERRORES
			}
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
}
