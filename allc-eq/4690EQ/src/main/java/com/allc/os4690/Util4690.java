package com.allc.os4690;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.allc.files.Files;
import com.allc.main.constants.Constants;
import com.allc.util.Util2;



public class Util4690 {
	static Logger log = Logger.getLogger(Util4690.class);
	
	/**
	 * obtiene el Ip del controlador
	 * @param nodo		nodo del controlador
	 * @param fileName	archivo donde se define la ip del controlador
	 * @return			ip del controlador
	 */
	public static String getIpAddress(String nodo){
		String ip = "127.0.0.1";
		String cadenaBusqueda = Constants.Comunicacion.SPACE + "lan0" + Constants.Comunicacion.SPACE;
		ArrayList arrayDatos = new ArrayList(1);
		String cadena = "";
		String fileName = "";
		int posicion = 0;
		try {
			/**Obtenemos el nombre y ruta del archivo donde se debe de buscar la IP**/
			fileName = Constants.Componente.RUTA_FILE_IP + "adxip" + nodo + "z.bat";
			
			/**leemos la data en un arrayList**/
			if(Files.leeDataArchivo4690(fileName, arrayDatos)){
				for(int i=0; i < arrayDatos.size();i++){
					cadena = ((String)arrayDatos.get(i)).trim();
					if(cadena.length()>0)
						/**si la linea no es comentario**/
						if(  !(cadena.substring(0,3).equals(Constants.Componente.COMMENT_CHAR)) ){
							/**Obtenemos la primera ocurrencia del nodo dentro de la cadena, se busca con un espacio antes**/
							posicion = cadena.indexOf(cadenaBusqueda);
							if(posicion != -1){
								/**Obtenemos el ip desde la primera posicion hasta el primer espacio en blanco**/
								ip = getIp(cadena.substring(posicion + 6));//posicion + len(" lan0 ")
								break;
							}
						}
				}
			}
			
		} catch (Exception e) {
			log.error("getIpAddress: " + e);
		}
		return ip;
	}
	
	/**
	 * dada una cadena del archivo de configuracion de IP, obtiene el ip que se encuentra en las primeras posiciones
	 * @param cadena	cadena leida del archivo de configuracion.
	 * @return			el ip de la cadena
	 */
	private static String getIp(String cadena){
		String valor;
		int posFin;
		try {
			posFin = Util2.obtPosNesimaOcurrencia(cadena, Constants.Comunicacion.SPACE, 1);
			/**Si encontro la **/
			if(posFin != -1){
				valor = cadena.substring(0, posFin);
			}else{
				valor = "127.0.0.1";
				log.error("getIp: cadena en la que se busco el ip: " + cadena + " devolvera 127.0.0.1 por defecto");
			}
		} catch (Exception e) {
			log.error("getIp: " + e);
			valor = "127.0.0.1";
		}
		return valor;
	}
	

	
}
