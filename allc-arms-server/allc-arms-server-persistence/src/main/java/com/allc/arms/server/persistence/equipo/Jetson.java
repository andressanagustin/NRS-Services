package com.allc.arms.server.persistence.equipo;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

public class Jetson implements Serializable {

    private static final long serialVersionUID = -7712668096032597448L;

    public Jetson() {
        super();
    }

    private Integer codigoEqu;
    private String desEqu;
    private String macEqu;
    private String ipEqu;
    private Date fecNew;
    private Date fecUpdate;
    private Integer estadoActivo;
    private BigInteger codigoVap;
    private Date hsExtErrores;
    private Date emtEstadoDesde;
    private Date emtEstadoHasta;
    private String userFtp;
    private String passFtp;
    private Integer idEquipo;
    private Integer status;
    private Integer statusReiniciar;
    private Integer statusApagar;
    private Integer statusUpload;
    private Date fecUpload;
    private String infoEqipo;
    private Integer pingStatus;
    private Integer pingStatusUp;
    private Integer statusUploadUp;
    private String fileUploadUp;
    private Integer statusRegistrarUp;
    private Integer idLocal;

    public Integer getCodigoEqu() {
        return codigoEqu;
    }

    public void setCodigoEqu(Integer codigoEqu) {
        this.codigoEqu = codigoEqu;
    }

    public String getDesEqu() {
        return desEqu;
    }

    public void setDesEqu(String desEqu) {
        this.desEqu = desEqu;
    }

    public String getMacEqu() {
        return macEqu;
    }

    public void setMacEqu(String macEqu) {
        this.macEqu = macEqu;
    }

    public String getIpEqu() {
        return ipEqu;
    }

    public void setIpEqu(String ipEqu) {
        this.ipEqu = ipEqu;
    }

    public Date getFecNew() {
        return fecNew;
    }

    public void setFecNew(Date fecNew) {
        this.fecNew = fecNew;
    }

    public Date getFecUpdate() {
        return fecUpdate;
    }

    public void setFecUpdate(Date fecUpdate) {
        this.fecUpdate = fecUpdate;
    }

    public Integer getEstadoActivo() {
        return estadoActivo;
    }

    public void setEstadoActivo(Integer estadoActivo) {
        this.estadoActivo = estadoActivo;
    }

    public BigInteger getCodigoVap() {
        return codigoVap;
    }

    public void setCodigoVap(BigInteger codigoVap) {
        this.codigoVap = codigoVap;
    }

    public Date getHsExtErrores() {
        return hsExtErrores;
    }

    public void setHsExtErrores(Date hsExtErrores) {
        this.hsExtErrores = hsExtErrores;
    }

    public String getUserFtp() {
        return userFtp;
    }

    public void setUserFtp(String userFtp) {
        this.userFtp = userFtp;
    }

    public String getPassFtp() {
        return passFtp;
    }

    public void setPassFtp(String passFtp) {
        this.passFtp = passFtp;
    }

    public Integer getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Integer idEquipo) {
        this.idEquipo = idEquipo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getInfoEqipo() {
        return infoEqipo;
    }

    public void setInfoEqipo(String infoEqipo) {
        this.infoEqipo = infoEqipo;
    }

    public Integer getPingStatus() {
        return pingStatus;
    }

    public void setPingStatus(Integer pingStatus) {
        this.pingStatus = pingStatus;
    }

    public Integer getStatusReiniciar() {
        return statusReiniciar;
    }

    public void setStatusReiniciar(Integer statusReiniciar) {
        this.statusReiniciar = statusReiniciar;
    }

    public Integer getStatusApagar() {
        return statusApagar;
    }

    public void setStatusApagar(Integer statusApagar) {
        this.statusApagar = statusApagar;
    }

    public Integer getStatusUpload() {
        return statusUpload;
    }

    public void setStatusUpload(Integer statusUpload) {
        this.statusUpload = statusUpload;
    }

    public Date getFecUpload() {
        return fecUpload;
    }

    public void setFecUpload(Date fecUpload) {
        this.fecUpload = fecUpload;
    }

    public Date getEmtEstadoDesde() {
        return emtEstadoDesde;
    }

    public void setEmtEstadoDesde(Date emtEstadoDesde) {
        this.emtEstadoDesde = emtEstadoDesde;
    }

    public Date getEmtEstadoHasta() {
        return emtEstadoHasta;
    }

    public void setEmtEstadoHasta(Date emtEstadoHasta) {
        this.emtEstadoHasta = emtEstadoHasta;
    }

    public Integer getPingStatusUp() {
        return pingStatusUp;
    }

    public void setPingStatusUp(Integer pingStatusUp) {
        this.pingStatusUp = pingStatusUp;
    }

    public Integer getStatusUploadUp() {
        return statusUploadUp;
    }

    public void setStatusUploadUp(Integer statusUploadUp) {
        this.statusUploadUp = statusUploadUp;
    }

    public Integer getStatusRegistrarUp() {
        return statusRegistrarUp;
    }

    public void setStatusRegistrarUp(Integer statusRegistrarUp) {
        this.statusRegistrarUp = statusRegistrarUp;
    }

    public Integer getIdLocal() {
        return idLocal;
    }

    public void setIdLocal(Integer idLocal) {
        this.idLocal = idLocal;
    }

    public String getFileUploadUp() {
        return fileUploadUp;
    }

    public void setFileUploadUp(String fileUploadUp) {
        this.fileUploadUp = fileUploadUp;
    }

    @Override
    public String toString() {
        return "Jetson [codigoEqu=" + codigoEqu + ", desEqu=" + desEqu + ", macEqu=" + macEqu + ", ipEqu=" + ipEqu
                + ", fecNew=" + fecNew + ", fecUpdate=" + fecUpdate + ", estadoActivo=" + estadoActivo + ", codigoVap=" + codigoVap
                + ", hsExtErrores=" + hsExtErrores + ", userFtp=" + userFtp + ", passFtp=" + passFtp
                + ", idEquipo=" + idEquipo + ", status=" + status + "]";
    }

}
