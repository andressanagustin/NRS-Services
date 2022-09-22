package com.allc.os4690.pipe;
import org.apache.log4j.Logger;

import com.ibm.OS4690.POSFile;
import com.ibm.OS4690.POSPipeInputStream;
import com.ibm.OS4690.POSPipeOutputStream;


public class Pi2DTO {
	static Logger log = Logger.getLogger(Pi2DTO.class);
	
	/**Attribute name of pipe**/
	private String name;
	
	/**Atributo utilizado cuando se desea leer un pipe pi2 que ya esta creado**/
	private POSFile posFile;	
	private int	  numBytesToRead = 0;
	
	/**Atributo utilizado cuando se desea escribir a un pipe pi2 que ya esta creado**/
	private POSPipeOutputStream posPipeOutputStream;
	
	/**Atributos usados cuando se crea un pipe pi2**/
	private POSPipeInputStream posPipeInputStream;
	private int sizePipeToCreate = 0;
	
	
	
	public int getSizePipeToCreate() {
		return sizePipeToCreate;
	}
	public void setSizePipeCreated(int sizePipeCreated) {
		this.sizePipeToCreate = sizePipeCreated;
	}
	public POSPipeInputStream getPosPipeInputStream() {
		return posPipeInputStream;
	}
	public void setPosPipeInputStream(POSPipeInputStream posPipeInputStream) {
		this.posPipeInputStream = posPipeInputStream;
	}
	public POSPipeOutputStream getPosPipeOutputStream() {
		return posPipeOutputStream;
	}
	public POSFile getPosFile() {
		return posFile;
	}
	public void setPosFile(POSFile posFile) {
		this.posFile = posFile;
	}
	public String getName() {
		return name;
	}
	public int getNumBytesToRead() {
		return numBytesToRead;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setNumBytesToRead(int numBytesToRead) {
		this.numBytesToRead = numBytesToRead;
	}

	public void setPosPipeOutputStream(POSPipeOutputStream posPipeOutputStream) {
		this.posPipeOutputStream = posPipeOutputStream;
	}
	
	public String toString() {
		return "Pi2DTO ["
				+ (name != null ? "name=" + name + ", " : "")
				+ "numBytesToRead="
				+ numBytesToRead
				+ ", "
				+ (posFile != null ? "pipe=" + posFile + ", " : "")
				+ (posPipeOutputStream != null ? "posPipeOutputStream="
						+ posPipeOutputStream + ", " : "")
				+ (posPipeInputStream != null ? "posPipeInputStream="
						+ posPipeInputStream + ", " : "") + "sizePipeToCreate="
				+ sizePipeToCreate + "]";
	}
	

	

}
