package com.allc.arms.server.persistence.reserva;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.allc.entities.Reserva;
import com.allc.entities.RetailStore;

public class ReservaDAO {
	
	static Logger log = Logger.getLogger(ReservaDAO.class);
	
	public Reserva getReservaByNumRsvAndNumSerie(Session sesion, Integer numReserva){
		
		Query query = sesion.createQuery(" FROM com.allc.entities.Reserva  WHERE codReserva = " + numReserva);
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (Reserva) list.get(0);
		return null;
	}
	
	public RetailStore getRetailStoreByCode(Session sesion, Integer retailStoreCode) {
		
		Query query = sesion.createQuery("from com.allc.entities.RetailStore where code = '" + retailStoreCode + "' ");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (RetailStore) list.get(0);
		
		return null;
	}
	
	public boolean insertReserva(Session sesion, Reserva reserva) {
		Transaction tx = null;
		try {
			tx = sesion.beginTransaction();
			sesion.saveOrUpdate(reserva);
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
