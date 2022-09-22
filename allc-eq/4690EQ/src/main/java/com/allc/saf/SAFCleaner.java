package com.allc.saf;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

import com.allc.files.Files;
import com.allc.util.Util2;
import com.ibm.OS4690.RandomAccessFile4690;


public class SAFCleaner {

	static Logger log = Logger.getLogger(SAFCleaner.class);

	/**
	 * Metodo que se encarga de limpiar eliminar la data la enviada en el archivo store
	 * Coloca el archivo seek en cero.
	 */
	public boolean cleaner(SAF saf){
		boolean result = false;
		long punteroSeek;
		long punteroStore;
		String fileNameTemporary = "M:/STS/4690EQ/tempor.tmp";
		String valorEnCero = Util2.rpad(String.valueOf(0), " ", 20) + saf.getCrlf();
		try{
			//crea el archivo store solo si no existe.
			if(!Files.fileExists4690(saf.getFileStore())){
				Files.creaArchivo4690(saf.getFileStore());
				//POSFile posFileStoreWriter = new POSFile(saf.getFileStore(), "rw", POSFile.SHARED_READ_WRITE_ACCESS, 50);
				//posFileStoreWriter.closeFull();
			}

			/**Si no existe el archivo seek lo crea con 0 ( no existe data anteriormente enviada la cual debamos limpiar )
			 * se indica que siga el proceso**/
			if(!Files.fileExists4690(saf.getFileSeek())){
				Files.creaEscribeDataArchivo4690(saf.getFileSeek(), valorEnCero, false);
/*				POSFile posFileSeekWriter = new POSFile(saf.getFileSeek(), "rw", POSFile.SHARED_READ_WRITE_ACCESS, 50);
				posFileSeekWriter.write(valorEnCero.getBytes(), 0, POSFile.FROM_START_OF_FILE, POSFile.FLUSH, valorEnCero.length());
				posFileSeekWriter.closeFull();*/
				result = true;
			}else{
				//obtenemos la cantidad de bytes que ya se envio del archvio store
	    		punteroSeek = obtieneOffsetSAF(saf.getFileSeek());
	    		//si no se ha enviado nada, no hay nada que limpiar
	    		if(punteroSeek == 0){
	    			log.info("there is nothing to clean");
	    			result = true;
	    		}else if(punteroSeek > 0){ //hay data que limpiar
	    			//obtenemos el tama�o del archivo store
		    		punteroStore = getFileLength(saf.getFileStore());
	    			//si se ha enviado todo el archivo
	    			if(punteroStore == punteroSeek){
	    				log.info("All SAFFileStore was sent, cleaning all file");
	    				// se borra todo el store
	    				Files.deleteArchivo4690(saf.getFileStore());
	    				// creamos el archivo seek en cero. 
	    				if(Files.creaEscribeDataArchivo4690(saf.getFileSeek(), valorEnCero , false))
							/**Indicamos a la clase que realiza la limpieza del SAF que puede continuar con el proceso**/
	    					result = true;
					//si hay data que aun no se ha enviado
	    			}else if (punteroStore > punteroSeek){ //Aqui se realiza el swap de la informacion
	    				log.info("FileStore Size: " + punteroStore + " FileSeek: " + punteroSeek + " removing " + (punteroStore - punteroSeek) + " bytes");
			    		//renombramos el archivo con la data con un nombre temporal
						if(rename(saf.getFileStore(), fileNameTemporary))
							//copiamos solo la data aun no enviada del archivo temporal al archivo store
							if(copy(fileNameTemporary, saf.getFileStore(), punteroSeek))
								//creamos el archivo seek en cero, tener en cuenta que se graba una cadena de largo 20 con espacios por la derecha
								if(Files.creaEscribeDataArchivo4690(saf.getFileSeek(), valorEnCero , false)){
									//borramos el archivo temporal
									Files.deleteArchivo4690(fileNameTemporary);
									/**Indicamos a la clase que realiza la limpieza del SAF que puede continuar con el proceso**/
									result = true;
								}
					}
		    	}
	    		
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}
		return result;
	}
	/**
	 * get The length of the file
	 * @param fileName		Name of the file
	 * @return		the length of the file
	 */
	private long getFileLength(String fileName){
		RandomAccessFile4690 raf = null ;
		long size = -1;
		try {
			raf = new RandomAccessFile4690( fileName, "r" ) ;
			size = raf.length();
			raf.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				raf.close();
			} catch (Exception e2) {}
		}
		return size;
	}
	
	/**
	 * Rename a file
	 * @param fileNameSource  	Name of the file source
	 * @param fileNameDes		Name of the file destiny
	 * @return
	 */
	private boolean rename(String fileNameSource, String fileNameDes ){
		boolean result = false;
		File fileSource = null;
		File fineDestiny = null;
		try {
			fileSource = new File(fileNameSource);
			fineDestiny= new File(fileNameDes);
			
			if(fileSource.renameTo(fineDestiny)){
				result = true;
			}
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}
	
	/**
	 * Copy information from the fileNameSource into the fileNameDest, starting at the pointer position
	 * @param fileNameSource	Name of the file source
	 * @param fileNameDest		Name of the file destiny
	 * @param pointer			position to start to copy the information
	 * @return					true if the information was copy, otherwise return false
	 */
	private boolean copy(String fileNameSource, String fileNameDest, long pointer){
		boolean result = false;
		RandomAccessFile in = null ;
		RandomAccessFile out = null;
		try {
		    in = new RandomAccessFile ( fileNameSource, "r" ) ; 
		    out= new RandomAccessFile ( fileNameDest, "rw" ) ;  

		    long begin = pointer;
		    long end=in.length();                                                                                                                                                               
		    
		    int buf_sz=1024*1024;                                                                                                                                                 
		    
		    byte buf [  ]  = new byte [ buf_sz ] ;
		    int cp_len;
		    while  ( begin < end )   {                                                                                                                                                            
		         cp_len = (int)(end - begin);                                                                                                                                                     
		         if  ( cp_len > buf_sz )  cp_len=buf_sz;                                                                                                                                         

		         in.seek ( begin ) ;                                                                                                                                                            
		         in.readFully ( buf,0,cp_len ) ; // read cp_len bytes                                           
		         out.write ( buf,0,cp_len ) ;                                                                                                                                                  
		         begin+=cp_len;                                                                                                                                                              
		    }                                                                                                                                                                            
		    in.close (  ) ;                                                                                                                                                                               
		    out.close (  ) ;  
		    result = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				in.close();
			} catch (Exception e2) {}
			try {
				out.close();
			} catch (Exception e2) {}
		}
		return result;
	}

    /**
     * Obtiene la posicion del archivo store, desde la que se debe de enviar la informacion.
     * @param nombreArchivo		Nombre del archivo seek
     * @return					La posicion de envio
     * @throws IOException		Si el archivo seek no contiene un numero.
     */
	private long obtieneOffsetSAF(String nombreArchivo) {
		long punteroFile;
		String data;
		try {
			data = Files.readLineOfFile4690(nombreArchivo);
			if(null == data)
				punteroFile = 0;
			else
				try {
					punteroFile = Long.parseLong(data.replaceAll(" ", ""));
				} catch (Exception e) {
					log.error("obtieneOffset: the file " + nombreArchivo + " not contain a number as a pointer. ", e);
					punteroFile = -1;
				}		
		}catch ( Exception e ){
			log.error(e.getMessage(), e);
			punteroFile = -1;
		}
		return punteroFile;
	}	
}
