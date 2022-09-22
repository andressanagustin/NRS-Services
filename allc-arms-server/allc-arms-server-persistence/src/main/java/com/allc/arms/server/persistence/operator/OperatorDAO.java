/**
 * 
 */
package com.allc.arms.server.persistence.operator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;



/**
 * @author GUSTAVOK
 *
 */
public class OperatorDAO {
	
	static Logger log = Logger.getLogger(OperatorDAO.class);
	
    public List getOperatorsByStatus(Session sesion, Integer status){
    	log.info("from com.allc.arms.server.persistence.operator.Operator where status = " + status + " and download=1");
    	Query query = sesion.createQuery("from com.allc.arms.server.persistence.operator.Operator where status = "+status+ " and download=1");
    	Iterator iterator = query.iterate();
    	List list = new ArrayList();
    	while(iterator.hasNext())
    		list.add(iterator.next());
    	return list;
    }
       
    public Operator getOperatorsByIdentityDocument(Session sesion, String identityDocument) throws Exception{ 
    	try {
    		Query query = sesion.createQuery("from com.allc.arms.server.persistence.operator.Operator where identityDocument = '"+identityDocument+"' ");
	    	List rows = query.list();
	    	return (rows != null && !rows.isEmpty())? (Operator)rows.get(0) : null;
	    } catch (Exception e) {
	    	log.error(e.getMessage(), e);
	    	throw e;
	    }
    }
    
    public boolean getOperatorsByIdentityDocumentAndStore(Session sesion, long operatorId, int codTienda, int tipoModelo) throws Exception{ 
    	try {
    		Query query = sesion.createQuery("from com.allc.arms.server.persistence.operator.OperatorStore where operadorId = "+operatorId+" AND codTienda = " + codTienda+" AND tipoModelo = " + tipoModelo);
	    	List rows = query.list();
	    	return rows!=null && !rows.isEmpty();
	    } catch (Exception e) {
	    	log.error(e.getMessage(), e);
	    	throw e;
	    }
    }
    
    public OperatorStore getOperatorStoreByCodStore(Session sesion, long operatorId, int codTienda, int tipoModelo) throws Exception{  	
    	try {
    		Query query = sesion.createQuery("from com.allc.arms.server.persistence.operator.OperatorStore where operadorId = "+operatorId+" AND codTienda = " + codTienda+" AND tipoModelo = " + tipoModelo);
	    	List<OperatorStore> rows = query.list();
	    	return (rows != null && !rows.isEmpty())? rows.get(0) : null;
	    } catch (Exception e) {
	    	log.error(e.getMessage(), e);
	    	throw e;
	    }
    }
    
    public boolean getOperatorStoreToProcesses(Session sesion, long operatorId) throws Exception{
    	try {
    		Query query = sesion.createQuery("from com.allc.arms.server.persistence.operator.OperatorStore where operadorId = "+operatorId+" AND (str_estado=1 OR str_estado=4 OR str_estado=5)");
			List rows = query.list();
	    	return rows!=null && !rows.isEmpty();
	    } catch (Exception e) {
	    	log.error(e.getMessage(), e);
	    	throw e;
	    }
    }
    
    public boolean getOperatorStoreToDownload(Session sesion, long operatorId, int download) throws Exception{
    	try {
    		Query query = sesion.createQuery("from com.allc.arms.server.persistence.operator.OperatorStore where operadorId = "+operatorId+" AND (str_estado=1 OR str_estado=4 OR str_estado=5) AND download=" + download);
			List rows = query.list();
	    	return rows!=null && !rows.isEmpty();
	    } catch (Exception e) {
	    	log.error(e.getMessage(), e);
	    	throw e;
	    }
    }
    
	public IndicatOPC getIndicatOPCById(Session sesion, int idIndicat,int bitPos) throws Exception {
		try {
    		Query query = sesion.createQuery("from com.allc.arms.server.persistence.operator.IndicatOPC where indicat.idIndicat = '"+idIndicat+"' and bitPos= '"+ bitPos +"' ");
	    	List<IndicatOPC> rows = query.list();
	    	return (rows != null && !rows.isEmpty())? rows.get(0) : null;
	    } catch (Exception e) {
	    	log.error(e.getMessage(), e);
	    	throw e;
	    }
	}
	
	public IndicatOPC getIndicatOPCByIdOPC(Session sesion, int idIndicatOPC) throws Exception {
		try {
    		Query query = sesion.createQuery("from com.allc.arms.server.persistence.operator.IndicatOPC where idIndicatOPC = '"+idIndicatOPC+"' ");
	    	List<IndicatOPC> rows = query.list();
	    	return (rows != null && !rows.isEmpty())? rows.get(0) : null;
	    } catch (Exception e) {
	    	log.error(e.getMessage(), e);
	    	throw e;
	    }
	}
	
	  
	public void updateOperator(Session session, Operator operator) throws Exception {
		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			session.save(operator); 
            tx.commit(); 
        } catch (Exception e) { 
        	tx.rollback();
        	throw e; 
        }  
	}
	
	public void updateOperatorStore(Session session, OperatorStore operadorTienda) throws Exception {
		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			session.save(operadorTienda); 
            tx.commit(); 
        } catch (Exception e) { 
        	tx.rollback();
        	throw e; 
        }  
	}
	
	
	public List getGerentesByStore(Session sesion, String tienda){
	    	try {
		    	Query query = sesion.createQuery("from com.allc.arms.server.persistence.operator.Operator  WHERE idModOpera = 10 and codTienda = '"+tienda+"' and subscribe=1");
		    	List rows = query.list();
		    	if(rows!=null && !rows.isEmpty()){
		    		return rows;
		    	}
	    	} catch (Exception e) {
	        	log.error(e.getMessage(), e);
	        }
	    	return null;
    }

	public List getOperatorsByOperatorStore(Session sesion, Integer tienda){
    	log.info("from com.allc.arms.server.persistence.operator.Operator where status != 3 and operadorId in (select operadorId from com.allc.arms.server.persistence.operator.OperatorStore where codTienda = " + tienda + ")");
    	Query query = sesion.createQuery("from com.allc.arms.server.persistence.operator.Operator where status != 3 and operadorId in (select operadorId from com.allc.arms.server.persistence.operator.OperatorStore where codTienda = " + tienda + ")");
    	Iterator iterator = query.iterate();
    	List list = new ArrayList();
    	while(iterator.hasNext())
    		list.add(iterator.next());
    	return list;
    }
	
}
