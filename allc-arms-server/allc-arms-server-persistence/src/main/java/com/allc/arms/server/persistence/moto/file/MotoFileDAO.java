package com.allc.arms.server.persistence.moto.file;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;


public class MotoFileDAO {
	
	static Logger log = Logger.getLogger(MotoFileDAO.class);
	
	public boolean existeMotoFile(Session sesion, String name) {
		try {
			Query query = sesion.createQuery(" FROM com.allc.arms.server.persistence.moto.file.MotoFile F "
					+ "WHERE F.motoFileName = '" + name + "'");
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}
	
	public boolean insertaMotoFile(Session sesion, MotoFile motoFile) {
		Transaction tx = null;
		try {
			tx = sesion.beginTransaction();
			sesion.saveOrUpdate(motoFile);
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
