/**
 * 
 */
package com.allc.arms.server.persistence.cer.itemBalanza;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author gustavo
 *
 */
public class ItemBalanzaDAO {
	static Logger log = Logger.getLogger(ItemBalanzaDAO.class);
	
	public boolean updateProcesadoAll(Session sesion, char procesado, Date fechaUltCambio, Integer tienda) {
		Transaction tx = null;
		try {
			tx = sesion.beginTransaction();
			Query query = sesion.createSQLQuery("UPDATE ARTICBALANZA SET PROCESADO = :valor1, FCH_ULT_CAMBIO = :valor2 WHERE DES_CLAVE = " + tienda);
			query.setParameter("valor1", procesado);
			query.setParameter("valor2", fechaUltCambio);
			Query query2 = sesion.createSQLQuery("UPDATE T_USERBALANZA SET PROCESADO = :valor1, FCH_ULT_CAMBIO = :valor2 WHERE DES_CLAVE = " + tienda);
			query2.setParameter("valor1", procesado);
			query2.setParameter("valor2", fechaUltCambio);
			query.executeUpdate();
			query2.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			tx.rollback();
			return false;
		}
		return true;
	}

}
