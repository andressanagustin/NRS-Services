/**
 * 
 */
package com.allc.arms.server.persistence.cedpadruc;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.entities.CedRegElec;
import com.allc.entities.CedRuc;
import com.allc.entities.Extranjero;
import com.allc.entities.Padron;

/**
 * @author gustavo
 *
 */
public class CedPadRucDAO {
	static Logger log = Logger.getLogger(CedPadRucDAO.class);

    public boolean insertCedRuc(Session sesion, CedRuc cedRuc){
    	Transaction tx = null;
    	try{
    		tx = sesion.beginTransaction();
    		sesion.saveOrUpdate(cedRuc); 
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
    
    public boolean insertPadron(Session sesion, Padron padron){
    	Transaction tx = null;
    	try{
    		tx = sesion.beginTransaction();
    		sesion.saveOrUpdate(padron); 
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
    
    public boolean insertCedRegElec(Session sesion, CedRegElec cedRegElec){
    	Transaction tx = null;
    	try{
    		tx = sesion.beginTransaction();
    		sesion.saveOrUpdate(cedRegElec); 
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
    
    public CedRuc getCedRucById(Session sesion, String id){
    	Query query = sesion.createQuery("from com.allc.entities.CedRuc where id = "+id+" ");
    	Iterator iterator = query.iterate();
    	if(iterator.hasNext())
    		return (CedRuc) iterator.next();
    	return null;
    }

    public CedRuc getCedRucByCode(Session sesion, String codigo){
    	Query query = sesion.createQuery("from com.allc.entities.CedRuc where codigo = '"+codigo+"' ");
    	Iterator iterator = query.iterate();
    	if(iterator.hasNext())
    		return (CedRuc) iterator.next();
    	return null;
    }
    
    public Padron getPadronById(Session sesion, String codigo){
    	Query query = sesion.createQuery("from com.allc.entities.Padron where codigo = '"+codigo+"' ");
    	Iterator iterator = query.iterate();
    	if(iterator.hasNext())
    		return (Padron) iterator.next();
    	return null;
    }
    public Extranjero getExtranjeroById(Session sesion, String codigo){
    	Query query = sesion.createQuery("from com.allc.entities.Extranjero where codigo = '"+codigo+"' ");
    	Iterator iterator = query.iterate();
    	if(iterator.hasNext())
    		return (Extranjero) iterator.next();
    	return null;
    }
}
