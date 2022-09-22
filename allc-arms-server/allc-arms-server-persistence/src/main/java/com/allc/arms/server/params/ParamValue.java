/**
 * 
 */
package com.allc.arms.server.params;

import java.math.BigInteger;

/**
 * Entidad que representa a un registro de la tabla PM_PARVAL.
 * 
 * @author gustavo
 *
 */
public class ParamValue {

	protected Long id;
	protected Param param;
	protected String valor;
	protected Integer estado;
	protected Integer tienda;
	protected BigInteger nivelGrupo;
	
	

	public BigInteger getNivelGrupo() {
		return nivelGrupo;
	}

	public void setNivelGrupo(BigInteger nivelGrupo) {
		this.nivelGrupo = nivelGrupo;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the param
	 */
	public Param getParam() {
		return param;
	}

	/**
	 * @param param the param to set
	 */
	public void setParam(Param param) {
		this.param = param;
	}

	/**
	 * @return the valor
	 */
	public String getValor() {
		return valor;
	}

	/**
	 * @param valor
	 *            the valor to set
	 */
	public void setValor(String valor) {
		this.valor = valor;
	}

	/**
	 * @return the estado
	 */
	public Integer getEstado() {
		return estado;
	}

	/**
	 * @param estado
	 *            the estado to set
	 */
	public void setEstado(Integer estado) {
		this.estado = estado;
	}

	/**
	 * @return the tienda
	 */
	public Integer getTienda() {
		return tienda;
	}

	/**
	 * @param tienda
	 *            the tienda to set
	 */
	public void setTienda(Integer tienda) {
		this.tienda = tienda;
	}

}
