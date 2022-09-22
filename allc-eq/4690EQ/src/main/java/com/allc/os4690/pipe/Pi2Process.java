package com.allc.os4690.pipe;

import org.apache.log4j.Logger;



public class Pi2Process {

	static Logger log = Logger.getLogger(Pi2Process.class);
	
	/**
	 * Method to write in Pi2 open-write-close
	 * @param pi2Name		name of pipe
	 * @param data			data to write in pipe
	 * @return				true = data wrote in pipe, false = cant write in pipe
	 */
	public static boolean write(String pi2Name, String data){
		boolean b_exito = false;
		try{
			Pi2DTO pi2DTOOut = new Pi2DTO();
			pi2DTOOut.setName(pi2Name);
			
			//Instantiate from Pi2DAO class
			Pi2DAO pi2DAOOut = new Pi2DAO(pi2DTOOut);
			//open pipe
			if(pi2DAOOut.openPi2Write()){
				//write in pipe
				if(pi2DAOOut.write(data.getBytes())){
					b_exito = true;
				}else{
					log.error("write: cant write information through the pipe: " + pi2DAOOut.getPi2DTO().getName());
				}
				//Close de pi2
				if(!pi2DAOOut.closePi2Write())
					log.error("Cant close pipe: " + pi2DAOOut.getPi2DTO().getName());
			}else{
				log.error("pipe: " + pi2DAOOut.getPi2DTO().getName() + " not open.");
			}
		}catch(Exception e){
			log.error("write: " + e);
		}
		return b_exito;
	}
	/**
	 * Method to read data from pipe, open-read-close
	 * @param pipeContents			 Arreglo de bytes donde se obtendra la data leida
	 * @param pi2Name				 Pipe name
	 * @param numBytesToReadFromPipe Cantidad de bytes a leer del pipe
	 * @return						 true = leyo data del pipe, false = no leyo data del pipe
	 */
	public static boolean read(byte[] pipeContents, String pi2Name, int numBytesToReadFromPipe){
		boolean b_exito = false;
		try{	
			//Instantiate from Pi2DTO class
			Pi2DTO pi2DTOIn = new Pi2DTO();
			pi2DTOIn.setName(pi2Name);
			pi2DTOIn.setNumBytesToRead(numBytesToReadFromPipe);
			//Instantiate from pi2DTO
			Pi2DAO pi2DAOIn = new Pi2DAO(pi2DTOIn);
			//Open pi2 from read
			if(pi2DAOIn.openPi2Read()){
				if(pi2DAOIn.readPipe(pipeContents) > 0){
				   b_exito = true;
				}else{
					log.error("Cant read from pipe: " + pi2DAOIn.getPi2DTO().getName());
				}
				//close pipe pi2
				if(!pi2DAOIn.closePi2Read())
				   log.error("cant close the pipe: " + pi2DAOIn.getPi2DTO().getName());							
			}else{
				log.error("pipe: " + pi2DAOIn.getPi2DTO().getName() + " not Open.");
			}

		}catch(Exception e){
			log.error("read: " + e);
		}
	return b_exito;		
	}
	
	/**
	 * Create pipe pi2 to Read
	 * @param name	pipe name
	 * @param size	size of pipe
	 * @return	Pi2DAO class
	 */
	public static Pi2DAO createPi2ToRead(String name, int size) {
		Pi2DAO pi2 = null;
		try {
			if(validatePipeName(name)){
				/**Instantiate from class pipe DTO **/
				Pi2DTO pi2DTO = new Pi2DTO();
				/**set the pipe's name**/
				pi2DTO.setName(name);
				/**set the pipe's size**/
				pi2DTO.setSizePipeCreated(size);
				/**Instantiate from class pipe DAO **/
				pi2 = new Pi2DAO(pi2DTO);
				/**Create pi2**/
				if(pi2.createPi2Read()){
				   //log.info("pipe: " + pi2.getPi2DTO().getName() + " created to be read.");
				}else{
					log.info("pipe: " + pi2.getPi2DTO().getName() + " Not created.");
					pi2 = null;
				}
				//log.info("Listening pipe: " + pi2DTO.getName());
			}else{
				log.error("The Pipe's name " + name + " is not valid, it must be start with pi: or PI:");
			}
		}catch (Exception e) {
			pi2 = null;
			log.error("createPi2ToRead: Cannot create communication pipe: " + e);
		}
		return pi2;
	}
	
	/**
	 * Open a pi2 en write mode
	 * @param name pipe name to open in write mode
	 * @return	Pi2DAO class
	 */
	public static Pi2DAO openPi2ToWrite(String name){
		Pi2DAO pi2DAO = null;
		try {
			if(validatePipeName(name)){
				Pi2DTO pi2DTORO = new Pi2DTO();
				pi2DTORO.setName(name);
		
				/**Instantiate from Pi2DAO class**/
				pi2DAO = new Pi2DAO(pi2DTORO);
				if(pi2DAO.openPi2Write()){
					//log.info("pipe: " + pi2DAO.getPi2DTO().getName() + " opened in write mode.");
				}else{
					log.info("pipe: " + pi2DAO.getPi2DTO().getName() + " not open in write mode.");
					pi2DAO = null;
				}
			}else{
				log.error("The Pipe's name " + name + " is not valid, it must be start with pi: or PI:");
			}
		} catch (Exception e) {
			log.error("openPi2ToWrite: Error opening communication pipe " + e);
			pi2DAO = null;
		}
		return pi2DAO;
	}

	/**
	 * Open a pi2 to read
	 * @param name				pipe's name
	 * @param numBytesToRead	number of bytes to read from pipe's name
	 * @return					Pi2DAO class
	 */
	public static Pi2DAO openPi2ToRead(String name, int numBytesToRead){
		Pi2DAO pi2DAO = null;
		try {
			if(validatePipeName(name)){
				/**Instantiate from Pi2DTO class**/
				Pi2DTO pi2DTO = new Pi2DTO();
				pi2DTO.setName(name);
				pi2DTO.setNumBytesToRead(numBytesToRead);
	
				/**Instantiate from Pi2DAO class**/
				pi2DAO = new Pi2DAO(pi2DTO);
				if(pi2DAO.openPi2Read()){
					//log.info("pipe: " + pi2DAO.getPi2DTO().getName() + " opened in read mode.");
				}else{
					log.info("pipe: " + pi2DAO.getPi2DTO().getName() + " can't open.");
					pi2DAO = null;
				}
			}else{
				log.error("The Pipe's name " + name + " is not valid, it must be start with pi: or PI:");
			}
		} catch (Exception e) {
			pi2DAO = null;
			log.error("openPi2ToRead: Error opening communication pipe " + e);
		}
		return pi2DAO;
	}
	
	
	/**
	 * Create a pipe to be read by an application in Basic or C language
	 * @param name  pipe's name
	 * @param size	pipe's size
	 * @return	Pi2DAO class
	 */
	public static Pi2DAO createPi2ToBeReadByBorC(String name, int size){
		Pi2DAO pi2OpenedToWrite = null;
		try {
			if(validatePipeName(name)){
				/**Cread pi2 to read**/
				Pi2DAO pi2 = createPi2ToRead(name, size);
				/**open pi2 in write mode**/
				pi2OpenedToWrite = openPi2ToWrite(name);
				/**close pi2 created to read**/
				pi2.closePi2Created();
				//log.info("Pipe " + name + " opened to be read by Basic or C application");
			}else{
				log.error("The Pipe's name " + name + " is not valid, it must be start with pi: or PI:");
			}
		} catch (Exception e) {
			log.error("createPi2ToBeReadedByBorC: " + e);
			pi2OpenedToWrite = null;
		}
		/**Must return the pi2 opened to write in, and unblocked to Basic or C app**/
		return pi2OpenedToWrite;
	}
	
	public static boolean validatePipeName(String name) {
		    return (name.startsWith("pi:") || name.startsWith("PI:")) && (name.length() <= 11);
	}
	
}
