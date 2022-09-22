package com.allc.arms.server.persistence.store;

import com.allc.entities.RetailStore;
import java.util.Iterator;

import org.hibernate.Query;
import org.hibernate.Session;


import com.allc.entities.Workstation;
import org.apache.log4j.Logger;
import org.hibernate.Transaction;

/**
 * @author maxip
 * Se relaciona con la tabla PA_STR_RTL de ARTS
 *
 */
public class RetailStoreDAO {
	
        static Logger log = Logger.getLogger(RetailStoreDAO.class);
    
        
        public com.allc.entities.RetailStore getRetailStoreByCode(Session sesion, Integer codigo){
            Query query = sesion.createQuery("from com.allc.entities.RetailStore where code = '"+codigo+"' ");
            Iterator iterator = query.iterate();
            while(iterator.hasNext())
    		return (com.allc.entities.RetailStore) iterator.next();
    	return null;
    }
	
	public Workstation getRetailTerminalByStoreCodeTerminalCode(Session session, String terminalCode, Integer storeCode) {
		Query query = session.createQuery("from com.allc.entities.Workstation W where W.code = '"+terminalCode+"' and W.store.retailStoreID= "+storeCode );
		Iterator iterator = query.iterate();
		while (iterator.hasNext())
			return (Workstation) iterator.next();
		return null;
					 
	}
        
        public void updateRetailStore(Session session, RetailStore retailStore) throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.saveOrUpdate(retailStore);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
}
