package com.allc.arms.server.persistence.pinpad;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

public class PinPadDAO {

	private Logger log = Logger.getLogger(PinPadDAO.class);
	
	public List getPinpadByStore(Session sesion, Integer store) {
		try {
			
			Query query = sesion.createQuery(" FROM com.allc.arms.server.persistence.pinpad.PinPad P WHERE P.codTienda = '" + store
					+ "' and P.actualizar = " + 0);
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return rows;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	 public List getPinpadByStatus(Session sesion, Integer status){
	    	Query query = sesion.createQuery("from com.allc.arms.server.persistence.pinpad.PinPad where actualizar = '"+status+"' ");
	    	Iterator iterator = query.iterate();
	    	List list = new ArrayList();
	    	while(iterator.hasNext())
	    		list.add(iterator.next());
	    	return list;
	    }
}
