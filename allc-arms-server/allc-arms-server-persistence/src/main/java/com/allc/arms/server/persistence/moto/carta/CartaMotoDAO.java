package com.allc.arms.server.persistence.moto.carta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.persistence.supervisor.Supervisor;

public class CartaMotoDAO {
	
	static Logger log = Logger.getLogger(CartaMotoDAO.class);
	
	 public List getCartasMotosToSend(Session sesion, Integer estado){
	    	Query query = sesion.createQuery("from com.allc.arms.server.persistence.moto.carta.CartaMoto where estado = '"+estado+"' ");
	    	Iterator iterator = query.iterate();
	    	List list = new ArrayList();
	    	while(iterator.hasNext())
	    		list.add(iterator.next());
	    	return list;
	    }
	 
	 public void updateCartaMoto(Session session, CartaMoto cartaMoto) throws Exception {
			Transaction tx = null;
			try{
				tx = session.beginTransaction();
				session.save(cartaMoto); 
	            tx.commit(); 
	        } catch (Exception e) { 
	        	tx.rollback();
	        	throw e; 
	        }  
		}

}
