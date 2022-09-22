/*
 *  ***************************************************************
 * NUO - 2020
 * Creado por:      Ing. Joel Jalon Gomez 
 * Motivo:          representacion json de respuesta servicio alertas tec
 * Fecha Creacion:  2020-11-26
 *  ***************************************************************
 */
package com.allc.arms.server.operations.colas;

public class DsError {
	private Integer codigo;
	private String mensaje;
	public Integer getCodigo() {
		return codigo;
	}
	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
	}
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
	
	

}
