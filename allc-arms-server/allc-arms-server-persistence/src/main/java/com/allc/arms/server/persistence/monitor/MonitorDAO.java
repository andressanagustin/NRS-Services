package com.allc.arms.server.persistence.monitor;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class MonitorDAO {
	static Logger log = Logger.getLogger(MonitorDAO.class);
	
	public Monitor getMonitorById(Session sesion, int id_local, String des_clave) {
		Query query = sesion.createQuery("from com.allc.arms.server.persistence.monitor.Monitor where id_local = " + id_local + " and des_clave='" + des_clave + "'");
		Iterator iterator = query.iterate();
		if (iterator.hasNext())
			return (Monitor) iterator.next();
		return null;
	}
	
	public List<Object[]> getOperacionByTienda(Session sesion, int id_local) {
		try {
			SQLQuery query = sesion.createSQLQuery("SELECT AW.id_ws, AW.cd_ws, COALESCE(SUM(CASE "
					+ "WHEN TL.fl_is_chng = '1' THEN - TL.MO_ITM_LN_TND "
					+ "WHEN TL.ai_ln_itm_vd <> 0 THEN - TL.MO_ITM_LN_TND "
					+ "ELSE TL.MO_ITM_LN_TND "
					+ "END)), 0) MO_ITM_LN_TND, "
					+ "COUNT(DISTINCT TR.id_trn) CANTIDAD "
					+ "FROM arts_ec.tr_trn TR "
					+ "LEFT JOIN arts_ec.tr_ltm_tnd TL ON TL.id_trn=TR.id_trn "
					+ "INNER JOIN arts_ec.AS_WS AW ON TR.id_ws=AW.id_ws "
					+ "INNER JOIN arts_ec.PA_STR_RTL PA ON AW.ID_BSN_UN = PA.ID_BSN_UN "
					+ "INNER JOIN saadmin.mn_tienda MN ON PA.cd_str_rt = MN.des_clave "
					+ "WHERE MN.cod_tienda=" + id_local + " AND DATE(TR.ts_trn_bgn) = CURRENT_DATE GROUP BY AW.id_ws, cd_ws");
			List<Object[]> rows = query.list();
			return rows;	
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	public boolean insertMonitor(Session sesion, Monitor monitor) {
		Transaction tx = null;
		try {
			tx = sesion.beginTransaction();
			sesion.saveOrUpdate(monitor);
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
