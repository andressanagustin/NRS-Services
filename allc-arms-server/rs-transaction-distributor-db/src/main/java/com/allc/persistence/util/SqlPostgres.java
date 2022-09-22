/*
 *  ***************************************************************
 * NUO - 2020
 * Creado por: Ing. Joel Jalon Gomez 
 * Motivo: queries a utilizar solo para Base de Datos PostgreSQL
 * Fecha Creacion: 2020-05-16
 *  ***************************************************************
 */
package com.allc.persistence.util;


public class SqlPostgres{
    public static final String OBTENER_OPERADORES_LENTOS_POR_TRX = "SELECT Y.*\r\n" +
    		",CAST(PAI.VALOR AS INTEGER) ITM_X_MIN_PARAM\r\n"+
    		"FROM (\r\n" + 
    		"	select x.*\r\n" + 
    		"		,case when cast ((X.TIEMPO_RING_ACTUAL/X.TOT_LINEAS_VENTA) as DECIMAL(18,2)) = 0 then 1.00\r\n" + 
    		"				else cast ((X.TIEMPO_RING_ACTUAL/X.TOT_LINEAS_VENTA) as DECIMAL(18,2)) end AS TIEMPO_VENTA_X_ITM\r\n" + 
    		"		, cast ((X.TOT_LINEAS_VENTA / CASE WHEN X.TIEMPO_RING_ACTUAL = 0 THEN 1 ELSE X.TIEMPO_RING_ACTUAL END ) as DECIMAL(18,2)) ITM_X_SEG\r\n" + 
    		"		, ( cast((X.TOT_LINEAS_VENTA / CASE WHEN X.TIEMPO_RING_ACTUAL = 0 THEN 1 ELSE X.TIEMPO_RING_ACTUAL END) as DECIMAL(18,2)) * X.SEG_FREQ) ITM_X_FREQ\r\n" +
    		" 		,TO_CHAR(CAST (CAST ((x.TIEMPO_RING_ACTUAL + x.TIEMPO_PAGO + X.TIEMPO_INACTIVIDAD_TRX_ACTUAL + X.TIEMPO_ASEGURADO_TRX_ACTUAL) AS VARCHAR)\r\n" + 
    		"					|| ' second' AS INTERVAL) ,'MI:SS') as TOTAL_TIEMPO_TRX \r\n"+
    		"		from (\r\n" + 
    		"		select \r\n" + 
    		"					td.id_bsn_un\r\n" + 
    		"					,td.cd_str_rt CODIGO_TIENDA\r\n" + 
    		"					,td.de_str_rt tienda\r\n" + 
    		"					,TA.IP_WS_NOTIF_SUPERV IP\r\n" +
    		
    		"					,t.id_opr\r\n" + 
    		"					,e.CD_WS CODIGO_CAJA\r\n" + 
    		"					,t.id_trn, t.id_ws, t.ai_trn, t.ts_trn_bgn, t.ts_trn_end\r\n" + 
    		"					, cast(t.ts_trn_bgn as date) fecha, cast(TO_CHAR(T.TS_TRN_BGN,'HH24') as INTEGER) hora\r\n" + 
    		"					,TT.IN_ELPSD_IDL TIEMPO_INACTIVIDAD_TRX_ANT\r\n" + 
    		"					,TT.IN_LCK_ELPSD TIEMPO_ASEGURADO_TRX_ANT\r\n" + 
    		"					,LEAD(TT.IN_ELPSD_IDL,1,cast (0 as numeric)) OVER(partition by t.id_bsn_un,t.id_ws ORDER BY t.id_bsn_un,cast (t.ts_trn_bgn as date), t.id_ws, t.ai_trn) TIEMPO_INACTIVIDAD_TRX_ACTUAL\r\n" + 
    		"					,LEAD(TT.IN_LCK_ELPSD,1,cast (0 as numeric)) OVER(partition by t.id_bsn_un,t.id_ws ORDER BY t.id_bsn_un,cast (t.ts_trn_bgn as date), t.id_ws, t.ai_trn) TIEMPO_ASEGURADO_TRX_ACTUAL\r\n" + 
    		"					,TT.IN_RNG_ELPSD TIEMPO_RING_ACTUAL\r\n" + 
    		"					,TT.IN_TND_ELPSD TIEMPO_PAGO\r\n" + 
    		"					,coalesce(e.tp_ws,7) ID_TIPO_CAJA \r\n" + 
    		"					,TE.DESCRIPCION TIPO_CAJA\r\n" + 
    		"					,? as SEG_FREQ --param\r\n" + 
    		"					,cast(COUNT(TL.ID_TRN) as INTEGER) AS TOT_LINEAS_VENTA\r\n" + 
    		"						,CAST(SUM(TL.QU_ITM_LM_RTN_SLS) AS INTEGER) AS CANTIDAD_ARTICULOS\r\n" +
    		"					,cast(an.notificar_alerta_superv as integer) notificar_alerta_superv\r\n"+
    		"					,COALESCE(OP.NOMB_ACE,'NOT DEFINED') OPERADOR\r\n" +
    		"					FROM ARTS_EC.TR_TRN T\r\n" + 
    		"					INNER JOIN ARTS_EC.AS_WS E ON E.ID_WS = T.ID_WS\r\n" + 
    		"					INNER JOIN ARTS_EC.PA_STR_RTL TD ON TD.ID_BSN_UN = E.ID_BSN_UN\r\n" + 
    		"					inner JOIN saadmin.mn_tienda ta on ta.des_clave = td.cd_str_rt\r\n" + 
    		"\r\n" + 
    		"							inner join ARTS_EC.TR_LTM_SLS_RTN TL ON TL.ID_TRN = T.ID_TRN AND TL.LU_MTH_ID_ENR != 'SERVICE' --(LU_MTH_ID_ENR = ''SCANNED'' OR LU_MTH_ID_ENR = ''KEYED'') \r\n" + 
    		"					INNER JOIN ARTS_EC.TR_RTL AS TT ON TT.ID_TRN = T.ID_TRN\r\n" + 
    		"					LEFT JOIN EYES_EC.ETC_TIPO_TERMINAL TE ON TE.ID_TIPO_TERMINAL = COALESCE(E.TP_WS,7)\r\n" + 
    		"						left join arts_ec.PA_OPR aop on t.id_opr = aop.id_opr\r\n" + 
    		"						LEFT JOIN operac_ec.op_operador OP ON OP.CC_OPERADOR = (CASE WHEN coalesce(aop.cd_opr,'') = '' THEN '1' ELSE aop.cd_opr END)\r\n" +
    		"					inner join colas_ec.AL_TIENDA_NOTIF_SUPERV an --para procesar solo las que esten configuradas y bandera de proceso activa\r\n" + 
    		"									on an.codigo_tienda = td.cd_str_rt \r\n" + 
    		"									and an.id_tipo_caja = COALESCE(E.TP_WS,7)\r\n" + 
    		"									and an.codigo_alerta =  '2.1'\r\n" + 
    		"									and an.procesar_alerta = 1\r\n"+
    		"					where t.id_trn = ? -- param\r\n" +  
    		"					group by \r\n" + 
    		"					td.id_bsn_un\r\n" + 
    		"					,td.cd_str_rt\r\n" + 
    		"					,td.de_str_rt \r\n" + 
    		"					,TA.IP_WS_NOTIF_SUPERV\r\n" + 
    		"					,an.notificar_alerta_superv\r\n"+
    		"					,t.id_opr\r\n" + 
    		"					,e.CD_WS\r\n" + 
    		"					,t.id_trn, t.id_ws, t.ai_trn, t.ts_trn_bgn, t.ts_trn_end\r\n" + 
    		"					, cast(t.ts_trn_bgn as date) , cast (TO_CHAR(T.TS_TRN_BGN,'HH24') as INTEGER)\r\n" + 
    		"					,TT.IN_ELPSD_IDL --TIEMPO_INACTIVIDAD_TRX_ANT\r\n" + 
    		"					,TT.IN_LCK_ELPSD --TIEMPO_ASEGURADO_TRX_ANT\r\n" + 
    		"					,TT.IN_RNG_ELPSD --TIEMPO_RING_ACTUAL\r\n" + 
    		"					,TT.IN_TND_ELPSD --TIEMPO_PAGO\r\n" + 
    		"					,e.tp_ws --ID_TIPO_CAJA \r\n" + 
    		"					,TE.DESCRIPCION --TIPO_CAJA\r\n" +
    		"				,COALESCE(OP.NOMB_ACE,'NOT DEFINED')\r\n" +
    		"	) x\r\n" + 
    		") Y\r\n" + 
    		"INNER JOIN colas_ec.fn_consulta_parametros_al_notif_superv(Y.CODIGO_TIENDA,Y.ID_TIPO_CAJA,'2.1') PAI\r\n" + 
    		"				ON PAI.PARAMETRO = 'proceso.cantidad.articulos.minuto.cajero.items.disparador'\r\n" + 
    		"where Y.TOT_LINEAS_VENTA >= CAST(PAI.VALOR AS INTEGER) --DISPARADOR ITEMS \r\n" + 
    		"AND Y.ITM_X_FREQ < CAST(PAI.VALOR AS INTEGER) --DISPARADOR ITEMS " + 
    		"";
    
    public static final String ACTUALIZAR_CAJERO_LENTO = "UPDATE ARTS_EC.AL_CAJA_ITM_EVALUAR SET IS_SLOW = ?, LAST_ID_TRN = ?, LAST_AI_TRN = ? WHERE ID_BSN_UN = ? AND ID_WS = ? AND CD_OPR = ?";
    public static final String VALIDA_EXISTE_CAJERO_LENTO = "SELECT COUNT(*) FROM ARTS_EC.AL_CAJA_ITM_EVALUAR WHERE ID_BSN_UN = ? AND ID_WS = ? AND CD_OPR = ?";
    public static final String INSERTA_CAJERO_LENTO = "INSERT INTO ARTS_EC.AL_CAJA_ITM_EVALUAR VALUES (?,?,?,?,?,?)";
    
    public static final String VALIDA_NOTIFICAR_ALERTA_PROCESO_TIENDA = "select count(*) as existe\r\n" + 
    		"from ARTS_EC.AL_TIENDA_NOTIF_SUPERV tn\r\n" +  
    		"WHERE TN.CODIGO_TIENDA = ?\r\n" + 
    		"AND TN.CODIGO_ALERTA = ?\r\n" + 
    		"AND TN.notificar_alerta_superv = 1";
    
    public static final String VALIDA_EJECUCION_PROCESO_ALERTA_SUPERVISOR = "SELECT count(CD.*) AS EXISTE\r\n" + 
    		"FROM SAADMIN.CATALOGO_DETALLE CD\r\n" + 
    		"WHERE CD.ID_CATALOGO = (\r\n" + 
    		"	select C.ID_CATALOGO \r\n" + 
    		"	FROM SAADMIN.CATALOGO C\r\n" + 
    		"	WHERE C.CODIGO = 'AL_NOTIF_SUPERV'\r\n" + 
    		")\r\n" + 
    		"AND CD.CODIGO = ? --PARAM CODIGO DE ALERTA\r\n" + 
    		"AND CD.ESTADO = 1";
    
    //public static final String OBTENER_PARAMETROS_ALERTAS_SUPERVISOR_EN_BD = "select parametro, valor from colas_ec.fn_consulta_parametros_al_notif_superv(null,null,?)";
    public static final String OBTENER_PARAMETROS_ALERTAS_SUPERVISOR_EN_BD = "select parametro, valor from colas_ec.fn_consulta_parametros_al_notif_superv(?,?,?)";
    
    public static final String AL_NS_REGISTRO_AUDIT = "insert into colas_ec.al_notif_superv_audit\r\n" + 
    		"values ";
    public static final String OBTENER_TITULO_ALERTA = "select VALOR_2 TITULO from saadmin.catalogo_detalle_central where id_catalogo = 2 AND CODIGO = ?";
    public static final String CONEXION_DB_LINK = "select public.dblink_connect(cast('dblink71' as text),cast('host=**HOST** dbname=**DBNAME** user=**USUARIO** password=**PASSWORD**' as text))";
    public static final String CHECK_DBLINK = "SELECT array_to_string(dblink_get_connections,',') from public.dblink_get_connections() where array_to_string(dblink_get_connections,',') like '%dblink71%' ";
    public static final String DESCONEXION_DBLINK = "select public.dblink_disconnect(cast('dblink71' as text))";
}
