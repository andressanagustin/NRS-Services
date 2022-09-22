package com.allc.arms.server.persistence.status;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

public class StoreStatusDAO {
	
	static Logger log = Logger.getLogger(StoreStatusDAO.class);
	
	public StoreStatus getStoreStatus(Session sesion, Integer storeCode){
		
		Query query = sesion.createQuery(" FROM com.allc.arms.server.persistence.status.StoreStatus where storeCode = '" + storeCode + "'");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (StoreStatus) list.get(0);
		StoreStatus storeStatus = new StoreStatus();
		return storeStatus;
	}
	
	public TerminalStatus getTerminalStatusByTerminalAndStoreCode(Session sesion, Integer terminal, Integer storeCode){
		
		Query query = sesion.createQuery(" FROM com.allc.arms.server.persistence.status.TerminalStatus WHERE terminalNumber = '" + terminal
				+ "' and storeCode = '"+storeCode+"'");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (TerminalStatus) list.get(0);
		TerminalStatus terminalStatus = new TerminalStatus();
		return terminalStatus;
	}
	
	public TerminalStatus getUltNumFactByStoreTerminal(Session sesion,Integer storeCode, Integer terminal){
    	Query query = sesion.createQuery("from com.allc.arms.server.persistence.status.TerminalStatus  where terminalNumber = "+terminal+"  and storeCode = "+storeCode+" ");
    	List list = query.list();
		if (list != null && !list.isEmpty())
			return (TerminalStatus) list.get(0);
		TerminalStatus terminalStatus = new TerminalStatus();
		return terminalStatus;
    }
	
}
