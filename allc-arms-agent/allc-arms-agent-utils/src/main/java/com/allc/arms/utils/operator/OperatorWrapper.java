package com.allc.arms.utils.operator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OperatorWrapper implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8730503664076826667L;
    /**
     *
     */

    private String operatorId;
    private String password;
    private String Name;
    private Date operatorBirthDate;
    private String ipStore;
    private String status;
    private String subscribe;
    private String identityDocument;
    private String nivelAut;
    private String nivelAutSO;
    private Integer tipoModelo;
    private List indicats = new ArrayList();
    private String levelAuthorizations;
    private Integer indSegMejorada;

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Date getOperatorBirthDate() {
        return operatorBirthDate;
    }

    public void setOperatorBirthDate(Date operatorBirthDate) {
        this.operatorBirthDate = operatorBirthDate;
    }

    public String getIpStore() {
        return ipStore;
    }

    public void setIpStore(String ipStore) {
        this.ipStore = ipStore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(String subscribe) {
        this.subscribe = subscribe;
    }

    /**
     * @return the nivelAut
     */
    public String getNivelAut() {
        return nivelAut;
    }

    /**
     * @param nivelAut the nivelAut to set
     */
    public void setNivelAut(String nivelAut) {
        this.nivelAut = nivelAut;
    }

    public String getNivelAutSO() {
        return nivelAutSO;
    }

    public void setNivelAutSO(String nivelAutSO) {
        this.nivelAutSO = nivelAutSO;
    }

    public List getIndicats() {
        return indicats;
    }

    public void setIndicats(List indicats) {
        this.indicats = indicats;
    }

    public String getIdentityDocument() {
        return identityDocument;
    }

    public void setIdentityDocument(String identityDocument) {
        this.identityDocument = identityDocument;
    }

    /**
     * @return the levelAuthorizations
     */
    public String getLevelAuthorizations() {
        return levelAuthorizations;
    }

    /**
     * @param levelAuthorizations the levelAuthorizations to set
     */
    public void setLevelAuthorizations(String levelAuthorizations) {
        this.levelAuthorizations = levelAuthorizations;
    }

    public Integer getIndSegMejorada() {
        return indSegMejorada;
    }

    public void setIndSegMejorada(Integer indSegMejorada) {
        this.indSegMejorada = indSegMejorada;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("OperatorWrapper [operatorId=");
        buffer.append(operatorId);
        buffer.append(", password=");
        buffer.append(password);
        buffer.append(", Name=");
        buffer.append(Name);
        buffer.append(", operatorBirthDate=");
        buffer.append(operatorBirthDate);
        buffer.append(", ipStore=");
        buffer.append(ipStore);
        buffer.append(", status=");
        buffer.append(status);
        buffer.append(", subscribe=");
        buffer.append(subscribe);
        buffer.append(", identityDocument=");
        buffer.append(identityDocument);
        buffer.append(", indicats=");
        buffer.append(indicats);
        buffer.append("]");
        return buffer.toString();
    }

	public Integer getTipoModelo() {
		return tipoModelo;
	}

	public void setTipoModelo(Integer tipoModelo) {
		this.tipoModelo = tipoModelo;
	}

}
