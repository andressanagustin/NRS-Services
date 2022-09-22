/**
 * 
 */
package com.allc.arms.server.persistence.fleje;

import java.io.Serializable;

/**
 * Entidad que representa a un archivo con flejes a imprimir (tabla PRC_FLJ_EC.ARC_ITEMS).
 * 
 * @author gustavo
 *
 */
public class ArchivoImp implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer idArchivoImp;
	private Integer flejesId;
	private Integer estado;
	private String archivo;
	/**
	 * @return the idArchivoImp
	 */
	public Integer getIdArchivoImp() {
		return idArchivoImp;
	}
	/**
	 * @param idArchivoImp the idArchivoImp to set
	 */
	public void setIdArchivoImp(Integer idArchivoImp) {
		this.idArchivoImp = idArchivoImp;
	}
	/**
	 * @return the flejesId
	 */
	public Integer getFlejesId() {
		return flejesId;
	}
	/**
	 * @param flejesId the flejesId to set
	 */
	public void setFlejesId(Integer flejesId) {
		this.flejesId = flejesId;
	}
	/**
	 * @return the estado
	 */
	public Integer getEstado() {
		return estado;
	}
	/**
	 * @param estado the estado to set
	 */
	public void setEstado(Integer estado) {
		this.estado = estado;
	}
	/**
	 * @return the archivo
	 */
	public String getArchivo() {
		return archivo;
	}
	/**
	 * @param archivo the archivo to set
	 */
	public void setArchivo(String archivo) {
		this.archivo = archivo;
	}
	
	
}
