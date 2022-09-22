package com.allc.os4690.pipe;

import org.apache.log4j.Logger;

import com.ibm.OS4690.FlexosException;
import com.ibm.OS4690.POSFile;
import com.ibm.OS4690.POSPipeInputStream;
import com.ibm.OS4690.POSPipeOutputStream;


public class Pi2DAO {
	static Logger log = Logger.getLogger(Pi2DAO.class);
	Pi2DTO pi2DTO;
	
	
	public Pi2DAO(Pi2DTO pi2dto) {
		super();
		pi2DTO = pi2dto;
	}
	
	public Pi2DAO() {
		super();
	}

	public Pi2DTO getPi2DTO() {
		return pi2DTO;
	}

	public void setPi2DTO(Pi2DTO pi2dto) {
		pi2DTO = pi2dto;
	}

	/**Open pipe for read
	 * @return
	 */
	public boolean openPi2Read(){
		boolean b_exito = false;
		try {
			this.getPi2DTO().setPosFile(new POSFile (this.getPi2DTO().getName(), "r", POSFile.SHARED_READ_ACCESS));
			b_exito = true;
		} catch (Exception e) {
			b_exito = false;
			log.error("openPi2Read: " + e);
		}
		return b_exito;

	}
	/**
	 * close pipe opened for read
	 * @return
	 */
	public boolean closePi2Read(){
		boolean b_exito = false;
		try {
			this.getPi2DTO().getPosFile().closePartial();
			b_exito = true;
		} catch (Exception e) {
			log.error("closePi2Read: " + e);
		}
		return b_exito;	
	}

	/**
	 * Read data from pipe ( la cantidad de bytes a leer lo toma del DTO getCantidadBytesLeer que se paso en el constructor)
	 * @param pipeContents   Array of bytes containing the read data
	 * @return
	 */
	public int readPipe(byte[] pipeContents){
		int totBytes = 0;
		try {
			if(this.getPi2DTO().getNumBytesToRead() == 0){
				log.info("Number of bytes to read from the pipe " + this.getPi2DTO().getName() + " must be greater than zero");
				totBytes = -1;
			}else{
				totBytes = this.getPi2DTO().getPosFile().read(pipeContents, 0, POSFile.FROM_START_OF_FILE, POSFile.READ_FROM_DISK, this.getPi2DTO().getNumBytesToRead());
			}
		} catch (FlexosException e) {
			log.error("readPipe: " + e);
		}
		return totBytes;
	}
	

	/**
	 * Create pipe PI2 to obtain the data
	 */
	public boolean createPi2Read(){
		boolean b_exito = false;
		try {
			if(this.getPi2DTO().getSizePipeToCreate()==0){
				log.info("Must indicate the size of pipe " + this.getPi2DTO().getName() + " to be create");
				b_exito = false;
			}else{
				this.getPi2DTO().setPosPipeInputStream(new POSPipeInputStream(this.getPi2DTO().getName(), this.getPi2DTO().getSizePipeToCreate()));
				b_exito = true;
			}
		} catch (Exception e) {
			b_exito = false;
			log.error("createPi2Read: " + e);
		}
		return b_exito;
		
	}
	/**
	 * Close pipe PI2 created to receive data
	 * @return
	 */
	public boolean closePi2Created(){
		boolean b_exito = false;
		try {
			this.getPi2DTO().getPosPipeInputStream().close();
			b_exito = true;
		} catch (Exception e) {
			b_exito = false;
			log.error("closePi2Created: " + e);
		}
		return b_exito;		
	}
	
	/**
	 * Open pipe PI2 in write mode
	 * @return
	 */
	public boolean openPi2Write(){
		boolean b_exito = false;
		try {
			this.getPi2DTO().setPosPipeOutputStream(new POSPipeOutputStream(this.getPi2DTO().getName()));
			b_exito = true;
		} catch (Exception e) {
			b_exito = false;
			log.error("openPi2Write: " + e);
		}
		return b_exito;

	}
	/**
	 * Close pipe PI2 opened in write mode
	 * @return
	 */
	public boolean closePi2Write(){
		boolean b_exito = false;
		try {
			this.getPi2DTO().getPosPipeOutputStream().close();
			b_exito = true;
		} catch (Exception e) {
			log.error("closePi2Write: " + e);
		}
		return b_exito;	
	}
	
	public boolean write(byte[] pipeContents){
		boolean b_exito = false;
		try {		
			this.getPi2DTO().getPosPipeOutputStream().write(pipeContents);
			b_exito = true;
		} catch (FlexosException e) {
			log.error("write: " + e);
		}
		return b_exito;	
	}
		
}
