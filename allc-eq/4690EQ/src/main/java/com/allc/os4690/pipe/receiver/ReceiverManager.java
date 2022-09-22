package com.allc.os4690.pipe.receiver;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.allc.conexion.Trama;
import com.allc.util.Util2;



/**
 * Clase encargada de recibir la informacion de un pipe pi2.
 * Los datos para realizar la lectura se encuentran en la clase Receiver que recibe como parametro en el constructor
 * Devuelve la lectura mediante la clase Trama
 * @author Alexander Padilla
 *
 */
public class ReceiverManager {


	static Logger log = Logger.getLogger(ReceiverManager.class);

	private Receiver receiver;

	public ReceiverManager(Receiver receiver){
		super();
		this.receiver = receiver;
	}

	/**
	 * Metodo que se encarga de leer la data de un pipe pi2 que se creo para recibir informacion
	 * No sale del metodo a menos que leea un dato
	 * @return		una Instancia de la clase Trama o un valor nulo si es que no llego a leer informacion.
	 */
	public Trama capture(){

		
		byte[] bytes;
		int totalleido;
		int totalleidos = 0;		
		int totbytesaleer = 0;
		String data = null;		
		Trama trama = null;
		try {
			while (true) {
				try{
					/**Se verifica que hayan llegado al menos los bytes de longitud**/
					if((totbytesaleer = receiver.getPosPipeInputStream().available() ) >= receiver.getCantBytesLongitud() ){
						/**Declaramos la variable en la que se almacena la longitud de la trama**/
						byte[] arrayOfBytesReadPI2 = new byte[receiver.getCantBytesLongitud()];
						/**Se obtiene la cantidad de bytes a leer en los primeros caracteres**/
						receiver.getPosPipeInputStream().read(arrayOfBytesReadPI2,0,receiver.getCantBytesLongitud());
						String strLong = new String(arrayOfBytesReadPI2);
						/**Si es numerica la longitud**/
						if(Util2.isNumeric(strLong)){
						  
						  totbytesaleer = Integer.parseInt(strLong);
						  log.info("Nro bytes recibidos: " + totbytesaleer);
						  if(totbytesaleer > 0){
							  
							  /**se crea un buffer con el tama�o de la trama que falta leer**/
							  bytes = new byte[totbytesaleer];
							  /**Leemos la data restante del pipe**/
							  while ((totbytesaleer - totalleidos) > 0){
								log.info("Total Bytes a leer del pipe: "+ String.valueOf(totbytesaleer));
								totalleido = receiver.getPosPipeInputStream().read(bytes, totalleidos, totbytesaleer - totalleidos);
								log.info("Total Bytes leidos del pipe: "+ totalleido);
								totalleidos = totalleidos + totalleido;
							  }
							  /**Se convierte a cadena**/
							  data = new String(bytes);
							  log.info("Data leida del pipe: "+ receiver.getNombrePipe() +": " + data);						   
							  /**Se carga la data en una lista**/ 
							  List list = Arrays.asList(receiver.getP().split(data));
							  /**Se crea una instancia de la clase Trama**/
							  trama = new Trama(list, receiver.getCantDatosHeader(), receiver.getCar());
							  /**si carga sin problemas**/
							  if(trama.loadData()){
								log.info("Clase Trama cargada correctamente: " + trama.toString());
							  }else{
								  trama = null;
							  }
						  }else
							  trama = null;
							  
						  break;

						}else{
							log.error("La trama no tiene el formato adecuado, longitud: " + strLong);
							break;
						}
						
					}else{
						Thread.sleep(receiver.getTimeOutSleep());	
					}
				}catch(Exception e){
					log.error("capture: " + e);
				}
			}
		}catch (Exception e) {
			log.error("capture: " + e);
		}

		return trama;
	}
	

}
