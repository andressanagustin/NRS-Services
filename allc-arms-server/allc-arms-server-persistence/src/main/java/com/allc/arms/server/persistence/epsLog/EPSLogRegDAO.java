package com.allc.arms.server.persistence.epsLog;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;


public class EPSLogRegDAO {
	
	private static Logger log = Logger.getLogger(EPSLogRegDAO.class);
	
	public boolean insertaEPSLogReg(Session sesion, EPSLogReg epsLogReg) {
		Transaction tx = null;
		try {
			tx = sesion.beginTransaction();
			sesion.saveOrUpdate(epsLogReg);
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
