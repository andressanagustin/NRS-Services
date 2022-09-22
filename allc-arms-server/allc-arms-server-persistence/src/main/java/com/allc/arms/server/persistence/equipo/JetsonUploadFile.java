/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.persistence.equipo;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Tyrone Lopez
 */
public class JetsonUploadFile implements Serializable {

    private static final long serialVersionUID = -7711987546797448L;

    public JetsonUploadFile() {
        super();
    }

    private Integer idFileEquipo;

    private Date fecha;

    private Integer codigoEqu;

    private String urlArchivosEquipoOrigenEnt;

    private Integer estado;

    public Integer getIdFileEquipo() {
        return idFileEquipo;
    }

    public void setIdFileEquipo(Integer idFileEquipo) {
        this.idFileEquipo = idFileEquipo;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Integer getCodigoEqu() {
        return codigoEqu;
    }

    public void setCodigoEqu(Integer codigoEqu) {
        this.codigoEqu = codigoEqu;
    }

    public String getUrlArchivosEquipoOrigenEnt() {
        return urlArchivosEquipoOrigenEnt;
    }

    public void setUrlArchivosEquipoOrigenEnt(String urlArchivosEquipoOrigenEnt) {
        this.urlArchivosEquipoOrigenEnt = urlArchivosEquipoOrigenEnt;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

}
