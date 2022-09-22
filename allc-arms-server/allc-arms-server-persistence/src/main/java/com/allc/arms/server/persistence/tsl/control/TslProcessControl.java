/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.persistence.tsl.control;

import java.io.Serializable;

/**
 *
 * @author Tyrone Lopez
 */
public class TslProcessControl implements Serializable {

    private static final long serialVersionUID = -32946534715229891L;

    private Integer idControlTls;
    private Integer idTienda;
    private Integer tslSeekBit;
    private Integer fileBit;
    private String fileName;
    private String tslSeekFileName;

    public Integer getIdControlTls() {
        return idControlTls;
    }

    public void setIdControlTls(Integer idControlTls) {
        this.idControlTls = idControlTls;
    }

    public Integer getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(Integer idTienda) {
        this.idTienda = idTienda;
    }

    public Integer getTslSeekBit() {
        return tslSeekBit;
    }

    public void setTslSeekBit(Integer tslSeekBit) {
        this.tslSeekBit = tslSeekBit;
    }

    public Integer getFileBit() {
        return fileBit;
    }

    public void setFileBit(Integer fileBit) {
        this.fileBit = fileBit;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTslSeekFileName() {
        return tslSeekFileName;
    }

    public void setTslSeekFileName(String tslSeekFileName) {
        this.tslSeekFileName = tslSeekFileName;
    }

}
