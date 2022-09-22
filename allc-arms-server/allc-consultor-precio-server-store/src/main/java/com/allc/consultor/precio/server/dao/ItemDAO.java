/**
 * 
 */
package com.allc.consultor.precio.server.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

/**
 * @author gustavo
 *
 */
public class ItemDAO {

//	static Logger log = Logger.getLogger(ItemDAO.class);

	public List<String> getItem(Session sesion, Long ean, Integer store) {
    	try {
    		//System.out.println("EAN: "+ean + " STORE: "+store);
	    	SQLQuery query = sesion.createSQLQuery("SELECT AS_ITM.DE_ITM, AS_ITM_STR.SLS_PRC, AS_ITM_STR.SLS_PRC, AS_ITM_STR.SLS_PRC, CASE WHEN AS_ITM_STR.TX_A = 1 THEN 'S' ELSE 'N' END TAX, 'N' PROMO, CASE WHEN CO_MRHRC_GP.PORC_REC > 0 THEN CO_MRHRC_GP.PORC_REC ELSE ID_DPT_PS.PORC_REC END  PORC_REC, PA_STR_RTL.IMP_1, ID_PS.PRTY FROM AS_ITM, AS_ITM_STR, PA_STR_RTL, ID_PS, CO_MRHRC_GP, ID_DPT_PS WHERE AS_ITM.ID_ITM = AS_ITM_STR.ID_ITM AND AS_ITM_STR.ID_BSN_UN = PA_STR_RTL.ID_BSN_UN AND AS_ITM.ID_ITM = ID_PS.ID_ITM AND AS_ITM.ID_MRHRC_GP = CO_MRHRC_GP.ID_MRHRC_GP AND CO_MRHRC_GP.ID_DPT_PS = ID_DPT_PS.ID_DPT_PS AND PA_STR_RTL.CD_STR_RT = "+store+" AND ID_PS.ID_ITM_PS = "+ean +" ORDER BY ID_PS.PRTY DESC");
	    	List<Object[]> rows = query.list();
	    	for(Object[] row : rows){
	    		List<String> retorno = new ArrayList<String>();
	    		retorno.add(row[0].toString());
	    		retorno.add(row[1].toString());
	    		retorno.add(row[2].toString());
	    		retorno.add(row[3].toString());
	    		retorno.add(row[4].toString());
	    		retorno.add(row[5].toString());
	    		retorno.add(row[6].toString());
	    		retorno.add(row[7].toString());
	    		//la prioridad solo la usamos para ordenar y que tome siempre uno con P
	    		return retorno;
	    	}
    	} catch (Exception e) {
    		e.printStackTrace();
//        	log.error(e.getMessage(), e);
        }
		return null;
	}

}
