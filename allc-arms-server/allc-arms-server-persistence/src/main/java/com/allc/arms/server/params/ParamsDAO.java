/**
 * 
 */
package com.allc.arms.server.params;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;


/**
 * @author gustavo
 *
 */
public class ParamsDAO {
	static Logger log = Logger.getLogger(ParamsDAO.class);

	/**
	 * Método que retorna todos los parámetros asociados a la tienda y al ambito recibidos como parametro.
	 * @param sesion
	 * @param codTienda
	 * @param ambito
	 * @return
	 */
	public List getParamsList(Session sesion, Integer codTienda, Integer ambito) {
		try {
			Query query = sesion.createQuery(" FROM com.allc.arms.server.params.ParamValue P WHERE (P.tienda = " + codTienda + " OR P.tienda IS NULL) AND P.param.ambito = " + ambito + " AND P.estado = 1 ORDER BY P.tienda ASC");
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return rows;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public ParamValue getParamByClave(Session sesion, String codTienda, Integer ambito, String clave){
	
	    try{
    	Query query = sesion.createQuery(" FROM com.allc.arms.server.params.ParamValue P WHERE (P.tienda = " + Integer.valueOf(codTienda) + " OR P.tienda IS NULL) AND P.param.clave = '" + clave + "' AND P.param.ambito = " + ambito + " ORDER BY P.tienda ASC");
    	Iterator iterator = query.iterate();
    	if(iterator.hasNext())
    		return (ParamValue) iterator.next();
	    }catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public ParamValue getParValSpecifiByClave(Session sesion, Integer codTienda, Integer ambito, String clave){
	    try{
	    	Query query = sesion.createQuery(" FROM com.allc.arms.server.params.ParamValue P WHERE P.tienda = " + codTienda + " AND P.param.clave = '" + clave + "' AND P.param.ambito = " + ambito + " ORDER BY P.tienda ASC");
	    	Iterator iterator = query.iterate();
	    	if(iterator.hasNext())
	    		return (ParamValue) iterator.next();
	    }catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public Param getParam(Session sesion, Integer ambito, String clave){
	    try{
    	Query query = sesion.createQuery(" FROM com.allc.arms.server.params.Param P WHERE  P.clave = '" + clave + "' AND P.ambito = " + ambito);
    	Iterator iterator = query.iterate();
    	if(iterator.hasNext())
    		return (Param) iterator.next();
	    }catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public List getParamsList(Session sesion, Integer codTienda) {
		try {
			Query query = sesion.createQuery(" FROM com.allc.arms.server.params.ParamValue P WHERE (P.tienda = " + codTienda + " OR P.tienda IS NULL) AND P.estado = 1 ORDER BY P.tienda ASC");
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return rows;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	
	
	public List getParamsListGroup(Session sesion, BigInteger idBsnUnGp) {
		try {
			Query query = sesion.createQuery(" FROM com.allc.arms.server.params.ParamValue P WHERE (P.nivelGrupo = " + idBsnUnGp + " or P.nivelGrupo is null) AND P.tienda IS NULL AND P.estado = 1 ORDER BY P.nivelGrupo ASC" );
			
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return rows;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public List getParamsListGroup(Session sesion, BigInteger idBsnUnGp,Integer ambito) {
		try {
			Query query = sesion.createQuery(" FROM com.allc.arms.server.params.ParamValue P WHERE (P.nivelGrupo = " + idBsnUnGp + " or P.nivelGrupo is null) AND P.tienda IS NULL AND P.estado = 1 AND P.param.ambito = " + ambito +" ORDER BY P.nivelGrupo ASC");
			
			List rows = query.list();
			if (rows != null && !rows.isEmpty()) {
				return rows;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * Retornar todos los parámetros asociados a la tienda, region  y al ambito.
	 * @param sesion
	 * @param idBsnUnGp
	 * @param codTienda
	 * @param ambito
	 * @author Joel Jalón Gómez
	 */
	public List<ParamValue> getParamsListStoreGroup(Session sesion, Integer idBsnUnGp,Integer codTienda, Integer ambito) {
		List<ParamValue> params = new ArrayList<ParamValue>();
		try {
			String nativeQuery = "SELECT PO.ID_PARVAL, \r\n" + 
					"					       P.VAR_PARAM CLAVE, \r\n" + 
					"					       PO.COD_PARAM, \r\n" + 
//					"					       COALESCE(PT.VAL_PARAM,PO.VAL_PARAM) VAL_PARAM, \r\n" + 
					"					       COALESCE(coalesce(PT.VAL_PARAM,PR.VAL_PARAM),PO.VAL_PARAM) VAL_PARAM, \r\n" + 
					"					       PO.ESTADO, \r\n" + 
					"					       PO.DES_CLAVE        \r\n" + 
					"					FROM SAADMIN.PM_PARVAL PO \r\n" + 
					"					INNER JOIN SAADMIN.PM_PARAM P ON P.COD_PARAM = PO.COD_PARAM\r\n" + 
					"					LEFT JOIN SAADMIN.PM_PARVAL PR ON PO.COD_PARAM = PR.COD_PARAM AND PR.ID_BSN_UN_GP = ? and pr.des_clave is null AND PR.ESTADO = 1\r\n" + 
					"					LEFT JOIN SAADMIN.PM_PARVAL PT ON PO.COD_PARAM = PT.COD_PARAM AND PT.DES_CLAVE = ?  and pt.ID_BSN_UN_GP is null AND PT.ESTADO = 1 \r\n" + 
//					"					LEFT JOIN SAADMIN.PM_PARVAL PT ON PO.COD_PARAM = PT.COD_PARAM AND PT.DES_CLAVE = ? AND PT.ESTADO = 1 \r\n" + 
					"					WHERE P.AMBITO = ?\r\n" + 
					"					AND PO.DES_CLAVE IS NULL \r\n" + 
					"					AND PO.ID_BSN_UN_GP IS NULL\r\n" + 
					"					AND PO.ESTADO = 1 \r\n" + 
					"					ORDER BY  \r\n" + 
					"					P.VAR_PARAM";
			
			
			SQLQuery query = sesion.createSQLQuery(nativeQuery);
			
			query.setInteger(0, idBsnUnGp);
			query.setInteger(1, codTienda);
			query.setInteger(2, ambito);
//			query.setInteger(0, codTienda);
//			query.setInteger(1, ambito);
			List<Object[]> rows = query.list();
			if (rows == null || rows.isEmpty())
			{
				log.info("No hay parametros configurados");
				return null;
			}
			
			for(Object[] obj : rows) {
				ParamValue pv = new ParamValue();
				Param pm = new Param();
				pm.setClave(obj[1].toString());
				pm.setCodigo(((Integer) obj[2]).longValue());
				
				pv.setEstado(1);
				pv.setId(((Integer) obj[0]).longValue());
				pv.setTienda(codTienda);
				pv.setValor(obj[3] != null ? obj[3].toString() : null);
				pv.setParam(pm);
				
				params.add(pv);
			}

			return params;
		} catch (Exception e) {
			log.error("Error al consultar parametros:");
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	public ParamValue getParValSpecifiByClaveGroup(Session sesion, BigInteger idBsnUnGp, Integer ambito, String clave){
	    try{
	    	Query query = sesion.createQuery(" FROM com.allc.arms.server.params.ParamValue P WHERE P.nivelGrupo = " + idBsnUnGp + " AND P.param.clave = '" + clave + "' AND P.param.ambito = " + ambito);
	    	Iterator iterator = query.iterate();
	    	if(iterator.hasNext())
	    		return (ParamValue) iterator.next();
	    }catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	
	
}
