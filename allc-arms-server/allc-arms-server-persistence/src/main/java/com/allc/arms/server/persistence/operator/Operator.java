package com.allc.arms.server.persistence.operator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Operator implements Serializable {

    private static final long serialVersionUID = -4674855467888138867L;

    public Operator() {
        super();
    }
    private String name;
    private String apellidoM;
    private String apellidoP;
    private Long operadorId;
    private String optionsLevel;
    private String nameAce;
    private Date operatorBirthDate;
    private Integer status;
    private Integer subscribe;
    private String identityDocument;
    private String ipTienda;
    private String ipTiendaAnt;
    private Integer codTienda;
    private Integer codTiendaAnt;
    private Integer statusTienda;
    private Integer subscribeTienda;
    private Integer codNegocio;
    private Integer codNegocioAnt;
    private Integer grupo;
    private Integer usuario;
    private Integer nivelAut;
    private Integer nivelAutSO;
    private Long idModOpera;
    private Long idModOperaSO;
    private String iniciales;
    private Integer idReg;
    private Integer tipoModelo;
    private Integer download;
    
    /**
     * 0: op sin seg mej, 1: seg mej por op, 2: depende seg mej tienda
     */
    private Integer indSegMejorada;

    private List<AuthorizesOPC> authorizations = new ArrayList<AuthorizesOPC>();
    private List<LevelAuthorizes> levelAuthorizations = new ArrayList<LevelAuthorizes>();
    private List<OperatorStore> tiendas = new ArrayList<OperatorStore>();

    public String getIniciales() {
        return iniciales;
    }

    public void setIniciales(String iniciales) {
        this.iniciales = iniciales;
    }

    public Integer getIdReg() {
        return idReg;
    }

    public void setIdReg(Integer idReg) {
        this.idReg = idReg;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(Integer subscribe) {
        this.subscribe = subscribe;
    }

    public Long getOperadorId() {
        return operadorId;
    }

    public void setOperadorId(Long operadorId) {
        this.operadorId = operadorId;
    }

    public String getOptionsLevel() {
        return optionsLevel;
    }

    public void setOptionsLevel(String optionsLevel) {
        this.optionsLevel = optionsLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getOperatorBirthDate() {
        return operatorBirthDate;
    }

    public void setOperatorBirthDate(Date operatorBirthDate) {
        this.operatorBirthDate = operatorBirthDate;
    }

    public String getIpTienda() {
        return ipTienda;
    }

    public void setIpTienda(String ipTienda) {
        this.ipTienda = ipTienda;
    }

    public String getIpTiendaAnt() {
        return ipTiendaAnt;
    }

    public Integer getStatusTienda() {
        return statusTienda;
    }

    public void setStatusTienda(Integer status) {
        this.statusTienda = status;
    }

    public Integer getSubscribeTienda() {
        return subscribeTienda;
    }

    public void setSubscribeTienda(Integer subscribe) {
        this.subscribeTienda = subscribe;
    }

    public Long getIdModOpera() {
        return idModOpera;
    }

    public void setIdModOpera(Long idModOpera) {
        this.idModOpera = idModOpera;
    }

    public Long getIdModOperaSO() {
        return idModOperaSO;
    }

    public void setIdModOperaSO(Long idModOperaSO) {
        this.idModOperaSO = idModOperaSO;
    }

    public void setIpTiendaAnt(String ipTiendaAnt) {
        this.ipTiendaAnt = ipTiendaAnt;
    }

    public List<AuthorizesOPC> getAuthorizations() {
        for (AuthorizesOPC authorization : authorizations) {
            authorization.setIdOperador(this.operadorId);
        }
        return authorizations;
    }

    public void setAuthorizations(List<AuthorizesOPC> authorizations) {
        this.authorizations = authorizations;
    }

    public String getIdentityDocument() {
        return identityDocument;
    }

    public void setIdentityDocument(String identityDocument) {
        this.identityDocument = identityDocument.trim();
    }

    public Integer getCodTienda() {
        if (codTienda == null) {
            codTienda = 0;
        }
        return codTienda;
    }

    public void setCodTienda(Integer codTienda) {
        this.codTienda = codTienda;
    }

    public Integer getCodTiendaAnt() {
        if (codTiendaAnt == null) {
            codTiendaAnt = 0;
        }
        return codTiendaAnt;
    }

    public void setCodTiendaAnt(Integer codTiendaAnt) {
        this.codTiendaAnt = codTiendaAnt;
    }

    public Integer getCodNegocio() {
        return codNegocio;
    }

    public void setCodNegocio(Integer codNegocio) {
        this.codNegocio = codNegocio;
    }

    public Integer getCodNegocioAnt() {
        return codNegocioAnt;
    }

    public void setCodNegocioAnt(Integer codNegocioAnt) {
        this.codNegocioAnt = codNegocioAnt;
    }

    public String getApellidoM() {
        return apellidoM;
    }

    public void setApellidoM(String apellidoM) {
        this.apellidoM = apellidoM;
    }

    public String getApellidoP() {
        return apellidoP;
    }

    public void setApellidoP(String apellidoP) {
        this.apellidoP = apellidoP;
    }

    public String getNameAce() {
        return nameAce;
    }

    public void setNameAce(String nameAce) {
        this.nameAce = nameAce;
    }

    /**
     * @return the grupo
     */
    public Integer getGrupo() {
        return grupo;
    }

    /**
     * @param grupo the grupo to set
     */
    public void setGrupo(Integer grupo) {
        this.grupo = grupo;
    }

    /**
     * @return the usuario
     */
    public Integer getUsuario() {
        return usuario;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(Integer usuario) {
        this.usuario = usuario;
    }

    /**
     * @return the levelAuthorizations
     */
    public List<LevelAuthorizes> getLevelAuthorizations() {
        for (LevelAuthorizes levelAuthorization : levelAuthorizations) {
            levelAuthorization.setIdOperador(this.operadorId);
        }
        return levelAuthorizations;
    }

    /**
     * @param levelAuthorizations the levelAuthorizations to set
     */
    public void setLevelAuthorizations(List<LevelAuthorizes> levelAuthorizations) {
        this.levelAuthorizations = levelAuthorizations;
    }

    /**
     * @return the nivelAut
     */
    public Integer getNivelAut() {
        return nivelAut;
    }

    /**
     * @param nivelAut the nivelAut to set
     */
    public void setNivelAut(Integer nivelAut) {
        this.nivelAut = nivelAut;
    }

    public Integer getIndSegMejorada() {
        return indSegMejorada;
    }

    public void setIndSegMejorada(Integer indSegMejorada) {
        this.indSegMejorada = indSegMejorada;
    }

    public List<OperatorStore> getTiendas() {
        for (OperatorStore tienda : tiendas) {
            tienda.setOperadorId(this.operadorId);
        }
        return tiendas;
    }

    public void setTiendas(List<OperatorStore> tiendas) {
        this.tiendas = tiendas;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Operator [operadorId=");
        builder.append(operadorId);
        builder.append(", OptionsLevel=");
        builder.append(optionsLevel);
        builder.append(", Name=");
        builder.append(name);
        builder.append(", operatorBirthDate=");
        builder.append(operatorBirthDate);
        builder.append(", status=");
        builder.append(status);
        builder.append(", subscribe=");
        builder.append(subscribe);
        builder.append(", identityDocument=");
        builder.append(identityDocument);
        builder.append(", tienda=");
        builder.append(ipTienda);
        builder.append(", tiendaAnt=");
        builder.append(ipTiendaAnt);
        builder.append(", authorizations=");
        builder.append(authorizations);
        builder.append(", download=");
		builder.append(download);
        builder.append(", tiendas=");
        builder.append(tiendas);
        builder.append("]");
        return builder.toString();
    }

	public Integer getNivelAutSO() {
		return nivelAutSO;
	}

	public void setNivelAutSO(Integer nivelAutSO) {
		this.nivelAutSO = nivelAutSO;
	}

	public Integer getTipoModelo() {
		return tipoModelo;
	}

	public void setTipoModelo(Integer tipoModelo) {
		this.tipoModelo = tipoModelo;
	}
	
	public Integer getDownload() {
		return download;
	}
	
	public void setDownload(Integer download) {
		this.download = download;
	}

}
