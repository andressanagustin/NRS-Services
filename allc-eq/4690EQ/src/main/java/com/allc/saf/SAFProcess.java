package com.allc.saf;



import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import com.allc.conexion.ConexionCliente;
import com.allc.files.Files;
import com.allc.main.constants.Constants;
import com.allc.util.Util2;
import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileOutputStream4690;
import com.ibm.OS4690.POSFile;
import com.ibm.OS4690.RandomAccessFile4690;

/**
 * Clase utilizada como Manager para realizar los procesos SAF: Store, Clean, Forward
 * @author Alexander Padilla
 *
 */
public class SAFProcess {
	static Logger log = Logger.getLogger(SAFProcess.class);
	static RandomAccessFile4690 randStoreRead = null;
	static FileOutputStream4690 writerFileStore = null;
	static RandomAccessFile4690 randSeekRead = null;
	static POSFile posFileSeekWriter = null;
	//static String valorEnCero = Util2.rpad(String.valueOf(0), " ", 20);
	static boolean finSAFForward = false;
	static boolean finForwardProcess = false;
	
	public static boolean init(SAF saf){
		boolean result = false;
		try {
			File4690 file = new File4690(saf.getFileStore());
    		if(!file.exists())
    			file.createNewFile();
			writerFileStore = new FileOutputStream4690(saf.getFileStore(), true);
			randStoreRead = new RandomAccessFile4690(saf.getFileStore(),"r");
			randSeekRead = new RandomAccessFile4690(saf.getFileSeek(),"r");
			posFileSeekWriter = new POSFile(saf.getFileSeek(), "rw", POSFile.SHARED_READ_WRITE_ACCESS);
			result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}
	
	public static boolean closure(){
		boolean result = false;
		try {
			log.info("Finalizando SAF process.");
			/**First of all stop to send the info**/
			finSAFForward = true;
			/**wait until the process finish**/
			while(!finForwardProcess)
				Thread.sleep(500);
			/**close the files**/
			writerFileStore.close();
			randStoreRead.close();
			randSeekRead.close();
			posFileSeekWriter.closeFull();
			result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}	
	 
	/**
	 * Metodo encargado en registrar las tramas en el archivo store
	 * @param data		data a registrar en el archivo
	 * @param eol		fin de linea 
	 */
    public static synchronized void stored(String data, String eol){
		try {
			String cadena = data + eol;
			writerFileStore.write(cadena.getBytes(), 0, cadena.length());
			writerFileStore.flush();
			if(log.isDebugEnabled())
			   log.debug("stored: " + data );
		}catch ( Exception e ){
			log.error(e.getMessage(), e);
		}
    }
	
	/**
	 * Se encarga de realizar el proceso de envio de la data registrada al servidor central
	 */
	private static void forward(ConexionCliente cnxion, SAF saf, Hashtable hash) {
		try {
			
			while(!finSAFForward){
				try {
					log.info("Procesando forward");
					Thread.sleep(saf.getTimeSAF());
					if(permiteEnvioSAF(saf)){
						if(cnxion.ConectaSocket()){
							log.info(Thread.currentThread().getName() + ": Connected.");
							if(log.isDebugEnabled())
								log.debug(Thread.currentThread().getName() + ": Connected.");
							//si hay data se envia.
							forwardSAF(cnxion, saf, hash);
							cnxion.closeConexion();						
						}else{
							//log.info(Thread.currentThread().getName() + ": Not Connected.");
							if(log.isDebugEnabled())
								log.debug(Thread.currentThread().getName() + ": Not connected.");
							continue;
						}

					}
					
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					cnxion.closeConexion();
				}
			}
			log.info("SAF Forward finished");
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}finally{
			cnxion.closeConexion();
		}
		/**to indicate the end of the process**/
		finForwardProcess = true;
	}

	
	/**
	 * Proceso que se encarga de realizar el envio de la informacion.
	 * @param name			nombre del thread
	 * @param conx			Clase que contiene los atributos para la conexion como cliente socket
	 * @param saf			Clase que contiene los atributos para el SAF
	 */
	public static void forward(SAF saf, Hashtable hash){
		ConexionCliente cnxion = null;
		try {
			log.info(saf.toString());
			/**Instanciamos a la clase conexion para conectarnos como cliente**/
			cnxion = new ConexionCliente( saf.getConexion().getIp(), saf.getConexion().getPuerto(), saf.getConexion().getReintentos(), saf.getConexion().getTimeOutConexion(), saf.getConexion().getTimeOutSleep(), saf.getConexion().getCantidadBytesLongitud(), null);
			log.info(cnxion.toString());
			/**Se llama al metodo que realizara el envio en SAF**/
			forward(cnxion, saf, hash);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}
	
    /**
     * Metodo que indica si se debe de ejecutar el envio de la data. 
     * Valida si existen los archivos seek y store
     * Valida que el archivo seek contenga un numero valido
     * @return		True: se debe de enviar la data
     * 				False: No existe data para enviar o no se puede enviar por que fallaron las validaciones
     */
    private static boolean permiteEnvioSAF(SAF saf){
    	long puntero;

    	String linea;
    	try{
    		if(!Files.fileExists4690(saf.getFileSeek())) return false;
    		if(!Files.fileExists4690(saf.getFileStore())) return false;
    		puntero = obtieneOffsetSAF(saf.getFileSeek());
    		if(puntero >= 0){
    			try{
					randStoreRead.seek(puntero);//Seek to end of file
					linea=randStoreRead.readLine();
					if(linea!=null){
						return true;
					}
    			}catch ( Exception e ){
    				log.error(e.getMessage(), e);
    			}
    		}
    	}catch ( Exception e ){
    		log.error(e.getMessage(), e);
		}
    	return false;
    }
    
    /**
     * Obtiene la posicion del archivo store, desde la que se debe de enviar la informacion.
     * @param nombreFileSeek		Nombre del archivo seek
     * @return					La posicion de envio
     * @throws IOException		Si el archivo seek no contiene un numero.
     */
	private static long obtieneOffsetSAF(String nombreFileSeek) {
		long punteroFile;
		String data;
		try {
			data = randSeekRead.readLine();
			randSeekRead.seek(0);
			if(null == data)
				punteroFile = 0;
			else
				try {
					punteroFile = Long.parseLong(data.replaceAll(" ", ""));
				} catch (Exception e) {
					log.error("obtieneOffset: the file " + nombreFileSeek + " not contain a number as a pointer. " ,  e);
					punteroFile = -1;
				}
		}catch ( Exception e ){
			log.error(e.getMessage(), e);
			punteroFile = -1;
		}
		return punteroFile;
	}	
	

	/**
	 * Metodo que se encarga de enviar la data guardada en el archivo store.
	 * Utiliza el archivo seek para tomar la posicion de envio en el archivo store
	 */
	public static void forwardSAF(ConexionCliente conexionCliente, SAF saf, Hashtable hash){
		long punteroFile=-1;
		String linea = null;
		String data;
		long tmp = 0;
		List list = null;
		int numOcurrencia = 0;
		boolean huboError = false;
		int numOfBytesToRead = 0;
		String valorPosicion = "";
		try{
	    	punteroFile = obtieneOffsetSAF(saf.getFileSeek());
	    	
	    	log.info("punteroFile: " + punteroFile);
	    	if(punteroFile >= 0){
		    	try{
					randStoreRead.seek(punteroFile);	//Seek to end of file
					while((linea=randStoreRead.readLine())!=null && !finSAFForward){
						log.info("forward: line read from " + saf.getFileStore() +": "+ linea);
						/**buscar la 11ava ocurrencia de | a partir de dicha posicion hasta el final buscar si se debe de enviar o no la data**/
						numOcurrencia = Util2.obtPosNesimaOcurrencia(linea, saf.getCar(), 11);
						if(numOcurrencia > 0){
							if(!isMessageAllowed(hash, linea.substring(numOcurrencia + 1))){
								log.info("linea descartada: " + linea);
								//avanzamos el puntero y omitimos el envio del mensaje
								tmp = randStoreRead.getFilePointer();
								valorPosicion = Util2.rpad(String.valueOf(tmp), " ", 20) + saf.getCrlf();
								posFileSeekWriter.write(valorPosicion.getBytes(), 0, POSFile.FROM_START_OF_FILE, POSFile.FLUSH, valorPosicion.length());
								log.info("file pointer new position: " + tmp);
								continue;
							}
						}
						
						linea = Util2.agregaLongitudInicioCadena(linea, saf.getCantBytesLongitud());
						if(conexionCliente.escribeDataSocket(linea)){
							//if(!conexionCliente.timeOutSocket()){ //si no hay timeout antes de X segundos
							if( null != (data = conexionCliente.timeOutSocket(conexionCliente.getCantidadBytesLongitud())) ){
								//data = conexionCliente.leeDataSocket(conexionCliente.leeLongitudDataSocket());
								try { 
									numOfBytesToRead = Integer.parseInt(data);	
								} catch (Exception e) {
									log.info("the first " + conexionCliente.getCantidadBytesLongitud() + " bytes : " + data + " is not a number");
									huboError = true;
									break;
								}
								if(null != (data = conexionCliente.timeOutSocket(numOfBytesToRead)  )){
									list = Arrays.asList(saf.getP().split(data));
									
									if(list.get(saf.getCantDatosHeader()).toString().equals(Constants.Comunicacion.CERO)){
										
										tmp = randStoreRead.getFilePointer();
										valorPosicion = Util2.rpad(String.valueOf(tmp), " ", 20) + saf.getCrlf();
										posFileSeekWriter.write(valorPosicion.getBytes(), 0, POSFile.FROM_START_OF_FILE, POSFile.FLUSH, valorPosicion.length());
										log.info("file pointer new position: " + tmp);
										continue;

									}else{
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
						log.info("Error trying to send: "+ linea +" it will try to send later" );
					}

		    	}catch(Exception e){
		    		log.error(e.getMessage(), e);
		    	}
	    	}
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}
	}
	
	private static boolean isMessageAllowed(Hashtable hash, String key){
		boolean result = true;
		try {
			if(hash.containsKey(key))
				result = false;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}	
	
}
