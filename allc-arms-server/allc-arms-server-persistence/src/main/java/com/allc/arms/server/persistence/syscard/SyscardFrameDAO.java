/**
 * 
 */
package com.allc.arms.server.persistence.syscard;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author GUSTAVOK
 *
 */
public class SyscardFrameDAO {
	private Logger log = Logger.getLogger(SyscardFrameDAO.class);

	public boolean insertSyscardFrame(Session sesion, SyscardFrame syscardFrame) {
		Transaction tx = null;
		try {
			tx = sesion.beginTransaction();
			sesion.saveOrUpdate(syscardFrame);
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}
	
}
