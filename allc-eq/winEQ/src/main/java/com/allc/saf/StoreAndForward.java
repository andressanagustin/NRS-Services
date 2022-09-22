package com.allc.saf;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.allc.conexion.ConexionCliente;
import com.allc.conexion.Trama;
import com.allc.files.Files;
import com.allc.util.Util2;


/**
 * Clase utilizada para realizar el registro y envio de informacion, para cuando se encuentre en condiciones de procesos que no esten en linea
 * o que no necesiten de comunicacion con respuesta inmediata.
 * @author Alexander Padilla
 *
 */
public class StoreAndForward {
	static Logger log = Logger.getLogger(StoreAndForward.class);

	ConexionCliente conexionCliente;
	Saf saf;
	
	static RandomAccessFile randStore = null;

	public StoreAndForward(){
		
	}
	
	public StoreAndForward(ConexionCliente conexionCliente, Saf saf){
		this.conexionCliente = conexionCliente;
		this.saf = saf;

	}


    /**
     * Metodo que indica si se debe de ejecutar el envio de la data. 
     * Valida si existen los archivos seek y store
     * Valida que el archivo seek contenga un numero valido
     * @return		True: se debe de enviar la data
     * 				False: No existe data para enviar o no se puede enviar por que fallaron las validaciones
     */
    public boolean permiteEnvioSAF(){
    	long puntero;

    	String linea;
    	try{
    		if(!Files.fileExists(saf.getFileSeek())) return false;
    		if(!Files.fileExists(saf.getFileStore())) return false;
    		puntero = obtieneOffsetSAF(saf.getFileSeek());
    		if(puntero >= 0){
    			try{
					randStore = new RandomAccessFile(saf.getFileStore(),"r");
					randStore.seek(puntero);//Seek to end of file
					linea=randStore.readLine();
					randStore.close();
					if(linea!=null){
						return true;
					}
    			}catch ( Exception ex ){
    				log.error("permiteEnvioSaf: " + ex.fillInStackTrace());
    			}
    		}
    	}catch ( Exception ex ){
			log.error("permiteEnvioSaf: " + ex.fillInStackTrace());
		}
    	return false;
    }

    /**
     * Obtiene la posicion del archivo store, desde la que se debe de enviar la informacion.
     * @param nombreArchivo		Nombre del archivo seek
     * @return					La posicion de envio
     * @throws IOException		Si el archivo seek no contiene un numero.
     */
	long obtieneOffsetSAF(String nombreArchivo) {
		long punteroFile;
		String data;
		try {
			data = Files.leerLineaArchivo(nombreArchivo);
			if(Util2.isBlankOrNull(data))
				punteroFile = 0;
			else
				try {
					punteroFile = Long.parseLong(data.trim());
				} catch (Exception e) {
					log.error("obtieneOffsetSAF: the file " + nombreArchivo + " not contain a number as a pointer. " + e.getMessage());
					punteroFile = -1;
				}		
		}catch ( Exception e ){
			log.error("obtieneOffsetSAF: " + e);
			punteroFile = -1;
		}
		return punteroFile;
	}	
	
	
	/**
	 * Metodo que se encarga de enviar la data guardada en el archivo store.
	 * Utiliza el archivo seek para tomar la posicion de envio en el archivo store
	 */
	public void forwardSAF( ){
    	File ArchivoStore = null;
		long punteroFile=-1;
		String linea = null;
		//int longlinea = 0 ;
		String data;
		boolean huboError = false;
		try{
    		ArchivoStore = new File (saf.getFileStore());
	    	punteroFile = obtieneOffsetSAF(saf.getFileSeek());
	    	log.info("file pointer: " + punteroFile);
	    	Trama trama;
	    	if(punteroFile >= 0){
		    	try{
					randStore = new RandomAccessFile(ArchivoStore,"r");
					randStore.seek(punteroFile);	//Seek to end of file
					while((linea=randStore.readLine())!=null){
						log.info("forward: line read from " + saf.getFileStore() +": "+ linea);
						//longlinea = linea.length();
						//log.info("forward: longitud de linea leida : " + longlinea);

						linea = Util2.agregaLongitudInicioCadena(linea, saf.getCantBytesLongitud());	
						if(conexionCliente.escribeDataSocket(linea)){
							if(!conexionCliente.timeOutSocket()){ //si no hay timeout antes de X segundos
								data = conexionCliente.leeDataSocket(conexionCliente.leeLongitudDataSocket());
								log.info("forward: data read : " + data);
								List<String> list = Arrays.asList(saf.getP().split(data));
							    //se carga la trama
							    trama = new Trama(list, saf.getCantDatosHeader(), saf.getCar());
								if(trama.loadData()){
									if(log.isDebugEnabled())
										log.debug(trama.toString());

									//Si la comunicacion fue correcta.
									if(trama.getStatusTrama() == 0 ){
										long tmp = randStore.getFilePointer();
										log.info("forward: data sent from SAF: " + linea +". the file pointer was moved to: " + tmp);
										if(Files.creaEscribeDataArchivo(saf.getFileSeek(), String.valueOf(tmp) + saf.getCrlf(), false))
											log.info("new pointer position registered: " + tmp);
										continue;
									}else{
										log.error("forward, Communication error: " + trama.getStatusTrama());
										huboError = true;
										break;
									}
								}else{
									huboError = true;
									break;
								}
							}else{//si no responde el server, retroceder el puntero una linea y break
								huboError = true;
								break;
							}
						}else{//si no hay conexion con el Pool, retroceder el puntero una linea y break
							huboError = true;
							break;
						}
					}
					if(huboError){
						log.info("Error trying to send: "+ linea +" it will try to send later." );
					}

		    	}catch(Exception e){
		    		log.error("forward: " + e);
		    	}finally{
			    	try {
						randStore.close();
					} catch (IOException e) {
						log.error("forward: " + e);
					}
		    	}
	    	}
		}catch(Exception e){
			log.error("forward: " + e.fillInStackTrace());
		}
	}
	

	public static RandomAccessFile getRandStore() {
		return randStore;
	}	
	public ConexionCliente getConexion() {
		return conexionCliente;
	}
	public void setConexion(ConexionCliente conexion) {
		this.conexionCliente = conexion;
	}

}
