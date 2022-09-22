package com.allc.arms.server.persistence.store.horas;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.persistence.operator.Operator;
import com.allc.arms.server.persistence.store.Store;


public class StoreTimeDAO {
	static Logger log = Logger.getLogger(StoreTimeDAO.class);
	
	public List<StoreTime> getStoreByCode(Session sesion, Integer idLocal, String fecha) {
		log.info("QUERY: " + "from com.allc.arms.server.persistence.store.horas.StoreTime where idLocal = " + idLocal + " AND '" + fecha + "' BETWEEN startDate AND endDate order by startDate desc");
		try {
	        Query query = sesion.createQuery("from com.allc.arms.server.persistence.store.horas.StoreTime where idLocal = '" + idLocal + "' AND '" + fecha + "' BETWEEN startDate AND endDate order by startDate desc");
	        List<StoreTime> rows = query.list();
	        return (rows != null && !rows.isEmpty())? rows : null;
		} catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
	
	public void updateStoreTime(Session session, StoreTime storeTime) throws Exception {
		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			session.save(storeTime); 
            tx.commit(); 
        } catch (Exception e) { 
        	tx.rollback();
        	throw e; 
        }  
	}
}
