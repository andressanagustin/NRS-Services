package com.allc.saf;



import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.apache.log4j.Logger;



import com.allc.conexion.ConexionCliente;
import com.allc.files.Files;
import com.allc.main.Main;

/**
 * Clase utilizada como Manager para realizar los procesos SAF: Store, Clean, Forward
 * @author Alexander Padilla
 *
 */
public class SafProcess {
	static Logger log = Logger.getLogger(SafProcess.class);
	
	
	 
	/**
	 * Metodo encargado en registrar las tramas en el archivo store
	 * @param nombreArchivo		Nombre del archivo donde registrara la informacion
	 * @param data				Data a registrarse en el archivo.
	 */
    public static synchronized void stored(Saf saf, String data){
    	PrintWriter myOutWriter = null;
		try {
			//myOutWriter = new PrintWriter( new BufferedWriter(new FileWriter(nombreArchivo, true))); //si uso buffered entonces no graba inmediatamente sino que lo maneja JAVA 
			myOutWriter = new PrintWriter( new FileWriter(saf.getFileStore(), true));
			myOutWriter.print(data + saf.getCrlf());
			myOutWriter.flush();
			log.info("stored: " + data );
		}catch ( Exception ex ){
			log.error("stored: " + ex);
		}finally {
			myOutWriter.close();
		}
    }
	    
	/**
	 * Metodo que se encarga de limpiar eliminar la data la enviada en el archivo store
	 * Coloca el archivo seek en cero.
	 */
	public static void cleaner(Saf saf){
		long punteroFile;
		File ArchivoStore = null;
		//RandomAccessFile randStore = null;
		String linea = null;
		ArrayList<String> arrDatos = new ArrayList<String>(1); 
		RandomAccessFile randStore = null;
		try{
			StoreAndForward storeAndForward = new StoreAndForward(null, saf);
			/**Si no existe el archivo seek lo crea con 0**/
			if(!Files.fileExists(saf.getFileSeek())){
				Files.creaEscribeDataArchivo(saf.getFileSeek(), String.valueOf(0) + saf.getCrlf(), false);
			}
    		punteroFile = storeAndForward.obtieneOffsetSAF(saf.getFileSeek());

			if(punteroFile > 0){
	    		ArchivoStore = new File (saf.getFileStore());
	    		randStore = new RandomAccessFile(ArchivoStore,"rw");
				randStore.seek(punteroFile);
				//leemos la data no enviada.
				while((linea=randStore.readLine())!=null){
					arrDatos.add(linea);
				}
				randStore.close();
				//ArchivoStore.delete();
				//sobreescribimos el archivo 

				try{
					Files.creaEscribeDataArchivo(saf.getFileStore(), String.valueOf(""), false);
				}catch(Exception e){
					log.info("cleaner: " + e);
				}

				//sobreescribimos el archivo 
				for(int i =0; i<arrDatos.size();i++) {
					//System.out.println((String)arrDatos.get(i)); 
					Files.adicionaDataArchivo(saf.getFileStore(), (String)arrDatos.get(i)+ saf.getCrlf());
				}
				//creamos el archivo seek en cero.
				Files.creaEscribeDataArchivo(saf.getFileSeek(), String.valueOf(0) + saf.getCrlf(), false);
				/**Indicamos a la clase que realiza la limpieza del SAF que puede continuar con el proceso**/
				
			}
			Main.setLimpio(true);		
		}catch(Exception e){
			log.error("cleaner: " + e);
		}
		

	}
		
	
	/**
	 * Se encarga de realizar el proceso de envio de la data registrada al servidor central
	 */
	private static void forward(ConexionCliente cnxion, Saf saf) {
		//ConexionCliente cnxion = null;
		boolean finSAF = false;
		try {
			
			
			StoreAndForward storeAndForward = new StoreAndForward(cnxion, saf);
			
			while(!finSAF){
				try {
					Thread.sleep(saf.getTimeSAF());
					if(storeAndForward.permiteEnvioSAF()){
						if(cnxion.ConectaSocket())
							log.info(Thread.currentThread().getName() + ": Connected.");
						else{
							log.info(Thread.currentThread().getName() + ": Not Connected.");
							continue;
						}
						//si hay data se envia.
						storeAndForward.forwardSAF();
						cnxion.closeConexion();
					}
					
				} catch (Exception e) {
					log.error("forward: " + e);
					cnxion.closeConexion();
				}
			}
		} catch (Exception e) {
			log.error("forward: " + e);
		}finally{
			cnxion.closeConexion();
		}
	}

	
	/**
	 * Proceso que se encarga de realizar el envio de la informacion.
	 * @param name			nombre del thread
	 * @param conx			Clase que contiene los atributos para la conexion como cliente socket
	 * @param saf			Clase que contiene los atributos para el SAF
	 */
	public static void forward(Saf saf){
		ConexionCliente cnxion = null;
		try {
			/**Instanciamos a la clase conexion para conectarnos como cliente**/
			cnxion = new ConexionCliente( saf.getConexion().getIp(), saf.getConexion().getPuerto(), saf.getConexion().getReintentos(), saf.getConexion().getTimeOutConexion(), saf.getConexion().getTimeOutSleep(), saf.getConexion().getCantidadBytesLongitud(), null);  
			/**Se llama al metodo que realizara el envio en SAF**/
			forward(cnxion, saf);
		} catch (Exception e) {
			log.error("forwardProcess: " + e.fillInStackTrace());
		}

	}
	
}
