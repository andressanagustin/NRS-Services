/**
 * 
 */
package com.allc.arms.server.persistence.fleje;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author gustavo
 *
 */
public class FlejesDAO {
	static Logger log = Logger.getLogger(FlejesDAO.class);

	public boolean insertaArchivo(Session sesion, ArchivoSAP archivo) {
		Transaction tx = null;
		try {
			tx = sesion.beginTransaction();
			sesion.saveOrUpdate(archivo);
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				tx.rollback();
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
			return false;
		}
		return true;
	}

	public boolean insertaFleje(Session sesion, Fleje fleje) {
		Transaction tx = null;
		try {
			tx = sesion.beginTransaction();
			sesion.saveOrUpdate(fleje);
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				tx.rollback();
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
			return false;
		}
		return true;
	}
	
	public boolean insertaArchivoImp(Session sesion, ArchivoImp archivoImp) {
		Transaction tx = null;
		try {
			tx = sesion.beginTransaction();
			sesion.saveOrUpdate(archivoImp);
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try {
				tx.rollback();
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
			return false;
		}
		return true;
	}

	public boolean insertaMov(Session sesion, Integer flejeId, Integer status, String horaMov) {
		Transaction tx = null;
		try {
			tx = sesion.beginTransaction();
			Query query = sesion.createSQLQuery("INSERT INTO ARC_MOV (ID_ARCPRC, ID_ESTPRC, HOR_MOV) VALUES (:valor1, :valor2, :valor3)");
			query.setParameter("valor1", flejeId);
			query.setParameter("valor2", status);
			query.setParameter("valor3", horaMov);
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}

	public boolean existeFleje(Session sesion, String name, Integer store) {
		try {
			Query query = sesion.createQuery(" FROM com.allc.arms.server.persistence.fleje.Fleje F WHERE F.status > -1 and F.name = '" + name
					+ "' and F.store = " + store);
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}
	

    public Fleje getFleje(Session sesion, String name){
    	try {
	    	Query query = sesion.createQuery(" FROM com.allc.arms.server.persistence.fleje.Fleje F WHERE F.lote = '"+name+"'");
	    	List rows = query.list();
	    	if(rows!=null && !rows.isEmpty()){
	    		return (Fleje) rows.get(0);
	    	}
    	} catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
    	return null;
    }
    
    public List getFlejes(Session sesion, String name) {
		try {
			Query query = sesion.createQuery(" FROM com.allc.arms.server.persistence.fleje.Fleje F WHERE F.name = '"+name+"'");
	    	List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return rows;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
    
    public Integer getMinStatusPorArcsap(Session sesion, Integer idArcSap){
    	try {
    		SQLQuery query = sesion.createSQLQuery("select MIN(f.ID_ESTPRC) from ARC_PRC f where id_arcsap = "+idArcSap);
	    	List rows = query.list();
	    	if(rows!=null && !rows.isEmpty()){
	    		return Integer.valueOf(rows.get(0).toString());
	    	}
    	} catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
    	return null;
    }
    
    public boolean existeArcSAP(Session sesion, String name, Integer store) {
		try {
			Query query = sesion.createQuery(" FROM com.allc.arms.server.persistence.fleje.ArchivoSAP F WHERE F.nombreItem = '" + name
					+ "' and F.codTienda = " + store);
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}
}
