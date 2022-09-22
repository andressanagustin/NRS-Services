/**
 * 
 */
package com.allc.arms.server.persistence.equipo;

import java.util.Date;

/**
 * Entidad que se relaciona con la tabla FM_EQUIPO.
 * 
 * @author gustavo
 *
 */
public class Equipo {
	private Integer idEquipo;
	private String desClave;
	private String desEquipo;
	private String ip;
	private Integer indOnline;
	private Integer indActivo;
	private Integer idTipo;
	private Integer idLocal;
	private Integer codUsuario;
	private Date fecActualizacion;
        private Integer idNegocio;
        private Integer idTipoTerminal;
	/**
	 * @return the idEquipo
	 */
	public Integer getIdEquipo() {
		return idEquipo;
	}
	/**
	 * @param idEquipo the idEquipo to set
	 */
	public void setIdEquipo(Integer idEquipo) {
		this.idEquipo = idEquipo;
	}
	/**
	 * @return the desClave
	 */
	public String getDesClave() {
		return desClave;
	}
	/**
	 * @param desClave the desClave to set
	 */
	public void setDesClave(String desClave) {
		this.desClave = desClave;
	}
	/**
	 * @return the desEquipo
	 */
	public String getDesEquipo() {
		return desEquipo;
	}
	/**
	 * @param desEquipo the desEquipo to set
	 */
	public void setDesEquipo(String desEquipo) {
		this.desEquipo = desEquipo;
	}
	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}
	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}
	/**
	 * @return the indOnline
	 */
	public Integer getIndOnline() {
		return indOnline;
	}
	/**
	 * @param indOnline the indOnline to set
	 */
	public void setIndOnline(Integer indOnline) {
		this.indOnline = indOnline;
	}
	/**
	 * @return the indActivo
	 */
	public Integer getIndActivo() {
		return indActivo;
	}
	/**
	 * @param indActivo the indActivo to set
	 */
	public void setIndActivo(Integer indActivo) {
		this.indActivo = indActivo;
	}
	/**
	 * @return the idTipo
	 */
	public Integer getIdTipo() {
		return idTipo;
	}
	/**
	 * @param idTipo the idTipo to set
	 */
	public void setIdTipo(Integer idTipo) {
		this.idTipo = idTipo;
	}
	/**
	 * @return the idLocal
	 */
	public Integer getIdLocal() {
		return idLocal;
	}
	/**
	 * @param idLocal the idLocal to set
	 */
	public void setIdLocal(Integer idLocal) {
		this.idLocal = idLocal;
	}
	/**
	 * @return the codUsuario
	 */
	public Integer getCodUsuario() {
		return codUsuario;
	}
	/**
	 * @param codUsuario the idLocal to set
	 */
	public void setCodUsuario(Integer codUsuario) {
		this.codUsuario = codUsuario;
	}
	/**
	 * @return the fecActualizacion
	 */
	public Date getFecActualizacion() {
		return fecActualizacion;
	}
	/**
	 * @param fecActualizacion the idLocal to set
	 */
	public void setFecActualizacion(Date fecActualizacion) {
		this.fecActualizacion = fecActualizacion;
	}
	/**
	 * @return the idNegocio
	 */
	public Integer getIdNegocio() {
		return idNegocio;
	}
	/**
	 * @param idNegocio the idLocal to set
	 */
	public void setIdNegocio(Integer idNegocio) {
		this.idNegocio = idNegocio;
	}
	/**
	 * @return the idTipoTerminal
	 */
	public Integer getIdTipoTerminal() {
		return idTipoTerminal;
	}
	/**
	 * @param idTipoTerminal the idLocal to set
	 */
	public void setIdTipoTerminal(Integer idTipoTerminal) {
		this.idTipoTerminal = idTipoTerminal;
	}

    @Override
    public String toString() {
        return "Equipo{" + "idEquipo=" + idEquipo + ", desClave=" + desClave + ", desEquipo=" + desEquipo + ", ip=" + ip + ", indOnline=" + indOnline + ", indActivo=" + indActivo + ", idTipo=" + idTipo + ", idLocal=" + idLocal + ", codUsuario=" + codUsuario + ", fecActualizacion=" + fecActualizacion + ", idNegocio=" + idNegocio + ", idTipoTerminal=" + idTipoTerminal + '}';
    }

}
