/**
 * 
 */
package com.allc.arms.server.persistence.cierre;

import java.util.Date;
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
public class CierreDAO {
	private Logger log = Logger.getLogger(CierreDAO.class);

    public Date getLastCloseDate(Session sesion){
    	try {
    		SQLQuery query = sesion.createSQLQuery("SELECT TOP 1 TR_TRN.DC_DY_BSN FROM TR_TRN, TR_BSN_EOD WHERE TR_TRN.ID_TRN=TR_BSN_EOD.ID_TRN ORDER BY TR_TRN.DC_DY_BSN DESC");
	    	List rows = query.list();
	    	if(rows != null && !rows.isEmpty()){
	    		return (Date) rows.get(0);
	    	}
    	} catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
    	return null;
    }
    
    public Integer getLastCloseTrxID(Session sesion, Integer codTienda){
    	try {
    		SQLQuery query = sesion.createSQLQuery("SELECT TOP 1 TR_TRN.ID_TRN FROM TR_TRN, TR_BSN_EOD, PA_STR_RTL WHERE TR_TRN.ID_TRN=TR_BSN_EOD.ID_TRN AND TR_TRN.ID_BSN_UN=PA_STR_RTL.ID_BSN_UN AND PA_STR_RTL.CD_STR_RT = '"+codTienda+"' ORDER BY TR_TRN.DC_DY_BSN DESC");
	    	List rows = query.list();
	    	if(rows != null && !rows.isEmpty()){
	    		return (Integer) rows.get(0);
	    	}
    	} catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
    	return null;
    }
    
	public boolean updateEndDate(Session sesion, Date fecha, Integer idTrn) {
		Transaction tx = null;
		try {
			tx = sesion.beginTransaction();
			Query query = sesion.createSQLQuery("UPDATE TR_TRN SET TS_TRN_END = :valor1 WHERE ID_TRN='"+idTrn+"'");
			query.setParameter("valor1", fecha);
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}

}
