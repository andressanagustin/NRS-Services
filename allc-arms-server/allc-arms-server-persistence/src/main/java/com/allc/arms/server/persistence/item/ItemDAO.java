/**
 * 
 */
package com.allc.arms.server.persistence.item;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.allc.entities.Item;

/**
 * @author gustavo
 *
 */
public class ItemDAO {
	static Logger log = Logger.getLogger(ItemDAO.class);

	public Item getItem(Session sesion, String codSAP) {
		try {
			Query query = sesion.createQuery(" FROM com.allc.entities.Item I WHERE I.codigoSAP = " + new Integer(codSAP));
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return (Item) rows.get(0);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
}
