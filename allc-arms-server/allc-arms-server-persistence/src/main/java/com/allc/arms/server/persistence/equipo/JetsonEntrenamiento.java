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
public class JetsonEntrenamiento implements Serializable {

    private static final long serialVersionUID = -77126657832597448L;

    public JetsonEntrenamiento() {
        super();
    }

    private Integer codigoEnt;

    private String urlArchivosEquipoOrigenEnt;

    private Integer codigoEqu;

    private Date fechaCreacionEnt;

    public Integer getCodigoEnt() {
        return codigoEnt;
    }

    public void setCodigoEnt(Integer codigoEnt) {
        this.codigoEnt = codigoEnt;
    }

    public String getUrlArchivosEquipoOrigenEnt() {
        return urlArchivosEquipoOrigenEnt;
    }

    public void setUrlArchivosEquipoOrigenEnt(String urlArchivosEquipoOrigenEnt) {
        this.urlArchivosEquipoOrigenEnt = urlArchivosEquipoOrigenEnt;
    }

    public Date getFechaCreacionEnt() {
        return fechaCreacionEnt;
    }

    public void setFechaCreacionEnt(Date fechaCreacionEnt) {
        this.fechaCreacionEnt = fechaCreacionEnt;
    }

    public Integer getCodigoEqu() {
        return codigoEqu;
    }

    public void setCodigoEqu(Integer codigoEqu) {
        this.codigoEqu = codigoEqu;
    }

}
