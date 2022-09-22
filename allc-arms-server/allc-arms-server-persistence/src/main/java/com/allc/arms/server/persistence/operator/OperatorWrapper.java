package com.allc.arms.server.persistence.operator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OperatorWrapper implements Serializable {

	private static final long serialVersionUID = -8730503664076826667L;
	/**
	 * 
	 */

	private String operatorId;
	private String Name;
	private String password;
	private Date operatorBirthDate;
	private String ipStore;
	private String status;
	private String subscribe;
	private String identityDocument;
	private String nivelAut;
	private String nivelAutSO;
	private Integer tipoModelo;
	private List<StringBuffer> indicats = new ArrayList<StringBuffer>();
	private String levelAuthorizations;
	private Integer indSegMejorada;

	public String getIdentityDocument() {
		return identityDocument;
	}

	public void setIdentityDocument(String identityDocument) {
		this.identityDocument = identityDocument;
	}

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

        public String getNivelAutSO() {
            return nivelAutSO;
        }

        public void setNivelAutSO(String nivelAutSO) {
            this.nivelAutSO = nivelAutSO;
        }

	/**
	 * @param nivelAut the nivelAut to set
	 */
	public void setNivelAut(String nivelAut) {
		this.nivelAut = nivelAut;
	}

	public List<StringBuffer> getIndicats() {
		return indicats;
	}

	public void setIndicats(List<StringBuffer> indicats) {
		this.indicats = indicats;
	}

	/**
	 * @return the levelAuthorizations
	 */
	public String getLevelAuthorizations() {
		return levelAuthorizations;
	}

	/**
	 * @param levelAuthorizations
	 *            the levelAuthorizations to set
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OperatorWrapper [operatorId=");
		builder.append(operatorId);
		builder.append(", password=");
		builder.append(password);
		builder.append(", Name=");
		builder.append(Name);
		builder.append(", operatorBirthDate=");
		builder.append(operatorBirthDate);
		builder.append(", ipStore=");
		builder.append(ipStore);
		builder.append(", status=");
		builder.append(status);
		builder.append(", subscribe=");
		builder.append(subscribe);
		builder.append(", identityDocument=");
		builder.append(identityDocument);
		builder.append(", indicats=");
		builder.append(indicats);
		builder.append("]");
		return builder.toString();
	}

	public Integer getTipoModelo() {
		return tipoModelo;
	}

	public void setTipoModelo(Integer tipoModelo) {
		this.tipoModelo = tipoModelo;
	}
}
