/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.arms.server.persistence.tsl.control;

import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Tyrone Lopez
 */
public class TslProcessControlDAO {

    static Logger log = Logger.getLogger(TslProcessControlDAO.class);

    public List<TslProcessControl> findAll(Session session) {
        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.tsl.control.TslProcessControl");
            List rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                return rows;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
     
    public TslProcessControl getProcessControlBYIdTienda(Session sesion, Integer id){
    	Query query = sesion.createQuery("from com.allc.arms.server.persistence.tsl.control.TslProcessControl where idTienda = :idTienda ");
        query.setParameter("idTienda", id);
        return (TslProcessControl) query.uniqueResult();
    }
     
    public boolean insertControl(Session sesion, TslProcessControl cedRuc){
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

}
