package com.allc.arms.server.persistence.arqueo;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;


public class ArqueoDAO {
	
	static Logger log = Logger.getLogger(ArqueoDAO.class);
	
	
	public List<Object[]> getArqueoPorMedioPago(Session sesion, String terminal, String operador, String fecha){
	    	try {
	    		SQLQuery query = sesion
	    				.createSQLQuery("select AS_TND.TY_TND, count (TR_LTM_TND.MO_ITM_LN_TND) as cantidad, sum (TR_LTM_TND.MO_ITM_LN_TND) as monto, PA_BIN.CD_BIN, PA_BIN.DES_BIN from PA_OPR as OPR, TR_TRN as TR,  AS_WS as WS, AS_TND, TR_LTM_TND  left join PA_BIN on PA_BIN.CD_BIN = LEFT(TR_LTM_TND.ID_ACNT_TND,6) where  OPR.ID_OPR=TR.ID_OPR and TR_LTM_TND.ID_TRN=TR.ID_TRN	and WS.ID_WS = TR.ID_WS and AS_TND.ID_TND = TR_LTM_TND.ID_TND and AS_TND.TY_TND NOT IN ( 21, 22, 23, 24) and OPR.CD_OPR = "+operador+ " and WS.CD_WS = "+terminal+" AND TR.DC_DY_BSN = convert(datetime,'"+fecha+"',120) group by AS_TND.TY_TND, PA_BIN.CD_BIN, PA_BIN.DES_BIN");
		    	List<Object[]> rows = query.list();
		    
		    		return rows;
		    	
		    	
	    	} catch (Exception e) {
	        	log.error(e.getMessage(), e);
	        }
	    	return null;
	    }
	
	public List<Object[]> getArqueoPorCheque(Session sesion, String terminal, String operador, String fecha){
    	try {
    		SQLQuery query = sesion
    				.createSQLQuery("select  AS_TND.TY_TND, count (TR_LTM_TND.MO_ITM_LN_TND) as cuenta, sum (TR_LTM_TND.MO_ITM_LN_TND) as monto,	CO_CHK_DT.BNK from PA_OPR as OPR, TR_TRN as TR, AS_WS as WS, AS_TND, TR_LTM_TND join CO_CHK_DT on TR_LTM_TND.ID_TRN = CO_CHK_DT.ID_TRN and TR_LTM_TND.AI_LN_ITM = CO_CHK_DT.SQ_NBR where  OPR.ID_OPR=TR.ID_OPR and TR_LTM_TND.ID_TRN=TR.ID_TRN and TR_LTM_TND.ID_TND=AS_TND.ID_TND and WS.ID_WS = TR.ID_WS and AS_TND.TY_TND IN ( 21, 22, 23, 24) and OPR.CD_OPR = "+operador+ " and WS.CD_WS = "+terminal+" AND TR.DC_DY_BSN = convert(datetime,'"+fecha+"',120) group by AS_TND.TY_TND, CO_CHK_DT.BNK");
	    	List<Object[]> rows = query.list();
	    
	    		return rows;
	    	
	    	
    	} catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
    	return null;
    }

}
