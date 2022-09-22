package com.allc.arms.server.persistence.supervisor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.arms.server.persistence.operator.Operator;


public class SupervisorDAO {
	
	static Logger log = Logger.getLogger(SupervisorDAO.class);
	
	 public List getSupOperatorsByStatus(Session sesion, Integer status){
	    	Query query = sesion.createQuery("from com.allc.arms.server.persistence.supervisor.Supervisor where estado = '"+status+"' ");
	    	Iterator iterator = query.iterate();
	    	List list = new ArrayList();
	    	while(iterator.hasNext())
	    		list.add(iterator.next());
	    	return list;
	    }
	 
	 public void updateOperatorSupervisor(Session session, Supervisor supervisor) throws Exception {
			Transaction tx = null;
			try{
				tx = session.beginTransaction();
				session.save(supervisor); 
	            tx.commit(); 
	        } catch (Exception e) { 
	        	tx.rollback();
	        	throw e; 
	        }  
		}
	 
	 public List getSupOperatorsByOperador(Session sesion, String operadorID){
	    	Query query = sesion.createQuery("from com.allc.arms.server.persistence.supervisor.Supervisor S where S.operador.operadorId = '"+operadorID+"' and S.estado < 5");
	    	Iterator iterator = query.iterate();
	    	List list = new ArrayList();
	    	while(iterator.hasNext())
	    		list.add(iterator.next());
	    	return list;
	    }

}
