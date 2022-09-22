/**
 *
 */
package com.allc.arms.server.persistence.store;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.LinkedList;
import org.hibernate.HibernateException;

/**
 * @author gustavo Se relaciona con el esquema SAADMIN
 *
 */
public class StoreDAO {

    static Logger log = Logger.getLogger(StoreDAO.class);
    
    public List getAllStore(Session sesion) {
    	try {
            Query query = sesion.createQuery("from com.allc.arms.server.persistence.store.Store");
            List rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                return rows;
            }
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public Store getStoreByCode(Session sesion, Integer code) {
        Query query = sesion.createQuery("from com.allc.arms.server.persistence.store.Store where key = '" + code + "' ");
        Iterator iterator = query.iterate();
        while (iterator.hasNext()) {
            return (Store) iterator.next();
        }
        return null;
    }
    
    public Store getStoreById(Session sesion, Integer id) {
        Query query = sesion.createQuery("from com.allc.arms.server.persistence.store.Store where storeId = '" + id + "' ");
        Iterator iterator = query.iterate();
        while (iterator.hasNext()) {
            return (Store) iterator.next();
        }
        return null;
    }

    public List getAllActiveStore(Session session) {

        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.store.Store where status = 1 and localServer = 0");
            List rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                return rows;
            }
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public boolean hayServidorLocal(Session session, Integer tienda) {

        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.store.Store where key = '" + tienda + "' and localServer = 1");
            List rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;

    }

    public List getAllActiveStores(Session session) {

        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.store.Store where status = 1 order by storeId");
            List rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                return rows;
            }
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public List getStoreLoadOperator(Session session, Integer estado) {

        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.store.Store where EstIniLoadOpe = '" + estado + "' ");
            List rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                return rows;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public List getStoreDownloadLog(Session session, Integer estado) {
        try {
            Query query = session.createQuery("from com.allc.arms.server.persistence.store.Store where downloadLog = '" + estado + "' ");
            List rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                return rows;
            }
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return new LinkedList();
    }
    
    public List getStoresByStatus(Session sesion, Integer status){
    	Query query = sesion.createQuery("from com.allc.arms.server.persistence.store.Store where statusDownload = "+status);
    	Iterator iterator = query.iterate();
    	List list = new ArrayList();
    	while(iterator.hasNext())
    		list.add(iterator.next());
    	return list;
    }

    public void updateStore(Session session, Store tienda) throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.saveOrUpdate(tienda);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    // probar si puede recibir lista
    public void updateStoreEstado(Session session, List<Store> tiendas) throws Exception {
        /*Transaction tx = null;
		int estado;
		int desClave;
		try {
			tx = session.beginTransaction();
			for(Store tienda: tiendas) {
				estado = tienda.getEstIniLoadOpe();
				desClave = Integer.parseInt(tienda.getKey());
				Query query = session.createSQLQuery("UPDATE MN_TIENDA SET ini_opera_estado = "+ estado +" WHERE des_clave = '"+ desClave +"' ");
				log.info(query.getQueryString());
				query.executeUpdate();
			}
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			throw e;
		}*/

 /*
		private void updateStock(Integer itemId, Integer retailStoreId, Integer qty) throws SQLException {
			Statement statement = sesion.connection().createStatement();
			int countRows = statement.executeUpdate("UPDATE AS_ITM_STR SET STOCK = STOCK - " + qty + " WHERE STOCK > 0 AND ID_ITM = " + itemId + " AND ID_BSN_UN = " + retailStoreId);	
		}*/
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            for (Store tienda : tiendas) {
                session.saveOrUpdate(tienda);
            }
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
    
    public List getBusinessStores(Session sesion, Integer idStore) {
    	try {
            Query query = sesion.createQuery("from com.allc.arms.server.persistence.store.BusinessStore e where e.storeId = " + idStore );
            List rows = query.list();
            if (rows != null && !rows.isEmpty()) {
                return rows;
            }
        } catch (HibernateException e) {
            log.error(e.getMessage(), e);
        }
        return new ArrayList();
    }
    
    public void deleteBusinessStore(Session session, Integer idStore) throws Exception  {

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Query query = session.createSQLQuery("delete from saadmin.mn_negtnd mn where cod_tienda =:valor1");
            query.setParameter("valor1", idStore);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tx.rollback();
            throw e;
        }

    }
    
    public void insertBusinessStore(Session session,Integer storeId, Integer businessId) throws Exception {
 
        Transaction tx = null;
        try {
                tx = session.beginTransaction(); 
                Query query = session.createSQLQuery("INSERT INTO saadmin.mn_negtnd (cod_negocio, cod_tienda) VALUES (:valor1, :valor2)");
                query.setParameter("valor1", businessId);
                query.setParameter("valor2", storeId);
                query.executeUpdate();
                tx.commit();
        } catch (Exception e) {
                log.error(e.getMessage(), e);
                tx.rollback();
                throw e;
        }
    }
}
