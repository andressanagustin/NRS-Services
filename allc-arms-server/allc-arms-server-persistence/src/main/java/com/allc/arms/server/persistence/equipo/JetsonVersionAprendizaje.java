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
public class JetsonVersionAprendizaje implements Serializable {

    private static final long serialVersionUID = -7712345776032597448L;

    public JetsonVersionAprendizaje() {
        super();
    }

    private Long codigoVap;

    private String urlArchivosVap;

    private double versionVap;

    private Date fechaCreacionVap;

    private Date fechaEdicionVap;

    private Long codigoEnt;

    public Long getCodigoVap() {
        return codigoVap;
    }

    public void setCodigoVap(Long codigoVap) {
        this.codigoVap = codigoVap;
    }

    public String getUrlArchivosVap() {
        return urlArchivosVap;
    }

    public void setUrlArchivosVap(String urlArchivosVap) {
        this.urlArchivosVap = urlArchivosVap;
    }

    public double getVersionVap() {
        return versionVap;
    }

    public void setVersionVap(double versionVap) {
        this.versionVap = versionVap;
    }

    public Date getFechaCreacionVap() {
        return fechaCreacionVap;
    }

    public void setFechaCreacionVap(Date fechaCreacionVap) {
        this.fechaCreacionVap = fechaCreacionVap;
    }

    public Date getFechaEdicionVap() {
        return fechaEdicionVap;
    }

    public void setFechaEdicionVap(Date fechaEdicionVap) {
        this.fechaEdicionVap = fechaEdicionVap;
    }

    public Long getCodigoEnt() {
        return codigoEnt;
    }

    public void setCodigoEnt(Long codigoEnt) {
        this.codigoEnt = codigoEnt;
    }

}
