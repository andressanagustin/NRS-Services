/**
 * 
 */
package com.allc.arms.server.persistence.devolucion;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.entities.CedRuc;

/**
 * @author GUSTAVOK
 *
 */
public class DevolucionDAO {
	private Logger log = Logger.getLogger(DevolucionDAO.class);

	public boolean existeDevolucion(Session sesion, String trxID) {
		try {
			SQLQuery query = sesion.createSQLQuery("SELECT * FROM DV_TICKET WHERE ID_TRN = " + trxID);
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}
	
	public Giftcard getGiftcard(Session sesion, Integer id){
    	Query query = sesion.createQuery("from com.allc.arms.server.persistence.devolucion.Giftcard where id = "+id+" ");
    	Iterator iterator = query.iterate();
    	if(iterator.hasNext())
    		return (Giftcard) iterator.next();
    	return null;
    }
	
	public Giftcard getGiftcardByCardNumber(Session sesion, String cardNumber){
    	Query query = sesion.createQuery("from com.allc.arms.server.persistence.devolucion.Giftcard where cardNumber = "+cardNumber+" order by id desc");
    	Iterator iterator = query.iterate();
    	if(iterator.hasNext())
    		return (Giftcard) iterator.next();
    	return null;
    }

	public boolean insertGiftcard(Session sesion, Giftcard giftcard) {
		Transaction tx = null;
		try {
			tx = sesion.beginTransaction();
			sesion.saveOrUpdate(giftcard);
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}
	
	public boolean insertIlimitada(Session sesion, Ilimitada ilimitada) {
		Transaction tx = null;
		try {
			tx = sesion.beginTransaction();
			sesion.saveOrUpdate(ilimitada);
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}
	
	public boolean insertPagoCer(Session sesion, PagoCer pagoCer) {
		Transaction tx = null;
		try {
			tx = sesion.beginTransaction();
			sesion.saveOrUpdate(pagoCer);
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}
}
