/*
 *  ***************************************************************
 * NUO - 2020
 * Creado por: Ing. Joel Jalon Gomez 
 * Motivo: queries a utilizar solo para Base de Datos PostgreSQL
 * Fecha Creacion: 2020-05-16
 *  ***************************************************************
 */
package com.allc.arms.server.operations.colas;


public class SqlPostgres{
    public static final String CONSULTAR_TRX_CON_CLIENTES_COLA_SUPERADO = "select \r\n" + 
    		"td.de_str_rt TIENDA\r\n" + 
    		",TA.IP_WS_NOTIF_SUPERV IP\r\n" + 
    		",cl.id_bsn_un, td.cd_str_rt CODIGO_TIENDA, cl.id_ws, cl.ai_trn, cl.number_queue as CANTIDAD_COLAS\r\n" + 
    		",E.CD_WS CODIGO_CAJA\r\n" + 
    		",t.id_trn, t.id_opr, t.ts_trn_bgn\r\n" + 
    		",(CASE WHEN coalesce(aop.cd_opr,'') = '' THEN 1 ELSE cast(aop.cd_opr as INTEGER) END) CODIGO_OPERADOR\r\n" +
    		",COALESCE(OP.NOMB_ACE,'NOT DEFINED') OPERADOR\r\n" +
    		",cast(an.notificar_alerta_superv as integer) notificar_alerta_superv\r\n" + 
    		"	,coalesce(e.tp_ws,7) ID_TIPO_CAJA \r\n" + 
    		"	,cast(PAC.VALOR as INTEGER) MINIMO_COLAS_PARAM \r\n"+
    		"from arts_ec.co_queue_dt cl\r\n" + 
    		"INNER JOIN ARTS_EC.AS_WS E ON E.ID_WS = CL.ID_WS\r\n" + 
    		"INNER JOIN ARTS_EC.PA_STR_RTL TD ON TD.ID_BSN_UN = E.ID_BSN_UN\r\n" + 
    		"inner JOIN saadmin.mn_tienda ta on ta.des_clave = td.cd_str_rt\r\n" +


			//CRUZAMOS CON LA ULTIMA TRANSACCION PARA OBTENER EL OPERADOR ASOCIADO A LA CAJA
			"INNER JOIN arts_ec.tr_trn t on " +
			"	t.id_bsn_un = cl.id_bsn_un " +
			"   and cl.id_ws = t.id_ws  " +
			"	and t.id_trn = (select max(id_trn) FROM arts_ec.tr_trn where " +
			"										id_bsn_un = cl.id_bsn_un and  id_ws = cl.id_ws and " +
			"										cast(cl.date_queue as date) = cast(ts_trn_bgn as date)) " +
			"	and cast(cl.date_queue as date) = cast(t.ts_trn_bgn as date)  " +


			"left join arts_ec.PA_OPR aop on t.id_opr = aop.id_opr\r\n" +
    		"LEFT JOIN operac_ec.op_operador OP ON OP.CC_OPERADOR = (CASE WHEN coalesce(aop.cd_opr,'') = '' THEN '1' ELSE aop.cd_opr END)\r\n" +
    		"inner join colas_ec.AL_TIENDA_NOTIF_SUPERV an --para procesar solo las que esten configuradas y bandera de proceso activa\r\n" + 
    		"						on an.codigo_tienda = td.cd_str_rt \r\n" + 
    		"						and an.id_tipo_caja = COALESCE(E.TP_WS,7)\r\n" + 
    		"						and an.codigo_alerta =  '3'\r\n" + 
    		"						and an.procesar_alerta = 1 \r\n"+
    		"INNER JOIN colas_ec.fn_consulta_parametros_al_notif_superv(TD.CD_STR_RT,coalesce(e.tp_ws,7),'3') PAC\r\n" + 
    		"	ON PAC.PARAMETRO = 'alertas.cantidad.clientes.cola.excedido' \r\n"+
    		"where cl.id_bsn_un = :idBsnUn \r\n" + 
    		"and cl.id_ws = :idWs \r\n" + 
    		"and cl.ai_trn = :aiTrn \r\n" + 
    		"and cast(cl.date_queue as date) = CURRENT_DATE \r\n"+
    		"AND cl.number_queue > cast(PAC.VALOR as INTEGER)";   
    public static final String CONSULTAR_TRX_CON_CLIENTES_COLA_SUPERADO_LOCAL = "select \r\n" + 
    		"td.de_str_rt TIENDA\r\n" + 
    		",TA.IP_WS_NOTIF_SUPERV IP\r\n" + 
    		",cl.id_bsn_un, td.cd_str_rt CODIGO_TIENDA, cl.id_ws, cl.ai_trn, cl.number_queue as CANTIDAD_COLAS\r\n" + 
    		",E.CD_WS CODIGO_CAJA\r\n" + 
    		",t.id_trn, t.id_opr, t.ts_trn_bgn\r\n" + 
    		",(CASE WHEN coalesce(aop.cd_opr,'') = '' THEN 1 ELSE cast(aop.cd_opr as INTEGER) END) CODIGO_OPERADOR\r\n" +
    		",COALESCE(OP.NOMB_ACE,'NOT DEFINED') OPERADOR\r\n" +
    		",cast(an.notificar_alerta_superv as integer) notificar_alerta_superv\r\n" + 
    		"	,coalesce(e.tp_ws,7) ID_TIPO_CAJA \r\n" + 
    		"	,cast(PAC.VALOR as INTEGER) MINIMO_COLAS_PARAM \r\n"+
    		"from arts_ec.co_queue_dt cl\r\n" + 
    		"INNER JOIN ARTS_EC.AS_WS E ON E.ID_WS = CL.ID_WS\r\n" + 
    		"INNER JOIN ARTS_EC.PA_STR_RTL TD ON TD.ID_BSN_UN = E.ID_BSN_UN\r\n" + 
    		"inner JOIN saadmin.mn_tienda ta on ta.des_clave = td.cd_str_rt\r\n" +
			//CRUZAMOS CON LA ULTIMA TRANSACCION PARA OBTENER EL OPERADOR ASOCIADO A LA CAJA
			"left JOIN arts_ec.tr_trn t on " +
			"	t.id_bsn_un = cl.id_bsn_un " +
			"   and cl.id_ws = t.id_ws  " +
			"	and t.id_trn = (select max(id_trn) FROM arts_ec.tr_trn where " +
			"										id_bsn_un = cl.id_bsn_un and  id_ws = cl.id_ws and " +
			"										cast(cl.date_queue as date) = cast(ts_trn_bgn as date)) " +
			"	and cast(cl.date_queue as date) = cast(t.ts_trn_bgn as date)  " +

			"left join arts_ec.PA_OPR aop on t.id_opr = aop.id_opr\r\n" +
    		"LEFT JOIN operac_ec.op_operador OP ON LTRIM(OP.CC_OPERADOR,'0') = (CASE WHEN coalesce(aop.cd_opr,'') = '' THEN '1' ELSE ltrim(aop.cd_opr,'0') END)\r\n" +
    		"inner join colas_ec.al_tienda_notif_superv_local an --para procesar solo las que esten configuradas y bandera de proceso activa\r\n" + 
    		"						on an.codigo_tienda = td.cd_str_rt \r\n" + 
    		"						and an.id_tipo_caja = COALESCE(E.TP_WS,7)\r\n" + 
    		"						and an.codigo_alerta =  '3'\r\n" + 
    		"						and an.procesar_alerta = 1 \r\n"+
    		"INNER JOIN colas_ec.fn_consulta_parametros_al_notif_superv_local(TD.CD_STR_RT,coalesce(e.tp_ws,7),'3') PAC\r\n" + 
    		"	ON PAC.PARAMETRO = 'alertas.cantidad.clientes.cola.excedido' \r\n"+
    		"where cl.id_bsn_un = :idBsnUn \r\n" + 
    		"and cl.id_ws = :idWs \r\n" + 
    		"and cl.ai_trn = :aiTrn \r\n" + 
    		"and cast(cl.date_queue as date) = CURRENT_DATE \r\n"+
    		"AND cl.number_queue > cast(PAC.VALOR as INTEGER)"; 
    
    public static final String CONSULTAR_TRX_CON_CLIENTES_COLA_SUPERADO_LOCAL_V4 = "select \r\n" + 
    		"td.de_str_rt TIENDA\r\n" + 
    		",TA.IP_WS_NOTIF_SUPERV IP\r\n" + 
    		",cl.id_bsn_un, td.cd_str_rt CODIGO_TIENDA, cl.id_ws, cl.ai_trn, cl.number_queue as CANTIDAD_COLAS\r\n" + 
    		",E.CD_WS CODIGO_CAJA\r\n" + 
    		",t.id_trn, t.id_opr, t.ts_trn_bgn\r\n" + 
    		",(CASE WHEN coalesce(cl.cd_opr\\:\\:text,'') = '' THEN 1 ELSE cast(cl.cd_opr as INTEGER) END) CODIGO_OPERADOR\r\n" +
    		",COALESCE(OP.NOMB_ACE,'NOT DEFINED') OPERADOR\r\n" +
    		",cast(an.notificar_alerta_superv as integer) notificar_alerta_superv\r\n" + 
    		"	,coalesce(e.tp_ws,7) ID_TIPO_CAJA \r\n" + 
    		"	,cast(PAC.VALOR as INTEGER) MINIMO_COLAS_PARAM \r\n"+
    		"from arts_ec.co_queue_dt cl\r\n" + 
    		"INNER JOIN ARTS_EC.AS_WS E ON E.ID_WS = CL.ID_WS\r\n" + 
    		"INNER JOIN ARTS_EC.PA_STR_RTL TD ON TD.ID_BSN_UN = E.ID_BSN_UN\r\n" + 
    		"inner JOIN saadmin.mn_tienda ta on ta.des_clave = td.cd_str_rt\r\n" +
			//CRUZAMOS CON LA ULTIMA TRANSACCION PARA OBTENER EL OPERADOR ASOCIADO A LA CAJA
			"left JOIN arts_ec.tr_trn t on " +
			"	t.id_bsn_un = cl.id_bsn_un " +
			"   and cl.id_ws = t.id_ws  " +
			"	and t.id_trn = (select max(id_trn) FROM arts_ec.tr_trn where " +
			"										id_bsn_un = cl.id_bsn_un and  id_ws = cl.id_ws and " +
			"										cast(cl.date_queue as date) = cast(ts_trn_bgn as date)) " +
			"	and cast(cl.date_queue as date) = cast(t.ts_trn_bgn as date)  " +
			"--left join arts_ec.PA_OPR aop on cl.id_opr = aop.id_opr\r\n" +
    		"LEFT JOIN operac_ec.op_operador OP ON LTRIM(OP.CC_OPERADOR,'0') = (CASE WHEN coalesce(cl.cd_opr\\:\\:text,'') = '' THEN '1' ELSE ltrim(cl.cd_opr\\:\\:text,'0') END)\r\n" +
    		"inner join colas_ec.al_tienda_notif_superv_local an --para procesar solo las que esten configuradas y bandera de proceso activa\r\n" + 
    		"						on an.codigo_tienda = td.cd_str_rt \r\n" + 
    		"						and an.id_tipo_caja = COALESCE(E.TP_WS,7)\r\n" + 
    		"						and an.codigo_alerta =  '3'\r\n" + 
    		"	 					and an.procesar_alerta = 1 \r\n"+
    		"INNER JOIN colas_ec.fn_consulta_parametros_al_notif_superv_local(TD.CD_STR_RT,coalesce(e.tp_ws,7),'3') PAC\r\n" + 
    		"	ON PAC.PARAMETRO = 'alertas.cantidad.clientes.cola.excedido' \r\n"+
    		"where cl.id_bsn_un = :idBsnUn \r\n" + 
    		"and cl.id_ws = :idWs \r\n" + 
    		"and cl.ai_trn = :aiTrn \r\n" + 
    		"and cast(cl.date_queue as date) = CURRENT_DATE \r\n"+
    		"AND cl.number_queue > cast(PAC.VALOR as INTEGER)"; 
    
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
    public static final String OBTENER_PARAMETROS_ALERTAS_SUPERVISOR_EN_BD_LOCAL = "select parametro, valor from colas_ec.fn_consulta_parametros_al_notif_superv_local(?,?,?)";
    
    public static final String AL_NS_REGISTRO_AUDIT = "insert into colas_ec.al_notif_superv_audit\r\n" + 
    		"values ";
    
    public static final String OBTENER_IP_SUITE_CENTRAL = "select IP from saadmin.mn_tienda  where des_clave = 0";
    
    public static final String OBTENER_TITULO_ALERTA = "select VALOR_2 TITULO from saadmin.catalogo_detalle_central where id_catalogo = 2 AND CODIGO = ?";
    public static final String OBTENER_TITULO_ALERTA_LOCAL = "select VALOR_2 TITULO from saadmin.catalogo_detalle where id_catalogo = 2 AND CODIGO = ?";
    public static final String CONEXION_DB_LINK = "select public.dblink_connect(cast('dblink71' as text),cast('host=**HOST** dbname=**DBNAME** user=**USUARIO** password=**PASSWORD**' as text))";
    public static final String CHECK_DBLINK = "SELECT array_to_string(dblink_get_connections,',') from public.dblink_get_connections() where array_to_string(dblink_get_connections,',') like '%dblink71%' ";
}
