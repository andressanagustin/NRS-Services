package com.allc.arms.server.persistence.store;

import java.io.Serializable;
import java.util.Date;

public class Store implements Serializable {

    private static final long serialVersionUID = -5594059270915229891L;
    /**
     *
     */

    private Integer storeId;
    private String name;
    private Integer key;
    private String address;
    private Integer regionCode;
    private Integer cityCode;
    private String ip;
    private Integer status;
    private Integer idReg;
    private Date lastUpdate;
    private Integer codCanton;
    private Integer codProvincia;
    private String rucTienda;
    private String codSri;
    private Integer localServer;
    private String paDivision;
    private String paCiudad;
    private String latitud;
    private String longitud;
    private Integer idBsnUnGp;
    private String ipWsNotifSuperv;
    private Integer EstIniLoadOpe;
    private Integer downloadLog;
    private String desClaveDownload;
    private String pgSync;
    private Date FecEstIniLoadOpe;
    private float progressIniLoadOpe;
    private Integer statusDownload;
    private Integer download;
            
    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the regionCode
     */
    public Integer getRegionCode() {
        return regionCode;
    }

    /**
     * @param regionCode the regionCode to set
     */
    public void setRegionCode(Integer regionCode) {
        this.regionCode = regionCode;
    }

    public Integer getCityCode() {
        return cityCode;
    }

    public void setCityCode(Integer cityCode) {
        this.cityCode = cityCode;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Integer getLocalServer() {
        return localServer;
    }

    public void setLocalServer(Integer localServer) {
        this.localServer = localServer;
    }

    public Integer getEstIniLoadOpe() {
        return EstIniLoadOpe;
    }

    public void setEstIniLoadOpe(Integer estIniLoadOpe) {
        EstIniLoadOpe = estIniLoadOpe;
    }

    public Date getFecEstIniLoadOpe() {
        return FecEstIniLoadOpe;
    }

    public void setFecEstIniLoadOpe(Date fecEstIniLoadOpe) {
        FecEstIniLoadOpe = fecEstIniLoadOpe;
    }

    public Integer getDownloadLog() {
        return downloadLog;
    }

    public void setDownloadLog(Integer downloadLog) {
        this.downloadLog = downloadLog;
    }

    public String getDesClaveDownload() {
        return desClaveDownload;
    }

    public void setDesClaveDownload(String desClaveDownload) {
        this.desClaveDownload = desClaveDownload;
    }
    
    public float getProgressIniLoadOpe() {
    	return progressIniLoadOpe;
    }
    
    public void setProgressIniLoadOpe(float progressIniLoadOpe) {
    	this.progressIniLoadOpe = progressIniLoadOpe;
    }

    public Integer getStatusDownload() {
        return statusDownload;
    }

    public void setStatusDownload(Integer statusDownload) {
        this.statusDownload = statusDownload;
    }

    public Integer getDownload() {
        return download;
    }

    public void setDownload(Integer download) {
        this.download = download;
    }

    public Integer getIdReg() {
        return idReg;
    }

    public void setIdReg(Integer idReg) {
        this.idReg = idReg;
    }

    public Integer getCodCanton() {
        return codCanton;
    }

    public void setCodCanton(Integer codCanton) {
        this.codCanton = codCanton;
    }

    public Integer getCodProvincia() {
        return codProvincia;
    }

    public void setCodProvincia(Integer codProvincia) {
        this.codProvincia = codProvincia;
    }

    public String getRucTienda() {
        return rucTienda;
    }

    public void setRucTienda(String rucTienda) {
        this.rucTienda = rucTienda;
    }

    public String getCodSri() {
        return codSri;
    }

    public void setCodSri(String codSri) {
        this.codSri = codSri;
    }

    public String getPaDivision() {
        return paDivision;
    }

    public void setPaDivision(String paDivision) {
        this.paDivision = paDivision;
    }

    public String getPaCiudad() {
        return paCiudad;
    }

    public void setPaCiudad(String paCiudad) {
        this.paCiudad = paCiudad;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public Integer getIdBsnUnGp() {
        return idBsnUnGp;
    }

    public void setIdBsnUnGp(Integer idBsnUnGp) {
        this.idBsnUnGp = idBsnUnGp;
    }

    public String getIpWsNotifSuperv() {
        return ipWsNotifSuperv;
    }

    public void setIpWsNotifSuperv(String ipWsNotifSuperv) {
        this.ipWsNotifSuperv = ipWsNotifSuperv;
    }

    public String getPgSync() {
        return pgSync;
    }

    public void setPgSync(String pgSync) {
        this.pgSync = pgSync;
    }

    @Override
    public String toString() {
        return "Store{" + "storeId=" + storeId + ", name=" + name + ", key=" + key + ", address=" + address + ", regionCode=" + regionCode + ", cityCode=" + cityCode + ", ip=" + ip + ", status=" + status + ", idReg=" + idReg + ", lastUpdate=" + lastUpdate + ", codCanton=" + codCanton + ", codProvincia=" + codProvincia + ", rucTienda=" + rucTienda + ", codSri=" + codSri + ", localServer=" + localServer + ", paDivision=" + paDivision + ", paCiudad=" + paCiudad + ", latitud=" + latitud + ", longitud=" + longitud + ", idBsnUnGp=" + idBsnUnGp + ", ipWsNotifSuperv=" + ipWsNotifSuperv + ", EstIniLoadOpe=" + EstIniLoadOpe + ", downloadLog=" + downloadLog + ", desClaveDownload=" + desClaveDownload + ", pgSync=" + pgSync + ", FecEstIniLoadOpe=" + FecEstIniLoadOpe + ", progressIniLoadOpe=" + progressIniLoadOpe + ", statusDownload=" + statusDownload + ", download=" + download + '}';
    }
    
}
