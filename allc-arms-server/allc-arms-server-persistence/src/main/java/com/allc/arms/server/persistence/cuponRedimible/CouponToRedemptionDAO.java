/**
 * 
 */
package com.allc.arms.server.persistence.cuponRedimible;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.entities.CouponToRedemption;

/**
 * @author gustavo
 *
 */
public class CouponToRedemptionDAO {
	static Logger log = Logger.getLogger(CouponToRedemptionDAO.class);

	public CouponToRedemption getCouponById(Session sesion, String couponCode) {
		Query query = sesion.createQuery("from com.allc.entities.CouponToRedemption where couponCode = '" + couponCode + "' ");
		Iterator iterator = query.iterate();
		if (iterator.hasNext())
			return (CouponToRedemption) iterator.next();
		return null;
	}
	
    public boolean insertCoupon(Session sesion, CouponToRedemption couponToRedemption){
    	Transaction tx = null;
    	try{
    		tx = sesion.beginTransaction();
    		sesion.saveOrUpdate(couponToRedemption); 
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
}
