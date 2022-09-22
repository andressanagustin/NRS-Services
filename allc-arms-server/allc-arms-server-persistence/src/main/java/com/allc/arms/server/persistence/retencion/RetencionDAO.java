/**
 * 
 */
package com.allc.arms.server.persistence.retencion;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;


/**
 * @author GUSTAVOK
 *
 */
public class RetencionDAO {
	static Logger log = Logger.getLogger(RetencionDAO.class);

    public List<String> getTrxData(Session sesion, String trxNmbr, String date){
    	try {
    		//TODO: revisar parametrizaci�n para determinar si la consulta debe ser realizada sobre Oracle o SqlServer
    		
	    	/*Consulta para Oracle*/
//    		SQLQuery query = sesion.createSQLQuery("SELECT TRN.FL_VD, SUM(CASE WHEN TND.TY_TND LIKE '4%' THEN 1 ELSE 0 END) AS TAR_PAY, SUM(CASE TND.TY_TND WHEN '61' THEN 1 ELSE 0 END) AS RET_PAY, SUM(CASE TOT.ID_TR_TOT_TYP WHEN 1 THEN TOT.MO_TOT_RTL_TRN ELSE -TOT.MO_TOT_RTL_TRN END) AS AMOUNT, TRN.ID_TRN, TR_INVC.ID_CPR, SUM(CASE TOT.ID_TR_TOT_TYP WHEN 3 THEN TOT.MO_TOT_RTL_TRN ELSE 0 END) AS IMP, STR.CD_STR_RT FROM AS_TND TND, TR_TRN TRN, TR_LTM_TND LTM, PA_STR_RTL STR, TR_TOT_RTL TOT, TR_INVC WHERE TRN.ID_BSN_UN = STR.ID_BSN_UN AND TRN.ID_TRN = LTM.ID_TRN AND LTM.ID_TND = TND.ID_TND AND TRN.ID_TRN = TOT.ID_TRN AND TRN.ID_TRN = TR_INVC.ID_TRN AND TR_INVC.INVC_NMB = "+trxNmbr+" AND TRN.DC_DY_BSN = TO_DATE('"+date+"','DD/MM/YYYY') GROUP BY TRN.ID_TRN, STR.CD_STR_RT, TRN.FL_VD, TR_INVC.ID_CPR");
    		/*Consulta para SQLServer*/
    		SQLQuery query = sesion.createSQLQuery("SELECT TRN.FL_VD, SUM(CASE WHEN TND.TY_TND LIKE '4%' THEN 1 ELSE 0 END) AS TAR_PAY, SUM(CASE TND.TY_TND WHEN '62' THEN 1 WHEN '63' THEN 1 ELSE 0 END) AS RET_PAY, SUM(CASE TOT.ID_TR_TOT_TYP WHEN 1 THEN TOT.MO_TOT_RTL_TRN ELSE -TOT.MO_TOT_RTL_TRN END) / COUNT(Distinct LTM.AI_LN_ITM) AS AMOUNT, TRN.ID_TRN, TR_INVC.ID_CPR, SUM(CASE TOT.ID_TR_TOT_TYP WHEN 3 THEN TOT.MO_TOT_RTL_TRN ELSE 0 END) / COUNT(Distinct LTM.AI_LN_ITM) AS IMP, STR.CD_STR_RT FROM AS_TND TND, TR_TRN TRN, TR_LTM_TND LTM, PA_STR_RTL STR, TR_TOT_RTL TOT, TR_INVC WHERE TRN.ID_BSN_UN = STR.ID_BSN_UN AND TRN.ID_TRN = LTM.ID_TRN AND LTM.ID_TND = TND.ID_TND AND TRN.ID_TRN = TOT.ID_TRN AND TRN.ID_TRN = TR_INVC.ID_TRN AND TR_INVC.INVC_NMB = '"+trxNmbr+"' AND CAST(TRN.TS_TRN_END AS DATE) = CAST(convert(datetime,'"+date+"',103) AS DATE) GROUP BY TRN.ID_TRN, STR.CD_STR_RT, TRN.FL_VD, TR_INVC.ID_CPR");
	    	List<Object[]> rows = query.list();
	    	for(Object[] row : rows){
	    		List<String> retorno = new ArrayList<String>();
	    		retorno.add(row[0]!= null ? row[0].toString() : null);
	    		retorno.add(row[1]!= null ? row[1].toString() : null);
	    		retorno.add(row[2]!= null ? row[2].toString() : null);
	    		retorno.add(row[3]!= null ? row[3].toString() : null);
	    		retorno.add(row[4]!= null ? row[4].toString() : null);
	    		retorno.add(row[5]!= null ? row[5].toString() : null);
	    		retorno.add(row[6]!= null ? row[6].toString() : null);
	    		retorno.add(row[7]!= null ? row[7].toString() : null);
	    		return retorno;
	    	}
    	} catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
    	return null;
    }
    
    public boolean existeItemServicio(Session sesion, String trxID){
    	try {
	    	SQLQuery query = sesion.createSQLQuery("SELECT TRN.ID_TRN FROM TR_TRN TRN, TR_LTM_SLS_RTN LTM, AS_ITM ITM, AS_ITM_STR ITMSTR WHERE TRN.ID_TRN = LTM.ID_TRN AND LTM.ID_ITM = ITM.ID_ITM AND ITM.ID_ITM = ITMSTR.ID_ITM AND (ITMSTR.ID_SPL_FMY = 2 OR ITMSTR.ID_SPL_FMY = 6 OR ITMSTR.ID_SPL_FMY = 7) AND TRN.ID_TRN = "+trxID);
	    	List rows = query.list();
	    	if(rows!=null && !rows.isEmpty()){
	    		return true;
	    	}
    	} catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
    	return false;
    }
    
    public boolean existRetencion(Session sesion, String nroFactura){
    	try {
	    	Query query = sesion.createQuery(" FROM com.allc.entities.RetencionData R WHERE R.tiquete = '"+nroFactura+"'");
	    	List rows = query.list();
	    	if(rows!=null && !rows.isEmpty()){
	    		return true;
	    	}
    	} catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
    	return false;
    }
	
}
