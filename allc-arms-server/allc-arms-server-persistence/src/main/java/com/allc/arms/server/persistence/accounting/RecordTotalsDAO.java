package com.allc.arms.server.persistence.accounting;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

public class RecordTotalsDAO {
	
	static Logger log = Logger.getLogger(RecordTotalsDAO.class);
	
	public StoreRecordTotals getStoreRecordByStoreDate(Session sesion, Integer storeCode, String fecha){
		
		Query query = sesion.createQuery(" FROM com.allc.arms.server.persistence.accounting.StoreRecordTotals WHERE storeCode = '" + storeCode
				+ "' and timeStamp = convert(datetime,'"+fecha+"',120)");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (StoreRecordTotals) list.get(0);
		StoreRecordTotals storeRecord = new StoreRecordTotals();
		return storeRecord;
	}
	
	public OperTermRecordTotals getOperTermRecordByAccountDate(Session sesion, Integer storeCode, String recordType, Integer account, String fecha){
		
		Query query = sesion.createQuery(" FROM com.allc.arms.server.persistence.accounting.OperTermRecordTotals WHERE storeCode = '"+ storeCode + "' and recordType = '"+ recordType +"' and accountId = '" + account
				+ "' and storeTimeStamp = convert(datetime,'"+fecha+"',120)");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (OperTermRecordTotals) list.get(0);
		OperTermRecordTotals storeRecord = new OperTermRecordTotals();
		return storeRecord;
	}
	
	public TenderTotalsVarietyRecord getTenderVarietyRecordByAccount(Session sesion, Integer storeCode, String recordType, Integer account, String fecha){
		
		Query query = sesion.createQuery(" FROM com.allc.arms.server.persistence.accounting.TenderTotalsVarietyRecord WHERE storeCode = '"+ storeCode + "' and recordType = '"+ recordType +"' and accountId = '" + account
				+ "' and timeStamp = convert(datetime,'"+fecha+"',120)");
		List list = query.list();
		if (list != null && !list.isEmpty())
			return (TenderTotalsVarietyRecord) list.get(0);
		TenderTotalsVarietyRecord tenderVariety = new TenderTotalsVarietyRecord();
		return tenderVariety;
	}

}
