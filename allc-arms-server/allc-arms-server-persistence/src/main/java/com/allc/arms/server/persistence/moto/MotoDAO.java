/**
 * 
 */
package com.allc.arms.server.persistence.moto;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author gustavo
 *
 */
public class MotoDAO {
	static Logger log = Logger.getLogger(MotoDAO.class);

	public boolean insertMoto(Session sesion, Moto moto) {
		Transaction tx = null;
		try {
			tx = sesion.beginTransaction();
			sesion.saveOrUpdate(moto);
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

	public boolean motoVendida(Session sesion, String serialNum) {
		try {
			SQLQuery query = sesion
					.createSQLQuery("SELECT M.* FROM TR_LTM_MOTO_DT M, TR_TRN T WHERE M.ID_TRN = T.ID_TRN and T.FL_CNCL = 0 and T.FL_VD = 0 and M.FL_VD = 0 and M.FL_RV = 0 and M.SRL_NBR = '" + serialNum + "'");
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	public Moto getMotoByNumSerie(Session sesion, String numSerie) {
		Query query = sesion.createQuery("from com.allc.arms.server.persistence.moto.Moto where serie = '" + numSerie + "'");
		Iterator iterator = query.iterate();
		if (iterator.hasNext())
			return (Moto) iterator.next();
		return null;
	}

	public boolean existeMotoByNumSerie(Session sesion, String numSerie) {
		try {
			Query query = sesion
					.createQuery("from com.allc.arms.server.persistence.moto.Moto where serie = '" + numSerie + "'");
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}
}
